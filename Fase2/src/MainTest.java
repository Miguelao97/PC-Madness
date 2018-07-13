import Ex1.ConcurrentQueue;

public class MainTest {

    public static void main(String[] args) throws InterruptedException {
        ConcurrentQueue<Integer> list = new ConcurrentQueue<>();

        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        Thread[] threads = new Thread[4];

        Runnable rIn = () -> {
            //        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
            Double value = Math.random()*10;
            System.out.println("Thread with id : " + Thread.currentThread().getId() +" - >Putting value: " + value.intValue());
            list.put(value.intValue());
        };

        Runnable rOut = () -> {
            //        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
            System.out.println("Read value " + list.tryTake() + " on thread " + Thread.currentThread().getId());
        };

        Runnable rEmpty = () -> {
            //        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
            System.out.println("List is empty ? " + list.isEmpty() + " on thread " + Thread.currentThread().getId());

        };

        threads[0] = new Thread(rIn);
        System.out.println(threads[0].getId());
        threads[1] = new Thread(rIn);
        System.out.println(threads[1].getId());
        threads[2] = new Thread(rOut);
        System.out.println(threads[2].getId());
        threads[3] = new Thread(rEmpty);
        System.out.println(threads[3].getId());

        // to see concurrent just uncomment the the values
        for (Thread t:
             threads) {
           t.start();
        }
        System.out.println("Wait for threads");
        Thread.sleep(1000);

    }
}
