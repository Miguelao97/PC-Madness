package Ex1;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class ConcurrentQueue<T> implements ILockFree<T> {

    private AtomicInteger iLast = new AtomicInteger(0);
    private AtomicInteger iFirst = new AtomicInteger(0);
    private AtomicReferenceArray<T> values = new AtomicReferenceArray<T>(Integer.MAX_VALUE / 1000000 );

    @Override
    public void put(T value) {
        T t;
        Integer idxLast;
        do {
             idxLast = iLast.get();
             t = values.get(idxLast);
        } while (!iLast.compareAndSet(idxLast, idxLast + 1) || !values.compareAndSet(idxLast,t,value));
    }

    @Override
    public T tryTake() {
        T t;
        Integer idxFirst;
        do {
            idxFirst = iFirst.get();
            if(idxFirst == iLast.get()) return null;
            t = values.get(idxFirst);
        } while (!iFirst.compareAndSet(idxFirst, idxFirst + 1) || !values.compareAndSet(idxFirst,t,null));
        return t;
    }

    @Override
    public boolean isEmpty() {
        return iLast.get() == iFirst.get();
    }
}
