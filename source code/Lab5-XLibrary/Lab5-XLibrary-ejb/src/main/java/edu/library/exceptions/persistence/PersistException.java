package edu.library.exceptions.persistence;

import edu.library.exceptions.AbstractLibraryException;

/**
 * Data access layer exception
 */
public class PersistException extends AbstractLibraryException
{
    
    public PersistException(final String message)
    {
        super(message);
    }
    
}
