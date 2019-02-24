package proj12DeGrawHangMarcello.bantam.semant;
import proj12DeGrawHangMarcello.bantam.util.*;
import proj12DeGrawHangMarcello.bantam.ast.*;
import proj12DeGrawHangMarcello.bantam.util.Error;
import proj12DeGrawHangMarcello.bantam.visitor.*;

import java.util.Iterator;

public class TypeCheckerVisitor extends Visitor
{
    private ClassTreeNode currentClass;
    private SymbolTable currentSymbolTable;
    private ErrorHandler errorHandler;

    /**
     * Helper method to find if the
     *
     * @param type
     * @return
     */
    private boolean isDefinedType(String type){
        return currentClass.getClassMap().containsKey(type) || type.equals("boolean") || type.equals("int")
                || type.equals("String");
    }

    private boolean isSubType(String node1, String node2){
        if(currentClass.getClassMap().get(node1).getParent().equals("Object")){
            return node1.equals(node2);
        }
        else{
            return currentClass.getClassMap().get(node1).getParent().equals(node2);
        }
    }

    private boolean isDefinedClassType(String type){
        return currentClass.getClassMap().containsKey(type);
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

        //if node's type is not a defined type
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
            if(!isSubType(initExpr.getExprType(),node.getType())){
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "The type of the initializer is " + initExpr.getExprType()
                         + " which is not compatible with the " + node.getName() +
                         " field's type " + node.getType());
            }
        }
        //Note: if there is no initExpr, then leave it to the Code Generator to
        //      initialize it to the default value since it is irrelevant to the
        //      SemanticAnalyzer.
        return null;
    }

    /**
     * Visit a method node
     *
     * @param node the Method node to visit
     * @return null
     */
    public Object visit(Method node) {
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
        node.getStmtList().accept(this);
        currentSymbolTable.exitScope();
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
     * Visit a array assignment expression node
     *
     * @param node the array assignment expression node
     * @return
     */
    public Object visit(ArrayAssignExpr node){
        node.getExpr().accept(this);
        node.getIndex().accept(this);
        if(!node.getIndex().getExprType().equals("int")){
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The index of the array assignment is a" + node.getIndex().getExprType() +
                            " and it should be an integer.");
        }
        node.getIndex().setExprType("int");

        return null;
    }

    //TODO: idk if this is right
    /**
     * Visit an array expression node
     *
     * @param node the array expression node
     * @return
     */
    public Object visit(ArrayExpr node){
        node.getRef().accept(this);
        node.getIndex().accept(this);

        node.setExprType(node.getIndex().getExprType());
        return null;
    }


    //TODO: idk if this is right
    /**
     * Visit an assignment expression node
     *
     * @param node the assignment expression node
     * @return
     */
    public Object visit(AssignExpr node){
        node.getExpr().accept(this);

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
        if(!node.getPredExpr().getExprType().equals("boolean")) {
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

    //TODO: Jackie Implemented idk about this one

    /**
     * Visit a for statement
     * @param node the for statement node
     * @return
     */
    public Object visit(ForStmt node){

        if(node.getInitExpr()!=null){
            node.getInitExpr().accept(this);
            if(!node.getInitExpr().getExprType().equals("int")) {
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "The type of the init is " + node.getInitExpr().getExprType()
                                + " which is not int.");
            }
            node.getInitExpr().setExprType("int");
        }


        node.getPredExpr().accept(this);
        //the predExpr's type is not "boolean"
        if(!node.getPredExpr().getExprType().equals("boolean")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The type of the predicate is " + node.getPredExpr().getExprType()
                            + " which is not boolean.");
        }
        node.getInitExpr().setExprType("boolean");

        if(node.getUpdateExpr()!=null){
            node.getUpdateExpr().accept(this);
            if(!node.getInitExpr().getExprType().equals("int")) {
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

    //TODO: Jackie Implemented

    /**
     * visit an if statement
     * @param node the if statement node
     * @return
     */
    public Object visit(IfStmt node) {
        node.getPredExpr().accept(this);
        //the predExpr's type is not "boolean"
        if(!node.getPredExpr().getExprType().equals("boolean")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The type of the predicate is " + node.getPredExpr().getExprType()
                            + " which is not boolean.");
        }
        node.getPredExpr().setExprType("boolean");
        currentSymbolTable.enterScope();
        node.getThenStmt().accept(this);
        if(node.getElseStmt()!=null){
            node.getElseStmt().accept(this);
        }
        currentSymbolTable.exitScope();
        return null;
    }


    //TODO: NOT DONE YET BISH DO THIS METHOD
    public Object visit(DispatchExpr node){
        node.getRefExpr().accept(this);
        if(currentClass.getMethodSymbolTable().lookup(node.getMethodName())== null){
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The method "+node.getMethodName()+"was not found in the method " +
                            "symbol table.");

        }

        node.getActualList().accept(this);


        //visit the ref expr- check to see if the class method symbol table has that method
        //check if the method takes in the right type of arguments  & right #
        //if the arg of the dispatch is an object make sure the object isnt a subtype of
        //the parameter before throwing an error
        //set type of dispatch expr to the return type of the ref

        node.setExprType(node.getRefExpr().getExprType());
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
        if(isDefinedClassType(node.getType())) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The type " + node.getType() + " does not exist.");
            node.setExprType("Object"); // to allow analysis to continue
        }
        else {
            node.setExprType(node.getType());
        }
        return null;
    }

    public Object visit(BinaryLogicAndExpr node){
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(!type2.equals(type1)||!type1.equals("boolean")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being compared are of types "+ type1
                            +" and " +type2+ ". They should both be of type boolean.");
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
    public Object visit(BinaryLogicOrExpr node){
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(!type2.equals(type1)||!type1.equals("boolean")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being compared are of types "+ type1
                            +" and " +type2+ ". They should both be of type boolean.");
        }
        node.setExprType("boolean");
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
        if(!isSubType(type1,type2)|| !isSubType(type2, type1)) {
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
     *
     * @param node the binary comparison not equals expression node
     * @return null
     */
    public Object visit (BinaryCompNeExpr node){
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        //if neither type1 nor type2 is a subtype of the other
        if(!isSubType(type1,type2)|| !isSubType(type2, type1)) {
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
    public Object visit(BinaryCompGtExpr node){
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        //if neither type1 nor type2 is a subtype of the other
        if(!type2.equals(type1)|| !type1.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being compared for greater than are of types "+ type1
                            +" and " +type2+ ". They should both be of type int.");
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
    public Object visit(BinaryCompLtExpr node){
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        //if neither type1 nor type2 is a subtype of the other
        if(!type2.equals(type1)|| !type1.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being compared for less than are of types "+ type1
                            +" and " +type2+ ". They should both be of type int.");
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
    public Object visit(BinaryCompGeqExpr node){
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        //if neither type1 nor type2 is a subtype of the other
        if(!type2.equals(type1)|| !type1.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being compared for greater than or equal to are of types "+ type1
                            +" and " +type2+ ". They should both be of type int.");
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
    public Object visit(BinaryCompLeqExpr node){
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        //if neither type1 nor type2 is a subtype of the other
        if(!type2.equals(type1)|| !type1.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being compared for less than or equal to are of types "+ type1
                            +" and " +type2+ ". They should both be of type int.");
        }
        node.setExprType("boolean");
        return null;
    }




    //TODO:Jackie implemented this

    /**
     * Visit a binary arithmetic divide expression node
     * @param node the binary arithmetic divide expression node
     * @return null
     */
    public Object visit(BinaryArithDivideExpr node){
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(!type2.equals(type1)|| !type1.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being used in the arithmetic division are of types "+ type1
                            +" and " +type2+ ". They should both be of type int.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a binary arithmetic minus expression node
     *
     * @param node the binary arithmetic minus expression node
     * @return
     */
    public Object visit(BinaryArithMinusExpr node){
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(!type2.equals(type1)|| !type1.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being used in the arithmetic subtraction are of types "+ type1
                            +" and " +type2+ ". They should both be of type int.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a binary arithmetic plus expression node
     *
     * @param node the binary arithmetic plus expression node
     * @return
     */
    public Object visit(BinaryArithPlusExpr node){
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(!type2.equals(type1)|| !type1.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being used in the arithmetic addition are of types "+ type1
                            +" and " +type2+ ". They should both be of type int.");
        }
        node.setExprType("int");

        return null;
    }

    /**
     * Visit a binary arithmetic times expression node
     *
     * @param node the binary arithmetic times expression node
     * @return
     */
    public Object visit(BinaryArithTimesExpr node){
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(!type2.equals(type1)|| !type1.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being used in the arithmetic multiplication are of types "+ type1
                            +" and " +type2+ ". They should both be of type int.");
        }
        node.setExprType("int");

        return null;
    }

    /**
     * Visit a binary arithmetic modulus expression node
     *
     * @param node the binary arithmetic modulus expression node
     * @return
     */
    public Object visit(BinaryArithModulusExpr node){
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(!type2.equals(type1)|| !type1.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being used in the arithmetic modulus are of types "+ type1
                            +" and " +type2+ ". They should both be of type int.");
        }
        node.setExprType("int");

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
        if(!type.equals("boolean")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The not (!) operator applies only to boolean expressions," +
                            " not " + type + " expressions.");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a unary decrement expression node
     *
     * @param node the unary decrement expression node
     * @return null
     */
    public Object visit(UnaryDecrExpr node){
        node.getExpr().accept(this);
        String type = node.getExpr().getExprType();
        if(!type.equals("int")) {
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
    public Object visit(UnaryIncrExpr node){
        node.getExpr().accept(this);
        String type = node.getExpr().getExprType();
        if(!type.equals("int")) {
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
    public Object visit(UnaryNegExpr node){
        node.getExpr().accept(this);
        String type = node.getExpr().getExprType();
        if(!type.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The - operator should only be used with int" +
                            " not " + type + " expressions.");
        }
        node.setExprType("int"); //to continue checking

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
     * Visit a string constant expression node
     *
     * @param node the string constant expression node
     * @return null
     */
    public Object visit(ConstStringExpr node) {
        node.setExprType("String");
        return null;
    }

    //TODO: Look over these 3 methods again

    /**
     * Visit a declaration statement node
     *
     * @param node the declaration statement node
     * @return null
     */
    public Object visit(DeclStmt node){
        if(currentSymbolTable.lookup(node.getName())!=null){
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The variable name " + node.getName() +
                            " you are trying to use already exists in this scope.");
        }

        node.getInit().accept(this);
        return null;
    }

    /**
     * Visit an expression statement node
     *
     * @param node the expression statement node
     * @return null
     */
    public Object visit(ExprStmt node){
        node.getExpr().accept(this);
        return null;
    }

    /**
     * Visit the return statement node
     *
     * @param node the return statement node
     * @return
     */
    public Object visit(ReturnStmt node){
        node.getExpr().accept(this);
        return null;
    }


    //TODO:Questionable

    /**
     * Visit a instanceof expression node
     *
     * @param node the instanceof expression node
     * @return null
     */
    public Object visit(InstanceofExpr node){
        node.getExpr().accept(this);

        String type1 = node.getExprType();
        String type2 = node.getType();

        if(currentClass.getVarSymbolTable().lookup(node.getExprType())==null){
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The type "+ node.getType()+ " you are trying to find an InstanceOf does not exist.");
        }

        node.setExprType("boolean");
        return null;
    }

    //TODO: Double check on this one

    /**
     * Visit a Variable Expression node
     *
     * @param node
     * @return null
     */
    public Object visit(VarExpr node){
        if(currentSymbolTable.lookup(node.getName())!=null){
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The variable name " + node.getName() +
                            " you are trying to use already exists in this scope.");
        }

        node.getRef().accept(this);
        return null;

    }

    //TODO: Error checking needed??
    /**
     * Visit a Program node
     * @param node the program node
     * @return null
     */
    public Object visit(Program node){
        node.getClassList().accept(this);
        return null;
    }

    /**
     * Visit a class node
     * @param node the class node
     * @return null
     */
    public Object visit(Class_ node){
        return null;
    }


    public Object visit(ExprList node){
        for(Iterator iterator = node.iterator(); iterator.hasNext();)
            ((Expr)iterator.next()).accept(this);

        return null;
    }





}
