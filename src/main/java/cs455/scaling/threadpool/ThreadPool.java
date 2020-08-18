package cs455.scaling.threadpool;

import cs455.scaling.util.LOGGER;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This is the threadpool class. This class contains a set of worker threads that pick a task from the task queue and
 * start executing that task. When that task is completed, they pick another task from the queue and start executing
 * that till there are no more tasks in the pool.
 */

public class ThreadPool {

  // Logging
  private static final LOGGER log = new LOGGER(ThreadPool.class.getSimpleName(), false);

  private BlockingQueue<Task> taskQueue;
  private ArrayList<WorkerThread> workers;
  private boolean status = false; // indicates whether or not the workers in the pool are running
  private int idleThreads = -1; // number of threads in the pool that are idle, negative numbers indicate
  // no workers

  /**
   * Constructor
   * @param numThreads the number of threads in the threadpool.
   */
  public ThreadPool(int numThreads) {
    this.workers = new ArrayList<WorkerThread>();
    this.taskQueue = new LinkedBlockingQueue<Task>();

    log.info("Initializing thread pool with "+numThreads+" threads.");
    // Initialize the worker threads
    for (int i=0; i < numThreads; ++i) {
      log.info("Initializing worker thread "+i);
      this.workers.add(new WorkerThread(this, i));
    }
    log.info("Done initializing threads.");

    // Set the number of idle threads
    idleThreads = this.workers.size();
  }

  /**
   * Get a task from the taskQueue. We do not need synchronized here since LinkedBlockingQueue is thread safe.
   * @return
   */
  public Task getTask() throws InterruptedException {
    return this.taskQueue.take();
  }

  /**
   * Add a task to the queue. We do not need synchronized here since LinkedBlockingQueue is thread-safe
   * @param e The task that is to be added to the task queue maintained by the threadpool.
   * @throws InterruptedException
   */
  public synchronized void addTask(Task e) throws InterruptedException {
    this.taskQueue.put(e);
  }

  /**
   * Call the start method on each worker. Unless this method is called, none of the workers thread start. Calling
   * this function turns each of the worker objects into threads by invoking the run method on each.
   */
  public void startWorkers() {
    for (WorkerThread worker : this.workers)
      worker.start();
    status = true;
  }

  /**
   * When a thread is busy executing a task, calling the following function from the worker thread before the
   * beginning of the execution will decrement the number of idle threads in the pool by 1, since the current worker
   * thread that called the following function is about the start executing its task and will be busy till it is done
   * with the task.
   */
  public synchronized void decrementIdleThreads() {
     this.idleThreads--;
  }

  /**
   * When a thread is done executing its task, calling the following from the worker thread increments the idle
   * thread counter since now the number of idle threads have increased by 1.
   */
  public synchronized void incrementIdleThread() {
    this.idleThreads++;
  }

  /**
   * Get the number of idle threads
   * @return
   */
  public int getIdleThreads() {
    return this.idleThreads;
  }

  /**
   * Get the status of the workers in the pool.
   * @return true if all the workers in the pool have started, false otherwise
   */
  public boolean getStatus() {
    return this.status;
  }

  /**
   * Return true if all the threads in the pool are done with all the tasks in the taskQueue.
   * @return
   */
  public boolean isDone() {
    return (this.getIdleThreads() == this.workers.size()) && (this.taskQueue.size() == 0);
  }

  /**
   * Get a string representation of the Thread Pool, indicating number of threads and number of tasks in queue
   * @return A string representation of the Thread Pool
   */
  public String toString() {
    return new String(
        "ThreadPool with "+this.workers.size()+" worker threads and "+
            this.taskQueue.size()+" elements in task queue."
    );
  }

  /**
   * Get the size of the thread pool, i.e. the number of threads in the pool
   * @return the size of the thread pool
   */
  public int size() {
    return this.workers.size();
  }

  /**
   * Get the number of elements in tha task queue
   * @return the number of elements in the task queue
   */
  public int getTaskQueueSize() {
    return this.taskQueue.size();
  }

  /**
   * Kill all worker threads
   */
  public void shutdown() {
    for (WorkerThread worker : this.workers) {
      worker.kill();
    }
    this.taskQueue = null;
    this.workers = null;
    this.idleThreads = -1;
  }

}
