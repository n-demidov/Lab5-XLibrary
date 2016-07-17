package edu.library.persistence.sorting;

public class BooksSorting {

	private final SortBy field;
	private final SortOrder order;
	
    /**
     * Sorting parameters (by field)
     */
    public enum SortBy
    {
        ID ("b.id"),
        Name ("b.name"),
        Genre ("genre.name"),
        Author ("b.author"),
        Publisher ("b.publisher"),
        ISBN ("b.isbn"),
        PageCount ("b.pageCount"),
        Description ("b.description");
        
        private final String name;       

        private SortBy(final String s)
        {
            name = s;
        }

        public boolean equalsName(final String otherName)
        {
            return (otherName == null) ? false : name.equals(otherName);
        }

        @Override
        public String toString()
        {
           return this.name;
        }
    }
    
    /**
     * Sorting parameters (Asc/Desc)
     */
    public enum SortOrder
    {
        Asc,
        Desc
    }
	
	public BooksSorting(final SortBy field, final SortOrder order) {
		super();
		
		this.field = field;
		this.order = order;
	}

	public final SortBy getField() {
		return field;
	}

	public final SortOrder getOrder() {
		return order;
	}

	@Override
	public String toString() {
		return "BooksSorting [field=" + field + ", order=" + order + "]";
	}
	
}
