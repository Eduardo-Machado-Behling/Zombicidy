package com.zombicidy.frontend.engine.components;

import com.zombicidy.frontend.ShaderManager;

public class UUID implements Component {
  private static int __ID = 1;

  public static void clear() { __ID = 1; }
  private final int id;

  public UUID() { id = __ID++; }

  public int getId() { return id; }

  @Override
  public void bind() {
    ShaderManager.get().setUniform("UUID", id);
  }

  @Override
  public void unbind() {}

  @Override
  public void finalize() {}
}
