package edu.library.beans.persistence;

import edu.library.beans.entity.Book;
import edu.library.beans.entity.Genre;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

public class HibernateSessionFactory
{
    
    private static ServiceRegistry serviceRegistry;
    private static final SessionFactory sessionFactory = configureSessionFactory();
 
    /**
     * Создание фабрики
     * @return 
     * @throws HibernateException
     */
    private static SessionFactory configureSessionFactory()
    {
        // Настройки hibernate
        Configuration configuration = new Configuration()
               .setProperty( "hibernate.connection.datasource", 
                                    "java:/jdbc/MySqlDB" )
                
               .setProperty( "hibernate.connection.autocommit", "false" )
               .setProperty( "hibernate.cache.provider_class", 
                                    "org.hibernate.cache.NoCacheProvider" )
               .setProperty( "hibernate.cache.use_second_level_cache", 
                                    "false" )
               .setProperty( "hibernate.cache.use_query_cache", "false" )
               .setProperty( "hibernate.dialect",
                                    "org.hibernate.dialect.MySQLDialect" )
               .setProperty( "hibernate.show_sql","true" )
               .setProperty( "hibernate.current_session_context_class",
                                    "thread" )
               .setProperty("hibernate.enable_lazy_load_no_trans", "true")
               .addAnnotatedClass(Book.class)
               .addAnnotatedClass(Genre.class)
               ;
        serviceRegistry = new ServiceRegistryBuilder().applySettings(
                configuration.getProperties()).buildServiceRegistry();
        return configuration.buildSessionFactory(serviceRegistry);
    }
 
    /**
     * Получить фабрику сессий
     * @return 
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    
}
