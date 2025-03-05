package main.board;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class FileReader {
    public Map<String, String[]> ReadSettings( String difficulty ) {
        Map<String, String[]> gameSettings = new HashMap<String, String[]>();
        try {
            File myObj = new File( "gamesettings/" + difficulty + ".csv" );;
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String splited[] = data.split( ",");
                gameSettings.put( splited[0] , splited );
            }
            myReader.close();
            return gameSettings;
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return gameSettings;
    } 

    public String[][] ReadRamdomMap() {
        Random rand = new Random();
        String[][] map = new String[10][10];
        int x = 0;
        try {
            File myObj = new File( "maps/map" + ( rand.nextInt(3) + 1 ) + ".csv" );
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String splited[] = data.split( ",");
                for( int y = 0 ; y < 10 ; y++ ) {
                    map[x][y] = splited[y];
                }
                x++;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }


        return map;
    }
}
