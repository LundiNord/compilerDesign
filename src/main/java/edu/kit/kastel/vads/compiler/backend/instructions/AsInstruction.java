package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public interface AsInstruction {

//    @Nullable
//    private Node ssaNode;

    String toString();

//    @Nullable
//    public Node getSsaNode() {
//        return ssaNode;
//    }
//    public void setSsaNode(Node ssaNode) {
//        this.ssaNode = ssaNode;
//    }
    Register getDestination();

}
