package com.zombicidy.frontend;

import com.zombicidy.frontend.engine.components.Shader;
import com.zombicidy.frontend.engine.components.Texture;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

public class AssetManager {
  static private AssetManager instance = null;

  private final HashMap<String, Texture.TextureData> textures = new HashMap<>();
  private final HashMap<String, WavefrontLoader.WavefrontData> wavefronts =
      new HashMap<>();
  private final HashMap<String, Shader> shaders = new HashMap<>();

  private AssetManager() {}

  static public AssetManager get() {
    if (instance == null) {
      instance = new AssetManager();
    }

    return instance;
  }

  public Texture.TextureData getTexture(String name) {
    if (!textures.containsKey(name)) {
      loadTexture(name);
    }

    return textures.get(name);
  }

  public WavefrontLoader.WavefrontData getWavefront(String name) {
    if (!wavefronts.containsKey(name)) {
      loadWavefront(name);
    }

    return wavefronts.get(name);
  }

  public Shader getShader(String name) {
    if (!shaders.containsKey(name)) {
      shaders.put(name, new Shader(name));
    }

    return shaders.get(name);
  }

  private void loadWavefront(String name) {
    wavefronts.put(name,
                   WavefrontLoader.loadOBJ("assets/models/" + name + ".obj"));
  }

  private void loadTexture(String name) {
    // Path should be relative to the resources folder root
    String filePath = "assets/textures/" + name + ".png";

    try (MemoryStack stack = MemoryStack.stackPush()) {
      // Get resource as stream directly - more reliable in JAR files
      InputStream inputStream =
          getClass().getClassLoader().getResourceAsStream(filePath);
      if (inputStream == null) {
        throw new RuntimeException("Failed to find resource: " + filePath);
      }

      // Read all bytes from the input stream
      byte[] byteArray = inputStream.readAllBytes();
      ByteBuffer imageBuffer = ByteBuffer.allocateDirect(byteArray.length);
      imageBuffer.put(byteArray);
      imageBuffer.flip(); // Prepare the byte buffer

      // Load the image using STB
      IntBuffer width = stack.mallocInt(1);
      IntBuffer height = stack.mallocInt(1);
      IntBuffer channels = stack.mallocInt(1);

      // Decode the image
      ByteBuffer image = STBImage.stbi_load_from_memory(
          imageBuffer, width, height, channels, STBImage.STBI_rgb_alpha);

      if (image == null) {
        throw new RuntimeException("Failed to load image: " +
                                   STBImage.stbi_failure_reason());
      }

      textures.put(name, new Texture.TextureData(width.get(0), height.get(0),
                                                 channels.get(0), image));
    } catch (IOException e) {
      throw new RuntimeException("Failed to load texture: " + e.getMessage(),
                                 e);
    }
  }
}