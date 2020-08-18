package cs455.scaling.task;

import cs455.scaling.server.ServerStatistics;
import cs455.scaling.threadpool.Task;
import cs455.scaling.util.LOGGER;
import cs455.scaling.util.Util;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is a task that is responsible for registering a client with the server. This class implements the Task
 * interface and implements the execute() method which is called by worker threads in the thread pool. The code for
 * registering a client is in this execute() method.
 */

public class ClientRegistrationTask implements Task {

  // for logging
  private static final transient LOGGER log = new LOGGER(ClientRegistrationTask.class.getSimpleName(), false);

  private final SelectionKey selectionKey;
  private final Selector selector;
  private final ServerSocketChannel serverSocketChannel;
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
  public ClientRegistrationTask(SelectionKey selectionKey,
                                ConcurrentHashMap<String, ServerStatistics> registeredClients) {
    this.selectionKey = selectionKey;
    this.selector = selectionKey.selector();
    this.serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
    this.registeredClients = registeredClients;
  }

  /**
   * The following function is from the Task interface implemented by this class. The following function contains the
   * necessary code for registering a client with the server socket channel maintained by the server. After a
   * client has been registered, it is added to the map of registered clients maintained by the server.
   */
  @Override
  public void execute() {
    log.info("Running client registration task");
    try {
      selector.wakeup();
      synchronized (selector) {
        log.info("Proceeding to register client.");
        // Grab the incoming socket from the serverSocket
        SocketChannel client = serverSocketChannel.accept();
        if (client != null) {
          // Configure it to be a new channel and key that our selector should monitor
          client.configureBlocking(false);
          client.register(selector, SelectionKey.OP_READ);
          registeredClients.put(Util.getRemoteAddressPortPair(client.socket()), new ServerStatistics());
          log.info("New client registered " + Util.getRemoteAddressPortPair(client.socket()));
          log.info("Number of registered clients: " + registeredClients.size());

        } else {
          log.error(log.RED("Client is null."));
        }
      }

    } catch (IOException e) {
      log.printStackTrace(e);


    } finally {
      selectionKey.attach(null); // detach the object
    }
  }
}
