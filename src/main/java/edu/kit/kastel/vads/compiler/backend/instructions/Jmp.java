package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import org.jspecify.annotations.Nullable;

public class Jmp extends AsInstruction {
    private final Label label;

    public Jmp(Label label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "jmp " + label.getName();
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