/*
 * File: MethodVisitor.java
 * Names: Lucas DeGraw, Jackie Hang, ChrisMarcello
 * Class: CS 361
 * Project 16
 * Date: April 11, 2018
 */


package proj16DeGrawHangMarcello.bantam.codegenmips;

import proj16DeGrawHangMarcello.bantam.ast.*;
import proj16DeGrawHangMarcello.bantam.visitor.Visitor;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * This visitor puts all the method names of a class
 * in an ArrayList and stores the list in hashmap where
 * the class name is a key and the list is a value
 *
 * @author Lucas DeGraw, Jackie Hang, ChrisMarcello
 * @since   4-11-2019
 *
 */
public class MethodVisitor extends Visitor {
    // key: class name, value: list of methods
    private HashMap<String, ArrayList<String>> classMethodsMap;
    private String curClassName;

    /**
     * gets a map of class names and corresponding methods
     *
     * @param ast abstract syntax tree
     * @return HashMap<String,ArrayList<String>
     *
     */
    public HashMap<String,ArrayList<String>> getMethods(Program ast) {
        classMethodsMap = new HashMap<>();

        // traverse the abstract syntax tree
        ast.accept(this);

        return classMethodsMap;
    }

    /**
     * Visits a class node
     *
     * @param node the class node
     * @return null
     */
    public Object visit(Class_ node){
        curClassName = node.getName();
        classMethodsMap.put(node.getName(), new ArrayList<>() );
        super.visit(node);
        return null;
    }

    /**
     * Visits a Method node
     * @param node the method node
     * @return null
     */
    public Object visit(Method node) {
        classMethodsMap.get( curClassName ).add(node.getName());
        super.visit(node);
        return null;
    }


}
