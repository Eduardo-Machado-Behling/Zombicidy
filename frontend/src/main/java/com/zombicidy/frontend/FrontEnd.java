package com.zombicidy.frontend;

import com.zombicidy.frontend.scenes.Cube;

public class FrontEnd {
  public void main() {
    Window win = Window.get();
    win.setScene(new Cube());

    win.run();
  }
}
