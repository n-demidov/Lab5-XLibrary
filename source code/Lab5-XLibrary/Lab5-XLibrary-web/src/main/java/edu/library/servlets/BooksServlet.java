package edu.library.servlets;

import edu.library.Constants;
import edu.library.beans.persistence.GenreDatastore;
import edu.library.beans.xml.converter.BooksXMLporter;
import edu.library.beans.entity.Book;
import edu.library.beans.entity.Genre;
import edu.library.beans.persistence.BookDatastore;
import edu.library.exceptions.db.PersistException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

@WebServlet(name = "Books", urlPatterns =
{
    "/books"
})
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 1, // 1MB
        maxFileSize = 1024 * 1024 * 1, // 1MB
        maxRequestSize = 1024 * 1024 * 2)    // 2MB
public class BooksServlet extends HttpServlet
{

    private static final String ACTION = "action",
            DELETE_ACTION = "delete", COPY_ACTION = "copy",
            EXPORT_ACTION = "export-xml", SHOW_ACTION = "show";
    private static final String SEARCH_FILTER = "search", GENRE_FILTER = "genre",
            SORTING_FIELD = "sortField", SORTING_ORDER = "sortOrder", UP_SORTING_ORDER = "up";
    private static final String SELECTED_BOOKS = "param[]";
    private static final String ERROR_WHILE_OPERATIONS = "При выполнении операции возникла ошибка: %s";
    private static final String ERR_MSG = "errMsg";
    private static final String EXPORT_FILE_NAME = "books.xml";
    private static final String FORM_UPLOAD_XML_IMPORT_FILE = "import-xml-file";
    private static final String IMPORT_ERROR = "Во время операции импорта книг возникла ошибка. Проверьте корректность xml-файла";
    private static final String IMPORT_INFO_LIST = "importInfoList";

    @EJB
    private BookDatastore bookDatastore;

    @EJB
    private GenreDatastore genreDatastore;

    @EJB
    private BooksXMLporter converter;

    private static final Map<String, BookDatastore.SortBy> ORDER_VALUES;  // предопределённые значения сортировки списка книг

    static
    {
        ORDER_VALUES = new LinkedHashMap<>();
        ORDER_VALUES.put("ID", BookDatastore.SortBy.ID);
        ORDER_VALUES.put("Название", BookDatastore.SortBy.Name);
        ORDER_VALUES.put("Жанр", BookDatastore.SortBy.Genre);
        ORDER_VALUES.put("Автор", BookDatastore.SortBy.Author);
        ORDER_VALUES.put("Издатель", BookDatastore.SortBy.Publisher);
        ORDER_VALUES.put("ISBN", BookDatastore.SortBy.ISBN);
        ORDER_VALUES.put("Кол-во страниц", BookDatastore.SortBy.PageCount);
        ORDER_VALUES.put("Описание", BookDatastore.SortBy.Description);
    }

    /**
     * Выводит список книг по фильтру и сортировке, указанным в GET-параметрах
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        renderPage(request, response);
    }

    /**
     * Обрабатывает действия пользователя со страницы списка книг. После чего
     * редиректит на страницу-источник.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        try
        {
            response.setCharacterEncoding("UTF-8");
            // Разбираем GET-параметры
            final String action = request.getParameter(ACTION);
            final String[] bookIdsString = request.getParameterValues(SELECTED_BOOKS);

            // Если это запрос на импорт книг из XML - обрабатываем
            if (isMultipartFormData(request))
            {
                processImportXML(request, response);
//                return;
            } else
            {

                if (bookIdsString == null)
                {
                    renderPage(request, response);
                    return;
                }

                // Получаем массив id книг, которые выбрал пользователь
                final List<Long> bookIds = new ArrayList<>(bookIdsString.length);
                for (int i = 0; i < bookIdsString.length; i++)
                {
                    bookIds.add(Long.parseLong(bookIdsString[i]));
                }

                // Выполняем действие
                if (DELETE_ACTION.equals(action))
                {
                    bookDatastore.delete(bookIds);
                } else if (COPY_ACTION.equals(action))
                {
                    bookDatastore.copy(bookIds);
                } else if (EXPORT_ACTION.equals(action))
                {
                    exportXMLRequest(response, bookIds);
                    return;
                } else if (SHOW_ACTION.equals(action))
                {
                    XSLTprocessing(response, bookIds);
                    return;
                }
            }
        } catch (final PersistException ex)
        {
            Logger.getLogger(BooksServlet.class.getName()).log(Level.SEVERE, null, ex);
            request.setAttribute(ERR_MSG, String.format(ERROR_WHILE_OPERATIONS, ex.getLocalizedMessage()));
        }

        // Отрисовываем страницу
        renderPage(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }

    // Отрисовывает страницу
    private void renderPage(final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            request.setCharacterEncoding("UTF-8");

            // Разбираем GET-параметры
            final String searchFilter = request.getParameter(SEARCH_FILTER);
            final String genreFilterParam = request.getParameter(GENRE_FILTER);
            final String sortingField = request.getParameter(SORTING_FIELD);
            final String sortingOrder = request.getParameter(SORTING_ORDER);
            final Long genreFilter
                    = (genreFilterParam == null || genreFilterParam.length() == 0)
                            ? null : Long.parseLong(genreFilterParam);

            final List<Genre> genres = genreDatastore.getAll();
            final List<Book> books = bookDatastore.getByFilter(
                    searchFilter,
                    genreFilter,
                    getSorting(sortingField),
                    UP_SORTING_ORDER.equals(sortingOrder)
                    ? BookDatastore.SortOrder.Desc : BookDatastore.SortOrder.Asc);

            request.setAttribute("books", books);
            request.setAttribute("genres", genres);
            request.setAttribute("ORDER_VALUES", ORDER_VALUES.keySet());
            request.setAttribute("fullURI", getFullURI(request));

            request.getRequestDispatcher("/books.jsp").forward(request, response);
        } catch (final PersistException ex)
        {
            Logger.getLogger(BooksServlet.class.getName()).log(Level.SEVERE, null, ex);
            request.setAttribute(ERR_MSG, String.format(ERROR_WHILE_OPERATIONS, ex.getLocalizedMessage()));
        }
    }

    // Возвращает сортировку
    private BookDatastore.SortBy getSorting(final String inputSortField)
    {
        BookDatastore.SortBy field = ORDER_VALUES.getOrDefault(inputSortField, null);
        if (field == null)
        {
            field = BookDatastore.SortBy.ID;
        }
        return field;
    }

    // Обрабатывает запрос пользователя на экспорт книг в XML
    private void exportXMLRequest(final HttpServletResponse response,
            final List<Long> bookIds) throws IOException
    {
        final String booksXml = converter.exportBooks(bookIds);

        try (final PrintWriter out = response.getWriter())
        {
            response.setHeader("Content-Type", "text/xml");
            response.setHeader("Content-Description", "File Transfer");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                    String.format("attachment; filename=\"%s\"", EXPORT_FILE_NAME));

            response.setHeader("Content-Transfer-Encoding", "binary");
            response.setHeader("Connection", "Keep-Alive");
            response.setHeader("Expires", "0");
            response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
            response.setHeader("Pragma", "public");
            response.setHeader("Content-Length", String.valueOf(booksXml));
            //response.setContentLength(booksXml.length());

            out.write(booksXml);
        }
    }

    // Обрабатывает запрос пользователя на импорт XML
    private void processImportXML(final HttpServletRequest request,
            final HttpServletResponse response) throws IOException, ServletException
    {
        try
        {
            System.out.println("+++ processImportXML");
            System.out.println("");

            final Part xmlFile = request.getPart(FORM_UPLOAD_XML_IMPORT_FILE);
            final List<String> importInfoList = converter.importBooks(xmlFile.getInputStream());
            request.setAttribute(IMPORT_INFO_LIST, importInfoList);

            System.out.println("");
            System.out.println("+++ end of import XML");
            System.out.println("");
        } catch (final JAXBException ex)
        {
            Logger.getLogger(BooksServlet.class.getName()).log(Level.WARNING, null, ex);

            request.setAttribute(ERR_MSG, IMPORT_ERROR);
            renderPage(request, response);
        }
        renderPage(request, response);
    }

    private void XSLTprocessing(final HttpServletResponse response,
            final List<Long> bookIds)
    {

        final String booksXml = converter.exportBooks(bookIds);

        try (final PrintWriter out = response.getWriter())
        {
            TransformerFactory factory = TransformerFactory.newInstance();

            Source xslt = new StreamSource(getServletContext().getResourceAsStream("/books.xsl"));
            Transformer transformer = factory.newTransformer(xslt);

            Source text = new StreamSource(new StringReader(booksXml));
            transformer.transform(text, new StreamResult(out));

        } catch (IOException | TransformerException ex)
        {
            Logger.getLogger(BooksServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Check is request with enctype multipart/form-data
    private boolean isMultipartFormData(final HttpServletRequest request)
    {
        final String MULTIPART_FORM_DATA = "multipart/form-data";
        return (request.getContentType() != null
                && request.getContentType().toLowerCase()
                .indexOf(MULTIPART_FORM_DATA) > -1);
    }

    // Возвращает полный uri страницы
    private String getFullURI(final HttpServletRequest request)
    {
        return request.getScheme() + "://" + request.getServerName()
                + ("http".equals(request.getScheme()) && request.getServerPort() == 80
                || "https".equals(request.getScheme()) && request.getServerPort() == 443
                ? "" : ":" + request.getServerPort()) + request.getRequestURI()
                + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
    }

}
