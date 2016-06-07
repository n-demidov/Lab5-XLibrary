package edu.library.beans.dao;

import edu.library.beans.entity.Book;
import edu.library.beans.entity.Genre;
import edu.library.exceptions.ValidationException;
import edu.library.exceptions.db.NoSuchEntityInDB;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 * Объект для управления персистентным состоянием объекта Book
 */
@Stateless
@LocalBean
public class BookDAO extends AbstractDAO
{

    public static final String BOOK_ID = "id", BOOK_NAME = "name",
            BOOK_AUTHOR = "author", BOOK_PUBLISHER = "publisher",
            BOOK_ISBN = "isbn", BOOK_PAGE_COUNT = "page_count",
            BOOK_DESCRIPTION = "description";
    public static final String BOOK_GENRE_ID = "genre_id", BOOK_GENRE_NAME = "genre_name";

    private static final String SELECT_TEMPLATE
            = "SELECT b.id, b.name, b.genre_id, b.author, b.publisher, b.isbn,"
            + " b.page_count, b.description, g.name as " + BOOK_GENRE_NAME
            + " FROM book b"
            + " INNER JOIN genre g on b.genre_id = g.id";
    private static final String ORDER_BY_ID = " ORDER BY b.id";
    private static final String SELECT_ALL_BOOKS = SELECT_TEMPLATE + ORDER_BY_ID;
    private static final String SELECT_BOOK = SELECT_TEMPLATE + " WHERE b.id = ?";
    private static final String UPDATE_BOOK = "UPDATE book"
            + " SET name = ?, genre_id = ?, author = ?, publisher = ?"
            + ", isbn = ?, page_count = ?, description = ?"
            + " WHERE id = ?";
    private static final String SELECT_BY_FLEXIBLE_PHRASE = SELECT_TEMPLATE
            + " WHERE true";
    private static final String WHERE_FLEXIBLE_PHRASE
            = " AND (b.name LIKE ? OR b.author LIKE ?)";
    private static final String WHERE_GENRE_ID
            = " AND (b.genre_id = ?)";
    private static final String ORDER_BY = " ORDER BY ";
    
    private static final String COPY_STRING = "CONCAT('copied at ', sysdate(6), '; random-string-', RAND())";
    private static final String INSERT_BOOK_TEMPLATE
            = "INSERT INTO book (name, genre_id, author, publisher, isbn, page_count, description)";
    private static final String INSERT_BOOK
            = INSERT_BOOK_TEMPLATE + " VALUES(?, ?, ?, ?, ?, ?, ?)";
    private static final String COPY_BOOKS
            = INSERT_BOOK_TEMPLATE
            + " SELECT name, genre_id, author, publisher,"
            + COPY_STRING + ","
            + " page_count, description"
            + " FROM book"
            + " WHERE id IN (";
    private static final String DELETE_BOOK = "DELETE FROM book WHERE id = ?";
    private static final String DELETE_BOOKS = "DELETE FROM book WHERE id IN (";
    
    private static final String S_QUESTION = "?,";
    private static final String ANY_CHARS = "%";
    private static final String DESC_SORT_ORDER = " DESC";
    
    private static final String NO_SUCH_ENTITY_IN_DB = "В базе данных нет книги с id = %d";
    
    // <editor-fold defaultstate="collapsed" desc="Enums for sorting results">
    /**
     * Параметры сортировки книг
     */
    public enum SortBy
    {
        ID ("b.id"),
        Name ("b.name"),
        Genre ("g.name"),
        Author ("b.author"),
        Publisher ("b.publisher"),
        ISBN ("b.isbn"),
        PageCount ("b.page_count"),
        Description ("b.description");
        
        private final String name;       

        private SortBy(final String s)
        {
            name = s;
        }

        public boolean equalsName(final String otherName)
        {
            return (otherName == null) ? false : name.equals(otherName);
        }

        @Override
        public String toString()
        {
           return this.name;
        }
    }
    
    /**
     * Параметры сортировки книг (в обычном\в обратном)
     */
    public enum SortOrder
    {
        Asc,
        Desc
    }
    // </editor-fold>
    
    /**
     * Возвращает список объектов соответствующих всем записям в базе данных
     * @return 
     * @throws java.sql.SQLException 
     */
    public List<Book> getAll() throws SQLException
    {
        final List<Book> books = new ArrayList<>();

        try (final PreparedStatement statement = connection.prepareStatement(SELECT_ALL_BOOKS);
             final ResultSet rs = statement.executeQuery())
        {
            while (rs.next())
            {
                books.add(getResultSetBook(rs));
            }
        }

        return books;
    }
    
    /**
     * Гибкий фильтр
     * Если параметры null - то для них фильтра нет
     * Если параметр filterGenreId null или 0, то фильтра для жанра не установлено
     * @param filterPhrase
     * @param filterGenreId
     * @param sort
     * @param sortOrder null (ASC), ASC or DESC values
     * @return
     * @throws SQLException 
     */
    public List<Book> getByFilter(final String filterPhrase, final Integer filterGenreId,
            final SortBy sort, final SortOrder sortOrder) throws SQLException
    {
        assert sort != null;
        
        final List<Book> books = new ArrayList<>();
        ResultSet rs = null;
        int i = 1;
        
        // Подготавливаем SQL-запрос
        String sql = SELECT_BY_FLEXIBLE_PHRASE;
        boolean isByFlexiblePhrase = false;
        boolean isByGenre = false;

        if (filterPhrase != null && !filterPhrase.trim().isEmpty())
        {
            isByFlexiblePhrase = true;
            sql += WHERE_FLEXIBLE_PHRASE;
        }
        if (filterGenreId != null && filterGenreId != 0)
        {
            isByGenre = true;
            sql += WHERE_GENRE_ID;
        }
        
        // Добавляем в запрос сортировку
        sql += ORDER_BY + sort.toString();
        
        // Добавляем прямой\обратный порядок сортировки
        if (sortOrder != null && sortOrder == SortOrder.Desc)
        {
            sql += DESC_SORT_ORDER;
        }
        
        try (final PreparedStatement statement = connection.prepareStatement(sql))
        {
            // Заполняем SQL-запрос параметрами
            if (isByFlexiblePhrase)
            {
                final String in = ANY_CHARS + filterPhrase.trim().replace(" ", ANY_CHARS) + ANY_CHARS;
                statement.setString(i++, in);
                statement.setString(i++, in);
            }
            if (isByGenre)
            {
                statement.setInt(i++, filterGenreId);
            }

            rs = statement.executeQuery();
            while (rs.next())
            {
                books.add(getResultSetBook(rs));
            }
        } finally
        {
            closeResultSet(rs);
        }

        return books;
    }
    
    /** 
     * Возвращает объект соответствующий записи. Поле Genre преобразуется в соответсвующий объект.
     * @param id
     * @return 
     * @throws java.sql.SQLException 
     * @throws edu.library.exceptions.db.NoSuchEntityInDB 
     */
    public Book get(final int id) throws SQLException, NoSuchEntityInDB
    {
        ResultSet rs = null;

        try (final PreparedStatement statement = connection.prepareStatement(SELECT_BOOK))
        {
            statement.setInt(1, id);
            
            rs = statement.executeQuery();
            
            if (rs.next())
            {
                return getResultSetBook(rs);
            } else
            {
                throw new NoSuchEntityInDB(String.format(NO_SUCH_ENTITY_IN_DB, id));
            }
        } finally
        {
            closeResultSet(rs);
        }
    }

    /**
     * Сохраняет состояние объекта в базе данных
     * @param book
     * @throws java.sql.SQLException
     * @throws edu.library.exceptions.ValidationException
     */
    public void update(final Book book) throws SQLException, ValidationException
    {
        validate(book);
        
        try (final PreparedStatement statement = connection.prepareStatement(UPDATE_BOOK))
        {
            statement.setString(1, String.valueOf(book.getName()));
            statement.setString(2, String.valueOf(book.getGenre().getId()));
            statement.setString(3, String.valueOf(book.getAuthor()));
            statement.setString(4, String.valueOf(book.getPublisher()));
            statement.setString(5, String.valueOf(book.getIsbn()));
            statement.setString(6, String.valueOf(book.getPageCount()));
            statement.setString(7, String.valueOf(book.getDescription()));
            statement.setInt(8, book.getId());

            statement.executeUpdate();
        }
    }
    
    /**
     * Создает новую запись. Изменяет primary key переданного объекта на сохранённый в БД.
     * @param book
     * @throws SQLException 
     * @throws edu.library.exceptions.ValidationException 
     */
    public void create(final Book book) throws SQLException, ValidationException
    {
        validate(book);
        
        ResultSet rs = null;
        try (final PreparedStatement statement = connection.prepareStatement(
                INSERT_BOOK, Statement.RETURN_GENERATED_KEYS))
        {
            statement.setString(1, book.getName());
            statement.setInt(2, book.getGenre().getId());
            statement.setString(3, book.getAuthor());
            statement.setString(4, book.getPublisher());
            statement.setString(5, book.getIsbn());
            statement.setInt(6, book.getPageCount());
            statement.setString(7, book.getDescription());

            statement.executeUpdate();
            
            rs = statement.getGeneratedKeys();
            rs.next();
            final int savedBookId = rs.getInt(1);
            book.setId(savedBookId);
        } finally
        {
            closeResultSet(rs);
        }
    }

    /**
     * Удаляет запись об объекте из базы данных
     * @param id
     * @throws java.sql.SQLException
     */ 
    public void delete(final int id) throws SQLException
    {
        try (final PreparedStatement statement = connection.prepareStatement(DELETE_BOOK))
        {
            statement.setInt(1, id);
            statement.execute();
        }
    }
    
    /**
     * Удаляет запись об объектах из базы данных
     * @param ids
     * @throws java.sql.SQLException
     */ 
    public void delete(final int[] ids) throws SQLException
    {
        doWithArray(ids, DELETE_BOOKS);
    }
    
    /**
     * Копирует записи.
     * К уникальному столбцу ISBN добавится рандомная строка.
     * @param ids
     * @throws SQLException 
     */
    public void copy(final int[] ids) throws SQLException
    {
        doWithArray(ids, COPY_BOOKS);
    }

    // Создаёт экземпляр класса Book из запроса к БД
    private Book getResultSetBook(final ResultSet rs) throws SQLException
    {
        final Genre genre = new Genre(
                rs.getInt(BOOK_GENRE_ID), 
                rs.getString(BOOK_GENRE_NAME));
        
        final Book book = new Book(
                rs.getInt(BOOK_ID),
                rs.getString(BOOK_NAME),
                genre,
                rs.getString(BOOK_AUTHOR),
                rs.getString(BOOK_PUBLISHER),
                rs.getString(BOOK_ISBN),
                rs.getInt(BOOK_PAGE_COUNT),
                rs.getString(BOOK_DESCRIPTION));
        
        return book;
    }

    /**
     * Применяет переданный SQL-запрос к массиву записей
     * @param ids
     * @param stratingSql
     * @throws SQLException 
     */
    private void doWithArray(final int[] ids, final String stratingSql) throws SQLException
    {
        if (ids == null || ids.length == 0) return;
        
        // Формируем prepared SQL-строку
        String sql = stratingSql + new String(new char[ids.length]).replace("\0", S_QUESTION);
        sql = sql.substring(0, sql.length() - 1);   // удаляем последний символ
        sql += ")";
        
        // Заполняем prepared SQL-строку значениями
        try (final PreparedStatement statement = connection.prepareStatement(sql))
        {
            for (int i = 0; i < ids.length; i++)
            {
                statement.setInt(i + 1, ids[i]);
            }
            statement.execute();
        }
    }
    
}
