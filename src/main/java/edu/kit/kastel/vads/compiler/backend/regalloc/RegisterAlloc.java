package edu.kit.kastel.vads.compiler.backend.regalloc;

import edu.kit.kastel.vads.compiler.backend.custom.InfiniteRegister;
import edu.kit.kastel.vads.compiler.backend.custom.StackRegister;
import edu.kit.kastel.vads.compiler.backend.custom.StandardRegister;
import edu.kit.kastel.vads.compiler.backend.instructions.AsInstruction;
import edu.kit.kastel.vads.compiler.backend.instructions.Movel;
import edu.kit.kastel.vads.compiler.backend.instructions.MovlConst;

import java.util.*;

public class RegisterAlloc {

    private final List<AsInstruction> assemblyCode;
    private final InterferenceGraph interferenceGraph;
    private static final Register[] REGISTERS = {
//           new StandardRegister("%ebx", false),
           new StandardRegister("%ecx", false),
           new StandardRegister("%esi", false),
           new StandardRegister("%edi", false),
           new StandardRegister("%r8d", false),
           new StandardRegister("%r9d", false),
           new StandardRegister("%r10d", false),
           new StandardRegister("%r11d", false),
           new StandardRegister("%r12d", false),
           new StandardRegister("%r13d", false),
           new StandardRegister("%r14d", false),
           new StandardRegister("%r15d", false),
   };

    public RegisterAlloc(List<AsInstruction> assemblyCode) {
        this.assemblyCode = assemblyCode;
        this.interferenceGraph = new InterferenceGraph();
    }

    public List<AsInstruction> doRegAlloc() {
        simpleInterference();
        int maxColor = interferenceGraph.doColoring();
        //count occurrences of each register and group by coloring
        Map<Integer, Integer> occurrences = new HashMap<>();    //Color, n                 //could maybe be done earlier
        for (int i = 0; i < assemblyCode.size(); i++) {
            AsInstruction instruction = assemblyCode.get(i);
            if (instruction.getDestination() != null && instruction.getDestination() instanceof InfiniteRegister infiniteRegister) {
                occurrences.put(interferenceGraph.getNode(infiniteRegister).getColor(), occurrences.getOrDefault(interferenceGraph.getNode(infiniteRegister).getColor(), 0) + 1);
            }       //ToDo: laufzeit von dem hier schlimm?
            if (instruction.getSource() != null && instruction.getSource() instanceof InfiniteRegister infiniteRegister) {
                occurrences.put(interferenceGraph.getNode(infiniteRegister).getColor(), occurrences.getOrDefault(interferenceGraph.getNode(infiniteRegister).getColor(), 0) + 1);
            }
        }
        //replace all infinite registers with standard registers
        Map<InfiniteRegister, Register> regSelection = selectRegisters(occurrences, maxColor);
        for (int i = 0; i < assemblyCode.size(); i++) {
            AsInstruction instruction = assemblyCode.get(i);
            if (instruction.getDestination() != null && instruction.getDestination() instanceof InfiniteRegister infiniteRegister) {
                infiniteRegister.setTrueRegister(regSelection.get(instruction.getDestination()));
            }
            if (instruction.getSource() != null && instruction.getSource() instanceof InfiniteRegister infiniteRegister) {
                infiniteRegister.setTrueRegister(regSelection.get(instruction.getSource()));
            }
        }
        return assemblyCode;
    }

    private void simpleInterference() {
        //calculate live info       //ToDo: use a better algorithm
        for (int i = assemblyCode.size() - 1; 0 < i; i--) {
            Register dest = assemblyCode.get(i).getDestination();
            Register src = assemblyCode.get(i).getSource();
            //And a variable that is used on the right-hand side of an instruction is live for that instruction
            if (src != null && src.getClass() == InfiniteRegister.class) {
                assemblyCode.get(i).addLiveIn(src);
            }
            //If a variable is live on one line, it is live on the preceding line unless it is assigned to on that line.
            if (i == assemblyCode.size() - 1) {
                //Do nothing
            } else {
                Set<Register> liveIn = assemblyCode.get(i + 1).getLiveIn();
                for (Register reg : liveIn) {
                    if (reg.getClass() == InfiniteRegister.class && !reg.equals(dest)) {
                        assemblyCode.get(i).addLiveIn(reg);
                    }
                }
            }
            //build vertexes in graph
            if (dest != null && dest.getClass() == InfiniteRegister.class) {
                interferenceGraph.addVertex(dest);
            }
            if (src != null && src.getClass() == InfiniteRegister.class) {
                interferenceGraph.addVertex(src);
            }
        }
        //build graph
        for (int i = 0; i < assemblyCode.size(); i++) {
            Register src = assemblyCode.get(i).getSource();
            Register dest = assemblyCode.get(i).getDestination();
            if (dest == null || dest instanceof StandardRegister) {
                continue;
            }
            if (assemblyCode.get(i).getClass() == MovlConst.class) {
                Set<Register> liveInAfter = assemblyCode.get(i + 1).getLiveIn();
                for (Register reg : liveInAfter) {
                    if (!reg.equals(dest)) {
                        interferenceGraph.addEdge(dest, reg);
                    }
                }
            }
            else if (assemblyCode.get(i).getClass() == Movel.class) {
                assert src != null;
                if (i + 1 == assemblyCode.size()) {
                    continue;
                }
                Set<Register> liveInAfter = assemblyCode.get(i + 1).getLiveIn();
                for (Register reg : liveInAfter) {
                    if (!reg.equals(dest) && !reg.equals(src)) {
                        interferenceGraph.addEdge(dest, reg);
                    }
                }
                //ToDo: we omit the potential edge between t and s
            } else {
                Set<Register> liveInAfter = assemblyCode.get(i + 1).getLiveIn();
                for (Register reg : liveInAfter) {
                    if (!reg.equals(dest)) {
                        interferenceGraph.addEdge(dest, reg);
                    }
                }
            }
        }
    }

    private Map<InfiniteRegister, Register> selectRegisters(Map<Integer, Integer> occurrences, int maxColor) {
        //14 registers available
        Map<InfiniteRegister, Register> regSelection = new HashMap<>();
        //top occurrences get color, the rest get space on the stack
        List<Integer> topColors = occurrences.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey).limit(REGISTERS.length).toList();

        for (InterferenceNode node : interferenceGraph.getVertexes()) {
            int color = node.getColor();
            if (topColors.contains(color) ) {
                StandardRegister reg = (StandardRegister) REGISTERS[topColors.indexOf(color)];
                regSelection.put((InfiniteRegister) node.getReg(), reg);
            } else {
                StackRegister reg = new StackRegister(8 * color);
                regSelection.put((InfiniteRegister) node.getReg(), reg);
            }
        }
        return regSelection;
    }

    public List<AsInstruction> removeMemToMem() {
        List<AsInstruction> newAssemblyCode = new ArrayList<>();
        StandardRegister temp = new StandardRegister("%ebx", false);
        for (AsInstruction instruction : assemblyCode) {
            if (instruction.getSource() instanceof InfiniteRegister inf1 && instruction.getDestination() instanceof InfiniteRegister inf2) {
                if (inf1.getTrueRegister() instanceof StackRegister && inf2.getTrueRegister() instanceof StackRegister) {
                    newAssemblyCode.add(new Movel(instruction.getSource(), temp));
                    instruction.changeSource(temp);
                    newAssemblyCode.add(instruction);
                } else {
                    newAssemblyCode.add(instruction);
                }
            } else {
                newAssemblyCode.add(instruction);
            }
        }
        return newAssemblyCode;
    }

}
