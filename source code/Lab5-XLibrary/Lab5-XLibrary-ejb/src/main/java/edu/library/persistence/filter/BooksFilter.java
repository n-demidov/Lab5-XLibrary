package edu.library.persistence.filter;

public class BooksFilter {

	private final String searchPhrase;
	private final Long genrePK;
	
	public BooksFilter(final String searchPhrase, final Long genrePK) {
		super();
		
		this.searchPhrase = searchPhrase;
		this.genrePK = genrePK;
	}

	public String getSearchPhrase() {
		return searchPhrase;
	}

	public Long getGenrePK() {
		return genrePK;
	}

}
