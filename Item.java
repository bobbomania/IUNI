// PPA - 1 / Assignment 2 - World of Zuul
// Gabriele Trotta (K21006956)

/**
 * This class is part of the project called "IUNI".
 * It's a simple text adventure game, where you can walk around 
 * and interact with characters and items, in order to regain your memory.
 * 
 * This class contains information about an item that can be located in the user's inventory,
 * the room of the house, or an NPC's inventory.
 *
 * @author Gabriele Trotta (K21006956)
 * @version 1.0 15/11/2021
 */
public class Item
{
   private String description;
   private String[] action;
   private int weight;
   private boolean holdable;

   private int memoriesGiven;   //the memory points given when interacting with the item
   private String position;     //the item's position in the room's map
   
   /**
    * Constructor for an Item object
    * @param description description is a short description of the item.

    * @param action action is what the item does when interacted with. Its composed of two elements:
    * 1) the action's "identifiers" : the identifier and parameters for an action function inside of the main class
    * 2) the action's description, that prints out what the aciton does.

    * @param weight weight is how heavy the item is
    * @param holdable holdable tells wheteher you can hold the item or not
    */
   public Item(String description, String[] action, int weight, boolean holdable){
        this.description = description;
        this.action = action;
        this.weight = weight;
        this.holdable = holdable;

        this.memoriesGiven = 0;
   }

    /**
    * Sets a random position for the item within the room map, of the format '[col],[row]'
    * @param dimensions the dimensions of the room in which the item is standing in
    */
    public void setRandomPosition(String dimensions){

        int width = Integer.parseInt(dimensions.split(",")[0]);
        int height = Integer.parseInt(dimensions.split(",")[1]);

        int randomX = (int) (Math.random()*(width-3)) + 2;
        int randomY = (int) (Math.random()*(height-3)) + 2;

        this.position = String.format("%d,%d",randomX, randomY);
    }

      
   /**
    * @return the description of the item
    */
   public String getDescription(){
       return this.description;
   }
   
   /**
    * @return the weight of the item
    */
   public int getWeight(){
       return this.weight;
   }

   /**
    * @return the position of the item in the room map in the format 'col,row'
    */
   public String getPosition(){
       return this.position;
   }
   
   /**
    * @return whether the item can be held or not
    */
   public boolean isHoldable(){
       return this.holdable;
   }
   
   /**
    * @return the command word associated with the action (e.g interact, sell, ...)
    */
   public String getAction(){
       return this.action[0].split(" ")[0];
   }

   /**
    * @return the specific description of the action 
    */
   public String getActionContent(){
       return this.action[1];
   }

   /**
    * @return the action's parameters for the action's function, from the action's identifiers
    */
   public String getActionParameters(){
       String[] actionWords = this.action[0].split(" ");
       return this.action[0].substring(actionWords[0].length()).trim();
   }

   /**
    * @return whether the action has a description or not
    */
   public boolean hasActionContent(){
       return !this.action[1].equals("");
   }

   /**
    * @return whether the item has a position or not
    */
   public boolean hasPosition(){
       return !this.position.equals("");
   }

   /**
    * Sets the position of the item in the room's map in the format 'col,row'
    * @param position the position of the item
    */
   public void setPosition(String position){
       this.position = position;
   }

   /**
    * sets the memory points given upon interacting with the item
    * @param mem the memory points set
    */
   public void setMemoriesGiven(int mem){
       this.memoriesGiven = mem;
   }

   /**
    * @return the memory points given upon interacting with the item
    */
   public int getMemoriesGiven(){
       return this.memoriesGiven;
   }
}
