package Ex1;

public interface ICompletion {
    void complete();
    boolean waitForCompletion(long millisTimeout) throws InterruptedException;
    void completeAll();
}
