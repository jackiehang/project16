/*
 * File: FileController.java
 * Names: Kevin Ahn, Matt Jones, Jackie Hang, Kevin Zhou
 * Class: CS 361
 * Project 4
 * Date: October 2, 2018
 * ---------------------------
 * Edited By: Zena Abulhab, Paige Hanssen, Kyle Slager, Kevin Zhou
 * Project 5
 * Date: October 12, 2018
 * ---------------------------
 * Edited By: Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 * Project 6/7/9
 * Date: October 26, 2018/ November 3, 2018/ November 20, 2018
 */

package proj10LiLianKeithHardyZhou;

import javafx.event.Event;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

/**
 * This class contains the handlers for each of the menu options in the IDE.
 *
 * Keeps track of the tab pane, the current tab, the index of the current tab
 * within the pane, and the File objects of the current tabs.
 *
 * @author  Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 * @author  Kevin Ahn, Jackie Hang, Matt Jones, Kevin Zhou
 * @author  Zena Abulhab, Paige Hanssen, Kyle Slager, Kevin Zhou
 * @version 3.0
 * @since   10-3-2018
 */
public class FileController {

    private CodeTabPane codeTabPane;
    private VBox vBox;
    private MasterController masterController;

    /**
     * Constructor for the class. Initializes the save status
     * and the filenames in a HashMap
     */
    public FileController(VBox vBox, CodeTabPane codeTabPane,MasterController masterController) {
        this.vBox = vBox;
        this.codeTabPane = codeTabPane;
        this.masterController = masterController;

    }

    /**
     * Handler for the "New" menu item in the "File" menu.
     * Adds a new Tab to the TabPane, adds null to the filenames HashMap,
     * and false to the saveStatus HashMap
     */
    public void handleNew() {
        this.codeTabPane.makeTabFromFile(null,false);
    }

    /**
     * Handler for the "Open" menu item in the "File" menu.
     * Writes the requested file to a new Tab.
     * @param file - file to open into a new Tab
     */
    public void handleOpen(File file) {
        if (file == null){
            return;
        }
        this.codeTabPane.makeTabFromFile(file,true);
    }


    /**
     * Creates a FileChooser to select a file
     * Use scanner to read the file.
     * @return File that needs to be opened by handleOpen
     */
    public File handleOpenDialog(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open");
        Window stage = this.vBox.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        return file;
    }

    /**
     * Handler for the "Close" menu item in the "File" menu.
     * Checks to see if the file has been changed since the last save.
     * If changes have been made, redirect to askSaveAndClose and then close the tab.
     * Otherwise, just close the tab.
     */
    public void handleClose(Event event) {
        Tab curTab = this.codeTabPane.getSelectionModel().getSelectedItem();
        if (this.codeTabPane.getFile(curTab) != null) {
            // check if any changes were made
            if (this.codeTabPane.getSaveStatus(curTab))
                this.closeTab();
            else
                this.askSaveAndClose(curTab.getText(),event);
        } else {
            if(this.codeTabPane.getFileMapSize() != 0) {
                this.askSaveAndClose(curTab.getText(),event);
            }
        }
    }

    /**
     * Handler for the "Save" menu item in the "File" menu.
     * If the current tab has been saved before, writes out the content to its corresponding
     * file in storage.
     * Else if the file has never been saved, opens a pop-up window that allows the user to
     * choose a filename and directory and then store the content of the tab to storage.
     */
    public boolean handleSave() {
        Tab curTab = this.codeTabPane.getSelectionModel().getSelectedItem();
        if (this.codeTabPane.curTabInMap()){

            writeFile(this.codeTabPane.getFile(curTab));
            this.codeTabPane.updateSaveStatus(curTab, true);
            return true;
        }
        else
            return this.handleSaveAs();
    }

    /**
     * Handler for the "Save as..." menu item in the "File" menu.
     * Opens a pop-up window that allows the user to choose a filename and directory.
     * Calls writeFile to save the file to memory.
     * Changes the name of the current tab to match the newly saved file's name.
     */
    public boolean handleSaveAs() {
        Tab curTab = this.codeTabPane.getSelectionModel().getSelectedItem();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save as...");
        Window stage = this.vBox.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file == null){
            return false;
        }
        else{
            writeFile(file);
            this.codeTabPane.updateTabFileMap(curTab, file);
            this.codeTabPane.updateSaveStatus(curTab,true);
        }
        curTab.setText(file.getName());
        // open file directory
        this.masterController.createDirectoryTree();
        // update structure view
        this.masterController.updateStructureView();
        return true;
    }

    /**
     * handle close all function. The function calls handle close on each opened tab
     * @return true if all tabs are successfully closed, false if user choose to cancel one of them
     */
    public boolean handleCloseAll(Event event){
        int numTabs = this.codeTabPane.getFileMapSize();

        // Close each tab using handleClose()
        // Check if current number of tabs decreased by one to know if the user cancelled.
        for (int i = 0; i < numTabs; i++ ) {
            this.handleClose(event);
            if (this.codeTabPane.getFileMapSize() == (numTabs - i))
                return true;
        }
        return false;
    }

    /**
     * Handler for the "Exit" menu item in the "File" menu.
     * Closes all the tabs using handleClose()
     * Returns when the user cancels exiting any tab.
     */
    public void handleExit(Event event) {
        boolean cancelled = this.handleCloseAll(event);
        if(!cancelled) {
            Platform.exit();
        }
    }

    /**
     * Creates a pop-up window which allows the user to select whether they wish to save
     * the current file or not.
     * Used by handleClose.
     *
     * @param filename The filename of the file to be saved (or not) at the user's discretion
     */
    private void askSaveAndClose(String filename,Event event) {
        String result = masterController.askSaveDialog("Save Changes?",
                "Do you want to save the changes you made to " + filename + "?",
                "Your changes will be lost if you don't save them.");

        if (result.equals("cancelButton")) {
            event.consume();
            return;
        } else if (result.equals("yesButton")) {
            boolean isNotCancelled = this.handleSave();
            if(isNotCancelled) {
                this.closeTab();
            } else {
                event.consume();
            }
            return;
        }
        this.closeTab();

    }


    /**
     * Saves the text present in the current tab to a given filename.
     * Used by handleSave, handleSaveAs.
     *
     * @param file The file object to which the text is written to.
     */
    private void writeFile(File file) {
        Tab curTab = this.codeTabPane.getSelectionModel().getSelectedItem();
        VirtualizedScrollPane<CodeArea> scrollPane = (VirtualizedScrollPane<CodeArea>) curTab.getContent();
        CodeArea codeArea = scrollPane.getContent();
        String text = codeArea.getText();

        // use a BufferedWriter object to write out the string to a file
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            writer.write(text);
            writer.close();
        }
        catch (IOException e) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setHeaderText("File Not Found Or Read-Only File");
            alert.setContentText("Cannot find file or the file is read-only. " +
                    "Please select a new file or try again.");
            alert.showAndWait();
            return;
        }

        // update File array
        this.codeTabPane.updateTabFileMap(curTab,file);
        this.codeTabPane.updateSaveStatus(curTab, true);
    }

    /**
     * Executes process for when a tab is closed, which is to remove the filename and saveStatus at
     * the corresponding HashMaps, and then remove the Tab object from TabPane
     */
    private void closeTab() {
        // NOTE: the following three lines has to be in this order removing the tab first would
        // result in calling handleUpdateCurrentTab() because the currently selected tab will
        // change, and thus the wrong File will be removed from the HashMaps
        Tab curTab = this.codeTabPane.getSelectionModel().getSelectedItem();
        this.codeTabPane.removeFromFileMap(curTab);
        this.codeTabPane.removeFromSaveMap(curTab);
        this.codeTabPane.getTabs().remove(curTab);
    }
}
