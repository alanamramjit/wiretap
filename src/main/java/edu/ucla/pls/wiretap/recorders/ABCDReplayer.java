package edu.ucla.pls.wiretap.recorders;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.ucla.pls.wiretap.Closer;
import edu.ucla.pls.wiretap.DeadlockDetector;
import edu.ucla.pls.wiretap.WiretapProperties;
import edu.ucla.pls.wiretap.utils.ConcurrentOutputStream;
import edu.ucla.pls.wiretap.managers.FieldManager;
import edu.ucla.pls.wiretap.Agent;

public class ABCDReplayer {

  private static final Map<Thread, ABCDReplayer> replayers =
    new ConcurrentHashMap<Thread, ABCDReplayer>();
  private static final LinkedList<Integer> permQueue = new LinkedList<Integer>();
  private static final ArrayList<Thread> threads = new ArrayList<Thread>();
  private static Thread wakeupthread;
  private static int permSize;

  private final int id;

  public ABCDReplayer (int id) {
    this.id = id;
  }
  public static ABCDReplayer getRecorder() {
    return getRecorderFromThread(Thread.currentThread());
  }

  public static ABCDReplayer getRecorderFromThread(Thread thread) {
    ABCDReplayer r = replayers.get(thread);
    if (r == null) {
      synchronized (threads) {
        int id = threads.size();
        r = new ABCDReplayer(id);
        replayers.put(thread, r);
        threads.add(thread);
      }
    }
    return r;
  }

  public static void readReplayFile(File file) {
    FileReader fr = null;
    BufferedReader br = null;
    try {
      fr = new FileReader(file);
      br = new BufferedReader(fr);
      String line = null;
      while ((line = br.readLine()) != null) {
        String [] items = line.split(" ", 2);
        permQueue.add(Integer.parseInt(items[0]));
      }
      permSize = permQueue.size();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (fr != null) fr.close();
        if (br != null) br.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static void setupRecorder(final WiretapProperties properties) {
    System.out.println("setupRecorder");
    readReplayFile(properties.getReplayFile());
    wakeupthread = new Thread (new Runnable () {
        public void run() {
          try {
            while (true) {
              Thread.sleep(1000);
              synchronized (permQueue) {
                permQueue.notifyAll();
              }
            }
          } catch (InterruptedException e) {
            System.err.println("WHYT");
          }
        }
      });
    wakeupthread.setDaemon(true);
    wakeupthread.start();
  }

  public synchronized static void closeRecorder() throws IOException {
    wakeupthread.interrupt();
  }

  public static volatile int counter = 0;

  public void printError(String msg) {
    System.err.println(msg + " in " + id + " @ " + (permSize - permQueue.size() + 1));
    Agent.v().halt(-17);
  }

  public int liveCount() {
    synchronized(threads) {
      int count = threads.size();
      for (Thread thread : threads) {
        // System.err.println("Thread " + thread + " in state " + thread.getState());
        if (thread.getState() == Thread.State.TERMINATED || thread.getState() == Thread.State.WAITING) {
          count = count - 1;
        }
      }
      return count;
    }
  }

  public void waitForPermission(String msg) {
    synchronized (permQueue) {
      try {
        while (true) {
          if (permQueue.isEmpty()) {
            printError("- Nothing in Queue");
          }

          int tid = permQueue.peekFirst();
          //System.err.println(id + " " + tid);

          if (tid == id) {
            break;
          } else if (threads.size() > tid && threads.get(tid).getState() == Thread.State.TERMINATED) {
            printError("- Threads dead " + tid);
          } else {
            if (1 == liveCount()) {
              printError("- All waiting: " + tid);
            }
            permQueue.wait();
          }
        }
      } catch (InterruptedException e) {
        System.err.println("- Thread interrupted: " + id);
        Agent.v().halt(-2);
      }
    }
  }

  public void givePermission() {
    synchronized (permQueue) {
      permQueue.pollFirst(); 
      permQueue.notifyAll();
    }
  }


  public final void fork(Object o, int inst) {
    if (o instanceof Thread) {
      getRecorderFromThread((Thread) o);
    }
  }

  public final void preread () {
    waitForPermission("read");
  }

  public final void read(Object o, int field, int inst) {
    givePermission();
  }

  public final void readarray(Object o, int idx, int inst) {
    givePermission();
  }

  public final void write(Object o, int field, int inst) {
    waitForPermission("write");
  }

  public final void writearray(Object o, int idx, int inst) {
    waitForPermission("wirtearray");
  }

  public final void postwrite () {
    givePermission();
  }


  public final void value(byte v) {}
  public final void value(char v) {}
  public final void value(short v) {}
  public final void value(int v) {}
  public final void value(long v) {}
  public final void value(float v) {}
  public final void value(double v) {}
  public final void value(Object o) {}

}
