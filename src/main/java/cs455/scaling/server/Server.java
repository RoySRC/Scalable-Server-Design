package cs455.scaling.server;

import java.io.IOException;

/**
 * This is the main class for starting the server
 */

public class Server {

  public static void main(String[] args) throws IOException, InterruptedException {

    ServerNode serverNode = new ServerNode(Integer.parseInt(args[0]), Integer.parseInt(args[1]),
                                            Integer.parseInt(args[2]), Double.parseDouble(args[3]));

    serverNode.runPrinter();
    serverNode.startServer();

  }

}
