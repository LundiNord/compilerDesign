package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import org.jspecify.annotations.Nullable;

import java.lang.classfile.Instruction;

public class Sarl extends AsInstruction {

    protected final Register destination;

    public Sarl(Register destination) {
        this.destination = destination;
    }

    @Override
    public String toString() {
        return String.format("sarl %%cl, %s", destination.toString());
    }

    public Register getDestination() {
        return destination;
    }
    @Nullable
    public Register getSource() {
        return destination;
    }
    public void changeSource(Register reg) {
        throw new UnsupportedOperationException("Sarl does not have a source register " + reg);
    }
}
