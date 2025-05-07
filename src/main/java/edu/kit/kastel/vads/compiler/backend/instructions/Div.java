package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.custom.InfiniteRegister;
import edu.kit.kastel.vads.compiler.backend.custom.StandardRegister;
import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public class Div extends AsInstruction {

    private final Register divisor;

    public Div(Register divisor) {
        this.divisor = divisor;
    }

    @Override
    public String toString() {
        return String.format("div %s", divisor.toString());
    }

    public Register getDestination() {
        return new StandardRegister("%eax", false);
    }
    public Register getSource() {
        return divisor;
    }
}
