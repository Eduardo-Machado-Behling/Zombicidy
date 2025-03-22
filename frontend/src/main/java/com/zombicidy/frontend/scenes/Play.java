package com.zombicidy.frontend.scenes;

import com.zombicidy.backend.board.baseclasses.Grid;
import com.zombicidy.backend.board.characters.CommomZombie;
import com.zombicidy.backend.board.characters.Player;
import com.zombicidy.backend.board.items.Gun;
import com.zombicidy.frontend.AnimationManager;
import com.zombicidy.frontend.AssetManager;
import com.zombicidy.frontend.WavefrontLoader;
import com.zombicidy.frontend.WavefrontLoader.WavefrontData;
import com.zombicidy.frontend.Window;
import com.zombicidy.frontend.engine.Camera;
import com.zombicidy.frontend.engine.Engine;
import com.zombicidy.frontend.engine.Engine.GameObject;
import com.zombicidy.frontend.engine.Engine.UIObject;
import com.zombicidy.frontend.engine.components.Color;
import com.zombicidy.frontend.engine.components.Material;
import com.zombicidy.frontend.engine.components.Mesh2D;
import com.zombicidy.frontend.engine.components.Shader;
import com.zombicidy.frontend.engine.components.Texture;
import com.zombicidy.frontend.engine.components.Texture.TextureData;
import com.zombicidy.frontend.engine.components.Transform;
import com.zombicidy.frontend.engine.components.UUID;
import com.zombicidy.frontend.engine.components.Variable;
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
  private final ArrayList<Engine.GameObject> gos = new ArrayList<>();
  private final ArrayList<Engine.UIObject> uios = new ArrayList<>();

  private final HashMap<Grid, Engine.GameObject> entities = new HashMap<>();
  private int begin = 0;

  private boolean paused = false;
  private boolean hasGun = false;
  private final Camera camera;
  private Vector3D cameraPos = new Vector3D(-4.5f, 3.6f, 3.5f);
  private Vector3D cameraVel = new Vector3D();
  private final Vector2D cameraRot = new Vector2D(0, 0);

  private Engine.UIObject[] HPlabel = new Engine.UIObject[2];
  private Vector2D HPLabelSize = null;
  private Vector2D HPLabelPos;

  private Engine.UIObject[] bandagesUIOS = new Engine.UIObject[2];
  private Vector2D bandageLabelPos;
  private Vector2D bandageLabelSize = new Vector2D(16, 16);
  private Vector2D bandageHitboxPos;
  private Vector2D bandageHitboxSize;

  int indexCameraMirror = 0;
  private int playerMaxHealth;
  private Engine.GameObject player;
  private Vector2D playerPos;
  private Vector2D newPlayerPos = null;

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

  private Vector2D fine2D;
  private Vector3D fineTuning;
  private Engine.GameObject fine;

  private Engine.UIObject chamber;
  private ArrayList<Engine.UIObject> bullets = new ArrayList<>();
  private UIObject healthBar;

  private Vector2D pausePos;
  private float pauseScale = 0.6f;
  private boolean escapable;
  private UIObject pauseTitle = null;
  private UIObject pauseBtns = null;

  private UIObject chestBG = null;
  private UIObject currSprite = null;
  private UIObject[] chestSprites = new UIObject[4];

  public Play(boolean debug) {
    UUID.clear();
    this.debug = debug;
    this.camera = Camera.Perspective(fov, Window.get().getAspectRatio(), 0.1f, 20)
        .lookAt(new Vector3D(cameraPos),
            new Vector3D(0.64f, -0.53f, -0.55f), new Vector3D(0, 1, 0));
    cameraTarget = new Vector3D();
    player = createPlayer();
    gos.add(player);
    generateBoard(new Vector3D(0), scale);
    generateUI();
    generateChestAssets();
    playerLight = new PointLight(new Vector3D(0.6f), new Vector3D(0.6f),
        new Vector3D(0.5f), new Vector3D(0, 1, 0),
        0.5f, 0.35f, 0.44f);

    board = new Grid[10][10];
    populateGrid();
    playerMaxHealth = getPlayerHealth();
  }

  @Override
  public void init() {
    Engine.get().enableTracking();

    paused = false;
    escapable = false;
    if (currSprite != null) {
      currSprite.hide();
      currSprite = null;
    }
    if (chestBG != null)
      chestBG.hide();

    if (pauseBtns != null) {
      pauseBtns.hide();
    }
    if (pauseTitle != null) {
      pauseTitle.hide();
    }

    if (newPlayerPos == null) {
      newPlayerPos = new Vector2D(0, 0);
    } else {
      // movePlayer(new int[] {(int)newPlayerPos.y, (int)newPlayerPos.x});
    }
    Engine.get().setCamera(camera);
    Engine.get().addLight(playerLight);
    Engine.get().addLight(
        new DirectionalLight(new Vector3D(0, -2, 0), new Vector3D(0.1f),
            new Vector3D(0.1f), new Vector3D(0.2f)));
    for (Engine.GameObject go : gos)
      Engine.get().addGameObject(go);

    for (Engine.UIObject uio : uios)
      Engine.get().addUIObject(uio);

    refreshHPLabel();
    refreshAmmo();
    refreshBandage();
  }

  private Engine.UIObject getCell(Vector2D pos, Vector2D size, int i, float p) {
    ArrayList<Mesh2D.Vertex> vertices = new ArrayList<>();
    float ud = 0.05f;
    float vd = 0.1f;
    float bu = 0.125f * (i % 8) + 0.04f;
    float bv = 0.2f * (i / 8) + 0.05f;
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y, p, bu, bv));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y, p, bu + ud, bv));
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y + size.y, p, bu, bv + vd));
    vertices.add(
        new Mesh2D.Vertex(pos.x + size.y, pos.y + size.y, p, bu + ud, bv + vd));

    Mesh2D mesh = new Mesh2D(vertices);
    Texture texture = new Texture(AssetManager.get().getTexture("alph"),
        new Texture.TextureParam());
    Color color = new Color(new Vector3D(1), 1.0f);
    Shader shader = new Shader("ui");

    return Engine.get().makeUIObject(mesh, shader, texture, color);
  }

  private void getCell(Vector2D pos, Vector2D size, int i,
      Engine.UIObject[] uio, int j) {
    ArrayList<Mesh2D.Vertex> vertices = new ArrayList<>();
    float ud = 0.05f;
    float vd = 0.1f;
    float bu = 0.125f * (i % 8) + 0.04f;
    float bv = 0.2f * (i / 8) + 0.05f;
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y, 1, bu, bv));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y, 1, bu + ud, bv));
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y + size.y, 1, bu, bv + vd));
    vertices.add(
        new Mesh2D.Vertex(pos.x + size.y, pos.y + size.y, 1, bu + ud, bv + vd));
    Mesh2D mesh = new Mesh2D(vertices);
    Texture texture = new Texture(AssetManager.get().getTexture("alph"),
        new Texture.TextureParam());
    Color color = new Color(new Vector3D(1), 1.0f);
    Shader shader = new Shader("ui");

    uio[j] = Engine.get().makeUIObject(mesh, shader, texture, color);
  }

  private void renderText(Vector2D pos, Vector2D size, String str) {
    final float space = 1.05f;
    float p = -0.8f;
    for (int i = 0; i < str.length(); i++) {
      if (str.charAt(i) == ' ') {
        pos.x += size.x * space;
        continue;
      }
      str = str.toLowerCase();
      int j = str.charAt(i) - "a".charAt(0);

      if (j < 0) {
        switch (str.charAt(i)) {
          case '$':
            j = 36;
            break;
          case ':':
            p = -0.9f;
            pos.x -= size.x * 0.3f;
            j = 37;
            break;
          case '?':
            j = 38;
            break;
          case '!':
            j = 39;
            break;
        }
      }

      uios.add(getCell(pos, size, j, p));

      pos.x += size.x * space;
    }
  }

  void drawNum(Vector2D pos, Vector2D size, int num, Engine.UIObject[] uio,
      int j) {

    int[] stack = new int[8];
    int l = 0;

    while (num > 9) {
      stack[l++] = num % 10;
      num /= 10;
    }
    stack[l++] = num % 10;

    for (int i = l - 1; i >= 0; i--) {
      getCell(pos, size, stack[i] + 26, uio, j);
      uios.add(uio[j++]);
      pos.x += size.x * 0.9f;
    }
  }

  void updateNum(Vector2D pos, Vector2D size, int num, Engine.UIObject[] uio,
      int j) {

    int[] stack = new int[8];
    int l = 0;

    while (num > 9) {
      stack[l++] = num % 10;
      num /= 10;
    }
    stack[l++] = num % 10;

    ArrayList<Mesh2D.Vertex> vertices = new ArrayList<>();
    for (int i = l - 1; i >= 0; i--) {
      int idx = stack[i] + 26;
      float ud = 0.05f;
      float vd = 0.1f;
      float bu = 0.125f * (idx % 8) + 0.04f;
      float bv = 0.2f * (idx / 8) + 0.05f;
      vertices.add(new Mesh2D.Vertex(pos.x, pos.y, 1, bu, bv));
      vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y, 1, bu + ud, bv));
      vertices.add(new Mesh2D.Vertex(pos.x, pos.y + size.y, 1, bu, bv + vd));
      vertices.add(new Mesh2D.Vertex(pos.x + size.y, pos.y + size.y, 1, bu + ud,
          bv + vd));
      uio[j++].setMesh(new Mesh2D(vertices));
      vertices.clear();
    }
  }

  private void generateUI() {
    Vector2D size = new Vector2D(32, 32);
    Vector2D pos = new Vector2D(50, Window.get().height() - 150);

    drawHP(pos, size);

    float scale = 150;
    float ap = 206.f / 198.f;
    drawRevolver(
        new Vector2D(Window.get().width() - 100 - scale / 2, pos.y - scale / 2),
        scale, ap);

    bandageHitboxPos = new Vector2D(Window.get().width() / 2, pos.y);
    bandageHitboxSize = new Vector2D(75, 75);
    drawBandage(new Vector2D(bandageHitboxPos),
        new Vector2D(bandageHitboxSize));
  }

  private void drawBandage(Vector2D pos, Vector2D size) {
    ArrayList<Mesh2D.Vertex> vertices = new ArrayList<>();
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y, 0.0f, 0, 0));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y, 0.0f, 1, 0));
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y + size.y, 0.0f, 0, 1));
    vertices.add(new Mesh2D.Vertex(pos.x + size.y, pos.y + size.y, 0.0f, 1, 1));

    Mesh2D mesh = new Mesh2D(vertices);
    Texture texture = new Texture(AssetManager.get().getTexture("bandage"),
        new Texture.TextureParam());
    Color color = new Color(new Vector3D(0.05f), 0.0f);
    Shader shader = new Shader("ui");

    uios.add(Engine.get().makeUIObject(mesh, shader, texture, color));

    bandageLabelPos = new Vector2D(pos.x + size.x - 20, pos.y + size.y - 20);

    drawNum(new Vector2D(bandageLabelPos), bandageLabelSize, 0, bandagesUIOS,
        0);
  }

  private void refreshBandage() {
    updateNum(bandageLabelPos, bandageLabelSize,
        Window.get()
            .getEventListener()
            .getBoard()
            .getPlayer()
            .getBandageAmount(),
        bandagesUIOS, 0);
  }

  private void drawRevolver(Vector2D pos, float scale, float aspectRatio) {
    Vector2D size = new Vector2D(scale, scale * aspectRatio);

    ArrayList<Mesh2D.Vertex> vertices = new ArrayList<>();
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y, 0.0f, 0, 0));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y, 0.0f, 1, 0));
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y + size.y, 0.0f, 0, 1));
    vertices.add(new Mesh2D.Vertex(pos.x + size.y, pos.y + size.y, 0.0f, 1, 1));

    Mesh2D mesh = new Mesh2D(vertices);
    Texture texture = new Texture(AssetManager.get().getTexture("chamber"),
        new Texture.TextureParam());
    Color color = new Color(new Vector3D(0.05f), 1.0f);
    Shader shader = new Shader("ui");

    pos.x += size.x / 2;
    pos.y += size.y / 2;
    Vector2D delta = new Vector2D(0, -size.y / 2 + 30 * aspectRatio);
    for (int i = 0; i < 6; i++) {
      Engine.UIObject bullet = drawBullet(
          new Vector2D(pos.add(delta.rotate(i * 60))), new Vector2D(size));
      bullet.hide();
      bullets.add(bullet);
      uios.add(bullet);
    }

    chamber = Engine.get().makeUIObject(mesh, shader, texture, color);
    uios.add(chamber);
  }

  private UIObject drawBullet(Vector2D pos, Vector2D size) {
    float wr = size.x / 198;
    float hr = size.y / 206;

    int w = (int) (wr * 60);
    int h = (int) (hr * 60);

    pos.x -= w / 2;
    pos.y -= h / 2;

    ArrayList<Mesh2D.Vertex> vertices = new ArrayList<>();
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y, 1f, 0, 0));
    vertices.add(new Mesh2D.Vertex(pos.x + w, pos.y, 1f, 1, 0));
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y + h, 1f, 0, 1));
    vertices.add(new Mesh2D.Vertex(pos.x + h, pos.y + h, 1f, 1, 1));

    Mesh2D mesh = new Mesh2D(vertices);
    Texture texture = new Texture(AssetManager.get().getTexture("bullet"),
        new Texture.TextureParam());
    Color color = new Color(new Vector3D(0), 0.0f);
    Shader shader = new Shader("ui");

    return Engine.get().makeUIObject(mesh, shader, texture, color);
  }

  private void generateChestAssets() {

    ArrayList<Mesh2D.Vertex> vertices = new ArrayList<>();
    TextureData data = AssetManager.get().getTexture("baubg");
    Vector2D pos = new Vector2D(Window.get().width() / 2 - data.width / 2,
        Window.get().height() / 2 - data.height / 2);
    Vector2D size = new Vector2D(data.width, data.height);

    vertices.add(new Mesh2D.Vertex(pos.x, pos.y, -0.9f, 0, 0));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y, -0.9f, 1, 0));
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y + size.y, -0.9f, 0, 1));
    vertices.add(
        new Mesh2D.Vertex(pos.x + size.x, pos.y + size.y, -0.9f, 1, 1));
    Mesh2D mesh = new Mesh2D(vertices);
    vertices.clear();
    Texture texture = new Texture(data, new Texture.TextureParam());
    Color color = new Color(new Vector3D(0.05f), 0.0f);
    Shader shader = new Shader("ui");
    this.chestBG = Engine.get().makeUIObject(mesh, shader, texture, color);
    uios.add(chestBG);
    this.chestBG.hide();

    String[] names = new String[] { "bandage_bau", "gun_bau", "bat_bau", "bullet_bau" };
    for (int i = 0; i < chestSprites.length; i++) {
      vertices.add(new Mesh2D.Vertex(pos.x, pos.y, 1.9f, 0, 0));
      vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y, 1.9f, 1, 0));
      vertices.add(new Mesh2D.Vertex(pos.x, pos.y + size.y, 1.9f, 0, 1));
      vertices.add(
          new Mesh2D.Vertex(pos.x + size.x, pos.y + size.y, 1.9f, 1, 1));

      mesh = new Mesh2D(vertices);
      data = AssetManager.get().getTexture(names[i]);
      texture = new Texture(data, new Texture.TextureParam());
      color = new Color(new Vector3D(0.05f), 0.0f);
      shader = new Shader("ui");
      this.chestSprites[i] = Engine.get().makeUIObject(mesh, shader, texture, color);
      uios.add(this.chestSprites[i]);
      this.chestSprites[i].hide();
    }
  }

  private void drawHP(Vector2D pos, Vector2D size) {
    ArrayList<Mesh2D.Vertex> vertices = new ArrayList<>();
    TextureData data = AssetManager.get().getTexture("hpbg");
    Vector2D oldSize = size;
    bandageHitboxSize = new Vector2D(data.width, data.height);
    bandageHitboxPos = new Vector2D(pos);
    size = new Vector2D(bandageHitboxSize);

    vertices.add(new Mesh2D.Vertex(pos.x, pos.y, -0.9f, 0, 0));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y, -0.9f, 1, 0));
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y + size.y, -0.9f, 0, 1));
    vertices.add(
        new Mesh2D.Vertex(pos.x + size.y, pos.y + size.y, -0.9f, 1, 1));
    Mesh2D mesh = new Mesh2D(vertices);
    vertices.clear();
    Texture texture = new Texture(data, new Texture.TextureParam());
    Color color = new Color(new Vector3D(0.05f), 0.0f);
    Shader shader = new Shader("ui");
    uios.add(Engine.get().makeUIObject(mesh, shader, texture, color));

    Vector2D newPos = pos.add(new Vector2D(20, size.y - 70));
    data = AssetManager.get().getTexture("hp");
    size = new Vector2D(data.width - 20, data.height);
    vertices.add(new Mesh2D.Vertex(newPos.x, newPos.y, 0.9f, 0, 0));
    vertices.add(new Mesh2D.Vertex(newPos.x + size.x, newPos.y, 0.9f, 1, 0));
    vertices.add(new Mesh2D.Vertex(newPos.x, newPos.y + size.y, 0.9f, 0, 1));
    vertices.add(
        new Mesh2D.Vertex(newPos.x + size.y, newPos.y + size.y, 0.9f, 1, 1));
    mesh = new Mesh2D(vertices);
    texture = new Texture(data, new Texture.TextureParam());
    shader = new Shader("health");
    healthBar = Engine.get().makeUIObject(mesh, shader, texture,
        new Color(new Vector3D(1f), 0.0f));
    uios.add(healthBar);
    size = oldSize;
    healthBar.getComponent().put("variables", new Variable());
    ((Variable) healthBar.getComponent().get("variables"))
        .addVariable("progress", 1.0f);

    pos.x += 70;
    pos.y += 40;
    HPLabelPos = pos;
    HPLabelSize = new Vector2D(16, 16);
    drawNum(new Vector2D(pos), HPLabelSize, getPlayerHealth(), HPlabel, 0);
    for (int idx = 0; idx < HPlabel.length; idx++) {
      UIObject elem = HPlabel[idx];
      if (elem != null)
        elem.getColor().setColor(
            new Vector3D(107 / 255.f, 250 / 255.f, 202 / 255f));
    }
  }

  private void populateGrid() {
    entities.clear();
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
            ((Transform) player.getComponent().get("transform"))
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
    WavefrontLoader.WavefrontData wall = AssetManager.get().getWavefront("wall");

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
    WavefrontLoader.WavefrontData data = AssetManager.get().getWavefront("cube_menu");

    Vector3D boardToWorld = boardToWorld(board);

    String string = grid.GetType();
    Vector3D color = new Vector3D();
    Vector3D rot = new Vector3D();
    Vector3D scale = new Vector3D(0.2f);
    float blendFactor = 1.0f;

    GameObject cube = null;
    rot = new Vector3D(-90, 0, 90);

    switch (string) {
      case "Wall":
        boardToWorld.y += 0.2f;
        cube = makeWall(boardToWorld, new Vector3D(0.10f, 0.20f, 0.1f));
        break;

      case "Chest":
        data = AssetManager.get().getWavefront("chest");
        blendFactor = 0.2f;
        boardToWorld.y += 0.1;
        boardToWorld.x -= 0.11;
        boardToWorld.y += 0.04;
        cube = Engine.get().makeGameObject(
            data.mesh, new Transform(new Vector3D(boardToWorld),
                new Vector3D(scale), new Vector3D(rot)));
        cube.addComponent("material", new Material(data.materials));

        fine = cube;
        fine2D = new Vector2D(board.x, board.y);
        fineTuning = new Vector3D(boardToWorld);
        cube.setVisible(debug);
        color = new Vector3D(1.0f, 1.0f, 0f);
        cube.addComponent("color", new Color(color, blendFactor));
        cube.addComponent("texture",
            new Texture(AssetManager.get().getTexture("wood"),
                new Texture.TextureParam()));
        break;

      case "CommomZombie":
        data = AssetManager.get().getWavefront("zombie");
        cube = Engine.get().makeGameObject(
            data.mesh, new Transform(new Vector3D(boardToWorld),
                new Vector3D(0.2f), new Vector3D(90, 0, 0)));

        cube.addComponent("material", new Material(data.materials));
        cube.setVisible(debug);
        color = new Vector3D(0.0f, 1.0f, 0f);
        cube.addComponent("color", new Color(color, blendFactor));
        break;

      case "RunnerZombie":
        data = AssetManager.get().getWavefront("runner");
        cube = Engine.get().makeGameObject(
            data.mesh, new Transform(new Vector3D(boardToWorld),
                new Vector3D(scale), new Vector3D(rot)));
        cube.addComponent("material", new Material(data.materials));
        cube.setVisible(debug);
        color = new Vector3D(0.0f, 0.0f, 1.0f);
        cube.addComponent("color", new Color(color, blendFactor));
        break;

      case "CrawlerZombie":
        data = AssetManager.get().getWavefront("crawler");
        cube = Engine.get().makeGameObject(
            data.mesh, new Transform(new Vector3D(boardToWorld),
                new Vector3D(scale), new Vector3D(rot)));
        cube.addComponent("material", new Material(data.materials));
        cube.setVisible(debug);
        color = new Vector3D(0.0f, 0.3f, 0.0f);
        cube.addComponent("color", new Color(color, blendFactor));
        break;

      case "GiantZombie":
        data = AssetManager.get().getWavefront("giant");
        cube = Engine.get().makeGameObject(
            data.mesh, new Transform(new Vector3D(boardToWorld),
                new Vector3D(scale), new Vector3D(rot)));
        cube.addComponent("material", new Material(data.materials));
        cube.setVisible(debug);
        color = new Vector3D(1.0f, 0.3f, 0.4f);
        cube.addComponent("color", new Color(color, blendFactor));
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
    gos.add(cube);
  }

  private GameObject createPlayer() {
    WavefrontLoader.WavefrontData data = AssetManager.get().getWavefront("player");

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
    AnimationManager.get().update((float) elapsed_time);
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
      camera.move(cameraVel, (float) elapsed_time * 2.5f);
      System.out.println("Position: " + camera.getPosition());
    }

    Engine.get().render((float) elapsed_time);
  }

  @Override
  public void onKeyEvent(long window, int key, int scancode, int action,
      int mods) {

    cameraVel = new Vector3D(0);
    Vector3D front = camera.getFront();
    Vector3D up = camera.getUp();
    Vector3D right = camera.getFront().cross(up).normalize();
    final Vector3D[] mirror = new Vector3D[] { new Vector3D(1, 1, 1).mult(cameraPos),
        new Vector3D(1, 1, -1).mult(cameraPos),
        new Vector3D(-1, 1, -1).mult(cameraPos),
        new Vector3D(-1, 1, 1).mult(cameraPos) };

    if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
      if (currSprite != null) {
        currSprite.hide();
        currSprite = null;
        chestBG.hide();
      }

      if (paused) {
        if (key == GLFW.GLFW_KEY_ESCAPE && escapable) {
          paused = false;
          pauseBtns.hide();
          pauseTitle.hide();
        }
        return;
      }
      switch (key) {
        case GLFW.GLFW_KEY_UP:
          cameraVel = cameraVel.add(front);
          break;

        case GLFW.GLFW_KEY_DOWN:
          cameraVel = cameraVel.sub(front);
          break;

        case GLFW.GLFW_KEY_LEFT:
          cameraVel = cameraVel.sub(right);
          break;

        case GLFW.GLFW_KEY_RIGHT:
          cameraVel = cameraVel.add(right);
          break;

        case GLFW.GLFW_KEY_F10:
          GameWin();
          break;

        case GLFW.GLFW_KEY_ESCAPE:
          pause();
          break;

        case GLFW.GLFW_KEY_F11:
          GameLose();
          break;

        case GLFW.GLFW_KEY_E:

          indexCameraMirror = indexCameraMirror - 1;
          if (indexCameraMirror < 0) {
            indexCameraMirror = mirror.length - 1;
          }
          camera.moveLooking(mirror[indexCameraMirror], cameraTarget);
          break;

        case GLFW.GLFW_KEY_Q:
          indexCameraMirror = (indexCameraMirror + 1) % mirror.length;
          camera.moveLooking(mirror[indexCameraMirror], cameraTarget);
          break;

        case GLFW.GLFW_KEY_LEFT_CONTROL:
          cameraVel = cameraVel.sub(up);
          break;

        case GLFW.GLFW_KEY_SPACE:
          cameraVel = cameraVel.add(up);
          break;

        case GLFW.GLFW_KEY_KP_8:
          Transform t = (Transform) fine.getComponent().get("transform");
          fineTuning.z += 0.01f;
          t.setTranslation(fineTuning);
          System.out.println("new Pos: " + fine2D + " -> " + fineTuning);
          break;
        case GLFW.GLFW_KEY_KP_2:
          t = (Transform) fine.getComponent().get("transform");
          fineTuning.z -= 0.01f;
          t.setTranslation(fineTuning);
          System.out.println("new Pos: " + fine2D + " -> " + fineTuning);
          break;
        case GLFW.GLFW_KEY_KP_6:
          t = (Transform) fine.getComponent().get("transform");
          fineTuning.x += 0.01f;
          t.setTranslation(fineTuning);
          System.out.println("new Pos: " + fine2D + " -> " + fineTuning);
          break;
        case GLFW.GLFW_KEY_KP_4:
          t = (Transform) fine.getComponent().get("transform");
          fineTuning.x -= 0.01f;
          t.setTranslation(fineTuning);
          System.out.println("new Pos: " + fine2D + " -> " + fineTuning);
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
    }

    cameraVel = cameraVel.normalize();
    System.out.println("cameraVel: " + cameraVel);
    System.out.println("-------------");
  }

  @Override
  public void onMouseEvent(long window, int button, int action, int mods) {
    if (currSprite != null) {
      currSprite.hide();
      currSprite = null;
      chestBG.hide();
    }

    if (paused) {
      if (button == 0 && action == GLFW.GLFW_PRESS) {
        double[][] coords = new double[2][1];
        GLFW.glfwGetCursorPos(window, coords[0], coords[1]);
        Vector2D mousePos = new Vector2D((float) coords[0][0], (float) coords[1][0]);

        Vector2D btnRematch = pausePos.add(new Vector2D(0, 648 * pauseScale));
        Vector2D btnNew = pausePos.add(new Vector2D(445 * pauseScale, 648 * pauseScale));
        Vector2D btnRematchDiff = mousePos.sub(btnRematch);
        Vector2D btnNewDiff = mousePos.sub(btnNew);
        if (btnRematchDiff.x > 0 && btnRematchDiff.x < 370 * pauseScale &&
            btnRematchDiff.y > 0 && btnRematchDiff.y < 134 * pauseScale) {
          paused = false;
          gos.clear();
          clean();
          UUID.clear();
          Window.get().getEventListener().RestartBoard();
          player = createPlayer();
          gos.add(player);
          populateGrid();
          generateBoard(new Vector3D(0), scale);
          init();
        } else if (btnNewDiff.x > 0 && btnNewDiff.x < 370 * pauseScale && btnNewDiff.y > 0 &&
            btnNewDiff.y < 134 * pauseScale) {
          AssetManager.get().clearWavefronts();
          AssetManager.get().clearTextures();
          Window.get().getEventListener().RestartBoard();
          Window.get().setScene(new Menu());
        }
      }

      return;
    }

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
    } else if (button == 0 && action == GLFW.GLFW_PRESS) {
      double[][] coords = new double[2][1];
      GLFW.glfwGetCursorPos(window, coords[0], coords[1]);
      Vector2D vec = new Vector2D((float) coords[0][0], (float) coords[1][0]);

      if (getBandageAmount() > 0) {
        Vector2D diff = vec.sub(bandageHitboxPos);
        if (diff.x > 0 && diff.y > 0 && diff.x < bandageHitboxSize.x &&
            diff.y < bandageHitboxSize.y)
          Window.get().getEventListener().CombatAction("IBandage");
      }
      int data = Engine.get().getUUID((int) vec.x, (int) vec.y);
      if (data > 0 && data <= 100)
        moveTo(data);
    }
  }

  private void moveTo(int data) {
    data -= begin;
    int x = 9 - (data % 10);
    int y = 9 - (int) (data / 10);

    System.out.println("Trying to move to " + x + ", " + y);
    System.out.println("Is in: " + getPlayer().getPosition()[0] + ", " +
        getPlayer().getPosition()[1]);
    int[] pos = new int[] { y, x };
    newPlayerPos.x = y;
    newPlayerPos.y = x;
    Window.get().getEventListener().MovePlayer(pos);
  }

  private Player getPlayer() {
    return Window.get().getEventListener().getBoard().getPlayer();
  }

  @Override
  public void clean() {
    Engine.get().clear();
    AnimationManager.get().clear();
  }

  @Override
  public void onResize(long window, int width, int height) {
    Engine.get().getCamera().setProjection(60, Window.get().getAspectRatio(),
        0.1f, 10f);
  }

  @Override
  public void onMouseMove(long window, double xpos, double ypos) {

    if (rool) {

      float xoffset = (float) xpos - lastX;
      float yoffset = lastY - (float) ypos;

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
    fov -= (float) yoffset;
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
        gos.add(createGround(pos, scale,
            new Vector3D((j + i) % 2 == 1 ? 1 : 0.3f)));
        pos.z += delta;
      }
      pos.x += delta;
    }
  }

  private int getBandageAmount() {
    return Window.get()
        .getEventListener()
        .getBoard()
        .getPlayer()
        .getBandageAmount();
  }

  @Override
  public void FinishCombat() {
  }

  @Override
  public void Combat(com.zombicidy.backend.board.combat.Combat combat) {
    Window.get().setScene(new Combat(this, playerMaxHealth, combat));
  }

  @Override
  public void ZombieKilled(CommomZombie zombie) {
    gos.remove(entities.get(zombie));
    Engine.get().removeGameObject(entities.get(zombie));
    entities.remove(zombie);
  }

  @Override
  public void UseBandage(boolean actionWasMade) {
    refreshBandage();
    refreshHPLabel();
  }

  @Override
  public void PlayerDealtDamage(int damage) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException(
        "Unimplemented method 'PlayerDealtDamage'");
  }

  @Override
  public void PlayerTookDamage(int damage) {
    refreshHPLabel();
  }

  private void refreshHPLabel() {
    updateNum(new Vector2D(HPLabelPos), HPLabelSize, getPlayerHealth(), HPlabel,
        0);
    ((Variable) healthBar.getComponent().get("variables"))
        .addVariable("progress", getPlayerHealth() / (float) playerMaxHealth);
  }

  private int getPlayerHealth() {
    return Window.get()
        .getEventListener()
        .getBoard()
        .getPlayer()
        .getHealthPoints();
  }

  @Override
  public void Redraw(int[] position, Grid grid) {
    Vector3D worldPos;
    Vector3D targetRot;
    Vector3D delta;

    System.err.println("Redraw: " + grid.GetType() + "| pos: " + position[0] +
        ", " + position[1]);
    switch (grid.GetType()) {
      case "Player":
        movePlayer(position);
        break;
      case "Ground":
        break;
      default:
        Vector2D pos = new Vector2D(position[0], position[1]);
        GameObject ent = entities.get(grid);
        if (ent == null)
          return;
        Transform t = (Transform) ent.getComponent().get("transform");
        worldPos = boardToWorld(pos);

        targetRot = new Vector3D(t.getRotation());
        delta = ((Transform) player.getComponent().get("transform"))
            .getTranslation()
            .sub(worldPos);
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
        animate(t,
            new Transform(new Vector3D(worldPos), new Vector3D(t.getScale()),
                new Vector3D(targetRot)),
            0.4f);
        break;
    }

    board[position[0]][position[1]] = grid;
  }

  private void updateVisibility(Vector2D pos, int maxDepth) {
    if (debug)
      return;

    updateVisibility(pos, new HashSet<>(), 0, maxDepth);
  }

  private void updateVisibility(Vector2D pos, Set<Vector2D> trace, int depth,
      int maxDepth) {
    if (pos.x < 0 || pos.x > 9 || pos.y < 0 || pos.y > 9 || depth > maxDepth)
      return;

    System.out.println(String.format("pos: " + pos));
    Grid g = board[(int) pos.x][(int) pos.y];
    if (!"Ground".equals(g.GetType()) && !"Player".equals(g.GetType())) {
      try {
        entities.get(g).setVisible(true);
      } catch (NullPointerException e) {

      }
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
    int[] pos = new int[] { (int) newPlayerPos.x, (int) newPlayerPos.y };
    Grid grid = Window.get().getEventListener().getGrid(pos);
    GameObject ent = entities.get(grid);
    gos.remove(ent);
    Engine.get().removeGameObject(ent);

    switch (item) {
      case "Gun":
        gainedGun();
        break;

      case "Bandage":
        gainedBandage();
        break;

      case "BeisebolBat":
        gainedBat();
        break;

      default:
        throw new RuntimeException("Gained some mysterious item!\n");
    }
    this.chestBG.show();
    this.currSprite.show();
  }

  private void gainedBandage() {
    this.currSprite = this.chestSprites[0];
    refreshBandage();
  }

  private void gainedBat() {
    WavefrontData data = AssetManager.get().getWavefront("hero_bat");
    player.setMesh(data.mesh);
    this.currSprite = this.chestSprites[2];
  }

  private void gainedGun() {
    chamber.getColor().setColor(new Vector3D(0.8f));
    refreshAmmo();
    if (!hasGun)
      this.currSprite = this.chestSprites[1];
    else
      this.currSprite = this.chestSprites[3];
    hasGun = true;
  }

  private void refreshAmmo() {

    Gun gun = Window.get().getEventListener().getBoard().getPlayer().getPlayerGun();
    int ammo = 0;
    if (gun != null) {
      this.chamber.getColor().setColor(new Vector3D(0.8f));
      ammo = gun.GetAmmo();
    } else {
      this.chamber.getColor().setColor(new Vector3D(0.05f));
    }
    for (int i = 0; i < bullets.size(); i++) {
      if (i < ammo) {
        bullets.get(i).show();
      } else {
        bullets.get(i).hide();
      }
    }
  }

  @Override
  public void GameWin() {
    ArrayList<Mesh2D.Vertex> vertices = new ArrayList<>();
    Vector2D pos = new Vector2D(Window.get().width() / 2, Window.get().height() / 2);
    TextureData data = AssetManager.get().getTexture("win");
    Vector2D size = new Vector2D(data.width * pauseScale, data.height * pauseScale);
    pos = pos.sub(new Vector2D(size.x / 2, size.y / 2));
    pausePos = pos;

    vertices.add(new Mesh2D.Vertex(pos.x, pos.y, 1.9f, 0, 0));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y, 1.9f, 1, 0));
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y + size.y, 1.9f, 0, 1));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y + size.y, 1.9f, 1, 1));
    Mesh2D mesh = new Mesh2D(vertices);
    vertices.clear();
    Texture texture = new Texture(data, new Texture.TextureParam());
    Color color = new Color(new Vector3D(0.05f), 0.0f);
    Shader shader = new Shader("ui");
    Engine.get().makeUIObject(mesh, shader, texture, color);
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y, 2f, 0, 0));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y, 2f, 1, 0));
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y + size.y, 2f, 0, 1));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y + size.y, 2f, 1, 1));
    mesh = new Mesh2D(vertices);
    vertices.clear();
    Engine.get().makeUIObject(
        mesh, shader,
        new Texture(AssetManager.get().getTexture("Buttons"),
            new Texture.TextureParam()),
        color);
    paused = true;
    escapable = false;
  }

  private void pause() {
    if (pauseBtns != null && pauseTitle != null) {
      pauseBtns.show();
      pauseTitle.show();
      paused = true;
      escapable = true;
      return;
    }

    ArrayList<Mesh2D.Vertex> vertices = new ArrayList<>();
    Vector2D pos = new Vector2D(Window.get().width() / 2, Window.get().height() / 2);
    TextureData data = AssetManager.get().getTexture("pause");
    Vector2D size = new Vector2D(data.width * pauseScale, data.height * pauseScale);
    pos = pos.sub(new Vector2D(size.x / 2, size.y / 2));
    pausePos = pos;

    vertices.add(new Mesh2D.Vertex(pos.x, pos.y, 1.9f, 0, 0));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y, 1.9f, 1, 0));
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y + size.y, 1.9f, 0, 1));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y + size.y, 1.9f, 1, 1));
    Mesh2D mesh = new Mesh2D(vertices);
    vertices.clear();
    Texture texture = new Texture(data, new Texture.TextureParam());
    Color color = new Color(new Vector3D(0.05f), 0.0f);
    Shader shader = new Shader("ui");
    pauseTitle = Engine.get().makeUIObject(mesh, shader, texture, color);
    uios.add(pauseTitle);
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y, 2f, 0, 0));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y, 2f, 1, 0));
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y + size.y, 2f, 0, 1));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y + size.y, 2f, 1, 1));
    mesh = new Mesh2D(vertices);
    vertices.clear();
    pauseBtns = Engine.get().makeUIObject(
        mesh, shader,
        new Texture(AssetManager.get().getTexture("Buttons"),
            new Texture.TextureParam()),
        color);
    uios.add(pauseBtns);
    paused = true;
    escapable = true;
  }

  @Override
  public void GameLose() {
    ArrayList<Mesh2D.Vertex> vertices = new ArrayList<>();
    Vector2D pos = new Vector2D(Window.get().width() / 2, Window.get().height() / 2);
    TextureData data = AssetManager.get().getTexture("lose");
    Vector2D size = new Vector2D(data.width * pauseScale, data.height * pauseScale);
    pos = pos.sub(new Vector2D(size.x / 2, size.y / 2));
    pausePos = pos;

    vertices.add(new Mesh2D.Vertex(pos.x, pos.y, 1.9f, 0, 0));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y, 1.9f, 1, 0));
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y + size.y, 1.9f, 0, 1));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y + size.y, 1.9f, 1, 1));
    Mesh2D mesh = new Mesh2D(vertices);
    vertices.clear();
    Texture texture = new Texture(data, new Texture.TextureParam());
    Color color = new Color(new Vector3D(0.05f), 0.0f);
    Shader shader = new Shader("ui");
    Engine.get().makeUIObject(mesh, shader, texture, color);

    vertices.add(new Mesh2D.Vertex(pos.x, pos.y, 2f, 0, 0));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y, 2f, 1, 0));
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y + size.y, 2f, 0, 1));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y + size.y, 2f, 1, 1));
    mesh = new Mesh2D(vertices);
    vertices.clear();
    Engine.get().makeUIObject(
        mesh, shader,
        new Texture(AssetManager.get().getTexture("Buttons"),
            new Texture.TextureParam()),
        color);
    paused = true;
    escapable = false;
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
            (float time, float duration) -> {
              float per = time / duration;
              float qper = -((2 * per - 1) * (2 * per - 1)) + 2;

              Vector3D newPos = new Vector3D(orig.getTranslation());
              newPos = newPos.add(target.getTranslation().sub(newPos).mult(per));
              newPos.y *= qper;

              Vector3D newRot = new Vector3D(orig.getRotation());
              newRot = newRot.add(target.getRotation().sub(newRot).mult(per));

              Vector3D newScale = new Vector3D(orig.getScale());
              newScale = newScale.add(target.getScale().sub(newScale).mult(per));

              dest.set(newPos, newScale, newRot);
            },
            dur)
        .start();
  }

  @Override
  public void SurpriseEncounter() {
    ((Transform) player.getComponent().get("transform"))
        .setTranslation(boardToWorld(newPlayerPos));
  }

  private void movePlayer(int[] position) {
    playerPos.x = position[0];
    playerPos.y = position[1];
    Vector3D worldPos = boardToWorld(playerPos);

    Transform orig = ((Transform) player.getComponent().get("transform"));
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
        new Transform(new Vector3D(worldPos), new Vector3D(orig.getScale()),
            new Vector3D(targetRot)),
        0.4f);

    worldPos.y += 1.0f;
    playerLight.setPosition(worldPos);
    updateVisibility(playerPos, 2);
  }
}