package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import org.jspecify.annotations.Nullable;

/**
 * Jump if Not Equal
 */
public class Jne extends AsInstruction {
    //https://www.aldeid.com/wiki/X86-assembly/Instructions/jnz

    private final Label label;

    public Jne(Label label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "jne " + label.getName();
    }

    @Override
    @Nullable
    public Register getDestination() {
        return null;
    }

    @Override
    @Nullable
    public Register getSource() {
        return null;
    }
}