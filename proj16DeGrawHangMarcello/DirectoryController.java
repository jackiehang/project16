/*
 * File: DirectoryController.java
 * CS361 Project 6
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 10/27/2018
 * This file contains the DirectoryController class, handling the file directory portion of the GUI.
 * ---------------------------
 * Edited By: Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 * Project 6/7/9
 * Date: October 26, 2018/ November 3, 2018/ November 20, 2018
 */
package proj16DeGrawHangMarcello;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import javafx.application.Platform;
import javafx.scene.control.*;

/**
 * This controller handles directory related actions.
 *
 * @author Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * @author Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 */
public class DirectoryController {
    private TreeView<String> directoryTree;
    //a HashMap mapping the tabs and the associated files
    private Map<Tab, File> tabFileMap;
    //A HashMap mapping the TreeItems and associated files
    private Map<TreeItem<String>, File> treeItemFileMap;
    private TabPane tabPane;
    private TreeItem<String> directoryTreeRoot;

    /**
     * This is the constructor of DirectoryController.
     * @param directoryTree the directory tree with file structures
     * @param tabPane the tab pane
     * @param tabFileMap  the HashMap mapping the tabs and the associated files
     */
    public DirectoryController(TreeView<String> directoryTree, TabPane tabPane, Map<Tab, File> tabFileMap){
        this.directoryTree = directoryTree;
        this.tabFileMap = tabFileMap;
        this.treeItemFileMap = new HashMap<>();
        this.tabPane = tabPane;
        // add listener to tab selection to switch directories based on open file
        this.tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) ->
                this.createDirectoryTree());
    }

    /**
     * Returns the directory tree for the given file
     *
     * @param file the file
     * @return the root TreeItem of the tree
     */
    private TreeItem<String> getNode(File file) {
        // create root, which is returned at the end
        TreeItem<String> root = new TreeItem<>(file.getName());
        this.treeItemFileMap.put(root, file);

        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                // recursively traverse file directory
                root.getChildren().add(getNode(f));
            } else {
                TreeItem<String> leaf = new TreeItem<>(f.getName());
                root.getChildren().add(leaf);
                this.treeItemFileMap.put(leaf, f);
            }
        }
        return root;
    }

    /**
     * Adds the directory tree for the current file to the GUI
     */
    public void createDirectoryTree() {
        new Thread(()->{
            createDirectoryTreeTask createDirectoryTreeTask=new createDirectoryTreeTask(this.tabFileMap,
                    this.tabPane, this);
            FutureTask<TreeItem<String>> curFutureTask = new FutureTask<>(createDirectoryTreeTask);
            ExecutorService curExecutor = Executors.newFixedThreadPool(1);
            curExecutor.execute(curFutureTask);
            try{
                this.directoryTreeRoot = curFutureTask.get();
                curExecutor.shutdown();
            }catch (InterruptedException|ExecutionException e){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Failed to create directory tree.");
                alert.showAndWait();
            }
            Platform.runLater(()->{
                this.directoryTree.setRoot(this.directoryTreeRoot);
            });
        }).start();
    }

    /**
     * Returns the tree item map
     * @return the HashMap mapping the TreeItems and associated files
     */
    public Map<TreeItem<String>, File> getTreeItemFileMap(){
        return this.treeItemFileMap;
    }

    /**
     * A private Callable class to be executed in a thread. This class updates the structure view
     */
    private class createDirectoryTreeTask implements Callable<TreeItem<String>> {
        private Map<Tab, File> tabFileMap;
        private TabPane tabPane;
        private DirectoryController directoryController;

        /**
         * Constructor
         * @param tabFileMap the tabFileMap in the directory controller
         * @param tabPane the tabPane in the directory controller
         * @param directoryController the directory controller
         */
        public createDirectoryTreeTask(Map<Tab, File> tabFileMap, TabPane tabPane,
                                        DirectoryController directoryController){
            this.tabFileMap=tabFileMap;
            this.tabPane=tabPane;
            this.directoryController=directoryController;

        }

        /**
         * create the directory tree root and return it
         * @return the created tree root
         */
        @Override
        public TreeItem<String> call(){
            File file = this.tabFileMap.get(this.tabPane.getSelectionModel().getSelectedItem());
            // create the directory tree
            if (file != null) {
                return this.directoryController.getNode(file.getParentFile());
            }
            else return null;
        }
    }
}

