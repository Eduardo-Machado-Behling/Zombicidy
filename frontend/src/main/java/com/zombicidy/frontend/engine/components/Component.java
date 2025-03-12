package com.zombicidy.frontend.engine.components;

public interface Component {
  void bind();
  void unbind();

  void clean();
}
