/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.library.beans.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(catalog = "library", schema = "", uniqueConstraints =
{
    @UniqueConstraint(columnNames =
    {
        "isbn"
    }),
    @UniqueConstraint(columnNames =
    {
        "id"
    })
})
@XmlRootElement
@NamedQueries(
{
    @NamedQuery(name = "Book.findAll", query = "SELECT b FROM Book b"),
    @NamedQuery(name = "Book.findById", query = "SELECT b FROM Book b WHERE b.id = :id"),
    @NamedQuery(name = "Book.findByName", query = "SELECT b FROM Book b WHERE b.name = :name"),
    @NamedQuery(name = "Book.findByAuthor", query = "SELECT b FROM Book b WHERE b.author = :author"),
    @NamedQuery(name = "Book.findByPublisher", query = "SELECT b FROM Book b WHERE b.publisher = :publisher"),
    @NamedQuery(name = "Book.findByIsbn", query = "SELECT b FROM Book b WHERE b.isbn = :isbn"),
    @NamedQuery(name = "Book.findByPageCount", query = "SELECT b FROM Book b WHERE b.pageCount = :pageCount"),
    @NamedQuery(name = "Book.findByDescription", query = "SELECT b FROM Book b WHERE b.description = :description")
})
public class Book implements Serializable
{

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(nullable = false)
    private Long id;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60, message = "Поле 'Название' должно быть от 1 до 60 символов")
    @Column(nullable = false, length = 60)
    private String name;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255, message="Поле 'Автор' должно быть от 1 до 255 символов")
    @Column(nullable = false, length = 255)
    private String author;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255, message="Поле 'Издатель' должно быть от 1 до 255 символов")
    @Column(nullable = false, length = 255)
    private String publisher;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100, message="Поле 'ISBN' должно быть от 1 до 100 символов")
    @Column(nullable = false, length = 100)
    private String isbn;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "page_count", nullable = false)
    private int pageCount;
    
    @Basic(optional = false)
    @NotNull
    @Size(max = 500, message="Поле 'Описание' не должно превышать 500 символов")
    @Column(nullable = false, length = 500)
    private String description;
    
    @JoinColumn(name = "genre_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    private Genre genre;

    public Book()
    {
    }

    public Book(Long id)
    {
        this.id = id;
    }

    public Book(Long id, String name, String author, String publisher, String isbn, int pageCount, String description)
    {
        this.id = id;
        this.name = name;
        this.author = author;
        this.publisher = publisher;
        this.isbn = isbn;
        this.pageCount = pageCount;
        this.description = description;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor(String author)
    {
        this.author = author;
    }

    public String getPublisher()
    {
        return publisher;
    }

    public void setPublisher(String publisher)
    {
        this.publisher = publisher;
    }

    public String getIsbn()
    {
        return isbn;
    }

    public void setIsbn(String isbn)
    {
        this.isbn = isbn;
    }

    public int getPageCount()
    {
        return pageCount;
    }

    public void setPageCount(int pageCount)
    {
        this.pageCount = pageCount;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Genre getGenre()
    {
        return genre;
    }

    public void setGenre(Genre genre)
    {
        this.genre = genre;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Book))
        {
            return false;
        }
        Book other = (Book) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "edu.library.beans.entity.Book[ id=" + id + " ]";
    }
    
}
