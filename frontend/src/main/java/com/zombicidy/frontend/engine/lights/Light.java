package com.zombicidy.frontend.engine.lights;

import com.zombicidy.frontend.ShaderManager;
import com.zombicidy.frontend.engine.math.Vector3D;

public abstract class Light {
  private final Vector3D ambient;
  private final Vector3D diffuse;
  private final Vector3D specular;

  protected Light(Vector3D ambient, Vector3D diffuse, Vector3D specular) {
    this.ambient = ambient;
    this.diffuse = diffuse;
    this.specular = specular;
  }

  public abstract int bind(int i);

  protected void bind(String base, int i) {
    ShaderManager.get().setUniform(base + ".light.ambient", ambient);
    ShaderManager.get().setUniform(base + ".light.diffuse", diffuse);
    ShaderManager.get().setUniform(base + ".light.specular", specular);
  }

  public void unbind() {}
}
