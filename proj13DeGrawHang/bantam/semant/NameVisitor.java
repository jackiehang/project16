package proj13DeGrawHang.bantam.semant;

import proj13DeGrawHang.bantam.ast.Class_;
import proj13DeGrawHang.bantam.ast.Field;
import proj13DeGrawHang.bantam.ast.Method;
import proj13DeGrawHang.bantam.ast.Program;
import proj13DeGrawHang.bantam.visitor.Visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class NameVisitor extends Visitor {
    private HashMap<String, ArrayList<String>> names;

    public HashMap<String, ArrayList<String>> getClassFieldMethodNames(Program ast) {
        names = new HashMap<>();

        ArrayList<String> f = new ArrayList<>();
        names.put("Field", f);
        ArrayList<String> c = new ArrayList<>();
        names.put("Class", c);
        ArrayList<String> m = new ArrayList<>();
        names.put("Method", m);

        // traverse the abstract syntax tree
        ast.accept(this);

        return names;
    }

    public Object visit(Field node){
        names.get("Field").add(node.getName());
        return null;
    }

    public Object visit(Class_ node){
        names.get("Class").add(node.getName());
        super.visit(node);
        return null;
    }

    public Object visit(Method node){
        names.get("Method").add(node.getName());
        return null;
    }

}
