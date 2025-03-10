package com.zombicidy.frontend.engine;

import com.zombicidy.frontend.ShaderManager;
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
    private Transform transform;

    public Mesh getMesh() { return mesh; }

    public void setMesh(Mesh mesh) { this.mesh = mesh; }

    public Shader getShader() { return shader; }

    public void setShader(Shader shader) { this.shader = shader; }

    public Transform getTransform() { return transform; }

    public void setTransform(Transform transform) {
      this.transform = transform;
    }
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

  public void render() {
    for (Entry<Shader, HashMap<Mesh, ArrayList<GameObject>>> entry :
         gameObjects.entrySet()) {

      ShaderManager.get().useProgram(entry.getKey().getProgram());

      GL40.glUniformMatrix4fv(
          ShaderManager.get().getUniformLocation("m_projection"), false,
          camera.getProjectionMatrix().toFloatBuffer());

      GL40.glUniformMatrix4fv(ShaderManager.get().getUniformLocation("m_view"),
                              false, camera.getViewMatrix().toFloatBuffer());

      for (Entry<Mesh, ArrayList<GameObject>> en :
           entry.getValue().entrySet()) {

        GL40.glBindVertexArray(
            en.getKey().getVAO()); // Ensure the VAO is bound here

        for (GameObject go : en.getValue()) {
          GL40.glUniformMatrix4fv(
              ShaderManager.get().getUniformLocation("m_model"), false,
              go.transform.getMatrix().toFloatBuffer());

          GL40.glDrawArrays(GL40.GL_TRIANGLES, 0, go.getMesh().verticeAmount());
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
    go.setTransform(transform);

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
