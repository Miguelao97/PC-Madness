package Ex4;

import Utils.TimeoutHolder;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleThreadPoolExecutor  implements ISimpleThreadPoolExecutor{

    private final int maxPoolSize;
    private final long time;

    private final ThreadPoolExecutor threadPool;

    private final State state;
    private Lock lock;
    private Condition shutCd;




    public SimpleThreadPoolExecutor (int maxPoolSize, long keepAliveTime){
        this.maxPoolSize = maxPoolSize;
        this.time = keepAliveTime;
        this.lock = new ReentrantLock();
        this.shutCd = this.lock.newCondition();
        this.state = new State();
        this.threadPool = new ThreadPoolExecutor(2,this.maxPoolSize,this.time,TimeUnit.SECONDS, new ArrayBlockingQueue<>(this.maxPoolSize));
    }

    @Override
    public boolean execute(Runnable command, long timeout) throws InterruptedException {
        lock.lock();
        try {
            if(state.isShutdown() || this.state.isShutting()) throw new RejectedExecutionException();

            if(timeout < 0L){
                return false;
            }

            TimeoutHolder th = new TimeoutHolder(timeout);
            long value;
//            do {
//                try {
//
//                }catch (InterruptedException ex){
//
//                }
//
//
//            }while ();
//

        }finally {
            lock.unlock();
        }

        //do stuff with threadexecutor...

        return true;
    }

    @Override
    public void shutdown() {
        lock.lock();
        try {
            if(this.state.isShutdown()) return;
            this.state.setShutting();
        }finally {
            lock.unlock();
        }
    }

    @Override
    public boolean awaitTermination(long timeout) throws InterruptedException {
        this.lock.lock();
        try{
            if(this.state.isShutdown())return true;
            if(timeout <= 0L){
                return false;
            }
            TimeoutHolder th = new TimeoutHolder(timeout);
            long value;
            do {
                try {
                    if((value = th.value()) > 0L){
                        this.shutCd.await(value,TimeUnit.MILLISECONDS);
                    }
                    else {
                        return false;
                    }
                } catch (InterruptedException ex) {
                    if(this.state.isShutdown()) return true;
                    throw ex;
                }
            }while (!this.state.isShutdown());
            return true;
        }finally {
            this.lock.unlock();
        }
    }

    private static class State{


        /*
         *
         * 0 0 1 - Running
         * 0 1 0 - Shutting
         * 1 0 0 - Shutdown
         *
         * */

        private byte value = 1;

        boolean isRunning(){
            return (value & ((byte)(1))) != 0;
        }

        boolean isShutdown(){
            return (value & ((byte)(1<<1))) != 0;
        }

        boolean isShutting(){
            return (value & ((byte)(1<<2))) != 0;
        }

        private void setShutting() {
            this.value = (byte) (this.value | (byte) (1<<1));
        }
        private void setShutdown(){
            this.value = (byte) (this.value | (byte) (1<<2));
        }
    }
}
