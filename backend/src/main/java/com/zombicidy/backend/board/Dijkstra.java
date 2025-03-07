package com.zombicidy.backend.board;

import com.zombicidy.backend.board.baseclasses.Grid;
import com.zombicidy.backend.board.characters.CommomZombie;
import com.zombicidy.backend.board.characters.Player;
import java.util.*;

public class Dijkstra {

  private Grid[][] board;
  private Node[][] pathMap = new Node[10][10];
  private int[] start;
  private int[] end;
  private Stack<int[]> path = new Stack<>();

  public Dijkstra(Player player, CommomZombie zombie, Grid[][] board) {
    this.board = board.clone();
    this.start = zombie.getPosition().clone();
    this.end = player.getPosition().clone();
    pathMap[start[0]][start[1]] = new Node(0);
    pathMap[end[0]][end[1]] = new Node(-1);
  }

  public void FindPath(int[] position) {
    int[] temp = position.clone();
    path.add(position.clone());
    // left case
    temp[1] -= 1;
    if (temp[1] >= 0) {
      if (FindLowestNeighbors(temp.clone(),
                              pathMap[position[0]][position[1]].distance)) {
        FindPath(temp.clone());
        return;
      }
    }

    // top case

    temp = position.clone();
    temp[0] += 1;
    if (temp[0] <= 9) {
      if (FindLowestNeighbors(temp.clone(),
                              pathMap[position[0]][position[1]].distance)) {
        FindPath(temp.clone());
        return;
      }
    }

    // right case

    temp = position.clone();
    temp[1] += 1;
    if (temp[1] <= 9) {
      if (FindLowestNeighbors(temp.clone(),
                              pathMap[position[0]][position[1]].distance)) {
        FindPath(temp.clone());
        return;
      }
    }

    // bottom case

    temp = position.clone();
    temp[0] -= 1;
    if (temp[0] >= 0) {
      if (FindLowestNeighbors(temp.clone(),
                              pathMap[position[0]][position[1]].distance)) {
        FindPath(temp.clone());
        return;
      }
    }
  }

  public boolean FindLowestNeighbors(int[] position, int distance) {
    if (pathMap[position[0]][position[1]] != null) {
      if (pathMap[position[0]][position[1]].distance == distance - 1) {
        return true;
      }
    }
    return false;
  }

  public Stack<int[]> CalculatePath() {
    CalculateDistance(start);
    /*for( int x = 0 ; x < 10 ; x++ ) {
        for( int y = 0 ; y < 10 ; y++ ) {
            if ( pathMap[x][y] == null ) {
                System.out.print(" -10 |");
            } else {
                System.out.print( " " + pathMap[x][y].distance + " |" );
            }
        }
        System.out.print( "\n" );
    }
    System.out.print( "\n" );*/
    FindPath(end.clone());
    path.pop();
    return path;
  }

  public void CalculateDistance(int[] position) {
    if (position[0] > 9 || position[1] > 9 || position[0] < 0 ||
        position[1] < 0) {
      return;
    }
    int[] nextPosition = position.clone();

    // left case

    nextPosition[1] -= 1;
    if (nextPosition[1] >= 0) {
      CalculateNeighbors(position.clone(), nextPosition.clone());
    }

    // top case

    nextPosition = position.clone();
    nextPosition[0] += 1;
    if (nextPosition[0] <= 9) {
      CalculateNeighbors(position.clone(), nextPosition.clone());
    }

    // right case

    nextPosition = position.clone();
    nextPosition[1] += 1;
    if (nextPosition[1] <= 9) {
      CalculateNeighbors(position.clone(), nextPosition.clone());
    }

    // bottom case

    nextPosition = position.clone();
    nextPosition[0] -= 1;
    if (nextPosition[0] >= 0) {
      CalculateNeighbors(position.clone(), nextPosition.clone());
    }
  }

  public void CalculateNeighbors(int[] position, int[] nextPosition) {
    if (pathMap[nextPosition[0]][nextPosition[1]] == null) {
      if (board[nextPosition[0]][nextPosition[1]].GetType().equals("Ground")) {
        pathMap[nextPosition[0]][nextPosition[1]] =
            new Node(pathMap[position[0]][position[1]].distance + 1);
        CalculateDistance(nextPosition.clone());
      }
    } else {
      if (nextPosition[0] == end[0] && nextPosition[1] == end[1]) {
        if (pathMap[nextPosition[0]][nextPosition[1]].distance == -1) {
          pathMap[nextPosition[0]][nextPosition[1]] =
              new Node(pathMap[position[0]][position[1]].distance + 1);
        } else if (pathMap[nextPosition[0]][nextPosition[1]].distance >
                   pathMap[position[0]][position[1]].distance + 1) {
          pathMap[nextPosition[0]][nextPosition[1]] =
              new Node(pathMap[position[0]][position[1]].distance + 1);
        }
        return;
      }
      if (pathMap[nextPosition[0]][nextPosition[1]].distance != 0) {
        if (pathMap[nextPosition[0]][nextPosition[1]].distance >
            pathMap[position[0]][position[1]].distance + 1) {
          pathMap[nextPosition[0]][nextPosition[1]] =
              new Node(pathMap[position[0]][position[1]].distance + 1);
          CalculateDistance(nextPosition.clone());
        }
      }
    }
  }

  private class Node {
    public int distance;
    public Node(int distance) { this.distance = distance; }
  }
}
