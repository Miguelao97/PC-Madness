package Ex1;

public interface ILockFree<T> {

    void put(T value);
    T tryTake();
    boolean isEmpty();

}
