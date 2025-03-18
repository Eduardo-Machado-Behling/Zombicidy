package com.zombicidy.frontend.scenes;

import com.zombicidy.backend.board.baseclasses.Grid;
import com.zombicidy.frontend.AnimationManager;
import com.zombicidy.frontend.AssetManager;
import com.zombicidy.frontend.WavefrontLoader;
import com.zombicidy.frontend.WavefrontLoader.WavefrontData;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL40;

public class Play extends Scene {
  private boolean debug;
  private final ArrayList<Engine.GameObject> go = new ArrayList<>();
  private final HashMap<Grid, Engine.GameObject> entities = new HashMap<>();
  private int begin = 0;

  private final Camera camera;
  private Vector3D cameraVel = new Vector3D();
  private final Vector2D cameraRot = new Vector2D(0, 0);

  private Engine.GameObject player;
  private Vector2D playerPos;

  private float fov = 60;
  private final float lastX = Window.get().width() / 2;
  private final float lastY = Window.get().width() / 2;
  private final double[] lastXS = new double[1];
  private final double[] lastYS = new double[1];
  private boolean rool = false;
  private PointLight playerLight;

  private Grid board[][];
  private float scale = 1 / 4.0f;
  private Vector3D cameraTarget;

  public Play(boolean debug) {
    this.debug = debug;
    this.camera =
        Camera.Perspective(fov, Window.get().getAspectRatio(), 0.1f, 20)
            .lookAt(new Vector3D(-4.5f, 3.6f, 3.5f),
                    new Vector3D(0.64f, -0.53f, -0.55f), new Vector3D(0, 1, 0));
    cameraTarget = new Vector3D();
  }

  @Override
  public void init() {
    Engine.get().enableTracking();

    player = createPlayer();
    Engine.get().setCamera(camera);
    generateBoard(new Vector3D(0), scale);

    playerLight = new PointLight(new Vector3D(0.4f), new Vector3D(0.5f),
                                 new Vector3D(0.6f), new Vector3D(0, 1, 0),
                                 1.0f, 0.35f, 0.44f);
    Engine.get().addLight(playerLight);

    if (!debug) {
      Engine.get().addLight(
          new DirectionalLight(new Vector3D(0, -1, 0), new Vector3D(0.5f),
                               new Vector3D(0.6f), new Vector3D(0.6f)));
    } else {
      Engine.get().addLight(
          new DirectionalLight(new Vector3D(0, -1, 0), new Vector3D(0.4f),
                               new Vector3D(0.4f), new Vector3D(0.5f)));
    }

    board = new Grid[10][10];
    populateGrid();
  }

  private void populateGrid() {
    int[] position = new int[2];
    for (int x = 0; x < 10; x++) {
      for (int y = 0; y < 10; y++) {
        position[0] = x;
        position[1] = y;
        board[x][y] = Window.get().getEventListener().getGrid(position);

        switch (board[x][y].GetType()) {
        case "Player":
          playerPos = new Vector2D(x, y);
          Vector3D worldPos = boardToWorld(playerPos);
          ((Transform)player.getComponent().get("transform"))
              .setTranslation(new Vector3D(worldPos));
          worldPos.y += 2.0f;
          playerLight.setPosition(worldPos);
          break;

        case "Wall":
        case "CommomZombie":
        case "Chest":
        case "RunnerZombie":
        case "CrawlerZombie":
        case "GiantZombie":
          createCubeAt(new Vector2D(x, y), board[x][y]);
          break;

        default:
          break;
        }
      }
    }

    updateVisibility(playerPos, 2);
  }

  private Engine.GameObject makeWall(Vector3D pos, Vector3D scale) {
    WavefrontLoader.WavefrontData wall =
        AssetManager.get().getWavefront("wall");

    Engine.GameObject go = Engine.get().makeGameObject(wall.mesh);
    go.addComponent("transform", new Transform(new Vector3D(pos), scale,
                                               new Vector3D(90, 0, 90)));
    go.addComponent("texture",
                    new Texture(AssetManager.get().getTexture("wall"),
                                new Texture.TextureParam()));
    go.addComponent(
        "color",
        new Color(new Vector3D(20.f / 255, 16.f / 255, 16.f / 255), 0.2f));
    go.addComponent("material", new Material(wall.materials));

    return go;
  }

  private void createCubeAt(Vector2D board, Grid grid) {
    WavefrontLoader.WavefrontData data =
        AssetManager.get().getWavefront("cube_menu");

    Vector3D boardToWorld = boardToWorld(board);

    String string = grid.GetType();
    System.out.println(grid.hashCode());
    Vector3D color = new Vector3D();
    Vector3D rot = new Vector3D();
    Vector3D scale = new Vector3D(0.1f);
    float blendFactor = 1.0f;

    GameObject cube = null;

    switch (string) {
    case "Wall":
      boardToWorld.y += 0.2f;
      cube = makeWall(boardToWorld, new Vector3D(0.25f));
      break;

    case "Chest":
      data = AssetManager.get().getWavefront("chest");
      boardToWorld.y += 0.2;
      boardToWorld.x -= 0.15;
      boardToWorld.z += 0.1;
      rot = new Vector3D(-90, 0, -90);
      scale = new Vector3D(0.08f);
      blendFactor = 0.2f;
      cube = Engine.get().makeGameObject(
          data.mesh, new Transform(new Vector3D(boardToWorld),
                                   new Vector3D(scale), new Vector3D(rot)));
      cube.addComponent("material", new Material(data.materials));
      cube.setVisible(debug);
      color = new Vector3D(1.0f, 1.0f, 0f);
      cube.addComponent("color", new Color(color, blendFactor));
      cube.addComponent("texture",
                        new Texture(AssetManager.get().getTexture("wood"),
                                    new Texture.TextureParam()));
      break;

    case "CommomZombie":
      color = new Vector3D(0.0f, 1.0f, 0f);
      break;

    case "RunnerZombie":
      color = new Vector3D(0.0f, 0.0f, 1.0f);
      break;

    case "CrawlerZombie":
      color = new Vector3D(0.0f, 0.3f, 0.0f);

    case "GiantZombie":
      color = new Vector3D(1.0f, 0.3f, 0.4f);
      break;
    default:
      throw new AssertionError();
    }

    if (cube == null) {
      cube = Engine.get().makeGameObject(
          data.mesh, new Transform(new Vector3D(boardToWorld),
                                   new Vector3D(scale), new Vector3D(rot)));
      cube.addComponent("material", new Material(data.materials));
      cube.setVisible(debug);
      cube.addComponent("color", new Color(color, blendFactor));
    }
    entities.put(grid, cube);
  }

  private GameObject createPlayer() {
    WavefrontLoader.WavefrontData data =
        AssetManager.get().getWavefront("player");

    GameObject player = Engine.get().makeGameObject(
        data.mesh, new Transform(new Vector3D(), new Vector3D(0.2f),
                                 new Vector3D(90, 0, 0)));

    player.addComponent("material", new Material(data.materials));
    player.addComponent("color",
                        new Color(new Vector3D(1.0f, 0.0f, 1.0f), 1.0f));

    return player;
  }

  @Override
  public void update(double elapsed_time) {
    AnimationManager.get().update((float)elapsed_time);
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

  int indexCameraMirror = -1;
  @Override
  public void onKeyEvent(long window, int key, int scancode, int action,
                         int mods) {

    cameraVel = new Vector3D(0);
    Vector3D front = camera.getFront();
    Vector3D up = camera.getUp();
    Vector3D right = camera.getFront().cross(up).normalize();
    final Vector3D[] mirror =
        new Vector3D[] {new Vector3D(1, 1, 1), new Vector3D(-1, 1, -1),
                        new Vector3D(-1, 1, 0), new Vector3D(1, 1, -1)};

    switch (key) {
    case GLFW.GLFW_KEY_UP:
      if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
        cameraVel = cameraVel.add(front);
      break;

    case GLFW.GLFW_KEY_DOWN:
      if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
        cameraVel = cameraVel.sub(front);
      break;

    case GLFW.GLFW_KEY_LEFT:
      if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
        cameraVel = cameraVel.sub(right);
      break;

    case GLFW.GLFW_KEY_RIGHT:
      if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
        cameraVel = cameraVel.add(right);
      break;

    case GLFW.GLFW_KEY_E:
      if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {

        indexCameraMirror = (indexCameraMirror + 1) % mirror.length;
        camera.moveLooking(camera.getPosition().mult(mirror[indexCameraMirror]),
                           cameraTarget);
      }
      break;

    case GLFW.GLFW_KEY_Q:
      if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
        indexCameraMirror = indexCameraMirror - 1;
        if (indexCameraMirror < 0) {
          indexCameraMirror = mirror.length - 1;
        }
        camera.moveLooking(camera.getPosition().mult(mirror[indexCameraMirror]),
                           cameraTarget);
      }
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

      int data = Engine.get().getUUID((int)vec.x, (int)vec.y);
      if (data != 0)
        moveTo(data);
    }
  }

  private void moveTo(int data) {
    data -= begin;
    int x = 9 - (data % 10);
    int y = 9 - (int)(data / 10);

    System.out.println("Trying to move to " + x + ", " + y);
    int[] pos = new int[] {y, x};
    Window.get().getEventListener().MovePlayer(pos);
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

  private Engine.GameObject createGround(Vector3D pos, float scale,
                                         Vector3D color) {
    WavefrontData data = AssetManager.get().getWavefront("cube_menu");
    Engine.GameObject go = Engine.get().makeGameObject(
        data.mesh, new Transform(pos, new Vector3D(scale), new Vector3D()));

    go.addComponent("material", new Material(data.materials));
    go.addComponent("color", new Color(color, 0.2f));

    UUID uuid = new UUID();
    go.addComponent("UUID", uuid);
    if (begin == 0) {
      begin = uuid.getId();
    }

    return go;
  }

  private Vector3D boardToWorld(Vector2D board) {
    final float xi = -scale * 10;

    float x = xi + scale * 2 * (9 - board.x);
    float z = xi + scale * 2 * (9 - board.y);
    return new Vector3D(x, scale, z);
  }

  private void generateBoard(Vector3D pos, float scale) {

    float delta = scale * 2;
    pos.x -= delta * 5;
    for (int i = 0; i < 10; i++) {
      pos.z = -delta * 5;
      for (int j = 0; j < 10; j++) {
        go.add(createGround(pos, scale,
                            new Vector3D((j + i) % 2 == 1 ? 1 : 0.3f)));
        pos.z += delta;
      }
      pos.x += delta;
    }
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
    switch (grid.GetType()) {
    case "Player":
      playerPos.x = position[0];
      playerPos.y = position[1];
      Vector3D worldPos = boardToWorld(playerPos);

      Transform orig = ((Transform)player.getComponent().get("transform"));
      Vector3D targetRot = new Vector3D(orig.getRotation());

      Vector3D delta = worldPos.sub(orig.getTranslation());
      if (delta.x > 0) {
        targetRot.z = 90;
      } else if (delta.x < 0) {
        targetRot.z = -90;
      }

      if (delta.z < 0) {
        targetRot.z = 0;
      } else if (delta.z > 0) {
        targetRot.z = 180;
      }

      animate(orig,
              new Transform(new Vector3D(worldPos),
                            new Vector3D(orig.getScale()),
                            new Vector3D(targetRot)),
              0.4f);

      worldPos.y += 1.0f;
      playerLight.setPosition(worldPos);
      updateVisibility(playerPos, 2);
      break;
    case "Ground":
      break;
    default:
      Vector2D pos = new Vector2D(position[0], position[1]);
      Transform t =
          (Transform)entities.get(grid).getComponent().get("transform");
      t.setTranslation(boardToWorld(pos));
      break;
    }

    board[position[0]][position[1]] = grid;
  }

  private void updateVisibility(Vector2D pos, int maxDepth) {
    if (debug)
      return;

    updateVisibility(pos, new HashSet<Vector2D>(), 0, maxDepth);
  }

  private void updateVisibility(Vector2D pos, Set<Vector2D> trace, int depth,
                                int maxDepth) {
    if (pos.x < 0 || pos.x > 9 || pos.y < 0 || pos.y > 9 || depth > maxDepth)
      return;

    System.out.println(String.format("pos: " + pos));
    Grid g = board[(int)pos.x][(int)pos.y];
    if (!"Ground".equals(g.GetType()) && !"Player".equals(g.GetType())) {
      entities.get(g).setVisible(true);
    }

    if ("Wall".equals(g.GetType())) {
      return;
    }

    Vector2D left = new Vector2D(pos.x - 1, pos.y);
    Vector2D right = new Vector2D(pos.x + 1, pos.y);
    Vector2D up = new Vector2D(pos.x, pos.y - 1);
    Vector2D down = new Vector2D(pos.x, pos.y + 1);

    if (!trace.contains(left)) {
      trace.add(left);
      updateVisibility(left, trace, depth + 1, maxDepth);
    }

    if (!trace.contains(right)) {
      trace.add(right);
      updateVisibility(right, trace, depth + 1, maxDepth);
    }

    if (!trace.contains(up)) {
      trace.add(up);
      updateVisibility(up, trace, depth + 1, maxDepth);
    }

    if (!trace.contains(down)) {
      trace.add(down);
      updateVisibility(down, trace, depth + 1, maxDepth);
    }
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
              System.out.println("pos: " + newPos);
              newPos =
                  newPos.add(target.getTranslation().sub(newPos).mult(per));
              System.out.println("__pos: " + newPos);
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
}