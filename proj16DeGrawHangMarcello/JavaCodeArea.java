/*
 * File: JavaCodeArea.java
 * Names: Zena Abulhab, Paige Hanssen, Kyle Slager, Kevin Zhou
 * Project 5
 * Date: October 12, 2018
 * ---------------------------
 * Edited By: Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 * Project 6/7/9
 * Date: October 26, 2018/ November 3, 2018/ November 20, 2018
 */

package proj16DeGrawHangMarcello;

import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.Selection;
import org.fxmisc.richtext.SelectionImpl;
import org.fxmisc.richtext.event.MouseOverTextEvent;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import proj16DeGrawHangMarcello.bantam.util.Error;

/**
 * This class is the controller for all of the toolbar functionality.
 * Specifically the compile, compile and run, and stop buttons
 *
 * @author  Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 * @author  Kevin Ahn, Jackie Hang, Matt Jones, Kevin Zhou
 * @author  Zena Abulhab, Paige Hanssen, Kyle Slager Kevin Zhou
 * @version 2.0
 * @since   10-3-2018
 */
public class JavaCodeArea extends CodeArea{

    ArrayList<Selection> selections;
    HashMap<Integer, String> lineErrorMap;

    /**
     * This is the constructor of JavaCodeArea
     */
    public JavaCodeArea() {
        super();
        selections = new ArrayList<>();
        lineErrorMap = new HashMap<>();
        this.subscribeToSyntaxHighlighting();
        setupErrorTooltip();
    }


    /**
     * creates a Popup and its Label to be used for displaying errors on mouse hover
     */
    private void setupErrorTooltip() {

        // create a popup
        Popup popup = new Popup();

        // create a new popup label
        Label popupMsg = new Label();

        // set the popup's style
        popupMsg.setStyle(
                "-fx-background-color: red;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 5;");

        // add the add the label to the popup
        popup.getContent().add(popupMsg);

        // show error message after 1 second
        this.setMouseOverTextDelay(Duration.ofSeconds(1));

        // set the popup's message on mouse over
        this.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, (e) ->
            showTooltipErrorMsg(e, popup, popupMsg)
        );

        // hide the popup when moving off the line
        this.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_END, (e) ->
            popup.hide()
        );
    }

    /**
     * sets the error message corresponding to the line hovered over and shows the tooltip
     *
     * @param e the MouseOverTextEvent
     * @param popup the popup that will show
     * @param popupText the Label containing the text for the popup
     */
    private void showTooltipErrorMsg(MouseOverTextEvent e, Popup popup, Label popupText) {

        // get index of cursor position within text
        int charIdx = e.getCharacterIndex();
        Position pos = this.offsetToPosition(charIdx, null);

        // get line #, +1 because getMajor() returns line nums indexed from 0
        int lineNum = pos.getMajor()+1;

        // if the map has an error associated with this line #
        if (lineErrorMap.containsKey(lineNum)) {

            // get the mouse hover location
            Point2D showLocation = e.getScreenPosition();

            // get the error message from the map
            String msg = this.lineErrorMap.get(lineNum);

            // set the popup label's text
            popupText.setText(msg);

            // show the popup at the location of the mouse hover
            popup.show(this, showLocation.getX(), showLocation.getY() + 10);
        }
    }

    /**
     * clears data associated with previous error highlight and error popups
     */
    public void removePreviousSelections() {

        // empty previous error map
        lineErrorMap.clear();

        // loop through current selections
        for (Selection s : this.selections) {

            // unhighlight the line
            s.deselect();

            // remove selection from this code area
            this.removeSelection(s);
        }
        // empty the list of selections
        this.selections.clear();
    }

    /**
     * highlights erroneous lines and sets up the map of line #, error msg pairs
     *
     * @param errors a list of errors from Scanning, Parsing or Checking
     */
    private void addNewSelections(List<Error> errors) {

        // loop through errors
        for (Error e : errors) {

            // get error line #
            int lineNum = e.getLineNum();

            // create a new selection (highlighted section)
            Selection highlightedLine = new SelectionImpl("selection", this);

            // will highlight the line that the error is on, paragraph is a single line
            highlightedLine.selectParagraph(lineNum-1);

            // add to list of current selections
            selections.add(highlightedLine);

            // highlight this section in this code area
            this.addSelection(highlightedLine);

            // add the line #, error pair to the map
            lineErrorMap.put(lineNum, e.getMessage());
        }
    }

    /**
     * removes previous errors and adds the new ones
     *
     * @param errors a list of errors from Scanning, Parsing or Checking
     */
    public void setRealTimeErrors(List<Error> errors) {
        removePreviousSelections();
        addNewSelections(errors);
    }



    /**
     * Method obtained from the RichTextFX Keywords Demo. Method allows
     * for syntax highlighting after a delay of 500ms after typing has ended.
     * This method was copied from JavaKeyWordsDemo
     * Original Author: Jordan Martinez
     */
    private void subscribeToSyntaxHighlighting() {
        // recompute the syntax highlighting 500 ms after user stops editing area
        Subscription codeCheck = this

                // plain changes = ignore style changes that are emitted when syntax highlighting is reapplied
                // multi plain changes = save computation by not rerunning the code multiple times
                //   when making multiple changes (e.g. renaming a method at multiple parts in file)
                .multiPlainChanges()

                // do not emit an event until 500 ms have passed since the last emission of previous stream
                .successionEnds(Duration.ofMillis(500))

                // run the following code block when previous stream emits an event
                .subscribe(ignore -> this.setStyleSpans(0, JavaStyle.computeHighlighting(this.getText())));
    }


}

    /**
     * source:  https://moodle.colby.edu/pluginfile.php/294745/mod_resource/content/0/JavaKeywordsDemo.java
     * @author  Matt Jones, Kevin Zhou, Kevin Ahn, Jackie Hang
     * @author  Zena Abulhab, Paige Hanssen, Kyle Slager Kevin Zhou
     * @version 3.0
     * @since   09-30-2018
     */
    class JavaStyle {

        // a list of strings that contain the keywords for the IDE to identify.
        private static final String[] KEYWORDS = new String[]{
                "abstract", "assert", "boolean", "break", "byte",
                "case", "catch", "char", "class", "const",
                "continue", "default", "do", "double", "else",
                "enum", "extends", "final", "finally", "float",
                "for", "goto", "if", "implements", "import",
                "instanceof", "int", "interface", "long", "native",
                "new", "package", "private", "protected", "public",
                "return", "short", "static", "strictfp", "super",
                "switch", "synchronized", "this", "throw", "throws",
                "transient", "try", "void", "volatile", "while", "var"
        };

        // the regex rules for the ide
        private static final String IDENTIFIER_PATTERN = "[a-zA-Z]+[a-zA-Z0-9_]*";
        private static final String FLOAT_PATTERN = "(\\d+\\.\\d+)";
        private static final String INTCONST_PATTERN = "\\d+";
        private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
        private static final String PAREN_PATTERN = "\\(|\\)";
        private static final String BRACE_PATTERN = "\\{|\\}";
        private static final String BRACKET_PATTERN = "\\[|\\]";
        private static final String SEMICOLON_PATTERN = "\\;";
        private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
        private static final String CHAR_PATTERN = "\"([^\'\\\\]|\\\\.)*\'";
        private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

        private static final Pattern PATTERN = Pattern.compile(
                "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                        + "|(?<PAREN>" + PAREN_PATTERN + ")"
                        + "|(?<BRACE>" + BRACE_PATTERN + ")"
                        + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                        + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                        + "|(?<STRING>" + STRING_PATTERN + ")"
                        + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                        + "|(?<FLOAT>" + FLOAT_PATTERN + ")"
                        + "|(?<INTCONST>" + INTCONST_PATTERN + ")"
                        + "|(?<IDENTIFIER>" + IDENTIFIER_PATTERN + ")"
                        + "|(?<CHARACTER>" + CHAR_PATTERN + ")"

        );

        /**
         * Method to highlight all of the regex rules and keywords.
         * Code obtained from the RichTextFX Demo from GitHub.
         *
         * @param text a string analyzed for proper syntax highlighting
         */
        public static StyleSpans<Collection<String>> computeHighlighting(String text) {
            Matcher matcher = PATTERN.matcher(text);
            int lastKwEnd = 0;
            StyleSpansBuilder<Collection<String>> spansBuilder
                    = new StyleSpansBuilder<>();
            while (matcher.find()) {
                String styleClass = matcher.group("KEYWORD") != null ? "keyword" :
                        matcher.group("PAREN") != null ? "paren" :
                                matcher.group("BRACE") != null ? "brace" :
                                        matcher.group("BRACKET") != null ? "bracket" :
                                                matcher.group("SEMICOLON") != null ? "semicolon" :
                                                        matcher.group("STRING") != null ? "string" :
                                                                matcher.group("COMMENT") != null ? "comment" :
                                                                        matcher.group("IDENTIFIER") != null ? "identifier" :
                                                                                matcher.group("INTCONST") != null ? "intconst" :
                                                                                        matcher.group("CHARACTER") != null ? "char" :
                                                                                        null; /* never happens */
                assert styleClass != null;
                spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
                spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
                lastKwEnd = matcher.end();
            }
            spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
            return spansBuilder.create();
        }
}

