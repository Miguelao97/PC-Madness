package Ex3;

import Utils.TimeoutHolder;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class Exchanger<T> {

    private Lock mLock;
    private List<WaitState<T>> states = new LinkedList<>();

    private static class WaitState<T> {
        private boolean finished = false;
        private Condition cd;
        private T valueToExchange;

        WaitState(T value, Condition cd) {
            this.cd = cd;
            this.valueToExchange = value;
        }

        private boolean isFinished() {
            return finished;
        }

        private void setFinished() {
            this.finished = true;
        }

        private void setValueToExchange(T valueToExchange) {
            this.valueToExchange = valueToExchange;
        }

        private T getValueToExchange() {
            return valueToExchange;
        }

        private Condition getCondition() {
            return this.cd;
        }
    }

    public T exchange(T mine, long timeout) throws InterruptedException {
        mLock.lock();
        try {
            if (this.states.size() > 0) {
                WaitState<T> s = this.states.remove(0);
                T res = s.getValueToExchange();
                s.setValueToExchange(mine);
                s.setFinished();
                s.getCondition().signal();
                return res;
            }
            if (timeout <= 0L) return null;


            WaitState<T> myState = new WaitState<>(mine, this.mLock.newCondition());
            this.states.add(myState);
            TimeoutHolder th = new TimeoutHolder(timeout);
            Long value;
            do {
                try {
                    if ((value = th.value()) > 0L) {
                        myState.getCondition().await(value,TimeUnit.MILLISECONDS);
                    } else {
                        myState.getCondition().await();
                    }
                } catch (InterruptedException ie) {
                    if(myState.isFinished()){
                        return myState.getValueToExchange();
                    }
                    this.states.remove(myState);
                    if(th.value() <= 0L) return null;
                    throw ie;
                }
            } while (!myState.isFinished());
            return myState.getValueToExchange();
        } finally {
            mLock.unlock();
        }

    }

}
