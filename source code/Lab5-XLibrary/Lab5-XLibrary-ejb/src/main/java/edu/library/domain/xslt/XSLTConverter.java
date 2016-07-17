package edu.library.domain.xslt;

import java.io.StringReader;
import java.io.Writer;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Класс преобразует XML в HTML с помощью XSLT
 */
@Stateless
@LocalBean
public class XSLTConverter
{
    
    /**
     * Записывает в источник преобразованный в HTML XML.
     * @param xml
     * @param out
     * @param xsltTemplate
     * @throws TransformerException 
     */
    public void convertXMLtoHTML(final String xml, final Writer out, final Source xsltTemplate)
            throws TransformerException
    {
        final TransformerFactory factory = TransformerFactory.newInstance();
        final Transformer transformer = factory.newTransformer(xsltTemplate);
        final Source text = new StreamSource(new StringReader(xml));
        
        transformer.transform(text, new StreamResult(out));
    }
    
}
