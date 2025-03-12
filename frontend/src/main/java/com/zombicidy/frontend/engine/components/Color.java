package com.zombicidy.frontend.engine.components;

import com.zombicidy.frontend.ShaderManager;
import com.zombicidy.frontend.engine.math.Vector3D;

public class Color implements Component {
  private final Vector3D color;
  private final float blendFactor;

  public Color(Vector3D color, float blendFactor) {
    this.color = color;
    this.blendFactor = blendFactor;
  }

  @Override
  public void bind() {
    ShaderManager.get().setUniform("solidColor", color);
    ShaderManager.get().setUniform("blendFactor", blendFactor);
  }

  @Override
  public void unbind() {}

  @Override
  public void clean() {}
}