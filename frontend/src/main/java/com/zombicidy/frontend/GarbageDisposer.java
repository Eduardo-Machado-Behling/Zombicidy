package com.zombicidy.frontend;

import java.util.concurrent.ConcurrentLinkedQueue;

public class GarbageDisposer {
  private static final ConcurrentLinkedQueue<Runnable> cleanupTasks =
      new ConcurrentLinkedQueue<>();

  public static void addCleanupTask(Runnable task) { cleanupTasks.add(task); }

  public static void executeCleanup() {
    while (!cleanupTasks.isEmpty()) {
      cleanupTasks.poll().run();
    }
  }
}
