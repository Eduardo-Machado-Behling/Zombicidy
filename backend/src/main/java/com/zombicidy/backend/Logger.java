package com.zombicidy.backend;

public class Logger {
  public enum Verbose { NONE, INFO, DEBUG, ALL }

  private static Logger instance = null;
  private Verbose outputLevel = Verbose.NONE;

  private Logger() {}

  public static Logger get() {
    if (instance == null) {
      instance = new Logger();
    }

    return instance;
  }

  public void setVerbose(Verbose level) { outputLevel = level; }

  public void log(Verbose level, String message) {
    if (level.ordinal() <= outputLevel.ordinal()) {
      System.out.println(message);
    }
  }

  public void log(Verbose level, String fmt, Object... args) {
    if (level.ordinal() <= outputLevel.ordinal()) {
      System.out.println(String.format(fmt, args));
    }
  }

  public void logError(Verbose level, String message) {
    if (level.ordinal() <= outputLevel.ordinal()) {
      System.err.println(message);
    }
  }

  public void logError(Verbose level, String fmt, Object... args) {
    if (level.ordinal() <= outputLevel.ordinal()) {
      System.err.println(String.format(fmt, args));
    }
  }
}
