package edu.kit.kastel.vads.compiler.backend.custom;

import edu.kit.kastel.vads.compiler.backend.instructions.*;
import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.*;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static edu.kit.kastel.vads.compiler.ir.util.GraphVizPrinter.print;

public class AssemblyGenerator {

    private final ArrayList<AsInstruction> assemblyCode;
    private final Set<Node> visited;
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

            """;
    private int nextRegister = 8;   //ToDo: implement register allocation

    public AssemblyGenerator() {
        assemblyCode = new ArrayList<AsInstruction>();
         visited = new HashSet<Node>();
    }

    public String generateCode(List<IrGraph> programs) {
        IrGraph program = programs.getFirst();

        System.out.println("-----------------");
        System.out.print(print(program));
        System.out.println();
        System.out.println("-----------------");

        Node endNode = program.endBlock();
        Node returnNode = endNode.predecessors().getFirst();
        maxMunch(returnNode);


        String result = assemblyCode.stream()
                .map(AsInstruction::toString)
                .reduce(template, (acc, instruction) -> acc + instruction + "\n");
        return result + "ret\n";
    }


    @Nullable
    private Register maxMunch(Node node) {
        boolean alreadyVisited = !visited.add(node);
        if (alreadyVisited) {
            assert node.getInstruction() != null;
            return node.getInstruction().getDestination();
        }
        switch (node) {
            case ModNode mod -> {
                Node successor1 = mod.predecessors().get(0);    //oberer -> dividend
                Node successor2 = mod.predecessors().get(1);    //unterer -> divisor
                Register succ1 = maxMunch(successor1);
                Register succ2 = maxMunch(successor2);
                Register dest = new InfiniteRegister("%edx", false);
                Register dividend = new InfiniteRegister("%eax", false); //dividend
                assemblyCode.add(new Movel(succ1, dividend));
                assert succ2 != null;
                assemblyCode.add(new MovlConst(0, dest));   //replace with xor for performance?
                assemblyCode.add(new Mod(succ2));
                return dest;
            }
            case DivNode div -> {
                Node successor1 = div.predecessors().get(0);    //oberer -> dividend
                Node successor2 = div.predecessors().get(1);    //unterer -> divisor
                Register succ1 = maxMunch(successor1);
                Register succ2 = maxMunch(successor2);
                Register dest = new InfiniteRegister("%eax", false);    //dividend
                assemblyCode.add(new Movel(succ1, dest));
                assert succ2 != null;
                assemblyCode.add(new MovlConst(0, new InfiniteRegister("%edx", false)));   //replace with xor for performance?
                assemblyCode.add(new Div(succ2));
                return dest;
            }
            case ConstIntNode constIntNode -> {
                Register dest = getFreshRegister();
                MovlConst movlConst = new MovlConst(constIntNode.value(), dest);
                node.setInstruction(movlConst);
                assemblyCode.add(movlConst);
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
                assemblyCode.add(new Subl(succ2, dest));
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
                node.setInstruction(addl);
                assemblyCode.add(addl);
                return dest;
            }
            case ReturnNode returnNode -> {
                Register succ = null;
                for (Node predecessor : returnNode.predecessors()) {
                    if (predecessor.getClass() == ProjNode.class && ((ProjNode) predecessor).info().equals("SIDE_EFFECT")) {     //get rid of project node
                        continue;
                    }
                    succ = maxMunch(predecessor);
                }
                assert succ != null;
                //succ = succ.replace("d", "");
                //return value should be in %rax
                assemblyCode.add(new Movel(succ, new InfiniteRegister("%eax", false)));
                return null;
            }
            case ProjNode projNode -> {
                if (projNode.info().equals("RESULT")) {
                    return maxMunch(projNode.predecessors().getFirst());
                } else {
                    return null;
                }
            }
            case Block _, StartNode _ -> {
                // do nothing, skip line break
                return null;
            }
            default -> throw new IllegalStateException("Unexpected value: " + node);
        }
    }

    private Register getFreshRegister() {
        String nextRegisterString = "%r" + nextRegister;
        nextRegister++;
        return new InfiniteRegister(nextRegisterString, true);
    }

}
















