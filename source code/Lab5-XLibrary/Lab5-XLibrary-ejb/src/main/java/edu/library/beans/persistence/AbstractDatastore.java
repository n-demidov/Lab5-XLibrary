package edu.library.beans.persistence;

import edu.library.exceptions.ValidationException;
import edu.library.exceptions.db.PersistException;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class AbstractDatastore
{
    
    private static final ValidatorFactory validationFactory
            = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = validationFactory.getValidator();
    
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
