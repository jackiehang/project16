package proj11DeGrawLian.bantam.semant;

import proj11DeGrawLian.bantam.ast.*;
import proj11DeGrawLian.bantam.visitor.Visitor;

import java.util.HashMap;
import java.util.Map;


public class NumLocalVarsVisitor extends Visitor {

    // holds all mappings for the whole input program
    private HashMap<String,Integer> completeLocalVarsMap = new HashMap();

    // holds all mappings for one class at a time
    private HashMap<String,Integer> curClassLocalVarsMap = new HashMap();

    // store # of local vars for one method at a time
    private int numLocalVarsFound = 0;


    /**
     *
     * @param ast an abstract syntax tree generated from Parser.parse()
     * @return a Map of ("className.MethodName",numLocalVarsInMethod) pairs
     */
    public Map<String,Integer> getNumLocalVars(Program ast) {
        ast.accept(this);
        return completeLocalVarsMap;
    }


    /**
     *
     * @param node the Class_ node being visited
     * @return result of the visit
     */
    public Object visit(Class_ node) {
        super.visit(node);

        // get class name
        String curClassName = node.getName();

        // loop through key, value pairs of map for current
        this.curClassLocalVarsMap.forEach( (methodName, numLocalVars) -> {

            // add "className." prefix to each methodName key
            String newKey = curClassName + "." + methodName;

            // add the new key & corresponding # local vars to the whole map
            this.completeLocalVarsMap.put(newKey, numLocalVars);
        });

        // reset the hashmap for the current class
        this.curClassLocalVarsMap = new HashMap<>();

        return null;
    }


    /**
     *
     * @param node the Method node being visited
     * @return result of the visit
     */
    public Object visit(Method node) {
        super.visit(node);

        // get method name
        String methodName = node.getName();

        // get num params for this method
        int numParams = node.getFormalList().getSize();

        // add methodName, numLocalVars + numParams to curClassLocalVarsMap
        this.curClassLocalVarsMap.put(methodName, this.numLocalVarsFound+numParams);

        // reset numLocalVarsFound to start counting for next method
        this.numLocalVarsFound = 0;

        return null;
    }


    /**
     *
     * @param node the AssignExpr node being visited
     * @return result of the visit
     */
    public Object visit(AssignExpr node) {

        // call parent visit method
        super.visit(node);

        // increment num local vars found in the current method
        this.numLocalVarsFound++;

        return null;
    }

}
