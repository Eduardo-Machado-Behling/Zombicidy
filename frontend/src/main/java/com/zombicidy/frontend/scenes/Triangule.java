package com.zombicidy.frontend.scenes;

import com.zombicidy.frontend.AssetManager;
import com.zombicidy.frontend.ShaderManager;
import java.nio.FloatBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL40;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class Triangule implements IScene {
  // Define the vertex data for the triangle
  private static final float[] VERTICES = {
      -0.5f, +0.5f, 0.0f, 0.0f, // Top-left vertex
      +0.5f, +0.5f, 1.0f, 0.0f, // Top-Right vertex
      -0.5f, -0.5f, 0.0f, 1.0f, // Bottom-left vertex
      +0.5f, -0.5f, 1.0f, 1.0f  // Bottom-rightvertex
  };

  private int vbo = GL40.glGenBuffers();
  private int vao = GL40.glGenVertexArrays();
  private int tex = GL11.glGenTextures();
  private float x = 0.0f;

  public Triangule() {

    // Set up vertex buffer object (VBO) and vertex array object (VAO)
    AssetManager.Texture texData = AssetManager.get().getTexture("pointer");
    ShaderManager.get().useProgram("example");
    GL40.glClearColor(0.152f, 0.152f, 0.211f, 1.0f);

    GL40.glBindTexture(GL40.GL_TEXTURE_2D, tex);
    GL40.glEnable(GL40.GL_BLEND);
    GL40.glBlendFunc(GL40.GL_SRC_ALPHA, GL40.GL_ONE_MINUS_SRC_ALPHA);

    // Set texture parameters
    GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_MIN_FILTER,
                         GL40.GL_NEAREST);
    GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_MAG_FILTER,
                         GL40.GL_NEAREST);
    GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_WRAP_S,
                         GL40.GL_REPEAT);
    GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_WRAP_T,
                         GL40.GL_REPEAT);

    // Upload texture data to OpenGL
    GL40.glTexImage2D(GL40.GL_TEXTURE_2D, 0, GL40.GL_RGBA, texData.width,
                      texData.height, 0, GL40.GL_RGBA, GL40.GL_UNSIGNED_BYTE,
                      texData.data);
    GL40.glGenerateMipmap(GL40.GL_TEXTURE_2D);

    GL40.glBindVertexArray(vao);
    updateTransform(ShaderManager.get().getUniformLocation("transform"), x,
                    0.1f, 0, 1, 1);
    GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, vbo);

    FloatBuffer verticesBuffer =
        MemoryUtil.memAllocFloat(VERTICES.length).put(VERTICES);
    verticesBuffer.flip();
    GL40.glBufferData(GL40.GL_ARRAY_BUFFER, verticesBuffer,
                      GL40.GL_STATIC_DRAW);
    MemoryUtil.memFree(verticesBuffer);

    GL40.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 4 * Float.BYTES, 0);
    GL40.glEnableVertexAttribArray(0);

    GL40.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 4 * Float.BYTES,
                               2 * Float.BYTES);
    GL40.glEnableVertexAttribArray(1);

    GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, 0);
    GL40.glBindVertexArray(0);
  }

  @Override
  public void display(double elapsed_time) {
    GL40.glActiveTexture(GL40.GL_TEXTURE0);
    GL40.glBindTexture(GL40.GL_TEXTURE_2D, tex);
    GL40.glBindVertexArray(vao); // Bind the VAO to use the VBO
    GL40.glDrawArrays(GL40.GL_TRIANGLE_STRIP, 0, 4); // Draw the triangle
  }

  @Override
  public void onKeyEvent(long window, int key, int scancode, int action,
                         int mods) {
    if (key == GLFW.GLFW_KEY_D) {
      x += 0.1;
      updateTransform(ShaderManager.get().getUniformLocation("transform"), x,
                      0.1f, 0, 1, 1);
    } else if (key == GLFW.GLFW_KEY_A) {
      x -= 0.1;
      updateTransform(ShaderManager.get().getUniformLocation("transform"), x,
                      0.1f, 0, 1, 1);
    }
  }

  @Override
  public void onMouseEvent(long window, int button, int action, int mods) {
    // TODO Auto-generated method stub
  }

  @Override
  public void update(double elapsed_time) {
    // TODO Auto-generated method stub
  }

  @Override
  public void clean() {
    GL40.glDeleteVertexArrays(vao);
    GL40.glDeleteBuffers(vbo);
  }

  private void updateTransform(int transformLocation, float translateX,
                               float translateY, float angle, float scaleX,
                               float scaleY) {
    // Create the transformation matrix manually (column-major order)
    float cosA = (float)Math.cos(Math.toRadians(angle));
    float sinA = (float)Math.sin(Math.toRadians(angle));

    float[] transformMatrix = {
        scaleX * cosA, -sinA,         0.0f, 0.0f, // Column 1 (X-axis)
        sinA,          scaleY * cosA, 0.0f, 0.0f, // Column 2 (Y-axis)
        0.0f,          0.0f,          1.0f, 0.0f, // Column 3 (Z-axis)
        translateX,    translateY,    0.0f, 1.0f  // Column 4 (Translation)
    };

    // Send the matrix to the shader
    try (MemoryStack stack = MemoryStack.stackPush()) {
      GL40.glUniformMatrix4fv(
          transformLocation, false,
          stack.mallocFloat(16).put(transformMatrix).flip());
    }
  }
}
