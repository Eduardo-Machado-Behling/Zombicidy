package com.zombicidy.frontend.engine.components;

import com.zombicidy.frontend.ShaderManager;
import com.zombicidy.frontend.engine.math.Vector3D;

public class Material implements Component {

  public static class MaterialData {
    public enum IllumType {
      FLAT,
      LAMBERT,
      PHONG,
      OTHER,
    }

    public Vector3D ambient;
    public Vector3D diffuse;
    public Vector3D specular;
    public float shininess;
    public float transparency;
    public float specularExp;
    public IllumType illum;
    public Texture.TextureData diffuseMap = null;
    public Texture.TextureData normalMap = null;

    public MaterialData() {
      this.ambient = new Vector3D();
      this.diffuse = new Vector3D();
      this.specular = new Vector3D();
      this.shininess = 0;
      this.transparency = 0;
    }
  }

  private MaterialData[] materials = new MaterialData[10];
  private int materialAmount = 0;

  // TODO: add support for texts
  final private int[] texts = new int[2];

  public Material() {
    // GL40.glGenTextures(texts);
    ;
  }

  public Material addMaterial(MaterialData md) {
    if (materialAmount < materials.length)
      materials[materialAmount++] = md;

    return this;
  }

  @Override
  public void bind() {
    for (int i = 0; i < materialAmount; i++) {
      ShaderManager.get().setUniform(String.format("materials[{}].ambient", i),
                                     materials[i].ambient);
      ShaderManager.get().setUniform(String.format("materials[{}].diffuse", i),
                                     materials[i].diffuse);
      ShaderManager.get().setUniform(String.format("materials[{}].specular", i),
                                     materials[i].specular);
      ShaderManager.get().setUniform(
          String.format("materials[{}].shininess", i), materials[i].shininess);
      ShaderManager.get().setUniform(
          String.format("materials[{}].transparency", i),
          materials[i].transparency);
    }
  }

  @Override
  public void unbind() {}

  @Override
  public void clean() {
    // GL40.glDeleteTextures(texts);
  }
}
