package edu.library.servlets;

import edu.library.beans.entity.Book;
import edu.library.beans.dao.BookDAO;
import edu.library.beans.entity.Genre;
import edu.library.beans.dao.GenreDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "Books", urlPatterns ={"/books"})
public class BooksServlet extends HttpServlet
{
    
    private static final String ACTION = "action",
            DELETE_ACTION = "delete", COPY_ACTION = "copy";
    private static final String SEARCH_FILTER = "search", GENRE_FILTER = "genre",
            SORTING_FIELD = "sortField", SORTING_ORDER = "sortOrder", UP_SORTING_ORDER = "up";
    private static final String SELECTED_BOOKS = "param[]";
    private static final String ERROR_WHILE_OPERATIONS = "При выполнении операции возникла ошибка: %s";
    private static final String ERR_MSG = "errMsg";
    
    @EJB
    private BookDAO bookDAO;
    
    @EJB
    private GenreDAO genreDAO;
    
    private static final Map<String, BookDAO.SortBy> ORDER_VALUES;  // предопределённые значения сортировки списка книг
    
    static
    {
        ORDER_VALUES = new LinkedHashMap<>();
        ORDER_VALUES.put("ID", BookDAO.SortBy.ID);
        ORDER_VALUES.put("Название", BookDAO.SortBy.Name);
        ORDER_VALUES.put("Жанр", BookDAO.SortBy.Genre);
        ORDER_VALUES.put("Автор", BookDAO.SortBy.Author);
        ORDER_VALUES.put("Издатель", BookDAO.SortBy.Publisher);
        ORDER_VALUES.put("ISBN", BookDAO.SortBy.ISBN);
        ORDER_VALUES.put("Кол-во страниц", BookDAO.SortBy.PageCount);
        ORDER_VALUES.put("Описание", BookDAO.SortBy.Description);
    }

    /**
     * Выводит список книг по фильтру и сортировке, указанным в GET-параметрах
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
     * Обрабатывает действия пользователя со страницы списка книг.
     * После чего редиректит на страницу-источник.
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
            // Разбираем GET-параметры
            final String action = request.getParameter(ACTION);
            final String[] bookIdsString = request.getParameterValues(SELECTED_BOOKS);
            if (bookIdsString == null)
            {
                renderPage(request, response);
                return;
            }
            
            int[] bookIds = new int[bookIdsString.length];
            for(int i = 0; i < bookIdsString.length; i++)
            {
               bookIds[i] = Integer.parseInt(bookIdsString[i]);
            }
            
            // Выполняем действие
            if (DELETE_ACTION.equals(action))
            {
                bookDAO.delete(bookIds);
            } else if (COPY_ACTION.equals(action))
            {
                bookDAO.copy(bookIds);
            }
        } catch (final SQLException ex)
        {
            Logger.getLogger(BooksServlet.class.getName()).log(Level.SEVERE, null, ex);
            request.setAttribute(ERR_MSG, String.format(ERROR_WHILE_OPERATIONS, ex.getLocalizedMessage()));
        }
        
        // Отрисовываем страницу
        renderPage(request, response);
    }

    /**
     * Returns a short description of the servlet.
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
            final Integer genreFilter
                    = (genreFilterParam == null || genreFilterParam.length() == 0)
                    ? null : Integer.parseInt(genreFilterParam);
            
            
            final List<Genre> genres = genreDAO.getAll();
            
            final List<Book> books = bookDAO.getByFilter(
                    searchFilter,
                    genreFilter,
                    getSorting(sortingField),
                    UP_SORTING_ORDER.equals(sortingOrder)
                            ? BookDAO.SortOrder.Desc : BookDAO.SortOrder.Asc);
            
            request.setAttribute("books", books);
            request.setAttribute("genres", genres);
            request.setAttribute("ORDER_VALUES", ORDER_VALUES.keySet());
            request.setAttribute("fullURI", getFullURI(request));
            
            request.getRequestDispatcher("/books.jsp").forward(request, response);
        } catch (final SQLException ex)
        {
            Logger.getLogger(BooksServlet.class.getName()).log(Level.SEVERE, null, ex);
            request.setAttribute(ERR_MSG, String.format(ERROR_WHILE_OPERATIONS, ex.getLocalizedMessage()));
        }
    }
    
    // Возвращает сортировку
    private BookDAO.SortBy getSorting(final String inputSortField)
    {
        BookDAO.SortBy field = ORDER_VALUES.getOrDefault(inputSortField, null);
        if (field == null) field = BookDAO.SortBy.ID;
        return field;
    }
    
    // Возвращает полный uri страницы
    private String getFullURI(final HttpServletRequest request)
    {
        return request.getScheme() + "://" + request.getServerName() + 
            ("http".equals(request.getScheme()) && request.getServerPort() == 80
             || "https".equals(request.getScheme()) && request.getServerPort() == 443 ?
                "" : ":" + request.getServerPort() ) + request.getRequestURI() +
            (request.getQueryString() != null ? "?" + request.getQueryString() : "");
    }
    
}
