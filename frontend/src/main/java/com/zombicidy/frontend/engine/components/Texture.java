package com.zombicidy.frontend.engine.components;

import com.zombicidy.frontend.ShaderManager;
import java.nio.ByteBuffer;
import org.lwjgl.opengl.GL40;

public class Texture implements Component {
  private final int textId;

  public static class TextureData {
    public final int width;
    public final int height;
    public final int channels;
    public final ByteBuffer data;

    public TextureData(int width, int height, int channels, ByteBuffer data) {
      this.width = width;
      this.height = height;
      this.channels = channels;
      this.data = data;
    }
  }

  public static class TextureParam {
    public final int textureWrapS;
    public final int textureWrapT;
    public final int textureMinFilter;
    public final int textureMaxFilter;

    public TextureParam(int textureWrapS, int textureWrapT,
                        int textureMinFilter, int textureMaxFilter,
                        float blendFactor) {
      this.textureWrapS = textureWrapS;
      this.textureWrapT = textureWrapT;
      this.textureMinFilter = textureMinFilter;
      this.textureMaxFilter = textureMaxFilter;
    }

    public TextureParam() {
      this.textureWrapS = GL40.GL_REPEAT;
      this.textureWrapT = GL40.GL_REPEAT;
      this.textureMinFilter = GL40.GL_LINEAR_MIPMAP_LINEAR;
      this.textureMaxFilter = GL40.GL_LINEAR;
    }
  }

  public Texture(TextureData data, TextureParam param) {
    textId = GL40.glGenTextures();
    GL40.glBindTexture(GL40.GL_TEXTURE_2D, textId);

    GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_WRAP_S,
                         param.textureWrapS);
    GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_WRAP_T,
                         param.textureWrapT);
    GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_MIN_FILTER,
                         param.textureMinFilter);
    GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_MAG_FILTER,
                         param.textureMinFilter);

    int glChannel = data.channels > 3 ? GL40.GL_RGBA : GL40.GL_RGB;

    GL40.glTexImage2D(GL40.GL_TEXTURE_2D, 0, glChannel, data.width, data.height,
                      0, glChannel, GL40.GL_UNSIGNED_BYTE, data.data);
    GL40.glGenerateMipmap(GL40.GL_TEXTURE_2D);
  }

  @Override
  public void bind() {
    GL40.glBindTexture(GL40.GL_TEXTURE_2D, textId);
    // ShaderManager.get().setUniform("blendFactor", 0.0f);
    ShaderManager.get().setUniform("isTextured", 1);
  }

  @Override
  public void clean() {
    GL40.glDeleteTextures(textId);
  }

  @Override
  public void unbind() {
    GL40.glBindTexture(GL40.GL_TEXTURE_2D, 0);
  }
}
