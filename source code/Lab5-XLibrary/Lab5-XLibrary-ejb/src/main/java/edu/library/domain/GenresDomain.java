package edu.library.domain;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import edu.library.exceptions.persistence.NoSuchPersistenceException;
import edu.library.exceptions.persistence.PersistException;
import edu.library.exceptions.persistence.ValidException;
import edu.library.persistence.dao.GenreDatastore;
import edu.library.persistence.entity.Genre;

@Stateless
@LocalBean
public class GenresDomain {
	
	@EJB
    private GenreDatastore genreDatastore;

	public List<Genre> getAll() throws PersistException {
		return genreDatastore.getAll();
	}

	public Genre get(final Long genreId) throws NoSuchPersistenceException, PersistException {
		return genreDatastore.get(genreId);
	}

	public void delete(final Long genreId) throws PersistException {
		genreDatastore.delete(genreId);
	}

	public void update(final Genre genre) throws PersistException, ValidException {
		genreDatastore.update(genre);
	}

	public void create(final Genre genre) throws PersistException, ValidException {
		genreDatastore.create(genre);
	}

}
