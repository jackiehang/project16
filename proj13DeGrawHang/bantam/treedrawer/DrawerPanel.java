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
 */

package proj13DeGrawHang.bantam.treedrawer;

import javafx.application.Platform;
import org.fxmisc.richtext.model.Paragraph;
import proj13DeGrawHang.JavaCodeArea;
import proj13DeGrawHang.bantam.ast.ASTNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

class DrawerPanel extends JPanel implements MouseListener
{
    private DrawingTree drawingTree;
    private JavaCodeArea javaCodeArea;
    private DrawingTree curNodeClicked;

    public void setCorrespondingCodeArea(JavaCodeArea javaCodeArea) {
        this.javaCodeArea = javaCodeArea;
    }

    public DrawerPanel() {

        // listen for a mouse click
        this.addMouseListener(this);
        setPreferredSize(new Dimension(4096, 4096));
    }

    public void setDrawing(DrawingTree drawingTree)
    {
        this.drawingTree = drawingTree;
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        g.setColor(getBackground());
        Dimension d = getSize();
        g.fillRect(0, 0, d.width, d.height);

        if (drawingTree != null) {
            drawingTree.paint(g);
        }
    }

    /**
     * checks for click on a DrawingTree node click, changes its color,
     * moves to node's line in the corresponding CodeArea
     *
     * @param e the mouse click event
     */
    @Override
    public void mouseReleased(MouseEvent e) {

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
        this.repaint();

        // get the ASTNode corresponding to the clicked DrawingTree node
        ASTNode nodeClicked = this.curNodeClicked.getNode();

        // get line # in file corresponding to clicked node
        int lineNum = nodeClicked.getLineNum()-1;

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
            Platform.runLater( () -> {

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
                this.javaCodeArea.selectRange(caretPos, caretPos+lineLength);
            });
        }
    }

    /**
     * recursively checks nodeRect and its children to determine if a rect was clicked
     *
     * @param nodeRect the rectangle drawn in the JPanel
     * @param mouseX click x coordinate
     * @param mouseY click y coordinate
     * @return the ASTNode associated with the DrawingTree rect that was clicked,
     *         null if no node clicked
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
            for (int i = 0, childrenLength = children.length; i < childrenLength; i++) {
                DrawingTree aChildren = children[i];

                // get current child
                curChild = aChildren;

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
     * @param mouseX click x coordinate
     * @param mouseY click y coordinate
     * @return whether or node a node rectangle was clicked in the AST drawing
     */
    private boolean nodeWasClicked(DrawingTree nodeRect, int mouseX, int mouseY) {

        // get node drawing left and right bounds
        int nodeRectLeft = nodeRect.pos.x;
        int nodeRectRight = nodeRectLeft + nodeRect.width;

        // get node drawing top and bottom bounds
        int nodeRectTop = nodeRect.pos.y;
        int nodeRectBottom = nodeRectTop + nodeRect.height;

        boolean clickedBetweenWidth = (nodeRectLeft <= mouseX) && (mouseX <= nodeRectRight);
        boolean clickedBetweenHeight = (nodeRectTop < mouseY) && (mouseY < nodeRectBottom);

        return clickedBetweenWidth && clickedBetweenHeight;
    }



    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}