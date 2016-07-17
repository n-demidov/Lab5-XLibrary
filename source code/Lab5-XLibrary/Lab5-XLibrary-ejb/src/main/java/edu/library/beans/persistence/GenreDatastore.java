package edu.library.beans.persistence;

import edu.library.beans.entity.Genre;
import edu.library.exceptions.ValidationException;
import edu.library.exceptions.db.NoSuchEntityInDB;
import edu.library.exceptions.db.PersistException;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import static javax.ejb.TransactionAttributeType.NOT_SUPPORTED;

/**
 * Объект для управления персистентным состоянием объекта Genre
 */
@Stateless
@LocalBean
public class GenreDatastore extends AbstractDatastore
{
    
    private static final String DELETE = "DELETE FROM Genre g WHERE g.id = :id";
    
    private static final String CONSTRAINT_ERR ="foreign key constraint fails";
    private static final String CONSTRAINT_VIOLATION_EXCEPTION
            = "Жанр нельзя удалить, т.к. на него ссылаются одна или более книг";
    
    public GenreDatastore()
    {
        super();
    }
    
    /**
     * Возвращает все объекты
     * @return
     * @throws edu.library.exceptions.db.PersistException
     */
    @TransactionAttribute(NOT_SUPPORTED)
    public List<Genre> getAll() throws PersistException
    {
        return super.getAll(Genre.class);
    }
    
    /** 
     * Возвращает объект соответствующий записи.
     * @param id
     * @return 
     * @throws edu.library.exceptions.db.NoSuchEntityInDB 
     * @throws edu.library.exceptions.db.PersistException 
     */
    @TransactionAttribute(NOT_SUPPORTED)
    public Genre get(final Long id) throws NoSuchEntityInDB, PersistException
    {
        return (Genre) super.get(Genre.class, id);
    }
    
    /**
     * Создает новую запись. Изменяет primary key переданного объекта на сохранённый в БД.
     * @param genre
     * @throws edu.library.exceptions.db.PersistException
     * @throws edu.library.exceptions.ValidationException
     */
    @TransactionAttribute(NOT_SUPPORTED)
    public void create(final Genre genre) throws PersistException, ValidationException
    {
        super.create(genre);
    }
    
    /**
     * Сохраняет состояние объекта в базе данных
     * @param genre
     * @throws edu.library.exceptions.db.PersistException
     * @throws edu.library.exceptions.ValidationException
     */
    @TransactionAttribute(NOT_SUPPORTED)
    public void update(final Genre genre) throws PersistException, ValidationException
    {
        super.update(genre);
    }
    
    /**
     * Удаляет запись об объекте из базы данных
     * @param id
     * @throws edu.library.exceptions.db.PersistException
     */
    @TransactionAttribute(NOT_SUPPORTED)
    public void delete(final Long id) throws PersistException
    {
        try
        {
            super.delete(Genre.class, id);
        } catch (final PersistException ex)
        {
            if (ex.getLocalizedMessage().contains(CONSTRAINT_ERR))
            {
                throw new PersistException(CONSTRAINT_VIOLATION_EXCEPTION);
            }
            throw new PersistException(getExceptionMessage(ex));
        }
    }

}
