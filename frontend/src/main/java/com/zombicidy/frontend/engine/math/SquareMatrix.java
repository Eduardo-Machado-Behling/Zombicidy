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

  public Vector4D multiply(Vector4D v) {
    Vector4D vec = new Vector4D();

    vec.x =
        get(0, 0) * v.x + get(0, 1) * v.y + get(0, 2) * v.z + get(0, 3) * v.w;
    vec.y =
        get(1, 0) * v.x + get(1, 1) * v.y + get(1, 2) * v.z + get(1, 3) * v.w;
    vec.z =
        get(2, 0) * v.x + get(2, 1) * v.y + get(2, 2) * v.z + get(2, 3) * v.w;
    vec.w =
        get(3, 0) * v.x + get(3, 1) * v.y + get(3, 2) * v.z + get(3, 3) * v.w;

    return vec;
  }

  // Matrix multiplication
  public SquareMatrix multiply(SquareMatrix other) {
    System.out.println("A*B");
    System.err.print("A = ");
    print();
    System.err.print("\nB = ");
    print();
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

  public static SquareMatrix rotation(Vector3D rotation) {
    Vector3D rotRadians = new Vector3D((float)Math.toRadians(rotation.x),
                                       (float)Math.toRadians(rotation.y),
                                       (float)Math.toRadians(rotation.z));

    SquareMatrix Rz = SquareMatrix.identity(4)
                          .set(0, 0, +(float)Math.cos(rotRadians.z))
                          .set(0, 1, -(float)Math.sin(rotRadians.z))
                          .set(1, 0, +(float)Math.sin(rotRadians.z))
                          .set(1, 1, +(float)Math.cos(rotRadians.z));

    SquareMatrix Ry = SquareMatrix.identity(4)
                          .set(0, 0, +(float)Math.cos(rotRadians.y))
                          .set(0, 2, +(float)Math.sin(rotRadians.y))
                          .set(2, 0, -(float)Math.sin(rotRadians.y))
                          .set(2, 2, +(float)Math.cos(rotRadians.y));

    SquareMatrix Rx = SquareMatrix.identity(4)
                          .set(1, 1, +(float)Math.cos(rotRadians.x))
                          .set(1, 2, -(float)Math.sin(rotRadians.x))
                          .set(2, 1, +(float)Math.sin(rotRadians.x))
                          .set(2, 2, +(float)Math.cos(rotRadians.x));

    return Rz.multiply(Ry).multiply(Rx);
  }

  public static SquareMatrix translation(Vector3D vector) {
    return SquareMatrix.identity(4)
        .set(3, 0, vector.x)
        .set(3, 1, vector.y)
        .set(3, 2, vector.z);
  }

  // thanks <https://stackoverflow.com/questions/1148309/inverting-a-4x4-matrix>
  // will try to understand this later
  public SquareMatrix inverse() {
    if (this.size != 4) {
      throw new RuntimeException("Only supported for 4x4");
    }

    SquareMatrix inv = SquareMatrix.identity(4);
    float det;

    inv.data[0] = data[5] * data[10] * data[15] -
                  data[5] * data[11] * data[14] - data[9] * data[6] * data[15] +
                  data[9] * data[7] * data[14] + data[13] * data[6] * data[11] -
                  data[13] * data[7] * data[10];

    inv.data[4] = -data[4] * data[10] * data[15] +
                  data[4] * data[11] * data[14] + data[8] * data[6] * data[15] -
                  data[8] * data[7] * data[14] - data[12] * data[6] * data[11] +
                  data[12] * data[7] * data[10];

    inv.data[8] = data[4] * data[9] * data[15] - data[4] * data[11] * data[13] -
                  data[8] * data[5] * data[15] + data[8] * data[7] * data[13] +
                  data[12] * data[5] * data[11] - data[12] * data[7] * data[9];

    inv.data[12] = -data[4] * data[9] * data[14] +
                   data[4] * data[10] * data[13] +
                   data[8] * data[5] * data[14] - data[8] * data[6] * data[13] -
                   data[12] * data[5] * data[10] + data[12] * data[6] * data[9];

    inv.data[1] = -data[1] * data[10] * data[15] +
                  data[1] * data[11] * data[14] + data[9] * data[2] * data[15] -
                  data[9] * data[3] * data[14] - data[13] * data[2] * data[11] +
                  data[13] * data[3] * data[10];

    inv.data[5] = data[0] * data[10] * data[15] -
                  data[0] * data[11] * data[14] - data[8] * data[2] * data[15] +
                  data[8] * data[3] * data[14] + data[12] * data[2] * data[11] -
                  data[12] * data[3] * data[10];

    inv.data[9] = -data[0] * data[9] * data[15] +
                  data[0] * data[11] * data[13] + data[8] * data[1] * data[15] -
                  data[8] * data[3] * data[13] - data[12] * data[1] * data[11] +
                  data[12] * data[3] * data[9];

    inv.data[13] = data[0] * data[9] * data[14] -
                   data[0] * data[10] * data[13] -
                   data[8] * data[1] * data[14] + data[8] * data[2] * data[13] +
                   data[12] * data[1] * data[10] - data[12] * data[2] * data[9];

    inv.data[2] = data[1] * data[6] * data[15] - data[1] * data[7] * data[14] -
                  data[5] * data[2] * data[15] + data[5] * data[3] * data[14] +
                  data[13] * data[2] * data[7] - data[13] * data[3] * data[6];

    inv.data[6] = -data[0] * data[6] * data[15] + data[0] * data[7] * data[14] +
                  data[4] * data[2] * data[15] - data[4] * data[3] * data[14] -
                  data[12] * data[2] * data[7] + data[12] * data[3] * data[6];

    inv.data[10] = data[0] * data[5] * data[15] - data[0] * data[7] * data[13] -
                   data[4] * data[1] * data[15] + data[4] * data[3] * data[13] +
                   data[12] * data[1] * data[7] - data[12] * data[3] * data[5];

    inv.data[14] = -data[0] * data[5] * data[14] +
                   data[0] * data[6] * data[13] + data[4] * data[1] * data[14] -
                   data[4] * data[2] * data[13] - data[12] * data[1] * data[6] +
                   data[12] * data[2] * data[5];

    inv.data[3] = -data[1] * data[6] * data[11] + data[1] * data[7] * data[10] +
                  data[5] * data[2] * data[11] - data[5] * data[3] * data[10] -
                  data[9] * data[2] * data[7] + data[9] * data[3] * data[6];

    inv.data[7] = data[0] * data[6] * data[11] - data[0] * data[7] * data[10] -
                  data[4] * data[2] * data[11] + data[4] * data[3] * data[10] +
                  data[8] * data[2] * data[7] - data[8] * data[3] * data[6];

    inv.data[11] = -data[0] * data[5] * data[11] + data[0] * data[7] * data[9] +
                   data[4] * data[1] * data[11] - data[4] * data[3] * data[9] -
                   data[8] * data[1] * data[7] + data[8] * data[3] * data[5];

    inv.data[15] = data[0] * data[5] * data[10] - data[0] * data[6] * data[9] -
                   data[4] * data[1] * data[10] + data[4] * data[2] * data[9] +
                   data[8] * data[1] * data[6] - data[8] * data[2] * data[5];

    det = data[0] * inv.data[0] + data[1] * inv.data[4] +
          data[2] * inv.data[8] + data[3] * inv.data[12];

    if (det == 0)
      throw new RuntimeException("Inversable matrix");

    det = 1.0f / det;

    return inv.multiply(det);
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

  public static SquareMatrix Perspective(float fov, float aspectRatio,
                                         float nearZ, float farZ) {

    float tan = (float)Math.tan(Math.toRadians(fov / 2.0f));
    float top = nearZ * tan;         // half height of near plane
    float right = top * aspectRatio; // half width of near plane

    return SquareMatrix.identity(4)
        .set(0, 0, nearZ / right)
        .set(1, 1, nearZ / top)
        .set(2, 2, -(farZ + nearZ) / (farZ - nearZ))
        .set(2, 3, -1.0f)
        .set(3, 2, -(2 * farZ * nearZ) / (farZ - nearZ))
        .set(3, 3, 0.0f);
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
