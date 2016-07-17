package edu.library.exceptions.persistence;

import edu.library.exceptions.AbstractLibraryException;

public class NoSuchPersistenceException extends AbstractLibraryException
{
    
    public NoSuchPersistenceException(final String message)
    {
        super(message);
    }
    
}
