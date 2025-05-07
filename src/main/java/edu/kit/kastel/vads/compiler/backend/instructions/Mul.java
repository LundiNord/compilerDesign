package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.custom.StandardRegister;
import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public class Mul extends AsInstruction {

    private final Register op1;

    public Mul(Register op1) {
        this.op1 = op1;
    }

    @Override
    public String toString() {
        return String.format("mul %s", op1.toString());
    }

    public Register getDestination() {
        return new StandardRegister("%eax", false);
    }
    public Register getSource() {
        return op1;
    }
}
