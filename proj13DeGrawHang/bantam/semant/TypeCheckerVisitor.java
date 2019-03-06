/**
 * File: TypeCheckerVisitor
 * User: djskrien
 * Date: 1/3/19
 */

package proj13DeGrawHang.bantam.semant;

import proj13DeGrawHang.bantam.ast.*;
import proj13DeGrawHang.bantam.util.ClassTreeNode;
import proj13DeGrawHang.bantam.util.ErrorHandler;
import proj13DeGrawHang.bantam.util.Error;
import proj13DeGrawHang.bantam.util.SymbolTable;
import proj13DeGrawHang.bantam.visitor.Visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * This visitor find the types of all expression nodes and sets the type field
 * of the nodes.  It reports an error for any type incompatability.
 */
public class TypeCheckerVisitor extends Visitor
{
    private ClassTreeNode currentClass;
    private Method currentMethod;
    private ErrorHandler errorHandler;
    private SymbolTable currentSymbolTable;
    private int currentClassFieldLevel; //level of class fields in currentSymbolTable
    private Stack<Stmt> currentLoop;

    public TypeCheckerVisitor(ErrorHandler errorHandler, ClassTreeNode root) {
        this.errorHandler = errorHandler;
        this.currentClass = root; // the Object class
        this.currentMethod = null;
        this.currentSymbolTable = null;
        this.currentLoop = new Stack<>();
    }

    /*
     * CLASS INVARIANT:  Every visit method sets the type of the Expr node being
     *                   visited to a valid type.  If the node's calculated type
     *                   was illegal, an error was reported and the node's type
     *                   was set to the type it should have been or to
     *                   a generic type like "Object".
     */

    /**
     * returns true if the first type is a subtype of the second type
     * It assumes t1 and t2 are legal types.
     *
     * @param t1 the String name of the first type
     * @param t2 the String name of the second type
     * @return true if t1 is a subtype of t2
     */
    private boolean isSubtype(String t1, String t2) {
        if (t1.equals("null") && !isPrimitiveType(t2)) {
            return true;
        }
        if (t1.equals("int") || t2.equals("int")) {
            return t2.equals(t1);
        }
        if (t1.equals("boolean") || t2.equals("boolean")) {
            return t2.equals(t1);
        }
        if (t1.endsWith("[]") || t2.endsWith("[]")) {
            return t2.endsWith("[]") && t1.endsWith("[]") && isSubtype(t1.substring(0,
                    t1.length() - 2), t2.substring(0, t2.length() - 2));
        }
        ClassTreeNode t1Node = currentClass.lookupClass(t1);
        ClassTreeNode t2Node = currentClass.lookupClass(t2);
        while (t1Node != null) {
            if (t1Node == t2Node) {
                return true;
            }
            t1Node = t1Node.getParent();
        }
        return false;
    }

    private boolean isPrimitiveType(String t2) {
        return t2.equals("int") || t2.equals("boolean");
    }

    private boolean typeHasBeenDeclared(String type) {
        return isPrimitiveType(type) || currentClass.lookupClass(type) != null
                || type.endsWith("[]") && typeHasBeenDeclared(type.substring(0, type.length() - 2));
    }


    private void registerError(ASTNode node, String message) {
        errorHandler.register(Error.Kind.SEMANT_ERROR,
                currentClass.getASTNode().getFilename(), node.getLineNum(), message);
    }

    /**
     * Visit a class node
     *
     * @param node the class node
     * @return result of the visit
     */
    public Object visit(Class_ node) {
        // set the currentClass to this class
        currentClass = currentClass.lookupClass(node.getName());
        currentSymbolTable = currentClass.getVarSymbolTable();
        currentClassFieldLevel = currentSymbolTable.getCurrScopeLevel();
        node.getMemberList().accept(this);
        return null;
    }

    /**
     * Visit a field node
     *
     * @param node the field node
     * @return result of the visit
     */
    public Object visit(Field node) {
        //The fields have already been added to the symbol table by the SemanticAnalyzer,
        // so the only thing to check is the compatibility of the init expr's type with
        //the field's type.
        if (!typeHasBeenDeclared(node.getType())) {
            registerError(node,"The declared type " + node.getType() +
                    " of the field " + node.getName() + " is undefined.");
        }
        Expr initExpr = node.getInit();
        if (initExpr != null) {
            initExpr.accept(this);
            if (!isSubtype(initExpr.getExprType(), node.getType())) {
                registerError(node,"The type of the initializer is " + initExpr.getExprType() + " "
                                + "which is not compatible with the " + node.getName() + " field's type " + node.getType());
            }
        }
        //Note: if there is no initial value, then leave it to the Code Generator to
        //      initialize it to the default value
        return null;
    }

    /**
     * Visit a method node
     *
     * @param node the method node
     * @return result of the visit
     */
    public Object visit(Method node) {
        if (!typeHasBeenDeclared(node.getReturnType()) && !node.getReturnType().equals(
                "void")) {
            registerError(node,"The return type " + node.getReturnType() +
                    " of the method " + node.getName() + " is undefined.");
        }

        //create a new scope for the method
        currentSymbolTable.enterScope();
        currentMethod = node;
        node.getFormalList().accept(this);
        node.getStmtList().accept(this);

        //check that non-void methods end with a return stmt
        if(! node.getReturnType().equals("void")) {
            StmtList sList = node.getStmtList();
            if( sList.getSize() == 0) {
                registerError(node, "Methods with non-void return type "
                        + "must end with a return statement.");
            }
            ASTNode stmt = sList.get(sList.getSize()-1);
            if( ! (stmt instanceof ReturnStmt) ) {
                registerError(node, "Methods with non-void return type "
                        + "must end with a return statement.");
            }
        }
        currentMethod = null;
        currentSymbolTable.exitScope();
        return null;
    }

    /**
     * Visit a formal node
     *
     * @param node the formal node
     * @return result of the visit
     */
    public Object visit(Formal node) {
        if (!typeHasBeenDeclared(node.getType())) {
            registerError(node,"The declared type " + node.getType() +
                    " of the formal parameter " + node.getName() + " is undefined.");
        }
        // add it to the current scope
        currentSymbolTable.add(node.getName(), node.getType());
        return null;
    }

    /**
     * Visit a declaration statement node
     *
     * @param node the declaration statement node
     * @return result of the visit
     */
    public Object visit(DeclStmt node) {
        Expr initExpr = node.getInit();
        initExpr.accept(this);
        if (initExpr.getExprType().equals("null")) {
            registerError(node,"We cannot infer a type for the local variable " +
                    node.getName() + " because it was initialized to null.");
            node.setType("Object"); // to allow analysis to continue
        }
        else {
            node.setType(initExpr.getExprType());
        }

        //check that there are no vars already declared with this name in the curr method
        SymbolTable clone = currentSymbolTable.clone();
        clone.setParent(null);
        int level = clone.getScopeLevel(node.getName());
        if (level > 1) //it's not a field
        {
            registerError(node,"There is already a local variable with the name " + node.getName());
        }
        // check that the var name is not a reserved word
        if (SemanticAnalyzer.reservedIdentifiers.contains(node.getName())) {
            registerError(node,"The name of the variable is: " + node.getName() + " which is illegal.");
        }

        currentSymbolTable.add(node.getName(), node.getType());
        return null;
    }

    /**
     * Visit an if statement node
     *
     * @param node the if statement node
     * @return result of the visit
     */
    public Object visit(IfStmt node) {
        node.getPredExpr().accept(this);
        String predExprType = node.getPredExpr().getExprType();
        if (!"boolean".equals(predExprType)) {
            registerError(node,"The type of the predicate is " +
                    (predExprType != null ? predExprType : "unknown") + ", not boolean.");
        }
        currentSymbolTable.enterScope();
        node.getThenStmt().accept(this);
        currentSymbolTable.exitScope();
        if (node.getElseStmt() != null) {
            currentSymbolTable.enterScope();
            node.getElseStmt().accept(this);
            currentSymbolTable.exitScope();
        }
        return null;
    }

    /**
     * Visit a while statement node
     *
     * @param node the while statement node
     * @return result of the visit
     */
    public Object visit(WhileStmt node) {
        node.getPredExpr().accept(this);
        if (!isSubtype(node.getPredExpr().getExprType(), "boolean")) {
            registerError(node,"The type of the predicate is " +
                    node.getPredExpr().getExprType() + " which is not boolean.");
        }
        currentSymbolTable.enterScope();
        currentLoop.push(node);
        node.getBodyStmt().accept(this);
        currentLoop.pop();
        currentSymbolTable.exitScope();
        return null;
    }

    /**
     * Visit a for statement node
     *
     * @param node the for statement node
     * @return result of the visit
     */
    public Object visit(ForStmt node) {
        if (node.getInitExpr() != null) {
            node.getInitExpr().accept(this);
        }
        if (node.getPredExpr() != null) {
            node.getPredExpr().accept(this);
            if (!isSubtype(node.getPredExpr().getExprType(), "boolean")) {
                registerError(node,"The type of the predicate is " +
                        node.getPredExpr().getExprType() + " which is not boolean.");
            }
        }
        if (node.getUpdateExpr() != null) {
            node.getUpdateExpr().accept(this);
        }
        currentSymbolTable.enterScope();
        currentLoop.push(node);
        node.getBodyStmt().accept(this);
        currentLoop.pop();
        currentSymbolTable.exitScope();
        return null;
    }

    /**
     * Visit a break statement node
     *
     * @param node the break statement node
     * @return result of the visit
     */
    public Object visit(BreakStmt node) {
        if (currentLoop.isEmpty()) {
            registerError(node,"Break statement is not in the body of any loop.");
        }
        return null;
    }

    /**
     * Visit a block statement node
     *
     * @param node the block statement node
     * @return result of the visit
     */
    public Object visit(BlockStmt node) {
        currentSymbolTable.enterScope();
        node.getStmtList().accept(this);
        currentSymbolTable.exitScope();
        return null;
    }

    /**
     * Visit a return statement node
     *
     * @param node the return statement node
     * @return result of the visit
     */
    public Object visit(ReturnStmt node) {
        if (node.getExpr() != null) {
            node.getExpr().accept(this);
            if (!isSubtype(node.getExpr().getExprType(), currentMethod.getReturnType())) {
                registerError(node,"The type of the return expr is " +
                        node.getExpr().getExprType() + " which is not compatible with the " +
                        currentMethod.getName() + " method's return type " + currentMethod.getReturnType());
            }
        }
        else if (!currentMethod.getReturnType().equals("void")) {
            registerError(node, "The type of the method " + currentMethod.getName() +
                    " is not void and so return statements in it must return a value.");
        }
        return null;
    }

    /**
     * Visit a dispatch expression node
     *
     * @param node the dispatch expression node
     * @return the type of the expression
     */
    public Object visit(DispatchExpr node) {
        ClassTreeNode classOfMethod = currentClass; //assume initially that ref is "this"
        if (node.getRefExpr() != null) {
            node.getRefExpr().accept(this);
            String className = node.getRefExpr().getExprType();
            classOfMethod = currentClass.lookupClass(className);
            if (classOfMethod == null) {
                registerError(node,"The method " + node.getMethodName() +
                        " is supposed to be in class " + className +
                        ", but there is no such class.");
                node.setExprType("Object"); // to allow analysis to continue
                return null;
            }
        }
        //check that the class has a method with this name
        Method method =
                (Method) classOfMethod.getMethodSymbolTable().lookup(node.getMethodName());
        if (method == null) {
            registerError(node,"The method " + node.getMethodName() +
                    " does not exist in the class " + classOfMethod.getName());
        }
        else { //we found a method in the class with the given name
            //Check that the actuals have types compatible with the formals
            List<String> actualTypes = (List<String>) node.getActualList().accept(this);
            List<String> formalTypes = getFormalTypesList(method);
            if (actualTypes.size() != formalTypes.size()) {
                registerError(node,"The method " + node.getMethodName() + " has " +
                        formalTypes.size() + " parameters, not " + actualTypes.size());
            }
            else {
                for (int i = 0; i < formalTypes.size(); i++) {
                    String formalType = formalTypes.get(i);
                    String actualType = actualTypes.get(i);
                    if (!isSubtype(actualType, formalType)) {
                        registerError(node,"The method " + node.getMethodName() +
                                " has type " + formalType + " for its index " + i +
                                " parameter, but the actual parameter has type " + actualType);
                    }
                }
            }
        }
        node.setExprType(method == null ? "Object" : method.getReturnType());
        return null;
    }

    /**
     * returns a list of the types of the formal parameters
     *
     * @param method the methods whose formal parameter types are desired
     * @return a List of Strings (the types of the formal parameters)
     */
    private List<String> getFormalTypesList(Method method) {
        List<String> result = new ArrayList<>();
        for (ASTNode formal : method.getFormalList())
            result.add(((Formal) formal).getType());
        return result;
    }

    /**
     * Visit a list node of expressions
     *
     * @param node the expression list node
     * @return result of the visit
     */
    public Object visit(ExprList node) {
        List<String> typesList = new ArrayList<>();
        for (ASTNode expr : node) {
            expr.accept(this);
            typesList.add(((Expr) expr).getExprType());
        }
        //return a List<String> of the types of the expressions
        return typesList;
    }

    /**
     * Visit a new expression node
     *
     * @param node the new expression node
     * @return the type of the expression
     */
    public Object visit(NewExpr node) {
        if (currentClass.lookupClass(node.getType()) == null) {
            registerError(node,"The type " + node.getType() + " does not exist.");
            node.setExprType("Object"); // to allow analysis to continue
        }
        else {
            node.setExprType(node.getType());
        }
        return null;
    }

    /**
     * Visit a new array expression node
     *
     * @param node the new array expression node
     * @return the type of the expression
     */
    public Object visit(NewArrayExpr node) {
        node.getSize().accept(this);
        if (!node.getSize().getExprType().equals("int")) {
            registerError(node,"The size of the array must be an integer.");
        }
        if (!typeHasBeenDeclared(node.getType())) {
            registerError(node,"The type " + node.getType() + " does not exist.");
        }
        node.setExprType(node.getType() + "[]");
        return null;
    }

    /**
     * Visit an instanceof expression node
     *
     * @param node the instanceof expression node
     * @return the type of the expression
     */
    public Object visit(InstanceofExpr node) {
        if (currentClass.lookupClass(node.getType()) == null) {
            registerError(node,"The reference type " + node.getType() + " does not exist.");
        }
        node.getExpr().accept(this);
        if (isSubtype(node.getExpr().getExprType(), node.getType())) {
            node.setUpCheck(true);
        }
        else if (isSubtype(node.getType(), node.getExpr().getExprType())) {
            node.setUpCheck(false);
        }
        else {
            registerError(node,"You can't compare type " +
                    node.getExpr().getExprType() + "to " + "incompatible type " + node.getType() + ".");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a cast expression node
     *
     * @param node the cast expression node
     * @return the type of the expression
     */
    public Object visit(CastExpr node) {
        if (currentClass.lookupClass(node.getType()) == null) {
            registerError(node,"The type " + node.getType() + " does not exist.");
        }
        node.getExpr().accept(this);
        if (isSubtype(node.getExpr().getExprType(), node.getType())) {
            node.setUpCast(true);
        }
        else if (isSubtype(node.getType(), node.getExpr().getExprType())) {
            node.setUpCast(false);
        }
        else {
            registerError(node,"You can't cast from type " +
                    node.getExpr().getExprType() + " to incompatible type " + node.getType() + ".");
        }
        node.setExprType(node.getType());
        return null;
    }

    /**
     * Visit an assignment expression node
     *
     * @param node the assignment expression node
     * @return the type of the expression
     */
    public Object visit(AssignExpr node) {
        String varType = null;
        String varName = node.getName();
        String refName = node.getRefName();
        if (refName == null) { //local var or field of "this"
            varType = (String) currentSymbolTable.lookup(varName);
        }
        else if (refName.equals("this")) {
            varType = (String) currentSymbolTable.lookup(varName, currentClassFieldLevel);
        }
        else if (refName.equals("super")) {
            varType = (String) currentSymbolTable.lookup(varName,
                    currentClassFieldLevel - 1);
        }
        else if (varName.equals("length")) { // <arrayName>.length is final
            varType = "int"; // to allow analysis to continue
            String arrayType = (String) currentSymbolTable.lookup(refName);
            if (arrayType.endsWith("[]")) {
                registerError(node,"The length field of an array is final.");
            }
        }
        else { // refName is not "this" or "super" or varName is not "length" for an array
            String refVarTypeName = (String) currentSymbolTable.lookup(refName);
            if (refVarTypeName == null) {
                registerError(node,"The identifier " + refName + " was never declared.");
                refVarTypeName = "Object";
            }
            ClassTreeNode refVarType = currentClass.lookupClass(refVarTypeName);
            if (refVarType == null) {
                registerError(node,"The identifier " + refVarTypeName + " is not the name of " +
                                "a declared or built-in type.");
                refVarType = currentClass.lookupClass("Object");
            }
            SymbolTable refTable = refVarType.getVarSymbolTable();
            varType = (String) refTable.lookup(varName);
        }

        if (varType == null) { // the type was not found
            registerError(node,"The identifier " + varName + " was never declared.");
            varType = "Object"; // to allow analysis to continue
        }

        // typecheck the expr and check compatability
        node.getExpr().accept(this);
        if(node.getExpr().getExprType() == null) {
            System.out.println("Found a null expr type");
        }
        if (!isSubtype(node.getExpr().getExprType(), varType)) {
            registerError(node,"The type of the expr is " +
                            node.getExpr().getExprType() + " " +
                            "which is not compatible with the " + varName +
                            " variable's declared type " + varType);
        }
        node.setExprType(varType);
        return null;
    }

    /**
     * Visit an array assignment expression node
     *
     * @param node the array assignment expression node
     * @return the type of the expression
     */
    public Object visit(ArrayAssignExpr node) {
        //check that the index's type is "int"
        node.getIndex().accept(this);
        if (!node.getIndex().getExprType().equals("int")) {
            registerError(node,"The index into the array must be an integer.");
        }

        //check that the refName is legit
        String varType = null;
        String varName = node.getName();
        String refName = node.getRefName();
        if (refName == null) { //local var or field of "this"
            varType = (String) currentSymbolTable.lookup(varName);
        }
        else if (refName.equals("this")) {
            varType = (String) currentSymbolTable.lookup(varName, currentClassFieldLevel);
        }
        else if (refName.equals("super")) {
            varType = (String) currentSymbolTable.lookup(varName,
                    currentClassFieldLevel - 1);
        }
        else { // if refName is not "this" or "super"
            String refVarTypeName = (String) currentSymbolTable.lookup(refName);
            if (refVarTypeName == null) {
                registerError(node,"The identifier " + refName + " was never declared.");
                refVarTypeName = "Object"; // to allow us to continue
            }
            ClassTreeNode refVarType = currentClass.lookupClass(refVarTypeName);
            if (refVarType == null) {
                registerError(node,"The identifier " + refVarTypeName +
                        " is not the name of a declared or built-in type.");
                refVarType = currentClass.lookupClass("Object");  // to allow us to continue
            }
            SymbolTable refTable = refVarType.getVarSymbolTable();
            varType = (String) refTable.lookup(varName);
        }

        if (varType == null) {  // lookup of varName failed
            registerError(node,"The identifier " + varName + " was never declared.");
            varType = "Object[]"; //to allow analysis to continue
        }
        else if (!varType.endsWith("[]")) {
            registerError(node,"The identifier " + varName + " is not an array type.");
        }

        //finally, check that the var's type is comp with the expr field's type.
        node.getExpr().accept(this);
        if (!isSubtype(node.getExpr().getExprType(), varType.substring(0,
                varType.length() - 2))) {
            registerError(node,"The type of the expr is " + node.getExpr().getExprType() +
                    " which is not compatible with the " + varName + " variable's base type " + varType);
        }
        node.setExprType(varType.substring(0, varType.length() - 2));
        return null;
    }

    /**
     * Visit a variable expression node
     *
     * @param node the variable expression node
     * @return the type of the expression
     */
    public Object visit(VarExpr node) {
        //check that ref.name is legit
        String varType;
        String varName = node.getName();
        if (node.getRef() == null && node.getName().equals("null")) { // expr = "null"
            varType = "null";  // special case
        }
        else if (node.getRef() == null) { //local var or field of "this"
            varType = (String) currentSymbolTable.lookup(varName);
        }
        else if ((node.getRef() instanceof VarExpr) &&
                ((VarExpr) node.getRef()).getName().equals("this")) {
            varType = (String) currentSymbolTable.lookup(varName, currentClassFieldLevel-1);
            if ((((VarExpr) node.getRef()).getRef() != null)) {
                registerError(node,"The identifier \"this\" cannot follow a dot");
            }
        }
        else if ((node.getRef() instanceof VarExpr) &&
                ((VarExpr) node.getRef()).getName().equals("super")) {
            varType = (String) currentSymbolTable.lookup(varName,
                    currentClassFieldLevel - 1);
            if ((((VarExpr) node.getRef()).getRef() != null)) {
                registerError(node, "The identifier \"super\" cannot follow a dot");
            }
        }
        else { // ref is not null, "this", or "super"
            // check the ref and get its type name
            node.getRef().accept(this);
            String refTypeName = node.getRef().getExprType();
            if( refTypeName.endsWith("[]") && varName.equals("length")) {
                varType = "int";
            }
            else if( refTypeName.endsWith("[]")) {
                registerError(node,"The only field of an array" +
                        " is \"length\"");
                varType = "Object"; //to allow analysis to continue

            }
            else if( refTypeName.equals("int") || refTypeName.equals("boolean")) {
                registerError(node, "A primitive type cannot have a field" +
                        " named " + varName);
                varType = "Object";
            }
            else {
                ClassTreeNode refType = currentClass.lookupClass(refTypeName);
                SymbolTable refTable = refType.getVarSymbolTable();
                int refFieldLevel = getClassFieldLevel(refType);
                varType = (String) refTable.lookup(varName, refFieldLevel-1); //check if it is a field
            }
        }

        if (varType == null) { // lookup of varType failed
            registerError(node,"The identifier " + varName +
                    " was not declared before it was used.");
            varType = "Object"; //to allow analysis to continue
        }
        node.setExprType(varType);
        return null;
    }

    private int getClassFieldLevel(ClassTreeNode node) {
        int level = 1;
        while(node.getParent() != null) {
            level++;
            node = node.getParent();
        }
        return level;
    }

    /**
     * Visit an array expression node
     *
     * @param node the array expression node
     * @return the type of the expression
     */
    public Object visit(ArrayExpr node) {
        //check that the index's type is "int"
        node.getIndex().accept(this);
        if (!node.getIndex().getExprType().equals("int")) {
            registerError(node, "The index into the array must be an integer.");
        }
        //check the ref and check whether the ref's type is an array type
        node.getRef().accept(this);
        String refType = node.getRef().getExprType();
        if (! refType.endsWith("[]")) {
            registerError(node,"The  expression is not an array type.");
            node.setExprType(refType); // to continue analysis
        }
        else {
            node.setExprType(refType.substring(0, refType.length() - 2));
        }
        return null;
    }

    /**
     * Visit a binary comparison equals expression node
     *
     * @param node the binary comparison equals expression node
     * @return the type of the expression
     */
    public Object visit(BinaryCompEqExpr node) {
        String[] types = getLeftAndRightTypes(node);
        if (types[0] == null || types[1] == null) {
            return null; //error in one expr, so skip further checking
        }
        if (!(isSubtype(types[0], types[1]) || isSubtype(types[1], types[0]))) {
            registerError(node,"The " + "two values being compared for equality are not compatible " + "types.");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a binary comparison not equals expression node
     *
     * @param node the binary comparison not equals expression node
     * @return the type of the expression
     */
    public Object visit(BinaryCompNeExpr node) {
        String[] types = getLeftAndRightTypes(node);
        if (!(isSubtype(types[0], types[1]) || isSubtype(types[1], types[0]))) {
            registerError(node,"The two values being compared for equality are not compatible types.");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a binary comparison less than expression node
     *
     * @param node the binary comparison less than expression node
     * @return the type of the expression
     */
    public Object visit(BinaryCompLtExpr node) {
        String[] types = getLeftAndRightTypes(node);
        if (!(types[0].equals("int") && types[1].equals("int"))) {
            registerError(node,"The two values being compared by \"<\" are not both ints.");
        }
        node.setExprType("boolean");
        return null;
    }

    private String[] getLeftAndRightTypes(BinaryExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        return new String[]{type1,type2};
    }

    /**
     * Visit a binary comparison less than or equal to expression node
     *
     * @param node the binary comparison less than or equal to expression node
     * @return the type of the expression
     */
    public Object visit(BinaryCompLeqExpr node) {
        String[] types = getLeftAndRightTypes(node);
        if (!(types[0].equals("int") && types[1].equals("int"))) {
            registerError(node,"The  two values being compared by \"<=\" are not both ints.");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a binary comparison greater than expression node
     *
     * @param node the binary comparison greater than expression node
     * @return the type of the expression
     */
    public Object visit(BinaryCompGtExpr node) {
        String[] types = getLeftAndRightTypes(node);
        if (!(types[0].equals("int") && types[1].equals("int"))) {
            registerError(node,"The two values being compared by \">\" are not both ints.");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a binary comparison greater than or equal to expression node
     *
     * @param node the binary comparison greater to or equal to expression node
     * @return the type of the expression
     */
    public Object visit(BinaryCompGeqExpr node) {
        String[] types = getLeftAndRightTypes(node);
        if (!(types[0].equals("int") && types[1].equals("int"))) {
            registerError(node,"The  two values being compared by \">=\" are not both ints.");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a binary arithmetic plus expression node
     *
     * @param node the binary arithmetic plus expression node
     * @return the type of the expression
     */
    public Object visit(BinaryArithPlusExpr node) {
        String[] types = getLeftAndRightTypes(node);
        if (!(types[0].equals("int") && types[1].equals("int"))) {
            registerError(node,"The two values being added are not both ints.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a binary arithmetic minus expression node
     *
     * @param node the binary arithmetic minus expression node
     * @return the type of the expression
     */
    public Object visit(BinaryArithMinusExpr node) {
        String[] types = getLeftAndRightTypes(node);
        if (!(types[0].equals("int") && types[1].equals("int"))) {
            registerError(node,"The two values being subtraced are not both ints.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a binary arithmetic times expression node
     *
     * @param node the binary arithmetic times expression node
     * @return the type of the expression
     */
    public Object visit(BinaryArithTimesExpr node) {
        String[] types = getLeftAndRightTypes(node);
        if (!(types[0].equals("int") && types[1].equals("int"))) {
            registerError(node,"The two values being multiplied are not both ints.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a binary arithmetic divide expression node
     *
     * @param node the binary arithmetic divide expression node
     * @return the type of the expression
     */
    public Object visit(BinaryArithDivideExpr node) {
        String[] types = getLeftAndRightTypes(node);
        if (!(types[0].equals("int") && types[1].equals("int"))) {
            registerError(node,"The two values being divided are not both ints.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a binary arithmetic modulus expression node
     *
     * @param node the binary arithmetic modulus expression node
     * @return the type of the expression
     */
    public Object visit(BinaryArithModulusExpr node) {
        String[] types = getLeftAndRightTypes(node);
        if (!(types[0].equals("int") && types[1].equals("int"))) {
            registerError(node,"The two values being operated on with % are not both ints.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a binary logical AND expression node
     *
     * @param node the binary logical AND expression node
     * @return the type of the expression
     */
    public Object visit(BinaryLogicAndExpr node) {
        String[] types = getLeftAndRightTypes(node);
        if (!(types[0].equals("boolean") && types[1].equals("boolean"))) {
            registerError(node,"The two values being operated on with && are not both booleans" + ".");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a binary logical OR expression node
     *
     * @param node the binary logical OR expression node
     * @return the type of the expression
     */
    public Object visit(BinaryLogicOrExpr node) {
        String[] types = getLeftAndRightTypes(node);
        if (!(types[0].equals("boolean") && types[1].equals("boolean"))) {
            registerError(node,"The two values being operated on with || are not both booleans" + ".");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a unary negation expression node
     *
     * @param node the unary negation expression node
     * @return the type of the expression
     */
    public Object visit(UnaryNegExpr node) {
        node.getExpr().accept(this);
        String type = node.getExpr().getExprType();
        if (!(type.equals("int"))) {
            registerError(node,"The value being negated is of type " + type + ", not int.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a unary NOT expression node
     *
     * @param node the unary NOT expression node
     * @return the type of the expression
     */
    public Object visit(UnaryNotExpr node) {
        node.getExpr().accept(this);
        String type = node.getExpr().getExprType();
        if (!type.equals("boolean")) {
            registerError(node,"The not (!) operator applies only to boolean expressions," +
                            " not " + type + " expressions.");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a unary increment expression node
     *
     * @param node the unary increment expression node
     * @return the type of the expression
     */
    public Object visit(UnaryIncrExpr node) {
        if (!(node.getExpr() instanceof VarExpr)) {
            registerError(node,"The  expression being incremented can only be " +
                    "a variable name with an optional \"this.\" or \"super.\" prefix.");
        }
        node.getExpr().accept(this);
        String type = node.getExpr().getExprType();
        if (!(type.equals("int"))) {
            registerError(node,"The value being incremented is of type " + type + ", not int.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a unary decrement expression node
     *
     * @param node the unary decrement expression node
     * @return the type of the expression
     */
    public Object visit(UnaryDecrExpr node) {
        node.getExpr().accept(this);
        String type = node.getExpr().getExprType();
        if (!(type.equals("int"))) {
            registerError(node,"The value being decremented is of type " + type + ", not int.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit an int constant expression node
     *
     * @param node the int constant expression node
     * @return the type of the expression
     */
    public Object visit(ConstIntExpr node) {
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a boolean constant expression node
     *
     * @param node the boolean constant expression node
     * @return the type of the expression
     */
    public Object visit(ConstBooleanExpr node) {
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a string constant expression node
     *
     * @param node the string constant expression node
     * @return the type of the expression
     */
    public Object visit(ConstStringExpr node) {
        node.setExprType("String");
        return null;
    }

}
