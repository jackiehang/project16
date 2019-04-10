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

____________________________________________
 * Edited by:
 * Names: Lucas DeGraw, Jackie Hang, ChrisMarcello
 * Class: CS 361
 * Project 16
 * Date: April 11, 2018

*/

package proj16DeGrawHangMarcello.bantam.codegenmips;

import proj16DeGrawHangMarcello.bantam.ast.*;
import proj16DeGrawHangMarcello.bantam.parser.Parser;
import proj16DeGrawHangMarcello.bantam.semant.SemanticAnalyzer;
import proj16DeGrawHangMarcello.bantam.semant.StringConstantsVisitor;
import proj16DeGrawHangMarcello.bantam.util.*;
import proj16DeGrawHangMarcello.bantam.util.Error;
import proj16DeGrawHangMarcello.bantam.util.ErrorHandler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;


/**
 * The <tt>MipsCodeGenerator</tt> class generates mips assembly code
 * targeted for the SPIM or Mars emulators.
 * <p/>
 * This class is incomplete and will need to be implemented by the student.
 */
public class MipsCodeGenerator {
    /**
     * Root of the AST
     */
    private Program ast;

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
    public void generate(ClassTreeNode root, String outFile, Program program) {
        this.root = root;
        this.ast = program;
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

        this.assemblySupport = new MipsSupport(this.out);

        // begin generating data section
        this.assemblySupport.genDataStart();
        this.out.print("\n");

        // generate garbage collecting flag section
        genGCSection(this.gc);

        generateStringConstants(outFile);

        generateClassTableNames();

        generateObjectTemplates();

        generateDispatchTables();
        this.out.print("\n");

        this.assemblySupport.genTextStart();
        generateTextStub();

    }



    /**
     * Generate the code for the gc_flag (garbage collection) section
     * @param collecting
     */
    private void genGCSection(boolean collecting) {
        int flag = collecting ? 1 : 0;
        assemblySupport.genLabel("gc_flag");
        assemblySupport.genWord(String.valueOf(flag));
        this.out.print("\n");

    }

    /**
     * Calculates the string size in bytes
     * 16 bytes +1 ascii + string length rounded
     * up to closest 4 factor
     *
     * @param string string finding length of
     * @return
     */
    private int getStringSize(String string){
        int length = 17 + string.length();
        double calc = Math.ceil((double)length/4);
        length = (int)calc * 4;
        return length;
    }
    /**
     * Generates the String Constants in the Data Section of the assembly file.
     * Each one is in the format:
     *
     */
    private void generateStringConstants(String fileName) {
        saveClassTableNames();

        String classNames[] = classNameTable.keySet().toArray(new String[0]);

        for (String className : classNames) {
            genStrConstHelper("class_name_" + classNameTable.get(className),className);
        }

        //Filename
        genStrConstHelper(assemblySupport.getLabel(), fileName.replace(".asm", ".btm"));

        //All string constants in the file
        StringConstantsVisitor stringConstantsVisitor = new StringConstantsVisitor();
        Map<String,String> stringConstantsMap = stringConstantsVisitor.getStringConstants(ast);
        for (Map.Entry<String,String> stringConstant : stringConstantsMap.entrySet()) {
            String strConst= stringConstant.getKey().substring(1,stringConstant.getKey().length()-1);
            genStrConstHelper(assemblySupport.getLabel(), strConst);
        }

    }

    /**
     * Used to help generate string constants
     * @param label label for the string constant
     * @param strConst string value
     */
    private void genStrConstHelper(String label, String strConst){
        assemblySupport.genLabel(label);
        assemblySupport.genWord("1");
        assemblySupport.genWord(String.valueOf(getStringSize(strConst))); //string length in bytes
        assemblySupport.genWord("String_dispatch_table"); //link to string dispatch table
        assemblySupport.genWord(String.valueOf(strConst.length())); //length of the string in chars
        assemblySupport.genAscii(strConst);
        this.out.print("\n");

    }

    /**
     * Gets the class names from the classMap
     * and matches them to appropriate indices.
     */
    private void saveClassTableNames() {
        //get class names as a set
        String[] classNames = root.getClassMap().keySet().toArray(new String[0]);
        int counter = 5;
        for(String cName: classNames){
            switch (cName) {
                case "Object":
                    classNameTable.put("Object", 0);
                    break;
                case "String":
                    classNameTable.put("String", 1);
                    break;

                case "Sys":
                    classNameTable.put("Sys", 2);
                    break;

                case "Main":
                    classNameTable.put("Main", 3);
                    break;

                case "TextIO":
                    classNameTable.put("TextIO", 4);
                    break;

                default:
                    classNameTable.put(cName, counter);
                    counter++;
                    break;
            }
        }

        //sorts the map of classes by their index
        classNameTable = classNameTable.entrySet().stream()
                .sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    /**
     * generates the class_name_table
     */
    private void generateClassTableNames() {

        this.out.println("class_name_table:");
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
     * Generates a stub for text section
     */
    private void generateTextStub() {
        String classNames[] = classNameTable.keySet().toArray(new String[0]);
        this.out.print("\n");
        for (String className : classNames) {
            this.assemblySupport.genLabel(className+"_init");
        }
        this.out.print("\n");

        //All methods in the file
        MethodVisitor methodVisitor = new MethodVisitor();
        Map<String,ArrayList<String>> classMethodsMap = methodVisitor.getMethods(ast);


        for (Map.Entry<String,ArrayList<String>> methodList : classMethodsMap.entrySet()) {
            for(String methodName: methodList.getValue()){
                assemblySupport.genLabel(methodList.getKey()+"."+methodName);
            }
        }

        this.out.println("\njr $ra");

    }

    /**
     * generates a template section for each object
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

            if (s.equals("TextIO")) genTextIOGlobals(keys);
        }
    }

    /**
     * generates the global dispatch labels in the TextIO_template section
     * @param keys
     */
    private void genTextIOGlobals(Set<String> keys) {
        this.out.println("\n");
        // foreach key of classNameTable
        for (String s : keys) {
           this.assemblySupport.genGlobal(s+"_dispatch_table");
        }
    }

    /**
     * generates a dispatch table for each object
     */
    private void generateDispatchTables(){

        Map classMap = this.root.getClassMap();
        Set<String> keys = this.classNameTable.keySet();

        List<String> methodNameList;

        ClassTreeNode curNode;
        LinkedHashMap<String, String> methodClassMap = new LinkedHashMap();
        MemberList members;
        String methodName;

        String curName = "";
        for (String s : keys) {

            curNode =  (ClassTreeNode)classMap.get(s);

            this.out.println("\n"+s+"_dispatch_table:");
            while (curNode != null) {

                curName = curNode.getName();

                // get each one's methods & fields (Members)
                members = curNode.getASTNode().getMemberList();

                int numMems = members.getSize();

                // loop backwards through the members to add them in the order declared in .btm file
                for (int i = numMems-1; i >= 0; i--) {

                    // get current member
                    Member member = (Member)members.get(i);

                    // if it's a method
                    if (member instanceof Method) {
                        methodName = ((Method) member).getName();   // save method name

                        if (methodClassMap.containsKey(methodName)) {   // if method already declared
                            methodClassMap.remove(methodName);  // remove existing declaration
                            methodClassMap.put(methodName, s);  // add declaration
                        }
                        else {  // method is not already declared
                            methodClassMap.put(methodName, curName);   // add new declaration
                        }
                    }
                }
                curNode = curNode.getParent();    // reset node
            }

            // save list of keys
            methodNameList = new ArrayList<>();
            methodNameList.addAll(methodClassMap.keySet());

            // loop backwards through list of method keys to write in order declared in file
            for (int i = methodNameList.size()-1; i >= 0; i--) {

                methodName = methodNameList.get(i);
                curName = methodClassMap.get(methodName);
                this.assemblySupport.genWord(curName+"."+methodName); // pop & write value of stack
            }
            methodClassMap.clear();
        }
    }

    public static void main(String[] args) {
        ErrorHandler errorHandler = new ErrorHandler();
        Parser parser = new Parser(errorHandler);
        SemanticAnalyzer analyzer = new SemanticAnalyzer(errorHandler);

        for (String inFile : args) {
            System.out.println("\n========== MIPS Code Generation results for " + inFile + " =============");
            try {
                errorHandler.clear();
                Program program = parser.parse(inFile);
                ClassTreeNode classTreeNode = analyzer.analyze(program);
                System.out.println(" Semantic Analysis was successful.");
                MipsCodeGenerator mipsCodeGenerator = new MipsCodeGenerator(errorHandler, false, false);
                mipsCodeGenerator.generate(classTreeNode, inFile.replace(".btm", ".asm"), program);
                System.out.println(" Generation of "+ inFile.replace(".btm", ".asm") + " was successful.");
            } catch (CompilationException ex) {
                System.out.println(" There were errors in generation:");
                List<Error> errors = errorHandler.getErrorList();
                for (Error error : errors) {
                    System.out.println("\t" + error.toString());
                }
            }
        }


    }
}
