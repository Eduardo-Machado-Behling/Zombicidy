package com.zombicidy.backend.board.characters;
import com.zombicidy.backend.board.items.*;
import java.util.ArrayList;

public class Player extends Character {
  protected int perception;
  private ArrayList<Item> items = new ArrayList<Item>();
  private Gun playerGun;
  private BeisebolBat playerBeisebalBat;

  public Player(int healthPoints, int movement, int perception) {
    super(healthPoints, movement);
    this.perception = perception;
    playerBeisebalBat = null;
    playerGun = null;
  }

  public void GainItem(Item item) {
    if (item instanceof Gun) {
      if (playerGun != null) {
        playerGun.AddAmmo();
      } else {
        playerGun = (Gun)item;
        items.add(item);
      }
    }

    if (item instanceof BeisebolBat) {
      if (playerBeisebalBat == null) {
        playerBeisebalBat = (BeisebolBat)item;
        items.add(item);
      }
    }

    if (item instanceof Bandage) {
      items.add(item);
    }
  }

  public Gun getPlayerGun() { return playerGun; }

  public BeisebolBat getPlayerBeisebalBat() { return playerBeisebalBat; }

  public void Heal(int heal) { healthPoints += heal; }

  public int Attack(int diceRoll) {
    int bonus = 0;
    if (getPlayerBeisebalBat() != null) {
      bonus = getPlayerBeisebalBat().getBonus();
    }

    if (diceRoll == 0) {
      return 0 + bonus;
    }
    if (diceRoll == 6) {
      return 2 + bonus;
    }
    return 1 + bonus;
  }

  public int Shoot() {
    if (getPlayerGun() != null) {
      return getPlayerGun().Attack();
    } else {
      return -1; // in case the is no gun, if returns 0 it has no ammo
    }
  }

  public boolean UseBandage() {
    for (Item item : items) {
      if (item instanceof Bandage) {
        Heal(((Bandage)item).Heal());
        items.remove(item);
        return true;
      }
    }
    return false;
  }

  public boolean TestPerception(int diceRoll) {
    if (diceRoll <= perception) {
      return true;
    } else {
      TakeDamage(1);
      return false;
    }
  }
}
