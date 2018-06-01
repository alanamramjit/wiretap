public class LockTest extends Thread {
    public static String result = "";
    public String name;

    public LockTest (String name) {
        this.name = name;
    }

    public void run () {
      try { Thread.sleep(1); } catch (InterruptedException e){ }
      synchronized (LockTest.class) {
        result += name;
      }
    }

    public static void main(String [] args) throws InterruptedException {
        Thread [] ts = new Thread [] { new LockTest("A"), new LockTest("B")};
        for (Thread t : ts) t.start();
        for (Thread t : ts) t.join();
        assert (result == "BA");
    }

}

