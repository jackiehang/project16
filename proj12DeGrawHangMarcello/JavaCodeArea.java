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

package proj12DeGrawHangMarcello;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /**
     * This is the constructor of JavaCodeArea
     */
    public JavaCodeArea(){
        super();
        this.subscribe();
    }

    /**
     * Method obtained from the RichTextFX Keywords Demo. Method allows
     * for syntax highlighting after a delay of 500ms after typing has ended.
     * This method was copied from JavaKeyWordsDemo
     * Original Author: Jordan Martinez
     */
    private void subscribe() {
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

