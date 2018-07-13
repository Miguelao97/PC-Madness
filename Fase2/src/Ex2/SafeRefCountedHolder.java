package Ex2;

import java.util.concurrent.atomic.AtomicInteger;

public class SafeRefCountedHolder<T> {

    private T value;
    private AtomicInteger refCount;

    public SafeRefCountedHolder(T value){
        this.value = value;
        this.refCount = new AtomicInteger(1);
    }

    public void addRef(){
        Integer p;
        do {
            p = this.refCount.get();
            if(p == 0){
                throw new IllegalStateException();
            }
        }while (!this.refCount.compareAndSet(p, p +1));
    }

    public void releaseRef(){
        Integer p;
        do {
            p = this.refCount.get();
            if(p == 0){
                throw new IllegalStateException();
            }
            if(this.refCount.decrementAndGet() == 0){
                value = null;
                break;
            }
        }while (!this.refCount.compareAndSet(p, p));
    }

    public T getValue(){
        Integer i;
        do {
            if((i = this.refCount.get()) == 0) throw new IllegalStateException();
        } while (!this.refCount.compareAndSet(i, i));
        return this.value;
    }

}
