package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public class Div implements AsInstruction {

    private final Register divisor;

    public Div(Register divisor) {
        this.divisor = divisor;
    }

    @Override
    public String toString() {
        return String.format("div %s", divisor.toString());
    }
}
