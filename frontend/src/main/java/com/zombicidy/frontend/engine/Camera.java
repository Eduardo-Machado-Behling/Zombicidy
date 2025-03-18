package com.zombicidy.frontend.engine;

import com.zombicidy.frontend.Window;
import com.zombicidy.frontend.engine.math.SquareMatrix;
import com.zombicidy.frontend.engine.math.Vector2D;
import com.zombicidy.frontend.engine.math.Vector3D;

public class Camera {
  private SquareMatrix projection = SquareMatrix.identity(4);
  private SquareMatrix view = SquareMatrix.identity(4);

  private Vector3D position;
  private Vector3D front;
  private Vector3D up;
  private float fov;

  public Camera() {}

  public static Camera Orthographic() {
    Camera camera = new Camera();

    return camera;
  }

  public static Camera Perspective(float fov, float aspectRatio, float nearZ,
                                   float farZ) {
    Camera camera = new Camera();

    camera.projection = SquareMatrix.Perspective(fov, aspectRatio, nearZ, farZ);
    camera.fov = fov;

    return camera;
  }

  public Camera lookAt(Vector3D pos, Vector3D target, Vector3D up) {
    this.position = pos;
    this.front = target;
    this.up = up;
    this.view = SquareMatrix.lookAt(pos, front.add(position), up);

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

  public Vector3D mouseToWorld(Vector2D mousePos) {
    // 1. Normalize mouse coordinates to range [-1, 1]
    float normalizedX = 2.0f * mousePos.x / Window.get().width() - 1.0f;
    float normalizedY =
        1.0f - 2.0f * mousePos.y /
                   Window.get().height(); // Invert Y for OpenGL convention

    Vector3D right = front.cross(up).normalize();

    // 3. Calculate the direction vector based on FOV and aspect ratio
    float aspectRatio = Window.get().getAspectRatio();
    float tanHalfFOV = (float)Math.tan(Math.toRadians(fov / 2.0f));

    Vector3D direction = new Vector3D();

    // Calculate the offset from the center of the screen in world space
    Vector3D rightOffset = right.mult(normalizedX * aspectRatio * tanHalfFOV);
    Vector3D upOffset = up.mult(normalizedY * tanHalfFOV);

    // The direction vector is the sum of the camera's forward direction and the
    // offsets
    direction = direction.add(front);
    direction = direction.add(rightOffset);
    direction = direction.add(upOffset);

    // Normalize the resulting direction vector
    return direction.normalize();
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

  public Camera rotateLooking(float yaw, float pitch, Vector3D target) {
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
    front = target;
    this.view = SquareMatrix.lookAt(position, front, up);
    return this;
  }

  public SquareMatrix getProjectionMatrix() { return projection; }

  public SquareMatrix getViewMatrix() { return view; }

  public void setProjection(float fov, float aspectRatio, float nearZ,
                            float farZ) {
    this.projection = SquareMatrix.Perspective(fov, aspectRatio, nearZ, farZ);
    this.fov = fov;
  }

  public void setView(SquareMatrix view) { this.view = view; }

  public Vector3D getFront() { return front; }

  public Vector3D getUp() { return up; }

  public Vector3D getPosition() { return position; }

  public void moveLooking(Vector3D pos, Vector3D cameraTarget) {
    this.front = cameraTarget.sub(pos).mult(-1);
    this.position = pos;
    this.view = SquareMatrix.lookAt(pos, cameraTarget, up);
  }
}
