package cs455.scaling.wireformats;

/**
 * This class stores the values of variables that are stored throughout the system.
 */

public interface Protocol {

  /**
   * The size of the message to send to the server
   */
  public final int MESSAGE_SIZE = 8192; //bytes

  /**
   * The size of the response message received from the server
   */
  public final int SERVER_RESPONSE_MESSAGE_SIZE = 40; // bytes

}
