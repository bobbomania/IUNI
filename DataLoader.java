// PPA - 1 / Assignment 2 - World of Zuul
// Gabriele Trotta (K21006956)

import java.util.HashMap;
import java.util.Random;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class is part of the project called "IUNI".
 * It's a simple text adventure game, where you can walk around 
 * and interact with characters and items, in order to regain your memory.
 * 
 * This class handles the managing and loading of all the game's data
 * When the game is started, the data is loaded from data.xml.
 * The first time the game is loaded, or when the data.xml file is corrupted, 
 * data.xml is the same as CONST_DATA.xml.
 * 
 * In this file, "at the start of the game" is referencing both the game if we save it, 
 * or when we start a completely new one.
 */
//npc change room attribute
public class DataLoader 
{
    private XmlManager dataDocManager;                  //the object responsible for the xml file

    private HashMap<String, Room> allRooms;             //hashmap containing all the room names in the keys, and the Room objects as values
    private HashMap<String, NPC> allNPCs;               //hashmap containing all the npc names in the keys, and the NPC objects as values

    private HashMap<String, Item> playerInventory;
    private int memoryScore;                            //the player's total "mem points"

    private Room currentRoom;
    private Document doc;                               //object containing the xml file's data

    /**
     * This creates our DataLoader object, initializing all of our data to being zero or empty
     */
    public DataLoader(){
        allRooms = new HashMap<>();
        allNPCs = new HashMap<>();
        playerInventory = new HashMap<>();

        memoryScore = 0;

        dataDocManager = new XmlManager("./xml_files/mutables/data.xml",
                                        "./xml_files/backups/backup_data.xml",false);

        doc = dataDocManager.getXmlDocument();
    }
    
    /**
     * @return the map containing all the npcs
     */
    public HashMap<String, NPC> getAllNpcs(){
        return allNPCs;
    }

    /**
     * @return the current room the player is at the start of the game
     */
    public Room getCurrentRoom(){
        return currentRoom;
    }

    /**
     * @return a random room from the hashMap
     */
    public Room getRandomRoom(){
        Random random = new Random();
        
        Object[] allRoomsArray = this.allRooms.values().toArray();

        return (Room) allRoomsArray[random.nextInt(allRoomsArray.length)];
    }

    /**
     * @return the map containing the player's inventory at the start of the game
     */
    public HashMap<String, Item> getPlayerInventory(){
        return playerInventory;
    }

    /**
     * @return the memory points of the player at the start of the game
     */
    public int getMemoryScore(){
        return memoryScore;
    }

    /**
     * resets the data.xml file to match the CONST_DATA.xml one
     */
    public void reset(){
        dataDocManager.resetDocument("./xml_files/mutables/data.xml", "./xml_files/constants/CONST_DATA.xml");
    }

    /**
     * changes the allData's "isCorrupt" attribute to false
     */
    public void isNotCorrupt(){
        dataDocManager.isNotCorrupt();
    }

    /**
     * Saves the game's data to the data.xml file
     * @param currentRoomName the name of the room the player is standing in after saving the game
     * @param newMemoryScore the memory points of the player after saving the game
     */
    public void save(String currentRoomName, int newMemoryScore){        
        NodeList allNPCList = doc.getElementsByTagName("npc");

        //for each npc we save its room's map position
        for (NPC npc : allNPCs.values()){
            for (int i=0; i < allNPCList.getLength(); i++){
                Element npcElement = (Element) allNPCList.item(i);

                if (npc.getName().equals(npcElement.getAttribute("name")))
                {
                    dataDocManager.modifyAttribute(npcElement, "room", npc.getCurrentRoomName());
                    dataDocManager.modifyAttribute(npcElement, "position", npc.getPosition());
                }
            }
        }

        NodeList allItemsList = doc.getElementsByTagName("item");

        //we iterate through all the items by iterating through the rooms, the npc's and the 
        //player's items.

        //for each item, we save whether the item is in a room, 
        //an npc's inventory or the player's inventory (when both 'room' and 'npc' attributes are empty)
        //and other item related data in their respective attributes
        itemLoop: for (int i=0; i < allItemsList.getLength(); i++){
            Element itemNode = (Element) allItemsList.item(i);
            String itemName = itemNode.getAttribute("name");
            
            //by default it's in the player's inventory
            dataDocManager.modifyAttribute(itemNode, "room", "");
            dataDocManager.modifyAttribute(itemNode, "npc", "");

            for (Room room : allRooms.values()){
                if (room.hasItem(itemName)){
                    dataDocManager.modifyAttribute(itemNode, "room", room.getName());
                    dataDocManager.modifyAttribute(itemNode, "memoryGiven", Integer.toString(room.getItem(itemName).getMemoriesGiven()));
                    dataDocManager.modifyAttribute(itemNode, "position", room.getItem(itemName).getPosition());
                    
                    continue itemLoop;
                }
            }

            for (NPC npc : allNPCs.values()){
                if (npc.hasItem(itemName)){
                    dataDocManager.modifyAttribute(itemNode, "npc", npc.getName());
                    dataDocManager.modifyAttribute(itemNode, "memoryGiven", Integer.toString(npc.getItem(itemName).getMemoriesGiven()));
                    dataDocManager.modifyAttribute(itemNode, "position", npc.getItem(itemName).getPosition());

                    continue itemLoop;
                }
            }

            for (Item item : playerInventory.values()){
                dataDocManager.modifyAttribute(itemNode, "memoryGiven", Integer.toString(item.getMemoriesGiven()) );
            }
        }

        Element player = (Element) doc.getElementsByTagName("player").item(0);

        dataDocManager.modifyAttribute(player, "current", currentRoomName);
        dataDocManager.modifyAttribute(player, "memoryScore", Integer.toString(newMemoryScore));

        //updates the data.xml document with the new changes
        dataDocManager.updateDocument();
    }

    /**
     * Sets exits to a room, given all the exits to the room in the format:
     * "[direction] [roomName]/[other_exits]"
     * @param room the room that we want to set the exits to
     * @param allExits all the exits to the room
     * @return the room with all the exits given set
     */
    private Room setExit(Room room, String allExits){
        String[] brokenKeywords;

        for (String keyWords : allExits.split("[/]")){
            brokenKeywords = keyWords.split(" ");

            room.setExit(brokenKeywords[0], allRooms.get(brokenKeywords[1]));
        }

        return room;
    }


    /**
     * This function loads in all the data regarding rooms from the data.xml file
     * into the allRooms map
     */
    public void loadAllRoomData(){

        String mapDimension = ((Element)doc.getElementsByTagName("map").item(0)).getAttribute("dimension");

        Room tempRoom;

        NodeList allRoomsList = doc.getElementsByTagName("room");

        for (int i=0; i < allRoomsList.getLength(); i++){

            Element roomElement = (Element) allRoomsList.item(i);
            String roomName = roomElement.getAttribute("name");
            String roomDescription = dataDocManager.cleanString(roomElement.getTextContent());
            String roomDimension = (roomElement.hasAttribute("dimension")) ? roomElement.getAttribute("dimension") : mapDimension;

            tempRoom = new Room( roomName, roomDescription, roomDimension );
            
            //all the fields that don't apply to all the rooms
            if (roomElement.getAttribute("isLocked").equals("true")){
                tempRoom.toggleLocked();
                tempRoom.setKey(roomElement.getAttribute("key"));
            }

            if (!roomElement.getAttribute("memoryRequired").equals("")){
                tempRoom.setMemoriesRequired(Integer.parseInt( roomElement.getAttribute("memoryRequired") ));
            }

            if (!roomElement.getAttribute("isEnd").equals("")){
                tempRoom.setEnd(roomElement.getAttribute("isEnd"));
            }

            if (!roomElement.getAttribute("noInvestigatePerson").equals("")){
                tempRoom.setNoInvestigatePerson(roomElement.getAttribute("noInvestigatePerson"));
            }

            allRooms.put(tempRoom.getName(), tempRoom);
        }

        //here we set the room's exits
        for (int i=0; i < allRoomsList.getLength(); i++){
            Element roomElement = (Element) allRoomsList.item(i);

            tempRoom = allRooms.get(roomElement.getAttribute("name"));
            tempRoom = setExit(tempRoom, roomElement.getAttribute("exits"));
            allRooms.put(tempRoom.getName(), tempRoom);
        }
    }

    /**
     * This function loads in all the data regarding NPCs and the player from the data.xml file
     * into the allNPCs and the player's respective data fields
     */
    public void loadAllNPCData(){
        NodeList allNPCsList = doc.getElementsByTagName("npc");

        for (int i=0; i < allNPCsList.getLength(); i++){
            Element elementNPC = (Element) allNPCsList.item(i);
            String tempNPCName = elementNPC.getAttribute("name");

            String position = elementNPC.getAttribute("position");

            NPC tempNPC = new NPC(tempNPCName, elementNPC.getAttribute("room"));
            tempNPC.setPosition(position);

            allNPCs.put(tempNPCName, tempNPC);
        }

        Element playerElement = (Element) doc.getElementsByTagName("player").item(0);

        this.currentRoom = allRooms.get(playerElement.getAttribute("current"));
        this.memoryScore = Integer.parseInt(playerElement.getAttribute("memoryScore"));

    }

    /**
     * This function loads in all the data regarding items from the data.xml file
     * into the respective rooms, NPCs or player's inventories
     */
    public void loadAllItemData(){

        NodeList allItemsList = doc.getElementsByTagName("item");
        String itemActionContent, itemDescription;
        String[] itemAction;
        int weight;

        for (int i=0; i < allItemsList.getLength(); i++){
            Element itemNode = (Element) allItemsList.item(i);
            
            //if there's no description leave empty
            if (itemNode.getElementsByTagName("description").getLength() > 0){
                itemDescription = dataDocManager.cleanString(itemNode.getElementsByTagName("description").item(0).getTextContent());
            }else{
                itemDescription = "";
            }
            
            //if there's no actions, leave empty
            if (itemNode.getElementsByTagName("action").getLength() > 0){
                itemActionContent = dataDocManager.cleanString(itemNode.getElementsByTagName("action").item(0).getTextContent());
            }else{
                itemActionContent = "";
            }

            //if there's no weight, set it to 0
            if (itemNode.getAttribute("weight").isEmpty()){
                weight = 0;
            }
            else{
                weight = Integer.parseInt(itemNode.getAttribute("weight"));
            }

            boolean isHoldable = itemNode.getAttribute("isHoldable").equals("true");

            String itemName = itemNode.getAttribute("name");

            String roomName = itemNode.getAttribute("room");
            String npcName = itemNode.getAttribute("npc");

            String itemGivesMemories = itemNode.getAttribute("memoryGiven");
            String position = itemNode.getAttribute("position");

            //if there's no content for the item's action leave blank
            if (itemActionContent.equals("")){
                itemAction = new String []{"",""};
            }else{
                itemAction = new String []{ itemNode.getAttribute("action") ,itemActionContent};
            }

            Item tempItem = new Item(itemDescription, itemAction, weight, isHoldable);
            tempItem.setPosition(position);

            //if the item gives memories, set them
            if (!itemGivesMemories.equals("")){
                tempItem.setMemoriesGiven(Integer.parseInt(itemGivesMemories));
            }

            //if the item xml element doesn't have a roomName or an npcName then its in the 
            //player's inventory
            if (!roomName.equals("")){
                allRooms.get(roomName).setItem(itemName, tempItem, true);
            }else if (!npcName.equals("")){
                allNPCs.get(npcName).setItem(itemName, tempItem);
            }else
            {
                playerInventory.put(itemName, tempItem);
            }
        }
    }
}