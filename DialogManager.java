// PPA - 1 / Assignment 2 - World of Zuul
// Gabriele Trotta (K21006956)

import java.util.HashMap;
import java.util.Scanner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import minigames.PigGame.PigGame;

/**
 * This class is part of the project called "IUNI".
 * It's a simple text adventure game, where you can walk around 
 * and interact with characters and items, in order to regain your memory.
 * 
 * The file DialogManager.java manages all of the dialog instances involving NPC's and the player.
 * 
 * All of the dialog is stored in CONST_DIALOG.xml and the modified version in dialog.xml,
 * to render the process of adding dialog of an NPC easier. A backup file, backup_dialog.xml is used
 * in case the game's data is not saved properly.
 */

/**
 * This interface is used as an adapter so as to specify and store a "checkCondition" function.
 * (only added for the Netcraft application, otherwise in other file)
 * 
 * The latter checks a condition involving an NPC and a possible supplementary parameter, that
 * will determine if the dialog should be started or not.
 */

abstract interface conditionFunction
{
    /**
     * This function checks a Condition specified in the DialogManager.java to test if a dialog should happen
     * or not.
     * @param npc the npc with which the player is interacting with
     * @param parameterName the name of a supplementary parameter needed to determine if the condition 
     * has been satisfied
     * @return whether the condition has been satisfied or not
     */

    public boolean checkCondition(NPC npc, String parameterName);
}

/** 
 * This class parses the dialog.xml file to get out the corresponding interaction with the player.
 * Two files are used so that one of them, dialog.xml can be modified to keep track of the player's interactions
 * with the NPCs.
 *
 * @author Gabriele Trotta (K21006956)
 * @version 17/11/21
 */
public class DialogManager
{
    private XmlManager dialogDocManager;           //the object responsible for the dialog.xml file
    private TextViewer text;                       //the object displaying the text to the terminal

    private NPC npc;                                
    private Scanner reader;
    private Document doc;                          //variable containing the data from the xml file

    private boolean wantToQuit;
    private String end;

    private boolean changesOccured;
    private boolean hasInteractionHappened;         //whether or not an interaction of depth 1 has happened
    private int memoriesGiven;                      //the amount of memory points given by the interaction

    private HashMap<String, Item> itemsTraded;      //the map containing all the items traded between two parties (any from the player/room and an npc)

    //map indexing a String key to a boolean function, the latter checks a condition
    //allowing a dialogue with the NPC.
    private HashMap<String, conditionFunction> allConditionFunctions;

    /**
     * Constructor for the DialogManager that loads in the document dialog.xml and that initializes all
     * of the functions that determine the value of a condition function.
     * 
     * @param npc the npc with which the player is interacting with
     */
    public DialogManager(NPC npc)
    {
        this.npc = npc;

        reader = new Scanner(System.in);
        text = new TextViewer();

        itemsTraded = new HashMap<>();


        dialogDocManager = new XmlManager("./xml_files/mutables/dialog.xml", 
                                          "./xml_files/backups/backup_dialog.xml", true);     //sets the isCorrupt attribute to true
        
        doc = dialogDocManager.getXmlDocument();

        wantToQuit = false;
        changesOccured = false;
        memoriesGiven = 0;
        end = "";

        allConditionFunctions = new HashMap<>();

        instantiateConditions();   
    }

    /**
     * This constructor is called when no npc is involved in the dialog, so only at the end of the program.
     * We then restore the dialog.xml content to the unchanged format in CONST_DIALOG.xml
     */
    public DialogManager()
    {
        dialogDocManager = new XmlManager("./xml_files/mutables/dialog.xml",
                                          "./xml_files/backups/backup_dialog.xml", true);

        //this function is embedded into constructor to avoid public access to it
        dialogDocManager.resetDocument("./xml_files/mutables/dialog.xml", "./xml_files/constants/CONST_DIALOG.xml");
    }

    /**
     * This function assigns a condition function for a specific key, these are later used to
     * see if the dialog should occur or not.
     */
    private void instantiateConditions()
    {
        //condition function deciding whether the npc has the item with the given item name
        allConditionFunctions.put("hasItem", new conditionFunction() {
            public boolean checkCondition(NPC npc, String itemName) { 
                return npc.hasItem(itemName); }
        });

        //condition function giving an npc's item to the player
        allConditionFunctions.put("giveItem", new conditionFunction(){
            public boolean checkCondition(NPC npc, String itemName){
                itemsTraded.put("give-" + itemName, npc.getItem(itemName));   //for every item given put a 'give-' at the start of its key
                npc.removeItem(itemName);

                return true;
            }
        });

        //condition function that drops an npc's item in the current room
        allConditionFunctions.put("dropItem", new conditionFunction(){
            public boolean checkCondition(NPC npc, String itemName){

                itemsTraded.put("drop-" + itemName, npc.getItem(itemName));    //for every item dropped put a 'drop-' at the start of its key
                npc.removeItem(itemName);

                return true;
            }
        });

        //condition function that starts the mini-game
        allConditionFunctions.put("playGame", new conditionFunction(){
            public boolean checkCondition(NPC npc, String itemName)
            {
                PigGame pigGame = new PigGame();
                wantToQuit = pigGame.play();
                
                if (pigGame.hasWon()){
                    itemsTraded.put("give-garden-key", npc.getItem("garden-key"));
                    npc.removeItem("garden-key");

                    text.printText("Well done charlie! You have won, here let me give you this as a reward.", 20);

                }else if(!wantToQuit){
                    text.printText("Well well well, you lost. Maybe you'll be lucky next time!", 20);
                }
                return true;
            }
        });

        //condition function that checks whether an npc is missing an item
        allConditionFunctions.put("missItem", new conditionFunction(){
            public boolean checkCondition(NPC npc, String itemName){
                return !npc.hasItem(itemName);
            }
        });

        //condition function that gives a set amount memory points to the player
        allConditionFunctions.put("giveMemories", new conditionFunction(){
            public boolean checkCondition(NPC npc, String memories){
                memoriesGiven = Integer.parseInt(memories);
                return true;
            }
        });
        
        //condition function that moves the npc to a new room
        allConditionFunctions.put("moveNpc", new conditionFunction(){
            public boolean checkCondition(NPC npc, String roomName){
                npc.setCurrentRoom(roomName);
                return true;
            }
        });

        //condition function that identifies an end when activated
        allConditionFunctions.put("endGame", new conditionFunction(){
            public boolean checkCondition(NPC npc, String endName){
                end = endName;
                return true;
            }
        });

        //condition function used to quit the game 
        allConditionFunctions.put("quitGame", new conditionFunction(){
            public boolean checkCondition(NPC npc, String itemName){
                wantToQuit = true;
                return true;
            }
        });

        //condition function used to save the game
        allConditionFunctions.put("save", new conditionFunction(){
            public boolean checkCondition(NPC npc, String itemName){
                wantToQuit = true;
                return true;
            }
        });

    }

    /**
     * Remove and return the item traded, according to its item key
     * @param itemKey the key identifying the item traded to be 'popped'
     * @return the item traded
     */
    public Item popItemTraded(String itemKey){
        return itemsTraded.remove(itemKey);
    }

    /**
     * @return whether or not there is an item traded by the npc
     */
    public boolean hasItemTraded(){
        return (itemsTraded.size() > 0);
    }

    /**
     * @return the name of any item traded by the npc
     * return "" if none are left
     */
    public String getNameOfItemTraded(){
        for (String key : itemsTraded.keySet()){
            return key;
        }
        return "";
    }

    /**
     * @return the memory points gained by this dialog
     */
    public int getMemories(){
        return memoriesGiven;
    }
    
    /**
     * changes the <allDialog> attribute "isCorrupt" to false, thus dialog.xml has not been reset
     */
    public void isNotCorrupt(){
        dialogDocManager.isNotCorrupt();
    }

    /**
     * @return whether the dialog yields an end to the game
     */
    public boolean hasEnd(){
        return (!end.equals(""));
    }

    /**
     * @return the end found after the dialog
     */
    public String getEnd(){
        return end;
    }

    /**
     * Checks whether the npc satisfies the special condition identified by the string "specialCondition"
     * @param specialCondition the string that identifies the special condition
     * @return whether the npc can satisfy the special condition
     */
    public boolean canDoSpecialCondition(String specialCondition)
    {
        NodeList npcDialogList = doc.getElementsByTagName("dialog");

        //iterates over every dialog node to find one where the name matches with the npc's name
        for (int i=0; i < npcDialogList.getLength(); i++)
        {
            Element npcDialog = (Element) npcDialogList.item(i);

            if (npcDialog.getAttribute("name").equals( npc.getName() ))
            {
                NodeList interactions = npcDialog.getElementsByTagName("interaction");

                //iterates over every interaction
                for (int j=0; j < interactions.getLength(); j++ )
                {
                    Element interaction = (Element) interactions.item(j);
                    if (interaction.getAttribute("specialCondition").equals( specialCondition )){
                        return true;
                    }
                }
            }
        }
        return false;
    }


    //--- start of functions parsing the document and handling the dialog. ---

    /**
     * This function starts the conversation between the player and NPC.
     * 
     * @return whether the player wants to quit the game or not
     */
    //used https://mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/ code to help with the parsing of the xml file
    public boolean startConversation(){

        //these lines show the start and end of dialogue
        text.printDialogBar();

        NodeList npcDialogList = doc.getElementsByTagName("dialog");
        boolean dialogExists = false;

        //iterates over every dialog node to find one where the name matches with the npc's name
        for (int i=0; i < npcDialogList.getLength(); i++){

            Element npcDialog = (Element) npcDialogList.item(i);
            if (npcDialog.getAttribute("name").equals( npc.getName() )){
                exploreDialogTree(npcDialog,0, false);
                dialogExists = true;

                break;
            }
        }

        if (!dialogExists){
           text.printText("I have nothing to say...", 20);
        }

        text.printDialogBar();
        
        if (changesOccured){
            dialogDocManager.updateDocument();
        }

        return wantToQuit;
    }

    /**
     * This function prints the content of various consecutive <messages> nodes in the dialog.xml file
     * @param messageSource the parent element of all the <messages> sibling element nodes.
     * code from : https://stackoverflow.com/questions/17641496/how-can-i-get-the-siblings-of-an-xml-element-in-java-using-the-dom-parser
     * was used to iterate the sibling element nodes.
     */
    private void printMessages(Element messageSource){

        Node siblingMessage = messageSource.getElementsByTagName("message").item(0);

        //the message must be a direct child of the messageSource
        if (siblingMessage == null || siblingMessage.getParentNode() != messageSource){
            return;
        }


        //while there is a sibling <message> element node
        while ( siblingMessage != null && siblingMessage.getNodeName().equals("message")){
            text.printText( this.npc.getCapName() + ": " + dialogDocManager.cleanString(siblingMessage.getTextContent()), 20);
            siblingMessage = siblingMessage.getNextSibling();

            //used to skip over the non-element nodes (such as comment nodes)
            while (!(siblingMessage instanceof Element) && siblingMessage != null){
                siblingMessage = siblingMessage.getNextSibling();
            }
        }
    }

    /**
     * This function modifies the totalRepetitions attribute in an interaction by decreasing it by 1
     * as one repetition of the action has been performed. 
     * 
     * @param interaction the iteraction whose attribute is being modified
     * @param isRepeatableString the text value of the totalRepetitions attribute
     */
    private void modifyInteractionRepeatability(Element interaction, String isRepeatableString){

        //only modify the attribute if the interaction hasn't been repeated
        if (!isRepeatableString.equals("0") && isRepeatableString != ""){
            int totalRepetitions = Integer.parseInt(isRepeatableString) - 1;
            dialogDocManager.modifyAttribute(interaction, "totalRepetitions" , Integer.toString(totalRepetitions));
    
            changesOccured = true;
        }
    }

    /**
     * This function checks if a match string is present in a string composed of words
     * separated with "/" (e.g. : "yes/yeah")
     * @param string the string containing the different words
     * @param match the word to be matched with the different words in string 
     * @return whether match appears in the list of words from string
     */
    private boolean multipleStringEquals(String string, String match){
        if (string == ""){ return false; }

        String[] allStrings = string.split("[/]");

        for(String e : allStrings){
            if (e.equals(match)){
                return true;
            }
        }

        return false;
    }

    /**
     * This function explores the element nodes of the <dialog> tree of the respective NPC,
     * so as to print out the various responses depending on conditions of the player's location,
     * the npc's inventory, player's responses to the NPC's questions or even if an interaction happened 
     * already or not.
     * 
     * @param interactionNodes all of the element nodes of the possible interactions of an NPC with the player
     * @param depth the depth of recursion of the function
     * @param conditionSatisfied whether or not the condition attribute of the <interaction> has been 
     * satisfied already 
     */
    private void exploreDialogTree(Element interactionNodes, int depth, boolean conditionSatisfied){  

        String response = "";
        boolean condition, specialCondition, isRepeateable;

        if (wantToQuit){return;}

        Node interactionNode = interactionNodes.getElementsByTagName("interaction").item(0);
            
        //the interaction must be a direct child of the interactionNodes
        if (interactionNode == null || interactionNode.getParentNode() != interactionNodes){
            return;
        }

        //while there is a sibling <interaction> element node
        while ( interactionNode != null && interactionNode.getNodeName().equals("interaction")){
            
            //only one interaction of depth 0 can happen
            if (depth == 0 && hasInteractionHappened){
                    return;
            }
            
            Element interaction = (Element) interactionNode;

            //the condition is satisfied if the npc is in the room specified by the condition,
            //if the interaction is of depth > 0 or if the condition is 'default'
            condition = npc.isInCurrentRoom( interaction.getAttribute("condition") ) || 
                        conditionSatisfied || 
                        interaction.getAttribute("condition").equals("default");
        
            String isRepeatableString = interaction.getAttribute("totalRepetitions"); 

            //returns true only if the interaction hasn't been done already
            isRepeateable = !isRepeatableString.equals("0");

            String specialConditionText = interaction.getAttribute("specialCondition");

            //evaluates the special condition 
            if (!specialConditionText.equals("")){ 
                String[] allConditionWords = specialConditionText.split(" ");
                
                //if the functionName is an actual key in our allConditionFunctions map
                if (allConditionFunctions.containsKey(allConditionWords[0])){ 
                    if ( allConditionWords.length > 1){
                        String functionName = allConditionWords[0];
                        String parameterName = specialConditionText.substring(functionName.length()).trim();
                        
                        specialCondition = allConditionFunctions.get(functionName).checkCondition( this.npc, parameterName );
                    }
                    //if it doesn't have two words, meaning there's just the key of the function
                    else{
                        specialCondition = allConditionFunctions.get(allConditionWords[0]).checkCondition( this.npc, "" );
                    }
                }else{
                    specialCondition = false;
                }
            }
            else{ specialCondition = true; }    //if no special condition automatically set it to true

            if (condition && isRepeateable){
                if (specialCondition){
                    if (depth == 0 && !hasInteractionHappened){ 
                        hasInteractionHappened = true;
                    }

                    this.printMessages( interaction );                    
                    
                    //the choice must be a direct child of the interaction
                    Node siblingChoice = interaction.getElementsByTagName("choice").item(0);
                    if (siblingChoice != null && siblingChoice.getParentNode() != interaction) { siblingChoice = null; }

                    //a further interaction must be a direct child of the interaction
                    Node furtherInteraction = interaction.getElementsByTagName("interaction").item(0);
                    if (furtherInteraction != null && furtherInteraction.getParentNode() != interaction) { furtherInteraction = null; }

                   //waits for user input only if there are choices available
                    if (siblingChoice != null){
                        System.out.print("> ");
                        response = reader.nextLine().toLowerCase();
                    
                    //if no choices are available we skip to the next interaction
                    }else if(furtherInteraction == null){
                        modifyInteractionRepeatability(interaction, isRepeatableString);    //the interaction has occured so the hasBeenRepeated attribute must be updated
                        interactionNode = interactionNode.getNextSibling();

                        //used to skip over the non-element nodes (such as comment nodes)
                        while (!(interactionNode instanceof Element) && interactionNode != null){
                            interactionNode = interactionNode.getNextSibling();
                        }
                        if (interactionNode != null && interactionNode.getParentNode() != interaction) { furtherInteraction = null; }
                        continue;
                    }

                    //if the user desires to quit the game mid-conversation (but not during the saving process)
                    if ((dialogDocManager.cleanString(response).equals("quit") || dialogDocManager.cleanString(response).equals("q") ) && !npc.getName().equals("save")){ 
                        wantToQuit = true;
                        return;
                    }

                    //while we have a choice element node
                    while ( siblingChoice != null && siblingChoice.getNodeName().equals("choice") ){
                        
                        Element siblingChoiceElement = (Element) siblingChoice;
                        
                        //if the user response matches one of the choices, or one of the choices is a "default" one
                        if (multipleStringEquals(siblingChoiceElement.getAttribute("id"), response) || siblingChoiceElement.getAttribute("id").equals("default")){
                            this.printMessages(siblingChoiceElement);
                            exploreDialogTree(siblingChoiceElement, depth + 1, true);

                            if (!siblingChoiceElement.getAttribute("id").equals("default")){
                            modifyInteractionRepeatability(interaction, isRepeatableString); //the interaction has occured so the hasBeenRepeated attribute must be updated
                            }

                            return;
                        }
                        
                        siblingChoice = siblingChoice.getNextSibling();

                        //used to skip over the non-element nodes (such as comment nodes)
                        while (!(siblingChoice instanceof Element) && siblingChoice != null){
                            siblingChoice = siblingChoice.getNextSibling();
                        }
                    }

                    //while we have an interaction element node
                    while ((furtherInteraction) != null && furtherInteraction.getNodeName().equals("interaction") ){
                        
                        exploreDialogTree(interaction, depth + 1, true);
                        modifyInteractionRepeatability(interaction, isRepeatableString); //the interaction has occured so the hasBeenRepeated attribute must be updated

                        if (wantToQuit) { return; }
                        furtherInteraction = furtherInteraction.getNextSibling();

                        //used to skip over the non-element nodes (such as comment nodes)
                        while (!(siblingChoice instanceof Element) && siblingChoice != null){
                            siblingChoice = siblingChoice.getNextSibling();
                        }
                    }
                }
                //if the special condition isn't satisfied
                else{
                    String unmatched = interaction.getAttribute("unmatched");

                    //if the unmatched attribute exists
                    if (!unmatched.equals("")){ 
                        text.printText(unmatched, 20); 
                        return;
                    }
                }
            }
            
            interactionNode = interactionNode.getNextSibling();

            //used to skip over the non-element nodes (such as comment nodes)
            while (!(interactionNode instanceof Element) && interactionNode != null){
                interactionNode = interactionNode.getNextSibling();
            }
        }
    }
}