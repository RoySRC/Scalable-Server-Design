import cs455.scaling.threadpool.*;
import cs455.scaling.util.LOGGER;
import cs455.scaling.util.Util;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class ThreadPoolTest {

  // Logging
  public static final LOGGER log = new LOGGER(ThreadPoolTest.class.getSimpleName(), false);


  class incrementTask implements Task {
    private int baseNumber = Util.randInt();
    private int finalNumber = 0;

    /**
     * Increment the base number by 1
     */
    @Override
    public void execute() {
      finalNumber = baseNumber + 1;
    }
  }

  class halfTask implements Task {
    private int baseNumber = Util.randInt();
    private int finalNumber = 0;

    /**
     * Divide the base number by 2
     */
    @Override
    public void execute() {
      finalNumber = baseNumber / 2;
    }
  }


  /**
   * The following test checks to make sure that the pool can be initialized with the specified number of threads
   */
  @Test
  public void TestCreation() {
    ThreadPool threadPool = new ThreadPool(12);
    String expected = "ThreadPool with 12 worker threads and 0 elements in task queue.";
    assertEquals(expected, threadPool.toString());
  }

  private incrementTask[] createIncrementTasks() {
    incrementTask[] tasks = new incrementTask[240];
    for (int i=0; i<tasks.length; ++i) {
      tasks[i] = new incrementTask();
    }
    return tasks;
  }

  /**
   * The following test only checks to make sure that tasks can be added successfully to the thread pool
   * @throws InterruptedException
   */
  @Test
  public void TestAddingTask() throws InterruptedException {
    int numThreads = 12;
    ThreadPool threadPool = new ThreadPool(numThreads);

    // Check to make sure that the thread pool contains the specified number of threads
    assertEquals(numThreads, threadPool.size());

    // Create a set of tasks
    Task[] tasks = createIncrementTasks();

    // Add the tasks to the thread pool
    for (Task t : tasks)
      threadPool.addTask(t);

    // check to make sure that all the tasks have been added
    assertEquals(tasks.length, threadPool.getTaskQueueSize());
  }

  /**
   * Test execution of the tasks in the thread pool after all the tasks have been added
   */
  @Test
  public void TestExecutionAfterAddingTasks() throws InterruptedException {
    int numThreads = 12;
    ThreadPool threadPool = new ThreadPool(numThreads);
    incrementTask[] tasks = createIncrementTasks();
    for (incrementTask t : tasks) threadPool.addTask(t);

    // turn all worker objects into threads
    threadPool.startWorkers();

    while (threadPool.getIdleThreads() != numThreads);
    Thread.sleep(10);

    // check the status of each task
    for (incrementTask t : tasks) {
      assertEquals(t.baseNumber+1, t.finalNumber);
    }
  }

  /**
   * Test execution of the tasks in the thread pool before all the tasks have been added. This is done by first
   * running all the workers in the pool in their own thread before adding tasks to the queue in the thread pool.
   * @throws InterruptedException
   */
  @Test
  public void TestExecutionBeforeAddingTasks() throws InterruptedException {
    int numThreads = 12;
    ThreadPool threadPool = new ThreadPool(numThreads);
    threadPool.startWorkers();

    incrementTask[] tasks = createIncrementTasks();
    for (incrementTask t : tasks) threadPool.addTask(t);

    while (!threadPool.isDone()) ; // wait for all tasks to be completed
    Thread.sleep(10);

    // check the status of each task
    for (incrementTask t : tasks) {
      assertEquals(t.baseNumber+1, t.finalNumber);
    }
  }

  /**
   * Test task execution while adding new tasks. This is done by adding a set of tasks to the task queue in the
   * thread pool before starting the worker threads. After the worker threads have been started and are working on
   * the current tasks in the queue, add more tasks to this queue to see if the worker threads are able to
   * successfully execute new incoming tasks. Test passes if all the tasks have completed successfully.
   * @throws InterruptedException
   */
  @Test
  public void TestExecutionDuringAddingTasks() throws InterruptedException {
    int numThreads = 12;
    int size = 8192;
    ThreadPool threadPool = new ThreadPool(numThreads);

    ArrayList<incrementTask> tasks = null;

    tasks = new ArrayList<>();
    for (int i=0; i<size; ++i) tasks.add(new incrementTask());
    for (incrementTask t : tasks) threadPool.addTask(t);

    threadPool.startWorkers();  // start the worker threads

    ArrayList<incrementTask> oldTasks = tasks;

    tasks = new ArrayList<>();
    for (int i=0; i<size; ++i) tasks.add(new incrementTask());
    for (incrementTask t : tasks) threadPool.addTask(t);


    while (!threadPool.isDone()) ; // wait for all tasks to be completed
    Thread.sleep(100);

    // Check the final state of the tasks
    for (incrementTask t : oldTasks) assertEquals(t.baseNumber+1, t.finalNumber);
    for (incrementTask t : tasks) assertEquals(t.baseNumber+1, t.finalNumber);
  }

  /**
   *
   * @throws InterruptedException
   */
  @Test
  public void TestExecutionWithDifferentTypesOfTask() throws InterruptedException {
    int numThreads = 12;
    int size = 8192;
    ThreadPool threadPool = new ThreadPool(numThreads);

    ArrayList<incrementTask> incrementTasks = new ArrayList<incrementTask>();
    for (int i=0; i<size; ++i) incrementTasks.add(new incrementTask());
    for (incrementTask t : incrementTasks) threadPool.addTask(t);

    threadPool.startWorkers();  // start the worker threads

    ArrayList<halfTask> halfTasks = new ArrayList<>();
    for (int i=0; i<size; ++i) halfTasks.add(new halfTask());
    for (halfTask t : halfTasks) threadPool.addTask(t);


    while (!threadPool.isDone()) ; // wait for all tasks to be completed
    Thread.sleep(100);

    // Check the final state of the tasks
    for (halfTask t : halfTasks) assertEquals(t.baseNumber/2, t.finalNumber);
    for (incrementTask t : incrementTasks) assertEquals(t.baseNumber+1, t.finalNumber);
  }

}
