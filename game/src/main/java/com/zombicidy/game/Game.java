package com.zombicidy.game;

import com.zombicidy.backend.frontend.Menu;
import com.zombicidy.frontend.FrontEnd;

public class Game {
  public static void main(String[] args) { debugFrontEnd(); }

  static public void debugFrontEnd() {
    FrontEnd f = new FrontEnd();
    f.main();
  }

  static public void debugBackEnd() { Menu f = new Menu(); }
}
