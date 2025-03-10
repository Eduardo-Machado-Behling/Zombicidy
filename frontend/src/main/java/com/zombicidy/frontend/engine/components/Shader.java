package com.zombicidy.frontend.engine.components;

public class Shader {
  final private String program;

  public Shader() { this.program = "default"; }
  public Shader(String program) { this.program = program; }

  public String getProgram() { return program; }
}
