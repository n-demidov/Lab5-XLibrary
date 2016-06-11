package edu.library.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public abstract class AbstractServlet extends HttpServlet
{
    
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
    
}
