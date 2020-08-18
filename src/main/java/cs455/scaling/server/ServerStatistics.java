package cs455.scaling.server;

/**
 * Class for storing per client statistics used by the server to compute server throughput, mean per-client
 * throughput and standard deviation of per-client throughput.
 */

public class ServerStatistics {

  /**
   * Counter for storing the total number of messages received from a particular client
   */
  private int messages = 0;

  /**
   * Empty constructor
   */
  public ServerStatistics() {}

  /**
   * Increment the counter variable for the number of messages received by 1
   */
  public void incrementMessageCount() {
    this.messages++;
  }

  /**
   * Get the value of the counter variable for the total number of messages received from a particular client
   * @return integer representing the total number of messages received from a particular client
   */
  public int getTotalMessages() {
    return this.messages;
  }

  /**
   * Reset the value in the counter variable for the total number of messages received from a particular client.
   */
  public void resetMessagesCount() {
    this.messages = 0;
  }

}
