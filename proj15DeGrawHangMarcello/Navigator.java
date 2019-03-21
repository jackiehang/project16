/*
 * File: Navigator.java
 * Names: Lucas DeGraw, Jackie Hang, ChrisMarcello
 * Class: CS 361
 * Project 13
 * Date: March 5, 2018
 */

package proj15DeGrawHangMarcello;


import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Window;
import org.fxmisc.richtext.CodeArea;
import proj15DeGrawHangMarcello.bantam.ast.ASTNode;
import proj15DeGrawHangMarcello.bantam.ast.Class_;
import proj15DeGrawHangMarcello.bantam.ast.Field;
import proj15DeGrawHangMarcello.bantam.ast.Method;
import proj15DeGrawHangMarcello.bantam.semant.SemanticAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class allows the user to navigate to where fields,
 * classes, and methods are declared in the file.
 * Handles all actions contained within the "Navigate" button.s
 *
 * @author  Lucas DeGraw, Jackie Hang, ChrisMarcello
 * @since   3-5-2019
 */

public class Navigator {
    // all the Class nodes of the Program
    private ArrayList<Class_> classes;
    // keys are the name of the class and values are an arrayList
    // class' fields and methods
    private HashMap<String, ArrayList<ASTNode>> classFieldsAndMethods;
    private CodeArea curCodeArea;

    /**
     * Constructor of the Navigator
     *
     * @param codeArea current CodeArea
     * @param checker SemanticAnalyzer
     */
    public Navigator(CodeArea codeArea, SemanticAnalyzer checker) {
        this.classes = checker.getClasses();
        this.curCodeArea = codeArea;
        classFieldsAndMethods = checker.getClassFieldsAndMethods();
        createNavigatorDialog();
    }

    /**
     * Creates the main Navigator dialog
     * User can choose to navigate to
     * a Class and find its declaration, its parent
     * declaration,or its fields and methods
     */
    private void createNavigatorDialog() {
        javafx.scene.control.Dialog<ButtonType> helperDialog = new Dialog<>();
        helperDialog.setTitle("Navigate");

        DialogPane dialogPane = new DialogPane();
        VBox vBox = new VBox();
        ListView<Text> inner = new ListView<>();

        // creates a list view with all available class names
        for (String s : this.classFieldsAndMethods.keySet()) {
            inner.getItems().add(new Text(s));
        }

        inner.setMaxHeight(80);

        Button findDecButton = new Button("Find Declaration");
        findDecButton.setMinWidth(200);
        findDecButton.setOnAction(event -> {
            if(inner.getSelectionModel().getSelectedItem()!=null){
                String name = inner.getSelectionModel().getSelectedItem().getText();
                findClassDeclaration(name);
                dialogPane.getScene().getWindow().hide();
            }
        });


        Button findParentButton = new Button("Find Parent Declaration");
        findParentButton.setMinWidth(200);
        findParentButton.setOnAction(event -> {
            if(inner.getSelectionModel().getSelectedItem()!=null){
                String name = inner.getSelectionModel().getSelectedItem().getText();
                findParentClassDeclaration(name);
                dialogPane.getScene().getWindow().hide();
            }
        });


        Button fieldsButton = new Button("Get Fields");
        fieldsButton.setMinWidth(200);
        fieldsButton.setOnAction(event -> {
            if(inner.getSelectionModel().getSelectedItem()!=null){
                String name = inner.getSelectionModel().getSelectedItem().getText();
                createHelperDialog(name, "Field");
                dialogPane.getScene().getWindow().hide();
            }
        });

        Button methodsButton = new Button("Get Methods");
        methodsButton.setMinWidth(200);
        methodsButton.setOnAction(event -> {
            if(inner.getSelectionModel().getSelectedItem()!=null){
                String name = inner.getSelectionModel().getSelectedItem().getText();
                createHelperDialog(name, "Method");
                dialogPane.getScene().getWindow().hide();
            }
        });

        vBox.getChildren().addAll(inner, findDecButton, findParentButton, fieldsButton, methodsButton);
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
        ListView<Text> inner = new ListView<>();

        //putting the proper node names in the listview
        switch (type) {
            case "Field":
                for (ASTNode node : this.classFieldsAndMethods.get(className)) {
                    if (node instanceof Field) {
                        Field fieldnode = (Field) node;
                        inner.getItems().add(new Text(fieldnode.getName()));
                    }
                }
                break;
            case "Method":
                for (ASTNode node : this.classFieldsAndMethods.get(className)) {
                    if (node instanceof Method) {
                        Method methodnode = (Method) node;
                        inner.getItems().add(new Text(methodnode.getName()));
                    }
                }
                break;
            default:
                break;
        }
        inner.setMaxHeight(80);

        Button findDecButton = new Button("Find Declaration");
        findDecButton.setOnAction(event -> {
            if(inner.getSelectionModel().getSelectedItem()!=null){
                String name = inner.getSelectionModel().getSelectedItem().getText();
                findFieldOrMethodDeclaration(className, name);
                dialogPane.getScene().getWindow().hide();
            }
        });
        vBox.getChildren().addAll(inner, findDecButton);

        if(type.equals("Method")) {
            Class_ node = findClassASTNode(className);
            String parentNode = node.getParent();

            if (classFieldsAndMethods.containsKey(parentNode)) {
                Button findParentButton = new Button("Find Overridden Parent Method");
                findParentButton.setOnAction(event -> {
                    if(inner.getSelectionModel().getSelectedItem()!=null){
                        String name = inner.getSelectionModel().getSelectedItem().getText();
                        findOverriddenMethodDeclaration(parentNode, name);
                        dialogPane.getScene().getWindow().hide();
                    }
                });
                vBox.getChildren().addAll(findParentButton);
            }
        }

        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(20);
        dialogPane.setContent(vBox);
        helperDialog.setDialogPane(dialogPane);
        Window window = helperDialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        helperDialog.show();
    }

    /**
     * Highlights where the chosen class, field,
     * or method was declared in the file
     *
     * @param className chosen class, field, or method
     */
    private void findFieldOrMethodDeclaration(String className, String name) {
        ASTNode node = null;

        //run through the fields and methods, find the matching name in the Class
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
        if(node == null){
            displayWarningDialog("Field or Class Cannot Be Found");
            return;
        }
        highlightText(node,name);
    }

    /**
     * Helper method to highlight the text
     *
     * @param node the ASTNode with row, col info
     * @param name text
     */
    private void highlightText(ASTNode node, String name){
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
        if(cNode != null) {
            highlightText(cNode,name);
        }

    }

    /**
     * Returns the class node
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
     * @param name class name
     */
    private void findParentClassDeclaration(String name) {

        Class_ node = findClassASTNode(name);
        String parentnode = node.getParent(); //only null if called on Object, which you can't

        //checks if the parent is not a built-in method/parent exists.
        if(parentnode.equals("Object")) {
            displayWarningDialog("Parent Class 'Object' is built-in and cannot be navigated to.");
        }
        else if (classFieldsAndMethods.containsKey(parentnode)) {
            findClassDeclaration(parentnode);
        } else {
            //should never reach this if semantic analysis is done correctly.
            displayWarningDialog("Chosen class '" + name + "' has declared parent '"
                    + parentnode + "', which is non-existent.");
        }
    }

    /**
     * Tries to find the Overridden method in the parent class
     * @param parentClassName class name
     * @param methodName method name
     */
    private void findOverriddenMethodDeclaration(String parentClassName, String methodName) {
        ASTNode node = null;
        for(ASTNode n : classFieldsAndMethods.get(parentClassName)){
            if(((Method) n).getName().equals(methodName)){
                highlightText(n,methodName);
                node = n;
                break;
            }
        }

        //if the method does not exist in the parent class
        if(node == null){
            displayWarningDialog("Parent class '" + parentClassName + "' does not contain method of name '" + methodName + "'");
        }
    }

    /**
     * Displays a warning dialog
     *
     * @param message warning message
     */
    private void displayWarningDialog(String message){
        javafx.scene.control.Dialog<ButtonType> warningDialog = new Dialog<>();
        warningDialog.setTitle("Warning");
        DialogPane noParentDialogPane = new DialogPane();
        noParentDialogPane.setContentText(message);
        warningDialog.setDialogPane(noParentDialogPane);
        Window window = warningDialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());
        warningDialog.show();

    }

}
