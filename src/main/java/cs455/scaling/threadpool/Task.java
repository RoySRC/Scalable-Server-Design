package cs455.scaling.threadpool;

/**
 * This interface should be implemented by any class that wishes to be put in the thread pool.
 */
public interface Task {

  /**
   * This method needs to be implemented by any class that is going to be added to the {@link ThreadPool}. The
   * execute method is what is run by the workers in the thread pool.
   */
  void execute();

}
