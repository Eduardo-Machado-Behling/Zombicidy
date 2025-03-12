package com.zombicidy.frontend.engine.components;

import com.zombicidy.frontend.ShaderManager;

public class Shader implements Component {
  final private String program;

  public Shader() { this.program = "default"; }
  public Shader(String program) { this.program = program; }

  public String getProgram() { return program; }
  @Override
  public void bind() {
    ShaderManager.get().useProgram(program);
  }
  @Override
  public void clean() {}

  @Override
  public void unbind() {}
}
