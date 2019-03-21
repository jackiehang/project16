/*
 * File: StructureViewController.java
 * CS361 Project 6
 * Names: Melody Mao, Zena Abulhab, Yi Feng, and Evan Savillo
 * Date: 10/27/2018
 * ---------------------------
 * Edited By: Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 * Project 6/7/9
 * Date: October 26, 2018/ November 3, 2018/ November 20, 2018
*/

package proj15DeGrawHangMarcello;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.input.KeyEvent;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import org.fxmisc.richtext.CodeArea;
import proj15DeGrawHangMarcello.Java8.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;


/**
 * Controller that manages the generation and display of the structure of the
 * java code in the file currently being viewed.
 * @author Melody Mao, Zena Abulhab, Yi Feng, and Evan Savillo
 */
public class StructureViewController
{
    private Map<TreeItem, Integer> treeItemLineNumMap;
    private CodeTabPane codeTabPane;
    private TreeView<String> treeView;
    private final ParseTreeWalker walker;
    private TreeItem<String> structureTreeRoot;


    /**
     * Constructor for this class
     * @param fileStructureTree the TreeView object passed by master controller
     *                          It holds all structure information
     */
    public StructureViewController(TreeView<String> fileStructureTree, CodeTabPane codeTabPane) {
        this.walker = new ParseTreeWalker();
        this.codeTabPane=codeTabPane;
        this.treeItemLineNumMap = new HashMap<>();
        this.treeView = fileStructureTree;

        // Updates the file structure view whenever a key is typed
        this.codeTabPane.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            this.updateStructureView();
        });
        //update the file structure view when user change tab
        this.codeTabPane.getSelectionModel().selectedItemProperty().addListener((observableValue, oldTab, newTab) -> {
            this.updateStructureView();
        });
    }

    /**
     * Parses a file thereby storing contents as TreeItems in our special tree.
     * @param fileContents the file to be parsed
     */
    public TreeItem<String> generateStructureTree(String fileContents)
    {
        TreeItem<String> newRoot = new TreeItem<>(fileContents);

        //build lexer, parser, and parse tree for the given file
        Java8Lexer lexer = new Java8Lexer(CharStreams.fromString(fileContents));
        lexer.removeErrorListeners();

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Java8Parser parser = new Java8Parser(tokens);
        parser.removeErrorListeners();

        ParseTree tree = parser.compilationUnit();

        //walk through parse tree with listening for code structure elements
        CodeStructureListener codeStructureListener = new CodeStructureListener(newRoot, this.treeItemLineNumMap);
        this.walker.walk(codeStructureListener, tree);

        return newRoot;
    }

    public void updateStructureView(){
        //declare a new thread and assign it with the work of updating the structure view
        new Thread(()->{
            updateStructureViewTask updateStructViewTask = new updateStructureViewTask(this.codeTabPane,this);
            FutureTask<TreeItem<String>> curFutureTask = new FutureTask<>(updateStructViewTask );
            ExecutorService curExecutor = Executors.newFixedThreadPool(1);
            curExecutor.execute(curFutureTask);
            //when the updating structure task is successfully done, close the current executor
            try {
                this.structureTreeRoot = curFutureTask.get();
                curExecutor.shutdown();
                //if the update failed, an dialog box will pops up reporting error
            }catch (InterruptedException| ExecutionException e){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Failed to update structure view.");
                alert.showAndWait();
            }
            //set the root node of the current structure view to the new root node
            Platform.runLater(()-> {
                this.setRootNode(this.structureTreeRoot);
            });
        }).start();
    }

    /**
     * Sets the currently displaying File TreeItem<String> View.
     *
     * @param root root node corresponding to currently displaying file
     */
    public void setRootNode(TreeItem<String> root)
    {
        this.treeView.setRoot(root);
        this.treeView.setShowRoot(false);
    }

    /**
     * Returns the line number currently associated with the specified tree item
     *
     * @param treeItem Which TreeItem to get the line number of
     * @return the line number corresponding with that tree item
     */
    public Integer getTreeItemLineNum(TreeItem treeItem) {
        return this.treeItemLineNumMap.get(treeItem);
    }

    /**
     * Private helper class that listens for code structure declarations
     * (classes, fields, methods) during a parse tree walk and builds a
     * TreeView subtree representing the code structure.
     */
    private class CodeStructureListener extends Java8BaseListener
    {
        private Image classPic;
        private Image methodPic;
        private Image fieldPic;
        private TreeItem<String> currentNode;
        private Map<TreeItem, Integer> treeItemIntegerMap;

        /**
         * creates a new CodeStructureListener that builds a subtree
         * from the given root TreeItem
         *
         * @param root root TreeItem to build subtree from
         * @param treeItemIntegerMap a map class that maps the tree item to the line number
         */
        public CodeStructureListener(TreeItem<String> root, Map<TreeItem, Integer> treeItemIntegerMap)
        {
            this.currentNode = root;
            this.treeItemIntegerMap = treeItemIntegerMap;
            this.classPic = new Image(getClass().getResource("resources/c.png").toExternalForm());
            this.methodPic = new Image(getClass().getResource("resources/m.png").toExternalForm());
            this.fieldPic = new Image(getClass().getResource("resources/f.png").toExternalForm());
        }

        /**
         * Starts a new subtree for the class declaration entered
         * @param declarationContext the context class for the declared context
         */
        @Override
        public void enterNormalClassDeclaration(Java8Parser.NormalClassDeclarationContext declarationContext)
        {
            //get class name
            TerminalNode node = declarationContext.Identifier();
            if(node!=null) {
                String className = node.getText();

                //add class to TreeView under the current class tree
                //set up the icon
                //store the line number of its declaration
                TreeItem<String> newNode = new TreeItem<>(className);
                newNode.setGraphic(new ImageView(this.classPic));
                newNode.setExpanded(true);
                this.currentNode.getChildren().add(newNode);
                this.currentNode = newNode; //move current node into new subtree
                this.treeItemIntegerMap.put(newNode, declarationContext.getStart().getLine());
            }
        }

        /**
         * ends the new subtree for the class declaration exited,
         * returns traversal to parent node
         * @param declarationContext the context class for the declared context
         */
        @Override
        public void exitNormalClassDeclaration(Java8Parser.NormalClassDeclarationContext declarationContext)
        {
            this.currentNode = this.currentNode.getParent(); //move current node back to parent
        }

        /**
         * adds a child node for the field entered under the TreeItem for the current class
         * @param fieldDeclarationContext the field context class for the declared context
         */
        @Override
        public void enterFieldDeclaration(Java8Parser.FieldDeclarationContext fieldDeclarationContext)
        {
            //get field name
            TerminalNode node = fieldDeclarationContext.variableDeclaratorList().variableDeclarator(0).variableDeclaratorId().Identifier();
            String fieldName = node.getText();

            //add field to TreeView under the current class tree
            //set up the icon
            //store the line number of its declaration
            TreeItem<String> newNode = new TreeItem<>(fieldName);
            newNode.setGraphic(new ImageView(this.fieldPic));
            this.currentNode.getChildren().add(newNode);
            this.treeItemIntegerMap.put(newNode, fieldDeclarationContext.getStart().getLine());
        }

        /**
         * adds a child node for the method entered under the TreeItem for the current class
         * @param headerDeclarationContext the header context class for the declared context
         */
        @Override
        public void enterMethodHeader(Java8Parser.MethodHeaderContext headerDeclarationContext)
        {
            //get method name
            TerminalNode nameNode = headerDeclarationContext.methodDeclarator().Identifier();
            String methodName = nameNode.getText();

            //add method to TreeView under the current class tree
            //set up the icon
            //store the line number of its declaration
            TreeItem<String> newNode = new TreeItem<>(methodName);
            newNode.setGraphic(new ImageView(this.methodPic));
            this.currentNode.getChildren().add(newNode);
            this.treeItemIntegerMap.put(newNode, headerDeclarationContext.getStart().getLine());

        }
    }
    /**
     * An inner class used for a thread to execute the run task
     * Designed to be used for compilation or running.
     * Writes the input/output error to the console.
     */
    private class updateStructureViewTask implements Callable<TreeItem<String>> {
        private CodeTabPane codeTabPane;
        private StructureViewController structureViewController;

        /**
         * Constructor for the task class that updates the structure view
         * @param codeTabPane the codeTabPane class to access the codearea and current file
         * @param structureViewController the structure view controller class to access the structure tree
         */

        public updateStructureViewTask(CodeTabPane codeTabPane,
                                       StructureViewController structureViewController){
            this.codeTabPane=codeTabPane;
            this.structureViewController = structureViewController;
        }

        /**
         * the run function that override the original ones offered by the runnable class
         * This one updates the structure view
         * @return the TreeItem root that indicates the file to be parsed
         */
        @Override
        public TreeItem<String> call(){
            CodeArea currentCodeArea=codeTabPane.getCodeArea();
            File currentFile=codeTabPane.getCurrentFile();
            // if the code area is open
            if (currentCodeArea != null) {
                // if this is not an unsaved file
                if (currentFile != null) {
                    String fileName = currentFile.getPath();
                    // if this is a java file
                    if (fileName.endsWith(".java")) {
                        // Re-generates the tree
                        return structureViewController.generateStructureTree(currentCodeArea.getText());
                    }
                }
            }
            return null;
        }

    }
}