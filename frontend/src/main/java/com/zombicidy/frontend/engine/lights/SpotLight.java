package com.zombicidy.frontend.engine.lights;

import com.zombicidy.frontend.ShaderManager;
import com.zombicidy.frontend.engine.math.Vector3D;

public class SpotLight extends Light {
  public Vector3D position;
  public Vector3D direction;
  public float cutout;

  public SpotLight(Vector3D ambient, Vector3D diffuse, Vector3D specular,
                   Vector3D position, Vector3D direction, float cutout) {
    super(ambient, diffuse, specular);
    this.position = position;
    this.direction = direction;
    this.cutout = cutout;
  }

  @Override
  public int bind(int i) {
    String base = String.format("spotLights[%d]", i);
    ShaderManager.get().setUniform(base + ".position", position);
    ShaderManager.get().setUniform(base + ".direction", position);
    ShaderManager.get().setUniform(base + ".cutout",
                                   (float)Math.cos(Math.toRadians(cutout)));
    super.bind(base, i);
    return 1;
  }

  public Vector3D getPosition() { return position; }

  public void setPosition(Vector3D position) { this.position = position; }
}
