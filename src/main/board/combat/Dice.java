package main.board.combat;

import java.util.Random;

public class Dice {
    private Random rand = new Random();

    public int RollD6() {
        return rand.nextInt( 6 ) + 1;
    }
    
    public int RollD3() {
        return rand.nextInt( 3 ) + 1;
    }
}
