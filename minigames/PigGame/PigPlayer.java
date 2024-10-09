// PPA - 1 / Assignment 2 - World of Zuul
// Gabriele Trotta (K21006956)

package minigames.PigGame;

/**
 * This class is part of the project called "IUNI".
 * It's a simple text adventure game, where you can walk around 
 * and interact with characters and items, in order to regain your memory.
 * 
 * This class is a player playing the minigame : Game of Pig.
 * 
 * This class manages the information of the player, namely its name, 
 * turn score and total score.
 * 
 * @author Gabriele Trotta (K21006956)
 * date : 27/11/21
 */

public class PigPlayer
{
    private String name;

    private int totalScore;
    private int turnTotal;

    /**
     * creates a player of the game of pig with the name specified
     * @param name the name of the player
     */
    public PigPlayer(String name){
        this.name = name;

        this.totalScore = 0;
        this.turnTotal = 0;
    }

    /**
     * @return the name of the player
     */
    public String getName(){
        return name;
    }

    /**
     * @return the player's total score
     */
    public int getTotalScore(){
        return totalScore;
    }

    /**
     * @return the total points amassed during the player's turn
     */
    public int getTurnTotal(){
        return turnTotal;
    }

    /**
     * Adds points to the turn's total score
     * @param points the points added to the score
     */
    public void addTurnTotal(int points){
        this.turnTotal += points;
    }

    /**
     * resets the turn's total points to 0
     */
    public void resetTurnTotal(){
        this.turnTotal = 0;
    }

    /**
     * resets the total score to 0
     */
    public void resetTotalScore(){
        this.totalScore = 0;
    }

    /**
     * adds the turn's points to the player's total points
     */
    public void addTotalScore(){
        this.totalScore += this.turnTotal;
        this.turnTotal = 0;
    }
}