package edu.kit.kastel.vads.compiler.backend.custom;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public class StandardRegister implements Register {

    private String name;

    public StandardRegister(String name, boolean d) {
        this.name = name;
        if (d) {
            this.name = name + "d";
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
