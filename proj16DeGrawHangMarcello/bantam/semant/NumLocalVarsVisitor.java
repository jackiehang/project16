/*
 * File: NumLocalVarsVisitor.java
 * Names: Lucas DeGraw and Iris Lian
 * Class: CS 461
 * Project 11
 * Date: February 12, 2019
 *
 * ------------------------------
 *
 * Edited By: Lucas DeGraw, Jackie Hang, Chris Marcello
 * Project 12
 * Date: February 25, 2019
 */

package proj16DeGrawHangMarcello.bantam.semant;

import proj16DeGrawHangMarcello.bantam.ast.*;
import proj16DeGrawHangMarcello.bantam.visitor.Visitor;

import java.util.HashMap;
import java.util.Map;

/**
 * A subclass of the Visitor class, has the public method getNumLocalVars
 * @author Lucas DeGraw
 */

//LUCAS VERSION - seems overly complicated, and I know mine better, so starting from there

//public class NumLocalVarsVisitor extends Visitor {
//
//    // holds all mappings for the whole input program
//    private HashMap<String,Integer> completeLocalVarsMap;
//
//    // holds all mappings for one class at a time
//    private HashMap<String,Integer> curClassLocalVarsMap;
//
//    // store # of local vars for one method at a time
//    private int numLocalVarsFound = 0;
//
//
//    /**
//     * traverses the input AST and returns the map
//     *
//     * @param ast an abstract syntax tree generated from Parser.parse()
//     * @return a Map of ("className.MethodName",numLocalVarsInMethod) pairs
//     */
//    public Map<String,Integer> getNumLocalVars(Program ast) {
//        completeLocalVarsMap = new HashMap<>();
//        curClassLocalVarsMap = new HashMap<>();
//        numLocalVarsFound = 0;
//
//        ast.accept(this);
//        return this.completeLocalVarsMap;
//    }
//
//
//    /**
//     * called each time a Class_ node is visited
//     *
//     * @param node the Class_ node being visited
//     * @return result of the visit
//     */
//    public Object visit(Class_ node) {
//        super.visit(node);
//
//        // get class name
//        String curClassName = node.getName();
//
//        // loop through key, value pairs of map for current
//        this.curClassLocalVarsMap.forEach( (methodName, numLocalVars) -> {
//
//            // add "className." prefix to each methodName key
//            String newKey = curClassName + "." + methodName;
//
//            // add the new key & corresponding # local vars to the whole map
//            this.completeLocalVarsMap.put(newKey, numLocalVars);
//        });
//
//        // reset the hashmap for the current class
//        this.curClassLocalVarsMap = new HashMap<>();
//
//        return null;
//    }
//
//
//    /**
//     * called each time a Method node is visited
//     *
//     * @param node the Method node being visited
//     * @return result of the visit
//     */
//    public Object visit(Method node) {
//        super.visit(node);
//
//        // get method name
//        String methodName = node.getName();
//
//        // get num params for this method
//        int numParams = node.getFormalList().getSize();
//
//        // add methodName, numLocalVars + numParams to curClassLocalVarsMap
//        this.curClassLocalVarsMap.put(methodName, this.numLocalVarsFound+numParams);
//
//        // reset numLocalVarsFound to start counting for next method
//        this.numLocalVarsFound = 0;
//
//        return null;
//    }
//
//
//    /**
//     * called each time a DeclStmt node is visited
//     *
//     * @param node the DeclExpr node being visited
//     * @return result of the visit
//     */
//    public Object visit(DeclStmt node) {
//        // increment num local vars found in the current method
//        this.numLocalVarsFound++;
//
//        return null;
//    }
//
//}

public class NumLocalVarsVisitor extends Visitor {

    private Map<String, Integer> localVars;
    private String className;
    private String key;
    private int count;

    /**
     * Returns a Map of the number of local variables and parameters present in
     * the AST, on a method level.
     * Key - Class.Method
     * Value - Number of local variables and parameters present (integer)
     *
     *@param ast the AST node
     *@return Map the map of local variables
     */
    public Map<String, Integer> getNumLocalVars(Program ast) {
        className = "";
        key = "";
        count = 0;
        localVars = new HashMap<>();
        ast.accept(this); //starts visitation
        return localVars;
    }


    /**
     * Visit a class node
     * sets the current class name each time the visitor enters a new class
     *
     * @param node the class node
     * @return result of the visit
     */
    @Override
    public Object visit(Class_ node) {
        className = node.getName();
        return super.visit(node);
    }


    /**
     * Visit a method node
     *
     * sets the current key name each time we enter a new method
     * resets the counter to 0 and then adds the number of parameters
     *
     * @param node the method node
     * @return result of the visit
     */
    @Override
    public Object visit(Method node) {
        key = className + "." + node.getName();
        count = node.getFormalList().getSize();
        //node.getFormalList().accept(this);  //no need to visit formal lists
        node.getStmtList().accept(this);
        localVars.put(key, count);
        return null;
    }


    /**
     * Visit a declaration statement node
     * adds to the local variable counter each time a new Declaration Statement is made
     *
     * @param node the declaration statement node
     * @return null, which is the result of the visit
     */
    @Override
    public Object visit(DeclStmt node) {
        count += 1;
        return null;
    }

    /**
     * Overridden so that Expr is not visited
     *
     * @param node the expression statement node
     * @return null, which is the result of the visit
     */
    @Override
    public Object visit(ExprStmt node) {
        return null;
    }

    /**
     * Overridden so that Expr is not visited
     *
     * @param node the if statement node
     * @return null, which is the result of the visit
     */
    @Override
    public Object visit(IfStmt node) {
        node.getThenStmt().accept(this);
        if (node.getElseStmt() != null) {
            node.getElseStmt().accept(this);
        }
        return null;
    }

    /**
     * Overridden so that Expr is not visited
     *
     * @param node the while statement node
     * @return which is the result of the visit
     */
    @Override
    public Object visit(WhileStmt node) {
        node.getBodyStmt().accept(this);
        return null;
    }

    /**
     * Overridden so that Expr is not visited
     *
     * @param node the for statement node
     * @return null, which is the result of the visit
     */
    @Override
    public Object visit(ForStmt node) {
        node.getBodyStmt().accept(this);
        return null;
    }

    /**
     * Overridden so that Expr is not visited
     *
     * @param node the return statement node
     * @return null, which is the result of the visit
     */
    @Override
    public Object visit(ReturnStmt node) {
        return null;
    }

    /**
     * Overridden so that Expr is not visited
     *
     * @param node the return statement node
     * @return null, which is the result of the visit
     */
    @Override
    public Object visit(ExprList node) {
        return null;
    }


}