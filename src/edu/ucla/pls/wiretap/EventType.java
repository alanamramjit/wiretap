package edu.ucla.pls.wiretap;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class EventType implements Opcodes{

  private final String methodName;
  private final Class<?>[] types;

  private final Type [] asmTypes;
  private final String signature;

  private String recorder;
  private Type recorderType;


  public EventType(String methodName, Class<?> [] types) {
    this.methodName = methodName;
    this.types = types;

    asmTypes = new Type[types.length];
    for (int i = 0, len = types.length ; i != len; i++) {
      asmTypes[i] = Type.getType(types[i]);
    }
    signature = Type.getMethodDescriptor(Type.VOID_TYPE, asmTypes);
  }

  public void setRecorder(Class<?> recorder) throws NoSuchMethodException {
    // Test that method exists.
    recorder.getMethod(methodName, types);
    this.recorderType = Type.getType(recorder);
    this.recorder = recorderType.getInternalName();
  }

  public Emitter getEmitter(RecorderAdapter out) {
    return new Emitter(out);
  }

  public class Emitter {

    private final RecorderAdapter out;

    public Emitter(RecorderAdapter out) {
      this.out = out;
    }

    public void emit(Object ... args) {

      if (args.length != types.length) {
        throw new IllegalArgumentException("The args needs to be same length " +
                                           " as the designated types");
      }

      // pushRecorder onto the stack
      out.pushRecorder();

      // Record.
      record(args);
    }

    public Type getType(int index) {
      return asmTypes[index];
    }

    public void log(Object... args) {
      checkLength(1, args);
      if (getType(0).getSize() == 2) {
        out.dup2();
      } else {
        out.dup();
      }
      consume(args);
    }

    public void logX1(Object... args) {
      checkLength(1, args);
      if (getType(0).getSize() == 2) {
        out.dup2X1();
      } else {
        out.dupX1();
      }
      consume(args);
    }

    public void logX2(Object... args) {
      checkLength(1, args);
      if (getType(0).getSize() == 2) {
        out.dup2X2();
      } else {
        out.dupX2();
      }
      consume(args);
    }

    public void checkLength(int logged, Object [] args) {
      if (args.length != types.length - logged) {
        throw new IllegalArgumentException("The args needs to be " + logged + " shorter" +
                                           " than the designated types");
      }
    }


    public void consume(Object... args) {
      checkLength(1, args);

      // Push the recorder
      out.pushRecorder();

      // Swap the recorder and the logged object.
      out.swap(getType(0), recorderType);

      // Record args;
      record(args);
    }

    public void consume2(Object... args) {

      checkLength(2, args);

      // Push the recorder
      out.pushRecorder();

      // Swap the recorder and the logged object.
      if (types[0] == Long.TYPE || types[0] == Double.TYPE ||
          types[1] == Long.TYPE || types[1] == Double.TYPE) {
        throw new IllegalArgumentException("Can't handle Long and Double... yet," +
                                           " so you need to do this by hand");
      } else {
        out.dupX2();
        out.pop();
      }

      // Record args;
      record(args);
    }

    /**
     * record the current stack to the recorder. This method assumes that objects
     * not in the constants given to the method is already on the stack. Record
     * will therefor check that the constants matches the types from the end.
     *
     */
    public void record(Object... args) {

      final int len = args.length;
      final int diff = types.length - len;
      // Add constants
      for (int i = 0; i != len; ++i) {
        if (primitiveTypeCheck(types[i + diff], args[i])) {
          if (args[i] == null) {
            out.visitInsn(ACONST_NULL);
          } else {
            out.visitLdcInsn(args[i]);
          }
        } else {
          throw new IllegalArgumentException("The arg " + i + " is of the wrong type");
        }
      }

      // Record.
      out.visitMethodInsn(INVOKEVIRTUAL,
                          recorder, methodName, signature,
                          false);
    }

    private boolean primitiveTypeCheck(Class<?> c, Object o) {
      if (o instanceof Integer) {
        return c == Integer.TYPE;
      } else if (o instanceof Byte) {
        return c == Byte.TYPE;
      } else if (o instanceof Character) {
        return c == Character.TYPE;
      } else if (o instanceof Float) {
        return c == Float.TYPE;
      } else if (o instanceof Long) {
        return c == Long.TYPE;
      } else if (o instanceof Double) {
        return c == Double.TYPE;
      } else if (o instanceof String) {
        return c == String.class;
      } else if (o instanceof Short) {
        return c == Short.TYPE;
      } else if (o instanceof Boolean) {
        return c == Boolean.TYPE;
      } else if (o == null) {
        return c == Object.class;
      } else {
        throw new IllegalArgumentException("Only Constants allowed");
      }
    }
  }
}
