package proj15DeGrawHangMarcello;

/**
 * @author Lucas DeGraw
 *
 * superclass to ParseTask and CheckTask, otherwise would duplicate
 * this field and method in both
 *
 * 'Active' because these compilation phases could be happening actively as the user edits
 * the file, in which case the results are displayed inline instead of being written to the
 * console
 */
public class ActiveCompilationPhase {

    // denotes whether or not to write results of compilation phase to
    protected boolean writeToConsole = true;

    /**
     * sets this.writeToConsole value
     * @param write boolean denoting whether to result compilation results to console
     */
    public void setWriteToConsole(boolean write) {
        this.writeToConsole = write;
    }
}
