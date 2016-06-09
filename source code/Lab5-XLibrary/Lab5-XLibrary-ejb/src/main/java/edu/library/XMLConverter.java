/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.library;

import edu.library.beans.entity.Book;
import edu.library.beans.persistence.BookDatastore;
import edu.library.exceptions.db.NoSuchEntityInDB;
import edu.library.exceptions.db.PersistException;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
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
public class XMLConverter {
    
    @EJB
    BookDatastore BookDS;
    
    JAXBContext jaxbContext;
    Marshaller booksJaxbMarshaller;

    public XMLConverter() {
        
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Book.class);
            booksJaxbMarshaller = jaxbContext.createMarshaller();
            booksJaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            
        } catch (JAXBException ex) {
            Logger.getLogger(XMLConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public String convertBooks(final List<Long> ids){
        
        StringWriter sw = new StringWriter();
        
        try {
            List<Book> bookList = BookDS.get(ids);
            
            for(Book book: bookList){
                booksJaxbMarshaller.marshal(book, sw);
            }
            
        } catch (PersistException ex) {
            Logger.getLogger(XMLConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JAXBException ex) {
            Logger.getLogger(XMLConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return sw.toString();
    }
    
}
