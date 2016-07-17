package edu.library.domain;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import edu.library.exceptions.persistence.NoSuchPersistenceException;
import edu.library.exceptions.persistence.PersistException;
import edu.library.exceptions.persistence.ValidException;
import edu.library.persistence.dao.BookDatastore;
import edu.library.persistence.dao.GenreDatastore;
import edu.library.persistence.entity.Book;
import edu.library.persistence.filter.BooksFilter;
import edu.library.persistence.sorting.BooksSorting;

@Stateless
@LocalBean
public class BooksDomain {

	@EJB
    private BookDatastore bookDatastore;

    @EJB
    private GenreDatastore genreDatastore;

	public List<Book> getByFilter(final BooksFilter booksFilter,
			final BooksSorting booksSorting) throws PersistException {
		return bookDatastore.getByFilter(booksFilter, booksSorting);
	}

	public Book get(final Long bookId) throws NoSuchPersistenceException, PersistException {
		return bookDatastore.get(bookId);
	}

	public void create(final Book book) throws PersistException, ValidException {
		bookDatastore.create(book);
	}

	public void update(final Book book) throws PersistException, ValidException {
		bookDatastore.update(book);
	}
	
	public void delete(final Long bookId) throws PersistException {
		bookDatastore.delete(bookId);
	}
	
	public void delete(final List<Long> bookIds) throws PersistException {
		bookDatastore.delete(bookIds);
	}
	
	public void copy(final List<Long> bookIds) throws PersistException {
		bookDatastore.copy(bookIds);
	}

}
