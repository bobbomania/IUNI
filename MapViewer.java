// PPA - 1 / Assignment 2 - World of Zuul
// Gabriele Trotta (K21006956)


import java.util.Arrays;

/**
 * This class is part of the project called "IUNI".
 * It's a simple text adventure game, where you can walk around 
 * and interact with characters and items, in order to regain your memory.
 * 
 * This class handles the display of the room's map on the terminal via ASCII art.
 * 
 * @author Gabriele Trotta (K21006956)
 * date : 26/11/21
 */
public class MapViewer {
    private String[][] map;

    private int width;                 //how many characters across the terminal will be displayed
    private int height;                //how many rows will be printed on the terminal

    private Room roomDisplayed;

    /**
     * creates the MapViewer object that will be able to create the room's ASCII map
     * @param room the room that is being made the map
     * @param positions the positions of the npc's that stand within the room
     */
    public MapViewer(Room room){

        this.width = this.getFirst(room.getDimensions());
        this.height = this.getSecond(room.getDimensions());

        this.roomDisplayed = room;

        this.map = new String[this.height][this.width];

        createMap();
    }

    /**
     * gets the element 'a' in the string 'a,b'
     * @param string the string that is being analyzed
     * @return the first element of the string being split
     */
    private int getFirst(String string){
        return Integer.parseInt(string.split(",")[0]);
    }

    /** 
     * gets the element 'b' in the string 'a,b'
     * @param string the string that is being analyzed
     * @return the second element of the string being split
     */
    private int getSecond(String string){
        return Integer.parseInt(string.split(",")[1]);
    }

    /**
     * creates a map of the room being displayed
     */
    private void createMap(){

        //for every direction if it's not an exit then leave blank
        if (roomDisplayed.hasExit("north")){
            createHorizontalWall("north",0);
        }else{
            createHorizontalWall("", 0);
        }

        if (roomDisplayed.hasExit("south")){
            createHorizontalWall("south", this.height-1);
        }else{
            createHorizontalWall("", this.height-1);
        }

        if (roomDisplayed.hasExit("west")){
            createVerticalWall("west", 0);
        }else{
            createVerticalWall("", 0);
        }

        if (roomDisplayed.hasExit("east")){
            createVerticalWall("east", this.width-1);
        }else{
            createVerticalWall("", this.width-1);
        }    

        //show the room name in the middle of the map
        showRoomName(roomDisplayed.getName());

        //show all of the positions of the objects on the map
        for (String[] position : roomDisplayed.getPositions()){
            showText(position[0], getSecond(position[1]), getFirst(position[1]), false);
        }
    }

    /**
     * handles the showing of the room name in the middle of the map
     * @param roomName the name of the room being displayed
     */
    private void showRoomName(String roomName){

        //create a line of asterisks
        char[] chars = new char[roomName.length()+2];
        Arrays.fill(chars, '*');
        String line = new String(chars);

        //making the asterisk box containing the name of the room in the middle of the map
        showText(line, this.height/2-1, this.width/2 - roomName.length()/2 - 2, true);
        showText("|" + roomName + "|", this.height/2, this.width/2 - roomName.length()/2 - 2, true);
        showText(line, this.height/2+1, this.width/2 - roomName.length()/2 - 2, true);
    }

    /**
     * checks if a coordinate (colIndex, rowIndex) is inside the room-name's box
     * @param rowIndex the row position of the first character text being checked
     * @param colIndex the column position of the first character text being checked
     * @param length the length of the word
     * @return whether the word is inside the box or not
     */
    private boolean isInForbiddenRange(int rowIndex, int colIndex, int length){
        int roomNameLength = this.roomDisplayed.getName().length();

        for (int j=0; j <= length; j++){
            for (int i=-1; i < 2; i++){
                //this is to adjust for the middle of the map
                if (( colIndex >= this.width/2 - roomNameLength/2 - 3 ) && (colIndex + j - 1 <= this.width/2 + roomNameLength/2 + 2)
                    && rowIndex == this.height/2-i ){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * creates a straight horizontal 'wall' of underscores at the specified row index,
     * with the specified exit in the middle
     * @param exit the name of the exit in the middle of the wall
     * @param rowIndex the index where the wall is placed
     */
    private void createHorizontalWall(String exit, int rowIndex){
        int temp_i = 0;
        
        String[] row = map[rowIndex];

        //if the exit is locked replace it with "locked"
        if (!exit.equals("") && roomDisplayed.getExit(exit).isLocked()){ exit = "locked"; }
        
        
        exit = (exit.length() > 0) ? "|" + exit + "|" : "";

        //if the exit is too long then remove the '|'
        exit = (exit.length() > this.width) ? exit.substring(1) : exit;

        int exitWidth = exit.length();

        //puts the exit name at the middle of the wall
        for (int i = 0; i < this.width; i++){
            if ( ( i < (this.width - exitWidth)/2 ) || ((i) >= (exitWidth + (this.width - exitWidth)/2))  ){
                row[i] = "_";
            }else{
                row[i] = "" + exit.charAt(temp_i);
                temp_i++;
            }
        }
    }

    /**
     * creates a straight vertical 'wall' of '|' at the specified column index,
     * with the specified exit in the middle
     * @param exit the name of the exit in the middle of the wall
     * @param colIndex the index where the wall is placed
     */
    private void createVerticalWall(String exit, int colIndex){
        int temp_i = 0;

        //if the exit is locked replace it with "locked"
        if (!exit.equals("") && roomDisplayed.getExit(exit).isLocked()){ exit = "locked"; }
                
        exit = "|" + exit + "|";

        //if the exit is too long then remove the '|'
        exit = (exit.length() > this.height) ? exit.substring(1) : exit;

        int exitHeight = exit.length();

        map[0][colIndex] = "_";

        //puts the exit name at the middle of the wall
        for (int i = 1; i < this.height; i++){
            if ( ( i < (this.height - exitHeight)/2 ) || ((i) >= (exitHeight + (this.height - exitHeight)/2))  ){
                map[i][colIndex] = "|";
            }else{
                map[i][colIndex] = "" + exit.charAt(temp_i);
                temp_i++;
            }
        }
    }

    /**
     * Shows the specified text horizontally at the specified coordinate (colIndex, rowIndex)
     * Cuts the word out if it's too long for the room map.
     * 
     * @param text the text that is shown on the map
     * @param rowIndex the row number where the text starts
     * @param colIndex the column number where the text starts
     * @param isName whether the text is part of the name box creation
     */
    public void showText(String text, int rowIndex, int colIndex, boolean isName){
        //if the text is inside the middle box with the name of the room then move it 
        //outside of the box, unless if its the name itself being shown on the map
        if (!isName && isInForbiddenRange(rowIndex, colIndex, text.length()))
        {
            showText(text, rowIndex+1, colIndex+1, false);
            return;
        }
        
        for ( int i = 0; i < text.length(); i++){
            if (colIndex + i <= this.width - 1){
                map[rowIndex][colIndex + i] = "" + text.charAt(i);
            }
        }
    }

    /**
     * prints out the map of the room to the terminal
     */
    public void displayMap(){
        for (String[] row : map){
            String rowString = "";
            for (String entry : row){
                //if the entry is null replace with an empty space
                if (entry == null) { entry = " "; }
                rowString += entry;
            }

            System.out.println(rowString);
        }
    }
}
