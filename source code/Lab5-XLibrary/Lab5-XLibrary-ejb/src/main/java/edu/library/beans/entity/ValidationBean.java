package edu.library.beans.entity;

import edu.library.exceptions.ValidationException;
import java.util.Set;
import javax.validation.ConstraintViolation;

public interface ValidationBean
{
    
    /**
     * Проверяет объект на валидность - показывает первую ошибку 
     * @return 
     * @throws edu.library.exceptions.ValidationException 
     */
    public Set<ConstraintViolation<Object>> validate() throws ValidationException;
    
}
