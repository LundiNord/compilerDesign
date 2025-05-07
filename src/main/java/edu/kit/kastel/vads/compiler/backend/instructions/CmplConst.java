package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import org.jspecify.annotations.Nullable;

/**
 * Compare and set zero flag if equal
 */
public class CmplConst extends AsInstruction {
    private final int value;
    private final Register register;

    public CmplConst(int value, Register register) {
        this.value = value;
        this.register = register;
    }

    @Override
    public String toString() {
        return "cmpl $" + value + ", " + register;
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
}