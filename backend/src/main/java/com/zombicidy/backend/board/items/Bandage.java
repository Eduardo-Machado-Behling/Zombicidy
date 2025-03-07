package com.zombicidy.backend.board.items;

public class Bandage extends Item {
  private int heal;

  public Bandage(int heal) { this.heal = heal; }

  public int Heal() { return heal; }
}
