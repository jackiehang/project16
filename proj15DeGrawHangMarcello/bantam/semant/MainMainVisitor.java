/*
 * File: MainMainVisitor.java
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


package proj15DeGrawHangMarcello.bantam.semant;

import proj15DeGrawHangMarcello.bantam.ast.*;
import proj15DeGrawHangMarcello.bantam.visitor.Visitor;

/**
 * A subclass of the Visitor class, has the public method hasMain
 * @author Iris Lian
 */
public class MainMainVisitor extends Visitor {
    private boolean hasMain = false;

    public boolean hasMain(Program ast){
        hasMain = false;
        ast.accept(this);
        return hasMain;
    }

    /**
     * Visit a list node of classes
     *
     * @param node the class list node
     * @return result of the visit
     */
    public Object visit(ClassList node) {
        for (ASTNode aNode : node){
            if(!hasMain)
                aNode.accept(this);
        }
        return null;
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

        if(!curClassName.equals("Main")){
            hasMain = false;
        }

        return null;
    }


    /**
     *
     * @param node the Method node being visited
     * @return result of the visit
     */
    public Object visit(Method node) {
        // get method name
        String methodName = node.getName();

        // get return type
        String returnType = node.getReturnType();

        // get num params for this method
        int numParams = node.getFormalList().getSize();

        if(methodName.equals("main") && returnType.equals("void") && numParams == 0){
            hasMain = true;
        }

        return null;
    }

    /**
     * Skips visit to initialization expression for field
     * @param f
     * @return null
     */
    public Object visit(Field f){
        return null;
    }
}
