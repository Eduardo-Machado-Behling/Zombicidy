package com.zombicidy.frontend.scenes;

import com.zombicidy.frontend.AssetManager;
import com.zombicidy.frontend.WavefrontLoader.WavefrontData;
import com.zombicidy.frontend.Window;
import com.zombicidy.frontend.engine.Camera;
import com.zombicidy.frontend.engine.Engine;
import com.zombicidy.frontend.engine.components.Color;
import com.zombicidy.frontend.engine.components.Material;
import com.zombicidy.frontend.engine.components.Transform;
import com.zombicidy.frontend.engine.lights.DirectionalLight;
import com.zombicidy.frontend.engine.lights.PointLight;
import com.zombicidy.frontend.engine.math.Vector2D;
import com.zombicidy.frontend.engine.math.Vector3D;
import java.util.ArrayList;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL40;


public class Play implements IScene {
  private boolean debug;
  private final ArrayList<Engine.GameObject> go = new ArrayList<>();

  private final Camera camera;
  private Vector3D cameraVel = new Vector3D();
  private final Vector2D cameraRot = new Vector2D(90, 0);

  private float fov = 60;
  private final float lastX = Window.get().width() / 2;
  private final float lastY = Window.get().width() / 2;
  private final double[] lastXS = new double[1];
  private final double[] lastYS = new double[1];
  private boolean rool = false;

  public Play(boolean debug) {
    this.debug = debug;
    this.camera =
        Camera.Perspective(fov, Window.get().getAspectRatio(), 0.1f, 10)
            .lookAt(new Vector3D(0, -3, 0), new Vector3D(0, 1, 0),
                    new Vector3D(0, -1, 0))
            .rotate(cameraRot.x, cameraRot.y);
  }

  @Override
  public void init() {
    Engine.get().setCamera(camera);
    generateBoard(new Vector3D(0), 1 / 4.0f);
    Engine.get().addLight(new PointLight(
        new Vector3D(0, -3, 0), new Vector3D(0.3f), new Vector3D(1.0f),
        new Vector3D(1.0f), 1.0f, 0.09f, 0.032f));
    Engine.get().addLight(
        new DirectionalLight(new Vector3D(0, 1, 0), new Vector3D(0.3f),
                             new Vector3D(1.0f), new Vector3D(1.0f)));
  }

  @Override
  public void update(double elapsed_time) {
    // TODO Auto-generated method stub
  }

  @Override
  public void display(double elapsed_time) {
    // Make sure depth testing is enabled
    GL40.glEnable(GL40.GL_DEPTH_TEST);
    GL40.glClear(GL40.GL_COLOR_BUFFER_BIT | GL40.GL_DEPTH_BUFFER_BIT);

    // rot.y += elapsed_time;
    // rot.z += 45 * elapsed_time;
    // rot.x += elapsed_time / 4;

    // Transform t = (Transform)go[0].getComponent().get("transform");
    // t.setRotation(rot);

    if (!cameraVel.all(0.0f)) {
      camera.move(cameraVel, (float)elapsed_time * 2.5f);
      System.out.println("Position: " + camera.getPosition());
    }

    Engine.get().render((float)elapsed_time);
  }

  @Override
  public void onKeyEvent(long window, int key, int scancode, int action,
                         int mods) {

    cameraVel = new Vector3D(0);
    Vector3D front = camera.getFront();
    Vector3D up = camera.getUp();
    Vector3D right = camera.getFront().cross(up).normalize();

    switch (key) {
    case GLFW.GLFW_KEY_W:
      if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
        cameraVel = cameraVel.add(front);
      break;

    case GLFW.GLFW_KEY_S:
      if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
        cameraVel = cameraVel.sub(front);
      break;

    case GLFW.GLFW_KEY_A:
      if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
        cameraVel = cameraVel.sub(right);
      break;

    case GLFW.GLFW_KEY_D:
      if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
        cameraVel = cameraVel.add(right);
      break;

    case GLFW.GLFW_KEY_LEFT_CONTROL:
      if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
        cameraVel = cameraVel.sub(up);
      break;

    case GLFW.GLFW_KEY_SPACE:
      if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
        cameraVel = cameraVel.add(up);
      break;

    case GLFW.GLFW_KEY_F3:
      if (action == GLFW.GLFW_PRESS) {
        System.out.println("camera.pos = " + camera.getPosition());
        System.out.println("camera.front = " + camera.getFront());
        System.out.println("camera.up = " + camera.getUp());
      }
      break;

    default:
      break;
    }

    cameraVel = cameraVel.normalize();
    System.out.println("cameraVel: " + cameraVel);
    System.out.println("-------------");
  }

  @Override
  public void onMouseEvent(long window, int button, int action, int mods) {
    if (button == 2) {
      rool = action == GLFW.GLFW_PRESS;
      if (rool) {
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR,
                              GLFW.GLFW_CURSOR_DISABLED);
        GLFW.glfwGetCursorPos(window, lastXS, lastYS);
        GLFW.glfwSetCursorPos(window, lastX, lastY);
      } else {
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR,
                              GLFW.GLFW_CURSOR_NORMAL);
        GLFW.glfwSetCursorPos(window, lastXS[0], lastYS[0]);
      }
    } else if (action == GLFW.GLFW_PRESS) {
      double[][] coords = new double[2][1];
      GLFW.glfwGetCursorPos(window, coords[0], coords[1]);
      Vector2D vec = new Vector2D((float)coords[0][0], (float)coords[1][0]);

      Vector3D worldRay = camera.mouseToWorld(vec);
      int i = getClickedCell(worldRay);
    }
  }

  @Override
  public void clean() {}

  @Override
  public void onResize(long window, int width, int height) {
    Engine.get().getCamera().setProjection(60, Window.get().getAspectRatio(),
                                           0.1f, 10f);
  }

  @Override
  public void onMouseMove(long window, double xpos, double ypos) {

    if (rool) {

      float xoffset = (float)xpos - lastX;
      float yoffset = lastY - (float)ypos;

      float sensitivity = 0.1f;
      xoffset *= sensitivity;
      yoffset *= sensitivity;

      cameraRot.x += xoffset;
      cameraRot.y += yoffset;

      if (cameraRot.y > 89.0f)
        cameraRot.y = 89.0f;
      if (cameraRot.y < -89.0f)
        cameraRot.y = -89.0f;

      camera.rotate(cameraRot.x, cameraRot.y);
      GLFW.glfwSetCursorPos(window, lastX, lastY);
    }
  }

  @Override
  public void onMouseScroll(long window, double xoffset, double yoffset) {
    fov -= (float)yoffset;
    if (fov < 1.0f)
      fov = 1.0f;
    if (fov > 120.0f)
      fov = 120.0f;

    camera.setProjection(fov, Window.get().getAspectRatio(), 0.1f, 10.0f);
  }

  private Engine.GameObject creteGround(Vector3D pos, float scale,
                                        Vector3D color) {
    WavefrontData data = AssetManager.get().getWavefront("cube_menu");
    Engine.GameObject go = Engine.get().makeGameObject(
        data.mesh, new Transform(pos, new Vector3D(scale), new Vector3D()));

    go.addComponent("material", new Material(data.materials));
    go.addComponent("color", new Color(color, 0.2f));

    return go;
  }

  private void generateBoard(Vector3D pos, float scale) {

    float delta = scale * 2;
    pos.x -= delta * 5;
    for (int i = 0; i < 10; i++) {
      pos.z = -delta * 5;
      for (int j = 0; j < 10; j++) {
        go.add(
            creteGround(pos, scale, new Vector3D((j + i) % 2 == 1 ? 1 : 0.3f)));
        pos.z += delta;
      }
      pos.x += delta;
    }
  }

  class Pair<K, V> {
    private final K key;
    private final V value;

    public Pair(K key, V value) {
      this.key = key;
      this.value = value;
    }

    public K getKey() { return key; }

    public V getValue() { return value; }
  }

  private int getClickedCell(Vector3D worldRay) {
    Pair<Integer, Float> closest = new Pair<>(-1, Float.POSITIVE_INFINITY);

    for (int idx = 0; idx < go.size(); idx++) {
      Engine.GameObject ground = go.get(idx);

      Transform t = (Transform)ground.getComponent().get("transform");
      Vector3D min =
          new Vector3D(-1).mult(t.getScale().x).add(t.getTranslation());
      Vector3D max =
          new Vector3D(1).mult(t.getScale().x).add(t.getTranslation());

      if (Engine.get().rayIntersectsAABB(camera.getPosition(), worldRay, min,
                                         max)) {
        float distance =
            camera.getPosition().sub(t.getTranslation()).magnitude();
        if (closest.value > distance) {
          closest = new Pair<>(idx, distance);
        }
      }
    }

    return closest.getKey();
  }
}