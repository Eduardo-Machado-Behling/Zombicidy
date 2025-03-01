package main;

import main.board.*;

import java.util.Map;
import java.util.HashMap;

import main.*;


public class App {
    public static void main(String[] args) {
        Board board = new Board( "easy" , false, null );
        int[] position = new int[2];
        Grid grid;
        for( int x = 0 ; x < 10 ; x++ ) {
            for( int y = 0 ; y < 10 ; y++ ) {
                position[0] = x;
                position[1] = y;
                grid = board.GetGrid( position );
                System.out.print(grid.GetType() + "|");
            }
            System.out.print("\n");
        }
    }
}
