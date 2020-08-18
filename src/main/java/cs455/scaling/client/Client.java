package cs455.scaling.client;

import cs455.scaling.util.LOGGER;

import java.io.IOException;

/**
 * This is the main class for starting the client.
 */

public class Client {

  // for logging
  private static final transient LOGGER log = new LOGGER(Client.class.getSimpleName(), false);

  public static void main(String[] args) {
    // Create the client node
    ClientNode clientNode = new ClientNode(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));

    log.info("Messaging rate: " + clientNode.getMessageRate());

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        clientNode.closeServerConnection();
      } catch (IOException e) {
        log.printStackTrace(e);
      }
    }));

    try {
      clientNode.connectToServer();
      clientNode.startReceiverThread();
      clientNode.startPrinter();
      while (clientNode.getServerConnection().isOpen()) {
        clientNode.sendMessage();
        Thread.sleep(1000 / clientNode.getMessageRate());
      }

    } catch (IOException e) {
      System.out.println("Client disconnected form server.");

    } catch (InterruptedException e) {
      System.out.println("Client received interrupted exception.");
    }

    try {
      clientNode.closeServerConnection();
    } catch (IOException e) {
      System.out.println("Failed to disconnect from server.");
    }
  }

}
