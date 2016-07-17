package edu.library.domain.xml.converter;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import edu.library.domain.xml.rootelement.Books;
import edu.library.exceptions.persistence.NoSuchPersistenceException;
import edu.library.exceptions.persistence.PersistException;
import edu.library.exceptions.persistence.ValidException;
import edu.library.persistence.dao.BookDatastore;
import edu.library.persistence.entity.Book;

@Stateless
@LocalBean
public class BooksXMLporter
{
    
    private static final String BOOK_ADDING_ERR = "Ошибка при добавлении книги '%s' (id=%d): %s";
    private static final String BOOK_WAS_ADDED = "Книга '%s' (id=%d) успешно добавлена";
    private static final String BOOK_UPDATING_ERR = "Ошибка при обновлении книги '%s' (id=%d): %s";
    private static final String BOOK_WAS_UPDATED = "Книга '%s' (id=%d) успешно обновлена";

    @EJB
    private BookDatastore bookDatastore;

    private JAXBContext jaxbContext;
    private Marshaller booksJaxbMarshaller;
    private Unmarshaller booksJaxbUnmarshaller;

    public BooksXMLporter()
    {
        try
        {
            jaxbContext = JAXBContext.newInstance(Books.class);
            booksJaxbMarshaller = jaxbContext.createMarshaller();
            booksJaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            
            booksJaxbUnmarshaller = jaxbContext.createUnmarshaller();
        } catch (final JAXBException ex)
        {
            Logger.getLogger(BooksXMLporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Экспортирует выбранные книги в XML
     * @param ids
     * @return 
     */
    public String exportBooks(final List<Long> ids)
    {
        final StringWriter sw = new StringWriter();

        try
        {
            final List<Book> bookList = bookDatastore.get(ids);
            final Books books = new Books();
            books.setBooks(bookList);
            booksJaxbMarshaller.marshal(books, sw);
        } catch (final PersistException | JAXBException ex)
        {
            Logger.getLogger(BooksXMLporter.class.getName()).log(Level.SEVERE, null, ex);
        }

        return sw.toString();
    }
    
    /**
     * Импортирует выбранные книги из XML. Добавляет книги в БД
     * @param inputStream
     * @return 
     * @throws JAXBException 
     */
    public List<String> importBooks(final InputStream inputStream) throws JAXBException
    {
        final List<String> results = new ArrayList<>();
        final Books books = (Books) booksJaxbUnmarshaller.unmarshal(inputStream);

        for (final Book book : books.getBooks())
        {
            try
            {
                bookDatastore.get(book.getId());
                
                try
                {
                    bookDatastore.update(book);
                    results.add(String.format(BOOK_WAS_UPDATED,
                            book.getName(),
                            book.getId()));
                } catch (final ValidException ex)
                {
                    results.add(String.format(BOOK_UPDATING_ERR,
                            book.getName(),
                            book.getId(),
                            ex.getLocalizedMessage()));
                }
            } catch (final NoSuchPersistenceException | PersistException e)
            {
                // если книги нет - добавляем
                try
                {
                    bookDatastore.create(book);
                    results.add(String.format(BOOK_WAS_ADDED,
                            book.getName(),
                            book.getId()));
                } catch (final PersistException | ValidException ex)
                {
                    results.add(String.format(BOOK_ADDING_ERR,
                            book.getName(),
                            book.getId(),
                            ex.getLocalizedMessage()));
                }
            }
        }
        
        return results;
    }

}
