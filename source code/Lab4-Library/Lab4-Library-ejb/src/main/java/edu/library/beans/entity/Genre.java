package edu.library.beans.entity;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Genre implements ValidationBean
{
    
    private int id;
    
    @NotNull(message="Поле 'Название' должно быть задано")
    @Size(min=1, max=100, message="Поле 'Название' должно быть от 1 до 100 символов")
    private String name;
    
    private static final ValidatorFactory validationFactory
            = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = validationFactory.getValidator();

    public Genre(final int id, final String name)
    {
        this.id = id;
        this.name = name;
    }

    public int getId()
    {
        return id;
    }

    public void setId(final int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }
    
    @Override
    public String toString()
    {
        return "Genre{" + "id=" + id + ", name=" + name + '}';
    }

    @Override
    public Set<ConstraintViolation<Object>> validate()
    {
        return validator.validate((Object)this);
    }
    
}
