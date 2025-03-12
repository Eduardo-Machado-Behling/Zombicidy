package com.zombicidy.frontend.engine;

import com.zombicidy.frontend.ShaderManager;
import com.zombicidy.frontend.engine.components.Component;
import com.zombicidy.frontend.engine.components.Mesh;
import com.zombicidy.frontend.engine.components.Shader;
import com.zombicidy.frontend.engine.components.Transform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import org.lwjgl.opengl.GL40;

public class Engine {
  public class GameObject {
    private Mesh mesh;
    private Shader shader;
    private final HashMap<String, Component> components = new HashMap<>();

    public Mesh getMesh() { return mesh; }

    public void setMesh(Mesh mesh) { this.mesh = mesh; }

    public Shader getShader() { return shader; }

    public void setShader(Shader shader) { this.shader = shader; }

    public void addComponent(String name, Component c) {
      components.put(name, c);
    }
    public void rmvComponent(String name) { components.remove(name); }

    public HashMap<String, Component> getComponent() { return components; }
  }

  final private HashMap<Shader, HashMap<Mesh, ArrayList<GameObject>>>
      gameObjects = new HashMap<>();
  private static Engine instance = null;
  private Camera camera;

  private Engine() {}

  public static Engine get() {
    if (instance == null) {
      instance = new Engine();
    }

    return instance;
  }

  public void setCamera(Camera camera) { this.camera = camera; }
  public Camera getCamera() { return this.camera; }

  public void render() {
    for (Entry<Shader, HashMap<Mesh, ArrayList<GameObject>>> entry :
         gameObjects.entrySet()) {

      entry.getKey().bind();

      ShaderManager.get().setUniform("m_projection",
                                     camera.getProjectionMatrix());
      ShaderManager.get().setUniform("m_view", camera.getViewMatrix());

      for (Entry<Mesh, ArrayList<GameObject>> en :
           entry.getValue().entrySet()) {

        en.getKey().bind();

        for (GameObject go : en.getValue()) {

          for (Component elem : go.getComponent().values()) {
            elem.bind();
          }

          GL40.glDrawArrays(GL40.GL_TRIANGLES, 0, go.getMesh().verticeAmount());

          for (Component elem : go.getComponent().values()) {
            elem.unbind();
          }
        }
      }
    }
  }

  public GameObject makeGameObject(Mesh mesh) {
    return makeGameObject(mesh, new Shader(), new Transform());
  }

  public GameObject makeGameObject(Mesh mesh, Shader shader) {
    return makeGameObject(mesh, shader, new Transform());
  }

  public GameObject makeGameObject(Mesh mesh, Shader shader,
                                   Transform transform) {
    GameObject go = new GameObject();
    go.setMesh(mesh);
    go.setShader(shader);
    go.addComponent("transform", transform);

    if (!gameObjects.containsKey(shader))
      gameObjects.put(shader, new HashMap<>());

    HashMap<Mesh, ArrayList<GameObject>> inter = gameObjects.get(shader);

    if (!inter.containsKey(mesh))
      inter.put(mesh, new ArrayList<>());

    inter.get(mesh).add(go);
    return go;
  }

  public void notifyProgram(GameObject go, Shader old, Shader upt) {
    gameObjects.get(old).get(go.getMesh()).remove(go);
    if (!gameObjects.containsKey(upt))
      gameObjects.put(upt, new HashMap<>());

    HashMap<Mesh, ArrayList<GameObject>> inter = gameObjects.get(upt);

    if (!inter.containsKey(go.getMesh()))
      inter.put(go.getMesh(), new ArrayList<>());
    inter.get(go.getMesh()).add(go);
  }
}
