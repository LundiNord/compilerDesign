package edu.kit.kastel.vads.compiler.ir.node;

import edu.kit.kastel.vads.compiler.parser.symbol.Name;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Set;

public class Scope {

    private final HashMap<Name, Node> content;

    public Scope() {
        content = new HashMap<>();
    }

    public void addVariable(Name name, Node value) {
        if (content.put(name, value) != null) {
            //throw new IllegalArgumentException("Variable " + name + " already exists in the scope.");
        }
    }

    public void replaceVariable(Name name, Node value) {
        if (!content.containsKey(name)) {
            throw new IllegalArgumentException("Variable " + name + " does not exist in the scope.");
        }
        content.put(name, value);
    }

    @Nullable
    public Node getVariable(Name name) {
        return content.get(name);
    }

    public Set<Name> getNames() {
        return content.keySet();
    }

}
