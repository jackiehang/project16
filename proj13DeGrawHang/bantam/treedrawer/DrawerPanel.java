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

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    /**
     *
     *
     * @param e the mouse click event
     */
    @Override
    public void mouseReleased(MouseEvent e) {

        // save result of searching for a rect clicked in this DrawerPanel
        ASTNode nodeClicked = findClickedNode(drawingTree, e.getX(), e.getY());

        // exit if no node was clicked
        if (nodeClicked == null) return;

        // get line # in file corresponding to clicked node
        int lineNum = nodeClicked.getLineNum();

        // move to the line
        if (this.javaCodeArea != null) {
            this.javaCodeArea.showParagraphAtTop(lineNum-1);
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
    private ASTNode findClickedNode(DrawingTree nodeRect, int mouseX, int mouseY) {

        // base case
        if (nodeWasClicked(nodeRect, mouseX, mouseY)) {
            return nodeRect.getNode();
        }

        // get the children
        DrawingTree[] children = nodeRect.getChildren();

        // initialize vars reset within loop
        DrawingTree curChild;
        ASTNode clickedNode;

        // if the children exist, loop through them and see if any were clicked
        if (children != null) {

            // loop through children
            for (int i = 0; i < children.length; i++) {

                // get current child
                curChild = children[i];

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

        // get node left and right bounds
        int nodeRectLeft = nodeRect.pos.x;
        int nodeRectRight = nodeRectLeft + nodeRect.width;

        // get node top and bottom bounds
        int nodeRectTop = nodeRect.pos.y;
        int nodeRectBottom = nodeRectTop + nodeRect.height;

        // if within AST rect bounds
        if (nodeRectLeft <= mouseX && mouseX <= nodeRectRight &&
                nodeRectTop < mouseY && mouseY < nodeRectBottom) {

            return true;
        }
        return false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}