/*
 * File: Navigator.java
 * Names: Lucas DeGraw, Jackie Hang, ChrisMarcello
 * Class: CS 361
 * Project 13
 * Date: March 5, 2018
 */

package proj13DeGrawHang;


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

    private HashMap<String, ArrayList<ASTNode>> names;
    private HashMap<String, ASTNode> map;
    private CodeArea curCodeArea;

    /**
     * Constructor of the navigator
     *
     * @param names a hashmap with "Class", "Field", "Method as keys and the
     *              corresponding nodes in an arraylist as values
     * @param codeArea current codearea
     */
    public Navigator(HashMap<String, ArrayList<ASTNode>> names, CodeArea codeArea){
        this.names = names;
        curCodeArea = codeArea;
        map = new HashMap<>();
        createNavigatorDialog();
    }

    /**
     * Creates the main Navigator dialog
     * User can choose to navigate to
     * a "Class", "Field", or "Method
     */
    private void createNavigatorDialog(){
        javafx.scene.control.Dialog<ButtonType> navigatorDialog = new Dialog<>();

        DialogPane dialogPane = new DialogPane();
        VBox vBox = new VBox();

        Button classes= new Button("Class");
        Button fields = new Button("Field");
        Button methods = new Button("Method");
        methods.setMinWidth(100);
        fields.setMinWidth(100);
        classes.setMinWidth(100);

        //setting on click actions
        classes.setOnAction(event -> {
            this.createHelperDialog(classes.getText());
            event.consume();
            dialogPane.getScene().getWindow().hide();
        });
        fields.setOnAction(event -> {
            this.createHelperDialog(fields.getText());
            event.consume();
            dialogPane.getScene().getWindow().hide();
        });
        methods.setOnAction( event->{
            this.createHelperDialog(methods.getText());
            event.consume();
            dialogPane.getScene().getWindow().hide();
        });

        vBox.getChildren().addAll(classes, fields, methods);
        dialogPane.setContent(vBox);
        navigatorDialog.setDialogPane(dialogPane);
        Window window = navigatorDialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        navigatorDialog.show();
    }

    /**
     * Creates a dialog with all of the
     * given type. Allows user to select one and
     * find where it was declared
     *
     * @param type "Class", "Field", or "Method
     */
    private void createHelperDialog(String type){
        javafx.scene.control.Dialog<ButtonType> helperDialog = new Dialog<>();
        helperDialog.setTitle(type);

        DialogPane dialogPane = new DialogPane();
        VBox outer = new VBox();
        ListView<Text> inner = new ListView<>();

        //putting the proper node names in the listview
        switch (type){
            case "Class":
                for(ASTNode node: names.get("Class")){
                    Class_ classnode = (Class_)node;
                    inner.getItems().add(new Text(classnode.getName()));
                    map.put(classnode.getName(),classnode);
                }
                break;
            case "Field":
                for(ASTNode node: names.get("Field")){
                    Field fieldnode = (Field)node;
                    inner.getItems().add(new Text(fieldnode.getName()));
                    map.put(fieldnode.getName(),fieldnode);
                }
                break;
            case "Method":
                for(ASTNode node: names.get("Method")){
                    Method methodnode = (Method)node;
                    inner.getItems().add(new Text(methodnode.getName()));
                    map.put(methodnode.getName(),methodnode);
                }
                break;
            default:
                break;
        }
        inner.setMaxHeight(80);

        Button findDecButton = new Button("Find Declaration");
        findDecButton.setOnAction(event -> {
            String name = inner.getSelectionModel().getSelectedItem().getText();
            findDeclaration(name);
            dialogPane.getScene().getWindow().hide();
        });

        outer.getChildren().addAll(inner,findDecButton);
        outer.setSpacing(20);
        dialogPane.setContent(outer);
        helperDialog.setDialogPane(dialogPane);
        Window window = helperDialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        helperDialog.show();

    }

    /**
     * Highlights where the chosen class, field,
     * or method was declared in the file
     *
     * @param name chosen class, field, or method
     */
    private void findDeclaration(String name){
        ASTNode node = map.get(name);

        int index=0;
        int rowNum = node.getLineNum()-1;
        int colPos = node.getColPos();

        curCodeArea.moveTo(rowNum, colPos);
        index = curCodeArea.getCaretPosition();

        curCodeArea.selectRange(index, index+name.length());
        curCodeArea.showParagraphAtTop(node.getLineNum()-2);
    }

}
