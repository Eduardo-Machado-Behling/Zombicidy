package com.zombicidy.frontend.scenes;

import com.zombicidy.frontend.AssetManager;
import com.zombicidy.frontend.WavefrontLoader;
import com.zombicidy.frontend.Window;
import com.zombicidy.frontend.engine.Camera;
import com.zombicidy.frontend.engine.Engine;
import com.zombicidy.frontend.engine.components.Texture;
import com.zombicidy.frontend.engine.components.Transform;
import com.zombicidy.frontend.engine.math.SquareMatrix;
import com.zombicidy.frontend.engine.math.Vector2D;
import com.zombicidy.frontend.engine.math.Vector3D;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL30;


public class Cube implements IScene {
  private Engine.GameObject go;
  private Vector3D rot = new Vector3D(0.3f, 0, 0);
  private final Camera camera;
  private Vector3D cameraVel = new Vector3D();
  private Vector2D cameraRot = new Vector2D(90, 0);

  private final float lastX = Window.get().width() / 2;
  private final float lastY = Window.get().width() / 2;
  private final double[] lastXS = new double[1];
  private final double[] lastYS = new double[1];
  private boolean rool = false;

  public Cube() {

    // Vertex[] vertices = new Vertex[] {
    //     new Vertex(new Vector3D(-0.5f, 0.0f, 1.5f), new Vector2D(0.0f, 0.0f),
    //                new Vector3D(0.0f, 0.0f, 0.0f)),
    //     new Vertex(new Vector3D(0.0f, 1.0f, 0.5f), new Vector2D(0.0f, 0.0f),
    //                new Vector3D(0.0f, 0.0f, 0.0f)),
    //     new Vertex(new Vector3D(0.5f, 0.0f, 1.5f), new Vector2D(0.0f, 0.0f),
    //                new Vector3D(0.0f, 0.0f, 0.0f)),
    // };

    WavefrontLoader.WavefrontData data =
        WavefrontLoader.loadOBJ("assets/models/untitled.obj");

    go = Engine.get().makeGameObject(data.mesh);
    go.addComponent("texture", new Texture(AssetManager.get().getTexture("foo"),
                                           new Texture.TextureParam()));
    // Engine.get().setCamera(new Camera());
    camera =
        Camera.Perspective(60, Window.get().getAspectRatio(), 0.1f, 10.0f)
            .lookAt(new Vector3D(0, 0, -3), new Vector3D(0, 0, 3).normalize(),
                    new Vector3D(0, -1, 0).normalize());
    Engine.get().setCamera(camera);
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

    // rot.y += elapsed_time;
    // rot.y += 2 * elapsed_time;
    // rot.z += elapsed_time / 4;

    Transform t = (Transform)go.getComponent().get("transform");
    t.setMatrix(SquareMatrix.rotation(rot));

    camera.move(cameraVel, (float)elapsed_time * 2.5f);

    Engine.get().render();
  }

  @Override
  public void onKeyEvent(long window, int key, int scancode, int action,
                         int mods) {

    cameraVel = new Vector3D(0);
    Vector3D front = camera.getFront();
    Vector3D up = camera.getUp();
    Vector3D right = camera.getFront().cross(up).normalize();

    switch (key) {
    case GLFW.GLFW_KEY_W:
      if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
        cameraVel = cameraVel.add(front);
      break;

    case GLFW.GLFW_KEY_S:
      if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
        cameraVel = cameraVel.sub(front);
      break;

    case GLFW.GLFW_KEY_A:
      if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
        cameraVel = cameraVel.sub(right);
      break;

    case GLFW.GLFW_KEY_D:
      if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
        cameraVel = cameraVel.add(right);
      break;

    case GLFW.GLFW_KEY_LEFT_CONTROL:
      if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
        cameraVel = cameraVel.sub(up);
      break;

    case GLFW.GLFW_KEY_SPACE:
      if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT)
        cameraVel = cameraVel.add(up);
      break;

    default:
      break;
    }

    cameraVel = cameraVel.normalize();
    System.out.println("cameraVel: " + cameraVel);
    System.out.println("-------------");
  }

  @Override
  public void onMouseEvent(long window, int button, int action, int mods) {
    if (button == 2) {
      rool = action == GLFW.GLFW_PRESS;
      if (rool) {
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR,
                              GLFW.GLFW_CURSOR_DISABLED);
        GLFW.glfwGetCursorPos(window, lastXS, lastYS);
        GLFW.glfwSetCursorPos(window, lastX, lastY);
      } else {
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR,
                              GLFW.GLFW_CURSOR_NORMAL);
        GLFW.glfwSetCursorPos(window, lastXS[0], lastYS[0]);
      }
    }
  }

  @Override
  public void clean() {}

  @Override
  public void onResize(long window, int width, int height) {
    Engine.get().getCamera().setProjection(
        SquareMatrix.Perspective(60, Window.get().getAspectRatio(), 0.1f, 5f));
  }

  @Override
  public void onMouseMove(long window, double xpos, double ypos) {

    if (rool) {

      float xoffset = (float)xpos - lastX;
      float yoffset = lastY - (float)ypos;

      float sensitivity = 0.1f;
      xoffset *= sensitivity;
      yoffset *= sensitivity;

      cameraRot.x += xoffset;
      cameraRot.y += yoffset;

      if (cameraRot.y > 89.0f)
        cameraRot.y = 89.0f;
      if (cameraRot.y < -89.0f)
        cameraRot.y = -89.0f;

      camera.rotate(cameraRot.x, cameraRot.y);
      GLFW.glfwSetCursorPos(window, lastX, lastY);
    }
  }
}
