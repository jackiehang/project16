/*
 * File: EditController.java
 * Names: Kevin Ahn, Matt Jones, Jackie Hang, Kevin Zhou
 * Class: CS 361
 * Project 4
 * Date: October 2, 2018
 * ---------------------------
 * Names: Zena Abulhab, Paige Hanssen, Kyle Slager Kevin Zhou
 * Project 5
 * Date: October 12, 2018
 * ---------------------------
 * Edited By: Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 * Project 6/7/9
 * Date: October 26, 2018/ November 3, 2018/ November 20, 2018
 */

package proj16DeGrawHangMarcello;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.fxmisc.richtext.CodeArea;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;

/**
 * This is the controller class for all of the edit functions
 * within the edit menu.
 *
 * @author  Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 * @author  Kevin Ahn, Jackie Hang, Matt Jones, Kevin Zhou
 * @author  Zena Abulhab, Paige Hanssen, Kyle Slager Kevin Zhou
 * @version 3.0
 * @since   10-3-2018
 */
public class EditController {

    // Reference to the tab pane of the IDE
    private CodeTabPane codeTabPane;

    /**
     * Constructor for the class. Initializes
     * the current tab to null
     */
    public EditController(CodeTabPane codeTabPane) {
        this.codeTabPane = codeTabPane;
    }

    /**
     * Handler for the "Undo" menu item in the "Edit" menu.
     */
    public void handleUndo() {
        CodeArea codeArea = this.codeTabPane.getCodeArea();
        if (codeArea != null) {
            codeArea.undo();
        }
    }

    /**
     * Handler for the "Redo" menu item in the "Edit" menu.
     */
    public void handleRedo() {
        CodeArea codeArea = this.codeTabPane.getCodeArea();
        if (codeArea != null) {
            codeArea.redo();
        }
    }

    /**
     * Handler for the "Cut" menu item in the "Edit" menu.
     */
    public void handleCut() {
        CodeArea codeArea = this.codeTabPane.getCodeArea();
        if (codeArea != null) {
            codeArea.cut();
        }
    }


    /**
     * Handler for the "Copy" menu item in the "Edit" menu.
     */
    public void handleCopy() {
        CodeArea codeArea = this.codeTabPane.getCodeArea();
        if (codeArea != null) {
            codeArea.copy();
        }
    }


    /**
     * Handler for the "Paste" menu item in the "Edit" menu.
     */
    public void handlePaste() {
        CodeArea codeArea = this.codeTabPane.getCodeArea();
        if (codeArea != null) {
            codeArea.paste();
        }
    }


    /**
     * Handler for the "SelectAll" menu item in the "Edit" menu.
     */
    public void handleSelectAll() {
        CodeArea codeArea = this.codeTabPane.getCodeArea();
        if (codeArea != null) {
            codeArea.selectAll();
        }
    }

    /**
     * Each section of four spaces in the highlighted text become a tab
     * or each tab in the highlighted text becomes four spaces
     * @param userSelection entab or detab
     */
    public void handleEntabOrDetab(String userSelection){
        CodeArea curCodeArea = this.codeTabPane.getCodeArea();
        String selectedText = curCodeArea.getSelectedText();
        String modified;

        if(userSelection.equals("entab")){
            modified = selectedText.replace("        ", "\t");
        }
        else {
            modified = selectedText.replace("\t", "        ");
            //There appears to be a bug in replaceSelection, and this line has fixed it
            curCodeArea.replaceSelection("");
        }

        curCodeArea.replaceSelection(modified);
        //restyle Code Area
        curCodeArea.setStyleSpans(0, JavaStyle.computeHighlighting(curCodeArea.getText()));
    }

    /**
     * Handler for the "Find & Replace" menu item in the "Edit" menu.
     */
    public void handleFindReplace() {
        if (this.codeTabPane.getCodeArea() != null) {
            new findReplace();
        }
    }

    /**
     * if a single "{", "}", "[", "]", "(", ")" is highlighted, this will attempt to find
     * the matching opening or closing character and if successful, will highlight the
     * text in between the matching set of {}, [], or (),
     * otherwise will display an appropriate error message
     */
    public void handleMatchBracketOrParen() {

        // get in-focus code area
        CodeArea curCodeArea = this.codeTabPane.getCodeArea();

        // get any highlighted text in the code area
        String highlightedText = curCodeArea.getSelectedText().trim();

        if (highlightedText.isEmpty() || highlightedText.length()!=1) {
            this.showAlert("Please Highlight a Bracket or Parentheses!");
            return;
        }
        // true if matching a closing character to an opening character,
        // false if matching an opening character to a closing character
        boolean findClosingCharacter;

        //The matching parenthesis/bracket to find
        String correspondingSymbol;

        switch (highlightedText) {
            case ("{"):
                correspondingSymbol = "}";
                findClosingCharacter = true;
                break;

            case ("["):
                correspondingSymbol = "]";
                findClosingCharacter = true;
                break;

            case ("("):
                correspondingSymbol = ")";
                findClosingCharacter = true;
                break;

            case ("}"):
                correspondingSymbol = "{";
                findClosingCharacter = false;
                break;

            case ("]"):
                correspondingSymbol = "[";
                findClosingCharacter = false;
                break;

            case (")"):
                correspondingSymbol = "(";
                findClosingCharacter = false;
                break;

            default:
                this.showAlert("INVALID CHARACTER HIGHLIGHTED\n" +
                        "VALID CHARACTERS ARE '{', '}', '[', ']', '(' or ')'");
                return;
        }

        Collection highlightedCharStyleClass = curCodeArea.getStyleOfChar(curCodeArea.getSelection().getStart());
        if(!(highlightedCharStyleClass.contains("bracket")|| highlightedCharStyleClass.contains("brace")
                ||highlightedCharStyleClass .contains("paren"))){
            this.showAlert("CANNOT MATCH BRACKET/BRACE/PAREnTHESES IN COMMENT,STRING OR CHAR");
            return;
        }

        // this stack holds only opening "[","(","{" or closing "]",")","}" characters
        // depending which type was initially highlighted to match against
        // start with initial highlighted bracket/parenthesis/brace on the stack
        Stack<String> charStack = new Stack<>();
        charStack.push(highlightedText);

        //The end index to stop searching
        int endIndex;

        //The index currently searching
        int curIndex;

        //The index to find in next iteration
        int nextIndex;

        //Index of the highlighted text in the codeArea
        int highlightedIndex;

        //Character currently checking
        String curChar;

        if (findClosingCharacter){
            endIndex = curCodeArea.getLength();
            highlightedIndex = curCodeArea.getSelection().getEnd();
        }
        else{
            endIndex = 0;
            highlightedIndex = curCodeArea.getSelection().getStart();
        }

        curIndex = highlightedIndex;

        //Loop through all the characters before a closing or after an opening symbol
        while (curIndex != endIndex){

            if(findClosingCharacter){
                nextIndex = curIndex + 1;
                curChar = curCodeArea.getText(curIndex, curIndex+1);
            }
            else{
                nextIndex = curIndex - 1;
                curChar = curCodeArea.getText(curIndex -1 ,curIndex);
            }

            //Check if the character being checked is a bracket, brace or parentheses by checking their styleClass.
            //Excludes any of them in comments or string or character
            Collection curCharStyleClass = curCodeArea.getStyleOfChar(curIndex);
            if(!(curCharStyleClass.contains("bracket")|| curCharStyleClass.contains("brace")
                    ||curCharStyleClass.contains("paren"))){
                curIndex = nextIndex;
                continue;
            }

            //pop the top opening symbol off the stack if its closing match is found,
            //otherwise push the newly found opening symbol onto the stack
            if(curChar.equals(correspondingSymbol)){
                charStack.pop();
            }
            else if(curChar.equals(highlightedText)){
                charStack.push(curChar);
            }

            // stack is empty if the originally highlighted character has been
            /// matched with the current character
            if (charStack.isEmpty()) {
                // highlight between matching characters ({}, () or [])
                curCodeArea.selectRange(highlightedIndex, curIndex);
                return;
            }

            curIndex = nextIndex;
        }
        this.showAlert("MATCHING CLOSING CHARACTER NOT FOUND");
    }

    /**
     * creates and displays an informational alert
     *
     * @param header the content of the alert
     */
    private void showAlert(String header) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(header);
        a.show();
    }


    /**
     * handler for Line Comments. allows the user to toggle on or off line comments for multiple lines
     */
    public void handleLineComment() {
        //get codeArea, text, selectedText, and Selected Range
        CodeArea codeArea = this.codeTabPane.getCodeArea();
        String codeAreaText = codeArea.getText();
        IndexRange selectedRange = codeArea.getSelection();
        String selectedText = codeArea.getSelectedText();

        if (selectedText.isEmpty()) {
            return;
        }

        //checks whether the first line is commented or not
        //if it is, then we want to uncomment all selected lines
        boolean firstLineCommented;
        String beforeComment = codeAreaText.substring(0, selectedRange.getStart());
        int lastNewLineBeforeComment = beforeComment.lastIndexOf("\n");

        if (lastNewLineBeforeComment != -1 && !beforeComment.endsWith("\n")) {
            if (beforeComment.endsWith("/") && selectedText.startsWith("/")) {
                firstLineCommented = true;
            } else if (beforeComment.endsWith("\n ") && selectedText.startsWith(" ")) {
                firstLineCommented = false;
            } else {
                firstLineCommented = beforeComment.substring(lastNewLineBeforeComment + 1, lastNewLineBeforeComment + 3).equals("//");
            }

        } else if (beforeComment.endsWith("\n")) {
            firstLineCommented = selectedText.startsWith("//");
        } else {
            firstLineCommented = beforeComment.startsWith("//");
        }

        String beforeCurrentLine;
        String afterComment;
        String currentLine;
        String newContent;
        String partialCurrentLine;

        //finds all new lines in the selected area
        ArrayList<Integer> newLineIndices = this.findNewLinesInString(selectedText);
        if (!newLineIndices.isEmpty()) {
            selectedText = this.commentMultipleLines(newLineIndices, selectedText);
        }

        //finds text after selected area
        if (selectedRange.getEnd() == codeAreaText.length() - 1) {
            afterComment = "";
        } else {
            afterComment = codeAreaText.substring(selectedRange.getEnd());
        }

        //checks for case where you highlight in between the comment characters
        if (beforeComment.endsWith("/") && selectedText.startsWith("/")) {
            beforeComment = beforeComment.substring(0, beforeComment.length() - 2);
            selectedText = selectedText.substring(1);
            if (!newLineIndices.isEmpty()) {
                for (int i = 0; i < newLineIndices.size(); i++) {
                    newLineIndices.set(i, newLineIndices.get(i) - 1);
                }
            }
            selectedRange = new IndexRange(selectedRange.getStart() - 1, selectedRange.getEnd() - 1);
        }

        //finds all text before the first selected line
        if (selectedRange.getStart() == 0 || lastNewLineBeforeComment == -1) {
            beforeCurrentLine = "";
        } else {
            if (lastNewLineBeforeComment == beforeComment.length()) {
                beforeCurrentLine = beforeComment;
            } else {
                beforeCurrentLine = beforeComment.substring(0, lastNewLineBeforeComment + 1);
            }
        }

        //either comment lines or uncomment lines
        if (firstLineCommented) {
            //uncomment
            selectedText = this.removeComments(selectedText, newLineIndices);

            partialCurrentLine = codeAreaText.substring(beforeCurrentLine.length(), selectedRange.getStart());
            if (partialCurrentLine.startsWith("//")) {
                partialCurrentLine = partialCurrentLine.substring(2);
            }
            currentLine = partialCurrentLine + selectedText;
        } else {
            //comment
            if (!newLineIndices.isEmpty()) {
                selectedText = this.commentMultipleLines(newLineIndices, selectedText);
            }
            partialCurrentLine = codeAreaText.substring(lastNewLineBeforeComment + 1, selectedRange.getStart());
            currentLine = "//" + partialCurrentLine + selectedText;
        }

        newContent = beforeCurrentLine + currentLine + afterComment;
        codeArea.replaceText(newContent);

        //restyle Code Area
        codeArea.setStyleSpans(0, JavaStyle.computeHighlighting(codeArea.getText()));
        codeArea.moveTo(beforeComment.length() + currentLine.length());
    }



    /**
     * Using newLineIndices, if there is a comment after a new line, removes the comment
     *
     * @param text text to remove comments from
     * @param newLineIndices ArrayList of indices of new line characters in text
     * @return new String text with all comments removed after the new lines.
     */
    private String removeComments(String text, ArrayList<Integer> newLineIndices) {
        if (text.startsWith("//")) {
            text = text.substring(2);

            //decrements the new line indices by 2 if we remove 2 characters
            if (!newLineIndices.isEmpty()) {
                for (int i = 0; i < newLineIndices.size(); i++) {
                    newLineIndices.set(i, newLineIndices.get(i) - 2);
                }
            }
        }

        if (newLineIndices.isEmpty()) {
            return text;
        }

        //iterates through the newline indices and removes all the comments after the new lines
        for (int i = 0; i < newLineIndices.size(); i++) {
            String nextLine = text.substring(newLineIndices.get(i));
            if (nextLine.length() == 0) {
                continue;
            }
            if (nextLine.startsWith("//")) {
                String beforeComment = text.substring(0, newLineIndices.get(i));
                String afterComment = nextLine.substring(2);
                text = beforeComment + afterComment;
                for (int j = i; j < newLineIndices.size(); j++) {
                    newLineIndices.set(j, newLineIndices.get(j) - 2);
                }
            }
        }
        return text;
    }

    /**
     * Finds all of the new lines in a String and returns them in an array list.
     *
     * @param text string to find all new lines in
     * @return ArrayList of indices of the new lines in text
     */
    private ArrayList<Integer> findNewLinesInString(String text) {
        ArrayList<Integer> newLineIndices = new ArrayList<Integer>();

        int firstIndex = text.indexOf("\n");
        if (firstIndex == -1) {
            return newLineIndices;
        }
        newLineIndices.add(firstIndex);

        int idx = firstIndex + 1;
        //loops through the text and searches for all new line characters
        while (idx != -1) {
            int nextIndex = text.indexOf("\n", idx);
            if (nextIndex == -1) {
                break;
            }
            newLineIndices.add(nextIndex);
            idx = nextIndex + 1;
        }
        for (int i = 0; i < newLineIndices.size(); i++) {
            newLineIndices.set(i, newLineIndices.get(i) + 1);
        }
        return newLineIndices;
    }

    /**
     * Using the locations of the new line characters in the text, inserts comments after the new line characters
     *
     * @param newLineIndices ArrayList of all new line characters in text
     * @param text           string to add comments to
     * @return text with comments inserted after new line characters
     */
    private String commentMultipleLines(ArrayList<Integer> newLineIndices, String text) {
        String beforeComment;
        String afterComment;

        //loops through the new line indices and adds a comment character after.
        for (int i = 0; i < newLineIndices.size(); i++) {
            beforeComment = text.substring(0, newLineIndices.get(i));
            afterComment = text.substring(newLineIndices.get(i));

            //makes sure there isn't a comment character already there
            if (afterComment.startsWith("//")) {
                afterComment = afterComment.substring(2);
            } else {
                if (newLineIndices.size() > 1) {
                    for (int j = i + 1; j < newLineIndices.size(); j++) {
                        newLineIndices.set(j, newLineIndices.get(j) + 2);
                    }
                }

            }
            text = beforeComment + "//" + afterComment;
        }
        return text;
    }

    /**
     * handler for Block Comments. allows user to toggle a block comment on or off.
     */
    public void handleBlockComment() {
        CodeArea codeArea = this.codeTabPane.getCodeArea();
        String codeAreaText = codeArea.getText();
        IndexRange selectedRange = codeArea.getSelection();
        String selectedText = codeArea.getSelectedText();
        if (selectedText.isEmpty()) {
            return;
        }

        //finds the substrings before and after the comment
        //insert the block comments and then recreates the codeAreaText in newContent
        String beforeComment;
        String afterComment;
        if (selectedRange.getStart() == 0) {
            beforeComment = "";
        } else {
            beforeComment = codeAreaText.substring(0, selectedRange.getStart());
        }
        if (selectedRange.getEnd() == codeAreaText.length() - 1) {
            afterComment = "";
        } else {
            afterComment = codeAreaText.substring(selectedRange.getEnd());
        }
        int openComment = beforeComment.lastIndexOf("/*");
        int endComment = beforeComment.lastIndexOf("*/");
        if (openComment > endComment) {
            return;
        }

        String newContent;
        if (selectedText.startsWith("/*") && selectedText.endsWith("*/")) {
            selectedText = selectedText.substring(2, selectedText.length() - 2);
            newContent = beforeComment + selectedText + afterComment;
            codeArea.replaceText(newContent);

            //move the caret to the end of the comment
            codeArea.moveTo(selectedRange.getEnd() - 4);
        } else {
            newContent = beforeComment + "/*" + selectedText + "*/" + afterComment;
            codeArea.replaceText(newContent);

            //move the caret to the end of the comment
            codeArea.moveTo(selectedRange.getEnd() + 4);
        }

        //restyle Code Area
        codeArea.setStyleSpans(0, JavaStyle.computeHighlighting(codeArea.getText()));
    }

    /**
     * A inner class that handles find and replace functionality
     */
    private class findReplace {
        private ArrayList<Integer> occurrenceIndices;
        // The target string to be found in handleFind
        private String targetText;
        // The index of the occurrence currently looked at.
        private int curOccurrenceIndex;
        // The find and replace dialog
        private Dialog<ButtonType> findReplaceDialog;
        private Button replaceButton;
        private Button replaceAllButton;

        /**
         * Creates the Find and Replace Dialog
         */
        public findReplace() {
            this.occurrenceIndices = new ArrayList<>();
            this.targetText = "";
            this.findReplaceDialog = new Dialog<>();
            this.findReplaceDialog.setTitle("Find & Replace");

            ButtonType find = new ButtonType("Find Next");
            ButtonType replace = new ButtonType("Replace");
            ButtonType replaceAll = new ButtonType("Replace All");
            findReplaceDialog.getDialogPane().getButtonTypes().addAll(find, replace, replaceAll, ButtonType.CANCEL);

            GridPane gridPane = new GridPane();
            gridPane.setHgap(10);
            gridPane.setVgap(10);
            gridPane.setPadding(new Insets(20, 150, 10, 10));

            TextField findField = new TextField();
            TextField replaceField = new TextField();

            gridPane.add(new Label("Find:"), 0, 0);
            gridPane.add(findField, 0, 1);
            gridPane.add(new Label("Replace With:"), 0, 2);
            gridPane.add(replaceField, 0, 3);
            findReplaceDialog.getDialogPane().setContent(gridPane);


            final Button findButton = (Button) findReplaceDialog.getDialogPane().lookupButton(find);
            findButton.addEventFilter(ActionEvent.ACTION, event -> {
                this.handleFind(findField.getText());
                event.consume();
            });

            this.replaceButton = (Button) findReplaceDialog.getDialogPane().lookupButton(replace);
            replaceButton.addEventFilter(ActionEvent.ACTION, event -> {
                this.handleReplace(replaceField.getText());
                event.consume();
            });

            this.replaceAllButton = (Button) findReplaceDialog.getDialogPane().lookupButton(replaceAll);
            replaceAllButton.addEventFilter(ActionEvent.ACTION, event -> {
                this.handleReplaceAll(replaceField.getText());
                event.consume();
            });

            this.setEnableReplaceReplaceAll(false);
            this.findReplaceDialog.showAndWait();

        }

        /**
         * searches through the current CodeArea for all instances of findText and populates this.findIndices.
         * Allows the User to scroll through all of the find results if the findText does not change
         * between button clicks.
         *
         * @param findText text to find in the CodeArea
         */
        public void handleFind(String findText) {

            CodeArea codeArea = EditController.this.codeTabPane.getCodeArea();
            String codeAreaText = codeArea.getText();

            //Check if this is the first time to find the given text
            //Or if an empty text is given
            if (!findText.equals(this.targetText)) {
                this.targetText = findText;
                this.occurrenceIndices.clear();
                this.curOccurrenceIndex = 0;

                int nextIndex = -1;
                while (true) {
                    nextIndex = codeAreaText.indexOf(findText, nextIndex+1);
                    if(nextIndex == -1){
                        break;
                    }
                    this.occurrenceIndices.add(nextIndex);
                }
                if (!this.occurrenceIndices.isEmpty()){
                    int firstIdx = this.occurrenceIndices.get(0);
                    codeArea.selectRange(firstIdx,  firstIdx + findText.length());
                    this.setEnableReplaceReplaceAll(true);
                }
            }

            //If the second or more time to find the given text
            else {
                //Check if there are any occurrence.
                if (this.occurrenceIndices.isEmpty()) {
                    this.setEnableReplaceReplaceAll(false);
                    return;
                }

                //Increment the current occurrence looking at.
                this.curOccurrenceIndex += 1;
                if (this.curOccurrenceIndex >= this.occurrenceIndices.size()) {
                    this.curOccurrenceIndex = 0;
                }
                int textIdx = this.occurrenceIndices.get(this.curOccurrenceIndex);
                //Select the text found
                codeArea.selectRange(textIdx, textIdx + findText.length());
            }
        }

        /**
         * Uses the currently selected instance of findText and replaces it with replaceText
         *
         * @param replaceText text to replace the current findText
         */
        private void handleReplace(String replaceText) {
            //Check if there are any occurrence of the text to be found
            if (this.occurrenceIndices == null || this.occurrenceIndices.isEmpty()) {
                return;
            }
            CodeArea codeArea = EditController.this.codeTabPane.getCodeArea();
            String codeAreaText = codeArea.getText();

            String newContent;
            String beforeFind;
            String afterFind;

            //Find the text indices of the text to replace
            int startReplace = this.occurrenceIndices.get(this.curOccurrenceIndex);
            int endReplace = startReplace + this.targetText.length();

            //Find the substring before and after the text to be replaced
            if (startReplace != 0) {
                beforeFind = codeAreaText.substring(0, startReplace);
                afterFind = codeAreaText.substring(endReplace);
            } else {
                beforeFind = "";
                afterFind = codeAreaText.substring(endReplace);
            }

            //Combine the substrings and the replaceText to get the new content in the codeArea
            newContent = beforeFind + replaceText + afterFind;

            //Update the index of the occurrences
            int lengthDiff = replaceText.length() - this.targetText.length();
            for (int i = this.curOccurrenceIndex; i < this.occurrenceIndices.size(); i++) {
                this.occurrenceIndices.set(i, this.occurrenceIndices.get(i) + lengthDiff);
            }

            //Remove the index of the occurrence that was replaced
            int removeIdx = this.curOccurrenceIndex;
            this.occurrenceIndices.remove(removeIdx);

            //Update the index of the found text currently highlighted
            if (this.curOccurrenceIndex == this.occurrenceIndices.size()) {
                this.curOccurrenceIndex = 0;
            }

            codeArea.replaceText(newContent);

            //High light the next found text if there are any
            if (!this.occurrenceIndices.isEmpty()) {
                int currentIdx = this.occurrenceIndices.get(curOccurrenceIndex);
                codeArea.selectRange(currentIdx, currentIdx + this.targetText.length());
            } else {
                //Disable replace if there are no more found text
                this.setEnableReplaceReplaceAll(false);
            }
        }

        /**
         * Replaces all the text that is this.textToFind with replaceText
         *
         * @param replaceText text to replace this.textToFind
         */
        private void handleReplaceAll(String replaceText) {
            //Check if there are any occurrence of the text to be found
            if (this.occurrenceIndices == null || this.occurrenceIndices.size() == 0) {
                return;
            }
            CodeArea codeArea = EditController.this.codeTabPane.getCodeArea();
            String codeAreaText = codeArea.getText();

            //Replace all the found text with new replace text
            String newContent = codeAreaText.replaceAll(this.targetText, replaceText);
            this.occurrenceIndices.clear(); //all have been replaced.
            this.curOccurrenceIndex = 0;
            this.targetText = null;

            codeArea.replaceText(newContent);
            this.setEnableReplaceReplaceAll(false);
        }

        /**
         * Enable or Disable the replace and replaceAll buttons in the find and replace dialog
         * @param enable Enable the buttons when true, disable the buttons when false
         */
        private void setEnableReplaceReplaceAll(boolean enable) {
            this.replaceButton.setDisable(!enable);
            this.replaceAllButton.setDisable(!enable);
        }
    }
}

