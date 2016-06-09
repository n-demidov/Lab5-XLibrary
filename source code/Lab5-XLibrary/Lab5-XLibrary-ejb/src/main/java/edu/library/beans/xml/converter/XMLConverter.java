package edu.library.beans.xml.converter;

import edu.library.beans.xml.rootelement.Books;
import edu.library.beans.entity.Book;
import edu.library.beans.persistence.BookDatastore;
import edu.library.exceptions.db.PersistException;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 *
 * @author Anatoly Lunev
 */
@Stateless
public class XMLConverter
{

    @EJB
    private BookDatastore bookDS;

    private JAXBContext jaxbContext;
    private Marshaller booksJaxbMarshaller;

    public XMLConverter()
    {
        try
        {
            jaxbContext = JAXBContext.newInstance(Books.class);
            booksJaxbMarshaller = jaxbContext.createMarshaller();
            booksJaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        } catch (final JAXBException ex)
        {
            Logger.getLogger(XMLConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String convertBooks(final List<Long> ids)
    {
        final StringWriter sw = new StringWriter();

        try
        {
            final List<Book> bookList = bookDS.get(ids);
            final Books books = new Books();
            books.setBooks(bookList);

//            for (final Book book : bookList)
//            {
//                booksJaxbMarshaller.marshal(book, sw);
//            }
            
            booksJaxbMarshaller.marshal(books, sw);
        } catch (final PersistException | JAXBException ex)
        {
            Logger.getLogger(XMLConverter.class.getName()).log(Level.SEVERE, null, ex);
        }

        return sw.toString();
    }

}
