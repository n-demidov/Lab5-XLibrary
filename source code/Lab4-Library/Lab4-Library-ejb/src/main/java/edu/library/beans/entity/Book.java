package edu.library.beans.entity;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Book implements ValidationBean
{
    
    private int id;
    
    @NotNull(message="Поле 'Название' должно быть задано")
    @Size(min=1, max=60, message="Поле 'Название' должно быть от 1 до 60 символов")
    private String name;
    
    @NotNull(message="Поле 'Жанр' должно быть задано")
    private Genre genre;
    
    @NotNull(message="Поле 'Автор' должно быть задано")
    @Size(min=1, max=255, message="Поле 'Автор' должно быть от 1 до 255 символов")
    private String author;
    
    @NotNull(message="Поле 'Издатель' должно быть задано")
    @Size(min=1, max=255, message="Поле 'Издатель' должно быть от 1 до 255 символов")
    private String publisher;

    @NotNull(message="Поле 'ISBN' должно быть задано")
    @Size(min=1, max=100, message="Поле 'ISBN' должно быть от 1 до 100 символов")
    private String isbn;
    
    @Min(value=0, message="Поле 'Количество страниц' должно иметь корректный формат")
    @Max(value=1000000, message="Поле 'Количество страниц' должно иметь корректный формат")
    private int pageCount;
    
    @NotNull(message="Поле 'Описание' должно быть задано")
    @Size(max=500, message="Поле 'Описание' не должно превышать 500 символов")
    private String description;
    
    private static final ValidatorFactory validationFactory
            = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = validationFactory.getValidator();

    public Book(){}

    public Book(final int id, final String name, final Genre genre, 
            final String author, final String publisher, final String isbn,
            final int pageCount, final String description)
    {
        this.id = id;
        this.name = name;
        this.genre = genre;
        this.author = author;
        this.publisher = publisher;
        this.isbn = isbn;
        this.pageCount = pageCount;
        this.description = description;
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

    public Genre getGenre()
    {
        return genre;
    }

    public void setGenre(final Genre genre)
    {
        this.genre = genre;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(final String author)
    {
        this.author = author;
    }

    public String getPublisher()
    {
        return publisher;
    }

    public void setPublisher(final String publisher)
    {
        this.publisher = publisher;
    }

    public String getIsbn()
    {
        return isbn;
    }

    public void setIsbn(final String isbn)
    {
        this.isbn = isbn;
    }

    public int getPageCount()
    {
        return pageCount;
    }

    public void setPageCount(final int pageCount)
    {
        this.pageCount = pageCount;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(final String description)
    {
        this.description = description;
    }
    
    @Override
    public String toString()
    {
        return "Book{" + "id=" + id + ", name=" + name + ", genre=" + genre + ", author=" + author + ", publisher=" + publisher + ", isbn=" + isbn + ", pageCount=" + pageCount + ", description=" + description + '}';
    }
    
    /**
     * Проверяет параметры книги на валидность
     * @return 
     */
    @Override
    public Set<ConstraintViolation<Object>> validate()
    {
        return validator.validate((Object)this);
    }

}
