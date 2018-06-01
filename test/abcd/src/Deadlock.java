public class Deadlock extends Thread { 
    static int x = 0;
    //static int y = 0;

    int r;
    static Object filesystem;
    static Object network;

    static boolean preventDeadlock = false;

    public Deadlock(int r) {
        this.r = r;
    }

    public void run () {
        if (r == 0) {
            preventDeadlock = true;
        }
        else if (r == 1) {
            synchronized (filesystem) {
                openFS();
                synchronized (network) {
                    openNetwork();
                }
            }
        } else if (r == 2) {
            if (!preventDeadlock) {
                synchronized (network) {
                    closeNetwork();
                    synchronized (filesystem) {
                        closeFS();
                    }
                }
            }
            else {
                synchronized (filesystem) {
                    closeFS();
                    synchronized (network) {
                        closeNetwork();
                        closeNetwork();
                    }
                }
            }
        }
    }

    public void closeNetwork() {
        x--;
    }
    public void closeFS() {
        x--;
    }

    public void openNetwork() {
        x++;
    }

    public void openFS() {
        x++;
    }

    public void sleep() {
        try { Thread.sleep(1); } catch (InterruptedException e) {}
    }

    public static void main(String[] args) {
        filesystem = new Object();
        network = new Object();

        // Prevent deadlock
        Thread t3 = new Deadlock(0);
        t3.start();

        Thread t1 = new Deadlock(1);
        Thread t2 = new Deadlock(2);

        t1.start();
        t2.start();

        assert(x == 0);

    }
}
