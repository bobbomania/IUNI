// PPA - 1 / Assignment 2 - World of Zuul
// Gabriele Trotta (K21006956)

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;
import java.io.IOException;

/**
 * This class is part of the project called "IUNI".
 * It's a simple text adventure game, where you can walk around 
 * and interact with characters and items, in order to regain your memory.
 * 
 * This class handles all of the creation, manipulation and restoration of the xml files
 * to previous versions of the files
 * 
 * This class handles also all operations regarding the content of the xml file
 * 
 * @author Gabriele Trotta (K21006956)
 */

public class XmlManager {
    private Document doc;

    private String fileName;
    private String backupFileName;

    /**
     * Creates a new XmlManager object with the filename of the file that is being accessed
     * and the filename of the file containing the unmodified file data
     * 
     * @param fileName the name of the file that is accessed and modified
     * @param constFileName the name of the file that is the initial state of the file above
     * @param skipCorrupt whether we need to check if the file is corrupt or not (not saved properly)
     */
    public XmlManager(String fileName, String backupFileName, boolean skipCorrupt){
        this.fileName = fileName;
        this.backupFileName = backupFileName;

        boolean needsCorruption = instantiateDocument(skipCorrupt);

        //only modify the isCorrupted attribute in xml file if the file actually needs 
        //to be saved
        if (needsCorruption){
            //saves the current file to its backup
            this.resetDocument(this.backupFileName, this.fileName);
            isCorrupt();
        }
    }

    /**
     * @return the xml document containing the file's information
     */
    public Document getXmlDocument(){
        return doc;
    }

    /**
     * This function loads in the document from the file whose name is fileName
     * in the doc variable so that we can extract its contents.
     * The code used here has been partly taken from : 
     *      https://mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
     *      by mkyong, published on 01/04/2021
     * 
     * to load in this document.
     */
    private boolean instantiateDocument(boolean skipCorrupt){
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            doc = db.parse(new File(fileName));

            doc.getDocumentElement().normalize();
            Element rootElement = doc.getDocumentElement();

            //if the file hasn't been saved properly, restore its contents to its backup file
            if (rootElement.getAttribute("isCorrupted").equals("true") && !skipCorrupt){
                this.resetDocument(this.fileName, this.backupFileName);
                this.instantiateDocument(skipCorrupt);
            }

            return (!rootElement.getAttribute("isCorrupted").equals(""));

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * This function updates the file, to commit the modification of 
     * attributes (e.g.: hasBeenRepated = true) to the file.
     *
     * code from : 
     *      https://stackhowto.com/how-to-change-an-attribute-value-in-xml-using-java-dom/
     *      published on 09/06/2021
     * 
     * was used to update the document
    */ 
    public void updateDocument(){
        try{
            // write the content to the xml file
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource src = new DOMSource(doc);
            StreamResult res = new StreamResult(new File(fileName));
            transformer.transform(src, res);

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This function resets the document specified by the oldFileName to the document specified
     * by the newFileName.
     * code from : 
     *      https://stackhowto.com/how-to-change-an-attribute-value-in-xml-using-java-dom/
     *      published on 09/06/2021
     * was similarly used to update the file.
     * 
     * @param newFileName the name of the file to be reset
     * @param oldFileName the name of the file that is being used to reset the file
     */
    public void resetDocument(String oldFileName, String newFileName){
        try{
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document base_doc = db.parse(new File(newFileName));

            // write the content to the xml file
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource src = new DOMSource(base_doc);

            StreamResult res = new StreamResult(new File(oldFileName));
            transformer.transform(src, res);

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Modifies the element's attribute's text content in the xml file
     * @param element the element of the attribute 
     * @param attribute the attribute whose the text contents are modified
     * @param newContent the content replacing the attribute's old content
     */
    public void modifyAttribute(Element element, String attribute, String newContent){
        if (element.hasAttribute(attribute)){
            NamedNodeMap attr = element.getAttributes();
            Node node = attr.getNamedItem(attribute);

            node.setTextContent(newContent);
        }
        else{
            element.setAttribute(attribute, newContent);
        }
    }

    /**
     * changes the isCorrupted attribute to false, indicating that the saving has been done
     * successfully
     */
    public void isNotCorrupt(){
        this.modifyAttribute(doc.getDocumentElement(), "isCorrupted", "false");
        this.updateDocument();
    }

    /**
     * modifies the isCorrupted attribute to true, indicating that the file hasn't been properly
     * saved yet
     */
    public void isCorrupt(){
        this.modifyAttribute(doc.getDocumentElement(), "isCorrupted", "true");
        this.updateDocument();
    }

    /**
     * Removes all unecessary whitespace in a string of text (useful in element's text content)
     * @param string the string being "cleaned"
     * @return the string without unecessary whitespace
     */
    public String cleanString(String string){
        
        string = string.trim();
        
        //we keep new lines
        string = string.replaceAll("[^\\S^\n]{2,}", "");
        
        return string;
    }
}
