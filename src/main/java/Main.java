import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.*;

public class Main implements GLEventListener {
	@Override
	public void init(GLAutoDrawable drawable) {
		// Initialization code
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// Cleanup code
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		gl.glClearColor(0.0f, 0.0f, 1.0f, 1.0f); // Blue background
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glViewport(0, 0, width, height);
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("JOGL Demo");
		GLProfile profile = GLProfile.get(GLProfile.GL4); // Use GL4 core profile
		GLCapabilities capabilities = new GLCapabilities(profile);
		GLCanvas canvas = new GLCanvas(capabilities);
		canvas.addGLEventListener(new Main());

		frame.getContentPane().add(canvas);
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		FPSAnimator animator = new FPSAnimator(canvas, 60);
		animator.start();
	}
}
