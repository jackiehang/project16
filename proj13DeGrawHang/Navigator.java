/*
 * File: Navigator.java
 * Names: Lucas DeGraw, Jackie Hang, ChrisMarcello
 * Class: CS 361
 * Project 13
 * Date: March 5, 2018
 */

package proj13DeGrawHang;


import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Window;
import org.fxmisc.richtext.CodeArea;
import proj13DeGrawHang.bantam.ast.ASTNode;
import proj13DeGrawHang.bantam.ast.Class_;
import proj13DeGrawHang.bantam.ast.Field;
import proj13DeGrawHang.bantam.ast.Method;
import proj13DeGrawHang.bantam.semant.SemanticAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class allows the user to navigate to where fields,
 * classes, and methods are declared in the file
 *
 * @author  Lucas DeGraw, Jackie Hang, ChrisMarcello
 * @since   3-5-2019
 */

public class Navigator {
    // all the Class nodes of the Program
    private ArrayList<Class_> classes;
    // keys are the name of the class and values are an arraylist
    // class' fields and methods
    private HashMap<String, ArrayList<ASTNode>> classFieldsAndMethods;
    private CodeArea curCodeArea;

    /**
     * Constructor of the Navigator
     *
     * @param classes arraylist of Class_ nodes
     * @param codeArea current CodeArea
     * @param checker SemanticAnalyzer
     */
    public Navigator(ArrayList<Class_> classes, CodeArea codeArea, SemanticAnalyzer checker) {
        this.classes = classes;
        this.curCodeArea = codeArea;
        classFieldsAndMethods = checker.getClassFieldsAndMethods();
        createNavigatorDialog();
    }

    /**
     * Creates the main Navigator dialog
     * User can choose a class to navigate to
     *the declaration of, declaration of its parent, its fields or its methods
     */
    private void createNavigatorDialog() {
        javafx.scene.control.Dialog<ButtonType> helperDialog = new Dialog<>();
        helperDialog.setTitle("Navigate");

        DialogPane dialogPane = new DialogPane();
        VBox vBox = new VBox();
        ListView<Text> listView = new ListView<>();
        // creates a list view with all available class names
        for (String s : this.classFieldsAndMethods.keySet()) {
            listView.getItems().add(new Text(s));
        }

        listView.setMaxHeight(80);

        Button findDecButton = new Button("Find Declaration");
        findDecButton.setMinWidth(200);
        findDecButton.setOnAction(event -> {
            String name = listView.getSelectionModel().getSelectedItem().getText();
            findClassDeclaration(name);
            dialogPane.getScene().getWindow().hide();
        });


        Button findParentButton = new Button("Find Parent Declaration");
        findParentButton.setMinWidth(200);

        findParentButton.setOnAction(event -> {
            String name = listView.getSelectionModel().getSelectedItem().getText();
            findParentClassDeclaration(name);
            dialogPane.getScene().getWindow().hide();
        });


        Button fieldsButton = new Button("Get Fields");
        fieldsButton.setMinWidth(200);
        fieldsButton.setOnAction(event -> {
            String name = listView.getSelectionModel().getSelectedItem().getText();
            this.createHelperDialog(name, "Field");
            dialogPane.getScene().getWindow().hide();
        });

        Button methodsButton = new Button("Get Methods");
        methodsButton.setMinWidth(200);
        methodsButton.setOnAction(event -> {
            String name = listView.getSelectionModel().getSelectedItem().getText();
            this.createHelperDialog(name, "Method");
            dialogPane.getScene().getWindow().hide();
        });
        vBox.getChildren().addAll(listView, findDecButton, findParentButton, fieldsButton, methodsButton);


        vBox.setSpacing(10);
        vBox.setAlignment(Pos.CENTER);
        dialogPane.setContent(vBox);
        helperDialog.setDialogPane(dialogPane);
        Window window = helperDialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        helperDialog.show();

    }

    /**
     * Creates a dialog with all of the
     * given type. Allows user to select one and
     * find where it was declared
     *
     * @param className class name
     * @param type "Field", or "Method
     */
    private void createHelperDialog(String className, String type) {
        javafx.scene.control.Dialog<ButtonType> helperDialog = new Dialog<>();
        helperDialog.setTitle(type);

        DialogPane dialogPane = new DialogPane();
        dialogPane.setHeaderText("Choose " + type);
        VBox vBox = new VBox();
        ListView<Text> listView = new ListView<>();

        //putting the proper node names in the listview
        switch (type) {
            case "Field":
                for (ASTNode node : this.classFieldsAndMethods.get(className)) {
                    if (node instanceof Field) {
                        Field fieldnode = (Field) node;
                        listView.getItems().add(new Text(fieldnode.getName()));
                    }
                }
                break;
            case "Method":
                for (ASTNode node : this.classFieldsAndMethods.get(className)) {
                    if (node instanceof Method) {
                        Method methodnode = (Method) node;
                        listView.getItems().add(new Text(methodnode.getName()));
                    }
                }
                break;
            default:
                break;
        }
        listView.setMaxHeight(80);

        Button findDecButton = new Button("Find Declaration");
        findDecButton.setOnAction(event -> {
            String name = listView.getSelectionModel().getSelectedItem().getText();
            findDeclaration(className, name);
            dialogPane.getScene().getWindow().hide();
        });

        vBox.getChildren().addAll(listView, findDecButton);
        vBox.setSpacing(20);
        dialogPane.setContent(vBox);
        helperDialog.setDialogPane(dialogPane);
        Window window = helperDialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        helperDialog.show();
    }

    /**
     * Highlights where the chosen field
     * or method was declared in the file
     *
     * @param className class name
     * @param name chosen field, or method
     */
    private void findDeclaration(String className, String name) {
        ASTNode node = null;
        for (ASTNode n : classFieldsAndMethods.get(className)) {
            if (n instanceof Field) {
                if (name.equals(((Field) n).getName())) {
                    node = n;
                    break;
                }
            } else {
                if (name.equals(((Method) n).getName())) {
                    node = n;
                    break;
                }
            }
        }

        int index;
        int rowNum = node.getLineNum() - 1;
        int colPos = node.getColPos();

        curCodeArea.moveTo(rowNum, colPos);
        index = curCodeArea.getCaretPosition();

        //highlights the field or method
        curCodeArea.selectRange(index, index + name.length());
        //adjusts code area to focus to right line
        curCodeArea.showParagraphAtTop(node.getLineNum() - 2);

    }

    /**
     * Highlights where the chosen class, field,
     * or method was declared in the file
     *
     * @param name chosen class, field, or method
     */
    private void findClassDeclaration(String name) {
        Class_ cNode = findClassASTNode(name);

        int index;
        int rowNum = cNode.getLineNum() - 1;
        int colPos = cNode.getColPos();

        curCodeArea.moveTo(rowNum, colPos);
        index = curCodeArea.getCaretPosition();

        //highlights the class declaration
        curCodeArea.selectRange(index, index + name.length());
        curCodeArea.showParagraphAtTop(cNode.getLineNum() - 2);

    }

    /**
     * helper method to get the class ASTNode
     *
     * @param name class name
     * @return class node
     */
    private Class_ findClassASTNode(String name){
        for(Class_ node: classes){
            if(node.getName().equals(name)){
                return node;
            }
        }
        return null;
    }

    /**
     * Finds the declaration of the Parent of the Class
     *
     * @param name
     */
    private void findParentClassDeclaration(String name) {

        Class_ node = findClassASTNode(name);
        String parentnode = node.getParent();

        if (classFieldsAndMethods.containsKey(parentnode)) {
            findClassDeclaration(parentnode);
        } else {
            javafx.scene.control.Dialog<ButtonType> noParentDialog = new Dialog<>();
            noParentDialog.setTitle("Warning");
            DialogPane noParentDialogPane = new DialogPane();
            noParentDialogPane.setContentText("Chosen Class has built-in or non-existent parent");
            noParentDialog.setDialogPane(noParentDialogPane);
            Window window = noParentDialog.getDialogPane().getScene().getWindow();
            window.setOnCloseRequest(event -> window.hide());

            noParentDialog.show();
        }
    }

//    //TODO - figure out if this works
//    private void findOverridenMethodDeclaration(String methodName) {
//        Method node = (Method) map.get(methodName);
//        MethodClassFinderVisitor visitor = new MethodClassFinderVisitor();
//        String className = visitor.getMethodClassName(this.ast, methodName);
//        this.semanticAnalyzer.getOverridenMethod(className, methodName);
//    }
}
