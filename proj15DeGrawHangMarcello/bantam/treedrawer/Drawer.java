/*
 * @(#)Drawer.java                        2.0 1999/08/11
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
 * Modified by Dale Skrien to work with Bantam Java
 * January, 2014
 *
 * Modified by Lucas Degraw to allow for navigation of the code file using the drawn AST.
 * March, 2019
 * -- Added JavaCodeArea field
 * -- Added setCorrespondingCodeArea() method

 */

package proj15DeGrawHangMarcello.bantam.treedrawer;

import proj15DeGrawHangMarcello.JavaCodeArea;
import proj15DeGrawHangMarcello.bantam.ast.Program;

import java.awt.*;

public class Drawer
{
    private JavaCodeArea javaCodeArea;

    /**
     * Displays a Swing window with a drawing of the AST
     * @param sourceName the name of the file containing the program parsed into the AST
     * @param AST The AST created when parsing the file with the given name.
     */
    public void draw(String sourceName, Program AST)
    {
        DrawerPanel panel;

        panel = new DrawerPanel();

        // give the panel the code area from which the tree is generated
        if (this.javaCodeArea != null) panel.setCorrespondingCodeArea(this.javaCodeArea);

        DrawerFrame frame = new DrawerFrame(sourceName, panel.getPanel());

        Font font = new Font("SansSerif", Font.PLAIN, 12);
        frame.setFont(font);

        FontMetrics fontMetrics = frame.getFontMetrics(font);

        proj15DeGrawHangMarcello.bantam.treedrawer.LayoutVisitor layout =
                new LayoutVisitor(fontMetrics);

        proj15DeGrawHangMarcello.bantam.treedrawer.DrawingTree theDrawing =
                (DrawingTree) AST.accept(layout);

        theDrawing.position(new Point(2048, 10));

        panel.setDrawing(theDrawing);

        frame.setVisible(true);
    }

    /**
     * sets this Drawer's code area
     *
     * @param javaCodeArea the code area from which this tree is drawn
     */
    public void setCorrespondingCodeArea(JavaCodeArea javaCodeArea) {
        this.javaCodeArea = javaCodeArea;
    }

}
