package edu.kit.kastel.vads.compiler.backend.custom;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

/**
 * Describes Space on the stack
 */
public class StackRegister implements Register {

    private final int offset;

    public StackRegister(int offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "-" + offset + "(%rbp)";
    }
}
