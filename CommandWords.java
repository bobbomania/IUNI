// PPA - 1 / Assignment 2 - World of Zuul
// Gabriele Trotta (K21006956)

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class is part of the project called "IUNI".
 * It's a simple text adventure game, where you can walk around 
 * and interact with characters and items, in order to regain your memory.
 * 
 * This class holds an enumeration of all command words known to the game.
 * It is used to recognise commands as they are typed in.
 *
 * @author  Michael Kölling and David J. Barnes
 * @version 2016.02.29
 */

public class CommandWords
{
    // a constant array that holds all valid command words
    private ArrayList<String[]> validCommands;

    /**
     * Constructor - initialise the command words.
     */
    public CommandWords()
    {
        validCommands = new ArrayList<>();

        XmlManager commandWordManager = new XmlManager("./xml_files/constants/CONST_COMMANDWORDS.xml",
                                                       "./xml_files/constants/CONST_COMMANDWORDS.xml", false);

        Document doc = commandWordManager.getXmlDocument();

        getCommands(doc);
        
    }

    /**
     * Returns the decription of the command formatted in the following way:
     * [command] [parameters] : [description]
     * @param command the command we want to describe
     * @param parameters the parameters of the command
     * @param content the actual description of the command
     * @return the full formatted description and use of the command
     */
    public String getCommandDescription(String command, String parameters, String content){
        String description = command;
        if (!parameters.equals("")){
            for (String parameter : parameters.split(" ")){
                description += " [" + parameter + "]";
            }
        }
        description += ": " + content;

        return description;
    }

    /**
     * Load in the commands and their descriptions from the CONST_COMMANDWORDS.xml file
     * to the validCommnads ArrayList with the array {[command],[description]}
     * @param doc the xml document we are parsing to get the information
     */
    public void getCommands(Document doc){
        NodeList allCommands = doc.getElementsByTagName("command");
        for (int i=0; i < allCommands.getLength(); i++){
            Element command = (Element) allCommands.item(i);
            
            String commandWord = command.getAttribute("name");
            String commandDescription = this.getCommandDescription(commandWord, command.getAttribute("parameters"), command.getTextContent());
            
            validCommands.add(new String[]{commandWord, commandDescription});
        }
    }

    /**
     * Check whether a given String is a valid command word. 
     * @return true if it is, false if it isn't.
     */
    public boolean isCommand(String aString)
    {
        for(int i = 0; i < validCommands.size(); i++) {
            if(validCommands.get(i)[0].split("/")[0].equals(aString))
                return true;
        }
        // if we get here, the string was not found in the commands
        return false;
    }

    /**
     * Print all valid commands to System.out.
     */
    public void showAll() 
    {
        for(String[] commandBundle: validCommands) {
            System.out.print(commandBundle[0] + "  ");
        }
        System.out.println();
    }

    /**
     * Prints out the full description and use for the command specified
     * @param command the command that we are getting the help for
     */
    public void helpCommand(String command){
        for (String[] commandBundle : validCommands){
            String[] aliasCommand = commandBundle[0].split("/");

            //any alias corresponds to the command
            for (String alias : aliasCommand){
                if (command.equals(alias)){
                    System.out.println(commandBundle[1]);
                }
            }
        }
    }

    /**
     * Takes a word, and matches it to the closest corresponding command word (as much as one letter difference). "Normalizing" here is meant to make the word fit the command words.
     * @param word the word we want to "normalize"
     * @return the "normalized" word. Return null if the word is not in the set of command words
     */
    public String normalizeCommandWord(String word){

        if (word == null){return null;}

        for (String[] commandBundle : validCommands){
            String[] aliasCommand = commandBundle[0].split("/");

            for (String alias : aliasCommand){
                if (word.equals(alias)){
                    return aliasCommand[0];
                }
            }
        }

        return null;
    }
}
