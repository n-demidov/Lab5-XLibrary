package edu.library.persistence.dao;

import static javax.ejb.TransactionAttributeType.NOT_SUPPORTED;

import java.util.Collections;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;

import org.hibernate.HibernateException;
import org.hibernate.Query;

import edu.library.exceptions.persistence.NoSuchPersistenceException;
import edu.library.exceptions.persistence.PersistException;
import edu.library.exceptions.persistence.ValidException;
import edu.library.persistence.entity.Book;
import edu.library.persistence.filter.BooksFilter;
import edu.library.persistence.sorting.BooksSorting;

/**
 * Объект для управления персистентным состоянием объекта Book
 */
@Stateless
@LocalBean
public class BookDatastore extends AbstractDatastore
{
    
    private static final String SELECT_TEMPLATE = "FROM Book b";
    private static final String ORDER_BY_ID = " ORDER BY id";
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
    private static final String DELETE_BY_IDS = "DELETE FROM Book b WHERE b.id IN (:ids)";

    private static final String ANY_CHARS = "%";
    
    public BookDatastore()
    {
        super();
    }
    
    /**
     * Возвращает все объекты
     * @return
     * @throws edu.library.exceptions.db.PersistException
     */
    @TransactionAttribute(NOT_SUPPORTED)
    public List<Book> getAll() throws PersistException
    {
        return super.getAll(Book.class);
    }
    
    /** 
     * Возвращает объект соответствующий записи
     * @param id
     * @return 
     * @throws edu.library.exceptions.NoSuchPersistenceException.NoSuchEntityInDB 
     * @throws edu.library.exceptions.db.PersistException 
     */
    @TransactionAttribute(NOT_SUPPORTED)
    public Book get(final Long id) throws NoSuchPersistenceException, PersistException
    {
        return (Book) super.get(Book.class, id);
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
     * Flexible filter.
	 * If one of parameter specify as null - then no filtering for this parameter.
     */
    @TransactionAttribute(NOT_SUPPORTED)
    public List<Book> getByFilter(final BooksFilter filterParams,
    		final BooksSorting sortingParams) throws PersistException {
        // Forming SQL query
        String sql = SELECT_BY_FLEXIBLE_PHRASE;
        boolean isByFlexiblePhrase = false;
        boolean isByGenre = false;

        if (filterParams != null) {
        	if (filterParams.getSearchPhrase() != null && !filterParams.getSearchPhrase().trim().isEmpty())
            {
        		isByFlexiblePhrase = true;
                sql += WHERE_FLEXIBLE_PHRASE;
            }
        	
            if (filterParams.getGenrePK() != null && filterParams.getGenrePK() != 0)
            {
            	isByGenre = true;
                sql += WHERE_GENRE_ID;
            }
        }
        
        // Add sorting to query
        if (sortingParams != null) {
        	if (sortingParams.getField() != null) {
        		sql += ORDER_BY + sortingParams.getField().toString();
        	}

        	if (sortingParams.getOrder() == BooksSorting.SortOrder.Desc) {
        		sql += DESC_SORT_ORDER;
        	}
        }

        // Execute SQL query
        try
        {
            startOperation();
            final Query query = session.createQuery(sql);
            
            // Fill query with parameters
            if (isByFlexiblePhrase)
            {
                final String in = ANY_CHARS + filterParams.getSearchPhrase().trim().replace(" ", ANY_CHARS) + ANY_CHARS;
                query.setParameter("phrase", in);
            }
            if (isByGenre)
            {
                query.setParameter("genre_id", filterParams.getGenrePK());
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
     * @throws edu.library.exceptions.ValidException
     */
    @TransactionAttribute(NOT_SUPPORTED)
    public void create(final Book book) throws PersistException, ValidException
    {
        super.create(book);
    }
    
    /**
     * Сохраняет состояние объекта в базе данных
     * @param book
     * @throws edu.library.exceptions.db.PersistException
     * @throws edu.library.exceptions.ValidException
     */
    @TransactionAttribute(NOT_SUPPORTED)
    public void update(final Book book) throws PersistException, ValidException
    {
        super.update(book);
    }
    
    /**
     * Удаляет запись об объекте из базы данных
     * @param id
     * @throws edu.library.exceptions.db.PersistException
     */
    @TransactionAttribute(NOT_SUPPORTED)
    public void delete(final Long id) throws PersistException
    {
        super.delete(Book.class, id);
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
