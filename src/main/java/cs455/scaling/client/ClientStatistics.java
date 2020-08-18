package cs455.scaling.client;

/**
 * This class is for storing the statistics of the client. These statistics include the number of messages sent to
 * the server and the number of messages received from it.
 */

public class ClientStatistics {

  /**
   * Counter for storing the total number of sent messages
   */
  private long totalSentCount = 0;

  /**
   * Counter for storing the total number of received messages
   */
  private long totalReceiveCount = 0;

  /**
   * Increment the count for the number of received messages
   */
  public void incrementReceiveCount() {
    this.totalReceiveCount++;
  }

  /**
   * Increment the count for the number of sent messages
   */
  public void incrementSentCount() {
    this.totalSentCount++;
  }

  /**
   * Get the total number of sent messages to the server
   * @return long representing the total number of messages sent to the server
   */
  public long getTotalSentCount() {
    return this.totalSentCount;
  }

  /**
   * Get the total number of messages received from the server
   * @return long representing the total number of messages received from the server
   */
  public long getTotalReceiveCount() {
    return this.totalReceiveCount;
  }

  /**
   * Reset the total sent and receive counters to zero.
   */
  public void resetCounters() {
    this.totalSentCount = 0;
    this.totalReceiveCount = 0;
  }

}
