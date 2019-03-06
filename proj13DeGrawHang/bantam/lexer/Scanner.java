/*
 * File: Scanner.java
 * Edited By: Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 * Project 9
 * Date: November 20, 2018
 */
package proj13DeGrawHang.bantam.lexer;

import java.io.Reader;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import proj13DeGrawHang.bantam.util.ErrorHandler;
import proj13DeGrawHang.bantam.util.Error;
import proj13DeGrawHang.bantam.util.CompilationException;


public class Scanner
{
    private SourceFile sourceFile;
    private ErrorHandler errorHandler;
    private char currentChar;
    private char prevChar;
    private Map<Character,Token.Kind> singleOperatorMap;
    private Map<String,Token.Kind> doubleOperatorMap;

    /**
     * constructor for the scanner when fed into an errorhandler
     * @param handler the error handler to be fed in
     */
    public Scanner(ErrorHandler handler) {
        errorHandler = handler;
        currentChar = ' ';
        prevChar = ' ';
        sourceFile = null;
        this.createOperatorMaps();
    }

    /**
     * constructor for the scanner when fed into an errorhandler and filename
     * @param filename the file name for the source file
     * @param handler the error handler to be fed in
     */
    public Scanner(String filename, ErrorHandler handler) {
        errorHandler = handler;
        currentChar = ' ';
        prevChar = ' ';
        sourceFile = new SourceFile(filename);
        this.createOperatorMaps();
    }

    /**
     * constructor for the scanner when fed into an errorhandler and reader
     * @param reader the reader for the sourcefile class
     * @param handler the error handler to be fed in
     */
    public Scanner(Reader reader, ErrorHandler handler) {
        errorHandler = handler;
        sourceFile = new SourceFile(reader);
        currentChar = ' ';
        prevChar = ' ';
        this.createOperatorMaps();
    }

    public String getFilename() { return this.sourceFile.getFilename(); }

    /**
     * setter for the source file
     */
    private void setSourceFile(SourceFile sourceFile){
        this.sourceFile = sourceFile;
    }

    /**
     * scan the file and return the first token seen
     * @return the first complete token object
     */
    public Token scan() {
        updateChars();
        //read through whitespace until reach valid token character
        while(Character.isSpaceChar(prevChar)||prevChar == '\n'||prevChar == '\r'|| prevChar == '\t'){
            updateChars();
        }
        int lineNumber = sourceFile.getCurrentLineNumber();
        String lastTwoChars = Character.toString(prevChar)+Character.toString(currentChar);
        //fixes bug where line number gets incremented too early on WindowsOS
        if(currentChar == '\r' || currentChar == '\n'){
            lineNumber--;
        }

        if(prevChar=='/'&&(currentChar=='/'||currentChar=='*')){
            return readComment();
        }
        else if(doubleOperatorMap.containsKey(lastTwoChars)){
            updateChars();
            return new Token(doubleOperatorMap.get(lastTwoChars), lastTwoChars, lineNumber);
        }
        else if(singleOperatorMap.containsKey(prevChar)){
            return new Token(singleOperatorMap.get(prevChar),Character.toString(prevChar),lineNumber);
        }
        else if(Character.isDigit(prevChar)){
            return readIntConst();
        }
        else if(prevChar == '"'){
            return readString();
        }
        else if(Character.isLetter(prevChar)||prevChar == '_'){
            return readIdentifier();
        }
        else if(prevChar == SourceFile.eof){
            return new Token(Token.Kind.EOF,"",lineNumber);
        }
        //If not one of the above characters, is not a legal character in Bantam Java, throw error
        errorHandler.register(Error.Kind.LEX_ERROR,sourceFile.getFilename(),
                lineNumber,"Illegal Character.");
        return new Token(Token.Kind.ERROR, Character.toString(prevChar),lineNumber);
    }

    /**
     * Reached a character that could be in an identifier, reads until character that could not be in an identifier
     * @return Identifier Token
     */
    private Token readIdentifier(){
        StringBuilder result = new StringBuilder().append(prevChar);
        int lineNumber = sourceFile.getCurrentLineNumber();

        //read while character is valid identifier character
        while(Character.isLetter(currentChar)||currentChar == '_'||Character.isDigit(currentChar)){
            result.append(currentChar);
            updateChars();
        }

        String resultString = result.toString();
        if(resultString.startsWith("_")){
            //Identifiers cannot just be the "_" character in Bantam
            errorHandler.register(Error.Kind.LEX_ERROR,sourceFile.getFilename(),
                    lineNumber,"Invalid Identifier Name.");
            return new Token(Token.Kind.ERROR,resultString,lineNumber);
        }
        return new Token(Token.Kind.IDENTIFIER,resultString,lineNumber);
    }

    /**
     * Reached an integer, reads until reaches character that is not an int and then returns
     * @return Integer Token
     */
    private Token readIntConst(){
        StringBuilder result = new StringBuilder().append(prevChar);
        int lineNumber = sourceFile.getCurrentLineNumber();

        //read while character is a digit
        while(Character.isDigit(currentChar)){
            result.append(currentChar);
            updateChars();
        }
        String resultString = result.toString();

        //case where integer is too large
        if(resultString.length()>11||Long.parseLong(resultString) > Integer.MAX_VALUE){
            errorHandler.register(Error.Kind.LEX_ERROR,sourceFile.getFilename(),
                    lineNumber,"Integer Value Too Large");
            return new Token(Token.Kind.ERROR,resultString,lineNumber);
        }
        return new Token(Token.Kind.INTCONST,resultString,lineNumber);
    }

    /**
     * Entered a string, now continues scanning until scan reaches closing string character.
     * @return Token with string inside
     */
    private Token readString(){
        List<Character> legalEscapeChars = Arrays.asList('t','n','"','f');
        StringBuilder result = new StringBuilder().append('"');
        int lineNumber = sourceFile.getCurrentLineNumber();
        boolean hasError = false;
        boolean inBackslash = false;
        boolean reachedEOF = true;

        //read string until reach end of file
        while(currentChar!= SourceFile.eof){
            if(inBackslash){
                prevChar = ' ';
                inBackslash = false;
            }
            result.append(currentChar);

            //string too long
            if(result.length()>5000){
                updateChars();
                errorHandler.register(Error.Kind.LEX_ERROR,sourceFile.getFilename(),
                        lineNumber,"String Literal Too Long");
                hasError = true;
                break;
            }
            //handle backslash character
            if(currentChar == '\\'&& prevChar=='\\'){
                inBackslash = true;
            }
            //handle escape characters
            else if(prevChar == '\\'){
                if(!legalEscapeChars.contains(currentChar)) {
                    errorHandler.register(Error.Kind.LEX_ERROR,sourceFile.getFilename(),
                            lineNumber, "Illegal escape character in string literal");
                    hasError = true;
                }
                inBackslash = false;
            }
            //exit string
            else if(currentChar == '"'){
                updateChars();
                reachedEOF = false;
                break;
            }
            //invalid string, new line character appears before end of the string
            else if(currentChar == '\n'){
                updateChars();
                errorHandler.register(Error.Kind.LEX_ERROR, sourceFile.getFilename(),
                        lineNumber,"Illegal line end in string literal");
                hasError = true;
                break;
            }
            updateChars();
        }
        //end of file reached before end of string
        if(reachedEOF) {
            errorHandler.register(Error.Kind.LEX_ERROR, sourceFile.getFilename(),
                    lineNumber, "Unclosed String");
            hasError = true;
        }
        //we read all the way through so that we can report all errors in the string to the errorHandler
        if(hasError) {
            return new Token(Token.Kind.ERROR, result.toString(), lineNumber);
        }
        else{
            return new Token(Token.Kind.STRCONST,result.toString(),lineNumber);
        }
    }

    /**
     * Entered comment, now reads all characters into the token until reaches the closing comment characters
     * @return Full comment token, either block comment or line comment.
     */
    private Token readComment(){
        boolean inLineComment = false;
        StringBuilder result = new StringBuilder().append('/');
        int lineNumber = sourceFile.getCurrentLineNumber();
        //first character always '/', now check second character to know what closing characters should be
        if (currentChar   == '/'){
            inLineComment = true;
        }

        result.append(currentChar);
        updateChars();
        while(currentChar!=SourceFile.eof){
            result.append(currentChar);

            if(inLineComment){
                //closing chracter \n
                if(currentChar == '\n'){
                    updateChars();
                    String resultString = result.toString().substring(0,result.length()-1);
                    return new Token(Token.Kind.COMMENT,resultString,lineNumber);
                }
            }
            else{
                //closing character */
                if(currentChar == '/'&&prevChar=='*'){
                    updateChars();
                    return new Token(Token.Kind.COMMENT, result.toString(),lineNumber);
                }
            }
            updateChars();
        }
        //handles line comment on last line of file, no new line char afterward.
        if(inLineComment){
            return new Token(Token.Kind.COMMENT,result.toString(),lineNumber);
        }

        //unclosed comment error
        errorHandler.register(Error.Kind.LEX_ERROR,result.toString(),
                sourceFile.getCurrentLineNumber(),"Unclosed Comment");
        return new Token(Token.Kind.ERROR, result.toString(),lineNumber);
    }

    /**
     * Increments prevChar and currentChar
     */
    private void updateChars(){
        prevChar = currentChar;
        currentChar = sourceFile.getNextChar();
    }

    /**
     * Creates single and double character operator maps to check when scanning
     */
    private void createOperatorMaps(){
        this.singleOperatorMap = Map.ofEntries(
                Map.entry('+',Token.Kind.PLUSMINUS),
                Map.entry('-',Token.Kind.PLUSMINUS),
                Map.entry('/',Token.Kind.MULDIV),
                Map.entry('*',Token.Kind.MULDIV),
                Map.entry('=',Token.Kind.ASSIGN),
                Map.entry('%',Token.Kind.MULDIV),
                Map.entry('<',Token.Kind.COMPARE),
                Map.entry('>',Token.Kind.COMPARE),
                Map.entry('!',Token.Kind.UNARYNOT),
                Map.entry('[',Token.Kind.LBRACKET),
                Map.entry(']',Token.Kind.RBRACKET),
                Map.entry('{',Token.Kind.LCURLY),
                Map.entry('}',Token.Kind.RCURLY),
                Map.entry('(',Token.Kind.LPAREN),
                Map.entry(')',Token.Kind.RPAREN),
                Map.entry('.',Token.Kind.DOT),
                Map.entry(';',Token.Kind.SEMICOLON),
                Map.entry(':',Token.Kind.COLON),
                Map.entry(',',Token.Kind.COMMA)
        );

        this.doubleOperatorMap = Map.ofEntries(
                Map.entry("==",Token.Kind.COMPARE),
                Map.entry("!=",Token.Kind.COMPARE),
                Map.entry("<=",Token.Kind.COMPARE),
                Map.entry(">=",Token.Kind.COMPARE),
                Map.entry("++",Token.Kind.UNARYINCR),
                Map.entry("--",Token.Kind.UNARYDECR),
                Map.entry("&&",Token.Kind.BINARYLOGIC),
                Map.entry("||",Token.Kind.BINARYLOGIC)
        );
    }
    public static void main(String[] argv){
        if(argv.length == 0){
            System.out.println("Please Provide Test Files");
            return;
        }

        for(String filename: argv) {
            int numerrors = 0;
            Scanner scanner;
            try {
                scanner = new Scanner(filename, new ErrorHandler());
            }catch(CompilationException e){
                System.out.println("Invalid filename: "+filename);
                continue;
            }
            Token token = scanner.scan();
            while (token.kind != Token.Kind.EOF) {
                if(token.kind == Token.Kind.ERROR){
                    numerrors++;
                }
                System.out.println(token);
                token = scanner.scan();
            }
            System.out.println("There were " + numerrors + " error tokens");
        }
    }

}
