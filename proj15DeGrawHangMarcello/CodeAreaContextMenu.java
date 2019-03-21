/*
 * File: CodeAreaContextMenu.java
 * Names: Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 * Class: CS 361
 * Project 6/7/9
 * Date: October 26, 2018/ November 3, 2018/ November 20, 2018
 */

package proj15DeGrawHangMarcello;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.binding.Bindings;

/**
 * This class contains the setup for the context menu in the code area in the IDE.
 *
 * The CodeAreaContextMenu has nine menu items - Undo, Redo, Cut, Copy, Paste,
 * SelectAll, Find & Replace, Compile, Compile & Run. Cut and Copy are disabled
 * when no text is selected.
 *
 * @author  Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 */
public class CodeAreaContextMenu extends ContextMenu {
    /**
     * Constructor for the class. Initializes the menu.
     */
    public CodeAreaContextMenu(MasterController controller){
        super();
        // initialize menu items
        MenuItem UndoMenuItem=new MenuItem();
        MenuItem RedoMenuItem=new MenuItem();
        MenuItem CutMenuItem=new MenuItem();
        MenuItem CopyMenuItem=new MenuItem();
        MenuItem PasteMenuItem=new MenuItem();
        MenuItem SelectAllMenuItem=new MenuItem();
        MenuItem FindAndReplaceMenuItem=new MenuItem();
        MenuItem ScanMenuItem = new MenuItem();
        MenuItem EnTabMenuItem=new MenuItem();
        MenuItem DeTabMenuItem=new MenuItem();

        // set handlers
        UndoMenuItem.setOnAction(event->controller.handleUndo());
        RedoMenuItem.setOnAction(event -> controller.handleRedo());
        CutMenuItem.setOnAction(event->controller.handleCut());
        CopyMenuItem.setOnAction(event->controller.handleCopy());
        PasteMenuItem.setOnAction(event->controller.handlePaste());
        SelectAllMenuItem.setOnAction(event->controller.handleSelectAll());
        FindAndReplaceMenuItem.setOnAction(event->controller.handleFindReplace());
        ScanMenuItem.setOnAction(event->controller.handleScan());
        EnTabMenuItem.setOnAction(event -> controller.handleEntab());
        DeTabMenuItem.setOnAction(event -> controller.handleDetab());
        // set texts
        UndoMenuItem.setText("Undo");
        RedoMenuItem.setText("Redo");
        CutMenuItem.setText("Cut");
        CopyMenuItem.setText("Copy");
        PasteMenuItem.setText("Paste");
        SelectAllMenuItem.setText("Select All");
        FindAndReplaceMenuItem.setText("Find & Replace");
        ScanMenuItem.setText("Scan");
        EnTabMenuItem.setText("Entab");
        DeTabMenuItem.setText("Detab");
        // add to menu
        this.getItems().addAll(UndoMenuItem,RedoMenuItem,CutMenuItem,CopyMenuItem,PasteMenuItem,SelectAllMenuItem,
                FindAndReplaceMenuItem,EnTabMenuItem,DeTabMenuItem,ScanMenuItem);

    }

    /**
     * Binder that binds the disableProperty of the cut and copy menu items
     * with whether some text is selected.
     * @param codeArea the corresponding JavaCodeArea
     */
    public void bindCutCopyMenuItems(JavaCodeArea codeArea){
        //Boolean Binding that reflects the selected text is null or not
        BooleanBinding selectIsNull=Bindings.equal( 0,
                new SimpleIntegerProperty(codeArea.getSelectedText().length()).asObject());

        //disable cut , copy , entab , detab when no text is selected
        this.getItems().get(2).disableProperty().bind(selectIsNull);
        this.getItems().get(3).disableProperty().bind(selectIsNull);
        this.getItems().get(7).disableProperty().bind(selectIsNull);
        this.getItems().get(8).disableProperty().bind(selectIsNull);

    }
}
