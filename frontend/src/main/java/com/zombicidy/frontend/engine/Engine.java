package com.zombicidy.frontend.engine;

import com.zombicidy.frontend.AssetManager;
import com.zombicidy.frontend.ShaderManager;
import com.zombicidy.frontend.Window;
import com.zombicidy.frontend.engine.components.Color;
import com.zombicidy.frontend.engine.components.Component;
import com.zombicidy.frontend.engine.components.Mesh2D;
import com.zombicidy.frontend.engine.components.Mesh3D;
import com.zombicidy.frontend.engine.components.Shader;
import com.zombicidy.frontend.engine.components.Texture;
import com.zombicidy.frontend.engine.components.Transform;
import com.zombicidy.frontend.engine.lights.Light;
import com.zombicidy.frontend.engine.lights.PointLight;
import com.zombicidy.frontend.engine.lights.SpotLight;
import com.zombicidy.frontend.engine.math.SquareMatrix;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL40;

public final class Engine {

  public class UIObject {
    private Mesh2D mesh = null;
    private Texture texture = null;
    private Color color = null;
    private Shader shader = null;
    private boolean visible = true;
    private final HashMap<String, Component> components = new HashMap<>();

    public UIObject(Mesh2D mesh, Texture texture, Color color, Shader shader) {
      this.mesh = mesh;
      this.texture = texture;
      this.color = color;
      this.shader = shader;

      components.put("texture", texture);
      components.put("color", color);
    }

    public UIObject() {
    }

    public Texture getTexture() {
      return texture;
    }

    public void setTexture(Texture texture) {
      components.put("texture", texture);
      this.texture = texture;
    }

    public Color getColor() {
      components.put("color", color);
      return color;
    }

    public void setColor(Color color) {
      this.color = color;
    }

    public Mesh2D getMesh() {
      return mesh;
    }

    public void setMesh(Mesh2D mesh) {
      if (this.mesh != null)
        Engine.get().changeMesh(this, mesh);
      this.mesh = mesh;
    }

    public Shader getShader() {
      return shader;
    }

    public void setShader(Shader shader) {
      this.shader = shader;
    }

    public HashMap<String, Component> getComponent() {
      return this.components;
    }

    public void hide() {
      visible = false;
    }

    public void show() {
      visible = true;
    }

    public boolean isVisible() {
      return visible;
    }
  }

  public class GameObject {
    private Mesh3D mesh = null;
    private Shader shader = null;
    private final HashMap<String, Component> components = new HashMap<>();
    private boolean visible = true;

    public Mesh3D getMesh() {
      return mesh;
    }

    public void setMesh(Mesh3D mesh) {
      if (this.mesh != null)
        Engine.get().changeMesh(this, mesh);
      this.mesh = mesh;
    }

    public Shader getShader() {
      return shader;
    }

    public void setShader(Shader shader) {
      this.shader = shader;
    }

    public void addComponent(String name, Component c) {
      components.put(name, c);
    }

    public void rmvComponent(String name) {
      components.remove(name);
    }

    public HashMap<String, Component> getComponent() {
      return components;
    }

    public boolean isVisible() {
      return visible;
    }

    public void setVisible(boolean visible) {
      this.visible = visible;
    }
  }

  private HashMap<Shader, HashMap<Mesh3D, ArrayList<GameObject>>> gameObjects = new HashMap<>();

  private HashMap<Shader, HashMap<Mesh2D, ArrayList<UIObject>>> uiObjects = new HashMap<>();

  private HashMap<Shader, HashMap<Mesh3D, ArrayList<GameObject>>> gameObjectsReserve = new HashMap<>();

  private ArrayList<Light> lights = new ArrayList<>();
  private ArrayList<Light> lightsReserve = new ArrayList<>();
  final private int lightsMax = 10;

  private static Engine instance = null;
  private Camera camera;
  private boolean mouseEnable = false;

  private int fbo;
  private int pickingTexture;
  private int depthTexture;

  private Engine() {
    // Generate and bind framebuffer
    fbo = GL40.glGenFramebuffers();
    GL40.glBindFramebuffer(GL40.GL_FRAMEBUFFER, fbo);

    // Generate texture to store object IDs
    pickingTexture = GL40.glGenTextures();
    GL40.glBindTexture(GL40.GL_TEXTURE_2D, pickingTexture);
    GL40.glTexImage2D(GL40.GL_TEXTURE_2D, 0, GL40.GL_R32UI,
        Window.get().width(), Window.get().height(), 0,
        GL40.GL_RED_INTEGER, GL40.GL_UNSIGNED_INT,
        (ByteBuffer) null);
    GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_MIN_FILTER,
        GL40.GL_NEAREST);
    GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_MAG_FILTER,
        GL40.GL_NEAREST);

    // Attach texture to framebuffer
    GL40.glFramebufferTexture2D(GL40.GL_FRAMEBUFFER, GL40.GL_COLOR_ATTACHMENT0,
        GL40.GL_TEXTURE_2D, pickingTexture, 0);

    // Create a depth buffer
    int depthBuffer = GL40.glGenRenderbuffers();
    GL40.glBindRenderbuffer(GL40.GL_RENDERBUFFER, depthBuffer);
    GL40.glRenderbufferStorage(GL40.GL_RENDERBUFFER, GL40.GL_DEPTH_COMPONENT,
        Window.get().width(), Window.get().height());
    GL40.glFramebufferRenderbuffer(GL40.GL_FRAMEBUFFER,
        GL40.GL_DEPTH_ATTACHMENT,
        GL40.GL_RENDERBUFFER, depthBuffer);

    // Check if framebuffer is complete
    if (GL40.glCheckFramebufferStatus(GL40.GL_FRAMEBUFFER) != GL40.GL_FRAMEBUFFER_COMPLETE) {
      throw new RuntimeException("Framebuffer not complete!");
    }

    // Unbind framebuffer
    GL40.glBindFramebuffer(GL40.GL_FRAMEBUFFER, 0);
  }

  public void changeMesh(UIObject uiObject, Mesh2D mesh) {
    HashMap<Mesh2D, ArrayList<UIObject>> shad = uiObjects.get(uiObject.getShader());
    shad.get(uiObject.getMesh()).remove(uiObject);

    if (!shad.containsKey(mesh)) {
      shad.put(mesh, new ArrayList<>());
    }

    shad.get(mesh).add(uiObject);
  }

  public void changeMesh(GameObject gameObject, Mesh3D mesh) {
    HashMap<Mesh3D, ArrayList<GameObject>> shad = gameObjects.get(gameObject.getShader());
    shad.get(gameObject.getMesh()).remove(gameObject);

    if (!shad.containsKey(mesh)) {
      shad.put(mesh, new ArrayList<>());
    }

    shad.get(mesh).add(gameObject);
  }

  public static Engine get() {
    if (instance == null) {
      instance = new Engine();
    }

    return instance;
  }

  public void enableTracking() {
    this.mouseEnable = true;
  }

  public void disableTracking() {
    this.mouseEnable = false;
  }

  public void setCamera(Camera camera) {
    this.camera = camera;
  }

  public Camera getCamera() {
    return this.camera;
  }

  public void reserve() {
    gameObjectsReserve = gameObjects;
    gameObjects = new HashMap<>();

    lightsReserve = lights;
    lights = new ArrayList<>();
  }

  public void unreserve() {
    gameObjects = gameObjectsReserve;
    lights = lightsReserve;
  }

  public void render(float elapsed_time) {
    if (mouseEnable)
      renderMouse();
    render3D(elapsed_time);
    render2D(elapsed_time);
  }

  private void render2D(float elapsed_time) {
    GL40.glEnable(GL40.GL_BLEND);
    // GL40.glDisable(GL40.GL_DEPTH_TEST);
    GL40.glBlendFunc(GL40.GL_SRC_ALPHA, GL40.GL_ONE_MINUS_SRC_ALPHA);
    GL40.glClear(GL40.GL_DEPTH_BUFFER_BIT);
    GL40.glEnable(GL40.GL_ALPHA_TEST);
    GL40.glAlphaFunc(GL40.GL_GREATER, 0.1f);

    for (Entry<Shader, HashMap<Mesh2D, ArrayList<UIObject>>> entry : uiObjects.entrySet()) {

      entry.getKey().bind();
      SquareMatrix orthoMatrix = SquareMatrix.orthographic(
          0, Window.get().width(), Window.get().height(), 0, -2, 2);
      ShaderManager.get().setUniform("m_projection", orthoMatrix);

      for (Entry<Mesh2D, ArrayList<UIObject>> en : entry.getValue().entrySet()) {

        en.getKey().bind();

        for (UIObject go : en.getValue()) {

          if (!go.isVisible())
            continue;
          for (Entry<String, Component> elem : go.getComponent().entrySet()) {
            elem.getValue().bind();
          }

          GL40.glDrawArrays(GL40.GL_TRIANGLE_STRIP, 0,
              go.getMesh().verticeAmount());

          for (Entry<String, Component> elem : go.getComponent().entrySet()) {
            elem.getValue().unbind();
          }
        }
      }
    }
  }

  private void renderMouse() {
    GL40.glBindFramebuffer(GL40.GL_FRAMEBUFFER, fbo);
    GL40.glClear(GL40.GL_COLOR_BUFFER_BIT | GL40.GL_DEPTH_BUFFER_BIT);

    // Ensure OpenGL knows we're rendering to an integer buffer
    GL40.glDrawBuffer(GL40.GL_COLOR_ATTACHMENT0);

    ShaderManager.get().useProgram("mouse");

    ShaderManager.get().setUniform("m_projection",
        camera.getProjectionMatrix());
    ShaderManager.get().setUniform("m_view", camera.getViewMatrix());
    for (HashMap<Mesh3D, ArrayList<GameObject>> hgo : gameObjects.values()) {
      for (Entry<Mesh3D, ArrayList<GameObject>> mgo : hgo.entrySet()) {
        mgo.getKey().bind();

        for (GameObject go : mgo.getValue()) {
          if (!go.getComponent().containsKey("UUID"))
            continue;

          go.getComponent().get("UUID").bind();
          go.getComponent().get("transform").bind();

          GL40.glDrawArrays(GL40.GL_TRIANGLES, 0, go.getMesh().verticeAmount());
        }
      }
    }

    // Unbind framebuffer
    GL40.glBindFramebuffer(GL40.GL_FRAMEBUFFER, 0);
  }

  public int getUUID(int mouseX, int mouseY) {
    GL40.glBindFramebuffer(GL40.GL_FRAMEBUFFER, fbo);

    // Read the pixel at the mouse position
    IntBuffer pixelBuffer = BufferUtils.createIntBuffer(1);
    GL40.glReadPixels(mouseX, Window.get().height() - mouseY, 1, 1,
        GL40.GL_RED_INTEGER, GL40.GL_UNSIGNED_INT, pixelBuffer);

    GL40.glBindFramebuffer(GL40.GL_FRAMEBUFFER, 0);

    return pixelBuffer.get(0); // Return the object ID
  }

  private void render3D(float elapsed_time) {
    GL40.glEnable(GL40.GL_DEPTH_TEST);
    GL40.glClear(GL40.GL_COLOR_BUFFER_BIT | GL40.GL_DEPTH_BUFFER_BIT);
    for (Entry<Shader, HashMap<Mesh3D, ArrayList<GameObject>>> entry : gameObjects.entrySet()) {
      entry.getValue().keySet().iterator().next().bind();
      entry.getKey().bind();

      int i = 0;
      int j = 0;
      for (Light light : lights) {
        if (light instanceof PointLight)
          i += light.bind(i);
        if (light instanceof SpotLight)
          j += light.bind(i);
      }

      ShaderManager.get().setUniform("pLightCount", i);
      ShaderManager.get().setUniform("spotLightCount", j);
      ShaderManager.get().setUniform("viewPos", camera.getPosition());
      ShaderManager.get().setUniform("m_projection",
          camera.getProjectionMatrix());
      ShaderManager.get().setUniform("m_view", camera.getViewMatrix());

      for (Entry<Mesh3D, ArrayList<GameObject>> en : entry.getValue().entrySet()) {

        en.getKey().bind();

        for (GameObject go : en.getValue()) {

          if (!go.isVisible())
            continue;

          for (Entry<String, Component> elem : go.getComponent().entrySet()) {
            if (!"UUID".equals(elem.getKey()))
              elem.getValue().bind();
          }

          GL40.glDrawArrays(GL40.GL_TRIANGLES, 0, go.getMesh().verticeAmount());

          for (Entry<String, Component> elem : go.getComponent().entrySet()) {
            if (!"UUID".equals(elem.getKey()))
              elem.getValue().unbind();
          }
        }
      }
    }
  }

  public void clear() {
    lights.clear();
    clearGameObjects();
    clearUIObjects();
  }

  private void clearGameObjects() {
    gameObjects.clear();
  }

  private void clearUIObjects() {
    uiObjects.clear();
  }

  public boolean addLight(Light light) {
    if (lightsMax == lights.size())
      return false;

    lights.add(light);
    return true;
  }

  public void rmvLight(Light light) {
    lights.remove(light);
  }

  public UIObject makeUIObject(Mesh2D mesh, Shader shader, Texture texture,
      Color color) {
    UIObject uiObject = new UIObject(mesh, texture, color, shader);

    addUIObject(uiObject);

    return uiObject;
  }

  public GameObject makeGameObject(Mesh3D mesh) {
    return makeGameObject(mesh, new Transform(),
        AssetManager.get().getShader("default"));
  }

  public GameObject makeGameObject(Mesh3D mesh, Transform transform) {
    return makeGameObject(mesh, transform,
        AssetManager.get().getShader("default"));
  }

  public GameObject makeGameObject(Mesh3D mesh, Transform transform,
      Shader shader) {
    GameObject go = new GameObject();
    go.setMesh(mesh);
    go.setShader(shader);
    go.addComponent("transform", transform);

    addGameObject(go);

    return go;
  }

  public void notifyProgram(GameObject go, Shader old, Shader upt) {
    gameObjects.get(old).get(go.getMesh()).remove(go);
    if (!gameObjects.containsKey(upt))
      gameObjects.put(upt, new HashMap<>());

    HashMap<Mesh3D, ArrayList<GameObject>> inter = gameObjects.get(upt);

    if (!inter.containsKey(go.getMesh()))
      inter.put(go.getMesh(), new ArrayList<>());
    inter.get(go.getMesh()).add(go);
  }

  public void removeGameObject(GameObject ent) {
    try {
      gameObjects.get(ent.getShader()).get(ent.getMesh()).remove(ent);
    } catch (NullPointerException e) {
      return;
    }
  }

  public void addGameObject(GameObject go) {
    if (!gameObjects.containsKey(go.getShader()))
      gameObjects.put(go.getShader(), new HashMap<>());

    HashMap<Mesh3D, ArrayList<GameObject>> inter = gameObjects.get(go.getShader());

    if (!inter.containsKey(go.getMesh()))
      inter.put(go.getMesh(), new ArrayList<>());

    inter.get(go.getMesh()).add(go);
  }

  public void addUIObject(UIObject uio) {
    if (!uiObjects.containsKey(uio.getShader()))
      uiObjects.put(uio.getShader(), new HashMap<>());

    HashMap<Mesh2D, ArrayList<UIObject>> inter = uiObjects.get(uio.getShader());

    if (!inter.containsKey(uio.getMesh()))
      inter.put(uio.getMesh(), new ArrayList<>());

    inter.get(uio.getMesh()).add(uio);
  }

  public void removeUIObject(UIObject ent) {
    try {
      uiObjects.get(ent.getShader()).get(ent.getMesh()).remove(ent);
    } catch (NullPointerException e) {
    }
  }
}