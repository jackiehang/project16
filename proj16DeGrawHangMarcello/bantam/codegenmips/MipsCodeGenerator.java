/* Bantam Java Compiler and Language Toolset.

   Copyright (C) 2009 by Marc Corliss (corliss@hws.edu) and 
                         David Furcy (furcyd@uwosh.edu) and
                         E Christopher Lewis (lewis@vmware.com).
   ALL RIGHTS RESERVED.

   The Bantam Java toolset is distributed under the following 
   conditions:

     You may make copies of the toolset for your own use and 
     modify those copies.

     All copies of the toolset must retain the author names and 
     copyright notice.

     You may not sell the toolset or distribute it in 
     conjunction with a commerical product or service without 
     the expressed written consent of the authors.

   THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS 
   OR IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE 
   IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
   PARTICULAR PURPOSE. 
*/

package proj16DeGrawHangMarcello.bantam.codegenmips;

import proj16DeGrawHangMarcello.bantam.util.ClassTreeNode;
import proj16DeGrawHangMarcello.bantam.util.CompilationException;
import proj16DeGrawHangMarcello.bantam.util.Error;
import proj16DeGrawHangMarcello.bantam.util.ErrorHandler;
import proj16DeGrawHangMarcello.bantam.visitor.Visitor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * The <tt>MipsCodeGenerator</tt> class generates mips assembly code
 * targeted for the SPIM or Mars emulators.
 * <p/>
 * This class is incomplete and will need to be implemented by the student.
 */
public class MipsCodeGenerator extends Visitor
{
    /**
     * Root of the class hierarchy tree
     */
    private ClassTreeNode root;

    /**
     * Print stream for output assembly file
     */
    private PrintStream out;

    /**
     * Assembly support object (using Mips assembly support)
     */
    private MipsSupport assemblySupport;

    /**
     * Boolean indicating whether garbage collection is enabled
     */
    private boolean gc = false;

    /**
     * Boolean indicating whether optimization is enabled
     */
    private boolean opt = false;

    /**
     * Boolean indicating whether debugging is enabled
     */
    private boolean debug = false;

    /**
     * for recording any errors that occur.
     */
    private ErrorHandler errorHandler;

    /**
     * maps identifier index to classname string
     */
    private Map<String, Integer> classnameTable = new HashMap();


    /**
     * MipsCodeGenerator constructor
     *
     * @param errorHandler ErrorHandler to record all errors that occur
     * @param gc      boolean indicating whether garbage collection is enabled
     * @param opt     boolean indicating whether optimization is enabled
     */
    public MipsCodeGenerator(ErrorHandler errorHandler, boolean gc, boolean opt) {
        this.gc = gc;
        this.opt = opt;
        this.errorHandler = errorHandler;
    }

    /**
     * Generate assembly file
     * <p/>
     * In particular, you will need to do the following:
     * 1 - start the data section
     * 2 - generate data for the garbage collector
     * 3 - generate string constants
     * 4 - generate class name table
     * 5 - generate object templates
     * 6 - generate dispatch tables
     * 7 - start the text section
     * 8 - generate initialization subroutines
     * 9 - generate user-defined methods
     * See the lab manual for the details of each of these steps.
     *
     * @param root    root of the class hierarchy tree
     * @param outFile filename of the assembly output file
     */
    public void generate(ClassTreeNode root, String outFile) {
        this.root = root;

        // set up the PrintStream for writing the assembly file.
        try {
            this.out = new PrintStream(new FileOutputStream(outFile));
            this.assemblySupport = new MipsSupport(out);
        } catch (IOException e) {
            // if don't have permission to write to file then throw an exception
            errorHandler.register(Error.Kind.CODEGEN_ERROR, "IOException when writing " +
                    "to file: " + outFile);
            throw new CompilationException("Could not write to output file.");
        }

        // comment out
//        throw new RuntimeException("MIPS code generator unimplemented");

        // add code here...

        this.assemblySupport = new MipsSupport(this.out);

        // begin generating data section
        this.assemblySupport.genDataStart();

        // generate garbage collecting flag section
        genGCSection(this.gc);

        generateStringConstants();

        generateClassTableNames();

        generateObjectTemplates();

        generateDispatchTables();

        generateTextSection();

        generateInItSubroutines();

        generateUserMethods();

    }

    /**
     * Generate the code for the gc_flag (garbage collection) section
     * @param collecting
     */
    private void genGCSection(boolean collecting) {
        int flag = collecting ? 1 : 0;
        out.println("gc_flag");
        out.println("\t.word:\t" + flag);
    }

    private void generateStringConstants() {

    }

    /**
     * generates the class_name_table
     */
    private void generateClassTableNames() {

        this.out.println("class_name_table");
        // get keys list
        Set<String> keys = this.classnameTable.keySet();
        // loop through length of keys to build field fields

         for (int i = 0; i < keys.size(); i++) {
             this.assemblySupport.genWord("class_name_"+i);
         }
        // loop through keys to build .globl lines
        for (String s : keys) {
            this.assemblySupport.genGlobal(s+"_template");
        }
    }

    private void generateObjectTemplates() {

    }
    private void generateDispatchTables() {

    }
    private void generateTextSection() {
    }

    private void generateInItSubroutines() {

    }
    private void generateUserMethods() {
    }





    public static void main(String[] args) {
        // ... add testing code here ...
    }
}
