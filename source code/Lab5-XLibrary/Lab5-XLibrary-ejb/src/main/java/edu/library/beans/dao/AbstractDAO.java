package edu.library.beans.dao;

import edu.library.beans.entity.ValidationBean;
import edu.library.beans.db.DatasourceConnection;
import edu.library.exceptions.ValidationException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.validation.ConstraintViolation;

public abstract class AbstractDAO
{
    
    protected Connection connection;
    
    @EJB
    private DatasourceConnection databaseConnection;
    
    @PostConstruct
    public void postConstruct()
    {
        // Получаем соединение к БД
        try
        {
            connection = databaseConnection.getConnection();
        } catch (final SQLException ex)
        {
            Logger.getLogger(GenreDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // Закрывает ResultSet
    protected void closeResultSet(final ResultSet rs)
    {
        if (rs != null)
        {
            try
            {
                rs.close();
            } catch (final SQLException ex)
            {
                Logger.getLogger(AbstractDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    // Закрывает PreparedStatement
    protected void closePreparedStatement(final PreparedStatement ps)
    {
        if (ps != null)
        {
            try
            {
                ps.close();
            } catch (final SQLException ex)
            {
                Logger.getLogger(AbstractDAO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Проверяет данные на валидность - показывает первую ошибку 
     * @param validationBean
     * @throws edu.library.exceptions.ValidationException 
     */
    protected void validate(final ValidationBean validationBean) throws ValidationException
    {
        for (final ConstraintViolation<Object> cv : validationBean.validate())
        {
            throw new ValidationException(cv.getMessage());
        }
    }
    
}
