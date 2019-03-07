/*
 * File: ClassVisitor.java
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
 * program and return an arraylist of class nodes
 *
 * @author  Lucas DeGraw, Jackie Hang, ChrisMarcello
 * @since   3-5-2019
 */
public class ClassVisitor extends Visitor {
    private ArrayList<Class_> classes;

    /**
     * Returns an ArrayList of Class nodes
     * @param ast
     * @return HashMap<String, ArrayList<ASTNode>>
     */
    public ArrayList<Class_> getClassNodes(Program ast) {
        classes = new ArrayList<>();

        // traverse the abstract syntax tree
        ast.accept(this);

        return classes;
    }

    /**
     * Visits a class nose and adds the node as a value
     * to a hashmap, with "Class" as a key
     * @param node the class node
     * @return null
     */
    public Object visit(Class_ node){
        classes.add(node);
        return null;
    }


}
