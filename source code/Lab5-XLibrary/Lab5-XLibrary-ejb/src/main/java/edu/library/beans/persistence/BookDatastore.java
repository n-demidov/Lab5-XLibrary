package edu.library.beans.persistence;

import edu.library.beans.entity.Book;
import edu.library.exceptions.ValidationException;
import edu.library.exceptions.db.NoSuchEntityInDB;
import edu.library.exceptions.db.PersistException;
import java.util.Collections;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import static javax.ejb.TransactionAttributeType.NOT_SUPPORTED;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolationException;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.TransactionException;

/**
 * Объект для управления персистентным состоянием объекта Book
 */
@Stateless
@LocalBean
public class BookDatastore extends AbstractDatastore
{
    
    public static final String BOOK_ID = "id", BOOK_NAME = "name",
            BOOK_AUTHOR = "author", BOOK_PUBLISHER = "publisher",
            BOOK_ISBN = "isbn", BOOK_PAGE_COUNT = "page_count",
            BOOK_DESCRIPTION = "description";
    public static final String BOOK_GENRE_ID = "genre_id", BOOK_GENRE_NAME = "genre_name";

    private static final String SELECT_TEMPLATE = "FROM Book b";
    private static final String ORDER_BY_ID = " ORDER BY id";
    private static final String SELECT_BOOK = SELECT_TEMPLATE + " WHERE b.id = :id";
    private static final String SELECT_ALL_BOOKS = SELECT_TEMPLATE + ORDER_BY_ID;
    private static final String SELECT_BY_IDS = SELECT_TEMPLATE + " WHERE b.id IN (:ids)"  + ORDER_BY_ID;

    private static final String SELECT_BY_FLEXIBLE_PHRASE = SELECT_TEMPLATE
            + " WHERE 1 = 1";
    private static final String WHERE_FLEXIBLE_PHRASE
            = " AND (b.name LIKE :phrase OR b.author LIKE :phrase)";
    private static final String WHERE_GENRE_ID
            = " AND (b.genre.id = :genre_id)";
    private static final String ORDER_BY = " ORDER BY ", DESC_SORT_ORDER = " DESC";
    
    private static final String COPY_STRING = "CONCAT('copied at ', sysdate(6), '; random-string-', RAND())";
    private static final String COPY_BOOKS
            = "INSERT INTO book (name, genre_id, author, publisher, isbn, page_count, description)"
            + " SELECT name, genre_id, author, publisher,"
            + COPY_STRING + ","
            + " page_count, description"
            + " FROM book"
            + " WHERE id IN (:ids)";
    private static final String DELETE = "DELETE FROM Book b WHERE b.id = :id";
    private static final String DELETE_BY_IDS = "DELETE FROM Book b WHERE b.id IN (:ids)";

    private static final String ANY_CHARS = "%";
    
    private static final String NO_SUCH_ENTITY_IN_DB = "В базе данных нет книги с id = %d";
    
    // <editor-fold defaultstate="collapsed" desc="Enums for sorting results">
    /**
     * Параметры сортировки книг
     */
    public enum SortBy
    {
        ID ("b.id"),
        Name ("b.name"),
        Genre ("genre.name"),
        Author ("b.author"),
        Publisher ("b.publisher"),
        ISBN ("b.isbn"),
        PageCount ("b.pageCount"),
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
     * Возвращает все объекты
     * @return
     * @throws edu.library.exceptions.db.PersistException
     */
    @TransactionAttribute(NOT_SUPPORTED)
    public List<Book> getAll() throws PersistException
    {
        try
        {
            startOperation();
            
            final List<Book> books = session.createQuery(SELECT_ALL_BOOKS).list();
            
            tx.commit();
            return books;
        } catch (final HibernateException ex)
        {
            tx.rollback();
            throw new PersistException(getExceptionMessage(ex));
        } finally
        {
            session.close();
        }
    }
    
    /** 
     * Возвращает объект соответствующий записи
     * @param id
     * @return 
     * @throws edu.library.exceptions.db.NoSuchEntityInDB 
     * @throws edu.library.exceptions.db.PersistException 
     */
    @TransactionAttribute(NOT_SUPPORTED)
    public Book get(final Long id) throws NoSuchEntityInDB, PersistException
    {
        try
        {
            startOperation();
            
            final Book book = (Book) session
                    .createQuery(SELECT_BOOK)
                    .setParameter("id", id)
                    .uniqueResult();
            tx.commit();
            
            if (book == null)
            {
                throw new NoSuchEntityInDB(String.format(NO_SUCH_ENTITY_IN_DB, id));
            }
            
            return book;
        } catch (final HibernateException ex)
        {
            tx.rollback();
            throw new PersistException(getExceptionMessage(ex));
        } finally
        {
            session.close();
        }
    }
    
    /**
     * Возвращает список объектов по их id
     * @param ids
     * @return 
     * @throws PersistException 
     */
    @TransactionAttribute(NOT_SUPPORTED)
    public List<Book> get(final List<Long> ids) throws PersistException
    {
        if (ids == null) return Collections.emptyList();

        try
        {
            startOperation();

            final List<Book> books = session.createQuery(SELECT_BY_IDS)
                .setParameterList("ids", ids)
                .list();
            
            tx.commit();
            return books;
        } catch (final HibernateException ex)
        {
            tx.rollback();
            throw new PersistException(getExceptionMessage(ex));
        } finally
        {
            session.close();
        }
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
     * @throws edu.library.exceptions.db.PersistException 
     */
    @TransactionAttribute(NOT_SUPPORTED)
    public List<Book> getByFilter(final String filterPhrase, final Long filterGenreId,
            final SortBy sort, final SortOrder sortOrder) throws PersistException
    {
        assert sort != null;
        
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
        
        // Выполняем SQL-запрос
        try
        {
            startOperation();
            final Query query = session.createQuery(sql);
            
            // Заполняем SQL-запрос параметрами
            if (isByFlexiblePhrase)
            {
                final String in = ANY_CHARS + filterPhrase.trim().replace(" ", ANY_CHARS) + ANY_CHARS;
                query.setParameter("phrase", in);
            }
            if (isByGenre)
            {
                query.setParameter("genre_id", filterGenreId);
            }
            
            final List<Book> books = query.list();
            
            tx.commit();
            return books;
        } catch (final HibernateException ex)
        {
            tx.rollback();
            throw new PersistException(getExceptionMessage(ex));
        } finally
        {
            session.close();
        }
    }
    
    /**
     * Создает новую запись. Изменяет primary key переданного объекта на сохранённый в БД.
     * @param book
     * @throws edu.library.exceptions.db.PersistException
     * @throws edu.library.exceptions.ValidationException
     */
    @TransactionAttribute(NOT_SUPPORTED)
    public void create(final Book book) throws PersistException, ValidationException
    {
        validate(book);
        
        try
        {
            startOperation();
            session.save(book);
            tx.commit();
        } catch (final HibernateException ex)
        {
            tx.rollback();
            throw new PersistException(getExceptionMessage(ex));
        } finally
        {
            session.close();
        }
    }
    
    /**
     * Сохраняет состояние объекта в базе данных
     * @param book
     * @throws edu.library.exceptions.db.PersistException
     * @throws edu.library.exceptions.ValidationException
     */
    @TransactionAttribute(NOT_SUPPORTED)
    public void update(final Book book) throws PersistException, ValidationException
    {
        validate(book);

        try
        {
            startOperation();
            session.update(book);
            tx.commit();
        } catch (final HibernateException | ConstraintViolationException ex)
        {
            tx.rollback();
            throw new PersistException(getExceptionMessage(ex));
        } finally
        {
            session.close();
        }
    }
    
    /**
     * Удаляет запись об объекте из базы данных
     * @param id
     * @throws edu.library.exceptions.db.PersistException
     */
    @TransactionAttribute(NOT_SUPPORTED)
    public void delete(final Long id) throws PersistException
    {
        if (id == null) return;

        try
        {
            startOperation();
            session.createQuery(DELETE)
                .setParameter("id", id)
                .executeUpdate();
            tx.commit();
        } catch (final HibernateException ex)
        {
            tx.rollback();
            throw new PersistException(getExceptionMessage(ex));
        } finally
        {
            session.close();
        }
    }
    
    /**
     * Удаляет запись об объектах из базы данных
     * @param ids
     * @throws edu.library.exceptions.db.PersistException
     */
    @TransactionAttribute(NOT_SUPPORTED)
    public void delete(final List<Long> ids) throws PersistException
    {
        if (ids == null) return;

        try
        {
            startOperation();
            session.createQuery(DELETE_BY_IDS)
                .setParameterList("ids", ids)
                .executeUpdate();
            tx.commit();
        } catch (final HibernateException ex)
        {
            tx.rollback();
            throw new PersistException(getExceptionMessage(ex));
        } finally
        {
            session.close();
        }
    }
    
    /**
     * Копирует записи.
     * К уникальному столбцу ISBN добавится рандомная строка.
     * @param ids 
     * @throws edu.library.exceptions.db.PersistException 
     */
    @TransactionAttribute(NOT_SUPPORTED)
    public void copy(final List<Long> ids) throws PersistException
    {
        if (ids == null) return;

        try
        {
            startOperation();
            session.createSQLQuery(COPY_BOOKS)
                .setParameterList("ids", ids)
                .executeUpdate();
            tx.commit();
        } catch (final HibernateException ex)
        {
            tx.rollback();
            throw new PersistException(getExceptionMessage(ex));
        } finally
        {
            session.close();
        }
    }
    
}
