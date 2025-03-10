package com.zombicidy.frontend;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

public class AssetManager {
  public class Texture {
    public int width;
    public int height;
    public int channels;
    public ByteBuffer data;

    public Texture(int width, int height, int channels, ByteBuffer data) {
      this.width = width;
      this.height = height;
      this.channels = channels;
      this.data = data;
    }
  };

  static private AssetManager instance = null;

  private HashMap<String, Texture> textures = new HashMap<>();

  private AssetManager() {}

  static public AssetManager get() {
    if (instance == null) {
      instance = new AssetManager();
    }

    return instance;
  }

  public Texture getTexture(String name) {
    if (!textures.containsKey(name)) {
      loadTexture(name);
    }

    return textures.get(name);
  }

  private void loadTexture(String name) {
    String filePath = "assets/textures/" + name + ".png";
    try (MemoryStack stack = MemoryStack.stackPush()) {
      InputStream inputStream =
          getClass().getClassLoader().getResourceAsStream(filePath);
      ByteBuffer imageBuffer =
          ByteBuffer.allocateDirect(inputStream.available());
      byte[] byteArray = new byte[inputStream.available()];
      inputStream.read(byteArray);
      imageBuffer.put(byteArray); // Put bytes into the ByteBuffer
      imageBuffer.flip();         // Prepare the byte buffer

      // Load the image using STB
      IntBuffer width = stack.mallocInt(1);
      IntBuffer height = stack.mallocInt(1);
      IntBuffer channels = stack.mallocInt(1);

      // Decode the image
      ByteBuffer image = STBImage.stbi_load_from_memory(
          imageBuffer, width, height, channels, STBImage.STBI_rgb_alpha);
      if (image == null) {
        throw new RuntimeException("Failed to load image: " + filePath);
      }

      textures.put(name, new Texture(width.get(0), height.get(0),
                                     channels.get(0), image));
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to load texture.");
    }
  }
}