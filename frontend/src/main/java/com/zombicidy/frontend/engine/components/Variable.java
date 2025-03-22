package com.zombicidy.frontend.engine.components;

import com.zombicidy.frontend.ShaderManager;
import java.util.HashMap;
import java.util.Map.Entry;

public class Variable implements Component {
  HashMap<String, Object> variables = new HashMap<>();

  public void addVariable(String name, Object val) { variables.put(name, val); }
  public void rmvVariable(String name) { variables.remove(name); }
  public void clear() { variables.clear(); }

  @Override
  public void bind() {
    for (Entry<String, Object> var : variables.entrySet()) {
      ShaderManager.get().setUniform(var.getKey(), var.getValue());
    }
  }

  @Override
  public void unbind() {}

  @Override
  public void finalize() {}
}
