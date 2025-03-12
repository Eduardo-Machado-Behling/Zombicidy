package com.zombicidy.frontend.engine.components;

import com.zombicidy.frontend.ShaderManager;
import com.zombicidy.frontend.engine.math.Vector3D;
import java.util.List;


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

  private final MaterialData[] materials = new MaterialData[10];
  private int materialAmount = 0;

  // TODO: add support for texts
  final private int[] texts = new int[2];

  public Material(List<MaterialData> materials) {
    for (MaterialData mat : materials) {
      addMaterial(mat);
    }
  }

  public final Material addMaterial(MaterialData md) {
    if (materialAmount < materials.length)
      materials[materialAmount++] = md;

    return this;
  }

  @Override
  public void bind() {
    for (int i = 0; i < materialAmount; i++) {
      String base = String.format("materials[%d]", i);
      ShaderManager.get().setUniform(base + ".ambient", materials[i].ambient);
      ShaderManager.get().setUniform(base + ".diffuse", materials[i].diffuse);
      ShaderManager.get().setUniform(base + ".specular", materials[i].specular);
      ShaderManager.get().setUniform(base + ".shininess",
                                     materials[i].shininess);
      ShaderManager.get().setUniform(base + ".transparency",
                                     materials[i].transparency);
      ShaderManager.get().setUniform(base + ".specularExp",
                                     materials[i].specularExp);
    }
  }

  @Override
  public void unbind() {}

  @Override
  public void clean() {
    // GL40.glDeleteTextures(texts);
  }
}
