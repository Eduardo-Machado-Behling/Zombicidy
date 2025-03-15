package com.zombicidy.frontend.scenes;

import com.zombicidy.frontend.AssetManager;
import com.zombicidy.frontend.WavefrontLoader;
import com.zombicidy.frontend.Window;
import com.zombicidy.frontend.engine.Camera;
import com.zombicidy.frontend.engine.Engine;
import com.zombicidy.frontend.engine.components.Color;
import com.zombicidy.frontend.engine.components.Material;
import com.zombicidy.frontend.engine.components.Texture;
import com.zombicidy.frontend.engine.components.Transform;
import com.zombicidy.frontend.engine.lights.DirectionalLight;
import com.zombicidy.frontend.engine.lights.PointLight;
import com.zombicidy.frontend.engine.math.Vector2D;
import com.zombicidy.frontend.engine.math.Vector3D;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL30;

public class Menu implements IScene {
  private final Engine.GameObject[] go = new Engine.GameObject[50];
  private Engine.GameObject light;
  private Engine.GameObject player;

  private final Camera camera;
  private Vector3D cameraVel = new Vector3D();
  private final Vector2D cameraRot = new Vector2D(90, 0);

  private float fov = 60;
  private final float lastX = Window.get().width() / 2;
  private final float lastY = Window.get().width() / 2;
  private final double[] lastXS = new double[1];
  private final double[] lastYS = new double[1];
  private boolean rool = false;

  public Menu() {
    camera = Camera.Perspective(fov, Window.get().getAspectRatio(), 1.0f, 10.0f)
                 .lookAt(new Vector3D(2, -3, -3.5f),
                         new Vector3D(-0.27f, 0.82f, 0.51f).normalize(),
                         new Vector3D(0, -1, 0).normalize());
  }

  public void init() {
    // Engine.get().setCamera(new Camera());

    player = Engine.get().makeGameObject(
        AssetManager.get().getWavefront("cube_menu").mesh,
        new Transform(new Vector3D(1, -0.355f, -1), new Vector3D(0.1f),
                      new Vector3D(0)));

    player.addComponent(
        "material",
        new Material((AssetManager.get().getWavefront("cube_menu").materials)));

    createBoard(new Vector3D(1, 0, -1), new Vector3D(0.25f));

    Engine.get().setCamera(camera);
  }

  @Override
  public void update(double elapsed_time) {
    // TODO Auto-generated method stub
  }

  @Override
  public void display(double elapsed_time) {
    // Make sure depth testing is enabled
    GL30.glEnable(GL30.GL_DEPTH_TEST);
    GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

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

      System.out.println("cameraPos = " + camera.getPosition());
      System.out.println("worldRay = " + worldRay);
      for (int i = 0; i < 4; i++) {
        Transform t = (Transform)go[i].getComponent().get("transform");
        Vector3D min =
            new Vector3D(-1).mult(t.getScale().x).add(t.getTranslation());
        Vector3D max =
            new Vector3D(1).mult(t.getScale().x).add(t.getTranslation());

        boolean hit = Engine.get().rayIntersectsAABB(camera.getPosition(),
                                                     worldRay, min, max);
        if (hit) {
          pressedCell(i);
          System.out.println("ray::return: " + hit + " on cube #" + i);
        }
      }
    }
  }

  private void pressedCell(int i) {
    switch (i) {
    case 0:
      Window.get().setScene(new Play(true));
      break;
    case 2:
      Window.get().terminate();
      break;
    case 3:
      Window.get().setScene(new Play(false));
      break;
    default:
      break;
    }
  }

  @Override
  public void clean() {
    Engine.get().clear();
  }

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

  private void createBoard(Vector3D pos, Vector3D scale) {
    WavefrontLoader.WavefrontData data =
        AssetManager.get().getWavefront("cube_menu");

    float delta = 2 * scale.x;
    pos.x -= delta;
    int i = 0;
    for (; i < 3; i++) {
      go[i] = Engine.get().makeGameObject(data.mesh);
      go[i].addComponent("texture",
                         new Texture(AssetManager.get().getTexture("sample"),
                                     new Texture.TextureParam()));
      go[i].addComponent("transform", new Transform(new Vector3D(pos), scale,
                                                    new Vector3D(0)));
      go[i].addComponent("color",
                         new Color(new Vector3D(1.0f, 0.2f, 0.5f), 1.0f));
      go[i].addComponent("material", new Material(data.materials));
      pos.x += delta;
    }

    pos.x -= delta * 2;
    pos.z += delta;
    go[i] = Engine.get().makeGameObject(data.mesh);
    go[i].addComponent("texture",
                       new Texture(AssetManager.get().getTexture("grass"),
                                   new Texture.TextureParam()));
    go[i].addComponent(
        "transform", new Transform(new Vector3D(pos), scale, new Vector3D(0)));
    go[i].addComponent("color",
                       new Color(new Vector3D(1.0f, 0.2f, 0.5f), 0.0f));
    go[i].addComponent("material", new Material(data.materials));

    Vector3D lightPos = new Vector3D(pos.x, pos.y - 1.5f, pos.z - delta);
    light = Engine.get().makeGameObject(data.mesh);
    light.addComponent("color",
                       new Color(new Vector3D(1.0f, 0.2f, 0.5f), 1.0f));
    ((Transform)light.getComponent().get("transform")).setTranslation(lightPos);
    ((Transform)light.getComponent().get("transform"))
        .setScale(scale.divide(4));

    Engine.get().addLight(new PointLight(lightPos, new Vector3D(0.3f),
                                         new Vector3D(1.0f), new Vector3D(1.0f),
                                         1.0f, 0.09f, 0.032f));
    Engine.get().addLight(
        new DirectionalLight(new Vector3D(0, 1, 0), new Vector3D(0.3f),
                             new Vector3D(1.0f), new Vector3D(1.0f)));
  }
}
