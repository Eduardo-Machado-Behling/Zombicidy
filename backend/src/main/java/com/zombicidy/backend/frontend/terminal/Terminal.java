package com.zombicidy.backend.frontend.terminal;

import com.zombicidy.backend.EventListener;
import com.zombicidy.backend.board.baseclasses.Grid;
import com.zombicidy.backend.board.characters.CommomZombie;
import com.zombicidy.backend.board.characters.Player;
import com.zombicidy.backend.board.combat.Combat;
import com.zombicidy.backend.frontend.BaseFrontend;
import java.util.Scanner;

public class Terminal extends BaseFrontend {
  private Grid board[][];
  private Scanner reader = new Scanner(System.in);
  private boolean combatBool = false;

  public Terminal(EventListener eventListener) {
    super(eventListener);
    board = new Grid[10][10];
    int[] position = new int[2];
    for (int x = 0; x < 10; x++) {
      for (int y = 0; y < 10; y++) {
        position[0] = x;
        position[1] = y;
        board[x][y] = eventListener.getGrid(position);
      }
    }
  }

  public void FinishCombat() { combatBool = false; }

  public void Combat(Combat combat) {
    System.out.println("Combat Initiated");
    combatBool = true;
    CommomZombie zombie = combat.getZombie();
    Player player = combat.getPlayer();
    int choise;
    while (combatBool) {
      System.out.println(zombie.GetType() + " " + zombie.getHealthPoints());
      System.out.println("Player " + player.getHealthPoints());
      System.out.println(
          " 1: Attack Hand | 2: Shoot | 3:Use Bandage | 4: Run ");
      choise = reader.nextInt();

      switch (choise) {
      case 1:
        eventListener.CombatAction("Attack");
        break;
      case 2:
        eventListener.CombatAction("Shoot");
        break;
      case 3:
        eventListener.CombatAction("Bandage");
        break;
      case 4:
        eventListener.CombatAction("Run");
        break;
      }
    }
  }

  public void ZombieKilled() { System.out.println("Zombie Killed"); }

  public void UseBandage(boolean actionWasMade) {
    if (actionWasMade) {
      System.out.println("Bandage Used");
    } else {
      System.out.println("You Don't Have any Bandage");
    }
  }

  public void SurpriseEncounter() { System.out.println("Surprise Encounter"); }

  public void PlayerDealtDamage(int damage) {
    System.out.println("Zombie Took " + damage + " Damage");
  }

  public void PlayerTookDamage(int damage) {
    System.out.println("Player Took " + damage + " Damage");
  }

  public void Print() {
    for (int x = 0; x < 10; x++) {
      for (int y = 0; y < 10; y++) {
        System.out.print(" " + x + "," + y + ":" + board[x][y].GetType() +
                         " |");
      }
      System.out.print("\n");
    }
  }

  public void Input() {
    int[] position = new int[2];
    position[0] = reader.nextInt();
    position[1] = reader.nextInt();
    eventListener.MovePlayer(position);
    Print();
  }

  public void Redraw(int[] position, Grid grid) {
    board[position[0]][position[1]] = grid;
  }

  public void GainedItem(String item) { System.out.println("Gained " + item); }

  public void GameWin() { System.out.println("Game Win"); }

  public void GameLose() { System.out.println("Game Lose"); }

  public void PlayerGunNoAmmo() {
    System.out.println("Player Gun Has No Ammo");
  }

  public void PlayerNoGun() { System.out.println("Player Has No Gun"); }
}
