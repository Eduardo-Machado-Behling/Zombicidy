package com.zombicidy.frontend;

import com.zombicidy.frontend.scenes.Play;

public class FrontEnd {
  public void main() {
    Window win = Window.get();
    win.setScene(new Play(false));

    win.run();
  }
}
