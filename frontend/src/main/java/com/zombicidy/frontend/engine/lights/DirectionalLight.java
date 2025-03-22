package com.zombicidy.frontend.engine.lights;

import com.zombicidy.frontend.ShaderManager;
import com.zombicidy.frontend.engine.math.Vector3D;

public class DirectionalLight extends Light {
  public Vector3D direction;

  public DirectionalLight(Vector3D direction, Vector3D ambient,
                          Vector3D diffuse, Vector3D specular) {
    super(ambient, diffuse, specular);
    this.direction = direction;
  }

  @Override
  public int bind(int i) {
    String base = String.format("dLight");
    ShaderManager.get().setUniform(base + ".direction", direction);
    super.bind(base, i);
    return 0;
  }
}