// PPA - 1 / Assignment 2 - World of Zuul
// Gabriele Trotta (K21006956)

import java.util.HashMap;

/**
 * This class is part of the project called "IUNI".
 * It's a simple text adventure game, where you can walk around 
 * and interact with characters and items, in order to regain your memory.
 * 
 * This class will represent all Non-Playable Characters (NPC) that the player may interact with.
 * They can all start a dialogue, or receive an item from the player.
 *
 * @author Gabriele Trotta (K21006956)
 * @version 1.0 17/11/21
 */
public class NPC
{
    private String name;
    private String currentRoom;             

    private String position;
    private boolean hasMoved;               //whether the npc has moved or not
    
    private HashMap<String, Item> inventory;
    
    /**
     *  Here we create the NPC object
     * @param name name is the name of the NPC
     * @param startingRoom startingRoom is the room in which the NPC is at when created
     */
    public NPC(String name, String startingRoom){
        this.name = name;
        this.currentRoom = startingRoom;

        //when the npc has been created it has been "moved" inside the room
        this.hasMoved = true;
        this.position = "";
        
        inventory = new HashMap<>();  
    }

    /**
     * Sets a random position for the NPC within the room map, of the format '[col],[row]'
     * @param dimensions the dimensions of the room in which the npc is standing in
     */
    public void setRandomPosition(String dimensions){

        int width = Integer.parseInt(dimensions.split(",")[0]);
        int height = Integer.parseInt(dimensions.split(",")[1]);
        
        //to avoid the npc from getting into the walls of the room map
        int randomX = (int) (Math.random()*(width-3)) + 2;
        int randomY = (int) (Math.random()*(height-3)) + 2;

        this.position = String.format("%d,%d",randomX, randomY);
    }

    /**
     * Get the name of the NPC
     * @return the name
     */
    public String getName(){
        return name;
    }

    /**
     * @return whether the npc has recently changed rooms or not
     */
    public boolean hasMoved(){
        return hasMoved;
    }

    /**
     * @return whether the npc has a nono-empty position field
     */
    public boolean hasPosition(){
        return !position.equals("");
    }

    /**
     * Sets the npc's position inside the room's map in the format '[col,row]'
     * @param position the position of the npc
     */
    public void setPosition( String position ){
        this.position = position;
    }

    /**
     * toggles the hasMoved field from true to false, and from false to true
     */
    public void toggleHasMoved(){
        hasMoved = !hasMoved;
    }

    /**
     * @return the capitalized version of the npc's name
     */
    public String getCapName(){
        return (this.name.substring(0, 1).toUpperCase() + this.name.substring(1));
    }
    
    /**
     * @return the npc's position within the room's map
     */
    public String getPosition(){
        return (this.position);
    }

    /**
     * Adds an item to the NPC's inventory
     * @param itemName the name of the item
     * @param item the item object itself
     */
    public void setItem(String itemName, Item item){
        inventory.put(itemName, item);
    }
    
    /**
     * @param roomName the name of the room
     * @return returns whether NPC is located in the given room
     */
    public boolean isInCurrentRoom(String roomName){
        return ( roomName.equals(this.currentRoom) );
    }
    
    /**
     * @return whether the NPC has the item with given item name
     * @param itemName the name of the item 
     */
    public boolean hasItem(String itemName){
        return ( inventory.get(itemName) != null );
    }

    /**
     * gets the item from the npc's inventory
     * @param itemName the name of the item gotten
     * @return the item gotten through the item name
     */
    public Item getItem(String itemName){
        return (inventory.get(itemName));
    }

    /**
     * removes an item from the npc's inventory
     * @param itemName the name of the item removed
     */
    public void removeItem(String itemName) {
        inventory.remove(itemName);
    }

    /**
     * sets the current room in which the npc is standing in
     * @param roomName the name of the room the npc is standing in
     */
    public void setCurrentRoom(String roomName){
        System.out.println(this.getCapName() + " decides to leave the " + this.currentRoom);
        this.currentRoom = roomName;
        this.hasMoved = true;
    }

    /**
     * @return the name of the room the npc is currently standing in
     */
    public String getCurrentRoomName(){
        return currentRoom;
    }
}
