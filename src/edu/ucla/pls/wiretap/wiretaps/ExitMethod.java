package edu.ucla.pls.wiretap.wiretaps;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import edu.ucla.pls.wiretap.EventType;
import edu.ucla.pls.wiretap.EventType.Emitter;
import edu.ucla.pls.wiretap.Method;
import edu.ucla.pls.wiretap.Wiretapper;

public class ExitMethod extends Wiretapper {

  EventType exit = declareEventType("exit", int.class);

  @Override
  public Wiretap createWiretap(MethodVisitor next,
                               final MethodVisitor out,
                               final Method method) {
    final Emitter exit = this.exit.getEmitter(out);
    return new Wiretap(next) {
      private final Label
        start = new Label(),
        end = new Label();

      @Override
      public void visitCode() {
        out.visitTryCatchBlock(start, end, end, null);
        out.visitLabel(start);
        super.visitCode();
      }

      @Override
      public void visitMaxs(int mStack, int mLocals) {
        out.visitLabel(end);
        exit.emit(method.getId());

        // Rethrow the exception
        out.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Throwable" );
        out.visitInsn(Opcodes.ATHROW);

        super.visitMaxs(mStack, mLocals);
      }
    };
  }
}
