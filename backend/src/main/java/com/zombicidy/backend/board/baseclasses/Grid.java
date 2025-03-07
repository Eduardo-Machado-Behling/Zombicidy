package com.zombicidy.backend.board.baseclasses;

public abstract class Grid extends GetTypeName {
  protected int[] position = new int[2];

  public int[] getPosition() { return position; }
  public void setPosition(int[] position) { this.position = position.clone(); }
}
