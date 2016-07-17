package edu.library.domain;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import edu.library.beans.persistence.BookDatastore;
import edu.library.beans.persistence.GenreDatastore;

@Stateless
@LocalBean
public class BooksDomain {

	@EJB
    private BookDatastore bookDatastore;

    @EJB
    private GenreDatastore genreDatastore;

}
