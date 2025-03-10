package com.zombicidy.frontend.engine.components;

import com.zombicidy.frontend.engine.math.SquareMatrix;
import com.zombicidy.frontend.engine.math.Vector3D;

public class Transform {
  private SquareMatrix matrix;

  public Transform() { matrix = SquareMatrix.identity(4); }

  public Transform(Vector3D translation, Vector3D scale, Vector3D rotation) {
    matrix = SquareMatrix
                 .translation(translation) // ✅ Apply translation last
                 .multiply(SquareMatrix.rotation(rotation)) // ✅ Rotate second
                 .multiply(SquareMatrix.scale(scale)); // ✅ Apply scale first
  }

  public SquareMatrix getMatrix() { return matrix; }
  public void setMatrix(SquareMatrix matrix) { this.matrix = matrix; }
}
