package cs455.scaling.util;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Random;

/**
 * This class contains a set of utility functions that are used throughout the system
 */

public class Util {

  // for logging
  private static final LOGGER log = new LOGGER(Util.class.getSimpleName(), false);

  /**
   * Return a random integer
   * @return
   */
  public static int randInt() {
    return new Random().nextInt();
  }

  /**
   * The following function is responsible for reading the contents of socket channel into the passed byte buffer.
   * The return value of this function is the number of bytes read. if for some reason the socket channel got closed
   * the bytes read will be -1 and the function will return -1
   * @param buffer The byte buffer in which the contents of the socketChannel will be read into
   * @param socketChannel The channel from where to read the data
   * @return Integer representing the number of bytes read from the socket channel
   * @throws IOException
   */
  public static int readFromChannel(ByteBuffer buffer, SocketChannel socketChannel) throws IOException {
    int bytesRead = socketChannel.isOpen() ? 0 : -1;
    while (buffer.hasRemaining() && bytesRead != -1) {
      bytesRead = socketChannel.read(buffer);
    }
    return bytesRead;
  }

  /**
   * The following function is responsible for writing the contents of @data into the socket channel passed as
   * argument. The return value of the function is the number of bytes written to the socket channel. If the socket
   * channel got closed for some reason, then the following function returns -1.
   * @param socketChannel The channel to where to write the data
   * @param data The actual data to be written to the socket channel
   * @return Integer representing the number of bytes written to the socket channel
   * @throws IOException
   */
  public static int writeToChannel(SocketChannel socketChannel, byte[] data) throws IOException {
    int bytesWritten = socketChannel.isOpen() ? 0 : -1;
    ByteBuffer buffer = ByteBuffer.wrap(data);
    while (buffer.hasRemaining() && bytesWritten != -1) {
      bytesWritten = socketChannel.write(buffer);
    }
    return bytesWritten;
  }

  /**
   * Get the IP address and port pair separated by a colon of the remote end of a socket connection
   * @param socket The socket connection whose remote IP and Port pair will be returned
   * @return A string containing the ip and port pair of the remote end of the socket connection
   */
  public static String getRemoteAddressPortPair(Socket socket) {
    InetSocketAddress address = (InetSocketAddress) socket.getRemoteSocketAddress();
    return address.getAddress().getHostAddress()+":"+address.getPort();
  }

  /**
   * Generate a random byte array of given size in bytes
   * @param size_bytes the length of the byte array to return
   * @return a random byte array of the specified size
   */
  public static byte[] randByteArray(int size_bytes) {
    byte[] returnValue = new byte[size_bytes];
    Random random = new Random();
    random.nextBytes(returnValue);
    return returnValue;
  }

  /**
   * Given a byte array compute and return its SHA1 hash
   * @param data The byte array to compute the SHA1 hash of
   * @return String representing the SHA1 hash of the byte array
   * @throws NoSuchAlgorithmException
   */
  public static String SHA1FromBytes(final byte[] data) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA1");
    byte[] hash = digest.digest(data);
    BigInteger hashInt = new BigInteger(1, hash);
    return hashInt.toString(16);
  }

  /**
   * Get number of milliseconds since January 1, 1970, 00:00:00 GMT
   * @return Long representing the current UNIX timestamp
   */
  public static long getTimestamp() {
    return System.currentTimeMillis();
  }

  /**
   * The following function computes the standard deviation for a set of data points.
   * @param X The set of data points to use to compute the standard deviation
   * @param mean The mean of the data points
   * @return The standard deviation of the set of data points @X
   */
  public static double computeStandardDeviation(ArrayList<Double> X, double mean) {
    double sumSquare = 0;
    for (double x : X) {
      sumSquare += Math.pow(x-mean, 2);
    }
    return Math.sqrt(sumSquare/X.size());
  }

}