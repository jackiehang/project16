package proj11DeGrawLian.bantam.semant;

import proj11DeGrawLian.bantam.ast.ConstStringExpr;
import proj11DeGrawLian.bantam.ast.Program;
import proj11DeGrawLian.bantam.visitor.Visitor;

import java.util.HashMap;
import java.util.Map;

public class StringConstantsVisitor extends Visitor {

    // create the map of (StringConstant_#, stringConstantValue) pairs
    private HashMap<String,String> stringMap = new HashMap<>();

    /**
     *
     * @param ast an abstract syntax tree generated from Parser.parse()
     * @return the number of string constants found in the program
     */
    public Map<String,String> getStringConstants(Program ast) {

        // traverse the abstract syntax tree
        ast.accept(this);

        return stringMap;
    }



    /**
     * each time a ConstStringExpr node is found during the traversal,
     * this method is called to visit the node
     * @param node the string constant expression node
     * @return null, because this node is always a leaf of the tree
     */
    public Object visit(ConstStringExpr node) {

        // call Visitor's visit() method for a ConstStringExpr node
        super.visit(node);

        // get num constants in map
        int numStringsInMap = stringMap.size();

        // build name
        String name = "StringConstant_" + Integer.toString(numStringsInMap);

        // get string value from ConstStringExpr node
        String value = node.getConstant();

        // add name, value pair to map
        stringMap.put(name, value);

        return null;
    }
}
