![](https://www.code-inspector.com/project/12395/score/svg?branch=master)
![](https://www.code-inspector.com/project/12395/status/svg?branch=master)

# Scalable-Server-Design

The objective of this project is to build a server to handle network traffic by designing and
building our own thread pool. This thread pool will have a configurable number of threads that will be
used to perform tasks relating to network communications. Specifically, we will use this thread pool to
manage all tasks relating to network communications. This includes:

* Managing incoming network connections
* Receiving data over these network connections
* Organizing data into batches to improve performance

This assignment uses Java NIO for communication to the server.

## Components
There are two components that we need for this project: 
* Server
* Client

### Server Node
There is exactly one server node in the system. The server node provides the following functions:
* Accepts incoming network connections from the clients.
* Accepts incoming traffic from these connections
* Groups data from the clients together into batches
* Replies to clients by sending back a hash code for each message received.
* The server performs functions A, B, C, and D by relying on the thread pool.

#### Client Nodes
Unlike the server node, there are multiple Clients (minimum of 100) in the system. 
The client provides the following functionalities:
* Connect and maintain an active connection to the server.
* Regularly send data packets to the server. The payloads for these data packets are 8 KB and
the values for these bytes are randomly generated. The rate at which each connection will
generate packets is R per-second. The typical value of R is between 2-4.
* The client tracks hashcodes of the data packets that it has sent to the server. The server
acknowledges every packet that it has received by sending the computed hash code back to
the client.

## Interactions between the components
The client sends messages at the rate specified during start-up. The client sends a `byte[]`
to the server. The size of this array is 8 KB and the contents of this array are randomly generated. The
client generates a new byte array for every transmission and tracks the hash codes associated with
the data that it transmits. Hashes will be generated with the SHA-1 algorithm. The following code
snippet computes the SHA-1 hash of a byte array, and returns its representation as a hex string:
```java
public String SHA1FromBytes(byte[] data) {
    MessageDigest digest = MessageDigest.getInstance("SHA1");
    byte[] hash = digest.digest(data);
    BigInteger hashInt = new BigInteger(1, hash);
}
return hashInt.toString(16);
```

The client maintains these hash codes in a linked list. For every data packet that is published, the client
adds the corresponding hashcode to the linked list. Upon receiving the data, the server will compute the
hash code for the data packet and send this back to the client. When an acknowledgement is received
from the server, the client checks the hashcode in the acknowledgement by scanning through the linked
list. Once the hashcode has been verified, it can be removed from the linked list.

The server relies on the thread pool to perform all tasks. 
The thread pool manager maintains a list of the work it needs to perform. It maintains these
work units in a FIFO queue implemented using the linked list data structure. Each unit of work is a list
of data packets with a maximum length of batch-size. Work units are added to the tail of the work
queue and when the work unit at the top of the queue has either: reached a length of batch-size
or batch-time has expired since the previous unit was removed, an available worker is assigned to
the work unit.

Every 20 seconds, the server prints its current throughput (number of messages processed per
second during last 20 seconds), the number of active client connections, and mean and standard
deviation of per-client throughput to the console. To calculate the per-client throughput statistics
(mean and standard deviation), throughputs for individual clients are maintained for last 20
seconds (number of messages processed per second sent by a particular client during last 20 seconds)
and the mean and the standard deviation of those throughput values are calculated. This message looks like the
 following:
 
**[timestamp] Server Throughput: x messages/s, Active Client Connections: y, Mean Per-
client Throughput: p messages/s, Std. Dev. Of Per-client Throughput: q messages/s**

***
**NOTE:**
*These statistics are used to evaluate correctness.
If the server is functioning correctly, the server throughput
should remain approximately constant throughout its operation. Note that it takes some time to
reach a stable value due to initialization overheads at both server and client ends. Mean per-client
throughput multiplied by the number of active connections is approximately equal to the sever
throughput.*
***

Similarly, once every 20 seconds after starting up, every client prints the number of messages it
has sent and received during the last 20 seconds. This log message looks similar to the following.

**[timestamp] Total Sent Count: x, Total Received Count: y**

## Command line arguments for the two components
Classes are organized in a package called cs455.scaling. The command-line arguments
and the order in which they should be specified for the Server and the Client are listed below:

**java cs455.scaling.server.Server portnum thread-pool-size batch-size batch-time**

**java cs455.scaling.client.Client server-host server-port message-rate**