package com.zombicidy.frontend;

import java.util.HashMap;
import org.lwjgl.opengl.GL40;

public class ShaderManager {
  static private ShaderManager instance = null;

  private final HashMap<String, Integer> programs = new HashMap<>();
  private int currentProgram = -1;

  private ShaderManager() {}

  static public ShaderManager get() {
    if (instance == null) {
      instance = new ShaderManager();
    }

    return instance;
  }

  public boolean useProgram(String name) {
    if (!programs.containsKey(name)) {
      try {
        programs.put(name, compileProgram(name));
      } catch (RuntimeException e) {
        return false;
      }
    }

    currentProgram = programs.get(name);
    GL40.glUseProgram(currentProgram);

    return true;
  }

  public int getUniformLocation(String name) {
    return GL40.glGetUniformLocation(currentProgram, name);
  }

  private enum ShaderType {
    VERTEX(GL40.GL_VERTEX_SHADER),
    FRAGMENT(GL40.GL_FRAGMENT_SHADER);

    private final int shaderType;

    // Constructor to assign the value to each constant
    ShaderType(int shaderType) { this.shaderType = shaderType; }

    // Getter method to retrieve the shader type value
    public int getShaderType() { return shaderType; }
  }

  private int compileVertex(String name) {
    return compileShader(getSource(name, ShaderType.VERTEX), ShaderType.VERTEX);
  }
  private int compileFragment(String name) {
    return compileShader(getSource(name, ShaderType.FRAGMENT),
                         ShaderType.FRAGMENT);
  }
  private int compileProgram(String name) {
    int vertex = compileVertex(name);
    int fragment = compileFragment(name);

    // Create the program object
    int program = GL40.glCreateProgram();
    if (program == 0) {
      throw new RuntimeException("Error creating shader program");
    }

    // Attach the shaders to the program
    GL40.glAttachShader(program, vertex);
    GL40.glAttachShader(program, fragment);

    // Link the program
    GL40.glLinkProgram(program);

    // Check for linking errors
    if (GL40.glGetProgrami(program, GL40.GL_LINK_STATUS) == GL40.GL_FALSE) {
      String errorMessage = GL40.glGetProgramInfoLog(program);
      throw new RuntimeException("Error linking program: " + errorMessage);
    }

    // Validate the program
    GL40.glValidateProgram(program);
    if (GL40.glGetProgrami(program, GL40.GL_VALIDATE_STATUS) == GL40.GL_FALSE) {
      String errorMessage = GL40.glGetProgramInfoLog(program);
      throw new RuntimeException("Error validating program: " + errorMessage);
    }

    return program;
  }

  private int compileShader(String shaderCode, ShaderType shaderType) {
    // Create the shader object
    int shader = GL40.glCreateShader(shaderType.getShaderType());
    if (shader == 0) {
      throw new RuntimeException("Error creating " + shaderType + "shader");
    }

    // Attach the source code to the shader
    System.err.println(shaderType + "\n" + shaderCode + "\n\n");
    GL40.glShaderSource(shader, shaderCode);
    GL40.glCompileShader(shader);

    // Check for compile errors
    if (GL40.glGetShaderi(shader, GL40.GL_COMPILE_STATUS) == GL40.GL_FALSE) {
      String errorMessage = GL40.glGetShaderInfoLog(shader);
      throw new RuntimeException("Error compiling shader: " + errorMessage);
    }

    return shader;
  }

  private String getSource(String name, ShaderType shaderType) {
    String filePath =
        "shaders/" + name + (shaderType == ShaderType.FRAGMENT ? ".fs" : ".vs");
    try (java.io.InputStream inputStream =
             ShaderManager.class.getClassLoader().getResourceAsStream(
                 filePath)) {
      if (inputStream == null) {
        throw new RuntimeException("Shader file not found: " + filePath);
      }

      StringBuilder shaderCode = new StringBuilder();
      try (java.util.Scanner scanner = new java.util.Scanner(inputStream)) {
        while (scanner.hasNextLine()) {
          shaderCode.append(scanner.nextLine()).append("\n");
        }
      }

      return shaderCode.toString();
    } catch (java.io.IOException e) {
      throw new RuntimeException("Error reading shader file: " + filePath, e);
    }
  }
}
