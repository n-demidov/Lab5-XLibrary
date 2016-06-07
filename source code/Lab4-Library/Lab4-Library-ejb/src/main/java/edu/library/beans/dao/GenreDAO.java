package edu.library.beans.dao;

import edu.library.beans.entity.Genre;
import edu.library.exceptions.ValidationException;
import edu.library.exceptions.db.NoSuchEntityInDB;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 * Объект для управления персистентным состоянием объекта Genre
 */
@Stateless
@LocalBean
public class GenreDAO extends AbstractDAO
{
    
    public static final String GENRE_ID = "id", GENRE_NAME = "name";
    
    private static final String SELECT_TEMPLATE = "SELECT g.id, g.name FROM genre g";
    private static final String GENRES = SELECT_TEMPLATE;
    private static final String SELECT_GENRE = SELECT_TEMPLATE + " WHERE g.id = ?";
    private static final String UPDATE = "UPDATE genre SET name = ? WHERE id = ?";
    private static final String INSERT = "INSERT INTO genre (name) VALUES(?)";
    private static final String DELETE = "DELETE FROM genre WHERE id = ?";
    
    private static final String NO_SUCH_ENTITY_IN_DB = "В базе данных нет жанра с id = %d";
    
    /**
     * Возвращает все объекты
     * @return
     * @throws SQLException 
     */
    public List<Genre> getAll() throws SQLException
    {
        final List<Genre> genres = new ArrayList<>();
        
        try (final PreparedStatement statement = connection.prepareStatement(GENRES);
             final ResultSet rs = statement.executeQuery())
        {
            while (rs.next())
            {
                final Genre genre = new Genre(
                        rs.getInt(GENRE_ID),
                        rs.getString(GENRE_NAME));
                genres.add(genre);
            }
        }
        
        return genres;
    }
    
    /** 
     * Возвращает объект соответствующий записи.
     * @param id
     * @return 
     * @throws java.sql.SQLException 
     * @throws edu.library.exceptions.db.NoSuchEntityInDB 
     */
    public Genre get(final int id) throws SQLException, NoSuchEntityInDB
    {
        ResultSet rs = null;

        try (final PreparedStatement statement = connection.prepareStatement(SELECT_GENRE))
        {
            statement.setInt(1, id);
            
            rs = statement.executeQuery();
            
            if (rs.next())
            {
                return getResultSetBook(rs);
            } else
            {
                throw new NoSuchEntityInDB(String.format(NO_SUCH_ENTITY_IN_DB, id));
            }
        } finally
        {
            closeResultSet(rs);
        }
    }
    
    /**
     * Сохраняет состояние объекта в базе данных
     * @param genre
     * @throws java.sql.SQLException
     * @throws edu.library.exceptions.ValidationException
     */
    public void update(final Genre genre) throws SQLException, ValidationException
    {
        validate(genre);
        
        try (final PreparedStatement statement = connection.prepareStatement(UPDATE))
        {
            statement.setString(1, String.valueOf(genre.getName()));
            statement.setInt(2, genre.getId());

            statement.executeUpdate();
        }
    }
    
    /**
     * 
     * Создает новую запись. Изменяет primary key переданного объекта на сохранённый в БД.
     * @param genre
     * @throws SQLException 
     * @throws edu.library.exceptions.ValidationException 
     */
    public void create(final Genre genre) throws SQLException, ValidationException
    {
        validate(genre);
        
        ResultSet rs = null;
        try (final PreparedStatement statement = connection.prepareStatement(
                INSERT, Statement.RETURN_GENERATED_KEYS))
        {
            statement.setString(1, genre.getName());

            statement.executeUpdate();
            
            rs = statement.getGeneratedKeys();
            rs.next();
            final int savedGenreId = rs.getInt(1);
            genre.setId(savedGenreId);
        } finally
        {
            closeResultSet(rs);
        }
    }

    /**
     * Удаляет запись об объекте из базы данных
     * @param id
     * @throws java.sql.SQLException
     */ 
    public void delete(final int id) throws SQLException
    {
        try (final PreparedStatement statement = connection.prepareStatement(DELETE))
        {
            statement.setInt(1, id);
            statement.execute();
        }
    }
    
    // Создаёт экземпляр класса Genre из запроса к БД
    private Genre getResultSetBook(final ResultSet rs) throws SQLException
    {
        final Genre genre = new Genre(
                rs.getInt(GENRE_ID), 
                rs.getString(GENRE_NAME));
        
        return genre;
    }
    
}
