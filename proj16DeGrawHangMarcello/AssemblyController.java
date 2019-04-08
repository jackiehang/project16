/**
 * ToolbarController handles Toolbar related actions
 *
 * @author Evan Savillo
 * @author Yi Feng
 * @author Zena Abulhab
 * @author Melody Mao
 *
 * Edited by Lucas DeGraw on 3/21/19
 */

package proj16DeGrawHangMarcello;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.scene.control.*;

import java.io.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class AssemblyController
{
    private Map<Tab, File> tabFileMap;
    private Tab selectedTab;


    /**
     * Console defined in Main.fxml
     */
    private Console console;
    /**
     * Process currently compiling or running a Java file
     */
    private Process curProcess;
    /**
     * Thread representing the Java program input stream
     */
    private Thread inThread;
    /**
     * Thread representing the Java program output stream
     */
    private Thread outThread;

    /**
     * Mutex lock to control input and output threads' access to console
     */
    private Semaphore mutex;
    /**
     * The consoleLength of the output on the console
     */
    private int consoleLength;

    /**
     * A CompileWorker object compiles a Java file in a separate thread.
     */
    private CompileWorker compileWorker;
    /**
     * A CompileRunWorker object compiles and runs a Java file in a separate thread.
     */
    private CompileRunWorker compileRunWorker;

    /**
     * constructor for the Compilation Controller Class
     *
     * @param console
     * @param tabFileMap
     */
    public AssemblyController(Console console,
                              Map<Tab, File> tabFileMap)
    {
        this.console = console;
        this.tabFileMap = tabFileMap;
        this.mutex = new Semaphore(1);
        this.compileWorker = new CompileWorker();
        this.compileRunWorker = new CompileRunWorker();
    }

    public void setSelectedTab(Tab tab) {
        this.selectedTab = tab;
    }

    /**
     * Helper method for assembling MIPS files.
     */
    private boolean assembleMipsFile(File file) {
        try {
            Platform.runLater(() -> {
                this.console.clear();
                this.consoleLength = 0;
            });


            ArrayList<String> commandInput = new ArrayList<>() {{add("java"); add("-jar");
                                                                   add("mars.jar"); add("a"); }};
            String filePath = file.getPath();

            commandInput.add(filePath);

            ProcessBuilder pb = new ProcessBuilder(commandInput);
            // Delete the filename from the full path string
            {
                int nameLength = this.selectedTab.getText().length();
                pb.directory(new File(filePath.substring(0,
                        filePath.length() - nameLength)));
            }

            this.curProcess = pb.start();

            this.outputToConsole();

            // true if compiled without compile-time error, else false
            return this.curProcess.waitFor() == 0;

        } catch (Throwable e) {
            Platform.runLater(() -> {
                createErrorDialog("Assembly Failed", e.getMessage());
            });
            return false;
        }
    }

    /**
     * Helper method for running MIPS Program.
     */
    private boolean runMipsFile(File file) {
        try {
            Platform.runLater(() -> {
                this.console.clear();
                consoleLength = 0;
            });

            ArrayList<String> commandInput = new ArrayList<>() {{add("java"); add("-jar");
                add("mars.jar");}};

            String filePath = file.getPath();

            commandInput.add(filePath);

            ProcessBuilder pb = new ProcessBuilder(commandInput);
            pb.directory(file.getParentFile());
            this.curProcess = pb.start();

            // Start output and input in different threads to avoid deadlock
            this.outThread = new Thread() {
                public void run() {
                    try {
                        // start output thread first
                        mutex.acquire();
                        outputToConsole();

                    } catch (Throwable e) {
                        Platform.runLater(() -> {
                            // print stop message if other thread hasn't
                            if (consoleLength == console.getLength()) {
                                console.appendText("\nProgram exited unexpectedly\n");
                                console.requestFollowCaret();
                            }
                        });
                    }
                }
            };
            outThread.start();

            inThread = new Thread() {
                public void run() {
                    try {
                        inputFromConsole();
                    } catch (Throwable e) {
                        Platform.runLater(() -> {
                            // print stop message if other thread hasn't
                            if (consoleLength == console.getLength()) {
                                console.appendText("\nProgram exited unexpectedly\n");
                                console.requestFollowCaret();
                            }
                        });
                    }
                }
            };
            inThread.start();

            // true if ran without error, else false
            return curProcess.waitFor() == 0;
        } catch (Throwable e) {
            Platform.runLater(() -> {
                createErrorDialog("Assembl & Run Failed", e.getMessage());
            });
            return false;
        }
    }

    /**
     * Helper method for getting program output
     */
    private void outputToConsole() throws java.io.IOException, java.lang.InterruptedException {
        InputStream stdout = this.curProcess.getInputStream();
        InputStream stderr = this.curProcess.getErrorStream();

        BufferedReader outputReader = new BufferedReader(new InputStreamReader(stdout));
        printOutput(outputReader);

        BufferedReader errorReader = new BufferedReader(new InputStreamReader(stderr));
        printOutput(errorReader);
    }

    /**
     * Helper method for getting program input
     */
    public void inputFromConsole() throws java.io.IOException, java.lang.InterruptedException {
        OutputStream stdin = curProcess.getOutputStream();
        BufferedWriter inputWriter = new BufferedWriter(new OutputStreamWriter(stdin));

        while (curProcess.isAlive()) {
            System.out.println("ALIVE");
            // wait until signaled by output thread
            this.mutex.acquire();
            // write input to program
            writeInput(inputWriter);
            // signal output thread
            this.mutex.release();
            // wait for output to acquire mutex
            Thread.sleep(1);
        }
        inputWriter.close();
    }

    /**
     * Helper method for printing to console
     *
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    private void printOutput(BufferedReader reader) throws java.io.IOException, java.lang.InterruptedException {
        // if the output stream is paused, signal the input thread
        if (!reader.ready()) {
            this.mutex.release();
        }

        int intch;
        // read in program output one character at a time
        while ((intch = reader.read()) != -1) {
            this.mutex.tryAcquire();
            char ch = (char) intch;
            String out = Character.toString(ch);
            Platform.runLater(() -> {
                // add output to console
                this.console.appendText(out);
                this.console.requestFollowCaret();
            });
            // update console length tracker to include output character
            this.consoleLength++;

            // if the output stream is paused, signal the input thread
            if (!reader.ready()) {
                this.mutex.release();
            }
            // wait for input thread to acquire mutex if necessary
            Thread.sleep(1);
        }
        this.mutex.release();
        reader.close();
    }

    /**
     * Helper function to write user input
     */
    public void writeInput(BufferedWriter writer) throws java.io.IOException {

        // wait for user to input line of text
        while (true) {
            if (this.console.getLength() > this.consoleLength) {
                // check if user has hit enter
                if (this.console.userPressedEnter()) {
                    break;
                }
            }
        }
        String userEntry = this.console.getText().substring(this.consoleLength);

        // write user-entered text to program input
        writer.write(userEntry);
        writer.flush();
        // update console length to include user input
        this.consoleLength = this.console.getLength();
    }

    /**
     * Handles the Compile button action.
     *
     * @param event Event object
     */
    public void handleAssembly(Event event) {
        // user select cancel button
        event.consume();
        File file = this.tabFileMap.get(this.selectedTab);
        if (file != null) {
            this.compileWorker.setFile(file);
            this.compileWorker.restart();
        }
    }

    /**
     * Handles the CompileRun button action.
     *
     * @param event Event object
     */
    public void handleRunMips(Event event) {
        // user select cancel button
        event.consume();
        File file = this.tabFileMap.get(this.selectedTab);
        if (file != null) {
            compileRunWorker.setFile(file);
            compileRunWorker.restart();
        }
    }

    /**
     * Handles the Stop button action.
     */
    public void handleStopAssembly() {
        try {
            if (this.curProcess.isAlive()) {
                this.inThread.interrupt();
                this.outThread.interrupt();
                this.curProcess.destroy();
            }
        } catch (Throwable e) {
            createErrorDialog("Assembly Stop Failed", e.getMessage());
        }
    }

    /**
     * A CompileWorker subclass handling Java program compiling in a separated thread in the background.
     * CompileWorker extends the javafx Service class.
     */
    protected class CompileWorker extends Service<Boolean> {
        /**
         * the file to be compiled.
         */
        private File file;

        /**
         * Sets the selected file.
         *
         * @param file the file to be compiled.
         */
        private void setFile(File file) {
            this.file = file;
        }

        /**
         * Overrides the createTask method in Service class.
         * Compiles the file embedded in the selected tab, if appropriate.
         *
         * @return true if the program compiles successfully;
         * false otherwise.
         */
        @Override
        protected Task<Boolean> createTask() {
            return new Task<Boolean>() {
                /**
                 * Called when we execute the start() method of a CompileRunWorker object
                 * Compiles the file.
                 *
                 * @return true if the program compiles successfully;
                 *         false otherwise.
                 */
                @Override
                protected Boolean call() {
                    Boolean compileResult = assembleMipsFile(file);
                    if (compileResult) {
                        Platform.runLater(() -> console.appendText("Compilation was successful!\n"));
                    }
                    return compileResult;
                }
            };
        }
    }

    /**
     * A CompileRunWorker subclass handling Java program compiling and running in a separated thread in the background.
     * CompileWorker extends the javafx Service class.
     */
    protected class CompileRunWorker extends Service<Boolean> {
        /**
         * the file to be compiled.
         */
        private File file;

        /**
         * Sets the selected file.
         *
         * @param file the file to be compiled.
         */
        private void setFile(File file) {
            this.file = file;
        }

        /**
         * Overrides the createTask method in Service class.
         * Compiles and runs the file embedded in the selected tab, if appropriate.
         *
         * @return true if the program runs successfully;
         * false otherwise.
         */
        @Override
        protected Task<Boolean> createTask() {
            return new Task<Boolean>() {
                /**
                 * Called when we execute the start() method of a CompileRunWorker object.
                 * Compiles the file and runs it if compiles successfully.
                 *
                 * @return true if the program runs successfully;
                 *         false otherwise.
                 */
                @Override
                protected Boolean call() {
                    if (assembleMipsFile(file)) {
                        return runMipsFile(file);
                    }
                    return false;
                }
            };
        }
    }

    public void createErrorDialog(String errorTitle, String errorString)
    {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(errorTitle + " Error");
        alert.setHeaderText(errorTitle + ", see details:");
        alert.setContentText(errorString);
        alert.showAndWait();
    }
}