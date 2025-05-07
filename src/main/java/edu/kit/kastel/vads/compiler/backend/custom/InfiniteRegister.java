package edu.kit.kastel.vads.compiler.backend.custom;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import org.jspecify.annotations.Nullable;

public class InfiniteRegister implements Register {

    private final int number;
    @Nullable
    private Register trueRegister;

    public InfiniteRegister(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
    public void setTrueRegister(Register reg) {
        this.trueRegister = reg;
    }
    @Nullable
    public Register getTrueRegister() {
        return trueRegister;
    }

    @Override
    public String toString() {
        if (trueRegister != null) {
            return trueRegister.toString();
        }
        return String.valueOf(number);
    }
    @Override
    public int hashCode() {
        return number;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() == InfiniteRegister.class) {
            return number == ((InfiniteRegister) obj).getNumber();
        } else return super.equals(obj);
    }

}
