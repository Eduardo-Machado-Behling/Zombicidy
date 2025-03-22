package com.zombicidy.frontend;

import com.zombicidy.frontend.engine.components.Material.MaterialData;
import com.zombicidy.frontend.engine.components.Mesh3D;
import com.zombicidy.frontend.engine.components.Mesh3D.Vertex;
import com.zombicidy.frontend.engine.math.Vector2D;
import com.zombicidy.frontend.engine.math.Vector3D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WavefrontLoader {
  public static class WavefrontData {
    public final List<MaterialData> materials;
    public final Mesh3D mesh;

    public WavefrontData(List<MaterialData> materials, Mesh3D mesh) {
      this.materials = materials;
      this.mesh = mesh;
    }
  };

  public static WavefrontData loadOBJ(String filePath) {
    List<Vector3D> vertices = new ArrayList<>();
    List<Vector2D> uvs = new ArrayList<>();
    List<Vector3D> normals = new ArrayList<>();
    List<String> materials = new ArrayList<>();
    List<Vertex> vertexList = new ArrayList<>();
    String materialFile = "";

    List<String> faces = new ArrayList<>();

    try (InputStream inputStream =
             WavefrontLoader.class.getClassLoader().getResourceAsStream(
                 filePath)) {
      if (inputStream == null) {
        throw new RuntimeException("Model file not found: " + filePath);
      }

      try (BufferedReader reader = new BufferedReader(
               new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          String[] tokens = line.split("\\s+");

          // Read vertex positions (v x y z)
          switch (tokens[0]) {
          case "v":
            float x = Float.parseFloat(tokens[1]);
            float y = Float.parseFloat(tokens[2]);
            float z = Float.parseFloat(tokens[3]);
            vertices.add(new Vector3D(x, y, z));
            break;
          case "vt":
            float u = Float.parseFloat(tokens[1]);
            float v = Float.parseFloat(tokens[2]);
            uvs.add(new Vector2D(u, v));
            break;
          case "vn":
            float nx = Float.parseFloat(tokens[1]);
            float ny = Float.parseFloat(tokens[2]);
            float nz = Float.parseFloat(tokens[3]);
            normals.add(new Vector3D(nx, ny, nz));
            break;
          case "usemtl":
            materials.add(tokens[1]);
            break;
          case "mtllib":
            materialFile = tokens[1];
            break;
          case "f":
            for (int i = 1; i < tokens.length; i++) {
              faces.add(tokens[i] + "/" + String.valueOf(materials.size() - 1));
            }
            break;
          default:
            break;
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
      int materialIndex =
          Integer.parseInt(vertexIndices[3]); // OBJ indices start at 1

      // Get the corresponding vertex, UV, and normal
      Vector3D vPos = vertices.get(vertexIndex);
      Vector2D vUv = uvs.get(uvIndex);
      Vector3D vNormal = normals.get(normalIndex);

      // Create a new Vertex and add it to the vertex list
      vertexList.add(new Vertex(vPos, vUv, vNormal, materialIndex));
    }

    Mesh3D mesh = new Mesh3D(vertexList);
    List<MaterialData> materialsData = loadMaterials(materialFile, materials);

    return new WavefrontData(materialsData, mesh);
  }

  private static List<MaterialData> loadMaterials(String filename,
                                                  List<String> materials) {
    String filePath = "assets/models/" + filename;

    HashMap<String, MaterialData> hmat = new HashMap<>();

    try (InputStream inputStream =
             WavefrontLoader.class.getClassLoader().getResourceAsStream(
                 filePath)) {
      if (inputStream == null) {
        throw new RuntimeException("Material file not found: " + filePath);
      }

      try (BufferedReader reader = new BufferedReader(
               new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
        String line;
        String name = "";
        MaterialData data = null;
        while ((line = reader.readLine()) != null) {
          String[] tokens = line.split("\\s+");

          // Read vertex positions (v x y z)
          switch (tokens[0]) {
          case "newmtl":
            if (data != null)
              hmat.put(name, data);
            data = new MaterialData();
            name = tokens[1];
            break;
          case "Ka":
            if (data != null) {
              data.ambient.x = Float.parseFloat(tokens[1]);
              data.ambient.y = Float.parseFloat(tokens[2]);
              data.ambient.z = Float.parseFloat(tokens[3]);
            }
            break;
          case "Kd":
            if (data != null) {
              data.diffuse.x = Float.parseFloat(tokens[1]);
              data.diffuse.y = Float.parseFloat(tokens[2]);
              data.diffuse.z = Float.parseFloat(tokens[3]);
            }
            break;
          case "Ks":
            if (data != null) {
              data.specular.x = Float.parseFloat(tokens[1]);
              data.specular.y = Float.parseFloat(tokens[2]);
              data.specular.z = Float.parseFloat(tokens[3]);
            }
            break;
          case "Ns":
            if (data != null) {
              data.specularExp = Float.parseFloat(tokens[1]);
            }
            break;
          case "d":
            if (data != null) {
              data.transparency = Float.parseFloat(tokens[1]);
            }
            break;
          case "illum":
            if (data != null) {
              data.illum =
                  MaterialData.IllumType.values()[Integer.parseInt(tokens[1])];
            }
            break;
          default:
            break;
          }
        }
        hmat.put(name, data);
      }
    } catch (IOException e) {
      throw new RuntimeException("Error reading model file: " + filePath, e);
    }

    List<MaterialData> mat = new ArrayList<>();

    for (String material : materials) {
      mat.add(hmat.get(material));
    }

    return mat;
  }
}
