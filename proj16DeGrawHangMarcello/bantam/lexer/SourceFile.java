/*
 * @(#)SourceFile.java                        2.0 1999/08/11
 *
 * Copyright (C) 1999 D.A. Watt and D.F. Brown
 * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
 * and School of Computer and Math Sciences, The Robert Gordon University,
 * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
 * All rights reserved.
 *
 * This software is provided free for educational use only. It may
 * not be used for commercial purposes without the prior written permission
 * of the authors.
 *
 * Modified by Dale Skrien, Fall 2018
 * Edited By: Lucas DeGraw, Jackie Hang, Chris Marcello
 * Project 13
 * Date: March 7, 2019
 */


package proj16DeGrawHangMarcello.bantam.lexer;

import proj16DeGrawHangMarcello.bantam.util.CompilationException;

import java.io.*;

/**
 * A class for extracting the characters, one at a time, from a text file or an
 * InputStream.
 */
class SourceFile
{
    static final char eol = '\n';         // end of line character
    private static final char cr = '\r';  // carriage return character
    static final char eof = '\u0000';     // end of file character

    private Reader sourceReader;   // the reader of the file
    private int currentLineNumber; // for bantam.error messages
    private int prevChar;          // the previous character read
    private String filename;       // the file currently being scanned.
    private int curColPos;

    /**
     * creates a new SourceFile object for the file with the given name
     * Note:  You should always call isValid() to check that the SourceFile
     * was set up properly before calling getNextChar().
     *
     * @param filename the name of the file to be read.
     */
    SourceFile(String filename) {
        try {
            sourceReader = new FileReader(filename);
        } catch (FileNotFoundException e) {
            throw new CompilationException("File " + filename + " not found.");
        }
        currentLineNumber = 1;
        curColPos = -1;
        prevChar = -1;
        this.filename = filename;
    }


    SourceFile(Reader in) {
        sourceReader = in;
        currentLineNumber = 1;
        prevChar = -1;
        curColPos = -1;
    }

    int getCurrentColPos(){return curColPos;}
    int getCurrentLineNumber() {
        return currentLineNumber;
    }

    String getFilename() { return filename; }

    /**
     * Finds and returns the next character in the source file.
     * If the end of the file has been reached or an exception occurs,
     * then an exception is thrown.
     * If the character is the cr character or the eol char not preceded by the cr
     * character, the current line number is also incremented.
     *
     * @return the next character in the source file
     */
    char getNextChar(){
        try {
            int c = sourceReader.read();
            curColPos++;

            if (c == -1) {
                c = eof;
            }
            else if (c == cr || (c == eol && prevChar != cr)) {
                currentLineNumber++;
                curColPos = -1;
            }
            prevChar = c;
            return (char) c;
        } catch (IOException e) {
            throw new CompilationException("File " + filename + " could not be read.");
        }
    }

}

