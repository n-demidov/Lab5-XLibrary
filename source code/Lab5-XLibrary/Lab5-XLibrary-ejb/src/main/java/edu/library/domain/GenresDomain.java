package edu.library.domain;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import edu.library.beans.entity.Genre;
import edu.library.beans.persistence.GenreDatastore;
import edu.library.exceptions.ValidationException;
import edu.library.exceptions.db.NoSuchEntityInDB;
import edu.library.exceptions.db.PersistException;

@Stateless
@LocalBean
public class GenresDomain {
	
	@EJB
    private GenreDatastore genreDatastore;

	public List<Genre> getAll() throws PersistException {
		return genreDatastore.getAll();
	}

	public Genre get(final Long genreId) throws NoSuchEntityInDB, PersistException {
		return genreDatastore.get(genreId);
	}

	public void delete(final Long genreId) throws PersistException {
		genreDatastore.delete(genreId);
	}

	public void update(final Genre genre) throws PersistException, ValidationException {
		genreDatastore.update(genre);
	}

	public void create(final Genre genre) throws PersistException, ValidationException {
		genreDatastore.create(genre);
	}

}
