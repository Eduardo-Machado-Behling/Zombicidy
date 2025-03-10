package com.zombicidy.frontend.engine.math;

public class Vector3D {
  public float x, y, z;

  public Vector3D(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  static public int SizeBytes() { return 3 * Float.BYTES; }

  public Vector3D cross(Vector3D other) {
    return new Vector3D(this.y * other.z - this.z * other.y,
                        this.z * other.x - this.x * other.z,
                        this.x * other.y - this.y * other.x);
  }

  public Vector3D sub(Vector3D other) {
    return new Vector3D(x - other.x, y - other.y, z - other.z);
  }

  public Vector3D add(Vector3D other) {
    return new Vector3D(x + other.x, y + other.y, z + other.z);
  }

  public Vector3D divide(float scalar) {
    return new Vector3D(x / scalar, y / scalar, z / scalar);
  }

  public Vector3D divide(Vector3D other) {
    return new Vector3D(x / other.x, y / other.y, z / other.z);
  }

  public Vector3D mult(float scalar) {
    return new Vector3D(x * scalar, y * scalar, z * scalar);
  }

  public Vector3D mult(Vector3D other) {
    return new Vector3D(x * other.x, y * other.y, z * other.z);
  }

  public Vector3D abs() {
    return new Vector3D(Math.abs(x), Math.abs(y), Math.abs(z));
  }

  @Override
  public String toString() {
    return String.format("Vector3(%.2f, %.2f, %.2f)", x, y, z);
  }

  public float magnitude() { return (float)Math.sqrt(x * x + y * y + z * z); }

  public Vector3D normalize() {
    float mag = magnitude();
    return this.divide(mag);
  }

  public float dot(Vector3D other) {
    return this.x * other.x + this.y * other.y + this.z * other.z;
  }
}
