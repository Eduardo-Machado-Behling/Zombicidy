package com.zombicidy.backend.board.combat;

import com.zombicidy.backend.EventListener;
import com.zombicidy.backend.board.Board;
import com.zombicidy.backend.board.characters.*;

public class Combat {
  private Player player;
  private CommomZombie zombie;
  private Dice dice;
  private Board board;
  private EventListener eventListener;
  private Boolean actionMade = true;

  public Combat(Player player, CommomZombie zombie, Board board,
                EventListener eventListener) {
    this.player = player;
    this.zombie = zombie;
    this.board = board;
    this.eventListener = eventListener;
    dice = new Dice();
  }

  public void Init(boolean surpriseEncounter) {
    if (surpriseEncounter) {
      eventListener.SurpriseEncounter();
      PlayerTestPerception();
    }
    eventListener.InitiateCombat(this);
  }

  public CommomZombie getZombie() { return zombie; }

  public Player getPlayer() { return player; }

  public void PlayerTestPerception() {
    if (player.TestPerception(dice.RollD3())) {
      eventListener.PlayerTookDamage(0);
    } else {
      eventListener.PlayerTookDamage(1);
    }
    if (!player.IsAlive()) {
      eventListener.GameLose();
      eventListener.FinishCombat();
    }
  }

  public void Action(String choise) {
    String type = zombie.GetType();
    int damage = 0;
    switch (choise) {
    case "Attack":
      if (type.equals("GiantZombie") && player.getPlayerBeisebalBat() == null) {
        break;
      }

      damage = player.Attack(dice.RollD6());
      zombie.TakeDamage(damage);
      eventListener.PlayerDealtDamage(damage);
      actionMade = true;
      break;
    case "Shoot":
      if (player.getPlayerGun() == null) {
        eventListener.PlayerNoGun();
        actionMade = false;
        return;
      }
      if (player.getPlayerGun().GetAmmo() == 0) {
        eventListener.PlayerGunNoAmmo();
        actionMade = false;
        return;
      }

      if (type.equals("RunnerZombie")) {
        player.Shoot();
        eventListener.PlayerDealtDamage(0);
        break;
      }
      damage = player.Shoot();
      zombie.TakeDamage(damage);
      eventListener.PlayerDealtDamage(damage);
      actionMade = true;
      break;
    case "Bandage":
      if (player.UseBandage()) {
        eventListener.UseBandage(true);
      } else {
        eventListener.UseBandage(false);
      }
      actionMade = false;
      break;
    case "Run":
      Run();
      break;
    }
    if (actionMade) {
      if (zombie.IsAlive()) {
        PlayerTestPerception();
      } else {
        if (zombie.GetType().equals("GiantZombie")) {
          eventListener.GameWin();
        }
        board.FinishCombat(zombie);
      }
    }
  }

  public void Run() { board.FinishCombat(zombie); }
}