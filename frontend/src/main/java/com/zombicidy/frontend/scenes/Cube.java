package com.zombicidy.frontend.scenes;

import com.zombicidy.frontend.Window;
import com.zombicidy.frontend.engine.Camera;
import com.zombicidy.frontend.engine.Engine;
import com.zombicidy.frontend.engine.components.Mesh;
import com.zombicidy.frontend.engine.components.Mesh.Vertex;
import com.zombicidy.frontend.engine.math.SquareMatrix;
import com.zombicidy.frontend.engine.math.Vector2D;
import com.zombicidy.frontend.engine.math.Vector3D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.opengl.GL30;

public class Cube implements IScene {
  private Engine.GameObject go;
  private Vector3D rot = new Vector3D(0.3f, 0, 0);

  public Cube() {
    Vertex[] vertices = loadOBJ("assets/models/untitled.obj");

    // Vertex[] vertices = new Vertex[] {
    //     new Vertex(new Vector3D(-0.5f, 0.0f, 1.5f), new Vector2D(0.0f, 0.0f),
    //                new Vector3D(0.0f, 0.0f, 0.0f)),
    //     new Vertex(new Vector3D(0.0f, 1.0f, 0.5f), new Vector2D(0.0f, 0.0f),
    //                new Vector3D(0.0f, 0.0f, 0.0f)),
    //     new Vertex(new Vector3D(0.5f, 0.0f, 1.5f), new Vector2D(0.0f, 0.0f),
    //                new Vector3D(0.0f, 0.0f, 0.0f)),
    // };

    Mesh mesh = new Mesh(vertices);
    go = Engine.get().makeGameObject(mesh);
    // Engine.get().setCamera(new Camera());
    Engine.get().setCamera(
        Camera.Perspective(60, Window.get().getAspectRatio(), 0.1f, 10.0f)
            .lookAt(new Vector3D(0, 0, -3), new Vector3D(0, 0, 3),
                    new Vector3D(0, 1, 0)));
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

    rot.y += elapsed_time;
    // rot.y += 2 * elapsed_time;
    // rot.z += elapsed_time / 4;

    go.getTransform().setMatrix(SquareMatrix.rotation(rot));

    Engine.get().render();
  }

  @Override
  public void onKeyEvent(long window, int key, int scancode, int action,
                         int mods) {}

  @Override
  public void onMouseEvent(long window, int button, int action, int mods) {}

  @Override
  public void clean() {}

  public Vertex[] loadOBJ(String filePath) {
    List<Vector3D> vertices = new ArrayList<>();
    List<Vector2D> uvs = new ArrayList<>();
    List<Vector3D> normals = new ArrayList<>();
    List<Vertex> vertexList = new ArrayList<>();

    List<String> faces = new ArrayList<>();

    try (InputStream inputStream =
             Cube.class.getClassLoader().getResourceAsStream(filePath)) {
      if (inputStream == null) {
        throw new RuntimeException("Model file not found: " + filePath);
      }

      try (BufferedReader reader = new BufferedReader(
               new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          String[] tokens = line.split("\\s+");

          // Read vertex positions (v x y z)
          if (tokens[0].equals("v")) {
            float x = Float.parseFloat(tokens[1]);
            float y = Float.parseFloat(tokens[2]);
            float z = Float.parseFloat(tokens[3]);
            vertices.add(new Vector3D(x, y, z));
          }

          // Read texture coordinates (vt u v)
          else if (tokens[0].equals("vt")) {
            float u = Float.parseFloat(tokens[1]);
            float v = Float.parseFloat(tokens[2]);
            uvs.add(new Vector2D(u, v));
          }

          // Read normals (vn x y z)
          else if (tokens[0].equals("vn")) {
            float nx = Float.parseFloat(tokens[1]);
            float ny = Float.parseFloat(tokens[2]);
            float nz = Float.parseFloat(tokens[3]);
            normals.add(new Vector3D(nx, ny, nz));
          }

          // Read faces (f v1/vt1/vn1 v2/vt2/vn2 ...)
          else if (tokens[0].equals("f")) {
            for (int i = 1; i < tokens.length; i++) {
              faces.add(tokens[i]);
            }
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Error reading model file: " + filePath, e);
    }

    // Generate vertices with positions, UVs, and normals based on face indices
    for (String face : faces) {
      String[] vertexIndices = face.split("/");

      int vertexIndex =
          Integer.parseInt(vertexIndices[0]) - 1; // OBJ indices start at 1
      int uvIndex =
          Integer.parseInt(vertexIndices[1]) - 1; // OBJ indices start at 1
      int normalIndex =
          Integer.parseInt(vertexIndices[2]) - 1; // OBJ indices start at 1

      // Get the corresponding vertex, UV, and normal
      Vector3D vPos = vertices.get(vertexIndex);
      Vector2D vUv = uvs.get(uvIndex);
      Vector3D vNormal = normals.get(normalIndex);

      // Create a new Vertex and add it to the vertex list
      vertexList.add(new Vertex(vPos, vUv, vNormal));
    }

    // Convert List<Vertex> to Vertex[]
    return vertexList.toArray(new Vertex[0]);
  }
}
