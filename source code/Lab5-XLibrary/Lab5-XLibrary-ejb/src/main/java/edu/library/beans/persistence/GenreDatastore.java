package edu.library.beans.persistence;

import edu.library.beans.entity.Genre;
import edu.library.exceptions.ValidationException;
import edu.library.exceptions.db.NoSuchEntityInDB;
import edu.library.exceptions.db.PersistException;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

/**
 * Объект для управления персистентным состоянием объекта Genre
 */
@Stateless
@LocalBean
public class GenreDatastore extends AbstractDatastore
{
    
    public static final String GENRE_ID = "id", GENRE_NAME = "name";
    
    private static final String SELECT_GENRES = "FROM Genre g";
    private static final String SELECT_GENRE = SELECT_GENRES + " WHERE g.id = :id";
    private static final String DELETE = "DELETE FROM Genre g WHERE g.id = :id";
    
    private static final String NO_SUCH_ENTITY_IN_DB = "В базе данных нет жанра с id = %d";
    private static final String CONSTRAINT_VIOLATION_EXCEPTION
            = "Жанр нельзя удалить, т.к. на него ссылаются одна или более книг";
    
    @PersistenceContext(unitName = "LibraryPU")
    private EntityManager entityManager;
    
    /**
     * Возвращает все объекты
     * @return
     * @throws edu.library.exceptions.db.PersistException
     */
    public List<Genre> getAll() throws PersistException
    {
        try
        {
            final List<Genre> genres = entityManager
                .createQuery(SELECT_GENRES, Genre.class)
                .getResultList();
            return genres;
        } catch (final PersistenceException ex)
        {
            throw new PersistException(getExceptionMessage(ex));
        }
    }
    
    /** 
     * Возвращает объект соответствующий записи.
     * @param id
     * @return 
     * @throws edu.library.exceptions.db.NoSuchEntityInDB 
     * @throws edu.library.exceptions.db.PersistException 
     */
    public Genre get(final Long id) throws NoSuchEntityInDB, PersistException
    {
        try
        {
            final Genre genre = entityManager
                .createQuery(SELECT_GENRE, Genre.class)
                .setParameter("id", id)
                .getSingleResult();
            return genre;
        } catch (final NoResultException ex)
        {
            throw new NoSuchEntityInDB(String.format(NO_SUCH_ENTITY_IN_DB, id));
        } catch (final PersistenceException ex)
        {
            throw new PersistException(getExceptionMessage(ex));
        }
    }
    
    /**
     * Создает новую запись. Изменяет primary key переданного объекта на сохранённый в БД.
     * @param genre
     * @throws edu.library.exceptions.db.PersistException
     * @throws edu.library.exceptions.ValidationException
     */
    public void create(final Genre genre) throws PersistException, ValidationException
    {
        validate(genre);
        
        try
        {
            entityManager.persist(genre);
        } catch (final PersistenceException ex)
        {
            throw new PersistException(getExceptionMessage(ex));
        }
    }
    
    /**
     * Сохраняет состояние объекта в базе данных
     * @param genre
     * @throws edu.library.exceptions.db.PersistException
     * @throws edu.library.exceptions.ValidationException
     */
    public void update(final Genre genre) throws PersistException, ValidationException
    {
        validate(genre);
        
        try
        {
            entityManager.merge(genre);
        } catch (final PersistenceException ex)
        {
            throw new PersistException(getExceptionMessage(ex));
        }
    }
    
    /**
     * Удаляет запись об объекте из базы данных
     * @param id
     * @throws edu.library.exceptions.db.PersistException
     */ 
    public void delete(final Long id) throws PersistException
    {
        if (id == null) return;

        try
        {
            entityManager.createQuery(DELETE)
                .setParameter("id", id)
                .executeUpdate();
        } catch (final PersistenceException ex)
        {
            if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException)
            {
                throw new PersistException(CONSTRAINT_VIOLATION_EXCEPTION);
            }
            
            throw new PersistException(getExceptionMessage(ex));
        }
    }

}
