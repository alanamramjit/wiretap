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
  private static final ArrayList<Thread> threads = new ArrayList<Thread>();
  private static Thread wakeupthread;
  private static int permSize;

  private final int id;
  private int order;

  public ABCDReplayer (int id) {
    this.id = id;
    this.order = order;
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

  public static int liveCount() {
    synchronized(threads) {
      int count = 0;
      for (Thread thread : threads) {
        Thread.State s = thread.getState();
        if (s == Thread.State.RUNNABLE) count += 1;
      }
      return count;
    }
  }

  public static void setupRecorder(final WiretapProperties properties) {
    readReplayFile(properties.getReplayFile());
    wakeupthread = new Thread (new Runnable () {
        Event lastTop = null;
        public void run() {
          boolean dangerzone = false;
          try {
            while (true) {
              Thread.sleep(100);
              synchronized (permQueue) {
                if (lastTop != top) {
                  lastTop = top;
                } else if (liveCount() == 0) {
                  if (!dangerzone) {
                    permQueue.notifyAll();
                    dangerzone = true;
                  } else {
                    System.err.println("- Timeout due to inactivity -- " + top + "@"
                                       + (permSize - permQueue.size() + 1));
                    Agent.v().halt(-17);
                  }
                }
              }
            }
          } catch (InterruptedException e) {}
        }
      });
    wakeupthread.setDaemon(true);
    wakeupthread.start();
  }

  public synchronized static void closeRecorder() throws IOException {
    wakeupthread.interrupt();
  }

  private static Event top = null;
  private static final LinkedList<Event> permQueue = new LinkedList<Event>();

  public static void pollEvent() {
    synchronized (permQueue) {
      top = permQueue.pollFirst();
    }
  }

  public static void readReplayFile(File file) {
    FileReader fr = null;
    BufferedReader br = null;
    try {
      fr = new FileReader(file);
      br = new BufferedReader(fr);
      String line = null;
      while ((line = br.readLine()) != null) {
        permQueue.add(Event.fromLine(line));
      }
      permSize = permQueue.size();
      pollEvent();
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

  public void printError(String msg) {
    System.err.println(msg +
                       " in " + id + ":" + order + " -- " + top + "@" + (permSize - permQueue.size() + 1));
    Agent.v().halt(-17);
  }
  // }

  public boolean isTerminated(int tid) {
    synchronized (threads) {
      return threads.size() > tid && threads.get(tid).getState() == Thread.State.TERMINATED;
    }
  }

  public void waitForPermission(String msg) {
    synchronized (permQueue) {
      try {
        while (true) {
          if (top == null) {
            printError("- Nothing in Queue: " + msg);
          } else if (top.thread == id) {
            if (top.order >= this.order || top.order == -1) {
              break;
            } else {
              printError("- Not supposed to happen");
            }
          } else if (isTerminated(top.thread)) {
            // System.out.println("skipping: " + id + " " + top);
            pollEvent();
            continue;
          } else {
            permQueue.wait();
          }
        }
      } catch (InterruptedException e) {
        System.err.println("- Thread interrupted: " + id);
        Agent.v().halt(-2);
      }
    }
    // Increment order
    order++;
  }

  public void givePermission() {
    synchronized (permQueue) {
      if (top.order < order) {
          pollEvent();
      }
      permQueue.notifyAll();
    }
  }

  public final void request (Object l, int inst) {
    waitForPermission("acquire");
  }

  public final void acquire (Object l, int inst) {
    givePermission();
  }

  public final void join(Thread o, int inst) {
    synchronized (permQueue) {
      ABCDReplayer r = getRecorderFromThread(o);
      if (top.thread == r.id) {
        pollEvent();
      }
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

  public static class Event {
    public final int thread;
    public final int order;
    public final String msg;

    public Event (int thread, int order, String msg) {
      this.thread = thread;
      this.order = order;
      this.msg = msg;
    }

    public static Event fromLine(String line) {
      String [] words = line.split(" ", 3);
      return new Event(Integer.parseInt(words[0]),
                       Integer.parseInt(words[1]),
                       words[2]);
    }

    public String toString() {
      return "Event(" + thread + " " + order + " " + msg + ")";
    }
  }
}
