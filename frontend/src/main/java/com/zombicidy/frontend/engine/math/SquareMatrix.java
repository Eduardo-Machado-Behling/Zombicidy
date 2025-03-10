package com.zombicidy.frontend.engine.math;

import java.nio.FloatBuffer;
import org.lwjgl.system.MemoryStack;


public class SquareMatrix {
  private final float[] data;
  private final int size;

  public SquareMatrix(int size) {
    this.size = size;
    this.data = new float[size * size];
  }

  public static SquareMatrix identity(int size) {
    SquareMatrix identity = new SquareMatrix(size);
    for (int i = 0; i < size; i++)
      identity.set(i, i, 1.0f);
    return identity;
  }

  public float get(int i, int j) { return data[i * size + j]; }

  public SquareMatrix set(int i, int j, float value) {
    data[i * size + j] = value;
    return this;
  }

  // Matrix addition
  public SquareMatrix add(SquareMatrix other) {
    for (int i = 0; i < size * size; i++) {
      this.data[i] += other.data[i];
    }
    return this;
  }

  // Matrix subtraction
  public SquareMatrix subtract(SquareMatrix other) {
    for (int i = 0; i < size * size; i++) {
      this.data[i] -= other.data[i];
    }

    return this;
  }

  // Scalar multiplication
  public SquareMatrix multiply(float scalar) {
    for (int i = 0; i < size * size; i++) {
      this.data[i] *= scalar;
    }
    return this;
  }

  // Matrix multiplication
  public SquareMatrix multiply(SquareMatrix other) {
    SquareMatrix result = new SquareMatrix(size);
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        float sum = 0;
        for (int k = 0; k < size; k++) {
          sum += this.get(i, k) * other.get(k, j);
        }
        result.set(i, j, sum);
      }
    }
    return result;
  }

  // Transpose matrix
  public SquareMatrix transpose() {
    SquareMatrix transposed = new SquareMatrix(size);
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        transposed.set(j, i, this.get(i, j));
      }
    }
    return transposed;
  }

  public static SquareMatrix rotation(Vector3D vector) {
    SquareMatrix Rz = SquareMatrix.identity(4)
                          .set(0, 0, +(float)Math.cos(vector.z))
                          .set(0, 1, -(float)Math.sin(vector.z))
                          .set(1, 0, +(float)Math.sin(vector.z))
                          .set(1, 1, -(float)Math.cos(vector.z));

    SquareMatrix Ry = SquareMatrix.identity(4)
                          .set(0, 0, +(float)Math.cos(vector.y))
                          .set(0, 2, +(float)Math.sin(vector.y))
                          .set(2, 0, -(float)Math.sin(vector.y))
                          .set(2, 2, +(float)Math.cos(vector.y));

    SquareMatrix Rx = SquareMatrix.identity(4)
                          .set(1, 1, +(float)Math.cos(vector.x))
                          .set(1, 2, -(float)Math.sin(vector.x))
                          .set(2, 1, +(float)Math.sin(vector.x))
                          .set(2, 2, +(float)Math.cos(vector.x));

    return Rz.multiply(Ry).multiply(Rx);
  }

  public static SquareMatrix translation(Vector3D vector) {
    return SquareMatrix.identity(4)
        .set(0, 3, vector.x)
        .set(1, 3, vector.y)
        .set(2, 3, vector.z);
  }

  public static SquareMatrix lookAt(Vector3D eye, Vector3D center,
                                    Vector3D up) {
    Vector3D zAxis = eye.sub(center).normalize(); // Forward vector
    Vector3D xAxis = up.cross(zAxis).normalize(); // Right vector
    Vector3D yAxis = zAxis.cross(xAxis);          // Up vector

    return SquareMatrix.identity(4)
        .set(0, 0, xAxis.x)
        .set(1, 0, xAxis.y)
        .set(2, 0, xAxis.z)
        .set(0, 1, yAxis.x)
        .set(1, 1, yAxis.y)
        .set(2, 1, yAxis.z)
        .set(0, 2, zAxis.x)
        .set(1, 2, zAxis.y)
        .set(2, 2, zAxis.z)
        .set(3, 0, -xAxis.dot(eye))
        .set(3, 1, -yAxis.dot(eye))
        .set(3, 2, -zAxis.dot(eye))
        .set(3, 3, 1.0f);
  }

  public static SquareMatrix scale(Vector3D vector) {
    return SquareMatrix.identity(4)
        .set(0, 0, vector.x)
        .set(1, 1, vector.y)
        .set(2, 2, vector.z);
  }

  public FloatBuffer toFloatBuffer() {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      FloatBuffer buffer = stack.mallocFloat(size * size);
      buffer.put(data).flip();
      return buffer;
    }
  }

  public FloatBuffer toFloatBuffer(FloatBuffer buffer) {
    buffer.put(data).flip();
    return buffer;
  }

  public void print() {
    System.out.println("[");
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        System.err.print(get(i, j) + ", ");
      }
      System.out.print('\n');
    }
    System.out.println("]");
  }

  public int SizeBytes() { return size * size * Float.BYTES; }
}
