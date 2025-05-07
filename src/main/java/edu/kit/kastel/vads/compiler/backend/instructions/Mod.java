package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.custom.StandardRegister;
import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public class Mod extends Div {


    public Mod(Register divisor) {
        super(divisor);
    }

    public Register getDestination() {
        return new StandardRegister("%edx", false);
    }

}
