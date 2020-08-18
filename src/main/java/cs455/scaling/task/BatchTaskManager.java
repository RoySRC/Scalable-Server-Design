package cs455.scaling.task;

import cs455.scaling.threadpool.Task;
import cs455.scaling.threadpool.ThreadPool;
import cs455.scaling.util.LOGGER;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is responsible for handling the batches containing the task from the server. This class handles adding
 * tasks to the batch while also at the same time making sure that if adding the task makes the batch reach its
 * maximum size, it is properly dispatched to the thread pool to be processed. This class also handles the timer for
 * each bach making sure that they do not exceed their expiration time. If a batch has reached its expiration time,
 * then that batch is dispatched to the threadpool immediately and any new tasks that were scheduled to be added to the
 * old batch are now added to the new batch.
 */

public class BatchTaskManager {

  // for logging
  private static final LOGGER log = new LOGGER(BatchTaskManager.class.getSimpleName(), false);

  private BatchTask batchTask;
  private final ThreadPool threadPool;
  private boolean _kill_ = false;
  private final long timeout; // in milliseconds
  private final ReentrantLock lock = new ReentrantLock();
  private final LinkedBlockingQueue<Task> taskQueue;
  private Thread[] threads;

  /**
   * Constructor
   *
   * @param maxSize    specifies the maximum batch size
   * @param timeout    specifies the maximum timeout in seconds before sending the batch to the threadpool
   * @param threadPool the threadpool to which to send the expired or full batches
   */
  public BatchTaskManager(int maxSize, double timeout, ThreadPool threadPool) {
    this.timeout = (long) (timeout * 1000); // convert seconds to milliseconds
    this.batchTask = new BatchTask(maxSize);
    this.threadPool = threadPool;
    this.taskQueue = new LinkedBlockingQueue<>();
    this.threads = new Thread[2];
  }


  /**
   * Get the instance of the thread pool object the batch task manager dispatches batches of tasks to.
   *
   * @return The instance of the thread pool object specified during construction
   */
  public ThreadPool getThreadPool() {
    return this.threadPool;
  }

  /**
   * set the stayAlive() function return false by changing the kill flag to true. After the kill flag has
   * been set, interrupt the thread.
   */
  public void shutdown() {
    this._kill_ = true;
    for (Thread t : threads)
      t.interrupt();
  }

  /**
   * Returns a boolean that indicates whether or the thread should stay alive
   * @return boolean indicating whether or not the thread should stay alive
   */
  private boolean stayAlive() {
    return !_kill_;
  }

  public void START() {
    Thread timerThread = new Thread(() -> {
      log.info("Running batch timer thread.");
      while (stayAlive()) {
        try {
          log.info("Sleeping for " + this.timeout + " milliseconds. ");
          Thread.sleep(this.timeout);
          log.info("Batch time has expired, sending to thread pool.");
          lock.lock();
          log.info("Acquired lock.");
          if (batchTask.size() > 0) {
            threadPool.addTask(batchTask);
            log.info("Tasks in thread pool: " + threadPool.getTaskQueueSize());
            log.info("old batchTask: " + batchTask);
            batchTask = new BatchTask(batchTask.maxSize());
            log.info("new batchTask: " + batchTask);
          }
          log.info("Successfully sent batch to thread pool.");

        } catch (InterruptedException e) {
          log.info("Batch timer thread interrupted");

        } finally {
          if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.info("Released lock.");
          }
        }
      }
      log.info("Batch timer thread shutting down.");
    });

    Thread taskAdder = new Thread(() -> {
      try {
        while (true) {
          Task t = this.taskQueue.take();
          taskAdder(t, timerThread);
        }

      } catch (InterruptedException e) {
        log.printStackTrace(e);
      }
    });

    timerThread.start();
    taskAdder.start();

    threads[0] = timerThread;
    threads[1] = taskAdder;
  }

  /**
   * Add a task to the task queue
   * @param t a class implementing the Task interface
   * @throws InterruptedException
   */
  public void addTask(Task t) throws InterruptedException {
    this.taskQueue.put(t);
  }

  /**
   * The following function adds a task to the current {@link BatchTask} instance maintained by the
   * {@link BatchTaskManager}. After adding a task to the batch, if the size of the batch has reached the maximum
   * specified size, then the batch is dispatched to the thread pool to be processed. After this function sends a
   * batch to the thread pool, it creates a new empty batch and resets the batch timer such that the batch can be
   * sent to the thread pool even if the batch has not reached the maximum size.
   *
   * @param t This is the task that is to be added to the thread pool
   */
  private void taskAdder(Task t, Thread timerThread) {
    lock.lock();
    try {
      batchTask.addTask(t);
      if (batchTask.size() >= batchTask.maxSize()) {
        log.info("Maximum batch size reached.");
        threadPool.addTask(batchTask);
        batchTask = new BatchTask(batchTask.maxSize());
        log.info("Interrupting timer thread to send batch to thread pool.");
        timerThread.interrupt();

      }

    } catch (Exception e) {
      log.printStackTrace(e);

    } finally {
      lock.unlock();
    }
  }

  /**
   * Get the number of tasks in the current batch task
   * @return integer representing the number of tasks in the current batch task
   */
  public int size() {
    return batchTask.size();
  }

}
