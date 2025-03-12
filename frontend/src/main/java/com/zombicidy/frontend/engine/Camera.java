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

    camera.projection = SquareMatrix.Perspective(fov, aspectRatio, nearZ, farZ);

    return camera;
  }

  public Camera lookAt(Vector3D pos, Vector3D target, Vector3D up) {
    this.position = pos;
    this.front = target;
    this.up = up;
    this.view = SquareMatrix.lookAt(pos, target, up);

    return this;
  }

  private void updateViewMatrix() {
    this.view = SquareMatrix.lookAt(position, front.add(position), up);
  }

  public Camera move(Vector3D direction, float amount) {
    position = position.add(direction.mult(amount));
    updateViewMatrix(); // Recalculate the view matrix
    return this;
  }

  public Camera rotate(float yaw, float pitch) {
    // Convert yaw and pitch to radians
    float yawRad = (float)Math.toRadians(yaw);
    float pitchRad = (float)Math.toRadians(pitch);

    // Create a rotation matrix (yaw rotation around Y, pitch rotation around X)
    float cosPitch = (float)Math.cos(pitchRad);
    float sinPitch = (float)Math.sin(pitchRad);
    float cosYaw = (float)Math.cos(yawRad);
    float sinYaw = (float)Math.sin(yawRad);

    // Compute the rotated front vector
    Vector3D newFront = new Vector3D(cosYaw * cosPitch, // X
                                     sinPitch,          // Y
                                     sinYaw * cosPitch  // Z
                                     )
                            .normalize();

    // Combine the new rotation with the existing front
    front = newFront;

    updateViewMatrix();
    return this;
  }

  public SquareMatrix getProjectionMatrix() { return projection; }

  public SquareMatrix getViewMatrix() { return view; }

  public void setProjection(SquareMatrix projection) {
    this.projection = projection;
  }

  public void setView(SquareMatrix view) { this.view = view; }

  public Vector3D getFront() { return front; }

  public Vector3D getUp() { return up; }
}
