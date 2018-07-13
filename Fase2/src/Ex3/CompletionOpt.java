package Ex3;

import Ex1.ConcurrentQueue;
import Utils.TimeoutHolder;


public class CompletionOpt {

    // Considering a correct implementation of ConcurrentQueue!!! <--
    private ConcurrentQueue<State> wq = new ConcurrentQueue<>();

    private static class State{
        private volatile boolean finished = false;

        private boolean isFinished() {
            return finished;
        }

        private void setFinished() {
            this.finished = true;
        }
    }

    public void complete() {
        if (wq.isEmpty()) return;
        wq.tryTake().setFinished();
    }

    public boolean waitForCompletion(long millisTimeout) throws InterruptedException {
        if(wq.isEmpty()) return true;

        if(millisTimeout == 0 ) return false;
        State s = new State();
        wq.put(s);
        TimeoutHolder th = new TimeoutHolder(millisTimeout);
        do {
            if (th.value() == 0 ) return false;
        }while (!s.isFinished());
        return true;
    }

    public void completeAll() {
        if (wq.isEmpty()) return;
        while (!wq.isEmpty()) wq.tryTake().setFinished();
    }
}
