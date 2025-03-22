package com.zombicidy.frontend.engine.components;

import com.zombicidy.frontend.GarbageDisposer;
import com.zombicidy.frontend.engine.math.Vector2D;
import com.zombicidy.frontend.engine.math.Vector3D;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import org.lwjgl.opengl.GL40;
import org.lwjgl.system.MemoryStack;


public class Mesh3D implements Component {
  final private int vbo;
  final private int vao;
  final private int vertexAmount;

  public static class Vertex {
    public final Vector3D vertices;
    public final Vector2D uv;
    public final Vector3D normal;
    public final int materialId;

    public final float[] data;

    public Vertex(Vector3D vertices, Vector2D uv, Vector3D normal,
                  int materialId) {
      this.vertices = vertices;
      this.uv = uv;
      this.normal = normal;
      this.materialId = materialId;
      this.data = new float[8];

      this.data[0] = vertices.x;
      this.data[1] = vertices.y;
      this.data[2] = vertices.z;
      this.data[3] = uv.x;
      this.data[4] = uv.y;
      this.data[5] = normal.x;
      this.data[6] = normal.y;
      this.data[7] = normal.z;
    }

    @Override
    public String toString() {
      return "Vertex(vertex = " + vertices + ", uv = " + uv +
          ", normal = " + normal + ")";
    }

    static public int SizeBytes() {
      return Vector3D.SizeBytes() + Vector2D.SizeBytes() +
          Vector3D.SizeBytes() + Integer.BYTES;
    }

    public float[] getData() { return data; }
  }

  public Mesh3D(List<Vertex> vertexs) {
    vbo = GL40.glGenBuffers();
    vao = GL40.glGenVertexArrays();
    vertexAmount = vertexs.size();

    GL40.glBindVertexArray(vao);
    GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, vbo);

    ByteBuffer buffer;

    try (MemoryStack stack = MemoryStack.stackPush()) {
      buffer = stack.malloc(vertexs.size() * Vertex.SizeBytes());
    } catch (java.lang.OutOfMemoryError e) {
      buffer = ByteBuffer.allocateDirect(vertexs.size() * Vertex.SizeBytes())
                   .order(ByteOrder.nativeOrder());
    }

    for (Vertex vertex : vertexs) {
      for (Float elem : vertex.getData()) {
        buffer.putFloat(elem);
      }
      buffer.putInt(vertex.materialId);
    }

    buffer.flip();
    GL40.glBufferData(GL40.GL_ARRAY_BUFFER, buffer, GL40.GL_STATIC_DRAW);

    // Set up vertex attributes
    GL40.glEnableVertexAttribArray(0);
    GL40.glVertexAttribPointer(0, 3, GL40.GL_FLOAT, false, Vertex.SizeBytes(),
                               0); // Position

    GL40.glEnableVertexAttribArray(1);
    GL40.glVertexAttribPointer(1, 2, GL40.GL_FLOAT, false, Vertex.SizeBytes(),
                               Vector3D.SizeBytes()); // UV

    GL40.glEnableVertexAttribArray(2);
    GL40.glVertexAttribPointer(2, 3, GL40.GL_FLOAT, false, Vertex.SizeBytes(),
                               Vector3D.SizeBytes() +
                                   Vector2D.SizeBytes()); // Normal

    GL40.glEnableVertexAttribArray(3);
    GL40.glVertexAttribPointer(3, 1, GL40.GL_INT, false, Vertex.SizeBytes(),
                               Vector3D.SizeBytes() * 2 +
                                   Vector2D.SizeBytes()); // MaterialId

    GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, 0);
  }

  public int verticeAmount() { return vertexAmount; }

  @Override
  public void bind() {
    GL40.glBindVertexArray(vbo); // Ensure the VAO is bound here
  }

  @Override
  public void finalize() {
    GarbageDisposer.addCleanupTask(() -> {
      GL40.glDeleteBuffers(vbo);
      GL40.glDeleteVertexArrays(vao);
    });
  }

  @Override
  public void unbind() {
    GL40.glBindVertexArray(0); // Ensure the VAO is bound here
  }
}
