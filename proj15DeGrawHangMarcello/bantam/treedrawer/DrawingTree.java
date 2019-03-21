/*
 * @(#)DrawingTree.java                        2.0 1999/08/11
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
 * --Added ASTNode field
 * --Added bkgColor field
 * --Added textColor field
 * --Changed constructor to take in ASTNode
 * --Added getNode() method
 * --Added setSelected() method
 */

package proj15DeGrawHangMarcello.bantam.treedrawer;

import proj15DeGrawHangMarcello.bantam.ast.ASTNode;
import java.awt.*;

public class DrawingTree
{

    String caption;
    int width, height;
    Point pos, offset;
    Polygon contour;
    DrawingTree parent;
    DrawingTree[] children;
    private ASTNode node;

    private final int FIXED_FONT_HEIGHT = 10;
    //private final int FIXED_FONT_ASCENT = 3; -- never used

    private Color bkgColor = Color.yellow;
    private Color textColor = Color.black;


    public DrawingTree(ASTNode node, String caption, int width, int height)
    {
        this.node = node;
        this.caption = caption;
        this.width = width;
        this.height = height;
        this.parent = null;
        this.children = null;
        this.pos = new Point(0, 0);
        this.offset = new Point(0, 0);
        this.contour = new Polygon();
    }


    public void setChildren(DrawingTree[] children)
    {
        this.children = children;
        for (DrawingTree child : children) child.parent = this;
    }

    /**
     *
     * @return the ASTNode associated with this drawn rect
     */
    public ASTNode getNode() {
        return this.node;
    }


    /**
     * sets the background and text drawing colors for this node
     *
     * @param isSelected boolean denoting whether this node was the last node clicked
     */
    public void setSelected(boolean isSelected) {
        this.bkgColor = isSelected ? Color.blue : Color.yellow;
        this.textColor = isSelected ? Color.white : Color.black;
    }

    public void paint(Graphics graphics)
    {
        graphics.setColor(this.bkgColor);
        graphics.fillRect(pos.x, pos.y, width, height);
        graphics.setColor(this.textColor);
        graphics.drawRect(pos.x, pos.y, width - 1, height - 1);
        graphics.drawString(caption, pos.x + 2,
                pos.y + (height + FIXED_FONT_HEIGHT) / 2);

        if (children != null) {
            for (DrawingTree child : children) child.paint(graphics);
        }

        if (parent != null) {
            graphics.setColor(Color.black);
            graphics.drawLine(pos.x + width / 2, pos.y,
                    parent.pos.x + parent.width / 2,
                    parent.pos.y + parent.height);
        }
    }

    public void position(Point pos)
    {

        this.pos.x = pos.x + this.offset.x;
        this.pos.y = pos.y + this.offset.y;

        Point temp = new Point(this.pos.x, this.pos.y);

        if (children != null) {
            for (DrawingTree child : children) {
                child.position(temp);
                temp.x += child.offset.x;
                temp.y = this.pos.y + children[0].offset.y;
            }
        }
    }

    /**
     * @return this DrawingTree's array of DrawingTree children
     */
    public DrawingTree[] getChildren() {
        return this.children;
    }


}