/*
 * File: Console.java
 * Names: Zena Abulhab, Paige Hanssen, Kyle Slager, Kevin Zhou
 * Project 5
 * Date: October 12, 2018
 *
 * ---------------------------
 * Edited By: Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 * Project 6/7/9
 * Date: October 26, 2018/ November 3, 2018/ November 20, 2018
 *
 * ---------------------------
 * Edited by: Lucas DeGraw
 * Project 15
 * Date: 3/21/19
 *
 */

package proj15DeGrawHangMarcello;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.richtext.StyleClassedTextArea;

/**
 * This class is used to support console functionality.
 * It can be used to write new lines of text to the console.
 * It can also be used to check whether user input been given,
 * and what the command string was.
 *
 * @author  Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 * @author  Zena Abulhab, Paige Hanssen, Kyle Slager Kevin Zhou
 * @version 1.0
 * @since   10-12-2018
 *
 */
public class Console extends StyleClassedTextArea {

    private String command;
    //The index of the first character of the command in the console text string
    private int commandStartIndex;
    private KeyCode lastKeyTyped;

    /**
     *  This is the constructor, setting up the console
     *  using StyleClassedTextArea as default.
     */
    public Console(){
        super();
        this.commandStartIndex = -1;
        this.command = "";
        this.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            this.handleKeyPressed(e);
        });
        this.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            this.handleKeyTyped(e);
        });
    }

    /**
     * Adds a new, separate line of text to this console.
     * Used in ToolbarController when printing to the console.
     * @param newString the string to add to the console
     */
    public void writeToConsole(String newString, String type) {

        int fromIndex = this.getText().length();
        this.appendText(newString);

        //Style the texts differently base on their source provided
        int toIndex = this.getText().length();
        switch (type) {
            case ("Output"):
                this.setStyleClass(fromIndex, toIndex, "output");
                break;
            case("Error"):
                this.setStyleClass(fromIndex, toIndex, "error");
                break;
            default:
                this.setStyleClass(fromIndex, toIndex, "processInfo");
        }

        this.moveCaretToEnd();
        this.setStyleClass(toIndex, toIndex, "default");
    }

    /**
     * @returns whether Enter was pressed between the last call to this method
     */
    public boolean userPressedEnter() {
        if (this.lastKeyTyped == KeyCode.ENTER) {
            // reset so that the in CompilationError.writeInput() stops after entering
            this.lastKeyTyped = null;
            return true;
        }
        return false;
    }

    /**
     * Consume all keyTyped event if it is before the commandstartindex
     * @param e the keyEvent
     */
     private void handleKeyTyped(KeyEvent e){
        if (this.getCaretPosition() < commandStartIndex) {
            e.consume();
        }
     }

    /**
     * Handles the keyPressed events in the console
     * Updates the content in the console and command stored in the field
     * The key press would not do anything if not pressed after the command start index
     * @param e the keyEvent
     */
     private void handleKeyPressed(KeyEvent e) {

         // save last key pressed
         this.lastKeyTyped = e.getCode();

        //If there is current command stored
        if (this.commandStartIndex != -1) {
            //Change the color of the user input text to default
            this.setStyleClass(commandStartIndex, this.getText().length(), ".default");
            this.command = this.getText().substring(commandStartIndex);
        }

        //If there are no command, update the start index of the command to the end of the current text
        else if (this.command.isEmpty()) {
            this.commandStartIndex = this.getText().length();
        }

        //If the user pressed Enter
        if (e.getCode() == KeyCode.ENTER) {
            e.consume();
            //If Enter was pressed in the middle of a command append a new line to the end
            if (this.getCaretPosition() >= commandStartIndex) {
                this.appendText("\n");
                this.requestFollowCaret();
            }
        }

        //If the user pressed back space.
        else if (e.getCode() == KeyCode.BACK_SPACE) {
            //If the keypress was before the start of the command, nothing would happen
            if (this.getCaretPosition() < commandStartIndex + 1) {
                e.consume();
            }
        }

        //If the user pressed delete key.
        else if (e.getCode() == KeyCode.DELETE) {
            //If the keypress was before the start of the command, nothing would happen
            if (this.getCaretPosition() < commandStartIndex){
                e.consume();
            }
        }
    }

    /**
     * Moves the caret to the end of the text and movee the scroll bar to the caret position
     */
    private void moveCaretToEnd(){
        int length = this.getText().length();
        this.moveTo(length);
        this.requestFollowCaret();
    }

}
