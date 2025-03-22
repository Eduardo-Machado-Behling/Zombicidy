package com.zombicidy.backend.board;

import com.zombicidy.backend.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;

public class FileReader {
  public Map<String, String[]> ReadSettings(String difficulty) {
    Map<String, String[]> gameSettings = new HashMap<>();

    // Construct the path to the settings file in resources
    String filePath = "config/gamesettings/" + difficulty + ".csv";

    try (InputStream inputStream =
             getClass().getClassLoader().getResourceAsStream(filePath);
         BufferedReader reader =
             new BufferedReader(new InputStreamReader(inputStream))) {

      if (inputStream == null) {
        Logger.get().logError(Logger.Verbose.INFO,
                              "File not found: " + filePath);
        return gameSettings;
      }

      String line;
      while ((line = reader.readLine()) != null) {
        String[] splited = line.split(",");
        gameSettings.put(splited[0], splited);
      }

      return gameSettings;
    } catch (IOException e) {
      Logger.get().logError(Logger.Verbose.INFO,
                            "An error occurred while reading the file.");

      e.printStackTrace();
    }

    return gameSettings;
  }

  public class MapData {
    public int id;
    public String[][] map;

    public MapData(int id, String[][] map) {
      this.id = id;
      this.map = map;
    }
  }

  public MapData ReadMap(int i) {
    String[][] map = new String[10][10];
    int x = 0;
    String filePath = "config/maps/map" + i + ".csv";

    // Using ClassLoader to read the resource as InputStream
    InputStream myObj =
        getClass().getClassLoader().getResourceAsStream(filePath);

    if (myObj == null) {
      System.out.println("File not found: " + filePath);
      return new MapData(i, map);
    }

    try (Scanner myReader = new Scanner(myObj)) {
      // Reading the file line by line and filling the map
      while (myReader.hasNextLine()) {
        String data = myReader.nextLine();
        String[] splited = data.split(",");
        for (int y = 0; y < splited.length && y < 10; y++) {
          map[x][y] = splited[y];
        }
        x++;
        if (x >= 10)
          break; // Stop if we've filled all rows (10 rows)
      }
    } catch (Exception e) {
      System.out.println("An error occurred while reading the map file.");
      e.printStackTrace();
    }

    return new MapData(i, map);
  }

  public MapData ReadRamdomMap() {
    Random rand = new Random();
    return ReadMap((rand.nextInt(3) + 1));
  }
}
