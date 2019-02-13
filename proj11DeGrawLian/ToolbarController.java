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
 * Project 6/7/9
 * Date: October 26, 2018/ November 3, 2018/ November 20, 2018
 */

package proj11DeGrawLian;

import javafx.application.Platform;
import proj11DeGrawLian.bantam.ast.Program;
import proj11DeGrawLian.bantam.parser.Parser;
import proj11DeGrawLian.bantam.semant.MainMainVisitor;
import proj11DeGrawLian.bantam.semant.NumLocalVarsVisitor;
import proj11DeGrawLian.bantam.semant.StringConstantsVisitor;
import proj11DeGrawLian.bantam.treedrawer.Drawer;
import proj11DeGrawLian.bantam.util.CompilationException;
import proj11DeGrawLian.bantam.util.ErrorHandler;
import proj11DeGrawLian.bantam.util.Error;
import proj11DeGrawLian.bantam.lexer.Scanner;
import proj11DeGrawLian.bantam.lexer.Token;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * This class is the controller for all of the toolbar functionality.
 * Specifically, the compile, compile and run, and stop buttons
 *
 * @author  Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 * @author  Kevin Ahn, Jackie Hang, Matt Jones, Kevin Zhou
 * @author  Zena Abulhab, Paige Hanssen, Kyle Slager Kevin Zhou
 * @version 2.0
 * @since   10-3-2018
 *
 */
public class ToolbarController {

    private boolean scanIsDone;
    private boolean parseIsDone;
    private Console console;
    private CodeTabPane codeTabPane;
    private Program AST;

    /**
     * This is the constructor of ToolbarController.
     * @param console the console
     * @param codeTabPane the tab pane
     */
    public ToolbarController(Console console, CodeTabPane codeTabPane){
        this.console = console;
        this.codeTabPane = codeTabPane;
        this.scanIsDone = true;
        this.parseIsDone = true;
    }

    /**
     * Handles actions for scan or scanParse buttons
     * @param method the string indicating which method is called
     */
    public void handleScanOrScanParse(String method){
        if(method.equals("scan")){
            this.handleScan();
        }else if(method.equals("scanParse")){
            this.handleScanAndParse();
        }
    }

    /**
     * Handles scanning the current CodeArea, prints results to a new code Area.
     */
    public void handleScan(){
        this.scanIsDone = false;
        //declare a new thread and assign it with the work of scanning the current tab
        new Thread(()-> {
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
    public void handleScanAndParse(){
        this.parseIsDone = false;
        new Thread (()->{
            ParseTask parseTask = new ParseTask();
            FutureTask<Program> curFutureTask = new FutureTask<Program>(parseTask);
            ExecutorService curExecutor = Executors.newFixedThreadPool(1);
            curExecutor.execute(curFutureTask);
            try{
                AST = curFutureTask.get();
                if(AST != null){
                    Drawer drawer = new Drawer();
                    drawer.draw(this.codeTabPane.getFileName(),AST);
                }
                this.parseIsDone = true;
            }catch(InterruptedException| ExecutionException e){
                Platform.runLater(()-> this.console.writeToConsole("Parsing failed \n", "Error"));
            }
        }).start();

    }

    /**
     * Check if the scan task is still running.
     * @return true if this task is done, and false otherwise
     */
    public boolean scanIsDone(){
        return this.scanIsDone;
    }

    /**
     * Check if the parse task is still running.
     * @return true if this task is done, and false otherwise
     */
    public boolean parseIsDone(){
        return this.parseIsDone;
    }

    /**
     * An inner class used to parse a file in a separate thread.
     * Prints error info to the console
     */
    private class ParseTask implements Callable{

        /**
         * Create a Parser and use it to create an AST
         * @return AST tree created by a parser
         */
        @Override
        public Program call(){
            ErrorHandler errorHandler = new ErrorHandler();
            Parser parser = new Parser(errorHandler);
            String filename = ToolbarController.this.codeTabPane.getFileName();
            Program AST = null;
            try{
                AST = parser.parse(filename);
                Platform.runLater(()->ToolbarController.this.console.writeToConsole(
                        "Parsing Successful.\n", "Output"));
            }
            catch (CompilationException e){
                Platform.runLater(()-> {
                    ToolbarController.this.console.writeToConsole("Parsing Failed\n","Error");
                    ToolbarController.this.console.writeToConsole("There were: " +
                            errorHandler.getErrorList().size() + " errors in " +
                            ToolbarController.this.codeTabPane.getFileName() + "\n", "Output");

                    if (errorHandler.errorsFound()) {
                        List<Error> errorList = errorHandler.getErrorList();
                        Iterator<Error> errorIterator = errorList.iterator();
                        ToolbarController.this.console.writeToConsole("\n", "Error");
                        while (errorIterator.hasNext()) {
                            ToolbarController.this.console.writeToConsole(errorIterator.next().toString() +
                                    "\n", "Error");
                        }
                    }
                });
            }
            return AST;
        }
    }

    /**
     * A private inner class used to scan a file in a separate thread
     * Print error messages to the console and write tokens in a new tab
     */
    private class ScanTask implements Callable {
        /**
         * Start the process by creating a scanner and use it to scan the file
         * @return a result string containing information about all the tokens
         */
        @Override
        public String call(){
            ErrorHandler errorHandler = new ErrorHandler();
            Scanner scanner = new Scanner(ToolbarController.this.codeTabPane.getFileName(), errorHandler);
            Token token = scanner.scan();
            StringBuilder tokenString = new StringBuilder();

            while(token.kind != Token.Kind.EOF){
                tokenString.append(token.toString() + "\n");
                token = scanner.scan();
            }
            String resultString = tokenString.toString();
            Platform.runLater(()-> {
                ToolbarController.this.console.writeToConsole("There were: " +
                        errorHandler.getErrorList().size() + " errors in " +
                        ToolbarController.this.codeTabPane.getFileName() + "\n","Output");
                if(errorHandler.errorsFound()){
                    List<Error> errorList= errorHandler.getErrorList();
                    Iterator<Error> errorIterator = errorList.iterator();
                    ToolbarController.this.console.writeToConsole("\n","Error");

                    while(errorIterator.hasNext()){
                        ToolbarController.this.console.writeToConsole(
                                errorIterator.next().toString() + "\n","Error");
                    }
                }
                ToolbarController.this.codeTabPane.createTabWithContent(resultString);
                ToolbarController.this.scanIsDone = true;
            });
            return tokenString.toString();
        }
    }

    private void handleCheckMainHelper(){
        MainMainVisitor mainMainVisitor = new MainMainVisitor();
        boolean hasMain = mainMainVisitor.hasMain(AST);
        String msg = "a Main class with a main method in it that has void return type and has no parameters.\n";
        if(hasMain) {
            Platform.runLater(() -> this.console.writeToConsole("\nThe program contains " + msg,
                    "Output"));
        }else{
            Platform.runLater(() -> this.console.writeToConsole("\nThe program does not contain " + msg,
                    "Output"));
        }
    }

    private void handleCheckStringHelper(){
        StringConstantsVisitor stringConstantsVisitor = new StringConstantsVisitor();
        Map<String,String> stringMap = stringConstantsVisitor.getStringConstants(AST);
        Platform.runLater(() -> this.console.writeToConsole("\nString Constants:\n",
                "Output"));
        // loop through key, value pairs of map for current
        stringMap.forEach( (id, string) -> {
            String pair = id + " : " + string;
            Platform.runLater(() -> this.console.writeToConsole(pair+"\n",
                    "Output"));
        });
    }

    private void handleCheckNumLocalHelper(){
        NumLocalVarsVisitor numLocalVarsVisitor = new NumLocalVarsVisitor();
        Map<String,Integer> numVarsMap = numLocalVarsVisitor.getNumLocalVars(AST);
        Platform.runLater(() -> this.console.writeToConsole("\nNumber of Local Variables:\n",
                "Output"));
        // loop through key, value pairs of map for current
        numVarsMap.forEach( (classMethod, numLocalVars) -> {
            String pair = classMethod + " : " + numLocalVars;
            Platform.runLater(() -> this.console.writeToConsole(pair+"\n",
                    "Output"));
        });

    }

    /**
     * scan and parse the selected CodeArea, pass the AST generated by the parser to
     * one of the three public methods in the three new visitor classes
     * @param method indicating which method to call
     */
    public void handleChecks(String method){
        this.parseIsDone = false;
        new Thread (()->{
            ParseTask parseTask = new ParseTask();
            FutureTask<Program> curFutureTask = new FutureTask<Program>(parseTask);
            ExecutorService curExecutor = Executors.newFixedThreadPool(1);
            curExecutor.execute(curFutureTask);
            try{
                AST = curFutureTask.get();
                if(AST != null){
                    switch (method){
                        case "checkMain": handleCheckMainHelper();
                            break;
                        case "checkString": handleCheckStringHelper();
                            break;
                        case "checkNumLoc": handleCheckNumLocalHelper();
                            break;
                    }
                }
                this.parseIsDone = true;
            }catch(InterruptedException| ExecutionException e){
                Platform.runLater(()-> this.console.writeToConsole("Parsing failed \n", "Error"));
            }
        }).start();
    }
}