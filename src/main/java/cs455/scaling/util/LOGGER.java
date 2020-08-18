package cs455.scaling.util;

public class LOGGER {

  private static final String ANSI_RESET = "\u001B[0m";
  private static final String ANSI_BLACK = "\u001B[30m";
  private static final String ANSI_RED = "\u001B[31m";
  private static final String ANSI_GREEN = "\u001B[32m";
  private static final String ANSI_YELLOW = "\u001B[33m";
  private static final String ANSI_BLUE = "\u001B[34m";
  private static final String ANSI_PURPLE = "\u001B[35m";
  private static final String ANSI_CYAN = "\u001B[36m";
  private static final String ANSI_WHITE = "\u001B[37m";
  private String className = null;

  private boolean log_status = false;

  private void print(String color, String msgType, String msg) {
    if (log_status) {
      System.out.print(color);
      System.out.print("["+msgType+"]");
      System.out.print(ANSI_RESET);
      System.out.println("["+System.currentTimeMillis()+"]["+Thread.currentThread().getId()+"]:"+className+"::"+msg);
      System.out.flush();
    }
  }

  public void sleep(long milliseconds) {
    try {
      if (log_status) {
        info("Sleeping for "+milliseconds+" milliseconds.");
        Thread.sleep(milliseconds);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public synchronized void info(String msg) {
    print(ANSI_GREEN, "INFO", msg);
  }

  public synchronized void printStackTrace(Exception e) {
    error("StackTrace:");
    if (log_status)
      e.printStackTrace();
  }

  public synchronized void warning(String msg) {
    print(ANSI_PURPLE, "WARN", msg);
  }

  public synchronized void error(String msg) {
    print(ANSI_RED, "ERROR", msg);
  }

  public String BLACK(String msg) {
    return ANSI_BLACK+msg+ANSI_RESET;
  }

  public String RED(String msg) {
    return ANSI_RED+msg+ANSI_RESET;
  }

  public String GREEN(String msg) {
    return ANSI_GREEN+msg+ANSI_RESET;
  }

  public String YELLOW(String msg) {
    return ANSI_YELLOW+msg+ANSI_RESET;
  }

  public String BLUE(String msg) {
    return ANSI_BLUE+msg+ANSI_RESET;
  }

  public String PURPLE(String msg) {
    return ANSI_PURPLE+msg+ANSI_RESET;
  }

  public String CYAN(String msg) {
    return ANSI_CYAN+msg+ANSI_RESET;
  }

  public String WHITE(String msg) {
    return ANSI_WHITE+msg+ANSI_RESET;
  }

  public LOGGER(String className) {
    this.className = className;
  }

  public LOGGER(String className, boolean log_status) {
    this.className = className;
    this.log_status = log_status;
  }

  public boolean getLogStatus() {
    return this.log_status;
  }

}
