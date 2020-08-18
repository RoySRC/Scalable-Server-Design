import cs455.scaling.task.*;
import cs455.scaling.threadpool.Task;
import cs455.scaling.threadpool.ThreadPool;
import cs455.scaling.util.Util;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class BatchTaskManagerTest {

  class divideTask implements Task {
    private int actual = Util.randInt();
    private int expected = actual/2;

    @Override
    public void execute() {
      actual /= 2;
    }
  }

  @Test
  public void TestBatchSize() throws InterruptedException {
    int timeout = 5000; // milliseconds
    int maxSize = 50;
    ThreadPool threadPool = new ThreadPool(12);
    threadPool.startWorkers();
    BatchTaskManager batchTaskManager = new BatchTaskManager(maxSize, timeout, threadPool);
    batchTaskManager.START();

    for (int i=0; i<20; ++i)
      batchTaskManager.addTask(new divideTask());

    Thread.sleep(120);

    assertEquals(20, batchTaskManager.size());
  }

  @Test
  public void TestDispatchToThreadPoolOnBatchSize() throws InterruptedException {
    int timeout = 1; // seconds
    int maxSize = 50;
    ThreadPool threadPool = new ThreadPool(12);
    threadPool.startWorkers();
    BatchTaskManager batchTaskManager = new BatchTaskManager(maxSize, timeout, threadPool);
    batchTaskManager.START();

    ArrayList<divideTask> tasks = new ArrayList<>();
    for (int i=0; i<maxSize; ++i) {
      tasks.add(new divideTask());
      batchTaskManager.addTask(tasks.get(i));
    }

    while (!batchTaskManager.getThreadPool().isDone()) ;
    Thread.sleep(10);

    batchTaskManager.shutdown();
    threadPool.shutdown();

    for (divideTask t : tasks) {
      assertEquals(t.expected, t.actual);
    }
  }

  @Test
  public void TestDispatchToThreadPoolOnBatchTimer() throws InterruptedException {
    int timeout = 3; // seconds
    int maxSize = 50;
    ThreadPool threadPool = new ThreadPool(12);
    threadPool.startWorkers();
    BatchTaskManager batchTaskManager = new BatchTaskManager(maxSize, timeout, threadPool);
    batchTaskManager.START();

    ArrayList<divideTask> tasks = new ArrayList<>();
    for (int i=0; i<maxSize/2; ++i) {
      tasks.add(new divideTask());
      batchTaskManager.addTask(tasks.get(i));
    }

    Thread.sleep(120);

    assertEquals(maxSize/2, batchTaskManager.size());

    System.out.println("Testing thread Sleeping...");
    Thread.sleep(6000);
    System.out.println("Testing thread woke up...");

    batchTaskManager.shutdown();
    threadPool.shutdown();

    for (divideTask t : tasks) {
      assertEquals(t.expected, t.actual);
    }
  }

}
