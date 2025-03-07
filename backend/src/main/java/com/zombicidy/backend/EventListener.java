package com.zombicidy.backend;

import com.zombicidy.backend.board.*;
import com.zombicidy.backend.board.baseclasses.Grid;
import com.zombicidy.backend.board.combat.Combat;
import com.zombicidy.backend.frontend.terminal.Terminal;

public class EventListener {
  private Board board;
  private Terminal terminal;
  private boolean game = true;

  public EventListener(String difficulty) {
    this.board = new Board(difficulty, false, this);
    this.terminal = new Terminal(this);
    PrintTerminal();
    while (game) {
      terminal.Input();
    }
  }

  public void PlayerDealtDamage(int damage) {
    terminal.PlayerDealtDamage(damage);
  }

  public void PlayerTookDamage(int damage) {
    terminal.PlayerTookDamage(damage);
  }

  public void CombatAction(String choise) { board.CombatAction(choise); }

  public void FinishCombat() { terminal.FinishCombat(); }

  public void InitiateCombat(Combat combat) { terminal.Combat(combat); }

  public void GameWin() {
    terminal.GameWin();
    game = false;
  }

  public void SurpriseEncounter() { terminal.SurpriseEncounter(); }

  public void ZombieKilled() { terminal.ZombieKilled(); }

  public void GameLose() {
    terminal.GameLose();
    game = false;
  }

  public void GainedItem(String item) { terminal.GainedItem(item); }

  public Grid getGrid(int[] position) { return board.GetGrid(position); }

  public void UseBandage(boolean actionWasMade) {
    terminal.UseBandage(actionWasMade);
  }

  public void PrintTerminal() { terminal.Print(); }

  public void Redraw(int[] position, Grid grid) {
    terminal.Redraw(position, grid);
  }

  public void MovePlayer(int[] position) { board.Input(position); }

  public void PlayerGunNoAmmo() { terminal.PlayerGunNoAmmo(); }

  public void PlayerNoGun() { terminal.PlayerNoGun(); }

  public void RestartBoard() { board.StartBoard(); }
}
