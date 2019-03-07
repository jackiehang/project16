/* Bantam Java Compiler and Language Toolset.

   Copyright (C) 2009 by Marc Corliss (corliss@hws.edu) and 
                         David Furcy (furcyd@uwosh.edu) and
                         E Christopher Lewis (lewis@vmware.com).
   ALL RIGHTS RESERVED.

   The Bantam Java toolset is distributed under the following 
   conditions:

     You may make copies of the toolset for your own use and 
     modify those copies.

     All copies of the toolset must retain the author names and 
     copyright notice.

     You may not sell the toolset or distribute it in 
     conjunction with a commerical product or service without 
     the expressed written consent of the authors.

   THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS 
   OR IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE 
   IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
   PARTICULAR PURPOSE.

   This file was modified by Dale Skrien, February, 2019.

*/

/*
 * File: SymbolTable.java
 * Edited By: Lucas DeGraw, Jackie Hang, Chris Marcello
 * Project 12
 * Date: February 25, 2019
 */


package proj13DeGrawHang.bantam.semant;

import proj13DeGrawHang.bantam.ast.*;
import proj13DeGrawHang.bantam.parser.Parser;
import proj13DeGrawHang.bantam.util.*;
import proj13DeGrawHang.bantam.util.Error;
import proj13DeGrawHang.bantam.visitor.Visitor;

import java.util.*;

/**
 * The <tt>SemanticAnalyzer</tt> class performs semantic analysis.
 * In particular this class is able to perform (via the <tt>analyze()</tt>
 * method) the following tests and analyses: (1) legal inheritence
 * hierarchy (all classes have existing parent, no cycles), (2)
 * legal class member declaration, (3) there is a correct bantam.Main class
 * and main() method, and (4) each class member is correctly typed.
 * <p>
 *
 *
 * Completed by:
 * @author Lucas DeGraw, Jackie Hang, Chris Marcello
 * @version 1.0
 * @since Feb 25, 2019
 *
 */
public class SemanticAnalyzer
{
    /**
     * reserved words that are tokens of type ID, but cannot be declared as the
     * names of (a) classes, (b) methods, (c) fields, (d) variables.
     * These words are:  null, this, super, void, int, boolean.
     * However, class names can be used as variable names.
     */


    public static final Set<String> reservedIdentifiers = new HashSet<>(Arrays.asList(
            "null", "this", "super", "void", "int", "boolean"));

    /**
     * Root of the AST
     */
    private Program program;

    /**
     * Root of the class hierarchy tree
     */
    private ClassTreeNode root;

    /**
     * Maps class names to ClassTreeNode objects representing the class
     */
    private Hashtable<String, ClassTreeNode> classMap = new Hashtable<String,ClassTreeNode>();

    /**
     * error handling
     */
    private ErrorHandler errorHandler;

    /*
     * If true (running from local main method) print out progress updates.
     */
    private static boolean verbose;

    /**
     * current filename
     */
    private static String filename;

    /**
     * Maximum number of inherited and non-inherited fields that can be defined for any
     * one class
     */
    private final int MAX_NUM_FIELDS = 1500;

    /**
     * SemanticAnalyzer constructor
     *
     * @param errorHandler the ErrorHandler to use for reporting errors
     */
    public SemanticAnalyzer(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * Analyze the AST checking for semantic errors and annotating the tree
     * Also builds an auxiliary class hierarchy tree
     *
     * @param program root of the AST to be checked
     * @return root of the class hierarchy tree (needed for code generation)
     * <p>
     * Must add code to do the following:
     * 1 - add built-in classes in classMap (already done)
     * 2 - add user-defined classes and build the inheritance tree of ClassTreeNodes - done
     * 3 - build the environment for each class (add class members only) and check
     *     that members are declared properly
     * 4 - check that the Main class and main method are declared properly
     * 5 - type check everything
     * See the lab manual for more details on each of these steps.
     */
    public ClassTreeNode analyze(Program program) {
        this.program = program;
        this.classMap.clear();

        // step 1:  add built-in classes to classMap
        addBuiltins();

        //step 2: add user-defined classes and build the inheritance tree of ClassTreeNodes
        if(verbose) { System.out.print("Beginning Build of Class Map... "); }
        addUserClasses();
        if(verbose) { System.out.println("Class Map Completed"); }

        if(verbose) { System.out.print("Beginning Build of Inheritance Relationships... "); }
        buildInheritance();
        if(verbose) { System.out.println("Inheritance Relationships Completed"); }

        //step 3: build the environment for each class (add class members only) and check that members are declared properly
        if(verbose) { System.out.print("Beginning Build of Class Environment... "); }
        buildClassEnvironment();
        if(verbose) { System.out.println("Class Environment Completed"); }

        //step 4: check that the Main class and main method are declared properly
        if(verbose) { System.out.print("Checking for Main Class & Method... "); }
        checkMain();
        if(verbose) { System.out.println("Main Class & Method Found"); }

        //step 5: Type Checking
        if(verbose) { System.out.print("Beginning Type Checking... "); }

        for(String key: classMap.keySet()) {

            if(!key.equals("Object") && !key.equals("String") && !key.equals("TextIO") && !key.equals("Sys")) {

                ErrorHandler checkerErrorHandler = new ErrorHandler();

                TypeCheckerVisitor typeCheckerVisitor = new TypeCheckerVisitor(checkerErrorHandler);

                typeCheckerVisitor.checkTypes(classMap.get(key));

                printErrors(checkerErrorHandler);
            }
        }
        if(verbose) { System.out.println("Type Checking Completed"); }

        return root;
    }

    /**
     * @return the ErrorHandler for this Parser
     */
    public ErrorHandler getErrorHandler() { return errorHandler; }

    /**
     * Add built-in classes to the classMap.
     * These are the classes Object, String, Sys, and TextIO
     */
    private void addBuiltins() {
        // create AST node for object
        Class_ astNode = new Class_(-1,
                "<built-in class>",
                "Object",
                null,
                (MemberList) (new MemberList(-1)).addElement(new Method(-1, "Object", "clone", new FormalList(-1),
                        (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1,
                                new VarExpr(-1, null, "null"))))).addElement(new Method(-1, "boolean",
                        "equals", (FormalList) (new FormalList(-1)).addElement(new Formal(-1,
                        "Object", "o")), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(
                                -1, new ConstBooleanExpr(-1, "false"))))).addElement(new Method(
                                        -1, "String", "toString", new FormalList(-1),
                        (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new VarExpr(-1,
                                null, "null"))))));

        // create a class tree node for object, save in variable root
        root = new ClassTreeNode(astNode,true, true, this.classMap);
        // add object class tree node to the mapping
        classMap.put("Object", root);

        // note: String, TextIO, and Sys all have fields that are not shown below.
        // Because these classes cannot be extended and their fields are protected,
        // they cannot be
        // accessed by other classes, so they do not have to be included in the AST.

        // create AST node for String
        astNode = new Class_(-1,
                "<built-in class>",
                "String",
                "Object",
                (MemberList) (new MemberList(-1)).addElement(new Field(-1, "int",
                        "length", /*0 by default*/null))
                        /* note: str is the character sequence -- no applicable type for a
                       character sequence so it is just made an int.  it's OK to
                       do this since this field is only accessed (directly) within
                       the runtime system */.addElement(new Method(-1, "int", "length",
                                new FormalList(-1),
                                (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new ConstIntExpr(-1, "0"))))).addElement(new Method(-1, "boolean", "equals", (FormalList) (new FormalList(-1)).addElement(new Formal(-1, "Object", "str")), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new ConstBooleanExpr(-1, "false"))))).addElement(new Method(-1, "String", "toString", new FormalList(-1), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new VarExpr(-1, null, "null"))))).addElement(new Method(-1, "String", "substring", (FormalList) (new FormalList(-1)).addElement(new Formal(-1, "int", "beginIndex")).addElement(new Formal(-1, "int", "endIndex")), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new VarExpr(-1, null, "null"))))).addElement(new Method(-1, "String", "concat", (FormalList) (new FormalList(-1)).addElement(new Formal(-1, "String", "str")), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new VarExpr(-1, null, "null"))))));
        // create class tree node for String, add it to the mapping
        classMap.put("String", new ClassTreeNode(astNode, /*built-in?*/true,
                /*extendable?*/false, classMap));

        // create AST node for TextIO
        astNode = new Class_(-1, "<built-in class>", "TextIO", "Object",
                (MemberList) (new MemberList(-1)).addElement(new Field(-1, "int",
                        "readFD", /*0 by default*/null)).addElement(new Field(-1, "int"
                        , "writeFD", new ConstIntExpr(-1, "1"))).addElement(new Method(-1, "void", "readStdin", new FormalList(-1), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, null)))).addElement(new Method(-1, "void", "readFile", (FormalList) (new FormalList(-1)).addElement(new Formal(-1, "String", "readFile")), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, null)))).addElement(new Method(-1, "void", "writeStdout", new FormalList(-1), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, null)))).addElement(new Method(-1, "void", "writeStderr", new FormalList(-1), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, null)))).addElement(new Method(-1, "void", "writeFile", (FormalList) (new FormalList(-1)).addElement(new Formal(-1, "String", "writeFile")), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, null)))).addElement(new Method(-1, "String", "getString", new FormalList(-1), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new VarExpr(-1, null, "null"))))).addElement(new Method(-1, "int", "getInt", new FormalList(-1), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new ConstIntExpr(-1, "0"))))).addElement(new Method(-1, "TextIO", "putString", (FormalList) (new FormalList(-1)).addElement(new Formal(-1, "String", "str")), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new VarExpr(-1, null, "null"))))).addElement(new Method(-1, "TextIO", "putInt", (FormalList) (new FormalList(-1)).addElement(new Formal(-1, "int", "n")), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new VarExpr(-1, null, "null"))))));
        // create class tree node for TextIO, add it to the mapping
        classMap.put("TextIO", new ClassTreeNode(astNode, /*built-in?*/true,
                /*extendable?*/false, classMap));

        // create AST node for Sys
        astNode = new Class_(-1, "<built-in class>", "Sys", "Object",
                (MemberList) (new MemberList(-1)).addElement(new Method(-1, "void",
                        "exit",
                        (FormalList) (new FormalList(-1)).addElement(new Formal(-1,
                                "int", "status")),
                        (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1,
                                null))))
                        /* MC: time() and random() requires modifying SPIM to add a time system
                         call
                       (note: random() does not need its own system call although it uses the time
                       system call).  We have a version of SPIM with this system call available,
                       otherwise, just comment out. (For x86 and jvm there are no issues.)
                       */.addElement(new Method(-1, "int", "time", new FormalList(-1),
                                (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new ConstIntExpr(-1, "0"))))).addElement(new Method(-1, "int", "random", new FormalList(-1), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new ConstIntExpr(-1, "0"))))));
        // create class tree node for Sys, add it to the mapping
        classMap.put("Sys", new ClassTreeNode(astNode, /*built-in?*/true, /*extendable
        ?*/false, classMap));
    }

    /**
     * Add user-defined classes to the classMap, and creates classTreeNodes
     */
    private void addUserClasses() {
        //creates a new visitor and puts it through the AST, adding classTreeNodes for each class
        ClassTreeNodeBuilder classTreeNodeBuilder = new ClassTreeNodeBuilder();
        classTreeNodeBuilder.build();
    }

    /**
     * creates the inheritance links for all classTreeNodes
     */
    private void buildInheritance() {

        //adds inheritance to built-in classes
        for(String key: classMap.keySet()) {
            if (!key.equals("Object")) {
                classMap.get(key).setParent(classMap.get("Object"));
            }
        }


        //adds inheritance to user classes
        InheritanceBuilder inheritanceBuilder = new InheritanceBuilder();
        inheritanceBuilder.build();
    }

    /**
     *Visitor to check if there exists a main method
     */
    private void checkMain() {
        MainMainVisitor mainMainVisitor = new MainMainVisitor();
        if(!mainMainVisitor.hasMain(this.program)) {
            errorHandler.register(Error.Kind.SEMANT_ERROR, filename, 0, "All Bantam Java " +
                    "require a class Main and and method main to run correctly.");
        }
    }

    /**
     * Visitor for traversing the AST to build the ClassTreeNodes
     */
    private class ClassTreeNodeBuilder extends Visitor {
        ClassTreeNode classTreeNode;

        public void build() {
            program.accept(this);
        }

        /**
         * Visits each class Node and builds appropriate classTreeNodes for each
         * @param node the class node
         * @return null
         */
        @Override
        public Object visit(Class_ node) {
            //creates a new classTreeNode for the class
            classTreeNode = new ClassTreeNode(node, false, true, classMap);
            classMap.put(node.getName(), classTreeNode);

            //doesn't visit children yet, since we're just building the CTN
            return true;
        }
    }

    /**
     * Visitor for traversing the AST to create inheritance links between ClassTreeNodes
     */
    private class InheritanceBuilder extends Visitor {
        ClassTreeNode classTreeNode;

        public void build() {
            program.accept(this);
        }

        @Override
        public Object visit(Class_ node) {
            classTreeNode = classMap.get(node.getName());

            int numObjectDescendants = classMap.get("Object").getNumDescendants();
            //sets parent
            if (!node.getParent().equals("")) {
                classTreeNode.setParent(classMap.get(node.getParent()));
                //see line 180 of ClassTreeNode
                //current attempt - see if the Object has a new descendent. if not, there's a cycle
                //if cycle, set the parent's parent to object as well as the current class's parent to object
                if (numObjectDescendants == classMap.get("Object").getNumDescendants()) {
                    classTreeNode.getParent().setParent(classMap.get("Object"));
                    classTreeNode.getParent().removeChild(classTreeNode);
                    classTreeNode.setParent(classMap.get("Object"));
                    errorHandler.register(Error.Kind.SEMANT_ERROR, filename, node.getLineNum(),
                            "Inheritance Cycle found between " + classTreeNode.getName() + "and "
                                    + classTreeNode.getParent().getName() );
                }
            }
            else {
                classMap.get("Object").addChild(classTreeNode);
                classTreeNode.setParent(classMap.get("Object"));
            }

            return null;
        }
    }

    /**
     * build the environment for each class
     */
    private void buildClassEnvironment() {
        ClassEnvironmentBuilder classEnvironmentBuilder = new ClassEnvironmentBuilder();
        classEnvironmentBuilder.build();
    }

    /**
     * Visitor for building the environment for each class
     */
    private class ClassEnvironmentBuilder extends Visitor {

        private ClassTreeNode currentClass;

        public void build() {
            currentClass = null;
            program.accept(this);
        }

        /**
         *Visit a class node
         * @param node the class node
         * @return null
         */
        @Override
        public Object visit(Class_ node) {

            //get the current class's tree node
            currentClass = classMap.get(node.getName());



            //enter current node's Symbol Table's scope
            currentClass.getVarSymbolTable().enterScope();
            currentClass.getMethodSymbolTable().enterScope();

            //traverse
            node.getMemberList().accept(this);


            return null;
        }

        /**
         * Add Fields to the current ClassTreeNode's Field Symbol Table
         *
         * @param node the field node
         * @return null
         */
        @Override
        public Object visit(Field node) {
            //standard check for reserved identifiers ("null", "this", "super", "void", "int", "boolean")
            if (reservedIdentifiers.contains(node.getName())) {
                errorHandler.register(Error.Kind.SEMANT_ERROR, filename, node.getLineNum(),
                        "Name " + node.getName() + " is reserved and cannot be used.");
            }
            //check to see if a Field of this name has already been declared in current class symbol table (an error)
            if (currentClass.getVarSymbolTable().peek(node.getName()) != null) {
                errorHandler.register(Error.Kind.SEMANT_ERROR, filename, node.getLineNum(),
                        "Field of name " + node.getName() +
                                " previously declared in class " + currentClass.getName() + ".");
            }
            //otherwise add it
            else {
                currentClass.getVarSymbolTable().add(node.getName(), node.getType());
            }
            return null;
        }

        /**
         * Adds Methods to the current ClassTreeNode's Methods Symbol Table
         *
         * @param node the method node
         * @return null
         */
        @Override
        public Object visit(Method node) {

            //standard check for reserved identifiers ("null", "this", "super", "void", "int", "boolean")
            if (reservedIdentifiers.contains(node.getName())) {
                errorHandler.register(Error.Kind.SEMANT_ERROR, filename, node.getLineNum(),
                        "Name " + node.getName() + " is reserved and cannot be used.");
            }
            //check to see if a Method of this name has already been declared in current class symbol table (an error)
            if (currentClass.getMethodSymbolTable().peek(node.getName()) != null) {
                errorHandler.register(Error.Kind.SEMANT_ERROR, filename, node.getLineNum(),
                        "Method of name " + node.getName() + " previously declared in class "
                                + currentClass.getName() + ".");
            }
            //otherwise add it
            else {
                currentClass.getMethodSymbolTable().add(node.getName(), node.getReturnType());

                //go into the method's symbol table
                currentClass.getVarSymbolTable().enterScope();

                //continue visitation
                //super.visit(node);
                node.getStmtList().accept(this);

                //exit
                currentClass.getVarSymbolTable().exitScope();
            }

            return null;
        }


        /**
         * Adds the formal parameter to the current scope
         * Shouldn't have to check for any overlap, but i guess someone could double up on parameters
         *
         * @param node the formal node
         * @return null
         */
        @Override
        public Object visit(Formal node) {
            //standard check for reserved identifiers ("null", "this", "super", "void", "int", "boolean")
            if (reservedIdentifiers.contains(node.getName())) {
                errorHandler.register(Error.Kind.SEMANT_ERROR, filename, node.getLineNum(),
                        "Name " + node.getName() + " is reserved and cannot be used.");
            }
            //check to see if a parameter of this name has already been declared in current class symbol table (an error)
            if (currentClass.getVarSymbolTable().peek(node.getName()) != null) {
                errorHandler.register(Error.Kind.SEMANT_ERROR, filename, node.getLineNum(),
                        "Parameter of name " + node.getName()
                                + " previously declared in class " + currentClass.getName() + ".");
            }
            //otherwise add it
            else {
                currentClass.getVarSymbolTable().add(node.getName(), node.getType());
            }
            return null;
        }

        /**
         * Adds the declared variable to the current scope
         *
         * @param node the declaration statement node
         * @return null
         */
        @Override
        public Object visit(DeclStmt node) {
            //standard check for reserved identifiers ("null", "this", "super", "void", "int", "boolean")
            if (reservedIdentifiers.contains(node.getName())) {
                errorHandler.register(Error.Kind.SEMANT_ERROR, filename, node.getLineNum(),
                        "Name " + node.getName() + " is reserved and cannot be used.");
            }
            //check to see if a Var of this name has already been declared in current class symbol table (an error)
            if (currentClass.getVarSymbolTable().peek(node.getName()) != null) {

                errorHandler.register(Error.Kind.SEMANT_ERROR, filename, node.getLineNum(),
                        "Var of name " + node.getName() + " previously declared in class " + currentClass.getName());
            }
            //check if the initialization statement is null
            if (node.getInit() == null) {
                errorHandler.register(Error.Kind.SEMANT_ERROR, filename, node.getLineNum(),
                        "Var of name " + node.getName() + " cannot be initialized to type null.");
            }
            //otherwise add it
            else {
                node.getInit().setExprType(node.getInit().toString());
                currentClass.getVarSymbolTable().add(node.getName(), node.getInit().toString());
                return super.visit(node);
            }
            return null;
        }

        /**
         * Enter new scope for For loop
         *
         * @param node the for statement node
         * @return null
         */
        @Override
        public Object visit(ForStmt node) {

            //enter new scope
            currentClass.getVarSymbolTable().enterScope();

            //standard for loop visitation (see Visitor.java)
            super.visit(node);

            //exit scope after completing For loop
            currentClass.getVarSymbolTable().exitScope();
            return null;
        }

        /**
         * Enter new scope for While loop
         *
         * @param node the while statement node
         * @return null
         */
        @Override
        public Object visit(WhileStmt node) {
            //enter new scope
            currentClass.getVarSymbolTable().enterScope();

            //standard while loop visitation (see Visitor.java)
            super.visit(node);

            //exit scope after completing While loop
            currentClass.getVarSymbolTable().exitScope();
            return null;
        }
    }

    /**
     * takes an ErrorHandler as input, prints its errors to the console
     *
     * @param errorHandler an ErrorHandler that should have at least 1 registered error
     */
    private static void printErrors(ErrorHandler errorHandler) {

        // if any errors were registered
        if (errorHandler.errorsFound()) {
            List<Error> errorList = errorHandler.getErrorList();  // get the list of errors

            // loop through the errors
            for (Error anErrorList : errorList) {
                // print the error
                System.out.println("\n" + anErrorList.toString());
            }
        }
    }


    /**
     * takes an array of file names as input,
     * loops through files and scan, parses and checks each one,
     * prints errors to the console
     * @param args an array of file names
     */
    public static void main(String[] args) {

        // verify that input was provided
        if (args.length == 0) {
            System.out.println("PROVIDE A FILENAME AS INPUT");
            return;
        }

        // initialize parser data
        ErrorHandler parseErrorHandler;
        Parser parser;
        Program ast;

        // initialize analyzer data
        ErrorHandler checkerErrorHandler;
        SemanticAnalyzer semanticAnalyzer;

        // denotes whether or not the program was successfully parsed
        Boolean parsingSuccessful;


        // loop through the file names
        for (String arg : args) {
            //sets current filename for use with error handler
            filename = arg;

            // reset parser data
            parseErrorHandler = new ErrorHandler();
            parser = new Parser(parseErrorHandler);
            ast = null;

            // reset analyzer data
            checkerErrorHandler = new ErrorHandler();
            semanticAnalyzer = new SemanticAnalyzer(checkerErrorHandler);

            System.out.println("\n\nCOMPILING: " + arg);

            // try to scan and parse the file
            try {
                ast = parser.parse(arg);
                System.out.println("\nParsing Successful");
                parsingSuccessful = true;
            } catch (CompilationException e) {
                System.out.println("Parse Failed");
                printErrors(parseErrorHandler);
                parsingSuccessful = false;
            }

            // if the program was parsed with no errors
            if (parsingSuccessful) {
                System.out.println("Starting Semantic Analysis");
                // try to check the program (semantic analysis)
                try {
                    verbose = true;
                    semanticAnalyzer.analyze(ast);
                    System.out.println("\nChecking Complete");
                    printErrors(checkerErrorHandler);
                }
                // includes CompilationExceptions (it's a subclass)
                catch (RuntimeException e) {
                    printErrors(checkerErrorHandler);
                }
            }
        }
    }
}
