package cs455.scaling.client;

import cs455.scaling.util.LOGGER;
import cs455.scaling.util.Util;
import cs455.scaling.wireformats.Protocol;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * This class contains the code implementing the client that is responsible for sending and receiving messages to and
 * from the server. Messages are sent to the server at the rate specified during construction of the object.
 */

public class ClientNode {

  // for logging
  private static final LOGGER log = new LOGGER(ClientNode.class.getSimpleName(), false);

  /**
   * The host address of where the server is located
   */
  private final String server_host;

  /**
   * The host port of where the server is located
   */
  private final int server_port;

  /**
   * The number of messages to send to the server per second
   */
  private final int message_rate;

  /**
   * A linked list containing the SHA1 message digests of the messages sent to the server
   */
  private final LinkedList<String> messageDigests;

  /**
   * Byte buffer to receive the messages sent from the server
   */
  private final ByteBuffer receiveBuffer;

  /**
   * Used to store the number of sent and received messages
   */
  private final ClientStatistics counter;

  /**
   * Socket channel for connection to server
   */
  private SocketChannel serverConnection = null;

  /**
   * The selector object for processing incoming messages from the server
   */
  private Selector selector = null;

  /**
   * Constructor
   *
   * @param server_host  The host where the server is running
   * @param server_port  the port on the host where the server is running
   * @param message_rate The rate at which to send messages to the server
   */
  public ClientNode(String server_host, int server_port, int message_rate) {
    this.server_host = server_host;
    this.server_port = server_port;
    this.message_rate = message_rate;
    this.messageDigests = new LinkedList<>();
    this.receiveBuffer = ByteBuffer.allocate(Protocol.SERVER_RESPONSE_MESSAGE_SIZE.getValue());
    this.counter = new ClientStatistics();
  }

  /**
   * Get the message rate
   *
   * @return the number of messages to be sent to the server per second
   */
  public int getMessageRate() {
    return this.message_rate;
  }

  /**
   * Get the socket channel
   *
   * @return the socket channel corresponding to the server connection
   */
  public SocketChannel getServerConnection() {
    return this.serverConnection;
  }

  /**
   * Start the printer thread responsible for printing client message statistics to screen
   */
  public void startPrinter() {
    Thread t = new Thread(() -> {
      while (this.serverConnection.isOpen()) {
        synchronized (this.counter) {
          System.out.println(
              String.format("[%d] Total Sent Count: %d, Total Received Count: %d",
                  Util.getTimestamp(), this.counter.getTotalSentCount(),
                  this.counter.getTotalReceiveCount())
          );
          this.counter.resetCounters();
          if (log.getLogStatus())
            System.out.println(log.PURPLE("size of linked list: " + this.messageDigests.size()));
        }
        System.out.flush();

        try {
          Thread.sleep(20 * 1000);
        } catch (InterruptedException e) {
          log.printStackTrace(e);
        }

      }
    });
    t.start();
  }

  /**
   * Create a connection channel to the server. Once this channel has been created, register this with the selector
   * so that it can listen for activity of interest - OP_READ - in the receiver thread.
   */
  public void connectToServer() {
    log.info("Connecting to server at " + server_host + ":" + server_port);
    try {
      selector = Selector.open(); // open a selector to listen for activity on SocketChannel

      // Open the SocketChannel
      serverConnection = SocketChannel.open(new InetSocketAddress(server_host, server_port));
      serverConnection.configureBlocking(false);
      log.info((serverConnection == null) ? "serverConnection is null" : "serverConnection is not null");
      log.info("Connected to server " + Util.getRemoteAddressPortPair(serverConnection.socket()));

      // Register the serverConnection SocketChannel with the selector
      serverConnection.register(selector, SelectionKey.OP_READ);

    } catch (Exception e) {
      e.printStackTrace();
    }
    log.info("Done with method connectToServer");
  }

  /**
   * Close the connection to the server
   *
   * @throws IOException
   */
  public void closeServerConnection() throws IOException {
    if (serverConnection.isOpen())
      serverConnection.close();
  }

  /**
   * Receiver thread, responsible for receiving incoming messages from the server and calling receiveMessage()
   * function when a message is received.
   */
  public void startReceiverThread() {
    log.info("Starting receiver thread");
    Thread t = new Thread(() -> {
      while (this.serverConnection.isOpen()) {
        this.receiveMessage();
      }
    });
    t.start();
    log.info("Receiver thread started");
  }

  /**
   * Receive message handler. Responsible for handling received messages. When a message is received by the receiver
   * thread, it calls the following function which removes the message from the linked list data structure maintained
   * for all messages sent to the server. When a message is sent to the server, the SHA1 hashed message digest is put
   * in the linked list, and when the hashed value of the message sent to the server is received, that entry is
   * removed from the linked list.
   */
  public void receiveMessage() {
    try {
      this.receiveBuffer.clear(); // prepare the receiveBuffer for reading

      // Selects a set of keys whose corresponding channels are ready for I/O operations.
      // block until at least one channel is ready for operation
      if (this.selector.select() == 0) {
        return;
      }

      // Loop over ready keys
      Iterator<SelectionKey> iter = this.selector.selectedKeys().iterator();
      while (iter.hasNext()) {
        SelectionKey key = iter.next(); // Grab the current key
        iter.remove();

        if (!key.isValid()) {
          log.error("Invalid key.");
          continue;
        }

        if (key.isReadable()) {
          int bytesRead = Util.readFromChannel(this.receiveBuffer, this.serverConnection);
          if (bytesRead == -1) {
            log.error("Bytes Read: " + bytesRead);
            continue;
          }
          String response = new String(this.receiveBuffer.array()).trim();
          log.info("Received response: " + response);
          this.removeFromMessageDigest(response);
          this.counter.incrementReceiveCount();

        }
      }

    } catch (CancelledKeyException e) {
      log.error("Received cancelled key exception");

    } catch (Exception e) {
      log.error("Server returned an element not in linked list.");
      log.error("Dumping linked list contents: " + this.messageDigests.toString());
      log.printStackTrace(e);
    }
  }

  /**
   * The following function is responsible for generating and sending random Protocol.MESSAGE_SIZE KB size messages to
   * the server. After this message has been sent to the server, it computes the SHA1 hash of the message and adds it
   * to the linked list data structure of message digests to keep track of messages sent to the server.
   *
   * @throws IOException
   */
  public void sendMessage() throws IOException {
    try {
      // generate a random byte array
      byte[] message = Util.randByteArray(Protocol.MESSAGE_SIZE.getValue());

      // Compute the hash and put into messageDigests linked list
      String messageDigest = String.format("%40s", Util.SHA1FromBytes(message)).replace(" ", "-");
      this.addToMessageDigest(messageDigest);
      this.counter.incrementSentCount();

      // Send the message to the server
      log.info("Sending message to server.");
      int bytesWritten = Util.writeToChannel(serverConnection, message);
      log.info("bytesWritten: " + bytesWritten);
      log.info("Message sent to server.");

    } catch (IOException e) {
      throw e;

    } catch (Exception e) {
      log.printStackTrace(e);
    }
  }

  /**
   * The following function adds a message digest to the linked list of message digests.
   *
   * @param message
   */
  private synchronized void addToMessageDigest(String message) {
    this.messageDigests.add(message);
    log.info("Added " + message + " to linked list.");
  }

  /**
   * Adapted from:
   * https://www.geeksforgeeks.org/linkedlist-remove-method-in-java/
   * <p></p>
   * The following function removes a message digest from the linked list of message digests. If the message digest
   * is not in the linked list, it throws an exception with the message "Linked list does not contain <message-digest>"
   *
   * @param message The message digest to be removed
   */
  private synchronized void removeFromMessageDigest(String message) throws Exception {
    boolean returnValue = this.messageDigests.remove(message);
    if (returnValue) {
      log.info("Successfully removed message: " + message);
    } else {
      log.error("Linked list does not contain message: " + message);
      log.error("Dumping linked list content: " + messageDigests.toString());
      if (log.getLogStatus()) {
        System.exit(-1);
      }
      throw new Exception("Linked list does not contain message: " + message);

    }
  }


}
