package edu.kit.kastel.vads.compiler.backend;

import edu.kit.kastel.vads.compiler.backend.instructions.Addl;
import edu.kit.kastel.vads.compiler.backend.instructions.AsInstruction;
import edu.kit.kastel.vads.compiler.backend.instructions.CmplConst;
import edu.kit.kastel.vads.compiler.backend.instructions.Div;
import edu.kit.kastel.vads.compiler.backend.instructions.Jne;
import edu.kit.kastel.vads.compiler.backend.instructions.Label;
import edu.kit.kastel.vads.compiler.backend.instructions.Mod;
import edu.kit.kastel.vads.compiler.backend.instructions.Movel;
import edu.kit.kastel.vads.compiler.backend.instructions.MovlConst;
import edu.kit.kastel.vads.compiler.backend.instructions.Mul;
import edu.kit.kastel.vads.compiler.backend.instructions.Sal;
import edu.kit.kastel.vads.compiler.backend.instructions.Subl;
import edu.kit.kastel.vads.compiler.backend.regalloc.InfiniteRegister;
import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.backend.regalloc.RegisterAlloc;
import edu.kit.kastel.vads.compiler.backend.regalloc.StandardRegister;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.AddNode;
import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.DivNode;
import edu.kit.kastel.vads.compiler.ir.node.ModNode;
import edu.kit.kastel.vads.compiler.ir.node.MulNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.ProjNode;
import edu.kit.kastel.vads.compiler.ir.node.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.node.StartNode;
import edu.kit.kastel.vads.compiler.ir.node.SubNode;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static edu.kit.kastel.vads.compiler.Main.DO_STRENGTH_REDUCTION;
import static edu.kit.kastel.vads.compiler.Main.PRINT_IR_GRAPH;
import static edu.kit.kastel.vads.compiler.ir.util.GraphVizPrinter.print;

/**
 * Entry Point for Instruction selection and register allocation.
 * @author ujiqk
 * @version 1.0 */
public class AssemblyGenerator {

    private List<AsInstruction> assemblyCode;
    private final Map<Node, Node> visited;
    private static final String template = """
            .global main
            .global _main
            .text
            main:
                call _main
            
                movq %rax, %rdi
                movq $0x3C, %rax
                syscall
            
            _main:
                pushq %rbp
                movq %rsp, %rbp

            """;
    private static final String endTemplate = """
            
            movq %rbp, %rsp
            popq %rbp
            ret
            """;
    private int nextRegister = 1;

    public AssemblyGenerator() {
        assemblyCode = new ArrayList<>();
        visited = new HashMap<>();
    }

    /**
     * Generates x86-64 assembly code from the IR SSA graph.
     * Side Effect: Populates assemblyCode List.
     * @param programs Multiple IR SSA graphs. Currently only translates the first graph.
     * @return The String with x86-64 assembly code.
     */
    public String generateCode(List<IrGraph> programs) {
        IrGraph program = programs.getFirst();
        if (PRINT_IR_GRAPH) {
            System.out.println("-----------------");
            System.out.print(print(program));
            System.out.println();
            System.out.println("-----------------");
        }

        Node endNode = program.endBlock();
        Node returnNode = endNode.predecessors().getFirst();
        maxMunch(returnNode);

        RegisterAlloc regAlloc = new RegisterAlloc(assemblyCode);
        assemblyCode = regAlloc.doRegAlloc();
        assemblyCode = regAlloc.removeMemToMem();

        StringBuilder result = new StringBuilder(template);
        assemblyCode.forEach(instruction -> result.append(instruction).append("\n"));
        return result.append(endTemplate).toString();
    }

    /**
     * Does recursive Instruction Selection using maximum munch algorithmus.
     * @param node Start Node.
     * @return Register/Variable where the result value is stored.
     */
    @Nullable
    private Register maxMunch(Node node) {      //ToDo: do strength reduction
        Node alreadyVisited = visited.get(node);
        if (alreadyVisited != null) {
            return alreadyVisited.getDestination();  //Could be changes to only store dest register
        } else {
            visited.put(node, node);
        }
        switch (node) {
            case ModNode mod -> {
                return modMaxMunch(mod);
            }
            case DivNode div -> {
                return divMaxMunch(div);
            }
            case ConstIntNode constIntNode -> {
                Register dest = getFreshRegister();
                MovlConst movlConst = new MovlConst(constIntNode.value(), dest);
                assemblyCode.add(movlConst);
                node.setDestination(dest);

                Node sideEffect = constIntNode.predecessors().isEmpty() ? null : constIntNode.predecessors().getFirst();
                if (sideEffect != null) {
                    maxMunch(sideEffect);
                }
                return dest;
            }
            case SubNode sub -> {
                Node successor1 = sub.predecessors().get(0);
                Node successor2 = sub.predecessors().get(1);
                Register succ1 = maxMunch(successor1);
                Register succ2 = maxMunch(successor2);
                Register dest = getFreshRegister();
                //move succ1 into fresh dest
                //add succ2 to dest
                assemblyCode.add(new Movel(succ1, dest));
                assert succ1 != null;
                assert succ2 != null;
                Subl subl = new Subl(succ2, dest);
                assemblyCode.add(subl);
                node.setDestination(dest);
                return dest;
            }
            case AddNode add -> {
                Node successor1 = add.predecessors().get(0);
                Node successor2 = add.predecessors().get(1);
                Register succ1 = maxMunch(successor1);
                Register succ2 = maxMunch(successor2);
                Register dest = getFreshRegister();
                //move succ1 into fresh dest
                //add succ2 to dest
                assemblyCode.add(new Movel(succ1, dest));
                assert succ1 != null;
                assert succ2 != null;
                Addl addl = new Addl(succ2, dest);
                assemblyCode.add(addl);
                node.setDestination(dest);
                return dest;
            }
            case MulNode mul -> {
                return mulMaxMunch(mul);
            }
            case ReturnNode returnNode -> {
                Register succ = null;
                for (Node predecessor : returnNode.predecessors()) {
                    if (predecessor.getClass() == ProjNode.class && ((ProjNode) predecessor).info().equals("SIDE_EFFECT")) {
                        maxMunch(predecessor); //calculate value and don't store it
                        continue;
                    }
                    succ = maxMunch(predecessor);
                }
                assert succ != null;
                //succ = succ.replace("d", "");
                //return value should be in %rax
                assemblyCode.add(new Movel(succ, new StandardRegister("%eax", false)));
                return null;
            }
            case ProjNode projNode -> {
                if (projNode.info().equals("RESULT")) {
                    Register reg = maxMunch(projNode.predecessors().getFirst());
                    assert reg != null;
                    node.setDestination(reg);
                    return reg;
                } else if (projNode.info().equals("SIDE_EFFECT")) {
                    // do nothing
                    Node successor1 = projNode.predecessors().getFirst();
                    Register reg = maxMunch(successor1);    //bei START hier null
                    node.setDestination(reg);
                    return null;
                } else {
                   throw new IllegalStateException("Unexpected value: " + projNode.info());
                }
            }
            case Block _, StartNode _ -> {
                // do nothing, skip line break
                return null;
            }
            default -> throw new IllegalStateException("Unexpected value: " + node);
        }
    }

    private Register mulMaxMunch(MulNode mul) {
        Node successor1 = mul.predecessors().get(0);
        Node successor2 = mul.predecessors().get(1);
        //do strength reduction
        if (successor1 instanceof ConstIntNode constInt) {  //case if both are const should not happen with
            Register ret = mulStrengthReduction(mul, Objects.requireNonNull(maxMunch(successor2)), constInt.value());
            if (ret != null) {
                return ret;
            }
        } else if (successor2 instanceof ConstIntNode constInt) {
            Register ret = mulStrengthReduction(mul, Objects.requireNonNull(maxMunch(successor1)), constInt.value());
            if (ret != null) {
                return ret;
            }
        }
        Register succ1 = maxMunch(successor1);
        Register succ2 = maxMunch(successor2);
        Register dest = new StandardRegister("%eax", false);    //dividend
        assemblyCode.add(new Movel(succ1, dest));
        assert succ2 != null;
        Mul mull = new Mul(succ2);
        assemblyCode.add(mull);

        Register destination = getFreshRegister();
        assemblyCode.add(new Movel(dest, destination));
        mul.setDestination(destination);
        return destination;
    }

    @Nullable
    private Register mulStrengthReduction(MulNode mul, Register succ, int value) {
        if (isPowerOfTwo(value) && DO_STRENGTH_REDUCTION) {
            //do bitwise shift
            int amount = Integer.numberOfTrailingZeros(value);
            Register dest = getFreshRegister();
            assemblyCode.add(new Movel(succ, dest));
            assemblyCode.add(new Sal(amount, dest));
            mul.setDestination(dest);
            return dest;
        }   //ToDo: more strength reductions
        return null;
    }

    boolean isPowerOfTwo(int n) {
        //https://www.baeldung.com/java-check-number-power-of-two
        return (n != 0) && ((n & (n - 1)) == 0);
    }
    private Register divMaxMunch(DivNode div) {
        Node successor1 = div.predecessors().get(0);    //oberer -> dividend
        Node successor2 = div.predecessors().get(1);    //unterer -> divisor
        Node sideEffect = div.predecessors().get(2);    //side effect
        maxMunch(sideEffect);
        Register succ1 = maxMunch(successor1);
        Register succ2 = maxMunch(successor2);
        Register dest = new StandardRegister("%eax", false);    //dividend
        assemblyCode.add(new Movel(succ1, dest));       //ToDo: change maxMunch to give in desired dest register
        assert succ2 != null;

        // Add runtime check for INT_MIN / -1
        Label skipSpecialCase = new Label("skip_special_case_" + nextRegister++);
        // Check if divisor is -1
        assemblyCode.add(new CmplConst(-1, succ2));
        assemblyCode.add(new Jne(skipSpecialCase));
        // Check if dividend is INT_MIN
        assemblyCode.add(new CmplConst(Integer.MIN_VALUE, dest));
        assemblyCode.add(new Jne(skipSpecialCase));
        // Throw exception by dividing by zero
        assemblyCode.add(new MovlConst(0, succ2));

        assemblyCode.add(skipSpecialCase);
        assemblyCode.add(new MovlConst(0, new StandardRegister("%edx", false)));   //replace with xor for performance?
        Div divl = new Div(succ2);
        assemblyCode.add(divl);

        Register destination = getFreshRegister();
        assemblyCode.add(new Movel(dest, destination));
        div.setDestination(destination);
        return destination;
    }

    private Register modMaxMunch(ModNode mod) {
        Node successor1 = mod.predecessors().get(0);    //oberer -> dividend
        Node successor2 = mod.predecessors().get(1);    //unterer -> divisor
        Node sideEffect = mod.predecessors().get(2); //side effect
        maxMunch(sideEffect);
        Register succ1 = maxMunch(successor1);
        Register succ2 = maxMunch(successor2);
        Register dest = new StandardRegister("%edx", false);
        Register dividend = new StandardRegister("%eax", false); //dividend
        assemblyCode.add(new Movel(succ1, dividend));
        assert succ2 != null;

        // Add runtime check for INT_MIN / -1
        Label skipSpecialCase = new Label("skip_special_case_" + nextRegister++);
        // Check if divisor is -1
        assemblyCode.add(new CmplConst(-1, succ2));
        assemblyCode.add(new Jne(skipSpecialCase));
        // Check if the dividend is INT_MIN
        assemblyCode.add(new CmplConst(Integer.MIN_VALUE, dividend));
        assemblyCode.add(new Jne(skipSpecialCase));
        // Throw exception by dividing by zero
        assemblyCode.add(new MovlConst(0, succ2));

        assemblyCode.add(skipSpecialCase);
        assemblyCode.add(new MovlConst(0, dest));   //replace with xor for performance?
        Mod modl = new Mod(succ2);
        assemblyCode.add(modl);

        Register destination = getFreshRegister();
        assemblyCode.add(new Movel(dest, destination));
        mod.setDestination(destination);
        return destination;
    }

    private Register getFreshRegister() {
        return new InfiniteRegister(nextRegister++);
    }

}
