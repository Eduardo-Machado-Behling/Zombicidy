package com.zombicidy.frontend.engine.math;

import java.nio.FloatBuffer;

public class Vector2D {

  public float x, y;

  public Vector2D(float x, float y) {
    this.x = x;
    this.y = y;
  }

  static public int SizeBytes() { return 2 * Float.BYTES; }

  public Vector2D(Vector2D other) {
    x = other.x;
    y = other.y;
  }

  @Override
  public String toString() {
    return String.format("Vector2(%.2f, %.2f)", x, y);
  }

  public float length() { return x * x + y * y; }

  public Vector2D rotate(float degrees) {
    // px = x * cs - y * sn;
    // py = x * sn + y * cs;
    double rad = Math.toRadians(degrees);
    return new Vector2D((float)(x * Math.cos(rad) - y * Math.sin(rad)),
                        (float)(x * Math.sin(rad) + y * Math.cos(rad)));
  }

  public Vector2D add(Vector2D o) { return new Vector2D(x + o.x, y + o.y); }

  public Vector2D sub(Vector2D o) { return new Vector2D(x - o.x, y - o.y); }

  public FloatBuffer toFloatBuffer(FloatBuffer buffer) {
    buffer.put(x);
    buffer.put(y);
    buffer.rewind();
    return buffer;
  }
}
