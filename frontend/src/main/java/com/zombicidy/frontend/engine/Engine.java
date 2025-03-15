package com.zombicidy.frontend.engine;

import com.zombicidy.frontend.AssetManager;
import com.zombicidy.frontend.ShaderManager;
import com.zombicidy.frontend.engine.components.Component;
import com.zombicidy.frontend.engine.components.Mesh3D;
import com.zombicidy.frontend.engine.components.Shader;
import com.zombicidy.frontend.engine.components.Transform;
import com.zombicidy.frontend.engine.lights.Light;
import com.zombicidy.frontend.engine.math.Vector3D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import org.lwjgl.opengl.GL40;


public class Engine {
  public class GameObject {
    private Mesh3D mesh;
    private Shader shader;
    private final HashMap<String, Component> components = new HashMap<>();

    public Mesh3D getMesh() { return mesh; }

    public void setMesh(Mesh3D mesh) { this.mesh = mesh; }

    public Shader getShader() { return shader; }

    public void setShader(Shader shader) { this.shader = shader; }

    public void addComponent(String name, Component c) {
      components.put(name, c);
    }
    public void rmvComponent(String name) { components.remove(name); }

    public HashMap<String, Component> getComponent() { return components; }
  }

  final private HashMap<Shader, HashMap<Mesh3D, ArrayList<GameObject>>>
      gameObjects = new HashMap<>();

  final private ArrayList<Light> lights = new ArrayList<>();
  final private int lightsMax = 10;

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

  public void render(float elapsed_time) {
    for (Entry<Shader, HashMap<Mesh3D, ArrayList<GameObject>>> entry :
         gameObjects.entrySet()) {

      entry.getKey().bind();

      int i = 0;
      for (Light light : lights) {
        i += light.bind(i);
      }

      ShaderManager.get().setUniform("viewPos", camera.getPosition());
      ShaderManager.get().setUniform("m_projection",
                                     camera.getProjectionMatrix());
      ShaderManager.get().setUniform("m_view", camera.getViewMatrix());

      for (Entry<Mesh3D, ArrayList<GameObject>> en :
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

  public void clear() {
    gameObjects.clear();
    lights.clear();
  }

  public boolean addLight(Light light) {
    if (lightsMax == lights.size())
      return false;

    lights.add(light);
    return true;
  }

  public void rmvLight(Light light) { lights.remove(light); }

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

    if (!gameObjects.containsKey(shader))
      gameObjects.put(shader, new HashMap<>());

    HashMap<Mesh3D, ArrayList<GameObject>> inter = gameObjects.get(shader);

    if (!inter.containsKey(mesh))
      inter.put(mesh, new ArrayList<>());

    inter.get(mesh).add(go);
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

  public boolean rayIntersectsAABB(Vector3D rayOrigin, Vector3D rayDir,
                                   Vector3D minBounds, Vector3D maxBounds) {
    float tmin = Float.NEGATIVE_INFINITY;
    float tmax = Float.POSITIVE_INFINITY;

    // X planes
    if (Math.abs(rayDir.x) > 1e-6) {
      float tx1 = (minBounds.x - rayOrigin.x) / rayDir.x;
      float tx2 = (maxBounds.x - rayOrigin.x) / rayDir.x;
      tmin = Math.max(tmin, Math.min(tx1, tx2));
      tmax = Math.min(tmax, Math.max(tx1, tx2));
    } else if (rayOrigin.x < minBounds.x || rayOrigin.x > maxBounds.x) {
      return false;
    }

    // Y planes
    if (Math.abs(rayDir.y) > 1e-6) {
      float ty1 = (minBounds.y - rayOrigin.y) / rayDir.y;
      float ty2 = (maxBounds.y - rayOrigin.y) / rayDir.y;
      tmin = Math.max(tmin, Math.min(ty1, ty2));
      tmax = Math.min(tmax, Math.max(ty1, ty2));
    } else if (rayOrigin.y < minBounds.y || rayOrigin.y > maxBounds.y) {
      return false;
    }

    // Z planes
    if (Math.abs(rayDir.z) > 1e-6) {
      float tz1 = (minBounds.z - rayOrigin.z) / rayDir.z;
      float tz2 = (maxBounds.z - rayOrigin.z) / rayDir.z;
      tmin = Math.max(tmin, Math.min(tz1, tz2));
      tmax = Math.min(tmax, Math.max(tz1, tz2));
    } else if (rayOrigin.z < minBounds.z || rayOrigin.z > maxBounds.z) {
      return false;
    }

    return tmax >= tmin && tmax >= 0;
  }
}
