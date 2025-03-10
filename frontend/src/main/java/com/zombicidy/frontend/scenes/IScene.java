package com.zombicidy.frontend.scenes;

public interface IScene {
  abstract void update(double elapsed_time);
  abstract void display(double elapsed_time);

  abstract void onKeyEvent(long window, int key, int scancode, int action,
                           int mods);

  abstract void onMouseEvent(long window, int button, int action, int mods);

  abstract void clean();
}