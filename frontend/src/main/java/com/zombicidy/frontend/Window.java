package com.zombicidy.frontend;

import static org.lwjgl.system.MemoryStack.stackPush;

import com.zombicidy.backend.EventListener;
import com.zombicidy.backend.board.baseclasses.Grid;
import com.zombicidy.backend.board.characters.CommomZombie;
import com.zombicidy.backend.board.combat.Combat;
import com.zombicidy.backend.frontend.FrontendAPI;
import com.zombicidy.frontend.scenes.Scene;
import java.nio.IntBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;
import org.lwjgl.system.MemoryStack;

public class Window implements FrontendAPI {
  static private Window rendererInstance = null;
  private EventListener eventListener;

  private long window;
  private Scene scene = null;

  private int width = 800;
  private int height = 600;

  private double last_time = GLFW.glfwGetTime();
  private double curr_time;

  private Window() {

    if (!GLFW.glfwInit()) {
      throw new IllegalStateException("Unable to initialize GLFW");
    }
    long primaryMonitor = GLFW.glfwGetPrimaryMonitor();
    GLFWVidMode vidmode = GLFW.glfwGetVideoMode(primaryMonitor);
    if (vidmode == null) {
      throw new RuntimeException("Failed to get video mode for monitor");
    }

    if (width == 0 && height == 0) {
      width = vidmode.width();
      height = vidmode.height();

      // Create a windowed mode window
      GLFW.glfwDefaultWindowHints();
      GLFW.glfwWindowHint(GLFW.GLFW_DECORATED,
                          GLFW.GLFW_FALSE); // Remove title bar and borders
      GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE,
                          GLFW.GLFW_FALSE); // Disable resizing
      GLFW.glfwWindowHint(GLFW.GLFW_FOCUSED,
                          GLFW.GLFW_TRUE); // Ensure window gets focus
      GLFW.glfwWindowHint(GLFW.GLFW_AUTO_ICONIFY,
                          GLFW.GLFW_FALSE); // Prevent minimizing on focus loss
      window = GLFW.glfwCreateWindow(width, height, "Zombicidy", 0, 0);
      if (window == 0) {
        throw new RuntimeException("Failed to create GLFW window");
      }
      try (MemoryStack stack = stackPush()) {
        IntBuffer xpos = stack.mallocInt(1);
        IntBuffer ypos = stack.mallocInt(1);
        GLFW.glfwGetMonitorPos(primaryMonitor, xpos, ypos);
        GLFW.glfwSetWindowPos(window, xpos.get(0), ypos.get(0));
      }
    }
    window = GLFW.glfwCreateWindow(width, height, "Zombicidy", 0, 0);

    // Center the window manually

    GLFW.glfwMakeContextCurrent(window);
    GL.createCapabilities();  // Initialize OpenGL capabilities
    GLFW.glfwSwapInterval(1); // Enable V-Sync
    GLFW.glfwShowWindow(window);

    GL43.glDebugMessageCallback(
        (source, type, id, severity, length, message, userParam)
            -> {
          System.err.println("GL CALLBACK: " +
                             org.lwjgl.opengl.GLDebugMessageCallback.getMessage(
                                 length, message));
        },
        0);
    GL43.glEnable(GL43.GL_DEBUG_OUTPUT);
    System.out.println("Renderer: " + GL40.glGetString(GL40.GL_RENDERER));
    System.out.println("Vendor: " + GL40.glGetString(GL40.GL_VENDOR));
    System.out.println("OpenGL Version: " + GL40.glGetString(GL40.GL_VERSION));
    System.out.println("GLSL Version: " +
                       GL40.glGetString(GL40.GL_SHADING_LANGUAGE_VERSION));

    GLFW.glfwSetKeyCallback(window, this::onKeyEvent);
    GLFW.glfwSetMouseButtonCallback(window, this::onMouseEvent);
    GLFW.glfwSetCursorPosCallback(window, this::onMouseMove);
    GLFW.glfwSetScrollCallback(window, this::onMouseScroll);
    GLFW.glfwSetFramebufferSizeCallback(window, this::onResize);
  }

  static public Window get() {
    if (rendererInstance == null) {
      rendererInstance = new Window();
    }

    return rendererInstance;
  }

  private void onMouseScroll(long window, double xoffset, double yoffset) {
    scene.onMouseScroll(window, xoffset, yoffset);
  }

  private void onResize(long window, int width, int height) {
    this.width = width;
    this.height = height;

    GL40.glViewport(0, 0, width, height);

    scene.onResize(window, width, height);
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
      GL40.glClear(GL40.GL_COLOR_BUFFER_BIT | GL40.GL_DEPTH_BUFFER_BIT);

      curr_time = GLFW.glfwGetTime();
      double elapsed_time = curr_time - last_time;
      GLFW.glfwSetWindowTitle(
          window, String.format("Zombicidy (%.0f FPS)", 1 / elapsed_time));
      scene.display(elapsed_time);
      scene.update(elapsed_time);
      last_time = curr_time;

      GLFW.glfwSwapBuffers(window);
      GLFW.glfwPollEvents();
    }

    GLFW.glfwDestroyWindow(window);
    GLFW.glfwTerminate();
  }

  public void setScene(Scene scene) {
    if (this.scene != null)
      this.scene.clean();
    this.scene = scene;
    this.scene.init();
  }

  public float getAspectRatio() { return (float)width / (float)height; }

  private void onMouseMove(long window, double xpos, double ypos) {
    scene.onMouseMove(window, xpos, ypos);
  }

  public int height() { return height; }

  public int width() { return width; }

  public void terminate() { GLFW.glfwSetWindowShouldClose(window, true); }

  @Override
  public void FinishCombat() {
    if (this.scene != null) {
      this.scene.FinishCombat();
    }
  }

  @Override
  public void Combat(Combat combat) {
    if (this.scene != null) {
      this.scene.Combat(combat);
    }
  }

  @Override
  public void ZombieKilled(CommomZombie zombie) {
    if (this.scene != null) {
      this.scene.ZombieKilled(zombie);
    }
  }

  @Override
  public void UseBandage(boolean actionWasMade) {
    if (this.scene != null) {
      this.scene.UseBandage(actionWasMade);
    }
  }

  @Override
  public void SurpriseEncounter() {
    // TODO Auto-generated method stub
    if (this.scene != null) {
      this.scene.SurpriseEncounter();
    }
  }

  @Override
  public void PlayerDealtDamage(int damage) {
    if (this.scene != null) {
      this.scene.PlayerDealtDamage(damage);
    }
  }

  @Override
  public void PlayerTookDamage(int damage) {
    if (this.scene != null) {
      this.scene.PlayerTookDamage(damage);
    }
  }

  @Override
  public void Redraw(int[] position, Grid grid) {
    if (this.scene != null) {
      this.scene.Redraw(position, grid);
    }
  }

  @Override
  public void GainedItem(String item) {
    if (this.scene != null) {
      this.scene.GainedItem(item);
    }
  }

  @Override
  public void GameWin() {
    if (this.scene != null) {
      this.scene.GameWin();
    }
  }

  @Override
  public void GameLose() {
    if (this.scene != null) {
      this.scene.GameLose();
    }
  }

  @Override
  public void PlayerGunNoAmmo() {
    if (this.scene != null) {
      this.scene.PlayerGunNoAmmo();
    }
  }

  @Override
  public void PlayerNoGun() {
    if (this.scene != null) {
      this.scene.PlayerNoGun();
    }
  }

  public EventListener getEventListener() { return eventListener; }

  public void setEventListener(EventListener eventListener) {
    this.eventListener = eventListener;
  }

  public void setScene(Scene scene, boolean b) {
    if (this.scene != null)
      this.scene.clean();
    this.scene = scene;
    if (b)
      this.scene.init();
  }
}
