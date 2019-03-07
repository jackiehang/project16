/*
 * File: classFieldMethodVisitor.java
 * Names: Lucas DeGraw, Jackie Hang, ChrisMarcello
 * Class: CS 361
 * Project 13
 * Date: March 5, 2018
 */

package proj13DeGrawHang.bantam.semant;

import proj13DeGrawHang.bantam.ast.*;
import proj13DeGrawHang.bantam.visitor.Visitor;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class allows the user to traverse through the
 * program and build a hashmap of
 * the fields, classes, and methods
 *
 * @author  Lucas DeGraw, Jackie Hang, ChrisMarcello
 * @since   3-5-2019
 */
public class classFieldMethodVisitor extends Visitor {
    private ArrayList<Class_> classes;

    /**
     * Initializes a HashMap with "Field", "Class",
     * and "Method" as keys and a new ArrayList of ASTNodes
     * as their values
     *
     * @param ast
     * @return HashMap<String, ArrayList<ASTNode>>
     */
    public ArrayList<Class_> getClassFieldMethodNodes(Program ast) {
        classes = new ArrayList<>();

        // traverse the abstract syntax tree
        ast.accept(this);

        return classes;
    }

//    /**
//     * Visits a field node and adds the node as a value
//     * to a hashmap, with "Field" as a key
//     *
//     * @param node the field node
//     * @return null;
//     */
//    public Object visit(Field node){
//        names.get("Field").add(node);
//        return null;
//    }

    /**
     * Visits a class nose and adds the node as a value
     * to a hashmap, with "Class" as a key
     * @param node the class node
     * @return null
     */
    public Object visit(Class_ node){
        classes.add(node);
        super.visit(node);
        return null;
    }
//
//    /**
//     * Visits a method node and adds the node as a value
//     * to a hashmap, with "Method" as a key
//     * @param node the method node
//     * @return null
//     */
//    public Object visit(Method node){
//        names.get("Method").add(node);
//        return null;
//    }

}
