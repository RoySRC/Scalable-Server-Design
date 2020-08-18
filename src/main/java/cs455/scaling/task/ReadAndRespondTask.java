package cs455.scaling.task;

import cs455.scaling.server.ServerStatistics;
import cs455.scaling.threadpool.Task;
import cs455.scaling.util.LOGGER;
import cs455.scaling.util.Util;
import cs455.scaling.wireformats.Protocol;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is a task that is responsible for responding to a client when a message is received by the server.
 * This class implements the Task interface and implements the execute() method which is called by worker threads in
 * the thread pool. The code for responding to a client is in this execute() method.
 */

public class ReadAndRespondTask implements Task {

  // for logging
  private static final transient LOGGER log = new LOGGER(ReadAndRespondTask.class.getSimpleName(), false);

  private final SelectionKey selectionKey;
  private final SocketChannel client;
  private final ConcurrentHashMap<String, ServerStatistics> registeredClients;

  /**
   * Constructor
   * @param selectionKey The key returned by the server selector when there is an activity on the server socket
   *                     channel. This key can be used to get the remote socket channel of the client and register
   *                     that client with the server.
   * @param registeredClients A map of registered clients maintained by the server. This map is mostly used to store
   *                          the server statistics per client. The server statistics include the number of messages
   *                          processed by the server per client.
   */
  public ReadAndRespondTask(SelectionKey selectionKey, ConcurrentHashMap<String, ServerStatistics> registeredClients) {
    this.selectionKey = selectionKey;
    this.client = (SocketChannel) selectionKey.channel();
    this.registeredClients = registeredClients;
  }

  /**
   * The following function is from the Task interface implemented by this class. The following function contains the
   * necessary code for responding to a client. After a response message containing the hash of the received message
   * is sent to the client, the server statistics count for the total number of messages processed for that particular
   * client is incremented. This is done by calling the incrementMessageCount() function of the server statistics
   * instance of the client maintained by the server in the registeredClients hashmap.
   */
  @Override
  public void execute() {
    try {
      // Create a buffer to read into
      ByteBuffer buffer = ByteBuffer.allocate(Protocol.MESSAGE_SIZE.getValue());

      //https://stackoverflow.com/questions/11854382/how-correctly-close-socketchannel-in-java-nio
      // Read from it
      int bytesRead = Util.readFromChannel(buffer, client);
      //https://stackoverflow.com/questions/24573136/java-nio-deregister-client-connection-on-server-side
      if (bytesRead == -1) {
        log.info(log.RED("bytesRead: "+bytesRead)+" closing connection");
        log.info("Removing "+Util.getRemoteAddressPortPair(client.socket())+" from registered clients list.");
        throw new Exception("Client disconnected.");

      } else {

        byte[] receivedMessage = buffer.array();
        String response = String.format("%40s", Util.SHA1FromBytes(receivedMessage)).replace(" ", "-");

        log.info("Sending "+response+" to "+Util.getRemoteAddressPortPair(client.socket()));
        log.info("Length of response message: "+response.length());

        if (!client.isOpen())
          throw new Exception("Client Disconnected");

        int bytesWritten = Util.writeToChannel(client, response.getBytes());
        if (bytesWritten < 0) {
          throw new Exception("Client disconnected.");
        }

        log.info("bytes written to " + Util.getRemoteAddressPortPair(client.socket()) + ": " + bytesWritten + " bytes");
        log.info("buffer has " + buffer.remaining() + " bytes remaining after writing.");
        log.info("Sent " + response + " to " + Util.getRemoteAddressPortPair(client.socket()));

        if (!client.isOpen())
          throw new Exception("Client Disconnected");

        registeredClients.get(Util.getRemoteAddressPortPair(client.socket())).incrementMessageCount();

        if (!client.isOpen())
          throw new Exception("Client Disconnected");

      }

    } catch (Exception e) {
      log.printStackTrace(e);
      registeredClients.remove(Util.getRemoteAddressPortPair(client.socket()));
      try {
        client.close();
      } catch (Exception ce){}
      selectionKey.cancel();

    } finally {
      selectionKey.attach(null);
    }
  }
}
