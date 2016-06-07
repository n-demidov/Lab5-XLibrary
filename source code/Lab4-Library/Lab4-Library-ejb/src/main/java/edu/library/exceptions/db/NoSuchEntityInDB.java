package edu.library.exceptions.db;

import edu.library.exceptions.AbstractLibraryException;

public class NoSuchEntityInDB extends AbstractLibraryException
{
    
    public NoSuchEntityInDB(final String message)
    {
        super(message);
    }
    
}
