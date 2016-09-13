package edu.ucla.pls.wiretap.managers;

import java.util.Objects;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class Method extends Managable<String> {
  private final int access;
  private final String owner;
  private final String name;
  private final String desc;
  private final String [] exceptions;

  private final String descriptor;

  public Method (int access,
                 String owner,
                 String name,
                 String desc,
                 String [] exceptions) {
    this.access = access;
    this.owner = owner;
    this.name = name;
    this.desc = desc;
    this.exceptions = exceptions;

    this.descriptor = MethodManager.getMethodDescriptor(owner, name, desc);
  }

  public String getOwner () {
    return owner;
  }

  public String getDescriptor() {
    return descriptor;
  }

  public boolean isSynchronized() {
    return (access & Opcodes.ACC_SYNCHRONIZED) != 0;
  }

  public boolean isStatic() {
    return (access & Opcodes.ACC_STATIC) != 0;
  }

  public Type[] getArgumentTypes () {
    return Type.getArgumentTypes(desc);
  }

  public int getNumberOfArgumentLocals() {
    return getArgumentTypes().length + (isStatic() ? 1: 0);
  }

}
