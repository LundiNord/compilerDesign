package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public class Or extends BitwiseComp {

    public Or(Register destination, Register source) {
        super(destination, source);
    }

    @Override
    public String toString() {
        return String.format("or %s, %s", source.toString(), destination.toString());
    }

}
