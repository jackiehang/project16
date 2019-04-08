/*
 * File: ToolbarController.java
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
 * Project 6/7/9/10
 * Date: October 26, 2018/ November 3, 2018/ November 20, 2018
 * ---------------------------
 * Edited By: Lucas DeGraw, Iris Lian
 * Project 11
 * Date: October 12, 2018
 * ----------------------------
 * Edited By: Lucas DeGraw, Jackie Hang, Chris Marcello
 * Project 12
 * Date: February 25, 2019
 * _____________________________
 * Edited By: Lucas DeGraw, Jackie Hang, Chris Marcello
 * Project 12
 * Date: March 7, 2019
 * ---------------------------
 * Edited By: Lucas DeGraw, Jackie Hang, Chris Marcello
 * Project 15
 * Date: March 21, 2019
 *
 */

package proj16DeGrawHangMarcello;

import javafx.application.Platform;
import proj16DeGrawHangMarcello.bantam.ast.Program;
import proj16DeGrawHangMarcello.bantam.semant.*;
import proj16DeGrawHangMarcello.bantam.treedrawer.Drawer;
import proj16DeGrawHangMarcello.bantam.util.ClassTreeNode;
import proj16DeGrawHangMarcello.bantam.util.CompilationException;
import proj16DeGrawHangMarcello.bantam.util.ErrorHandler;
import proj16DeGrawHangMarcello.bantam.util.Error;
import proj16DeGrawHangMarcello.bantam.lexer.Scanner;
import proj16DeGrawHangMarcello.bantam.lexer.Token;
import proj16DeGrawHangMarcello.bantam.parser.Parser;

import java.util.*;
import java.util.concurrent.*;


/**
 * This class is the controller for all of the toolbar functionality.
 * Specifically, the compile, compile and run, and stop buttons
 *
 * @author Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 * @author Kevin Ahn, Jackie Hang, Matt Jones, Kevin Zhou
 * @author Zena Abulhab, Paige Hanssen, Kyle Slager Kevin Zhou
 * @version 2.0
 * @since 10-3-2018
 */
public class ToolbarController {

    private boolean scanIsDone;
    private boolean parseIsDone;
    private boolean checkIsDone;
    private Console console;
    private CodeTabPane codeTabPane;
    private Program AST;
    private SemanticAnalyzer checker;

    /**
     * This is the constructor of ToolbarController.
     *
     * @param console     the console
     * @param codeTabPane the tab pane
     */
    public ToolbarController(Console console, CodeTabPane codeTabPane) {
        this.console = console;
        this.codeTabPane = codeTabPane;
        this.scanIsDone = true;
        this.parseIsDone = true;
        this.checkIsDone = false;
    }

    /**
     * Handles actions for scan, scanParse and scanParseCheck buttons in IDE
     * will only be called after the current file contents have been saved
     *
     * @param method the string indicating which compilation phases to execute
     */
    public void handleCompilationPhases(String method) {

        switch (method) {
            case "scan":
                this.handleScan();
                break;
            case "scanParse":
                this.handleScanAndParse(true, true);
                break;
            case "scanParseCheck":
                this.handleScanParseCheck(true, true);
                break;
            default:
                System.out.println("ERROR: UNKNOWN COMPILATION PHASES");
        }
    }

    /**
     * Handles scanning the current CodeArea, prints results to a new code Area.
     */
    public void handleScan() {
        this.scanIsDone = false;
        //declare a new thread and assign it with the work of scanning the current tab
        new Thread(() -> {
            ScanTask scanTask = new ScanTask();
            FutureTask<String> curFutureTask = new FutureTask<>(scanTask);
            ExecutorService curExecutor = Executors.newFixedThreadPool(1);
            curExecutor.execute(curFutureTask);
        }).start();
    }

    /**
     * handles scanning and parsing in the code area.
     * Draw the AST to a Java Swing window
     */
    public void handleScanAndParse(boolean drawParseTree, boolean writeToConsole) {

        this.console.clear();

        this.parseIsDone = false;

        Thread scanParseThread = new Thread(() -> {

            ParseTask parseTask = new ParseTask();

            /* denotes whether or not to write scanning/parsing results to console
             * true when called from IDE button press, false during active compilation */
            parseTask.setWriteToConsole(writeToConsole);

            FutureTask<Program> curFutureTask = new FutureTask<Program>(parseTask);
            ExecutorService curExecutor = Executors.newFixedThreadPool(1);
            curExecutor.execute(curFutureTask);
            try {
                AST = curFutureTask.get();

                if (drawParseTree) {

                    Platform.runLater(() -> {
                        if (AST != null) {

                            Drawer drawer = new Drawer();

                            JavaCodeArea curCodeArea =
                                    (JavaCodeArea) this.codeTabPane.getCodeArea();

                            // set the drawer's associated code area
                            drawer.setCorrespondingCodeArea(curCodeArea);

                            // draw the tree
                            drawer.draw(this.codeTabPane.getFileName(), AST);
                        }
                    });
                }

                this.parseIsDone = true;
                this.checkIsDone = false;
            } catch (InterruptedException | ExecutionException e) {
                if (writeToConsole) {
                    Platform.runLater(() ->
                            this.console.writeToConsole(
                                    "Parsing failed \n", "Error"));
                }

            }
        });

        // begin the thread
        scanParseThread.start();

        // wait for the thread to die
        try {
            scanParseThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * executes semantic analysis of the input file if it was parsed with no errors
     */
    public void handleScanParseCheck(boolean drawParseTree, boolean writeToConsole) {

        // scan and parse the program
        handleScanAndParse(drawParseTree, writeToConsole);

        // verify that parsing is finished
        if (!this.parseIsDone() || AST == null) {
            System.out.println("Cannot Check before parsing successfully completed");
            return;
        }

        // check the program once parsing is finished
        handleSemanticAnalysis(writeToConsole);

    }

    /**
     * executes semantic analysis of the program in a new thread
     */
    public void handleSemanticAnalysis(boolean writeToConsole) {


        // begin the semantic analysis phase in a new thread
        new Thread(() -> {

            // create and begin semantic analysis task
            CheckTask checkTask = new CheckTask();

            // tell it whether or not to write to the console
            checkTask.setWriteToConsole(writeToConsole);

            FutureTask<ClassTreeNode> curFutureTask = new FutureTask<ClassTreeNode>(checkTask);
            ExecutorService curExecutor = Executors.newFixedThreadPool(1);
            curExecutor.execute(curFutureTask);

            try {
                // get the root of the class hierarchy tree to be used for code generation
                ClassTreeNode root = curFutureTask.get();
                this.checkIsDone = true;

            } catch (InterruptedException | ExecutionException e) {
                Platform.runLater(() ->
                        this.console.writeToConsole("Semantic Analysis failed \n", "Error"));
            }
        }).start();


    }

    /**
     * allows user to navigate through the file to find declarations of
     * classes, fields, and methods
     */
    public void handleNavigate() {

        if (!this.checkIsDone) {
            Platform.runLater(() -> this.console.writeToConsole("You must parse and check a program first.\n",
                    "Error"));
        } else {
            Navigator navigator = new Navigator(codeTabPane.getCodeArea(), this.checker);
        }

    }

    /**
     * Check if the scan task is still running.
     *
     * @return true if this task is done, and false otherwise
     */
    public boolean scanIsDone() {
        return this.scanIsDone;
    }

    /**
     * Check if the parse task is still running.
     *
     * @return true if this task is done, and false otherwise
     */
    public boolean parseIsDone() {
        return this.parseIsDone;
    }

    /**
     * sets that the semantic check has not been done
     */
    public void setCheckNotDone() {
        this.checkIsDone = false;
    }

    /**
     * A private inner class used to scan a file in a separate thread
     * Print error messages to the console and write tokens in a new tab
     */
    private class ScanTask implements Callable {
        /**
         * Start the process by creating a scanner and use it to scan the file
         *
         * @return a result string containing information about all the tokens
         */
        @Override
        public String call() {

            ErrorHandler errorHandler = new ErrorHandler();
            Scanner scanner = new Scanner(ToolbarController.this.codeTabPane.getFileName(), errorHandler);
            Token token = scanner.scan();
            StringBuilder tokenString = new StringBuilder();

            while (token.kind != Token.Kind.EOF) {
                tokenString.append(token.toString() + "\n");
                token = scanner.scan();
            }
            String resultString = tokenString.toString();

            Platform.runLater(() -> {
                ToolbarController.this.console.writeToConsole("There were: " +
                        errorHandler.getErrorList().size() + " errors in " +
                        ToolbarController.this.codeTabPane.getFileName() + "\n", "Output");

                if (errorHandler.errorsFound()) {

                    List<Error> errorList = errorHandler.getErrorList();
                    Iterator<Error> errorIterator = errorList.iterator();
                    ToolbarController.this.console.writeToConsole("\n", "Error");

                    while (errorIterator.hasNext()) {
                        ToolbarController.this.console.writeToConsole(
                                errorIterator.next().toString() + "\n", "Error");
                    }
                }
                ToolbarController.this.codeTabPane.createTabWithContent(resultString);
                ToolbarController.this.scanIsDone = true;
            });
            return tokenString.toString();
        }
    }

    /**
     * An inner class used to parse a file in a separate thread.
     * Prints error info to the console
     */
    private class ParseTask extends ActiveCompilationPhase implements Callable {

        /**
         * Create a Parser and use it to create an AST
         *
         * @return AST tree created by a parser
         */
        @Override
        public Program call() {

            JavaCodeArea codeArea = (JavaCodeArea) codeTabPane.getCodeArea();

            ErrorHandler errorHandler = new ErrorHandler();
            Parser parser = new Parser(errorHandler);
            String filename = ToolbarController.this.codeTabPane.getFileName();
            Program AST = null;
            try {
                AST = parser.parse(filename);
                codeArea.removePreviousSelections();    // remove errors due to success

                if (this.writeToConsole) {  // inherited field
                    Platform.runLater(() ->
                    {
                        ToolbarController.this.console.writeToConsole(
                                "Parsing Successful.\n", "Output");
                    });
                }
            } catch (CompilationException e) {

                Platform.runLater(() -> {

                    if (errorHandler.errorsFound()) {

                        List<Error> errorList = errorHandler.getErrorList();

                        // tell code area to actively display errors
                        codeArea.setRealTimeErrors(errorList);

                        if (this.writeToConsole) {  // inherited field

                            ToolbarController.this.console.writeToConsole("Parsing Failed\n", "Error");
                            ToolbarController.this.console.writeToConsole("There were: " +
                                    errorHandler.getErrorList().size() + " errors in " +
                                    ToolbarController.this.codeTabPane.getFileName() + "\n", "Output");

                            Iterator<Error> errorIterator = errorList.iterator();
                            ToolbarController.this.console.writeToConsole("\n", "Error");
                            while (errorIterator.hasNext()) {
                                ToolbarController.this.console.writeToConsole(errorIterator.next().toString() +
                                        "\n", "Error");
                            }
                        }
                    }
                });

            }
            return AST;
        }
    }

    /**
     * An inner class used to perform semantic analysis in a separate thread
     * Prints error info to the console
     */
    private class CheckTask extends ActiveCompilationPhase implements Callable {

        @Override
        public ClassTreeNode call() {

            JavaCodeArea codeArea = (JavaCodeArea) codeTabPane.getCodeArea();

            // create an error handler
            ErrorHandler errorHandler = new ErrorHandler();

            // create a checker that uses the new error handler
            checker = new SemanticAnalyzer(errorHandler);

            // initialize the root of the class hierarchy tree to be used for code generation
            ClassTreeNode root = null;
            try {
                // attempt to analyze the abstract syntax tree
                root = checker.analyze(AST);
                codeArea.removePreviousSelections();    // remove errors due to success

                if (this.writeToConsole) {
                    // if checking phase generated no errors, display a success message
                    Platform.runLater(() -> ToolbarController.this.console.writeToConsole(
                            "Semantic Analysis Successful.\n", "Output"));
                }
            } catch (RuntimeException e) {


                // if any exceptions were thrown during semantic analysis,
                Platform.runLater(() -> {

                    // display each individual error in the console
                    if (errorHandler.errorsFound()) {

                        List<Error> errorList = errorHandler.getErrorList();

                        // tell code area to actively display errors
                        codeArea.setRealTimeErrors(errorList);

                        if (this.writeToConsole) {

                            // display error message in the console
                            ToolbarController.this.console.writeToConsole("Semantic Analysis Failed\n", "Error");

                            // display num errors in the console
                            ToolbarController.this.console.writeToConsole("There were: " +
                                    errorHandler.getErrorList().size() + " errors in " +
                                    ToolbarController.this.codeTabPane.getFileName() + "\n", "Output");

                            Iterator<Error> errorIterator = errorList.iterator();
                            ToolbarController.this.console.writeToConsole("\n", "Error");
                            while (errorIterator.hasNext()) {
                                ToolbarController.this.console.writeToConsole(errorIterator.next().toString() +
                                        "\n", "Error");
                            }
                        }
                    }
                });
            }
            return root;
        }
    }

}