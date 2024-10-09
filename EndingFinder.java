// PPA - 1 / Assignment 2 - World of Zuul
// Gabriele Trotta (K21006956)

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is part of the project called "IUNI".
 * It's a simple text adventure game, where you can walk around 
 * and interact with characters and items, in order to regain your memory.
 * 
 * This class handles the finding and printing of the different endings of the game,
 * from an XML file named CONST_END.xml
 * 
 * @author Gabriele Trotta (K21006956)
 */
public class EndingFinder 
{
    private String end;                     //the identifier relating to the end in the XML file
    private TextViewer text;                //the object responsible for printing the end on the terminal

    private XmlManager endDocManager;       //the object responsible for handling the Xml file's data


    /**
     * Creates the EndingFinder object, that correlates the end to the corresponding
     * block of text in the CONST_END.xml file
     * @param end the identifier for the ending passed in
     * @param hasPie whether or not the player has a slice of pie (Item)
     */
    public EndingFinder(String end, boolean hasPie)
    {
        this.end = end;
        this.text = new TextViewer();

        this.endDocManager = new XmlManager("./xml_files/constants/CONST_END.xml", 
                                       "./xml_files/constants/CONST_END.xml", false);

        printEnd(hasPie);
    }

    /**
     * This function prints out the ending taken from the corresponding <end> element from the
     * XML file
     */
    private void printEnd(boolean hasPie)
    {
        Document doc = endDocManager.getXmlDocument();
        NodeList endList = doc.getElementsByTagName("end");

        for (int i=0; i < endList.getLength(); i++)
        {
            Element endElement = (Element) endList.item(i);

            if (endElement.getAttribute("id").equals(this.end))
            {

                Node nodeSibling = endElement.getElementsByTagName("text").item(0);
                String nodeSiblingName = nodeSibling.getNodeName();


                //while there is an element node
                while ( nodeSibling != null && (nodeSiblingName.equals("text") || nodeSiblingName.equals("pie")))
                {
                    nodeSiblingName = nodeSibling.getNodeName();

                    //if the node sibling is not the child node of the end's node
                    if (nodeSibling == null || nodeSibling.getParentNode() != endElement){ return; }

                    //prints out the <pie> element only if hasPie is true
                    //otherwise if it's a <text> element it will be printed out
                    if (hasPie || nodeSiblingName.equals("text")){
                        //if there is an ENDING in the end, then the ending's "number" will be shown
                        if (nodeSibling.getTextContent().contains("ENDING")){
                            text.printText(endDocManager.cleanString(nodeSibling.getTextContent()) + String.format(" (%d/%d)", i+1, endList.getLength()), 25);
                        }else{
                            text.printText(endDocManager.cleanString(nodeSibling.getTextContent()), 15);
                        }
                    }
                    nodeSibling = nodeSibling.getNextSibling();
        
                    //used to skip over the non-element nodes (such as comment nodes)
                    while (!(nodeSibling instanceof Element) && nodeSibling != null){
                        nodeSibling = nodeSibling.getNextSibling();
                    }

                }
                System.out.println();
            }
        }
    }
}
