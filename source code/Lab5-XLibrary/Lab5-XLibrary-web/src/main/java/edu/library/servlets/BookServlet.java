package edu.library.servlets;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.library.ServletConstants;
import edu.library.domain.BooksDomain;
import edu.library.domain.GenresDomain;
import edu.library.exceptions.ParseIntException;
import edu.library.exceptions.persistence.NoSuchPersistenceException;
import edu.library.exceptions.persistence.PersistException;
import edu.library.exceptions.persistence.ValidException;
import edu.library.persistence.entity.Book;
import edu.library.persistence.entity.Genre;

@WebServlet(name = "Book", urlPatterns ={"/book"})
public class BookServlet extends AbstractServlet
{

    private static final String PAGE_TYPE = "type", PAGE_TYPE_ADD = "add";
    
    private static final String PAGE_COUNT_ERR = "Неправильный формат количества страниц";
    private static final String GENRE_ERR = "Неправильный формат жанра";
    private static final String GENRE_ID = "genre_id";
    private static final String SAVE_AND_STAY = "save_and_stay",
                SAVE_AND_ADD_ANOTHER = "save_and_add_another", DELETE = "delete";
    private static final String REDIRECT_BOOK_DETAILS = "book?id=", REDIRECT_ADD_BOOK = "book?type=add";
    private static final String BOOK_ID_ERR = "Неправильный формат id книги";
    
    @EJB
    private BooksDomain booksDomain;
    
    @EJB
    private GenresDomain genresDomain;

    /**
     * Обрабатывает запросы пользователя на просмотр страницы редактирование\добавление книги
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        Book book = null;
        boolean isAddBook = true;
        try
        {
            final String pageType = request.getParameter(PAGE_TYPE);

            // Если в параметре запроса указано "add" - то создаём новую книгу, иначе - редактируем
            if (!PAGE_TYPE_ADD.equals(pageType))
            {
                // Во всех остальных случаях - edit
                final long bookId = Integer.parseInt(request.getParameter("id"));
                book = booksDomain.get(bookId);
                isAddBook = false;
            }
        } catch (final NoSuchPersistenceException | NumberFormatException ex)
        {
            java.util.logging.Logger.getLogger(BookServlet.class.getName()).log(Level.SEVERE, null, ex);
            response.sendRedirect(ServletConstants.REDIRECT_BOOKS_PAGE);
            return;
        } catch (final PersistException ex)
        {
            Logger.getLogger(BookServlet.class.getName()).log(Level.SEVERE, null, ex);
            request.setAttribute("errMsg", ex.getMessage());
        }

        forwardToJSP(request, response, book, isAddBook);
    }

    /**
     * Обрабатывает запросы пользователя на изменение - редактирование\добавление\удаление книги
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        
        Book book = new Book();
        boolean isAddBook = false;

        try
        {
            final String submitType = request.getParameter("submit_type");
            final String idString = request.getParameter(ServletConstants.BOOK_ID);
            final Long bookId = (idString == null || idString.isEmpty())
                    ? null : Long.parseLong(idString);
            isAddBook = (bookId == null);
            
            // Заполняем объект книги данными
            if (!isAddBook)
            {
                book = booksDomain.get(bookId);
            }
            book.setId(bookId);
            fillBookFromRequest(book, request);
            
            // Если пользователь нажал удалить - удаляем книгу по id
            if (DELETE.equals(submitType))
            {
                booksDomain.delete(bookId);
                response.sendRedirect(ServletConstants.REDIRECT_BOOKS_PAGE);
                return;
            }

            // Делаем операции
            if (isAddBook)
            {
                booksDomain.create(book);
            } else
            {
                booksDomain.update(book);
            }

            // Перенаправляем польз-ля на нужную ему страницу
            if (SAVE_AND_STAY.equals(submitType))
            {
                // перерисовываем страницу
                if (isAddBook)
                {
                    response.sendRedirect(REDIRECT_BOOK_DETAILS + book.getId());
                } else
                {
                    forwardToJSP(request, response, book, isAddBook);
                }
            } else if (SAVE_AND_ADD_ANOTHER.equals(submitType))
            {
                response.sendRedirect(REDIRECT_ADD_BOOK);       // редирект на страницу добавления
            } else
            {
                response.sendRedirect(ServletConstants.REDIRECT_BOOKS_PAGE);     // редирект на страницу списка книг
            }
        } catch (final NumberFormatException | ValidException | PersistException ex)
        {
            java.util.logging.Logger.getLogger(BookServlet.class.getName()).log(Level.SEVERE, null, ex);
            
            request.setAttribute("errMsg", ex.getMessage());
            forwardToJSP(request, response, book, isAddBook);
        } catch (final NoSuchPersistenceException ex)
        {
            response.sendRedirect(ServletConstants.REDIRECT_BOOKS_PAGE);
        }
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

    private void forwardToJSP(final HttpServletRequest request, final HttpServletResponse response,
            final Book book, final boolean isAddBook) throws ServletException, IOException
    {
        try
        {
            request.setCharacterEncoding("UTF-8");
            
            final List<Genre> genres = genresDomain.getAll();
            
            request.setAttribute("genres", genres);
            request.setAttribute("book", book);
            request.setAttribute("isAddBook", isAddBook);
            request.setAttribute("fullUrl", getFullURI(request));

            request.getRequestDispatcher("/book.jsp").forward(request, response);
        } catch (final PersistException ex)
        {
            java.util.logging.Logger.getLogger(BookServlet.class.getName()).log(Level.SEVERE, null, ex);
            
            request.setAttribute("errMsg", ex.getMessage());
        }
    }

    // Заполняет экземпляр книги параметрами из запроса
    private void fillBookFromRequest(final Book book, final HttpServletRequest request)
            throws ValidException
    {
        boolean isError = false;
        String error = "";

        book.setName(request.getParameter(ServletConstants.BOOK_NAME).trim());
        book.setAuthor(request.getParameter(ServletConstants.BOOK_AUTHOR).trim());
        book.setPublisher(request.getParameter(ServletConstants.BOOK_PUBLISHER).trim());

        // Разбираем жанр
        try
        {
            final Long genreId = (long) parseInt(request.getParameter(GENRE_ID), GENRE_ERR);
            final Genre genre = genresDomain.get(genreId);
            book.setGenre(genre);
        } catch (final NoSuchPersistenceException | ParseIntException | PersistException ex)
        {
            isError = true;
            error = ex.getLocalizedMessage();
        }

        // Разбираем кол-во страниц
        try
        {
        book.setPageCount(parseInt(
                request.getParameter(ServletConstants.BOOK_PAGE_COUNT), PAGE_COUNT_ERR));
        } catch (final ParseIntException ex)
        {
            isError = true;
            error = ex.getLocalizedMessage();
        }

        book.setIsbn(request.getParameter(ServletConstants.BOOK_ISBN).trim());
        book.setDescription(request.getParameter(ServletConstants.BOOK_DESCRIPTION).trim());

        // Если были ошибки
        if (isError)
        {
            throw new ValidException(error);
        }
    }
    
}
