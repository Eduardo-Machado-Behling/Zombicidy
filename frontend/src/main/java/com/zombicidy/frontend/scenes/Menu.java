package com.zombicidy.frontend.scenes;

import com.zombicidy.backend.board.baseclasses.Grid;
import com.zombicidy.frontend.AnimationManager;
import com.zombicidy.frontend.AssetManager;
import com.zombicidy.frontend.WavefrontLoader;
import com.zombicidy.frontend.Window;
import com.zombicidy.frontend.engine.Camera;
import com.zombicidy.frontend.engine.Engine;
import com.zombicidy.frontend.engine.Engine.GameObject;
import com.zombicidy.frontend.engine.components.Color;
import com.zombicidy.frontend.engine.components.Material;
import com.zombicidy.frontend.engine.components.Texture;
import com.zombicidy.frontend.engine.components.Transform;
import com.zombicidy.frontend.engine.components.UUID;
import com.zombicidy.frontend.engine.lights.DirectionalLight;
import com.zombicidy.frontend.engine.lights.PointLight;
import com.zombicidy.frontend.engine.math.Vector2D;
import com.zombicidy.frontend.engine.math.Vector3D;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL40;


public class Menu extends Scene {
  private final Engine.GameObject[] go = new Engine.GameObject[50];
  private Engine.GameObject light;
  private Engine.GameObject player;

  private final Camera camera;
  private Vector3D cameraVel = new Vector3D();
  private final Vector2D cameraRot = new Vector2D(90, 0);

  private float fov = 60;
  private final float scale = 0.25f;
  private int begin = 0;
  private boolean debug = false;
  private final float lastX = Window.get().width() / 2;
  private final float lastY = Window.get().width() / 2;
  private final double[] lastXS = new double[1];
  private final double[] lastYS = new double[1];
  private boolean rool = false;

  public Menu() {
    camera = Camera.Perspective(fov, Window.get().getAspectRatio(), 1.0f, 10.0f)
                 .lookAt(new Vector3D(0.48f, -2.43f, -3.70f),
                         new Vector3D(0.18f, 0.46f, 0.87f).normalize(),
                         new Vector3D(0, -1, 0).normalize());
  }

  public void init() {
    // Engine.get().setCamera(new Camera());

    Engine.get().enableTracking();

    player = Engine.get().makeGameObject(
        AssetManager.get().getWavefront("player").mesh,
        new Transform(new Vector3D(1, -0.355f, -1), new Vector3D(0.2f),
                      new Vector3D(-90, 0, 0)));

    player.addComponent(
        "material",
        new Material((AssetManager.get().getWavefront("player").materials)));
    player.addComponent("Color", new Color(new Vector3D(0.5f), 1.0f));

    createBoard(new Vector3D(1, 0, -1), new Vector3D(scale));

    Engine.get().setCamera(camera);
    GL40.glClearColor(8.0f / 255.0f, 23.0f / 255.0f, 48.0f / 255.0f, 1.0f);
  }

  @Override
  public void update(double elapsed_time) {
    AnimationManager.get().update((float)elapsed_time);
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

      int uuid = Engine.get().getUUID((int)vec.x, (int)vec.y);
      if (uuid > 0 && uuid - 1 < go.length)
        pressedCell(uuid - 1);
    }
  }

  private void animate(Transform dest, Transform target, float dur) {
    Transform orig = new Transform(new Vector3D(dest.getTranslation()),
                                   new Vector3D(dest.getScale()),
                                   new Vector3D(dest.getRotation()));
    AnimationManager
        .get()

        .makeSequence()
        .add(
            (float time, float duration)
                -> {
              float per = time / duration;
              float qper = -((2 * per - 1) * (2 * per - 1)) + 2;

              Vector3D newPos = new Vector3D(orig.getTranslation());
              newPos =
                  newPos.add(target.getTranslation().sub(newPos).mult(per));
              newPos.y *= qper;

              Vector3D newRot = new Vector3D(orig.getRotation());
              newRot = newRot.add(target.getRotation().sub(newRot).mult(per));

              Vector3D newScale = new Vector3D(orig.getScale());
              newScale =
                  newScale.add(target.getScale().sub(newScale).mult(per));

              dest.set(newPos, newScale, newRot);
            },
            dur)
        .start();
  }

  private void pressedCell(int i) {
    System.out.println("pressed #" + i);

    Transform tp = (Transform)player.getComponent().get("transform");
    Transform tg = (Transform)go[i].getComponent().get("transform");

    Vector3D pos = new Vector3D(tg.getTranslation());
    Vector2D delta = new Vector2D(tp.getTranslation().x - pos.x,
                                  tp.getTranslation().z - pos.z);

    float distance = delta.length();

    System.out.println("distance: " + distance);
    if (distance > 0.25)
      return;

    Transform t = (Transform)player.getComponent().get("transform");
    Vector3D origRot = new Vector3D(t.getRotation());
    Vector3D origPos = new Vector3D(t.getTranslation());
    Vector3D target = new Vector3D(origRot);
    if (delta.x < 0) {
      target.z = 90;
    } else if (delta.x > 0) {
      target.z = -90;
    }

    if (delta.y > 0) {
      target.x = 90;
    } else if (delta.y < 0) {
      target.x = -90;
    }

    pos.y -= scale * 1.5;

    animate(t, new Transform(pos, t.getScale(), target), 0.4f);
    // tp.setTranslation(pos);

    switch (i) {
    case 0:
      debug = !debug;
      break;
    case 4:
      Window.get().terminate();
      break;
    case 5:
      Window.get().getEventListener().run("easy");
      Window.get().setScene(new Play(debug));
      break;
    case 6:
      Window.get().getEventListener().run("medium");
      Window.get().setScene(new Play(debug));
      break;
    case 7:
      Window.get().getEventListener().run("hard");
      Window.get().setScene(new Play(debug));
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

  private Engine.GameObject makeWall(Vector3D pos, Vector3D scale) {
    WavefrontLoader.WavefrontData wall =
        AssetManager.get().getWavefront("wall");

    Engine.GameObject go = Engine.get().makeGameObject(wall.mesh);
    go.addComponent("transform", new Transform(new Vector3D(pos), scale,
                                               new Vector3D(90, 0, 0)));
    go.addComponent("texture",
                    new Texture(AssetManager.get().getTexture("wall"),
                                new Texture.TextureParam()));
    go.addComponent(
        "color",
        new Color(new Vector3D(20.f / 255, 16.f / 255, 16.f / 255), 0.2f));
    go.addComponent("material", new Material(wall.materials));

    return go;
  }

  private void createBoard(Vector3D pos, Vector3D scale) {
    WavefrontLoader.WavefrontData data =
        AssetManager.get().getWavefront("cube_menu");

    float delta = 2 * scale.x;
    pos.x -= delta * 2;
    int i = 0;
    for (; i < 5; i++) {
      go[i] = Engine.get().makeGameObject(data.mesh);
      go[i].addComponent("texture",
                         new Texture(AssetManager.get().getTexture("ground"),
                                     new Texture.TextureParam()));
      go[i].addComponent("transform", new Transform(new Vector3D(pos), scale,
                                                    new Vector3D(0)));
      go[i].addComponent("material", new Material(data.materials));
      go[i].addComponent("color", new Color(new Vector3D(0.3f), 0.6f));
      UUID uuid = new UUID();
      go[i].addComponent("UUID", uuid);
      if (begin == 0) {
        begin = uuid.getId();
      }
      pos.x += delta;
    }

    pos.z += delta;
    pos.x -= delta;
    for (int j = 0; j < 3; j++) {
      pos.x -= delta;
      go[i] = Engine.get().makeGameObject(data.mesh);
      go[i].addComponent("texture",
                         new Texture(AssetManager.get().getTexture("ground"),
                                     new Texture.TextureParam()));
      go[i].addComponent("transform", new Transform(new Vector3D(pos), scale,
                                                    new Vector3D(0)));
      go[i].addComponent("material", new Material(data.materials));
      go[i].addComponent("color", new Color(new Vector3D(0.3f), 0.6f));
      go[i++].addComponent("UUID", new UUID());
    }

    pos.y -= delta;
    pos.x = -delta;
    pos.z = -delta * 2;
    go[i++] = makeWall(pos, scale);
    pos.x += delta;
    pos.z += delta;
    go[i++] = makeWall(pos, scale);
    makeSign(pos, scale, "debug");
    pos.x += delta;
    pos.z += delta;
    go[i++] = makeWall(pos, scale);
    makeSign(pos, scale, "easy");

    String[] signs = new String[] {"medium", "hard"};
    for (int j = 0; j < 2; j++) {
      pos.x += delta;
      go[i++] = makeWall(pos, scale);
      makeSign(pos, scale, signs[j]);
    }
    pos.x += delta;
    pos.z -= delta;
    go[i++] = makeWall(pos, scale);
    makeSign(pos, scale, "exit");
    pos.x += delta;
    pos.z -= delta;
    go[i++] = makeWall(pos, scale);

    Vector3D lightPos = new Vector3D(pos.x, pos.y - 1.5f, pos.z - delta);
    light = Engine.get().makeGameObject(data.mesh);
    light.addComponent("color",
                       new Color(new Vector3D(1.0f, 0.2f, 0.5f), 1.0f));
    ((Transform)light.getComponent().get("transform")).setTranslation(lightPos);
    ((Transform)light.getComponent().get("transform"))
        .setScale(scale.divide(4));

    Engine.get().addLight(new PointLight(
        new Vector3D(0.7f), new Vector3D(0.7f), new Vector3D(0.7f),
        camera.getPosition(), 1.0f, 0.09f, 0.032f));
    Engine.get().addLight(
        new DirectionalLight(new Vector3D(0, 1, 0), new Vector3D(0.3f),
                             new Vector3D(1.0f), new Vector3D(1.0f)));
  }

  private void makeSign(Vector3D pos, Vector3D scale, String sign) {
    WavefrontLoader.WavefrontData model =
        AssetManager.get().getWavefront("sign");
    WavefrontLoader.WavefrontData text = AssetManager.get().getWavefront(sign);

    Vector3D position = new Vector3D(pos);
    position.z -= scale.z;
    position.y += 0.25;
    GameObject go = Engine.get().makeGameObject(
        model.mesh,
        new Transform(position, new Vector3D(0.15f), new Vector3D(90, 0, 0)));
    go.addComponent("material", new Material(model.materials));
    go.addComponent(
        "color",
        new Color(new Vector3D(161.f / 255, 102.f / 255, 47.f / 255), 1.0f));

    position = new Vector3D(pos);
    float scalef = 0.2f;
    if ("easy".equals(sign) || "hard".equals(sign) || "medium".equals(sign)) {
      position.y -= 0.17;
      position.z -= scale.z * 1.1;
    } else if ("exit".equals(sign)) {
      position.y -= 0.13;
      position.z -= scale.z * 1.1;
    } else {
      position.y -= 0.14;
      position.z -= scale.z * 1.1;
      scalef = 0.18f;
    }
    GameObject got = Engine.get().makeGameObject(
        text.mesh,
        new Transform(position, new Vector3D(scalef), new Vector3D(90, 0, 0)));
    got.addComponent("material", new Material(text.materials));
    got.addComponent("color", new Color(new Vector3D(), 1.0f));
  }

  @Override
  public void FinishCombat() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException(
        "Unimplemented method 'FinishCombat'");
  }

  @Override
  public void Combat(com.zombicidy.backend.board.combat.Combat combat) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'Combat'");
  }

  @Override
  public void ZombieKilled() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException(
        "Unimplemented method 'ZombieKilled'");
  }

  @Override
  public void UseBandage(boolean actionWasMade) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException(
        "Unimplemented method 'UseBandage'");
  }

  @Override
  public void SurpriseEncounter() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException(
        "Unimplemented method 'SurpriseEncounter'");
  }

  @Override
  public void PlayerDealtDamage(int damage) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException(
        "Unimplemented method 'PlayerDealtDamage'");
  }

  @Override
  public void PlayerTookDamage(int damage) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException(
        "Unimplemented method 'PlayerTookDamage'");
  }

  @Override
  public void Redraw(int[] position, Grid grid) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'Redraw'");
  }

  @Override
  public void GainedItem(String item) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException(
        "Unimplemented method 'GainedItem'");
  }

  @Override
  public void GameWin() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'GameWin'");
  }

  @Override
  public void GameLose() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'GameLose'");
  }

  @Override
  public void PlayerGunNoAmmo() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException(
        "Unimplemented method 'PlayerGunNoAmmo'");
  }

  @Override
  public void PlayerNoGun() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException(
        "Unimplemented method 'PlayerNoGun'");
  }
}
