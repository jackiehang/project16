/*
 * File: MipsCodeArea.java
 * Names: Zena Abulhab, Paige Hanssen, Kyle Slager, Kevin Zhou
 * Project 15
 * Date: March 20, 2019
 *
 */

package proj15DeGrawHangMarcello;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

import java.time.Duration;
import java.util.*;
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
public class MipsCodeArea extends CodeArea{


    /**
     * This is the constructor of JavaCodeArea
     */
    public MipsCodeArea() {
        super();
        this.subscribeToSyntaxHighlighting();
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
                .subscribe(ignore -> this.setStyleSpans(0, MipsStyle.computeHighlighting(this.getText())));
    }


}

/**
 * source:  https://moodle.colby.edu/pluginfile.php/294745/mod_resource/content/0/JavaKeywordsDemo.java
 * @author  Matt Jones, Kevin Zhou, Kevin Ahn, Jackie Hang
 * @author  Zena Abulhab, Paige Hanssen, Kyle Slager Kevin Zhou
 * @version 3.0
 * @since   09-30-2018
 */
class MipsStyle {
    // a list of strings that contain the keywords for the IDE to identify.
    private static final String[] MIPS_KEYWORDS = new String[]{
            ".data",".asciiz"," .text",".globl",".global"
    };


    // a list of strings that contain the MIPS instructions for the IDE to identify.
    private static final String[] MIPS_INSTRUCTIONS = new String[]{
            "add", "sub","addi","addu","addiu","subu",
            "mfhi", "mflo","move", "b", "beq", "blt",
            "ble", "bgt", "bge","bne","j","jr","jal","jr",
            "and","andi","div","divu","mult","multu", "nor",
            "or","ori","sll","sllv","sra","srav","srl","srlv",
            "xor","xori","lhi","llo","slt","sltu","slti","sltiu",
            "bgtz","blez", "jalr","lb","lbu","lh","lhu","lw",
            "sb","sh","sw","mthi","mtlo","trap"
    };

    // the regex rules for the ide
    private static final String MIPS_REGISTER_PATTERN = "\\$[a-zA-Z0-9_]*";
    private static final String MIPS_INSTRUCTION_PATTERN = "\\b(" + String.join("|", MIPS_INSTRUCTIONS) + ")\\b";
    private static final String MIPS_KEYWORD_PATTERN = "\\b(" + String.join("|", MIPS_KEYWORDS) + ")\\b";
    private static final String MIPS_COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<MIPSKEYWORD>" + MIPS_KEYWORD_PATTERN + ")"
                    + "|(?<MIPSINSTRUCTION>" + MIPS_INSTRUCTION_PATTERN + ")"
                    + "|(?<MIPSCOMMENT>" + MIPS_COMMENT_PATTERN + ")"
                    + "|(?<MIPSREGISTER>" + MIPS_REGISTER_PATTERN + ")"


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
            String styleClass = matcher.group("MIPSKEYWORD") != null ? "mipsKeyword" :
                    matcher.group("MIPSINSTRUCTION") != null ? "mipsInstruction" :
                            matcher.group("MIPSCOMMENT") != null ? "mipsComment" :
                                    matcher.group("MIPSREGISTER") != null ? "mipsRegister" :
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