/*
 * @(#)DrawerPanel.java                        2.0 1999/08/11
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
 * Modified by Lucas Degraw to allow for navigation of the code file using the drawn AST.
 * March, 2019
 * -- Extends MouseAdapter instead of JPanel
 * -- Added JPanel field that overrides paintComponent() method
 * -- Added getPanel() method to get the JPanel from the Drawer
 * -- Added JavaCodeArea field
 * -- Added DrawingTree field for current node clicked (to reset previous)
 * -- Implements MouseListener
 * -- Added mouseClicked(e)
 * -- Added findClickedNode()
 * -- Added nodeWasClicked()
 * -- Added highlightLine()
 */

package proj13DeGrawHang.bantam.treedrawer;

import javafx.application.Platform;
import org.fxmisc.richtext.model.Paragraph;
import proj13DeGrawHang.JavaCodeArea;
import proj13DeGrawHang.bantam.ast.ASTNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

class DrawerPanel extends MouseAdapter {

    private DrawingTree drawingTree;
    private JavaCodeArea javaCodeArea;
    private DrawingTree curNodeClicked;
    private JPanel panel;


    public DrawerPanel() {

        // override paintComponent method in this JPanel instance to manually repaint tree
        this.panel = new JPanel() {
            @Override
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.setColor(getBackground());
                Dimension d = getSize();
                g.fillRect(0, 0, d.width, d.height);

                if (drawingTree != null) {
                    drawingTree.paint(g);
                }
            }
        };

        // listen for a mouse click
        panel.addMouseListener(this);

        // set panel dimensions
        panel.setPreferredSize(new Dimension(4096, 4096));
    }


    /**
     * @return the JPanel
     */
    public JPanel getPanel() {
        return this.panel;
    }

    public void setDrawing(DrawingTree drawingTree) {
        this.drawingTree = drawingTree;
    }

    public void setCorrespondingCodeArea(JavaCodeArea javaCodeArea) {
        this.javaCodeArea = javaCodeArea;
    }

    /**
     * checks for click on a DrawingTree node click, changes its color,
     * moves to node's line in the corresponding CodeArea
     *
     * @param e the mouse click event
     */
    @Override
    public void mouseClicked(MouseEvent e) {

        // save result of searching for a rect clicked in this DrawerPanel
        DrawingTree nodeRectClicked = findClickedNode(drawingTree, e.getX(), e.getY());

        // exit if no node was clicked
        if (nodeRectClicked == null) return;

        // set the clicked node the first time one is clicked
        if (this.curNodeClicked == null) this.curNodeClicked = nodeRectClicked;
        else {
            // reset the previous node clicked
            this.curNodeClicked.setSelected(false);
            this.curNodeClicked = nodeRectClicked;
        }

        // the node has been clicked
        this.curNodeClicked.setSelected(true);

        // repaint the tree
        this.panel.repaint();

        // get the ASTNode corresponding to the clicked DrawingTree node
        ASTNode nodeClicked = this.curNodeClicked.getNode();

        // get line # in file corresponding to clicked node
        int lineNum = nodeClicked.getLineNum() - 1;

        // highlight the line in the code area
        highlightLine(lineNum);

    }

    /**
     * moves to and highlights a line in a CodeArea
     *
     * @param lineNum line index of a line in a CodeArea
     */
    private void highlightLine(int lineNum) {

        // move to the line
        if (this.javaCodeArea != null) {

            // must run on GUI thread
            Platform.runLater(() -> {

                // focus on the code area
                this.javaCodeArea.requestFocus();

                // go to the line containing the clicked node
                this.javaCodeArea.showParagraphAtTop(lineNum);

                // get current line
                Paragraph curLine = this.javaCodeArea.getParagraph(lineNum);

                // place caret at start of line
                this.javaCodeArea.moveTo(lineNum, 0);

                // get caret position (offset from first character in file)
                int caretPos = this.javaCodeArea.getCaretPosition();

                // get line length
                int lineLength = curLine.length();

                // highlight the line
                this.javaCodeArea.selectRange(caretPos, caretPos + lineLength);
            });
        }
    }

    /**
     * recursively checks nodeRect and its children to determine if a rect was clicked
     *
     * @param nodeRect the rectangle drawn in the JPanel
     * @param mouseX   click x coordinate
     * @param mouseY   click y coordinate
     * @return the ASTNode associated with the DrawingTree rect that was clicked,
     * null if no node clicked
     */
    private DrawingTree findClickedNode(DrawingTree nodeRect, int mouseX, int mouseY) {

        // base case
        if (nodeWasClicked(nodeRect, mouseX, mouseY)) {
            return nodeRect;
        }

        // get the children
        DrawingTree[] children = nodeRect.getChildren();

        // initialize vars reset within loop
        DrawingTree curChild;
        DrawingTree clickedNode;

        // if the children exist, loop through them and see if any were clicked
        if (children != null) {

            // loop through children
            for (DrawingTree child : children) {
                // get current child
                curChild = child;

                // check this child and its children for a clicked node
                clickedNode = findClickedNode(curChild, mouseX, mouseY);

                // return the node if it was clicked
                if (clickedNode != null) return clickedNode;

            }
        }
        return null;
    }

    /**
     * compares mouse click location to nodeRect location to determine whether or not
     * the nodeRect was clicked
     *
     * @param nodeRect the rectangle drawn in the JPanel
     * @param mouseX   click x coordinate
     * @param mouseY   click y coordinate
     * @return whether or node a node rectangle was clicked in the AST drawing
     */
    private boolean nodeWasClicked(DrawingTree nodeRect, int mouseX, int mouseY) {

        // get node drawing left and right bounds
        int nodeRectLeft = nodeRect.pos.x;
        int nodeRectRight = nodeRectLeft + nodeRect.width;

        // get node drawing top and bottom bounds
        int nodeRectTop = nodeRect.pos.y;
        int nodeRectBottom = nodeRectTop + nodeRect.height;

        // booleans denoting whether click was within horizontal, vertical node bounds
        boolean clickedBetweenWidth = (nodeRectLeft <= mouseX) && (mouseX <= nodeRectRight);
        boolean clickedBetweenHeight = (nodeRectTop < mouseY) && (mouseY < nodeRectBottom);

        return clickedBetweenWidth && clickedBetweenHeight;
    }

}