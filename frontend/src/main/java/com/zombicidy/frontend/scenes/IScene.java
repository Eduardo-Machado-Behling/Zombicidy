package com.zombicidy.frontend.scenes;

public interface IScene {
  abstract void update(double elapsed_time);
  abstract void display(double elapsed_time);

  abstract void onKeyEvent(long window, int key, int scancode, int action,
                           int mods);

  abstract void onMouseEvent(long window, int button, int action, int mods);

  abstract void onResize(long window, int width, int height);
  abstract void onMouseMove(long window, double xpos, double ypos);
  abstract void onMouseScroll(long window, double xoffset, double yoffset);

  abstract void clean();
}