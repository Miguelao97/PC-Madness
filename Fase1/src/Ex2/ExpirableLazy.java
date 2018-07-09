package Ex2;

import Utils.TimeoutHolder;

import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class ExpirableLazy<T> {

    private Lock mLock;
    private Condition cd;
    private State state = State.NOT_CALCULATED;

    private final Supplier<T> provider;
    private final TimeoutHolder timeToLive;
    private T value;

    private enum State{
        NOT_CALCULATED,
        CALCULATING,
        FAILED_CALC,
        PRESENT}

    public ExpirableLazy (Supplier<T> provider, Period timeToLive){
        this.mLock = new ReentrantLock();
        this.cd = mLock.newCondition();
        this.provider = provider;
        this.timeToLive = new TimeoutHolder(timeToLive.get(ChronoUnit.MILLIS));
    }

    public T getValue() throws InterruptedException {

        mLock.lock();
        try{
            if(value == null || timeToLive.value() <= 0L) {
                boolean awaited = false;
                if (state.equals(State.CALCULATING)) {
                    //await
                    awaited = true;
                    try {
                        long valueTime;
                        if ((valueTime = timeToLive.value()) > 0) {
                            this.cd.await(valueTime, TimeUnit.MILLISECONDS);
                        } else {
                            this.cd.await();
                        }
                    } catch (InterruptedException ex) {
                        throw ex;
                    }
                }

                if (state.equals(State.FAILED_CALC ) || !awaited) {
                    this.state = State.CALCULATING;
                    try {
                        this.value = provider.get();
                        this.state = State.PRESENT;
                        this.cd.signalAll();
                    } catch (Exception e) {
                        this.state = State.FAILED_CALC;
                        cd.signal();
                        throw e;
                    }
                }
            }
            return this.value;
        }finally {
            mLock.unlock();
        }


    }
}
