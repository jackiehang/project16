package proj12DeGrawHangMarcello.bantam.semant;

import proj12DeGrawHangMarcello.bantam.util.*;
import proj12DeGrawHangMarcello.bantam.ast.*;
import proj12DeGrawHangMarcello.bantam.util.Error;
import proj12DeGrawHangMarcello.bantam.visitor.*;

import java.util.*;

public class TypeCheckerVisitor extends Visitor {
    private ClassTreeNode currentClass;
    private SymbolTable currentSymbolTable;
    private ErrorHandler errorHandler;

    // sets the error handler upon initialization
    public TypeCheckerVisitor(ErrorHandler errHandler) {
        errorHandler = errHandler;
    }

    /**
     * begins traversal of the AST to perform type checking
     *
     * @param curClass the top level ClassTreeNode
     */
    public void checkTypes(ClassTreeNode curClass) {

        // save the top level class
        currentClass = curClass;

        // save top level symbol table
        currentSymbolTable = currentClass.getVarSymbolTable();

        // get class root
        Class_ root = currentClass.getASTNode();

        System.out.println("BEGIN CHECKING: " + root);

        // begin traversal
        root.accept(this);

        System.out.println("DONE CHECKING");
    }

    /**
     * Helper method to find if the type is a defined type
     *
     * @param type a string representing a data type
     * @return boolean denoting whether or not the type
     */
    private boolean isDefinedType(String type) {
        return currentClass.getClassMap().containsKey(type) || type.equals("boolean")
                || type.equals("int") || type.equals("String");
    }

    /**
     * Helper method to check if nodeType is a subtype of targetType
     *
     * @param nodeType type of node
     * @param targetType type against which nodeType is being checked
     * @return boolean
     */
    private boolean isSubType(String nodeType, String targetType) {

        // if the node types are the exact same type, return true
        if (nodeType.equals(targetType)) return true;

        // initialize a temp node
        ClassTreeNode curNode;

        // loop until reaching the top of the tree
        while (!nodeType.equals("Object")) {

            if (nodeType.equals(targetType)) {  // if nodes are the same return true
                return true;
            }

            // get the type's associated tree node
            curNode = currentClass.getClassMap().get(nodeType);

            // set the node type to its parent type
            nodeType = curNode.getParent().getName();
        }
        return false;
    }

    /**
     * Helper method to check if type is a defined class type
     *
     * @param type
     * @return boolean
     */
    private boolean isDefinedClassType(String type) {
        return currentClass.getClassMap().containsKey(type);
    }

    /**
     * Visit a array assignment expression node
     *
     * @param node the array assignment expression node
     * @return the result of the visit
     */
    public Object visit(ArrayAssignExpr node) {

        // visit the child nodes
        node.getExpr().accept(this);
        node.getIndex().accept(this);

        // if the index to the array expresssion is not an integer, register an error
        if (!node.getIndex().getExprType().equals("int")) {

            String errorMsg = "The index of the array assignment is a" +
                    node.getIndex().getExprType() + " and it should be an integer.";

            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(), errorMsg);
        }
        // set the appropriate array index expression type
        node.getIndex().setExprType("int");

        // get the type of the array
        String typeOfArray = (String) currentSymbolTable.lookup(node.getName());

        // TODO: WHY?
        typeOfArray = typeOfArray.substring(0, typeOfArray.length()-2); //type of Array

        // get the type of the right side of the assignment expression
        String assignType = node.getExpr().getExprType();

        // if the type is not a subtype of the array's type, register an error
        if (!isSubType(assignType, typeOfArray)) {

            String errorMsg = "The type of array is " + typeOfArray +
                    " and the value you are trying to assign is " + assignType +
                    ". They are not compatible";

            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(), errorMsg);


            // set the right side of the expression to the array's actual type
            node.getExpr().setExprType(typeOfArray);
        }
        // set the node's expression type to the array's actual type
        node.setExprType(typeOfArray);

        return null;
    }

    /**
     * Visit an array expression node
     *
     * @param node the array expression node
     * @return null
     */
    public Object visit(ArrayExpr node) {
        if (node.getRef() != null) {
            node.getRef().accept(this);
        }
        node.getIndex().accept(this);

        if (!node.getIndex().getExprType().equals("int")) {

            String errorMsg = "The index of the array assignment is a" +
                        node.getIndex().getExprType() + " and it should be an integer.";
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(), errorMsg);
        }

        String typeOfArray = (String) currentSymbolTable.lookup(node.getName());
        typeOfArray = typeOfArray.substring(0, typeOfArray.length()-2); //type of Array
        node.setExprType(typeOfArray);
        return null;
    }

    /**
     * Visit an assignment expression node
     *
     * @param node the assignment expression node
     * @return the result of the visit
     */
    public Object visit(AssignExpr node) {
//        node.getExpr().accept(this);

        currentSymbolTable.dump();
        if (currentSymbolTable.lookup(node.getName()) == null) {

            String errorMsg = "The variable " + node.getName() + " has not been defined yet";
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(), errorMsg);
        }

        String exprType = node.getExpr().getExprType();
        String variableType = (String) currentSymbolTable.lookup(node.getName());

        if (!isSubType(exprType, variableType)) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The value has a type of " + exprType +
                            ",different than variable it is being assigned to, of type" +
                            currentSymbolTable.lookup(node.getName()));
        }
        node.setExprType(variableType);
        node.getExpr().accept(this);
        return null;

    }

    /**
     * Visit a binary arithmetic divide expression node
     *
     * @param node the binary arithmetic divide expression node
     * @return null
     */
    public Object visit(BinaryArithDivideExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if (!type2.equals(type1) || !type1.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being used in the arithmetic division are of types " + type1
                            + " and " + type2 + ". They should both be of type int.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a binary arithmetic minus expression node
     *
     * @param node the binary arithmetic minus expression node
     * @return the result of the visit
     */
    public Object visit(BinaryArithMinusExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if (!type2.equals(type1) || !type1.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being used in the arithmetic subtraction are of types " + type1
                            + " and " + type2 + ". They should both be of type int.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a binary arithmetic plus expression node
     *
     * @param node the binary arithmetic plus expression node
     * @return the result of the visit
     */
    public Object visit(BinaryArithPlusExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if (!type2.equals(type1) || !type1.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being used in the arithmetic addition are of types " + type1
                            + " and " + type2 + ". They should both be of type int.");
        }
        node.setExprType("int");

        return null;
    }

    /**
     * Visit a binary arithmetic times expression node
     *
     * @param node the binary arithmetic times expression node
     * @return the result of the visit
     */
    public Object visit(BinaryArithTimesExpr node) {

        // visit child nodes
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);

        // get the operand types
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();

        // if the types are not equal or the first is not an int
        if (!type2.equals(type1) || !type1.equals("int")) {

            String errorMsg = "The two values being used in the arithmetic multiplication "
                              + "are of types " + type1 + " and " + type2
                               + ". They should both be of type int.";

            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),errorMsg);
        }
        node.setExprType("int");

        return null;
    }

    /**
     * Visit a binary arithmetic modulus expression node
     *
     * @param node the binary arithmetic modulus expression node
     * @return the result of the visit
     */
    public Object visit(BinaryArithModulusExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if (!type2.equals(type1) || !type1.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being used in the arithmetic modulus are of types " + type1
                            + " and " + type2 + ". They should both be of type int.");
        }
        node.setExprType("int");

        return null;
    }

    /**
     * Visit a binary comparison equals expression node
     *
     * @param node the binary comparison equals expression node
     * @return null
     */
    public Object visit(BinaryCompEqExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        //if neither type1 nor type2 is a subtype of the other
        if (!isSubType(type1, type2) || !isSubType(type2, type1)) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being compared for equality are not compatible types.");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a binary comparison not equals expression node
     *
     * @param node the binary comparison not equals expression node
     * @return null
     */
    public Object visit(BinaryCompNeExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        //if neither type1 nor type2 is a subtype of the other
        if (!isSubType(type1, type2) || !isSubType(type2, type1)) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being compared for equality are not compatible types.");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a binary comparison greater than expression node
     *
     * @param node the binary comparison greater than expression node
     * @return null
     */
    public Object visit(BinaryCompGtExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if (!type2.equals(type1) || !type1.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being compared for greater than are of types " + type1
                            + " and " + type2 + ". They should both be of type int.");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a binary comparison less than expression node
     *
     * @param node the binary comparison less than expression node
     * @return null
     */
    public Object visit(BinaryCompLtExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if (!type2.equals(type1) || !type1.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being compared for less than are of types " + type1
                            + " and " + type2 + ". They should both be of type int.");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a binary comparison greater to or equal to expression node
     *
     * @param node the binary comparison greater to or equal to expression node
     * @return null
     */
    public Object visit(BinaryCompGeqExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if (!type2.equals(type1) || !type1.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being compared for greater than or equal to are of types " + type1
                            + " and " + type2 + ". They should both be of type int.");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a binary comparison less than or equal to expression node
     *
     * @param node the binary comparison less than or equal to expression node
     * @return null
     */
    public Object visit(BinaryCompLeqExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if (!type2.equals(type1) || !type1.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being compared for less than or equal to are of types " + type1
                            + " and " + type2 + ". They should both be of type int.");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * visit a binary logical AND expression node
     *
     * @param node the binary logical AND expression node
     * @return null
     */
    public Object visit(BinaryLogicAndExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if (!type2.equals(type1) || !type1.equals("boolean")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being compared are of types " + type1
                            + " and " + type2 + ". They should both be of type boolean.");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a binary logical OR expression node
     *
     * @param node the binary logical OR expression node
     * @return null
     */
    public Object visit(BinaryLogicOrExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if (!type2.equals(type1) || !type1.equals("boolean")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being compared are of types " + type1
                            + " and " + type2 + ". They should both be of type boolean.");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a block statement node
     *
     * @param node the block statement node
     * @return null
     */
    public Object visit(BlockStmt node) {
        currentSymbolTable.enterScope();
        node.getStmtList().accept(this);
        currentSymbolTable.exitScope();
        return null;
    }

    /**
     * visit a break statement node
     *
     * @param node the break statement node
     * @return null
     */
    public Object visit(BreakStmt node) {
        node.accept(this);
        return null;
    }

    /**
     * Visit a cast expression node
     *
     * @param node the cast expression node
     * @return the result of the visit
     */
    public Object visit(CastExpr node) {

        node.getExpr().accept(this);
        String target = node.getType();
        String exprType = node.getExpr().getExprType();

        // if exprType is a subtype of target
        if (isSubType(exprType, target)) {  // upcast
            node.setUpCast(true);
        }
        else if (!isSubType(target, exprType)){ // if neither are subtypes of each other,

            // throw error
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "Illegal attempt to cast a variable of type \'" + exprType
                            + "\' to type \'" + target + "\'");
        }
        // make the expression type valid to continue analyzing
        node.setExprType(node.getType());

        return null;
    }

    /**
     * Visit a boolean constant expression node
     *
     * @param node the boolean constant expression node
     * @return null
     */
    public Object visit(ConstBooleanExpr node) {
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit an int constant expression node
     *
     * @param node the int constant expression node
     * @return null
     */
    public Object visit(ConstIntExpr node) {
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a string constant expression node
     *
     * @param node the string constant expression node
     * @return null
     */
    public Object visit(ConstStringExpr node) {
        node.setExprType("String");
        return null;
    }

    /**
     * Visit a declaration statement node
     *
     * @param node the declaration statement node
     * @return null
     */
    public Object visit(DeclStmt node) {
        System.out.println("DECL");
        node.getInit().accept(this);
        if (!isSubType(node.getInit().getExprType(), node.getType())) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The variable declaration you are making is invalid. Variable of type " +
                            node.getType() + " cannot have value of type " + node.getInit().getExprType());
        }

        return null;
    }

    /**
     * checks that the parameter types of the dispatch expression match the allowed types
     * and visits the child nodes
     *
     * @param node the dispatch expression node
     * @return the result of the visit
     */
    public Object visit(DispatchExpr node) {

        //visit the ref expr- check to see if the class method symbol table has that method
        Expr refExpr = node.getRefExpr();
        refExpr.accept(this);

        Method methodNode =
                (Method) currentClass.getMethodSymbolTable()
                        .lookup(node.getMethodName());

        // visit child nodes
//        node.getActualList().accept(this);

        if (methodNode == null) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The method " + node.getMethodName() + "was not found in the method " +
                            "symbol table.");

            node.setExprType("Object");
            return null;
        }

        // get the parameters being passed in
        ExprList actualParams = node.getActualList();
        int numActualParams = actualParams.getSize();

        // get the acceptable parameters
        FormalList allowedArgs = methodNode.getFormalList();

        // if they have the same number of parameters
        if (numActualParams == allowedArgs.getSize()) {

            // list of types that cannot be subclassed in Bantam Java
            Set<String> nonObjectTypes = Set.of("int", "boolean", "String");

            // loop through the arguments of both lists (same length)
            for (int i = 0; i < numActualParams; i++) {

                // get the two argument objects
                Formal actualArg = (Formal) actualParams.get(i);
                Formal allowedArg = (Formal) allowedArgs.get(i);

                // get the two argument types
                String actualType = actualArg.getType();
                String allowedType = allowedArg.getType();

                // if the types don't match
                if (!actualType.equals(allowedType)) {

                    // if one type is not an object
                    if (nonObjectTypes.contains(actualType)
                            || nonObjectTypes.contains(allowedType)) {

                        String errorMsg = "Actual type " + "\'" + actualType + "\' of parameter "
                                + i + " to method " + node.getMethodName() + " "
                                + "does not match expected type " + "\'" + allowedType + "\'";

                        // throw an error
                        errorHandler.register(Error.Kind.SEMANT_ERROR,
                                currentClass.getASTNode().getFilename(),
                                node.getLineNum(), errorMsg);
                    }
                    // else if actual param is not a subtype of acceptable param
                    else if (!isSubType(actualType, allowedType)) {

                        String errorMsg = "Actual type " + "\'" + actualType +
                                "\' of parameter " + i + " to method " + node.getMethodName()
                                + " " + "is not a subtype of " + "\'" + allowedType + "\'";

                        // throw an error
                        errorHandler.register(Error.Kind.SEMANT_ERROR,
                                currentClass.getASTNode().getFilename(),
                                node.getLineNum(), errorMsg);
                    }
                }
            }

        }

        //set type of dispatch expr to the return type of the ref
        node.setExprType(refExpr.getExprType());
        return null;
    }

    /**
     * Visit a field node
     *
     * @param node the field node
     * @return null
     */
    public Object visit(Field node) {
        // The fields should have already been added to the symbol table by the
        // SemanticAnalyzer so the only thing to check is the compatibility of the init
        // expr's type with the field's type.

        // if node's type is not a defined type
        if (!isDefinedType(node.getType())) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The declared type " + node.getType() + " of the field "
                            + node.getName() + " is undefined.");
        }
        Expr initExpr = node.getInit();
        if (initExpr != null) {
            initExpr.accept(this);
            //if the initExpr's type is not a subtype of the node's type
            if (!isSubType(initExpr.getExprType(), node.getType())) {
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "The type of the initializer is " + initExpr.getExprType()
                                + " which is not compatible with the " + node.getName() +
                                " field's type " + node.getType());
            }
        }
        // Note: if there is no initExpr, then leave it to the Code Generator to
        //      initialize it to the default value since it is irrelevant to the
        //      SemanticAnalyzer.
        return null;
    }

    /**
     * Visit a formal parameter node
     *
     * @param node the Formal node
     * @return null
     */
    public Object visit(Formal node) {

        //the node's type is not a defined type
        if (!isDefinedType(node.getType())) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The declared type " + node.getType() + " of the formal" +
                            " parameter " + node.getName() + " is undefined.");
        }
        // add it to the current scope
        currentSymbolTable.add(node.getName(), node.getType());
        return null;
    }

    /**
     * Visit a for statement
     *
     * @param node the for statement node
     * @return the result of the visit
     */
    public Object visit(ForStmt node) {

        if (node.getInitExpr() != null) {
            node.getInitExpr().accept(this);
            if (!node.getInitExpr().getExprType().equals("int")) {
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "The type of the init is " + node.getInitExpr().getExprType()
                                + " which is not int.");
            }
            node.getInitExpr().setExprType("int");
        }


        node.getPredExpr().accept(this);
        //the predExpr's type is not "boolean"
        if (!node.getPredExpr().getExprType().equals("boolean")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The type of the predicate is " + node.getPredExpr().getExprType()
                            + " which is not boolean.");
        }
        node.getInitExpr().setExprType("boolean");

        if (node.getUpdateExpr() != null) {
            node.getUpdateExpr().accept(this);
            if (!node.getInitExpr().getExprType().equals("int")) {
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "The type of the init is " + node.getInitExpr().getExprType()
                                + " which is not int.");
            }
            node.getInitExpr().setExprType("int");

        }


        currentSymbolTable.enterScope();
        node.getBodyStmt().accept(this);
        currentSymbolTable.exitScope();
        return null;

    }

    /**
     * visit an if statement
     *
     * @param node the if statement node
     * @return the result of the visit
     */
    public Object visit(IfStmt node) {
        node.getPredExpr().accept(this);
        //the predExpr's type is not "boolean"
        if (!node.getPredExpr().getExprType().equals("boolean")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The type of the predicate is " + node.getPredExpr().getExprType()
                            + " which is not boolean.");
        }
        node.getPredExpr().setExprType("boolean");
        currentSymbolTable.enterScope();
        node.getThenStmt().accept(this);
        if (node.getElseStmt() != null) {
            node.getElseStmt().accept(this);
        }
        currentSymbolTable.exitScope();
        return null;
    }

    /**
     * Visit a instanceof expression node
     *
     * @param node the instanceof expression node
     * @return null
     */
    public Object visit(InstanceofExpr node) {
        node.getExpr().accept(this);

        String leftExpr = node.getExprType();
        String rightType = node.getType();

        if (!isSubType(rightType, leftExpr)) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The left expression of type " + leftExpr +
                            " cannot be cast to the inconvertible right-hand type " + rightType);
        }

        node.setUpCheck(true);
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a method node
     *
     * @param node the Method node to visit
     * @return null
     */
    public Object visit(Method node) {
        System.out.println("VISITING METHOD: " + node.getName());
        System.out.println(currentClass.getName());
        //if the node's return type is not a defined type and not "void"
        if (!isDefinedType(node.getReturnType()) && !node.getReturnType().equals("void")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The return type " + node.getReturnType() + " of the method "
                            + node.getName() + " is undefined.");
        }

        //create a new scope for the method body
        currentSymbolTable.enterScope();
        node.getFormalList().accept(this);
//        System.out.println("HERE: " + node.getStmtList().getSize());
        node.getStmtList().accept(this);
        System.out.println("HERE1");
        currentSymbolTable.exitScope();
        System.out.println("HERE2");
        return null;
    }

    /**
     * Visits a new array expression node
     *
     * @param node the new array expression node
     * @return null
     */
    public Object visit(NewArrayExpr node) {
        node.getSize().accept(this);
        if (!node.getSize().getExprType().equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The size of the array should be an int, but " +
                            "it has been defined as " + node.getSize().getExprType());
            node.getSize().setExprType("int"); // to allow analysis to continue
        }

        if (!isDefinedType(node.getType())) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The type " + node.getType() + " does not exist.");
            node.setExprType("Object"); // to allow analysis to continue
        } else {
            node.setExprType(node.getType());
        }


        return null;
    }

    /**
     * Visit a new expression node
     *
     * @param node the new expression node
     * @return null
     */
    public Object visit(NewExpr node) {
        //the node's type is not a defined class type
        Map curClassMap = currentClass.getClassMap();//.containsKey(type);

        if (!curClassMap.containsKey(node.getType())) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The type " + node.getType() + " does not exist.");
            node.setExprType("Object"); // to allow analysis to continue
        } else {
            node.setExprType(node.getType());
        }
        return null;
    }

    /**
     * Visit the return statement node
     *
     * @param node the return statement node
     * @return
     */
    public Object visit(ReturnStmt node) {
        node.getExpr().accept(this);
        return null;
    }

    /**
     * Visit a unary decrement expression node
     *
     * @param node the unary decrement expression node
     * @return null
     */
    public Object visit(UnaryDecrExpr node) {
        node.getExpr().accept(this);
        String type = node.getExpr().getExprType();
        if (!type.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The -- operator should only be used with int" +
                            " not " + type + " expressions.");
        }
        node.setExprType("int"); //to continue checking

        return null;
    }

    /**
     * Visit a unary increment expression node
     *
     * @param node the unary increment expression node
     * @return null
     */
    public Object visit(UnaryIncrExpr node) {
        node.getExpr().accept(this);
        String type = node.getExpr().getExprType();
        if (!type.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The ++ operator should only be used with int" +
                            " not " + type + " expressions.");
        }
        node.setExprType("int"); //to continue checking

        return null;
    }

    /**
     * Visit a unary negation expression node
     *
     * @param node the unary negation expression node
     * @return null
     */
    public Object visit(UnaryNegExpr node) {
        node.getExpr().accept(this);
        String type = node.getExpr().getExprType();
        if (!type.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The - operator should only be used with int" +
                            " not " + type + " expressions.");
        }
        node.setExprType("int"); //to continue checking

        return null;
    }

    /**
     * Visit a unary NOT expression node
     *
     * @param node the unary NOT expression node
     * @return null
     */
    public Object visit(UnaryNotExpr node) {
        node.getExpr().accept(this);
        String type = node.getExpr().getExprType();
        if (!type.equals("boolean")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The not (!) operator applies only to boolean expressions," +
                            " not " + type + " expressions.");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a Variable Expression node
     *
     * @param node
     * @return null
     */
    public Object visit(VarExpr node) {
        if (currentSymbolTable.peek(node.getName()) != null) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The variable name " + node.getName() +
                            " you are trying to use already exists in this scope.");
        }

        node.getRef().accept(this);
        node.setExprType(node.getRef().getExprType());
        return null;

    }

    /**
     * Visit a while statement node
     *
     * @param node the while statement node
     * @return null
     */
    public Object visit(WhileStmt node) {
        node.getPredExpr().accept(this);
        //the predExpr's type is not "boolean"
        if (!node.getPredExpr().getExprType().equals("boolean")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The type of the predicate is " + node.getPredExpr().getExprType()
                            + " which is not boolean.");
        }
        currentSymbolTable.enterScope();
        node.getBodyStmt().accept(this);
        currentSymbolTable.exitScope();
        return null;
    }

}
