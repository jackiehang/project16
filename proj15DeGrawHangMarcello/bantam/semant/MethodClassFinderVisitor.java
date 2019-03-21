/*
 * File: MethodParentVisitor.java
 * Names: Lucas DeGraw, Jackie Hang, Chris Marcello
 * Class: CS 461
 * Project 13
 * Date: March 6, 2019
 *
 */


package proj15DeGrawHangMarcello.bantam.semant;

import proj15DeGrawHangMarcello.bantam.ast.*;
import proj15DeGrawHangMarcello.bantam.visitor.Visitor;

/**
 * A subclass of the Visitor class, has the public method hasMain
 * @author Iris Lian
 */
public class MethodClassFinderVisitor extends Visitor{

    // create the map of (Method, OverridenMethod) pairs
    //private HashMap<Method,Method> overriddenMethodsMap;
    //private Set<String> builtIns = Set.of("Object", "TextIO", "String", "Sys");
    private String curClass;
    private String methodName;
    private boolean found;

    /**
     * traverses the input AST and returns the map of string constants
     *
     * @param ast an abstract syntax tree generated from Parser.parse()
     * @return the HashMap of string constants
     */
    public String getMethodClassName(Program ast, String methodName) {
        //overriddenMethodsMap = new HashMap<>();
        this.methodName = methodName;
        this.found = false;

        // traverse the abstract syntax tree
        ast.accept(this);

        return curClass;
    }

    @Override
    public Object visit(Class_ node) {
        if(this.found == false) {
            curClass = node.getName();
            node.getMemberList().accept(this);
        }
        return null;
    }

    @Override
    public Object visit(Method node) {
        if(node.getName().equals(methodName)) {
            this.found = true;
        }
        return null;
    }
}
