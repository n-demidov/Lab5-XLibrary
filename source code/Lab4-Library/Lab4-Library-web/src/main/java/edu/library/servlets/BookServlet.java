package edu.library.servlets;

import edu.library.Constants;
import edu.library.beans.entity.Book;
import edu.library.beans.dao.BookDAO;
import edu.library.beans.entity.Genre;
import edu.library.beans.dao.GenreDAO;
import edu.library.exceptions.ParseIntException;
import edu.library.exceptions.ValidationException;
import edu.library.exceptions.db.NoSuchEntityInDB;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "Book", urlPatterns ={"/book"})
public class BookServlet extends HttpServlet
{

    private static final String PAGE_TYPE = "type", PAGE_TYPE_ADD = "add";
    
    private static final String ERR_EMPTY = " (незаполнено)";
    private static final String PAGE_COUNT_ERR = "Неправильный формат количества страниц";
    private static final String GENRE_ERR = "Неправильный формат жанра";
    private static final String GENRE_ID = "genre_id";
    private static final String SAVE_AND_STAY = "save_and_stay",
                SAVE_AND_ADD_ANOTHER = "save_and_add_another", DELETE = "delete";
    private static final String REDIRECT_BOOK_DETAILS = "book?id=", REDIRECT_ADD_BOOK = "book?type=add";
    private static final String BOOK_ID_ERR = "Неправильный формат id книги";
    
    @EJB
    private BookDAO bookDAO;
    
    @EJB
    private GenreDAO genreDAO;

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
                final int bookId = Integer.parseInt(request.getParameter("id"));
                book = bookDAO.get(bookId);
                isAddBook = false;
            }
        } catch (final NumberFormatException | SQLException ex)
        {
            java.util.logging.Logger.getLogger(BookServlet.class.getName()).log(Level.SEVERE, null, ex);
            request.setAttribute("errMsg", ex.getMessage());
        } catch (final NoSuchEntityInDB ex)
        {
            response.sendRedirect(Constants.REDIRECT_BOOKS_PAGE);
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
            final String bookIdString = request.getParameter(BookDAO.BOOK_ID);
            
            final int bookId = (bookIdString == null || bookIdString.isEmpty())
                    ? 0 : parseInt(bookIdString, BOOK_ID_ERR);
            isAddBook = (bookId == 0);
            
            // Если пользователь нажал удалить - удаляем книгу по id
            if (DELETE.equals(submitType))
            {
                bookDAO.delete(bookId);
                response.sendRedirect(Constants.REDIRECT_BOOKS_PAGE);
                return;
            }

            // Заполняем объект книги данными
            if (!isAddBook)
            {
                book = bookDAO.get(bookId);
            }
            book.setId(bookId);
            fillBookFromRequest(book, request);

            // Делаем операции
            if (isAddBook)
            {
                bookDAO.create(book);
            } else
            {
                bookDAO.update(book);
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
                response.sendRedirect(Constants.REDIRECT_BOOKS_PAGE);     // редирект на страницу списка книг
            }
        } catch (final NumberFormatException | ParseIntException | ValidationException | SQLException ex)
        {
            java.util.logging.Logger.getLogger(BookServlet.class.getName()).log(Level.SEVERE, null, ex);
            
            request.setAttribute("errMsg", ex.getMessage());
            forwardToJSP(request, response, book, isAddBook);
        } catch (final NoSuchEntityInDB ex)
        {
            response.sendRedirect(Constants.REDIRECT_BOOKS_PAGE);
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
            
            final List<Genre> genres = genreDAO.getAll();
            
            request.setAttribute("genres", genres);
            request.setAttribute("book", book);
            request.setAttribute("isAddBook", isAddBook);
            request.setAttribute("fullUrl", getFullURI(request));

            request.getRequestDispatcher("/book.jsp").forward(request, response);
        } catch (final SQLException ex)
        {
            java.util.logging.Logger.getLogger(BookServlet.class.getName()).log(Level.SEVERE, null, ex);
            
            request.setAttribute("errMsg", ex.getMessage());
        }
    }

    // Парсит число из строки
    private int parseInt(final String parsedString, final String errDescr) 
            throws ParseIntException
    {
        try
        {
            if (parsedString == null || parsedString.trim().isEmpty())
            {
                throw new ParseIntException(errDescr + ": " + ERR_EMPTY);
            }
            
            return Integer.parseInt(parsedString);
        } catch (final NumberFormatException ex)
        {
            throw new ParseIntException(errDescr + ": " + parsedString);
        }
    }

    // Заполняет экземпляр книги параметрами из запроса
    private void fillBookFromRequest(final Book book, final HttpServletRequest request)
            throws ValidationException
    {
        boolean isError = false;
        String error = "";

        book.setName(request.getParameter(BookDAO.BOOK_NAME).trim());
        book.setAuthor(request.getParameter(BookDAO.BOOK_AUTHOR).trim());
        book.setPublisher(request.getParameter(BookDAO.BOOK_PUBLISHER).trim());

        // Разбираем жанр
        try
        {
            final int genreId = parseInt(request.getParameter(GENRE_ID), GENRE_ERR);
            final Genre genre = genreDAO.get(genreId);
            book.setGenre(genre);
        } catch (final SQLException | NoSuchEntityInDB | ParseIntException ex)
        {
            isError = true;
            error = ex.getLocalizedMessage();
        }

        // Разбираем кол-во страниц
        try
        {
        book.setPageCount(parseInt(
                request.getParameter(BookDAO.BOOK_PAGE_COUNT), PAGE_COUNT_ERR));
        } catch (final ParseIntException ex)
        {
            isError = true;
            error = ex.getLocalizedMessage();
        }

        book.setIsbn(request.getParameter(BookDAO.BOOK_ISBN).trim());
        book.setDescription(request.getParameter(BookDAO.BOOK_DESCRIPTION).trim());

        // Если были ошибки
        if (isError)
        {
            throw new ValidationException(error);
        }
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
