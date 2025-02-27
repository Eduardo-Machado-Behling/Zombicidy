package main.board;

import java.util.Random;

public class Dice {
    Random rand = new Random();

    public int RollD6() {
        return rand.nextInt( 5 ) + 1;
    }
    
    public int RollD3() {
        return rand.nextInt( 2 ) + 1;
    }
}
