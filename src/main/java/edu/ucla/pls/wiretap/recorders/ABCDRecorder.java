package edu.ucla.pls.wiretap.recorders;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
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
import edu.ucla.pls.wiretap.managers.InstructionManager;
import edu.ucla.pls.wiretap.Agent;

public class ABCDRecorder {

  private static final Map<Thread, ABCDRecorder> recorders =
    new ConcurrentHashMap<Thread, ABCDRecorder>();

  private static OutputStream globalWriter;
  private static FieldManager fm;
  private static InstructionManager im;
  private static final AtomicInteger recorderId = new AtomicInteger();

  private final int id;
  private final OutputStream out;
  private int order;

  public ABCDRecorder (int id, OutputStream s) {
    this.id = id;
    this.out = s;
    this.order = order;
  }
  public static ABCDRecorder getRecorder() {
    return getRecorderFromThread(Thread.currentThread());
  }

  public static ABCDRecorder getRecorderFromThread(Thread thread) {
    ABCDRecorder r = recorders.get(thread);
    if (r == null) {
      int id = recorderId.getAndIncrement();
      r = new ABCDRecorder(id, globalWriter);
      recorders.put(thread, r);
    }
    return r;
  }

  public static void setupRecorder(final WiretapProperties properties) {
    System.out.println("setupRecorder");
    fm = Agent.v().getFieldManager();
    im = Agent.v().getInstructionManager();
    // mm = Agent.v().getMethodManager();
    try {
      File instfile = new File(properties.getOutFolder(), "history.log");
      OutputStream s = new FileOutputStream(instfile);
      globalWriter = new ConcurrentOutputStream(new BufferedOutputStream(s, 32768));
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  public void log (String s, int inst) {
    try {
      out.write((id + " " + order++ + " " + s + " " + value
                 + " -- " + im.get(inst) +"\n").getBytes());
    } catch (IOException e) {
    }
  }

  public synchronized static void closeRecorder() throws IOException {
    Closer.close("the global writer", globalWriter, 100);
  }

  private Object value = null;
  private static final Lock rwlock = new ReentrantLock();

  public final void join(Thread o, int inst) {
    ABCDRecorder r = getRecorderFromThread(o);
    try {
      out.write((r.id + " " + -1 + " join from " + id + " -- " + im.get(inst) +"\n").getBytes());
    } catch (IOException e) {
    }
  }

  public final void acquire (Object l, int inst) {
    log("acquire " + l, inst);
  }

  public final void preread () {
    rwlock.lock();
  }

  public final void read(Object o, int field, int inst) {
    if (field < 0) {
      field = fm.check(field);
    }
    log("read " + fm.get(field), inst);
    rwlock.unlock();
  }


  public final void write(Object o, int field, int inst) {
    rwlock.lock();
    if (field < 0) {
      field = fm.check(field);
    }
    log("write " + fm.get(field), inst);
  }

  public final void postwrite () {
    rwlock.unlock();
  }

  public final void writearray(Object o, int idx, int inst) {
    rwlock.lock();
    log("write " + "[" + idx + "]", inst);
  }

  public final void readarray(Object o, int idx, int inst) {
    log("read " + "[" + idx + "]", inst);
    rwlock.unlock();
  }

  public final void value(byte v) { value = v; }
  public final void value(char v) { value = v; }
  public final void value(short v) { value = v; }
  public final void value(int v) { value = v; }
  public final void value(long v) { value = v; }
  public final void value(float v) { value = v; }
  public final void value(double v) { value = v; }
  public final void value(Object o) { value = o; }

}
