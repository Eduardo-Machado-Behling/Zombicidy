package com.zombicidy.backend;

import com.zombicidy.backend.board.Board;
import com.zombicidy.backend.board.baseclasses.Grid;
import com.zombicidy.backend.board.characters.CommomZombie;
import com.zombicidy.backend.board.combat.Combat;
import com.zombicidy.backend.frontend.BaseFrontend;
import com.zombicidy.backend.frontend.terminal.Terminal;

public class EventListener {
  private Board board;
  private Terminal terminal;
  private BaseFrontend frontend;
  private String diff;

  public EventListener(BaseFrontend frontend) {
    this.terminal = new Terminal(this);
  }

  public void run(String diff) {
    this.board = new Board(this);
    this.diff = diff;
    this.board.StartBoard(this.diff, false);
    this.terminal.loadMap();
    PrintTerminal();
  }

  public void PlayerDealtDamage(int damage) {
    terminal.PlayerDealtDamage(damage);
    if (frontend != null)
      frontend.PlayerDealtDamage(damage);
  }

  public void PlayerTookDamage(int damage) {
    terminal.PlayerTookDamage(damage);
    if (frontend != null)
      frontend.PlayerTookDamage(damage);
  }

  public void CombatAction(String choise) { board.CombatAction(choise); }

  public void FinishCombat() {
    terminal.FinishCombat();
    if (frontend != null)
      frontend.FinishCombat();
  }

  public void InitiateCombat(Combat combat) {
    terminal.Combat(combat);
    if (frontend != null)
      frontend.Combat(combat);
  }

  public void GameWin() {
    terminal.GameWin();
    if (frontend != null)
      frontend.GameWin();
  }

  public void SurpriseEncounter() {
    terminal.SurpriseEncounter();
    if (frontend != null)
      frontend.SurpriseEncounter();
  }

  public void ZombieKilled(CommomZombie zombie) {
    terminal.ZombieKilled(zombie);
    if (frontend != null)
      frontend.ZombieKilled(zombie);
  }

  public void GameLose() {
    terminal.GameLose();
    if (frontend != null)
      frontend.GameLose();
  }

  public void GainedItem(String item) {
    terminal.GainedItem(item);
    if (frontend != null)
      frontend.GainedItem(item);
  }

  public Grid getGrid(int[] position) { return board.GetGrid(position); }

  public void UseBandage(boolean actionWasMade) {
    terminal.UseBandage(actionWasMade);
    if (frontend != null)
      frontend.UseBandage(actionWasMade);
  }

  public void PrintTerminal() { terminal.Print(); }

  public void Redraw(int[] position, Grid grid) {
    terminal.Redraw(position, grid);
    if (frontend != null)
      frontend.Redraw(position, grid);
  }

  public void MovePlayer(int[] position) {
    PrintTerminal();
    board.Input(position);
    PrintTerminal();
  }

  public void PlayerGunNoAmmo() {
    terminal.PlayerGunNoAmmo();
    if (frontend != null)
      frontend.PlayerGunNoAmmo();
  }

  public void PlayerNoGun() {
    terminal.PlayerNoGun();
    if (frontend != null)
      frontend.PlayerNoGun();
  }

  public void RestartBoard() { board.StartBoard(this.diff, true); }

  public BaseFrontend getFrontend() { return frontend; }

  public void setFrontend(BaseFrontend frontend) { this.frontend = frontend; }

  public Board getBoard() { return board; }
}
