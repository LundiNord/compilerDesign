package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public class Mod implements AsInstruction {

    private final Register divisor;

    public Mod(Register divisor) {
        this.divisor = divisor;
    }

    @Override
    public String toString() {
        return String.format("div %s", divisor.toString());
    }

}
