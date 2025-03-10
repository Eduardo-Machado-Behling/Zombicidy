package com.zombicidy.frontend.engine;

import com.zombicidy.frontend.engine.math.SquareMatrix;
import com.zombicidy.frontend.engine.math.Vector3D;

public class Camera {
  private SquareMatrix projection = SquareMatrix.identity(4);
  private SquareMatrix view = SquareMatrix.identity(4);

  private Vector3D position;
  private Vector3D front;
  private Vector3D up;

  public Camera() {}

  public static Camera Orthographic() {
    Camera camera = new Camera();

    return camera;
  }

  public static Camera Perspective(float fov, float aspectRatio, float nearZ,
                                   float farZ) {
    Camera camera = new Camera();

    float tan = (float)Math.tan(Math.toRadians(fov / 2.0f));
    camera.projection.set(0, 0, 1 / (tan * aspectRatio))
        .set(1, 1, 1 / tan)
        .set(2, 2, -(farZ + nearZ) / (farZ - nearZ))
        .set(2, 3, -1.0f)
        .set(3, 2, -(2 * farZ * nearZ) / (farZ - nearZ))
        .set(3, 3, 0.0f);

    return camera;
  }

  public Camera lookAt(Vector3D pos, Vector3D target, Vector3D up) {
    this.position = pos;
    this.front = target;
    this.up = up;
    this.view = SquareMatrix.lookAt(pos, target, up);

    return this;
  }

  private void updateViewMatrix() { lookAt(position, front, up); }

  public Camera move(Vector3D direction, float amount) {
    position.add(direction.mult(amount));
    updateViewMatrix(); // Recalculate the view matrix
    return this;
  }

  public Camera rotate(float yaw, float pitch) {
    front.x = (float)Math.cos(Math.toRadians(yaw)) *
              (float)Math.cos(Math.toRadians(pitch));
    front.y = (float)Math.sin(Math.toRadians(pitch));
    front.z = (float)Math.sin(Math.toRadians(yaw)) *
              (float)Math.cos(Math.toRadians(pitch));

    front.normalize();
    updateViewMatrix();

    return this;
  }

  public SquareMatrix getProjectionMatrix() { return projection; }

  public SquareMatrix getViewMatrix() { return view; }
}
