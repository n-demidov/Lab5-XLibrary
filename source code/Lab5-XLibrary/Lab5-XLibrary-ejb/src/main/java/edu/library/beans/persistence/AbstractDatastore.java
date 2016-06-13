package edu.library.beans.persistence;

import edu.library.exceptions.ValidationException;
import edu.library.exceptions.db.NoSuchEntityInDB;
import edu.library.exceptions.db.PersistException;
import java.util.List;
import javax.ejb.TransactionAttribute;
import static javax.ejb.TransactionAttributeType.NOT_SUPPORTED;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import javax.validation.ConstraintViolationException;

public abstract class AbstractDatastore
{
    
    private static final String SELECT_TEMPLATE = "FROM %s x";
    private static final String ORDER_BY_ID = " ORDER BY id";
    private static final String SELECT_ALL = SELECT_TEMPLATE + ORDER_BY_ID;
    private static final String SELECT_BY_ID = "FROM %s x WHERE x.id = :id";
    private static final String NO_SUCH_ENTITY_IN_DB = "В базе данных нет объекта %s с id = %d";
    private static final String DELETE_BY_ID = "DELETE FROM %s x WHERE x.id = :id";
    
    private static final ValidatorFactory validationFactory
            = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = validationFactory.getValidator();
    
    private final SessionFactory sessionFactory = HibernateSessionFactory.getSessionFactory();
    
    protected Session session;
    protected Transaction tx;
    
    /**
     * Подготавливает сессию и транзакцию
     * @throws HibernateException 
     */
    protected void startOperation() throws HibernateException
    {
        session = sessionFactory.openSession();
        tx = session.beginTransaction();
    }
    
    /**
     * Возвращает все объекты
     * @param <T>
     * @param clazz
     * @return
     * @throws edu.library.exceptions.db.PersistException
     */
    @TransactionAttribute(NOT_SUPPORTED)
    protected <T> List<T> getAll(final Class clazz) throws PersistException
    {
        try
        {
            startOperation();
            final List<T> objs = session.createQuery(String.format(
                    SELECT_ALL,
                    clazz.getName()))
                    .list();
            tx.commit();
            return objs;
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
     * @param clazz
     * @param id
     * @return 
     * @throws edu.library.exceptions.db.NoSuchEntityInDB 
     * @throws edu.library.exceptions.db.PersistException 
     */
    @TransactionAttribute(NOT_SUPPORTED)
    protected Object get(final Class clazz, final Long id) throws NoSuchEntityInDB, PersistException
    {
        try
        {
            startOperation();
            final Object obj = session
                    .createQuery(String.format(
                            SELECT_BY_ID,
                            clazz.getName()))
                    .setParameter("id", id)
                    .uniqueResult();
            tx.commit();
            
            if (obj == null)
            {
                throw new NoSuchEntityInDB(String.format(
                        NO_SUCH_ENTITY_IN_DB,
                        clazz.getName(),
                        id));
            }
            
            return obj;
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
     * @param obj
     * @throws edu.library.exceptions.db.PersistException
     * @throws edu.library.exceptions.ValidationException
     */
    @TransactionAttribute(NOT_SUPPORTED)
    protected void create(final Object obj) throws PersistException, ValidationException
    {
        validate(obj);
        
        try
        {
            startOperation();
            session.save(obj);
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
     * Обновляет состояние объекта в базе данных
     * @param obj
     * @throws edu.library.exceptions.db.PersistException
     * @throws edu.library.exceptions.ValidationException
     */
    @TransactionAttribute(NOT_SUPPORTED)
    protected void update(final Object obj) throws PersistException, ValidationException
    {
        validate(obj);

        try
        {
            startOperation();
            session.update(obj);
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
     * @param clazz
     * @param id
     * @throws edu.library.exceptions.db.PersistException
     */
    @TransactionAttribute(NOT_SUPPORTED)
    protected void delete(final Class clazz, final Long id) throws PersistException
    {
        if (id == null) return;

        try
        {
            startOperation();
            session.createQuery(String.format(
                    DELETE_BY_ID,
                    clazz.getName()))
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
     * Выбрасывает исключение PersistException по переданному исключению
     * @param ex
     * @return 
     * @throws PersistException 
     */
    protected String getExceptionMessage(final Throwable ex) throws PersistException
    {
        String causeString;
        final Throwable cause = ex.getCause();
        if (cause != null)
        {
            causeString = cause.getLocalizedMessage();
        } else
        {
            causeString = ex.getLocalizedMessage();
        }

        return causeString;
    }
    
    /**
     * Проверяет данные на валидность - показывает первую ошибку 
     * @param object
     * @throws edu.library.exceptions.ValidationException 
     */
    private void validate(final Object object) throws ValidationException
    {
        for (final ConstraintViolation<Object> cv : validator.validate(object))
        {
            throw new ValidationException(cv.getMessage());
        }
    }
    
}
