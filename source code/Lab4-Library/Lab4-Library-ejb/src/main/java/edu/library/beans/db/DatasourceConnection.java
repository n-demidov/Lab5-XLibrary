package edu.library.beans.db;

import java.sql.Connection;
import java.sql.SQLException;
import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.sql.DataSource;

@Singleton
@LocalBean
public class DatasourceConnection
{
    
    private static final String DS_NAME = "java:/jdbc/MySqlDB";
    
    @Resource(mappedName=DS_NAME, type = javax.sql.DataSource.class)
    private DataSource dataSource;
    private Connection connection;
    
    public Connection getConnection() throws SQLException
    {
        if (connection == null)
        {
            connection = dataSource.getConnection();
        }
        
        return connection;
    }
    
}
