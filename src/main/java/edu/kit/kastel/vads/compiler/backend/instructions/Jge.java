package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import org.jspecify.annotations.Nullable;

/**
 * Jump if Less
 * <a href="https://stackoverflow.com/questions/9617877/assembly-jg-jnle-jl-jnge-after-cmp">Link</a>
 */
public class Jge extends AsInstruction {
    private final Label label;

    public Jge(Label label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "jge " + label.getName();
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
        throw new UnsupportedOperationException("Cannot change source of Jge");
    }
}
