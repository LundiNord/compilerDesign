package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import org.jspecify.annotations.Nullable;

/**
 * Jump if Equal
 */
public class Je extends AsInstruction {
    private final Label label;

    public Je(Label label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "je " + label.getName();
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

    public void changeSource(Register reg) {
        throw new UnsupportedOperationException("Cannot change source of Je");
    }
}
