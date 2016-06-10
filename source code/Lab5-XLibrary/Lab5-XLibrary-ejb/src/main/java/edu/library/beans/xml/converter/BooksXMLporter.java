package edu.library.beans.xml.converter;

import edu.library.beans.xml.rootelement.Books;
import edu.library.beans.entity.Book;
import edu.library.beans.persistence.BookDatastore;
import edu.library.exceptions.ValidationException;
import edu.library.exceptions.db.NoSuchEntityInDB;
import edu.library.exceptions.db.PersistException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

@Stateless
public class BooksXMLporter
{

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

//            for (final Book book : bookList)
//            {
//                booksJaxbMarshaller.marshal(book, sw);
//            }
            
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
     * @throws JAXBException 
     */
    public List<String> importBooks(final InputStream inputStream) throws JAXBException
    {
        System.out.println("unConvertBooks");
        
        final List<String> results = new ArrayList<>();
        final Books books = (Books) booksJaxbUnmarshaller.unmarshal(inputStream);

        for (final Book book : books.getBooks())
        {

            try
            {
                bookDatastore.get(book.getId());
            }  catch (NoSuchEntityInDB | PersistException e)
            {
                // если книги нет - добавляем
                try
                {
                    bookDatastore.create(book);
                    results.add(String.format(
                            "Книга '%s' (id=%d) успешно добавлена",
                            book.getName(),
                            book.getId()));
                } catch (final PersistException | ValidationException ex)
                {
                    //Logger.getLogger(BooksXMLporter.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("~~~~~ err int hadnling book update");
                    System.out.println(ex.getMessage());
                    results.add(String.format(
                            "Ошибка при добавлении книги '%s' (id=%d): %s",
                            book.getName(),
                            book.getId(),
                            ex.getLocalizedMessage()));
                }
            }
        }
        return results;
    }

}
