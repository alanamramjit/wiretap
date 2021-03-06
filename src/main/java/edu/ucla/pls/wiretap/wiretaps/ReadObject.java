package edu.ucla.pls.wiretap.wiretaps;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import edu.ucla.pls.wiretap.EventType;
import edu.ucla.pls.wiretap.EventType.Emitter;
import edu.ucla.pls.wiretap.RecorderAdapter;
import edu.ucla.pls.wiretap.ValueWiretapper;
import edu.ucla.pls.wiretap.managers.Field;

public class ReadObject extends ValueWiretapper {

  EventType preread = declareEventType("preread");
  EventType read = declareEventType("read", Object.class, int.class, int.class);
  EventType readarray = declareEventType("readarray", Object.class, int.class, int.class);

  @Override
  public Wiretap createWiretap(MethodVisitor next,
                               final RecorderAdapter out,
                               final ValueEmitter value) {
    final Emitter read = this.read.getEmitter(out);
    final Emitter preread = this.preread.getEmitter(out);
    final Emitter readarray = this.readarray.getEmitter(out);
    return new Wiretap(next) {

			@Override
      public void visitInsn(int opcode) {

        if (opcode == Opcodes.AALOAD) {

          preread.emit();
          // Copy array and index
          out.dup2();

          super.visitInsn(opcode);

          // Copy it up 2 in the stack;
          out.dupX2();
          // consume it;
          value.vObject.consume();
          // Consume the array and index
          readarray.consume2(createInstructionId());
        } else {
          super.visitInsn(opcode);
        }

      }

      @Override
      public void visitFieldInsn(int opcode,
                                 String owner,
                                 String name,
                                 String desc) {

        if (desc.charAt(0) == 'L' || desc.charAt(0) == '[') {
          Field f = getField(owner, name, desc);
          switch (opcode) {

          case GETSTATIC:
            preread.emit();
            // Log the written value. Ignore everything else on the stack.
            super.visitFieldInsn(opcode, owner, name, desc);
            value.vObject.log();
            read.emit(null, f.getId(), createInstructionId());
            return;

          case GETFIELD:
            preread.emit();
            // Copy object on the stack.  Object -> Object, Object
            out.dup();
            // Fetch value -> Object, Value
            super.visitFieldInsn(opcode, owner, name, desc);
            out.dupX1();
            // Consume value Object, Value -> Value, Object, Value
            value.vObject.consume();
            // Consume the object
            read.consume(f.getId(), createInstructionId());
            return;
          }
        }
        super.visitFieldInsn(opcode, owner, name, desc);
      }
    };
  }
}
