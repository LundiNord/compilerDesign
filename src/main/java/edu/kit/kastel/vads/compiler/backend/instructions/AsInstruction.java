package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public abstract class AsInstruction {

    private final Set<Register> liveIn;       //better performance with List?

    protected AsInstruction() {
        this.liveIn = new HashSet<>();
    }

    public abstract String toString();
    @Nullable
    public abstract Register getDestination();
    @Nullable
    public abstract Register getSource();

    public void addLiveIn(Register reg) {
        liveIn.add(reg);
    }
    public Set<Register> getLiveIn() {
        return liveIn;
    }

    public abstract void changeSource(Register reg);

}







//    @Nullable
//    private Node ssaNode;
//    @Nullable
//    public Node getSsaNode() {
//        return ssaNode;
//    }
//    public void setSsaNode(Node ssaNode) {
//        this.ssaNode = ssaNode;
//    }