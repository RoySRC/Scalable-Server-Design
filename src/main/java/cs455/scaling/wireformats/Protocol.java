package cs455.scaling.wireformats;

/**
 * This class stores the values of variables that are stored throughout the system.
 */

public enum Protocol {

  /**
   * The size of the message to send to the server
   */
  MESSAGE_SIZE(8192), //bytes

  /**
   * The size of the response message received from the server
   */
  SERVER_RESPONSE_MESSAGE_SIZE(40); // bytes

  private final int id;

  Protocol(int id) {
    this.id = id;
  }

  public int getValue() {
    return id;
  }
}
