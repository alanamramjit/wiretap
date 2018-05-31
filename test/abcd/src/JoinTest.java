public class JoinTest extends Thread { 
    static int x = 0;
    boolean r;

    static int y = 0;
    static int z = 0;
    static int w = 0;

    static Object o;

    public JoinTest(boolean r) {
        this.r = r;
    }

    public void run () {
        if (r) {
            x++;
        }
        if (x == 30) {
            if (y == 0) {
                sleep();
                y++;
            } else if (w == 0) {
                sleep();
                w++;
            } else if (z == 0)  {
                sleep();
                z++;
            }

            sleep();
            synchronized(o) {
                if (y > 0) {
                    sleep();
                    y--;
                }
                if (z > 0) {
                    sleep();
                    z--;
                }
                if (w > 0) {
                    sleep();
                    w--;
                }
            }
        }
    }

    public void sleep() { try {Thread.sleep(1);} catch (InterruptedException e) { } }

    public static void main(String[] args) {
        Thread[] threads = new Thread [30];
        o = new Object();

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new JoinTest(true);
        }

        for (Thread t : threads) { 
            t.start();
        }

        for (Thread t : threads) {
            try {t.join();} catch (InterruptedException e) {}
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new JoinTest(false);
        }

        for (Thread t : threads) { 
            t.start();
        }

        for (Thread t : threads) {
            try {t.join();} catch (InterruptedException e) {}
        }

        assert(x == 30 && y == 0 && z == 0 && w == 0);

    }
}
