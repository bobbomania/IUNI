// PPA - 1 / Assignment 2 - World of Zuul
//Gabriele Trotta (K21006956)

import java.util.HashMap;

/**
 *  This class is the main class of the game "IUNI"
 * It's a simple text adventure game, where you can walk around 
 * and interact with characters and items, in order to regain your memory.
 * This latter is measured with "mem points", which could be gained with interactions.
 *  
 *  To play this game, create an instance of this class and call the "play"
 *  method.
 */

 /**
  * This interface is used as an adapter to store the various acitons the player can perform with an
  * item.
  */
abstract interface itemActionFunction{
    /**
     * The function to be defined that performs an action for the specified item
     * @param item the item we're using
     */
    public void performAction(Item item, String parameters);
}
 
 /** 
 *  This main class creates and initialises all the others: it creates all
 *  rooms, creates the parser and starts the game.  It also evaluates and
 *  executes the commands that the parser returns.
 * 
 * In this project we abbreviate Non-Playable-Character as NPC or npc
 * 
 * @author  Michael Kölling and David J. Barnes and Gabriele Trotta (K21006956)
 * @version 2016.02.29
 */


public class Game 
{
    private Parser parser;
    private Player player;

    private HashMap<String, NPC> allNpcs;           //map containing all the NPCs of the game, with their name as a key

    private DialogManager currentDialog;            //the object handling the current dialog
    private DataLoader dataLoader;                  //the object responsible for loading in all the game's data and saving it

    private HashMap<String, itemActionFunction> allItemActionFunctions; //map that contains as a key the name of an action and 
                                                                        //as a value it contains a function "performing" the action of an item

    private String end;                             //used to identify various endings
        
    /**
     * Create the game, load in all the game data from the data.xml file and initialize room map.
     */
    public Game() 
    {
        dataLoader = new DataLoader();

        dataLoader.loadAllRoomData();
        dataLoader.loadAllNPCData();

        allNpcs = dataLoader.getAllNpcs();

        player = new Player(dataLoader.getCurrentRoom(), dataLoader.getPlayerInventory(), dataLoader.getMemoryScore());

        dataLoader.loadAllItemData();
        parser = new Parser();

        end = "";

        //instantiates the various actions that could be performed by items
        initializeItemActions();
    }

    /**
     * This function initializes all the functions that define actions that items can perform
     */
    public void initializeItemActions(){
        allItemActionFunctions = new HashMap<>();

        //this function allows the player to be moved to a new, random room
        //the parameters are the item's name 
        allItemActionFunctions.put("moveRandom", new itemActionFunction() {
            public void performAction(Item item, String parameters){
                Room randRoom = player.getCurrentRoom();

                //we don't want a room that is the same as the current room,
                //that is locked without the player having the keys to the room,
                //that has a memory score if it has an ending

                while ( randRoom == player.getCurrentRoom() || 
                      (randRoom.isLocked() && !player.hasItem(randRoom.getKey())) || 
                        randRoom.getMemoriesRequired() > 0 ||
                        randRoom.hasEnd() ) 
                {
                    randRoom = dataLoader.getRandomRoom();
                }

                player.setCurrentRoom(randRoom);

                player.removeItem(parameters);

                showMap();
            }
        });
    }

    /**
     *  Main play routine.  Loops until end of play.
     */
    public void play() 
    {   
        printWelcome();

        // Enter the main command loop.  Here we repeatedly read commands and
        // execute them until the game is over.
                
        boolean finished = false;
        boolean wantToSave = false;
        
        while (! finished && end.equals("")) {
            Command command = parser.getCommand();
            finished = processCommand(command);
        }

        //if we just quit the game to save it
        if (end.equals(""))
        {
            currentDialog = new DialogManager(new NPC("save",""));
            wantToSave = currentDialog.startConversation();

            if (!wantToSave){
                //resets the dialog.xml and data.xml files
                currentDialog = new DialogManager();
                dataLoader.reset();
            }else{
                //saves the data.xml and dialog.xml files
                dataLoader.save(player.getCurrentRoomName(), player.getMemoryScore());

                dataLoader.isNotCorrupt();
                currentDialog.isNotCorrupt();
            }
        }else{
            
            //show end and print farewell dialog
            new EndingFinder(end, player.hasItem("pie-slice"));

            currentDialog = new DialogManager();
            dataLoader.reset();

            System.out.println("Thank you for playing, have fun finding all of the other endings!");
        }
    }

    /**
     * Print out the opening message for the player, and shows the current room's map
     */
    private void printWelcome()
    {
        System.out.println();
        System.out.println("You find yourself without remembering what brought you in this room.");
        System.out.println("Your sole wish is to find your family, whomever they were.");
        System.out.println("\nType 'help' if you need help.");
        System.out.println();
        System.out.println(player.getCurrentRoom().getLongDescription());
        showMap();
    }

    /**
     * Given a command, process (that is: execute) the command.
     * @param command The command to be processed.
     * @return true If the command ends the game, false otherwise.
     */
    private boolean processCommand(Command command) 
    {
        boolean wantToQuit = false;

        if(command.isUnknown()) {
            System.out.println("I don't know what you mean...");
            return false;
        }

        String commandWord = command.getCommandWord();
        
        switch (commandWord)
        {
            case "help":
                printHelp(command);
                break;

            case "go":
                goRoom(command);

                //shows map automatically even if the player doesn't change rooms
                //to remind player of the room's exits
                showMap();
                break;
        
            case "get":
                getItem(command);
                break;
                
            case "interact":
                interactItem(command);
                break;
                
            case "drop":
                dropItem(command);
                break;
            
            case "investigate":
                investigateRoom();
                break;
            
            case "map":
                showMap();
                break;
            
            case "inspect":
                inspectItem(command);
                break;
            
            case "back":
                goBack();

                //shows map automatically even if the player doesn't change rooms
                showMap();
                break;
                
            case "give":
                wantToQuit = giveItem(command);
                break;
            
            case "talk":
                wantToQuit = talkToNpc(command);
                break;
        
            case "quit":
                wantToQuit = quit(command);
        }

        return wantToQuit;
    }

    // implementations of user commands:

    /**
     * Print out some help information.
     * Here we print some a message and a list of the 
     * command words.
     */
    private void printHelp(Command command) 
    {
        //if the player needs help with a specific command
        if (command.hasSecondWord()){
            parser.helpCommand(command.getSecondWord());
            return;
        }

        System.out.println("You find yourself without remembering what brought you in this room.");
        System.out.println("Your sole wish is to find your family, whomever they were.");
        System.out.println();
        System.out.println("'-_-_-_-' lines denote the start and end of dialogue");
        System.out.println("'-------' lines denote the start of a turn in a mini-game\n");
        System.out.println("Your command words are:");
        parser.showCommands();
        System.out.println("If you want more information about a command type");
        System.out.println("help [command]");

        return;
    }

    /** 
     * Try to go to one direction. If there is an exit, enter the new
     * room, otherwise print an error message.
     */
    private void goRoom(Command command) 
    {
        if(!command.hasSecondWord()) {
            // if there is no second word, we don't know where to go...
            System.out.println("Go where?");
            return;
        }

        String direction = command.getSecondWord();

        // Try to leave current room.
        Room nextRoom = player.getCurrentRoom().getExit(direction);

        if (nextRoom == null) {
            System.out.println("There is no door!");
        }
        else {
            //can only enter some rooms if the player meets some requirements

            //if the room is locked
            if (nextRoom.isLocked())
            {
                if (player.hasItem(nextRoom.getKey())){
                    System.out.println("You decide to use the " + nextRoom.getKey() + " to open the locked door to the room.");
                    nextRoom.toggleLocked();
                }else{
                    System.out.println("You currently don't have a key to unlock this space.");
                    return;
                }
            //if the room requires mem points
            }else if (nextRoom.getMemoriesRequired() > 0)
            {
                if (player.getMemoryScore() >= nextRoom.getMemoriesRequired()){
                    System.out.println("You manage to overcome your fears and decide to swing open the door.");
                    nextRoom.setMemoriesRequired(0);
                }else{
                    System.out.println("Some strange spiral patterns decorate the door and they are staring at you.");
                    System.out.println("You decide to avoid the door as it gives an odd feeling");
                    return;
                }
            }
            
            //if the room leads to an end ask if the player is sure to continue
            if (nextRoom.hasEnd()){

                currentDialog = new DialogManager(new NPC("confirmation", ""));
                boolean wantToEnd = currentDialog.startConversation();

                if (wantToEnd){
                    this.end = nextRoom.getEnd();
                }else{
                    System.out.println("You decided to not go in the next room.");
                    return;
                }
            }

            player.setCurrentRoom(nextRoom);

        }
    }
    
    /**
     * Interact with an item, so it prints out the item's action
     * @param command the player's command
     */
    private void interactItem(Command command){
        if(!command.hasSecondWord()) {
            // if there is no second word, we don't know what item to interact with
            System.out.println("What item?");
            return;
        }
        
        String itemName = command.getSecondWord();
        
        Item item = player.getCurrentRoomItem(itemName);
        
        //if the item isn't in the room it may be in the player's inventory
        if (item == null)
        {
            item = player.getItem(itemName);
            
            //if it isn't in the inventory either then the player can't get the item
            if (item == null){
                System.out.println("There isn't a " + itemName + "!");
                return;
            }

        //if the item can be held, you can interact with it only by holding it
        }else if (item.isHoldable()){
            System.out.println("You need the " + itemName + " in your inventory to interact with it!");
            return;
        }
        
        //if the item has an action description
        if (item.hasActionContent()){
            System.out.println(item.getActionContent() + "\n");

            //if the item has an action defined in our item action functions
            if ( allItemActionFunctions.containsKey(item.getAction())){
                allItemActionFunctions.get(item.getAction()).performAction(item, item.getActionParameters());
            }

            //if an item's action triggers memories, we add memory points
            player.addMemoryScore(item.getMemoriesGiven());
            item.setMemoriesGiven(0);

        }else{
            System.out.println(itemName + " can't be used to interact.");
        }
    }
    
    /**
     * Sets the player's current room to the previous room visited by them
     */
    private void goBack(){
        if (player.getPastRoom() == null){
             System.out.println("There are no rooms to go back to!");
        }
        else{
            player.setCurrentRoom(player.getPastRoom());
        }
    }

    /**
     * Get the item specified in the command from the room, 
     * only when it can be held and if your inventory isn't too heavy.
     * @param command the command given by the player
     */
    private void getItem(Command command){
        if(!command.hasSecondWord()) {
            // if there is no second word, we don't know what item to get from the room
            System.out.println("What item?");
            return;
        }

        //the maximum weight of the inventory
        int maxInventoryWeight = 10;
        
        String itemName = command.getSecondWord();

        //we pop the item from the current room's items
        Item item = player.popCurrentRoomItem(itemName);
        
        if (item == null){
            System.out.println("There isn't a " + itemName + " in this room!");
        
        //if the item picked up can't be held
        }else if (!item.isHoldable())
        {
            System.out.println("You can't pick that up!");
            player.getCurrentRoom().setItem(itemName, item);

        //if the new weight of the inventory exceeds the max weight
        }else if ((player.getInventoryWeight() + item.getWeight()) > maxInventoryWeight){
            player.setCurrentRoomItem(itemName, item);
            System.out.println("You can't carry any more extra weight!");
        }
        else{
            player.setItem(itemName, item);
            System.out.println("You pick up the " + itemName);

            //automatically inspects the item that you pick up
            inspectItem(new Command("inspect", itemName, null));
        }
        
    }
    
    /**
     * Drop the item specified in the command, from your inventory
     * inside the room you're currently standing in
     * @param command the command given by the player
     */
    private void dropItem(Command command){
        if(!command.hasSecondWord()) {
            // if there is no second word, we don't know where to go...
            System.out.println("What item?");
            return;
        }
        
        String itemName = command.getSecondWord();
        Item item = player.getItem(itemName);
        
        if (item == null){
            System.out.println("There isn't that item in your inventory!");
        }
        else{
            player.removeItem(itemName);
            player.setCurrentRoomItem(itemName, item);
                        
            System.out.println("You drop the " + itemName);
        }
    }

    /**
     * Gets the description of the item specified by the command.
     * Can also inspect the inventory to see its contents
     * 
     * @param command the command given by the player
     */
    private void inspectItem(Command command){
        if(!command.hasSecondWord()) {
            // if there is no second word, we don't know where to go...
            System.out.println("What item?");
            return;
        }
        
        String itemName = command.getSecondWord();
        Item item = player.getCurrentRoomItem(itemName);
        
        //given the player wants to inspect their inventory
        if (itemName.equals("inventory")){
            
            if (player.getInventorySize() == 0){ 
                System.out.println("There's nothing in your inventory");

            }
            else{
                player.showInventory();
            }
            return;
        
        //if the item isn't in the room then it may be in the player's inventory
        } else if (item == null){
            
            item = player.getItem(itemName);
            
            if (item == null){
                System.out.println("There isn't a " + itemName + "!");
                return;
            }
        }
        //if the item does have a description
        if (item.getDescription() == null || item.getDescription().length() == 0){
            System.out.println("It seems that there is nothing special about " + itemName);
        }else{
            System.out.println("\nYou take a closer look and it seems there's " + item.getDescription()); 
        }
    }

    /**
     * The player looks around the room to look for npcs and items
     * The player isn't allowed to look for items if its considered 'rude' by an npc
     * that is inside the room
     */
    private void investigateRoom(){

        System.out.println("The room has : " + player.getCurrentRoom().getExitString() + "\n");

        String npcInRoomName = "";

        //get the possible npc that considers it "rude" to investigate in the room
        String roomNoIvestigatePerson = player.getCurrentRoom().getNoInvestigatePerson();

        //check if this npc is in the room or not
        if (!roomNoIvestigatePerson.equals("")){

            for (NPC npc : allNpcs.values()){
                //if the npc is in the current room
                if (npc.isInCurrentRoom(player.getCurrentRoomName())){
                    //if the npc name corresponds to the npc we're looking for
                    if (npc.getName().equals(roomNoIvestigatePerson)){
                        npcInRoomName = npc.getName();
                        break;
                    }
                }
            }
        }

        if (npcInRoomName.equals("")){
            showItems();
        }else{
            System.out.println("It's rude to look into this room with " + npcInRoomName + " inside.");
        }

        //shows the npcs regardless if its rude or not
        showNpcs();

    }
    
    /**
     * Show the list of the items inside the room
     */
    private void showItems(){
        player.showItemsOfCurrentRoom();
    }

    /**
     * Shows the list of npc's inside the room
     */
    private void showNpcs(){
        String allNPCString = "It also seems that there is";
        boolean npcPresent = false;
        
        for (NPC npc : allNpcs.values()){
            if (npc.isInCurrentRoom(player.getCurrentRoomName())){
                allNPCString += "\n*" + npc.getName();
                npcPresent = true;
            }
        }
        
        if (npcPresent){
            System.out.println(allNPCString);
        }else{
            //if there's no one
            System.out.println(allNPCString + " no one else...");
        }
    }
    
    /**
     * Talk to the npc specified inside the command
     * @param command the command specified by the user
     * @return return whether or not the user wants to leave the game
     */
    private boolean talkToNpc(Command command){
        boolean wantToQuit = false;
        
        if(!command.hasSecondWord()) {
            // if there is no second word, we don't who to talk to
            System.out.println("Talk to who?");
            return wantToQuit;
        }
        
        String npcName = command.getSecondWord();
        
        NPC npc = allNpcs.get(npcName);
        
        if (npc == null){
            System.out.println("That person doesn't exist!");
        }else if (npc.isInCurrentRoom( player.getCurrentRoomName() )){
            
            currentDialog = new DialogManager(npc);
            wantToQuit = currentDialog.startConversation();

            //here we handle the changes of our NPC, Player or Room objects that 
            //result from our interaction
            this.processInteraction(npc);
        }else{
            System.out.println( npc.getCapName() + " isn't in this room!");
        }
        
        return wantToQuit;
    }

    /**
     * This function handles the modification of the NPC, Room and Player objects
     * in response to an interaction. (e.g an item has been transferred from npc to player)
     * @param npc the npc involved in the interaction
     */
    private void processInteraction(NPC npc){
        
        //while there's items that have been traded between the parties
        while (currentDialog.hasItemTraded()){
            String tradedItemKey = currentDialog.getNameOfItemTraded();
            String tradedItemName = (tradedItemKey != null) ? tradedItemKey.substring(5) : null;
            
            if (tradedItemKey.substring(0,5).equals("drop-")){
                //drops item in the room
                player.getCurrentRoom().setItem(tradedItemName, currentDialog.popItemTraded(tradedItemKey));
                System.out.println("The " + tradedItemName + " has been left behind.");
            
            }else if (tradedItemKey.substring(0,5).equals("give-")){
                //transfers item from npc inventory to the player's
                player.setItem( tradedItemName, currentDialog.popItemTraded(tradedItemKey));
                System.out.println("You received " + tradedItemName + " from " + npc.getCapName());
            }

        } 

        //if the npc moved then we reset its position and remove it from the current room's
        //map. Its position will be randomly chosen when loading the room it has moved in
        if (npc.hasMoved()){
            player.getCurrentRoom().removePosition(npc.getPosition());
            npc.setPosition("");
        }
        
        //if memory points were gained by the interaction
        if (currentDialog.getMemories() > 0){
            player.addMemoryScore(currentDialog.getMemories());
        }

        //if the interaction results in an ending of the game
        if (currentDialog.hasEnd()){
            end = currentDialog.getEnd();
        }
    }
    
    /**
     * Gives the item specified by the command to the npc specified by the command.
     * If it gives an item to an npc that needs it, a dialogue will start.
     * Otherwise, the npc will reject the item
     * @param command the command specified by the user
     * @return whether the user wants to leave the game or not
     */
    private boolean giveItem(Command command){
        boolean wantToQuit = false;

        if(!command.hasSecondWord()) {
            // if there is no second word, we don't know what to give
            System.out.println("Give what?");
            return wantToQuit;
        
        }else if (!command.hasThirdWord()){
            //if there is no third word, we don't know who to give it to
            System.out.println("Give it to who?");
            return wantToQuit;
        }
        
        String itemName = command.getSecondWord();
        String npcName = command.getThirdWord();
                
        Item item = player.getItem(itemName);
        
        if (item == null){
            System.out.println("You don't have " + itemName +  " in your inventory!");
        }else
        {
            NPC npc = allNpcs.get(npcName);
            
            if (npc == null){
                System.out.println("The person you want to give your " + itemName + " to doesn't exist!");

            //given a valid npc and item name
            }else if (npc.isInCurrentRoom( player.getCurrentRoomName() )){

                //start a conversation with the npc
                currentDialog = new DialogManager(npc);

                //if the npc doesn't need an item for an interaction then the player can't
                //give the item
                if (!currentDialog.canDoSpecialCondition("hasItem " + itemName)){
                    System.out.println("Thanks for the " + itemName + " but I don't need it.");
                }else{
                    npc.setItem(itemName,item);
                    player.removeItem(itemName);

                    wantToQuit = currentDialog.startConversation();

                    //here we handle the changes of our NPC, Player or Room objects that 
                    //result from our interaction
                    processInteraction(npc);
                }
            }else{
                System.out.println(npc.getCapName() + " isn't in this room!");
            }
        }
        return wantToQuit;
    }

    /**
     * Shows the room map of the current room on the terminal
     */
    private void showMap(){

        Room currentRoom = player.getCurrentRoom();

        for (NPC npc : allNpcs.values()){
            if (npc.isInCurrentRoom(player.getCurrentRoomName())){
                //while the npc has recently moved and it doesn't have a position or it has a position overlapping another position on the map
                //change the npc's position to a random one
                while(npc.hasMoved() && (!npc.hasPosition() || !currentRoom.addToPositions("O", npc.getPosition()))){
                    npc.setRandomPosition(currentRoom.getDimensions());
                };

                if (npc.hasMoved()){
                    npc.toggleHasMoved();
                }
            }
        }

        //builds the map
        MapViewer map = new MapViewer(player.getCurrentRoom());

        //actually prints out map
        map.displayMap();

        //shows map key
        System.out.println("\nMap key:");
        System.out.println("'X' : item in the room");
        System.out.println("'O' : person in the room");
    }
    
    /** 
     * "Quit" was entered. Check the rest of the command to see
     * whether we really quit the game.
     * @return true, if this command quits the game, false otherwise.
     */
    private boolean quit(Command command) 
    {
        if(command.hasSecondWord()) {
            System.out.println("Quit what?");
            return false;
        }
        else {
            return true;  // signal that we want to quit
        }
    }
}