package Ex1;

import Utils.TimeoutHolder;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Completion implements ICompletion {

    private Lock mLock;
    private Condition cd;
    private List<State> wq = new LinkedList<>();

    private static class State{
        private boolean finished = false;

        private boolean isFinished() {
            return finished;
        }

        private void setFinished() {
            this.finished = true;
        }
    }

    public Completion(){
        this.mLock = new ReentrantLock();
        this.cd = mLock.newCondition();
    }

    @Override
    public void complete() {
        mLock.lock();
        try {
            if(wq.size() < 1) return;
            wq.remove(0).setFinished();
            this.cd.signal();
        }finally {
            mLock.unlock();
        }
    }

    @Override
    public boolean waitForCompletion(long millisTimeout) throws InterruptedException {
        mLock.lock();
        try {
            if(millisTimeout <= 0L){
                return false;
            }
            State s = new State();
            wq.add(s);
            TimeoutHolder th = new TimeoutHolder(millisTimeout);
            long value;
            do {
                try {
                    if((value = th.value() ) > 0L){
                        this.cd.await(value,TimeUnit.MILLISECONDS);
                    }
                    else {
                        this.cd.await();
                    }
                }catch (InterruptedException ie){
                    if(s.isFinished()){
                        return true;
                    }
                    if(th.value() <= 0L){
                        return false;
                    }
                    throw ie;
                }
            } while (!s.isFinished());
            return true;
        }finally {
            mLock.unlock();
        }
    }

    @Override
    public void completeAll() {
        mLock.lock();
        try {
            if(wq.size() < 1) return;
            for (int i = 0; i < wq.size(); i++){
                wq.remove(i).setFinished();
            }
            this.cd.signalAll();
        }finally {
            mLock.unlock();
        }
    }
}
