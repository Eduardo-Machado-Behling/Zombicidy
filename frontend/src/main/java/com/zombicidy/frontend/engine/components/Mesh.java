package com.zombicidy.frontend.engine.components;

import com.zombicidy.frontend.engine.math.Vector2D;
import com.zombicidy.frontend.engine.math.Vector3D;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import org.lwjgl.opengl.GL40;
import org.lwjgl.system.MemoryStack;


public class Mesh {
  final private int vbo;
  final private int vao;
  final private int vertexAmount;

  public static class Vertex {
    public Vector3D vertices;
    public Vector2D uv;
    public Vector3D normal;
    public float[] data;

    public Vertex(Vector3D vertices, Vector2D uv, Vector3D normal) {
      this.vertices = vertices;
      this.uv = uv;
      this.normal = normal;
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
      return Vector3D.SizeBytes() + Vector2D.SizeBytes() + Vector3D.SizeBytes();
    }

    public float[] getData() { return data; }
  }

  public Mesh(Vertex[] vertexs) {
    vbo = GL40.glGenBuffers();
    vao = GL40.glGenVertexArrays();
    vertexAmount = vertexs.length;
    GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, vbo);
    GL40.glBindVertexArray(vao);

    FloatBuffer buffer;

    try (MemoryStack stack = MemoryStack.stackPush()) {
      buffer = stack.mallocFloat(vertexs.length * Vertex.SizeBytes());
    } catch (java.lang.OutOfMemoryError e) {
      buffer =
          ByteBuffer
              .allocateDirect(vertexs.length * Vertex.SizeBytes() * Float.BYTES)
              .order(ByteOrder.nativeOrder())
              .asFloatBuffer();
    }

    for (Vertex vertex : vertexs) {
      buffer.put(vertex.getData());
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

    GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, 0);
  }

  public int getVBO() { return vbo; }
  public int getVAO() { return vao; }

  public int verticeAmount() { return vertexAmount; }
}
