/*
 * File: MasterController.java
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
 * ---------------------------
 * Edited By: Lucas DeGraw, Iris Lian
 * Project 11
 * Date: October 12, 2018
 */

package proj13DeGrawHang;

import javafx.beans.property.SimpleListProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.event.Event;
import org.fxmisc.richtext.CodeArea;
import java.io.*;
import java.time.Duration;
import java.util.Optional;

/**
 * This is the master controller for the program. it references
 * the other controllers for proper menu functionality.
 *
 * @author  Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 * @author  Kevin Ahn, Jackie Hang, Matt Jones, Kevin Zhou
 * @author  Zena Abulhab, Paige Hanssen, Kyle Slager, Kevin Zhou
 * @version 2.0
 * @since   10-3-2018
 */
public class MasterController {
    @FXML private Menu editMenu;
    @FXML private CodeTabPane codeTabPane;
    @FXML private VBox vBox;
    @FXML private MenuItem saveMenuItem;
    @FXML private MenuItem saveAsMenuItem;
    @FXML private MenuItem closeMenuItem;
    @FXML private Console console;
    @FXML private TabPane structureTabPane;
    @FXML private CheckMenuItem fileStructureItem;
    @FXML private CheckMenuItem directoryTreeItem;

    @FXML private Button scanButton;
    @FXML private Button scanParseButton;
    @FXML private Button scanParseCheckButton;
    @FXML private Button navigatorButton;
    @FXML private TreeView<String> directoryTree;
    @FXML private TreeView<String> fileStructureTree;

    private EditController editController;
    private FileController fileController;
    private ToolbarController toolbarController;
    private PreferenceController preferenceController;
    private DirectoryController directoryController;
    private HelpMenuController helpMenuController;
    private StructureViewController structureViewController;


    /**
     * Initializes all the controllers and binds some properties
     */
    public void initialize(){
        //make sure the user doesn't close the structure tabs
        this.structureTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        //initialize the popup menus that are clicked on CodeArea, tab, and console
        CodeAreaContextMenu codeAreaContextMenu = new CodeAreaContextMenu(this);
        TabContextMenu tabContextMenu = new TabContextMenu(this);
        ConsoleContextMenu consoleContextMenu = new ConsoleContextMenu(this);

        //initialize the controllers
        this.editController = new EditController(this.codeTabPane);
        this.fileController = new FileController(this.vBox,this.codeTabPane,this);
        this.toolbarController = new ToolbarController(this.console, this.codeTabPane);

        this.preferenceController = new PreferenceController(this.vBox, this.console, structureTabPane,
                fileStructureItem,directoryTreeItem,this.fileStructureTree,this.directoryTree);
        this.helpMenuController = new HelpMenuController();

        this.directoryController = new DirectoryController(this.directoryTree,this.codeTabPane,
                this.codeTabPane.getFileNames());
        this.structureViewController=new StructureViewController(this.fileStructureTree, this.codeTabPane);

        //bind the edit, save, saveAs, close menus to the property of a list of opened tabs
        SimpleListProperty<Tab> tabsProperty = new SimpleListProperty<> (this.codeTabPane.getTabs());

        this.editMenu.disableProperty().bind(tabsProperty.emptyProperty());
        this.saveMenuItem.disableProperty().bind(tabsProperty.emptyProperty());
        this.saveAsMenuItem.disableProperty().bind(tabsProperty.emptyProperty());
        this.closeMenuItem.disableProperty().bind(tabsProperty.emptyProperty());

        //disable Toolbar items to start and pass controllers and context menu to console
        this.disableToolbar();
        this.console.setToolbarController(this.toolbarController);
        this.console.setContextMenu(consoleContextMenu);

        this.codeTabPane.passControllerContextMenus(this,codeAreaContextMenu,tabContextMenu);

    }

    /**
     * Handler for the "About" menu item in the "File" menu.
     * Creates an Information alert dialog to display author and information of this program
     */
    @FXML public void handleAbout() { this.helpMenuController.handleAbout(); }

    /**
     * Handler for the "New" menu item in the "File" menu.
     * Adds a new Tab to the TabPane, and also adds null to the HashMap
     * Also sets the current tab for both the file and edit controllers.
     */
    @FXML public void handleNew() {
        fileController.handleNew();
        if(toolbarController.scanIsDone()) {
            this.scanButton.setDisable(false);
            this.scanParseButton.setDisable(false);
            this.scanParseCheckButton.setDisable(false);
            this.navigatorButton.setDisable(false);
        }
        this.updateStructureView();
        setInlineParsing();
    }

    /**
     * Handler for the "Open" menu item in the "File" menu.
     * Creates a FileChooser to select a file
     * Use scanner to read the file and write it into a new tab.
     * Also sets the current tab for both the file and edit controllers.
     */
    @FXML public void handleOpen() {
        File file = fileController.handleOpenDialog();
        fileController.handleOpen(file);
        if(toolbarController.scanIsDone() && !this.codeTabPane.getTabs().isEmpty()) {
            this.scanButton.setDisable(false);
            this.scanParseButton.setDisable(false);
            this.scanParseCheckButton.setDisable(false);
            this.navigatorButton.setDisable(false);
        }
        this.updateStructureView();
        this.createDirectoryTree();
        setInlineParsing();
    }

    /**
     * sets up code areas to auto-save on a key release and to parse every 500 ms if
     * if the code area has been changed
     */
    private void setInlineParsing() {

        // get the current code area
        JavaCodeArea codeArea = (JavaCodeArea)this.codeTabPane.getCodeArea();

        // save the file after each key press
        codeArea.setOnKeyReleased((e) -> this.handleSave());

        // subscribe
        codeArea.multiPlainChanges()

                // do not emit an event until 1000 ms have passed since the last emission of previous stream
                .successionEnds(Duration.ofMillis(500))

                // run the following code block when previous stream emits an event
                .subscribe(ignore -> this.toolbarController.handleScanAndParse());
    }

    /**
     * Handler for the "Close" menu item in the "File" menu.
     * Checks to see if the file has been changed since the last save.
     * If changes have been made, redirect to askSave and then close the tab.
     * Otherwise, just close the tab.
     */
    @FXML public void handleClose(Event event) {
        fileController.handleClose(event);
        if (this.codeTabPane.getTabs().isEmpty()&&toolbarController.scanIsDone()){
            disableToolbar();
        }
    }

    /**
     * Handler for the "Save" menu item in the "File" menu.
     * If the current tab has been saved before, writes out the content to its corresponding
     * file in storage.
     * Else if the file has never been saved, opens a pop-up window that allows the user to
     * choose a filename and directory and then store the content of the tab to storage.
     */
    @FXML public void handleSave() { fileController.handleSave(); }

    /**
     * Handler for the "Save as..." menu item in the "File" menu.
     * Opens a pop-up window that allows the user to choose a filename and directory.
     * Calls writeFile to save the file to memory.
     * Changes the name of the current tab to match the newly saved file's name.
     */
    @FXML public void handleSaveAs( ) { fileController.handleSaveAs(); }

    /**
     * Handler for the "Exit" menu item in the "File" menu.
     * Closes all the tabs using handleClose()
     * Returns when the user cancels exiting any tab.
     * @param event an event
     */
    @FXML public void handleExit(Event event) { fileController.handleExit(event); }

    /**
     * Handler for the "Undo" menu item in the "Edit" menu.
     */
    @FXML
    public void handleUndo() { editController.handleUndo(); }

    /**
     * Handler for the "Redo" menu item in the "Edit" menu.
     */
    @FXML
    public void handleRedo() { editController.handleRedo(); }

    /**
     * Handler for the "Cut" menu item in the "Edit" menu.
     */
    @FXML
    public void handleCut() { editController.handleCut(); }

    /**
     * Handler for the "Copy" menu item in the "Edit" menu.
     */
    @FXML
    public void handleCopy() { editController.handleCopy();}

    /**
     * Handler for the "Paste" menu item in the "Edit" menu.
     */
    @FXML
    public void handlePaste() { editController.handlePaste(); }

    /**
     * Handler for the "Comment with Line Comments" menu item in the "Edit" menu.
     */
    @FXML
    public void handleLineComment(){ editController.handleLineComment(); }

    /**
     * Handler for the "Comment with Block Comments" menu item in the "Edit" menu.
     */
    @FXML
    public void handleBlockComment(){ editController.handleBlockComment(); }

    /**
     * Handler for the "SelectAll" menu item in the "Edit" menu.
     */
    @FXML
    public void handleSelectAll() { editController.handleSelectAll(); }

    /**
     * Handler for the "Entab" menu item in the "Edit" menu.
     */
    @FXML
    public void handleEntab() {
        editController.handleEntabOrDetab("entab");
    }

    /**
     * Handler for the "Detab" menu item in the "Edit" menu.
     */
    @FXML
    public void handleDetab() {
        editController.handleEntabOrDetab("detab");
    }

    /**
     * Handler for the "Find & Replace" menu item in the "Edit menu.
      */
    @FXML
    public void handleFindReplace(){ editController.handleFindReplace();  }

    /**
     * Handler for the "NightMode" Toggle menu item in the "Preferences" Menu.
      */
    @FXML
    public void handleNightMode(){ preferenceController.handleNightMode(); }

    /**
     * Handler for the "Java Tutorial" menu item in the "Help" Menu.
     * When the item is clicked, a Java tutorial will be opened in a browser.
     */
    @FXML
    public void handleJavaTutorial(){
        this.helpMenuController.handleJavaTutorial();
    }

    /**
     * Calls the method that handles the Keyword color menu item from the PreferenceController.
     */
    @FXML public void handleKeywordColorAction() { this.preferenceController.handleColorAction("Keyword"); }

    /**
     * Calls the method that handles the Parentheses/Brackets color menu item from the PreferenceController.
     */
    @FXML public void handleParenColorAction() { this.preferenceController.handleColorAction("Paren"); }

    /**
     * Calls the method that handles the String color menu item from the PreferenceController.
     */
    @FXML public void handleStrColorAction() { this.preferenceController.handleColorAction("Str"); }

    /**
     * Calls the method that handles the Int color menu item from the PreferenceController.
     */
    @FXML public void handleIntColorAction() { this.preferenceController.handleColorAction("Int"); }

    /**
     * Jump to the line where the selected class/method/field is declared.
     */
    @FXML
    private void handleTreeItemClicked()
    {
        //get the selected tree item and get the codeArea it corresponds to
        TreeItem selectedTreeItem = (TreeItem) this.fileStructureTree.getSelectionModel().getSelectedItem();
        CodeArea currentCodeArea = this.codeTabPane.getCodeArea();

        //jump to the line in the codeArea where the selected class/method/field is declared
        if (selectedTreeItem != null)
        {
            int lineNum = this.structureViewController.getTreeItemLineNum(selectedTreeItem);
            if (currentCodeArea != null) currentCodeArea.showParagraphAtTop(lineNum - 1);
        }
    }

    /**
     * Event handler to open a file selected from the directory
     *
     * @param event a MouseEvent object
     */
    @FXML
    private void handleDirectoryItemClicked(MouseEvent event){
        // only open file if double clicked
        if (event.getClickCount() == 2 && !event.isConsumed()) {
            event.consume();
            TreeItem selectedItem = directoryTree.getSelectionModel().getSelectedItem();

            //fixes bug where pressing arrow throws NullPointerException
            if(selectedItem == null){
                return;
            }
            //check if the selected file is a java file and open it if so
            String fileName = (String) selectedItem.getValue();
            if (fileName.endsWith(".java")) {
                this.fileController.handleOpen(this.directoryController.getTreeItemFileMap().get(selectedItem));
            }
        }
    }

    /**
     * handles checkMenuItem for File Structure Tab
     * Opens/closes the tab
     */
    @FXML
    public void handleFileStructureTab(){
        this.preferenceController.handleFileStructureTab();
    }

    /**
     * handles checkMenuItem for Directory Tree Tab
     * Opens/closes the tab
     */
    @FXML
    public void handleDirectoryTreeTab(){
        this.preferenceController.handleDirectoryTreeTab();
    }

    /**
     * Calls handleMatchBracketOrParen() of the editController
     */
    @FXML
    public void handleMatchBracketOrParen() {
        editController.handleMatchBracketOrParen();
    }


    /**
     * Parses and generates the structure view for the currently open code area
     */
    public void updateStructureView(){
        structureViewController.updateStructureView();
    }


    /**
     * close all tabs when Close All menu item is clicked
     * @param event an action event
     */
    public void handleCloseAll(Event event){
        fileController.handleCloseAll(event);
    }

    /**
     * clears the console
     */
    public void handleClearConsole(){
        this.console.clear();
    }

    /**
     * Pops up a dialog asking if the user wants to save the changes
     * and return a string indicating which button the user clicked
     * @param title the title of the dialog
     * @param header the header of the dialog
     * @param context the context of the dialog
     * @return a string indicating which button the user clicked
     */
    public String askSaveDialog(String title, String header, String context){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(context);
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);
        Optional<ButtonType> result = alert.showAndWait();
        if(result.get() == yesButton){
            return "yesButton";
        }
        else if(result.get() == noButton){
            return "noButton";
        }
        else{
            return "cancelButton";
        }
    }

    /**
     * Disables the buttons in the toolbar
     */
    private void disableToolbar(){
        this.scanButton.setDisable(true);
        this.scanParseButton.setDisable(true);
        this.scanParseCheckButton.setDisable(true);
        this.navigatorButton.setDisable(true);
   }

    /**
     * Call the createDirectoryTree function in the directory controller
     */
    public void createDirectoryTree(){
        this.directoryController.createDirectoryTree();
    }

    /**
     * routes to compilation phase handler
     * tells it to scan the current file
     */
    @FXML
    public void handleScan(){
        this.handleCompilationPhases("scan");
    }

    /**
     * routes to compilation phase handler
     * tells it to scan and parse the current file
     */
    @FXML
    private void handleScanAndParse(){
        this.handleCompilationPhases("scanParse");
    }

    /**
     * routes to compilation phase handler
     * tells it to scan, parse and check (semantic analysis) the current file
     */
    @FXML
    private void handleScanParseCheck() {
        handleCompilationPhases("scanParseCheck");
    }

    /**
     * calls handleNavigate in toolbarController
     */
    @FXML
    private void handleNavigate(){ toolbarController.handleNavigate();}

    /**
     * prompts user to save file before telling the toolbarController to handle the
     * appropriate compilation steps indicate by the phasesToExecute string
     *
     * @param phasesToExecute a string indicating which compilation phases should be done
     *                        either: "scan", "scanParse" or "scanParseCheck"
     */
    private void handleCompilationPhases(String phasesToExecute) {
        Tab curTab = this.codeTabPane.getSelectionModel().getSelectedItem();

        if (this.codeTabPane.getSaveStatus(curTab)) {
            toolbarController.handleCompilationPhases(phasesToExecute);
        } else {
            askToSave(curTab, phasesToExecute);
        }
    }


    /**
     *
     * @param curTab the currently selected tab in the IDE
     * @param phasesToExecute a string indicating which compilation phases should be done
     *                        either: "scan", "scanParse" or "scanParseCheck"
     */
    private void askToSave(Tab curTab, String phasesToExecute) {
        String saveResult = this.askSaveDialog(null,
                "Do you want to save your changes?",null);
        switch (saveResult) {
            case("yesButton"):  // if they saved

                // false only when the "current" file is null from closing file
                boolean isNotCancelled = fileController.handleSave();

                if(isNotCancelled){
                    toolbarController.handleCompilationPhases(phasesToExecute);
                }

                break;
            case("noButton"):
                if (this.codeTabPane.getFileName() == null) {
                    this.console.writeToConsole("File Not Found: " + curTab.getText() +"\n","Error");
                    this.console.writeToConsole("File must be saved to scan \n","Error");
                }
                return;
            case("cancelButton"):
                return;
        }
    }

}
