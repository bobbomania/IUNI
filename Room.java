// PPA - 1 / Assignment 2 - World of Zuul
// Gabriele Trotta (K21006956)

import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is part of the project called "IUNI".
 * It's a simple text adventure game, where you can walk around 
 * and interact with characters and items, in order to regain your memory.  
 *
 * A "Room" represents one location in the scenery of the game.  It is 
 * connected to other rooms via exits.  For each existing exit, the room 
 * stores a reference to the neighboring room.
 * 
 * @author  Michael Kölling, David J. Barnes and Gabriele Trotta (K21006956)
 * @version 2016.02.29
 */

public class Room 
{
    private String name;
    private String description;

    private String dimensions;
    private ArrayList<String[]> positions;      //the positions of every object on the room's ASCII map
                                                //they are stored in an array of length 2 where the first element
                                                //is the symbol of the object, and the second element is the position itself

    private HashMap<String, Room> exits;        // stores exits of this room.
    private HashMap<String, Item> items;        // stores the items of this room

    private boolean locked;                 
    private String key;                     //the name of the key unlocking the room
    private int memoriesRequired;           //how many mem points are required by the player to enter this room
    private String isEnd;                   //specifies the end upon entering the room
    private String noInvestigatePerson;     //specifies a person that doesn't allow investigating inside the room

    /**
     * Create a room described "description" and named "name". Initially, it has
     * no exits, nor items nor positions of objects inside the map. 
     *
     * Initially it is unlocked, can be investigated into, 
     * has no end nor any memory requirements.
     * 
     * @param name the name of the room
     * @param description The room's description.
     * @param dimensions the dimensions in the room in "cols,rows" shown on the terminal
     */
    public Room(String name, String description, String dimensions) 
    {
        this.name = name;
        this.description = description;

        this.dimensions = dimensions;
        this.positions = new ArrayList<>();

        this.locked = false;
        this.memoriesRequired = 0;
        this.key = "";
        this.isEnd = "";
        this.noInvestigatePerson = "";
        
        exits = new HashMap<>();
        items = new HashMap<>();
    }
    

    /**
     * Define an exit from this room.
     * @param direction The direction of the exit.
     * @param neighbor  The room to which the exit leads.
     */
    public void setExit(String direction, Room neighbor) 
    {
        exits.put(direction, neighbor);
    }

    /**
     * Define an item in this room., by also setting its position inside the room's map
     * @param itemName The name of the item (e.g television).
     * @param item The actual item in the room.  
     */
    public void setItem(String itemName, Item item){
        //while the item doesn't have a position or if its position is the same as another object
        //inside the room, then give the item a random position
        while (!item.hasPosition() || !this.addToPositions("X", item.getPosition())){
            item.setRandomPosition(this.dimensions);
        }

        items.put(itemName, item);
    }

    /**
     * Define an item in this room, overrides the other method when loading in the data from the
     * data.xml file.
     * @param itemName The name of the item
     * @param item The actual item in the room
     * @param isStarting whether the game is loading in the data from the data.xml file 
     * (used to override the method above)
     */
    public void setItem(String itemName, Item item, boolean isLoading){
        //if the item's position isn't specified when loading the data, assign a random position inside the room
        if (!item.hasPosition()) { 
            item.setRandomPosition(this.dimensions); 
            while (!this.addToPositions("X", item.getPosition())){
                item.setRandomPosition(this.dimensions);
            }
        }else{
            this.addToPositions("X", item.getPosition());
        }

        items.put(itemName,item);
    }

    /**
     * @return The short description of the room
     * (the one that was defined in the constructor).
     */
    public String getShortDescription()
    {
        return description;
    }
    
    /**
     * @return the name of the room
     */
    public String getName(){
        return name;
    }

    /**
     * Return a description of the room in the form:
     *     You are in the kitchen.
     *     Exits: north west
     * @return A long description of this room
     */
    public String getLongDescription()
    {
        return "You find yourself " + description + "\n\n" + getExitString();
    }

    /**
     * Return a string describing the room's exits, for example
     * "Exits: north west".
     * @return Details of the room's exits.
     */
    public String getExitString()
    {
        String returnString = "Exits:";
        Set<String> keys = exits.keySet();
        for(String exit : keys) {
            returnString += " " + exit;
        }
        return returnString;
    }
    
    /**
     * @return the string containing the list of the items inside the room
     */
    public String getItemsString()
    {
        
        if (items.size() == 0){ return "There is nothing special here."; }
        
        String itemsString  = "It seems that there ";
        itemsString += (items.size() > 1) ? "are:\n" : "is:\n";
        
        Set<String> keySet = items.keySet();
        
        for (String item : keySet){
            itemsString += "* " + item + "\n";
        }
        
        return itemsString;
    }

    /**
     * Return the room that is reached if we go from this room in direction
     * "direction". If there is no room in that direction, return null.
     * @param direction The exit's direction.
     * @return The room in the given direction.
     */
    public Room getExit(String direction) 
    {
        return exits.get(direction);
    }

    /**
     * If the room has the specified exit given a direction
     * @param direction the direction where the exit should be
     * @return whether the room has the exit in the direction given
     */
    public boolean hasExit(String direction){
        return exits.get(direction) != null;
    }
    
    /**
     * @param itemName the name of the item
     * @return the item stored inside the room based on the item name. 
     * returns null if the item isn't in the room's item map
     */
    public Item getItem(String itemName)
    {
        return items.get(itemName);
    }

    /**
     * @return the name of the key opening the room
     */
    public String getKey(){
        return this.key;
    }

    /**
     * @return the dimensions of the room in '[width],[height]' format
     */
    public String getDimensions(){
        return dimensions;
    }

    /**
     * 
     * @param itemName the name of the item checked
     * @return whether the room contains a certain item
     */
    public boolean hasItem(String itemName){
        return (items.get(itemName) != null);
    }

    /**
     * remove the item from the room's item map
     * @param itemName
     * @return 
     */
    public Item removeItem(String itemName){
        Item itemRemoved = items.remove(itemName);
        if (itemRemoved != null){
            this.removePosition(itemRemoved.getPosition());
        }

        return itemRemoved;
    }

    /**
     * @return whether the room is locked or not
     */
    public boolean isLocked(){
        return this.locked;
    }

    /**
     * @param newKey sets the key locking the room
     */
    public void setKey(String newKey){
        this.key = newKey;
    }

    /**
     * toggles whether the room is locked or not
     */
    public void toggleLocked(){
        this.locked = !locked;
    }

    /**
     * @return how many memory points are needed to enter the room
     */
    public int getMemoriesRequired(){
        return memoriesRequired;
    }

    /**
     * @param newMemoryScore sets the memories required to enter the room
     */
    public void setMemoriesRequired(int newMemoryScore){
        this.memoriesRequired = newMemoryScore;
    }

    /**
     * @param end sets the end string that identifies the respective end upon entering the room
     */
    public void setEnd(String end){
        this.isEnd = end;
    }

    /**
     * @return the name of the end triggered upon entering the room
     */
    public String getEnd(){
        return isEnd;
    }

    /**
     * @return whether the room has an end or not
     */
    public boolean hasEnd(){
        return (!isEnd.equals(""));
    }

    /**
     * @param person sets the person name that doesn't allow an investigation within the room
     */
    public void setNoInvestigatePerson(String person){
        this.noInvestigatePerson = person;
    }

    /**
     * @return the name of the person that doesn't allow an investigation within the room
     */
    public String getNoInvestigatePerson(){
        return noInvestigatePerson;
    }

    //Functions handling the room's map's positions.

    /**
     * Checks if the given position is the same as the position of an item
     * @param position the position to be checked
     * @return whether the position is in the same position of an item
     */
    private boolean checkSamePosition(String checkPosition){
        for (String[] position : this.positions){
            if (position[1].equals(checkPosition)) {return true;}
        }

        return false;
    }

    /**
     * @return all the positions of the objects inside the room's map
     */
    public ArrayList<String[]> getPositions(){
        return this.positions;
    }

    /**
     * Adds a position, given a symbol, to the room's map. If the position is the same
     * as another position in the room's map, then return false and the position isn't added
     * @param mapSymbol the symbol indicating the position on the map
     * @param newPosition the position that we want to add
     * @return whether the position has been added or not
     */
    public boolean addToPositions(String mapSymbol, String newPosition){
        if (!checkSamePosition(newPosition)){
            this.positions.add(new String[] {mapSymbol, newPosition});
            return true;
        }
        return false;
    }

    /**
     * Remove a given position from the positions in the room's map
     * @param position the position we want to remove
     */
    public void removePosition(String position){
        for (String[] posBundle : this.positions){
            if (posBundle[1].equals(position)){
                this.positions.remove(posBundle);
                return;
            }
        }
    }

}

