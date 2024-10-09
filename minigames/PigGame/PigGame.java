// PPA - 1 / Assignment 2 - World of Zuul
// Gabriele Trotta (K21006956)

package minigames.PigGame;

import java.util.Scanner;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

/**
 * This class is part of the project called "IUNI".
 * It's a simple text adventure game, where you can walk around 
 * and interact with characters and items, in order to regain your memory.
 * 
 * This class is the minigame accessed during an interaction with barbara
 * 
 * This class handles the main playing aspects the Game of Pig, such as the turns
 * and the throw of the die, as it is a die game.
 */

 public class PigGame {
    private boolean quit;

    private PigPlayer[] allPigPlayers;      //array containing all the players of the minigame
    private int currentPigPlayerIndex;      //the index indicating what player is currently playing
    private Scanner reader;                 // source of command input

    private String winner;                  //the name of the winner
    private Stack<String> playerMoves;      //the stack containing all the moves done by the main character


    /**
     * This creates the object for handling the Game of Pig
     */
    public PigGame(){
        quit = false;
        playerMoves = new Stack<>();

        winner = "";

        reader = new Scanner(System.in);
        
        PigPlayer PigPlayer = new PigPlayer("charlie");
        PigPlayer opponent = new PigPlayer("barbara");

        allPigPlayers = new PigPlayer[] {PigPlayer, opponent};
        currentPigPlayerIndex = 0;
    }

    /**
     * This is the main loop of the game, it's broken only if a player wins or the
     * user wants to quit the main game.
     * @return whether the player wants to quit the main game or not
     */
    public boolean play()
    {
        showInstructions();

        while (!quit)
        {
            PigPlayer currentPigPlayer = allPigPlayers[currentPigPlayerIndex];
            if (currentPigPlayer.getName().equals("charlie")){ playerMoves = new Stack<>(); }
            turn(currentPigPlayer);

            //loop through the players
            currentPigPlayerIndex = (currentPigPlayerIndex + 1)%allPigPlayers.length;

        }

        //if the player wants to quit the main game, the winner field will have value ""
        return quit && (winner.equals(""));
    }

    /**
     * This function handles the turn taken by the player
     * @param currentPigPlayer the current player playing the turn
     */
    public void turn(PigPlayer currentPigPlayer)
    {
        String response;

        //while the player isn't holding
        turnLoop : while (true)
        {
            //show text laying out information about the player
            horizontalLine();

            System.out.println(String.format("%s has %d points this turn and %d total points", currentPigPlayer.getName() ,
                                                                                                currentPigPlayer.getTurnTotal(),
                                                                                                currentPigPlayer.getTotalScore()));

            System.out.println( currentPigPlayer.getName() + " do you want to hold or roll?\n");

            //if the computer is playing no input is required
            if (currentPigPlayer.getName().equals("barbara"))
            {
                try {
                    response = calculateResponse();
                } catch (InterruptedException e) {
                    //in case of an error, her response is hold
                    response = "hold";
                }

                System.out.println(response);

            }else
            {
                response = reader.nextLine();
                response = response.toLowerCase();
            }

            //if the player wants to quit the overall game
            if (response.equals("quit") || response.equals("q"))
            {
                quit = true;
                return;
            }

            if (response.equals("hold"))
            {
                break turnLoop;
            }else if (response.equals("roll"))
            {
                //if charlie is rolling then we add his move to the stack
                if (currentPigPlayer.getName().equals("charlie")){
                    playerMoves.push("roll");
                }

                //random number between 1 and 6
                int dice = (int) Math.round(Math.random()*5) + 1;

                System.out.println(String.format("%s just rolled a %d\n", currentPigPlayer.getName(), dice));

                //if the die value isn't one add the die value to the turn total
                if (dice > 1){
                    System.out.println(String.format("Well done! You just added %d to your turn total\n", dice));
                    currentPigPlayer.addTurnTotal(dice);

                }else
                {
                    //the player loses the turn's total
                    System.out.println("Oh no! " + currentPigPlayer.getName() + " just lost your points in this turn!\n");
                    currentPigPlayer.resetTurnTotal();

                    break turnLoop;
                }
            
            //if the player needs help
            }else if (response.equals("help") || response.equals("h")){
                showInstructions();
            }
            else
            {
                System.out.println("You need to answer with either 'hold' or 'roll' \n");
            }

            //if the player has enough points with the total score and the turn's total
            //he wins
            if ((currentPigPlayer.getTotalScore() + currentPigPlayer.getTurnTotal()) >= 100)
            {
                currentPigPlayer.addTotalScore();

                System.out.println(String.format("At the end of this turn, %s has %d total points\n\n", currentPigPlayer.getName(), currentPigPlayer.getTotalScore()));
                horizontalLine();

                winner = currentPigPlayer.getName();
                quit = true;
                break turnLoop;
            }
        }
        
        //add the turn's score to the total score of the player
        currentPigPlayer.addTotalScore();
        System.out.println(String.format("At the end of this turn, %s has %d total points\n\n", currentPigPlayer.getName(), currentPigPlayer.getTotalScore()));
    }

    /**
     * This function is used by the computer to calculate a response to the player's turn.
     * The strategy is copying the user strategy, and adding a bit of randomness to it.
     * @return the computer's response
     * @throws InterruptedException
     */
    public String calculateResponse() throws InterruptedException{
        TimeUnit.MILLISECONDS.sleep(1000);

        //pop the stack until it's empty, in that there's an element of randomness
        //otherwise we hold
        if (playerMoves.size() > 0)
        {
            return playerMoves.pop(); 
        }else if (Math.random() > 0.3f){
            return "roll";
        }

        return "hold";
    }

    /**
     * @return whether the use has won or not
     */
    public boolean hasWon(){
        return (winner.equals("charlie"));
    }

    /**
     * prints out an horizontal line composed of dashes / used for formatting
     */
    public void horizontalLine()
    {
        System.out.println("-----------------------------------------------------------------------------");
    }

    /**
     * prints out the instructions explaining the Game of Pig
     */
    public void showInstructions()
    {
        horizontalLine();
        System.out.println("We'll play the Game of Pig:");
        System.out.println("One dice will be rolled at the start of each turn");
        System.out.println("You will have two options : either you hold or you roll.");
        System.out.println("If you roll, you will throw the die and if it doesn't land on one,");
        System.out.println("its value will be added to the turn's pot of points.");
        System.out.println("If a one appears, you lose the turn's points and the turn goes to the opponent.");
        System.out.println("If you hold, you add your turn's points to your total score, and the turn goes to the oppopnent.");
        System.out.println("\nThe first one to reach 100 points wins!");

        horizontalLine();
    }
}