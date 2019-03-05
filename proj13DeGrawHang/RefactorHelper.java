package proj13DeGrawHang;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.HashMap;

public class RefactorHelper {
    private ArrayList<String> classNames;
    private ArrayList<String> fieldNames;
    private ArrayList<String> methodNames;
    private javafx.scene.control.Dialog<ButtonType> refactorDialog;

    public RefactorHelper(HashMap<String, ArrayList<String>> names){
        classNames = names.get("Class");
        fieldNames = names.get("Field");
        methodNames = names.get("Method");
        createRefactorDialog();
    }

    private void createRefactorDialog(){
        this.refactorDialog = new Dialog<>();
        this.refactorDialog.setTitle("Refactor");

        DialogPane dialogPane = new DialogPane();
        VBox outer = new VBox();

        Button classes= new Button("Class");
        Button fields = new Button("Field");
        Button methods = new Button("Method");


        classes.setOnAction(event -> {
            this.createHelperDialog(classes.getText());
            event.consume();
            //TODO: should the dialog close?
            dialogPane.getScene().getWindow().hide();
        });
        fields.setOnAction(event -> {
            this.createHelperDialog(fields.getText());
            event.consume();
            this.refactorDialog.close();
        });
        methods.setOnAction( event->{this.createHelperDialog(methods.getText());
        event.consume();
        this.refactorDialog.close();
        });

        outer.getChildren().addAll(classes, fields, methods);
        dialogPane.setContent(outer);
        refactorDialog.setDialogPane(dialogPane);
        Window window = refactorDialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        this.refactorDialog.showAndWait();
    }

    private void createHelperDialog(String type){
        javafx.scene.control.Dialog<ButtonType> helperDialog = new Dialog<>();
        helperDialog.setTitle(type);

        DialogPane dialogPane = new DialogPane();
        VBox outer = new VBox();

        ListView<Text> inner = new ListView<>();
        switch (type){
            case "Class":

                for(String s: classNames){
                    inner.getItems().add(new Text(s));
                }
                break;
            case "Field":
                for(String s: fieldNames){
                    inner.getItems().add(new Text(s));
                }
                break;
            case "Method":
                for(String s: methodNames){
                    inner.getItems().add(new Text(s));
                }
                break;
            default:
                break;
        }
        inner.setMaxHeight(80);

        Text replace = new Text("Replacement Name: ");
        TextField newName = new TextField();
        Button refactorButton = new Button("Refactor");

        outer.getChildren().addAll(inner,replace, newName,refactorButton);
        outer.setSpacing(20);
        dialogPane.setContent(outer);
        helperDialog.setDialogPane(dialogPane);
        Window window = helperDialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(event -> window.hide());

        helperDialog.show();

    }


}
