package com.zombicidy.frontend.engine.math;

public class Vector2D {

  public float x, y;

  public Vector2D(float x, float y) {
    this.x = x;
    this.y = y;
  }

  static public int SizeBytes() { return 2 * Float.BYTES; }

  @Override
  public String toString() {
    return String.format("Vector2(%.2f, %.2f)", x, y);
  }
}
