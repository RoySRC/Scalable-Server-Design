package cs455.scaling.task;

import cs455.scaling.threadpool.Task;
import cs455.scaling.util.LOGGER;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class is not meant to be used directly. This class is only meant to be used by the {@link BatchTaskManager}.
 * This class contains a list of tasks that are to be executed when the batch task manager sends an instance of this
 * class to the thread pool. The set of tasks can include either the client registration task or the read and respond
 * task, or any kind of task as long as it implements the @{@link Task} interface.
 */

public class BatchTask implements Task{

  // for logging
  private static final LOGGER log = new LOGGER(BatchTask.class.getSimpleName(), false);

  private final LinkedBlockingQueue<Task> tasks;
  private final int maxSize;

  public BatchTask(int maxSize) {
    this.maxSize = maxSize;
    this.tasks = new LinkedBlockingQueue<>();
  }

  public int size() {
    return this.tasks.size();
  }

  public int maxSize() {
    return this.maxSize;
  }

  public void addTask(Task t) throws InterruptedException {
    this.tasks.put(t);
  }

  @Override
  public void execute() {
    log.info("Total tasks in this batch: "+ tasks.size());
    for (Task t : tasks) {
      t.execute();
    }
  }
}
