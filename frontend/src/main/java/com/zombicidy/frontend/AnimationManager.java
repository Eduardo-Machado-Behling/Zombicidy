package com.zombicidy.frontend;

import java.util.ArrayList;

public class AnimationManager {
  @FunctionalInterface
  public interface Task {
    void execute(float time, float duration);
  }

  private static class AnimationData {

    float elapsed_time = 0;
    float duration;
    Task path;

    public AnimationData(float duration, Task path) {
      this.duration = duration;
      this.path = path;
    }

    public boolean update(float elapsed_time) {

      this.elapsed_time += elapsed_time;
      if (this.elapsed_time >= duration) {
        path.execute(duration, duration);
        return true;
      }
      path.execute(this.elapsed_time, duration);
      return false;
    }
  }

  static public class Sequence {
    ArrayList<AnimationData> animations = new ArrayList<>();
    boolean play = false;

    public Sequence add(Task task, float duration) {
      this.animations.add(new AnimationData(duration, task));
      return this;
    }

    public void start() { play = true; }

    public void pause() { play = false; }

    public boolean paused() { return !play; }

    public boolean update(float elapsed_time) {
      if (animations.isEmpty()) {
        return true;
      }

      AnimationData data = animations.get(0);
      if (data.update(elapsed_time)) {
        animations.remove(0);
        if (animations.isEmpty()) {
          return true;
        }
      }
      return false;
    }
  }

  private ArrayList<Sequence> animations = new ArrayList<>();
  private ArrayList<Sequence> delete = new ArrayList<>();
  private static AnimationManager instance = null;

  private AnimationManager() {}

  public static AnimationManager get() {
    if (instance == null) {
      instance = new AnimationManager();
    }

    return instance;
  }

  public Sequence makeSequence() {
    Sequence seq = new Sequence();
    animations.add(seq);
    return seq;
  }

  public void update(float elapsed_time) {
    for (Sequence seq : animations) {
      if (!seq.paused())
        if (seq.update(elapsed_time))
          delete.add(seq);
    }

    for (Sequence seq : delete) {
      animations.remove(seq);
    }
  }

  public void clear() { animations.clear(); }
}
