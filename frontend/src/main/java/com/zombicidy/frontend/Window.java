package com.zombicidy.frontend;

import com.zombicidy.frontend.scenes.IScene;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class Window {
  static private Window rendererInstance = null;

  private long window;
  private IScene scene = null;

  private int width = 800;
  private int height = 600;

  private double last_time = GLFW.glfwGetTime();
  private double curr_time;

  private Window() {
    if (!GLFW.glfwInit()) {
      throw new IllegalStateException("Failed to initialize GLFW!");
    }

    window = GLFW.glfwCreateWindow(width, height, "LWJGL Window", 0, 0);
    if (window == 0) {
      throw new RuntimeException("Failed to create the GLFW window");
    }

    GLFW.glfwMakeContextCurrent(window);
    GL.createCapabilities();

    GLFW.glfwSetKeyCallback(window, this::onKeyEvent);
    GLFW.glfwSetMouseButtonCallback(window, this::onMouseEvent);
    GLFW.glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
      this.width = width;
      this.height = height;
    });
  }

  static public Window get() {
    if (rendererInstance == null) {
      rendererInstance = new Window();
    }

    return rendererInstance;
  }

  private void onKeyEvent(long window, int key, int scancode, int action,
                          int mods) {
    if (action == GLFW.GLFW_PRESS) {
      System.out.println("Key pressed: " + key);
    } else if (action == GLFW.GLFW_RELEASE) {
      System.out.println("Key released: " + key);
    }

    scene.onKeyEvent(window, key, scancode, action, mods);
  }

  private void onMouseEvent(long window, int button, int action, int mods) {
    if (action == GLFW.GLFW_PRESS) {
      System.out.println("Mouse button " + button + " pressed");
    } else if (action == GLFW.GLFW_RELEASE) {
      System.out.println("Mouse button " + button + " released");
    }

    scene.onMouseEvent(window, button, action, mods);
  }

  public void run() {
    while (!GLFW.glfwWindowShouldClose(window)) {
      GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

      curr_time = GLFW.glfwGetTime();
      double elapsed_time = curr_time - last_time;
      scene.display(elapsed_time);
      scene.update(elapsed_time);
      last_time = curr_time;

      GLFW.glfwSwapBuffers(window);
      GLFW.glfwPollEvents();
    }

    GLFW.glfwDestroyWindow(window);
    GLFW.glfwTerminate();
  }

  public void setScene(IScene scene) {
    if (this.scene != null)
      this.scene.clean();
    this.scene = scene;
  }

  public float getAspectRatio() { return (float)width / (float)height; }
}
