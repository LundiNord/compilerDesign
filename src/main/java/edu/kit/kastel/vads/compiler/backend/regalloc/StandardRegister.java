package edu.kit.kastel.vads.compiler.backend.regalloc;

/**
 * A Register in the cpu.
 * @author ujiqk
 * @version 1.0 */
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
