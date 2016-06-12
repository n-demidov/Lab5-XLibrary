package edu.library.beans.persistence;

import edu.library.exceptions.ValidationException;
import edu.library.exceptions.db.PersistException;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class AbstractDatastore
{
    
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
     * Выбрасывает исключение PersistException
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
    protected void validate(final Object object) throws ValidationException
    {
        for (final ConstraintViolation<Object> cv : validator.validate(object))
        {
            throw new ValidationException(cv.getMessage());
        }
    }
    
}
