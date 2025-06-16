package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import org.jspecify.annotations.Nullable;

/**
 * Compare and set zero flag if equal
 */
public class Cmpl extends AsInstruction {
    private final Register register1;
    private final Register register;

    public Cmpl(Register register1, Register register) {
        this.register1 = register1;
        this.register = register;
    }

    @Override
    public String toString() {
        return "cmpl " + register1 + ", " + register;
    }

    @Override
    @Nullable
    public Register getDestination() {
        return null;
    }

    @Override
    @Nullable
    public Register getSource() {
        return register;
    }

    public void changeSource(Register reg) {
        throw new UnsupportedOperationException("Cannot change source of CmplConst");
    }
}
