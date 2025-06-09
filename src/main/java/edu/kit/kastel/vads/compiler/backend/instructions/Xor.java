package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public class Xor extends BitwiseComp {

    public Xor(Register destination, Register source) {
        super(destination, source);
    }

    @Override
    public String toString() {
        return String.format("xor %s, %s", source.toString(), destination.toString());
    }

}
