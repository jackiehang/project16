/*
 * File: MipsCodeArea.java
 * Names: Lucas DeGraw, Jackie Hang, Chris Marcello
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
 * This class creates a MipsCodeArea that highlights
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
 * @author  Lucas DeGraw, Jackie Hang, Chris Marcello
 * @version 1.0
 * @since   03-21-19
 */
class MipsStyle {

    // a list of strings that contain the mips directives for the IDE to identify.
    private static final String[] MIPS_DIRECTIVES = new String[]{
            "align","ascii","asciiz","byte","data","double","end_macro",
            "eqv","extern","float","globl","half","include","kdata",
            "ktext","macro","set","space","text","word"
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
            "sb","sh","sw","mthi","mtlo","trap", "li", "syscall",
            "abs.d","abs.s","add.d","add.s","bc1f","bc1t","bgez",
            "bgezal","bgtz","blez","bltz","bltzal","break","c.eq.d",
            "c.eq.s","c.le.d","c.le.s","c.lt.d","c.lt.s","ceil.w.d",
            "ceil.w.s","clo","clz","cvt.d.s","cvt.d.w","cvt.s.d",
            "cvt.s.w","ctv.w.d","cvt.w.s","div.s","div.d","eret",
            "floor.w.d","floor.w.s","jalr","ldc1","ll","lui","lwc1",
            "lwl","lwr","madd","maddu","mfc0","mfc1","mfhi","mflo",
            "mov.d","mov.s","movf","movf.d","movf.s","movn","movn.d",
            "movn.s","movt","movt.d","movt.s","movz","movz.d","movz.s",
            "msub","msubu","mtc0","mtc1","mthi","mtlo","mul","mul.d",
            "mul.s","neg.d","neg.s","nop", "nor", "round.w.d","round.w.s",
            "sc","sqrt.d","sqrt.s","sub.d","sub.s","swr","teq","teqi","tge",
            "tgeiu","tgeu","tlt","tlti","tltiu","tltu","tne","tnei","trunc.w.d",
            "trunc.w.s","l.d","l.s","la","ld","ll","mulo","mulou","mulu","rem",
            "remu","rol","ror","s.d","s.s","sdc1","seq","sge","sgeu","sgt",
            "sgtu","sle","sleu","sne","swc1","swl","ulh","ulhu","ulw",
            "ush","usw"
    };

    // a list of strings that contain the mips register for the IDE to identify.
    private static final String[] MIPS_REGISTERS = new String[]{
            "zero","at","v0","v1","a0","a1","a2","a3","t0",
            "t1","t2","t3","t4","t5","t6","t7","s0","s1",
            "s2","s3","s4","s5","s6","s7","t8","t9","k0",
            "k1","gp","sp","s8","fp","ra","1","0","1","2",
            "3","4","5","6","7","8","9","10","11","12","13",
            "14","15","16","17","18","19","20","21","22","23",
            "24","25","26","27","28","29","30","31"
    };


    // the mips regex rules for the ide
    private static final String MIPS_DIRECTIVE_PATTERN = "\\.(" + String.join("|", MIPS_DIRECTIVES) + ")\\b";
    private static final String MIPS_INSTRUCTION_PATTERN = "\\b(" + String.join("|", MIPS_INSTRUCTIONS) + ")\\b";
    private static final String MIPS_COMMENT_PATTERN = "#[^\n]*";
    private static final String MIPS_REGISTER_PATTERN = "\\$(" + String.join("|", MIPS_REGISTERS) + ")\\b";
    private static final String MIPS_STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String MIPS_LABEL_PATTERN = "[a-zA-Z]*[a-zA-Z0-9]*:";


    private static final Pattern PATTERN = Pattern.compile(
            "(?<MIPSDIRECTIVE>" + MIPS_DIRECTIVE_PATTERN + ")"
                    + "|(?<MIPSINSTRUCTION>" + MIPS_INSTRUCTION_PATTERN + ")"
                    + "|(?<MIPSCOMMENT>" + MIPS_COMMENT_PATTERN + ")"
                    + "|(?<MIPSSTRING>" + MIPS_STRING_PATTERN + ")"
                    + "|(?<MIPSREGISTER>" + MIPS_REGISTER_PATTERN + ")"
                    + "|(?<MIPSLABEL>" + MIPS_LABEL_PATTERN + ")"

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
            String styleClass = matcher.group("MIPSDIRECTIVE") != null ? "mipsDirective" :
                    matcher.group("MIPSINSTRUCTION") != null ? "mipsInstruction" :
                            matcher.group("MIPSCOMMENT") != null ? "mipsComment" :
                                    matcher.group("MIPSSTRING") != null ? "mipsComment" :
                                        matcher.group("MIPSREGISTER") != null ? "mipsRegister" :
                                                matcher.group("MIPSLABEL") != null ? "mipsLabel" :

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