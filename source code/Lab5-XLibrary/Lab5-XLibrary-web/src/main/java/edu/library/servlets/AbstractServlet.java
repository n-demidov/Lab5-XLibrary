package edu.library.servlets;

import edu.library.exceptions.ParseIntException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public abstract class AbstractServlet extends HttpServlet
{
    
    private static final String ERR_EMPTY = " (незаполнено)";
    
    // Check is request with enctype multipart/form-data
    protected boolean isMultipartFormData(final HttpServletRequest request)
    {
        final String MULTIPART_FORM_DATA = "multipart/form-data";
        return (request.getContentType() != null
                && request.getContentType().toLowerCase()
                .indexOf(MULTIPART_FORM_DATA) > -1);
    }
    
    // Возвращает полный uri страницы
    protected String getFullURI(final HttpServletRequest request)
    {
        return request.getScheme() + "://" + request.getServerName()
                + ("http".equals(request.getScheme()) && request.getServerPort() == 80
                || "https".equals(request.getScheme()) && request.getServerPort() == 443
                ? "" : ":" + request.getServerPort()) + request.getRequestURI()
                + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
    }
    
    // Парсит число из строки
    protected int parseInt(final String parsedString, final String errDescr) 
            throws ParseIntException
    {
        try
        {
            if (parsedString == null || parsedString.trim().isEmpty())
            {
                throw new ParseIntException(errDescr + ": " + ERR_EMPTY);
            }
            
            return Integer.parseInt(parsedString);
        } catch (final NumberFormatException ex)
        {
            throw new ParseIntException(errDescr + ": " + parsedString);
        }
    }
    
}
