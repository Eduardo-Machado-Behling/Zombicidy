package com.zombicidy.frontend.scenes;

import com.zombicidy.backend.board.baseclasses.Grid;
import com.zombicidy.backend.board.characters.CommomZombie;
import com.zombicidy.backend.board.characters.Player;
import com.zombicidy.backend.board.items.Gun;
import com.zombicidy.frontend.AnimationManager;
import com.zombicidy.frontend.AssetManager;
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
import com.zombicidy.frontend.engine.components.Variable;
import com.zombicidy.frontend.engine.lights.DirectionalLight;
import com.zombicidy.frontend.engine.lights.PointLight;
import com.zombicidy.frontend.engine.math.Vector2D;
import com.zombicidy.frontend.engine.math.Vector3D;
import java.util.ArrayList;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL40;

public class Combat extends Scene {

  private Engine.GameObject playerGO;
  private Player player;

  private final Camera camera;
  private Vector3D cameraVel = new Vector3D();
  private final Vector2D cameraRot = new Vector2D(90, 0);

  private float fov = 60;
  private boolean debug = false;
  private final float lastX = Window.get().width() / 2;
  private final float lastY = Window.get().width() / 2;
  private final double[] lastXS = new double[1];
  private final double[] lastYS = new double[1];
  private boolean rool = false;
  private Scene original;
  private com.zombicidy.backend.board.combat.Combat combat;

  private Vector2D fineTuning;
  private Vector2D bandageLabelSize = new Vector2D(16, 16);
  private Vector2D bandageHitboxPos;
  private Vector2D bandageHitboxSize;
  private UIObject[] bandagesUIOS = new UIObject[2];
  private ArrayList<UIObject> uios = new ArrayList<>();

  private Vector2D bandageLabelPos;
  private ArrayList<Engine.UIObject> bullets = new ArrayList<>();
  private UIObject chamber;
  private Vector2D HPLabelPos;
  private UIObject[] HPlabel = new UIObject[2];
  private Vector2D HPLabelSize;

  private UIObject enemyHP;
  private int enemyMaxHP;
  private Engine.GameObject enemyGO;
  private CommomZombie enemy;

  private boolean hasGun;

  private UIObject healthBar;
  private float playerMaxHealth;
  private UIObject EnemyHearhBG;

  public Combat(Scene original, int maxHealthPlayer,
      com.zombicidy.backend.board.combat.Combat combat) {
    this.original = original;
    this.player = combat.getPlayer();
    this.playerMaxHealth = maxHealthPlayer;
    this.combat = combat;
    this.enemy = combat.getZombie();
    enemyMaxHP = combat.getZombie().getHealthPoints();

    camera = Camera.Perspective(fov, Window.get().getAspectRatio(), 1.0f, 10.0f)
        .lookAt(new Vector3D(-0.37f, -1.87f, -2.12f),
            new Vector3D(0.78f, 0.28f, 0.57f).normalize(),
            new Vector3D(0, -1, 0).normalize());

    boolean hasBat = combat.getPlayer().getPlayerBeisebalBat() != null;
    hasGun = combat.getPlayer().getPlayerGun() != null;
    playerGO = Engine.get().makeGameObject(
        AssetManager.get().getWavefront(hasBat ? "hero_bat" : "player").mesh,
        new Transform(new Vector3D(1, -0.355f, -1), new Vector3D(0.2f),
            new Vector3D(-90, 0, 76)));
  }

  public void init() {
    // Engine.get().setCamera(new Camera());

    Engine.get().disableTracking();

    Engine.get().addGameObject(playerGO);
    Engine.get().addLight(new PointLight(
        new Vector3D(0.7f), new Vector3D(0.7f), new Vector3D(0.7f),
        camera.getPosition(), 1.0f, 0.09f, 0.032f));
    Engine.get().addLight(
        new DirectionalLight(new Vector3D(0, 1, 0), new Vector3D(0.3f),
            new Vector3D(1.0f), new Vector3D(1.0f)));

    playerGO.addComponent(
        "material",
        new Material((AssetManager.get().getWavefront("player").materials)));
    playerGO.addComponent("Color", new Color(new Vector3D(0.5f), 1.0f));

    enemyGO = createEnemy();

    playerGO.addComponent(
        "material",
        new Material((AssetManager.get().getWavefront("player").materials)));
    playerGO.addComponent("Color", new Color(new Vector3D(0.5f), 1.0f));

    createUI();

    Engine.get().setCamera(camera);
    GL40.glClearColor(8.0f / 255.0f, 23.0f / 255.0f, 48.0f / 255.0f, 0.0f);

    refreshAmmo();
    refreshBandage();
    refreshHPLabel();
  }

  private void createUI() {
    Vector2D size = new Vector2D(16, 16);
    Vector2D pos = new Vector2D(50, Window.get().height() - 150);
    createHUD();
    createEnemyHealth();

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

  private void createEnemyHealth() {
    Vector2D pos = new Vector2D(490, 200);
    TextureData text = AssetManager.get().getTexture("hearthbg");
    Vector2D size = new Vector2D(text.width / 3, text.height / 3);

    ArrayList<Mesh2D.Vertex> vertices = new ArrayList<>();
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y, 0, 0, 0));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y, 0, 1, 0));
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y + size.y, 0, 0, 1));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y + size.y, 0, 1, 1));

    Texture texture = new Texture(
        text, new Texture.TextureParam(GL40.GL_REPEAT, GL40.GL_REPEAT,
            GL40.GL_LINEAR, GL40.GL_LINEAR));
    Mesh2D mesh = new Mesh2D(vertices);
    Color color = new Color(new Vector3D(1), 0.0f);
    Shader shader = new Shader("ui");
    Texture text_h = new Texture(AssetManager.get().getTexture("hearth"),
        new Texture.TextureParam(GL40.GL_REPEAT, GL40.GL_REPEAT,
            GL40.GL_LINEAR, GL40.GL_LINEAR));

    EnemyHearhBG = Engine.get().makeUIObject(mesh, shader, texture, color);
    vertices.clear();
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y, 1, 0, 0));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y, 1, 1, 0));
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y + size.y, 1, 0, 1));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y + size.y, 1, 1, 1));
    mesh = new Mesh2D(vertices);
    enemyHP = Engine.get().makeUIObject(mesh, new Shader("health"), text_h, color);

    enemyHP.getComponent().put("variables", new Variable());
    Variable vars = (Variable) enemyHP.getComponent().get("variables");
    System.out.println("maxHP = " + enemyMaxHP + " | start = " + pos.x +
        "| end = " + size.x);
    vars.addVariable("progress", 1.0f);
  }

  private void createHUD() {
    Vector2D pos = new Vector2D(120, Window.get().height() / 2);
    TextureData text = AssetManager.get().getTexture("battle");
    Vector2D size = new Vector2D(text.width + 50, text.height);

    ArrayList<Mesh2D.Vertex> vertices = new ArrayList<>();
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y, 0, 0, 0));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y, 0, 1, 0));
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y + size.y, 0, 0, 1));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y + size.y, 0, 1, 1));

    Texture texture = new Texture(
        text, new Texture.TextureParam(GL40.GL_REPEAT, GL40.GL_REPEAT,
            GL40.GL_LINEAR, GL40.GL_LINEAR));
    Mesh2D mesh = new Mesh2D(vertices);
    Color color = new Color(new Vector3D(1), 0.0f);
    Shader shader = new Shader("ui");

    Engine.get().makeUIObject(mesh, shader, texture, color);
  }

  private void refreshAmmo() {
    Gun gun = Window.get().getEventListener().getBoard().getPlayer().getPlayerGun();
    if (gun == null)
      return;
    int ammo = gun.GetAmmo();
    for (int i = 0; i < bullets.size(); i++) {
      if (i < ammo) {
        bullets.get(i).show();
      } else {
        bullets.get(i).hide();
      }
    }
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
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y, 0, bu, bv));
    vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y, 0, bu + ud, bv));
    vertices.add(new Mesh2D.Vertex(pos.x, pos.y + size.y, 0, bu, bv + vd));
    vertices.add(
        new Mesh2D.Vertex(pos.x + size.y, pos.y + size.y, 0, bu + ud, bv + vd));

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

      getCell(pos, size, j, p);

      pos.x += size.x * space;
    }
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

    Engine.get().makeUIObject(mesh, shader, texture, color);

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
    Color color = new Color(new Vector3D(hasGun ? 0.7f : 0.05f), 1.0f);
    Shader shader = new Shader("ui");

    pos.x += size.x / 2;
    pos.y += size.y / 2;
    Vector2D delta = new Vector2D(0, -size.y / 2 + 30 * aspectRatio);
    for (int i = 0; i < 6; i++) {
      Engine.UIObject bullet = drawBullet(
          new Vector2D(pos.add(delta.rotate(i * 60))), new Vector2D(size));
      bullet.hide();
      bullets.add(bullet);
    }

    chamber = Engine.get().makeUIObject(mesh, shader, texture, color);
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

  private void drawHP(Vector2D pos, Vector2D size) {
    ArrayList<Mesh2D.Vertex> vertices = new ArrayList<>();
    TextureData data = AssetManager.get().getTexture("hpbg");
    Vector2D oldSize = size;
    size = new Vector2D(data.width, data.height);

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
    Engine.get().makeUIObject(mesh, shader, texture, color);

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
    size = oldSize;
    healthBar.getComponent().put("variables", new Variable());
    ((Variable) healthBar.getComponent().get("variables"))
        .addVariable("progress", 1.0f);

    pos.x += 70;
    pos.y += 40;
    HPLabelPos = pos;
    HPLabelSize = size;
    drawNum(new Vector2D(pos), size, player.getHealthPoints(), HPlabel, 0);
    for (int idx = 0; idx < HPlabel.length; idx++) {
      UIObject elem = HPlabel[idx];
      if (elem != null)
        elem.getColor().setColor(
            new Vector3D(107 / 255.f, 250 / 255.f, 202 / 255f));
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
      vertices.add(new Mesh2D.Vertex(pos.x, pos.y, 0, bu, bv));
      vertices.add(new Mesh2D.Vertex(pos.x + size.x, pos.y, 0, bu + ud, bv));
      vertices.add(new Mesh2D.Vertex(pos.x, pos.y + size.y, 0, bu, bv + vd));
      vertices.add(new Mesh2D.Vertex(pos.x + size.y, pos.y + size.y, 0, bu + ud,
          bv + vd));
      uio[j++].setMesh(new Mesh2D(vertices));
      vertices.clear();
    }
  }

  private GameObject createEnemy() {
    WavefrontData data = AssetManager.get().getWavefront("player");
    Vector3D rot = new Vector3D(90, 0, -71);
    Vector3D pos = new Vector3D(6, 0.35f, 0.5f);
    float scale = 0.5f;

    switch (combat.getZombie().GetType()) {
      case "CommomZombie":
        data = AssetManager.get().getWavefront("zombie");
        rot.x = -90;
        rot.z = -108;
        pos.x = 5;
        break;

      case "RunnerZombie":
        data = AssetManager.get().getWavefront("runner");
        break;

      case "CrawlerZombie":
        data = AssetManager.get().getWavefront("crawler");

        break;

      case "GiantZombie":
        data = AssetManager.get().getWavefront("giant");
        break;
      default:
        throw new AssertionError();
    }
    GameObject go = Engine.get().makeGameObject(
        data.mesh, new Transform(new Vector3D(pos), new Vector3D(scale),
            new Vector3D(rot)));

    go.addComponent("material", new Material(data.materials));
    go.addComponent("Color", new Color(new Vector3D(1f), 1.0f));

    return go;
  }

  @Override
  public void update(double elapsed_time) {
    AnimationManager.get().update((float) elapsed_time);
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

    if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
      switch (key) {
        case GLFW.GLFW_KEY_W:
          Window.get().getEventListener().CombatAction("Shoot");
          refreshAmmo();
          break;
        case GLFW.GLFW_KEY_D:
          Window.get().getEventListener().CombatAction("Attack");
          break;
        case GLFW.GLFW_KEY_A:
          Window.get().getEventListener().CombatAction("Bandage");
          break;
        case GLFW.GLFW_KEY_S:
          Window.get().getEventListener().CombatAction("Run");
          break;

        case GLFW.GLFW_KEY_LEFT_CONTROL:
          cameraVel = cameraVel.sub(up);
          break;

        case GLFW.GLFW_KEY_SPACE:
          cameraVel = cameraVel.add(up);
          break;

        case GLFW.GLFW_KEY_F3:
          System.out.println("camera.pos = " + camera.getPosition());
          System.out.println("camera.front = " + camera.getFront());
          System.out.println("camera.up = " + camera.getUp());
          break;

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

        case GLFW.GLFW_KEY_F11:
          GameLose();
          break;

        case GLFW.GLFW_KEY_KP_8:
          Transform t = (Transform) enemyGO.getComponent().get("transform");
          Vector3D newRot = t.getRotation().add(new Vector3D(0, 0, 1));
          t.setRotation(newRot);
          System.err.println("enemy.newRot = " + newRot);
          break;
        case GLFW.GLFW_KEY_KP_2:
          t = (Transform) enemyGO.getComponent().get("transform");
          newRot = t.getRotation().add(new Vector3D(0, 0, -1));
          t.setRotation(newRot);
          System.err.println("enemy.newRot = " + newRot);
          break;
        case GLFW.GLFW_KEY_KP_6:
          t = (Transform) playerGO.getComponent().get("transform");
          newRot = t.getRotation().add(new Vector3D(0, 0, 1));
          t.setRotation(newRot);
          System.err.println("player.newRot = " + newRot);
          break;
        case GLFW.GLFW_KEY_KP_4:
          t = (Transform) playerGO.getComponent().get("transform");
          newRot = t.getRotation().add(new Vector3D(0, 0, -1));
          t.setRotation(newRot);
          System.err.println("player.newRot = " + newRot);
          break;

        default:
          break;
      }

    cameraVel = cameraVel.normalize();
    System.out.println("cameraVel: " + cameraVel);
    System.out.println("-------------");
  }

  @Override
  public void clean() {
    AnimationManager.get().clear();
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

  private void refreshHPLabel() {
    updateNum(new Vector2D(HPLabelPos), HPLabelSize,
        Window.get()
            .getEventListener()
            .getBoard()
            .getPlayer()
            .getHealthPoints(),
        HPlabel, 0);

    ((Variable) healthBar.getComponent().get("variables"))
        .addVariable("progress",
            player.getHealthPoints() / (float) playerMaxHealth);
  }

  @Override
  public void FinishCombat() {
    Engine.get().clear();
    Window.get().setScene(original);
  }

  @Override
  public void Combat(com.zombicidy.backend.board.combat.Combat combat) {
  }

  @Override
  public void ZombieKilled(CommomZombie zombie) {
  }

  @Override
  public void UseBandage(boolean actionWasMade) {
    refreshBandage();
  }

  @Override
  public void SurpriseEncounter() {
  }

  @Override
  public void PlayerDealtDamage(int damage) {
    Variable vars = (Variable) enemyHP.getComponent().get("variables");
    vars.addVariable("progress", enemy.getHealthPoints() / (float) enemyMaxHP);

    UIObject[] label = new UIObject[2];
    Vector2D origPos = new Vector2D(Window.get().width() / 2 + 100,
        Window.get().height() / 2 - 50);
    drawNum(origPos, new Vector2D(32, 32), damage, label, 0);
    AnimationManager.get()
        .makeSequence()
        .add(
            (float time, float dur) -> {
              if (time == dur) {
                Engine.get().removeUIObject(label[0]);
              }
            },
            0.4f)
        .start();
  }

  @Override
  public void PlayerTookDamage(int damage) {
    refreshHPLabel();

    UIObject[] label = new UIObject[2];
    Vector2D origPos = new Vector2D(Window.get().width() / 2 - 10,
        Window.get().height() / 2 - 50);
    drawNum(origPos, new Vector2D(32, 32), damage, label, 0);
    AnimationManager.get()
        .makeSequence()
        .add(
            (float time, float dur) -> {
              if (time == dur) {
                Engine.get().removeUIObject(label[0]);
              }
            },
            0.4f)
        .start();
  }

  @Override
  public void Redraw(int[] position, Grid grid) {
  }

  @Override
  public void GainedItem(String item) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException(
        "Unimplemented method 'GainedItem'");
  }

  @Override
  public void GameWin() {
  }

  @Override
  public void GameLose() {
  }

  @Override
  public void PlayerGunNoAmmo() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException(
        "Unimplemented method 'PlayerGunNoAmmo'");
  }

  @Override
  public void PlayerNoGun() {
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
    }
  }
}
