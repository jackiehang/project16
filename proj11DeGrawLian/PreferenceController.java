/*
 * File: PreferenceController.css
 * Names: Michael Li, Iris Lian, Zeb Keith-Hardy, Kevin Zhou
 * Class: CS 361
 * Project 6/7/9
 * Date: October 26, 2018/ November 3, 2018/ November 20, 2018
 */

package proj10LiLianKeithHardyZhou;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * This is the controller class for the preference menu
 * At this point it only has one choice - night mode
 * @author Michael Li, Iris Lian, Zeb Keith-Hardy, Kevin Zhou
 */
public class PreferenceController {
    private VBox vBox;
    private Console console;
    private TabPane structureTabPane;
    private CheckMenuItem fileStructureItem;
    private CheckMenuItem directoryTreeItem;
    private TreeView fileStructureTree;
    private TreeView directoryTree;
    private String curKeywordColor;
    private String curIntColor;
    private String curStrColor;
    private String curParenColor;

    /**
     * This is the constructor of PreferenceController
     *
     * @param vBox    the container
     * @param console the console
     */
    public PreferenceController(VBox vBox, Console console, TabPane structureTabPane,
                                CheckMenuItem fileStructureItem, CheckMenuItem directoryTreeItem,
                                TreeView fileStructureTree, TreeView directoryTree) {
        this.console = console;
        this.vBox = vBox;
        setCurColorToDefault();
        this.fileStructureItem = fileStructureItem;
        this.directoryTreeItem = directoryTreeItem;
        this.structureTabPane = structureTabPane;
        this.fileStructureTree = fileStructureTree;
        this.directoryTree = directoryTree;
    }

    /**
     * When night mode is checked or unchecked, switches to another style sheet
     */
    public void handleNightMode() {
        String nightMode = getClass().getResource("resources/NightMode.css").toExternalForm();
        if (vBox.getStylesheets().contains(nightMode)) {
            vBox.getStylesheets().remove(nightMode);
        } else {
            this.console.setStyleClass(this.console.getText().length(), this.console.getText().length(),
                    "default");
            this.vBox.getStylesheets().add( nightMode);
        }
        setCurColorToDefault();
    }

    /**
     * Handle change color action for the given type
     * Pops up a dialog window displaying the color selections for the given type
     * Change the color of the given type base on selection in the choiceBox
     * @param type "Int", "Str", "Keyword", "Paren". Type to change color.
     */
    public void handleColorAction(String type) {
        Stage keywordColorWin = new Stage();
        keywordColorWin.setTitle(type + " Color");
        VBox parentVBox = this.vBox;
        VBox colorRoot = new VBox();
        colorRoot.setAlignment(Pos.CENTER);
        colorRoot.setSpacing(10);

        final Rectangle rect = new Rectangle(75, 75);

        ChoiceBox colorCheckBox = new ChoiceBox(FXCollections.observableArrayList(
                                "White","Purple", "Blue", "Teal", "Pink", "Orange",
                                        "Red","LightGreen","Salmon","Firebrick","Black"));

        Text message = new Text(type + " Color");
        message.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        ObservableList<String> currentStyleSheets = parentVBox.getStylesheets();

        //Check the type to find the corresponding current color
        String selectionValue;
        switch (type) {
            case "Str":
                selectionValue = this.curStrColor;
                break;
            case "Int":
                selectionValue = this.curIntColor;
                break;
            case "Keyword":
                selectionValue = this.curKeywordColor;
                break;
            default:
                selectionValue = this.curParenColor;
                break;
        }

        colorCheckBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                String color = (String)colorCheckBox.getValue();
                updateColor(type, color);
                //sets the color of the rectangle
                switch (color){
                    case("White"):
                        rect.setFill(Color.WHITE);
                        message.setFill(Color.WHITE);
                        break;
                    case("Salmon"):
                        rect.setFill(Color.SALMON);
                        message.setFill(Color.SALMON);
                        break;
                    case("LightGreen"):
                        rect.setFill(Color.LIGHTGREEN);
                        message.setFill(Color.LIGHTGREEN);
                        break;
                    case("Black"):
                        rect.setFill(Color.BLACK);
                        message.setFill(Color.BLACK);
                        break;
                    case("Blue"):
                        rect.setFill(Color.BLUE);
                        message.setFill(Color.BLUE);
                        break;
                    case("Red"):
                        rect.setFill(Color.RED);
                        message.setFill(Color.RED);
                        break;
                    case("Purple"):
                        rect.setFill(Color.PURPLE);
                        message.setFill(Color.PURPLE);
                        break;
                    case("Firebrick"):
                        rect.setFill(Color.FIREBRICK);
                        message.setFill(Color.FIREBRICK);
                        break;
                    case("Orange"):
                        rect.setFill(Color.ORANGE);
                        message.setFill(Color.ORANGE);
                        break;
                    case("Pink"):
                        rect.setFill(Color.PINK);
                        message.setFill(Color.PINK);
                        break;
                    default: // Teal
                        rect.setFill(Color.TEAL);
                        message.setFill(Color.TEAL);
                        break;
                }
            }
        });
        colorCheckBox.setValue(selectionValue);
        colorRoot.getChildren().addAll(message, rect, colorCheckBox);
        Scene keywordColorScene = new Scene(colorRoot, 200, 200);
        keywordColorWin.setScene(keywordColorScene);
        keywordColorWin.show();

    }

    /**
     * Set all the current color fields to default of normal or night mode
     */
    private void setCurColorToDefault(){
        String nightMode = getClass().getResource("resources/NightMode.css").toExternalForm();
        if(!this.vBox.getStylesheets().contains(nightMode)) {
            this.curKeywordColor = "Purple";
            this.curStrColor = "Blue";
            this.curIntColor = "Firebrick";
            this.curParenColor = "Teal";
        }else{
            this.curKeywordColor = "Orange";
            this.curStrColor = "LightGreen";
            this.curIntColor = "Blue";
            this.curParenColor = "Salmon";
        }
        this.updateColor("all","current");
    }

    /**
     * Update the corresponding current color field of a type to the given color string
     * @param type the type of current color field to update
     * @param color the new value for the type color field
     */
    private void updateColor(String type, String color){
        String strColorStyle = "-Str-color: " + this.curStrColor + ";";
        String intColorStyle = "-Int-color: " + this.curIntColor + ";";
        String keywordColorStyle = "-Keyword-color: " + this.curKeywordColor + ";";
        String parenColorStyle = "-Paren-color: " + this.curParenColor + ";";

        //Construct the color styles for each type
        switch (type){
            case("Int"):
                intColorStyle = "-Int-color: " + color+ ";";
                this.curIntColor = color;
                break;
            case("Str"):
                strColorStyle = "-Str-color: " + color+ ";";
                this.curStrColor = color;
                break;
            case("Keyword"):
                keywordColorStyle = "-Keyword-color: " + color+ ";";
                this.curKeywordColor = color;
                break;
            case("Paren"):
                parenColorStyle = "-Paren-Color:" + color+ ";";
                this.curParenColor = color;
                break;
        }
        //Update the color style
        this.vBox.setStyle(keywordColorStyle + strColorStyle + intColorStyle + parenColorStyle);
    }

    /**
     * Handles check menu item for opening and closing file structure tree tab
     */
    public void handleFileStructureTab(){
        if(!this.fileStructureItem.isSelected()){
            this.structureTabPane.getSelectionModel().selectLast();
            Tab fileStructureTab = this.structureTabPane.getSelectionModel().getSelectedItem();
            this.structureTabPane.getTabs().remove(fileStructureTab);
        } else{
            //add new fileStructure Tab
            Tab fileStructureTab = new Tab();
            fileStructureTab.setId("fileStructureTab");
            fileStructureTab.setText("File Structure");
            fileStructureTab.setContent(this.fileStructureTree);
            if(this.structureTabPane.getTabs().isEmpty()){
                this.structureTabPane.getTabs().add(0,fileStructureTab);
            }else {
                this.structureTabPane.getTabs().add(1, fileStructureTab);
            }
        }
    }

    /**
     * Handles check menu item for opening and closing directory tree tab
     */
    public void handleDirectoryTreeTab(){
        if(!this.directoryTreeItem.isSelected()){
            this.structureTabPane.getSelectionModel().selectFirst();
            Tab directoryTreeTab = this.structureTabPane.getSelectionModel().getSelectedItem();
            this.structureTabPane.getTabs().remove(directoryTreeTab);
        } else{
            //add new directoryTreeTab
            Tab directoryTreeTab = new Tab();
            directoryTreeTab.setId("directoryTreeTab");
            directoryTreeTab.setText("Directory");
            directoryTreeTab.setContent(this.directoryTree);
            this.structureTabPane.getTabs().add(0,directoryTreeTab);
        }
    }
}