/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.library;

import edu.library.beans.dao.AbstractDAO;
import edu.library.beans.dao.BookDAO;
import edu.library.beans.entity.Book;
import edu.library.exceptions.db.NoSuchEntityInDB;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.sql.SQLException;
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
    BookDAO bookDAO;
    
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
    
    public String convertBooks(final int[] ids){
        
        StringWriter sw = new StringWriter();
        
        for(int id : ids){
            try {
                Book book = bookDAO.get(id);
                booksJaxbMarshaller.marshal(book, sw);
                
            } catch (SQLException ex) {
                Logger.getLogger(XMLConverter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchEntityInDB ex) {
                Logger.getLogger(XMLConverter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JAXBException ex) {
                Logger.getLogger(XMLConverter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return sw.toString();
    }
    
}
