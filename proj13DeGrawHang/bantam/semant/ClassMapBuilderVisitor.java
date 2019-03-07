/**
 * File: ClassMapBuilderVisitor
 * User: djskrien
 * Date: 1/2/14
 */

package proj13DeGrawHang.bantam.semant;

import proj13DeGrawHang.bantam.ast.Class_;
import proj13DeGrawHang.bantam.util.ClassTreeNode;
import proj13DeGrawHang.bantam.util.ErrorHandler;
import proj13DeGrawHang.bantam.util.Error;
import proj13DeGrawHang.bantam.visitor.Visitor;

import java.util.Hashtable;

/**
 * This class visits the AST to find all class declarations and add an entry
 * for those classes in the classMap.
 */
public class ClassMapBuilderVisitor extends Visitor {
    private Hashtable<String, ClassTreeNode> classMap;
    private ErrorHandler errorHandler;

    ClassMapBuilderVisitor(Hashtable<String, ClassTreeNode> classMap, ErrorHandler
            errorHandler) {
        this.classMap = classMap;
        this.errorHandler = errorHandler;
    }

    /**
     * adds a new ClassTreeNode for this node to the classMap.
     * @param node the class node
     * @return null
     */
    public Object visit(Class_ node) {
        if(classMap.keySet().contains(node.getName()))
            errorHandler.register(Error.Kind.SEMANT_ERROR,node.getFilename(),
                    node.getLineNum(),"Two classes declared with the same name; " +
                            node.getName());
        else if(SemanticAnalyzer.reservedIdentifiers.contains(node.getName()))
            errorHandler.register(Error.Kind.SEMANT_ERROR,node.getFilename(),
                    node.getLineNum(),"A class cannot be named 'this', 'super'," +
                            "'void', 'int', 'boolean', or 'null'; " +
                            node.getName());
        else {
            ClassTreeNode treeNode = new ClassTreeNode(node, false, true, classMap);
            classMap.put(node.getName(), treeNode);
        }
        return null;
    }
}
