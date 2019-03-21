/*
 * File: CodeTabPane.java
 * Edited By: Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 * Project 9
 * Date: November 20, 2018
 */

package proj15DeGrawHangMarcello;

import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;


/**
 * This class subclasses TabPane to keep track of the file map and save map for the CodeTabs
 *
 * Handles creation of new tabs and updating of the Two Hash maps
 *
 * @author  Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 */
public class CodeTabPane extends TabPane {
    private HashMap<Tab, Boolean> saveStatus;
    private HashMap<Tab, File> tabFileMap;
    private MasterController masterController;
    private CodeAreaContextMenu codeAreaContextMenu;
    private TabContextMenu tabContextMenu;

    /**
     * Constructor of CodeTabPane
     */
    public CodeTabPane(){
        super();
        this.saveStatus = new HashMap<>();
        this.tabFileMap = new HashMap<>();
    }

    /**
     * Pulls the reference to the Master Controller from the Master Controller so that it can
     * be passed into the CodeTabs.
     * Not in initialization of CodeTabPane because CodeTabPane is created in the FXML file
     * and the Master Controller is not initialized at that point.
     * @param masterController the master controller
     * @param codeAreaContextMenu the code area context menu
     * @param tabContextMenu the tab context menu
     */
    public void passControllerContextMenus(MasterController masterController,
                                           CodeAreaContextMenu codeAreaContextMenu, TabContextMenu tabContextMenu){
        this.masterController = masterController;
        this.codeAreaContextMenu = codeAreaContextMenu;
        this.tabContextMenu = tabContextMenu;
    }

    /**
     * A method to create a new tab with a codeArea inside of it and add it to the tabPane
     * if there exists a file, a scanner will read in the content
     * and write it to the codeArea.
     *
     * @param file the file opened into the new tab
     */
    public void makeTabFromFile(File file, boolean preSaved){
        // Make a new tab with a TextArea containing the content String,
        // Make it the first tab and select it
        String filename = "Untitled-" + Integer.toString(this.getFileMapSize()+1);
        String content = "";
        if (file != null){
            filename = file.getName();
            try {
                Scanner scanner = new Scanner(file).useDelimiter("\\Z");
                if (scanner.hasNext())
                    content = scanner.next();
            }
            catch (FileNotFoundException | NullPointerException e) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("File Not Found");
                alert.setContentText("Please select a new file.");
                alert.showAndWait();
            }
        }
        this.createNewTab(filename,content,preSaved, file);

    }

    /**
     * Creates an untitled tab with content
     * @param content String to be placed in code area
     */
    public void createTabWithContent(String content){
        String filename = "Untitled-" + Integer.toString(this.getFileMapSize()+1);
        this.createNewTab(filename,content,false,null);

    }

    /**
     * Creates a tab with the given specifications
     * @param filename Filename to be displayed
     * @param content String to be placed in code area
     * @param preSaved Whether or not the file has been save
     * @param file File to be placed in tab if opening a file
     */
    public void createNewTab(String filename, String content, boolean preSaved, File file){
        CodeTab newTab = new CodeTab(this.masterController,this.codeAreaContextMenu,
                this.tabContextMenu, this, filename, content);
        this.getTabs().add(0,newTab);
        this.getSelectionModel().select(newTab);

        this.addToFileMap(newTab, file);
        this.addToSaveMap(newTab, preSaved);
    }

    /**
     * Returns the name of the file open in the current tab.
     * @return The name of the currently open file
     */
    public String getFileName(){
        Tab curTab = this.getSelectionModel().getSelectedItem();
        if(this.tabFileMap.get(curTab) == null){
            return null;
        }
        return this.tabFileMap.get(curTab).getPath();
    }

    /**
     * Finds if the currently open tab is in the file map
     * @return true if tab is in file map else return false
     */
    public Boolean curTabInMap(){
        Tab curTab = this.getSelectionModel().getSelectedItem();
        return tabFileMap.get(curTab) != null;
    }

    /**
     * @return the tabFileMap
     */
    public HashMap<Tab, File> getTabFileMap() {
        return this.tabFileMap;
    }

    /**
     * Returns the corresponding file of the tab
     * @param tab the selected tab
     * @return the corresponding file
     */
    public File getFile(Tab tab){
        return this.tabFileMap.get(tab);
    }

    /**
     * Returns the file object in the current tab
     *
     * @return the File object of the item selected in the tab pane
     */
    public File getCurrentFile() {
        Tab selectedTab = this.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            return this.getFile(selectedTab);
        } else return null;
    }

    /**
     * return the status of the requested tab from the saveStatus HashMap
     * @param tab tab to ask for save status
     * @return boolean indicating saved or not
     */
    public boolean getSaveStatus(Tab tab){
        return this.saveStatus.get(tab);
    }

    /**
     * Returns the names of all stored files in the map
     * @return The names of all stored files in the map
     */
    public HashMap<Tab,File> getFileNames(){
        return this.tabFileMap;
    }

    /**
     * Returns the number of files in the file map
     * @return the number of files in the file map
     */
    public int getFileMapSize(){
        return this.tabFileMap.size();
    }

    /**
     * Update the corresponding file of the tab with the new file
     * @param tab the tab
     * @param file the new file
     */
    public void updateTabFileMap(Tab tab, File file){
        this.tabFileMap.replace(tab,file);
    }

    /**
     * Update the save status of the tab
     * @param tab the tab
     * @param status the new status
     */
    public void updateSaveStatus(Tab tab, boolean status){
        this.saveStatus.replace(tab,status);
    }

    /**
     * Add tab file pair to the tabFileMap
     * @param tab the tab
     * @param file the file
     */
    public void addToFileMap(Tab tab, File file){
        this.tabFileMap.put(tab,file);
    }

    /**
     * Add tab status pair to the saveStatusMap
     * @param tab the tab
     * @param status the status
     */
    public void addToSaveMap(Tab tab, boolean status){
        this.saveStatus.put(tab, status);
    }

    /**
     * Remove the tab (and its corresponding file) from the tabFileMap
     * @param tab the tab
     */
    public void removeFromFileMap(Tab tab){
        this.tabFileMap.remove(tab);
    }

    /**
     * Remove the tab (and its corresponding status) from the saveStatusMap
     * @param tab the tab
     */
    public void removeFromSaveMap(Tab tab){
        this.saveStatus.remove(tab);
    }

    /**
     * Reset the save status of the current tab
     */
    public void resetSaveStatus(){
        Tab tab = this.getSelectionModel().getSelectedItem();
        this.updateSaveStatus(tab,false);
    }

    /**
     * Using the tabPane field, gets the Code area that is currently selected.
     */
    public CodeArea getCodeArea() {
        Tab selectedTab = this.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            return (CodeArea) ((VirtualizedScrollPane) selectedTab.getContent()).getContent();
        } else
            return null;
    }
}
