package Ex4;

public interface ISimpleThreadPoolExecutor {
    boolean execute(Runnable command, long timeout) throws InterruptedException;
    void shutdown();
    boolean awaitTermination(long timeout) throws InterruptedException;
}
