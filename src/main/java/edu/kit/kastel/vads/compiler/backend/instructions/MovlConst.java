package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public class MovlConst extends Movel {

    private final int value;

    public MovlConst(int source, Register destination) {
        super(null, destination);
        this.value = source;
    }

    @Override
    public String toString() {
        return String.format("movl $%d, %s", value, destination);
    }

    @Override
    public void changeSource(Register reg) {
        //Do nothing
    }
}
