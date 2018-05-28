public class BetterTest extends Thread { 
    static int x = 0;
    //static int y = 0;

    public void run () {
       if (x == 0) {
           x++;
           try { Thread.sleep(1); } catch (InterruptedException e) { 
           }
           x--;
       }
    }

    public static void main(String[] args) {
        Thread[] threads = new Thread [100];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new BetterTest();
        }

        for (Thread t : threads) { 
            t.start();
        }

        assert(x == 0);

    }
}
