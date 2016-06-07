package edu.library.exceptions;

public class AbstractLibraryException extends Exception
{
    
    public AbstractLibraryException()
    {
        super();
    }

    public AbstractLibraryException(final String message)
    {
        super(message);
    }
    
    public AbstractLibraryException(final Throwable cause)
    {
        super(cause);
    }
    
}
