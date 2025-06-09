package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public class And extends BitwiseComp {

    public And(Register destination, Register source) {
        super(destination, source);
    }

    @Override
    public String toString() {
        return String.format("and %s, %s", source.toString(), destination.toString());
    }

}
