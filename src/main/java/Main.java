import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class Main {
	public static void main(String[] args) {
		if (!GLFW.glfwInit()) {
			throw new IllegalStateException("Failed to initialize GLFW!");
		}

		long window = GLFW.glfwCreateWindow(800, 600, "LWJGL Window", 0, 0);
		if (window == 0) {
			throw new RuntimeException("Failed to create the GLFW window");
		}

		GLFW.glfwMakeContextCurrent(window);
		GL.createCapabilities();

		while (!GLFW.glfwWindowShouldClose(window)) {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			GLFW.glfwSwapBuffers(window);
			GLFW.glfwPollEvents();
		}

		GLFW.glfwDestroyWindow(window);
		GLFW.glfwTerminate();
	}
}
