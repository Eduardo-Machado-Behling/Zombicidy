package com.zombicidy.frontend.engine.math;

import java.nio.FloatBuffer;

public class Vector4D {

  public float x = 0, y = 0, z = 0, w = 0;

  public Vector4D() {}
  public Vector4D(float val) {
    this.x = val;
    this.y = val;
    this.z = val;
    this.w = val;
  }

  public Vector4D(float x, float y, float z, float w) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.w = w;
  }

  public Vector4D(Vector3D position, float f) {
    x = position.x;
    y = position.y;
    z = position.z;
    w = f;
  }
  static public int SizeBytes() { return 2 * Float.BYTES; }

  @Override
  public String toString() {
    return String.format("Vector2(%.2f, %.2f, %.2f, %.2f) ", x, y, z, w);
  }

  public FloatBuffer toFloatBuffer(FloatBuffer buffer) {
    buffer.put(x);
    buffer.put(y);
    buffer.put(z);
    buffer.put(w);
    buffer.flip();

    return buffer;
  }
  public Vector3D toVec3() { return new Vector3D(x, y, z); }
  public Vector4D divideW() { return new Vector4D(x / w, y / w, z / w, w); }
}
