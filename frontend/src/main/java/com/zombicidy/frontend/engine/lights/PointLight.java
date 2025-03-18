package com.zombicidy.frontend.engine.lights;

import com.zombicidy.frontend.ShaderManager;
import com.zombicidy.frontend.engine.math.Vector3D;

public class PointLight extends Light {
  public Vector3D position;

  public float constant;
  public float linear;
  public float quadratic;

  public PointLight(Vector3D ambient, Vector3D diffuse, Vector3D specular,
                    Vector3D position, float constant, float linear,
                    float quadratic) {

    super(ambient, diffuse, specular);
    this.position = position;
    this.constant = constant;
    this.linear = linear;
    this.quadratic = quadratic;
  }

  public PointLight(Vector3D position, Vector3D ambient, Vector3D diffuse,
                    Vector3D specular) {
    super(ambient, diffuse, specular);
    this.position = position;
  }

  @Override
  public int bind(int i) {
    String base = String.format("pLights[%d]", i);
    ShaderManager.get().setUniform(base + ".position", position);
    ShaderManager.get().setUniform(base + ".linear", linear);
    ShaderManager.get().setUniform(base + ".constant", constant);
    ShaderManager.get().setUniform(base + ".quadratic", quadratic);
    super.bind(base, i);
    return 1;
  }

  public Vector3D getPosition() { return position; }

  public void setPosition(Vector3D position) { this.position = position; }
}
