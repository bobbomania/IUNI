// PPA - 1 / Assignment 2 - World of Zuul
// Gabriele Trotta (K21006956)

import java.util.concurrent.TimeUnit;

/**
 * This class is part of the project called "IUNI".
 * It's a simple text adventure game, where you can walk around 
 * and interact with characters and items, in order to regain your memory.
 * 
 * This class handles the displaying of text onto the terminal. 
 * This class is primarily used in dialogue, to give the impression that the 
 * NPC is talking with the player.
 * 
 * No constructor is needed as for the moment, this class is only displaying text on the
 * terminal, but possibly it could be used to display text on a GUI
 */
public class TextViewer {

    /**
     * This function prints text to the terminal one character at a time, separated by a 
     * time interval
     * @param text the text to be printed out on the terminal
     * @param timeDelta the time delay between the print of each character
     */
    public void printText(String text, long timeDelta){
        System.out.println();

        for (int i=0; i < text.length(); i++){
            System.out.print(text.charAt(i));

            try {
                TimeUnit.MILLISECONDS.sleep(timeDelta);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println();
    }

    /**
     * Prints to the terminal a bar of characters indicating a dialog has started or ended
     */
    public void printDialogBar(){
        this.printText("_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_",0);
    }

}
