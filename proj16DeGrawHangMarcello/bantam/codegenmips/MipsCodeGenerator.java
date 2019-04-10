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

import proj16DeGrawHangMarcello.bantam.ast.Program;
import proj16DeGrawHangMarcello.bantam.parser.Parser;
import proj16DeGrawHangMarcello.bantam.semant.SemanticAnalyzer;
import proj16DeGrawHangMarcello.bantam.semant.StringConstantsVisitor;
import proj16DeGrawHangMarcello.bantam.util.ClassTreeNode;
import proj16DeGrawHangMarcello.bantam.util.CompilationException;
import proj16DeGrawHangMarcello.bantam.util.Error;
import proj16DeGrawHangMarcello.bantam.util.ErrorHandler;
import proj16DeGrawHangMarcello.bantam.visitor.Visitor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;


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
    private Map<String, Integer> classNameTable = new HashMap();


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

        saveClassTableNames();

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
        out.println("\t.word:\t" + flag + "\n");
    }

    /**
     * Generates the String Constants in the Data Section of the assembly file.
     * Each one is in the format:
     *
     */
    private void generateStringConstants() {
        String classNames[] = classNameTable.keySet().toArray(new String[0]);

        for (String className : classNames) {
            assemblySupport.genLabel("class_name_" + classNameTable.get(className)); //generates String Constant Label
            assemblySupport.genWord("1"); //String Template's Index
            assemblySupport.genWord(String.valueOf( 16 + (className.length()*2) )); //string length in bytes TODO - get actual math here
            assemblySupport.genWord("String_dispatch_table"); //link to string dispatch table
            assemblySupport.genWord(String.valueOf(className.length())); //length of the string in chars
            assemblySupport.genAscii(className); //string in ASCII
            out.print("\n");
        }

        //StringConstantsVisitor stringConstantsVisitor = new StringConstantsVisitor();

        //not sure how to visit here - we need the AST
        //Map<String,String> stringConstantsMap = stringConstantsVisitor.getStringConstants(root);

        /*
        for (Map.Entry<String,String> stringConstant : stringConstantsMap.entrySet()) {
            this.out.print("");
        }
    */
    }

    /**
     * Gets the class names from the classMap and matches them to appropriate indices.
     */
    private void saveClassTableNames() {
        //get class names as a set
        String[] classNames = root.getClassMap().keySet().toArray(new String[0]);
        //System.out.println(classNames.toString());
        int counter = 5;
        for(String cName: classNames){
            switch (cName){
                case "Object":
                    classNameTable.put("Object", 0);
                    break;
                case "String":
                    classNameTable.put("String",1);
                    break;

                case "Sys":
                    classNameTable.put("Sys",2);
                    break;

                case "Main":
                    classNameTable.put("Main",3);
                    break;

                case "TextIO":
                    classNameTable.put("TextIO",4);
                    break;

                    default:
                    classNameTable.put(cName,counter);
                    counter ++;
                    break;
                }
        }
    }

    /**
     * generates the class_name_table
     */
    private void generateClassTableNames() {
        this.out.println("class_name_table");
        // get keys list
        Set<String> keys = this.classNameTable.keySet();
        // loop through length of keys to build field fields

        // loop through keys to build the .word lines
        for (int i = 0; i < keys.size(); i++) {
            this.assemblySupport.genWord("class_name_"+i);
        }

        // loop through keys to build .globl lines
        for (String s : keys) {
            this.assemblySupport.genGlobal(s+"_template");
        }
    }


    /**
     * generates a template section for each object
     *
     */
    private void generateObjectTemplates() {

        Map classMap = this.root.getClassMap();
        Set<String> keys = this.classNameTable.keySet();

        // initialize vars that will change for each class in the loop
        ClassTreeNode curClassNode;
        int numVars;
        int numTables;
        int numFields;
        int classID;

        // foreach key of classNameTable
        for (String s : keys) {

            this.out.println("\n" + s + "_template:");    // write template classname

            curClassNode = (ClassTreeNode) classMap.get(s); // get the ClassTreeNode
            numVars = curClassNode.getVarSymbolTable().getSize(); // get # entries in symbol table(s)
            numTables = curClassNode.getVarSymbolTable().getCurrScopeLevel(); // get # symbol tables
            numFields = numVars - 2 * numTables; // # fields = # entires - 2 * # STs
            classID = this.classNameTable.get(s);

            // write first 3 lines of object template
            this.assemblySupport.genWord(String.valueOf(classID)); // write saved identifier
            this.assemblySupport.genWord(String.valueOf(12 + 4*numFields)); // write object size
            this.assemblySupport.genWord(s + "_dispatch_table"); // write name_dispatch_table

            // generate a word for each field
            for (int i = 0; i < numFields; i++) {
                this.assemblySupport.genWord("0");
            }
        }
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
        ErrorHandler errorHandler = new ErrorHandler();
        Parser parser = new Parser(errorHandler);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(errorHandler);

        for (String inFile : args) {
            System.out.println("\n========== Semantic Analysis results for " + inFile + " =============");
            try {
                errorHandler.clear();
                Program program = parser.parse(inFile);
                ClassTreeNode classTreeNode = analyzer.analyze(program);
                System.out.println(" Semantic Analysis was successful.");
                MipsCodeGenerator mipsCodeGenerator = new MipsCodeGenerator(null, false, false);
                mipsCodeGenerator.generate(classTreeNode, inFile.replace(".btm", ".asm"));
            } catch (CompilationException ex) {
                System.out.println(" There were errors in Semantic Analysis:");
                List<Error> errors = errorHandler.getErrorList();
                for (Error error : errors) {
                    System.out.println("\t" + error.toString());
                }
            }
        }


    }
}
