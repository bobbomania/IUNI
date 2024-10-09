// PPA - 1 / Assignment 2 - World of Zuul
// Gabriele Trotta (K21006956)

import java.util.HashMap;
import java.util.Set;

/**
 * This class is part of the project called "IUNI".
 * It's a simple text adventure game, where you can walk around 
 * and interact with characters and items, in order to regain your memory.
 * 
 * Class Player - the user playing the game
 * 
 * This class handles all of the information regarding the player
 * and its object within the scope of the game
 * 
 * @author Gabriele Trotta (K21006956)
 * 
 */
public class Player {
    private Room currentRoom;
    private String currentRoomName;
    private Room pastRoom;

    private HashMap<String, Item> inventory;
    private int memoryScore;                    //how many "mem points" the player has

    /**
     * Creates a Player object that contains all of the information that regards the player
     * @param currentRoom the room in which the player is standing in
     * @param inventory the player's inventory
     * @param memoryScore a measure to understand how much the main character has 'remembered'
     */
    public Player(Room currentRoom, HashMap<String, Item> inventory, int memoryScore){
        this.currentRoom = currentRoom;
        this.currentRoomName = currentRoom.getName();
        this.inventory = inventory;
        this.memoryScore = memoryScore;
    }

    /**
     * @return the current room the player is standing in
     */
    public Room getCurrentRoom(){
        return this.currentRoom;
    }

    /**
     * @return the name of the current room the player is standing in
     */
    public String getCurrentRoomName(){
        return this.currentRoomName;
    }

    /**
     * @return the past room in which the player found themselves in
     */
    public Room getPastRoom(){
        return this.pastRoom;
    }

    /**
     * Sets the current room to the room specified and thus updates all fields related to it
     * @param room the new room in which the player is standing in
     */
    public void setCurrentRoom(Room room){
        this.pastRoom = this.currentRoom;
        this.currentRoom = room;
        this.currentRoomName = room.getName();

        System.out.println(currentRoom.getLongDescription());
    }

    /**
     * @param itemName the item name searched
     * @return the item inside the current room, specified by its item name
     */
    public Item getCurrentRoomItem(String itemName){
        return this.currentRoom.getItem(itemName);
    }

    /**
     * This function 'pops' the item from the current room, thus returning it and removing it 
     * from the current room
     * @param itemName the name of the item we are removing
     * @return the item that is removed
     */
    public Item popCurrentRoomItem(String itemName){
        return this.currentRoom.removeItem(itemName);
    }

    /**
     * set the item in the current room
     * @param itemName the name of the item that is set
     * @param item the item object that is set
     */
    public void setCurrentRoomItem(String itemName, Item item){
        this.currentRoom.setItem(itemName, item); 
    }

    /**
     * prints out all of the items inside the current room
     */
    public void showItemsOfCurrentRoom(){
        System.out.println(currentRoom.getItemsString());
    }

    /**
     * adds the item specified inside the player's inventory
     * @param itemName the name of the item set
     * @param item the item object set inside the inventory
     */
    public void setItem(String itemName, Item item){
        inventory.put(itemName, item);
    }

    /**
     * gets the item specified by its name
     * @param itemName the name of the item to be gotten
     * @return the item searched
     */
    public Item getItem(String itemName){
        return inventory.get(itemName);
    }

    /**
     * removes an item from the inventory
     * @param itemName the name of the item removed
     */
    public void removeItem(String itemName){
        inventory.remove(itemName);
    }

    /**
     * this function tests if the player has an item in their inventory
     * @param itemName the name of the item tested
     * @return whether the item is in the inventory
     */
    public boolean hasItem(String itemName){
        return (inventory.get(itemName) != null);
    }

    /**
     * calculates the total weight of the player's inventory
     * @return the inventory weight
     */
    public int getInventoryWeight(){
        int weight = 0;
        for (Item item : inventory.values()){
            weight += item.getWeight();
        }

        return weight;
    }
    
    /**
     * gets the how many items the player is carrying
     * @return the size of the inventory
     */
    public int getInventorySize(){
        return this.inventory.size();
    }

    /**
     * @return the player's memory score
     */
    public int getMemoryScore(){
        return memoryScore;
    }

    /**
     * adds the number of points specified to the player's total memory score
     * @param number the number of points added
     */
    public void addMemoryScore(int number){
        if (number != 0){
            System.out.println(String.format("You just gained %d mem points.", number));
            this.memoryScore += number;
        }
    }

    /**
     * shows the items inside the player's inventory
     */
    public void showInventory(){
        String itemsString  = "It seems that there ";
        itemsString += (this.getInventorySize() > 1) ? "are:\n" : "is:\n";
        
        Set<String> keySet = inventory.keySet();
    
        for (String key : keySet){
            itemsString += "* " + key + "\n";
        }
        
        System.out.println(itemsString);
    }
}
