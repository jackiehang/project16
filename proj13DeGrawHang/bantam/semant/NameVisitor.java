package proj13DeGrawHang.bantam.semant;

import proj13DeGrawHang.bantam.ast.Class_;
import proj13DeGrawHang.bantam.ast.Field;
import proj13DeGrawHang.bantam.ast.Method;
import proj13DeGrawHang.bantam.ast.Program;
import proj13DeGrawHang.bantam.visitor.Visitor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class NameVisitor extends Visitor {
    private HashMap<String, LinkedList<String>> names;

    public Map<String,LinkedList<String>> getClassFieldMethodNames(Program ast) {
        names = new HashMap<>();
        // traverse the abstract syntax tree
        ast.accept(this);

        return names;
    }

    public Object visit(Field node){
        if(names.get("Field")==null){
            LinkedList<String> f = new LinkedList<>();
            names.put("Field", f);
        }
        names.get("Field").add(node.getName());
        return null;
    }

    public Object visit(Class_ node){
        if(names.get("Class")==null){
            LinkedList<String> f = new LinkedList<>();
            names.put("Class", f);
        }
        names.get("Class").add(node.getName());
        return null;
    }

    public Object visit(Method node){
        if(names.get("Method")==null){
            LinkedList<String> f = new LinkedList<>();
            names.put("Method", f);
        }
        names.get("Method").add(node.getName());
        return null;
    }

}
