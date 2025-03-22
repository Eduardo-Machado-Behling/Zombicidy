package com.zombicidy.frontend.engine.components;

import com.zombicidy.frontend.ShaderManager;
import com.zombicidy.frontend.engine.math.SquareMatrix;
import com.zombicidy.frontend.engine.math.Vector3D;

public class Transform implements Component {
  private SquareMatrix matrix;
  private Vector3D translation = new Vector3D();
  private Vector3D rotation = new Vector3D();
  private Vector3D scale = new Vector3D(1);

  public Transform(Vector3D translation, Vector3D scale, Vector3D rotation) {
    this.translation = translation;
    this.scale = scale;
    this.rotation = rotation;
    genMatrix();
  }

  public Transform() { matrix = SquareMatrix.identity(4); }

  private void genMatrix() {
    matrix =
        SquareMatrix
            .scale(scale)                              // ✅ translation last
            .multiply(SquareMatrix.rotation(rotation)) // ✅ Rotate second
            .multiply(SquareMatrix.translation(translation)); // ✅  scale first
  }

  public SquareMatrix getMatrix() { return matrix; }

  @Override
  public void bind() {
    ShaderManager.get().setUniform("m_model", matrix);
  }

  @Override
  public void finalize() {}

  @Override
  public void unbind() {}

  public Vector3D getTranslation() { return translation; }

  public void setTranslation(Vector3D translation) {
    this.translation = translation;
    genMatrix();
  }

  public Vector3D getRotation() { return rotation; }

  public void setRotation(Vector3D rotation) {
    this.rotation = rotation;
    genMatrix();
  }

  public void set(Vector3D translation, Vector3D scale, Vector3D rotation) {
    this.translation = translation;
    this.scale = scale;
    this.rotation = rotation;
    genMatrix();
  }

  public Vector3D getScale() { return scale; }

  public void setScale(Vector3D scale) {
    this.scale = scale;
    genMatrix();
  }
}
