package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.regalloc.StandardRegister;
import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public class Mod extends Div {

    public Mod(Register divisor) {
        super(divisor);
    }

    @Override
    public Register getDestination() {
        return new StandardRegister("%edx", false);
    }

}
