/*
 * @(#)LayoutVisitor.java                        2.0 1999/08/11
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
 * Modified by Dale Skrien to work with the Bantam Java compiler
 * --added layoutNary method
 * --changed all the visit methods to work with the Visitor class
 *
 * Modified by Lucas Degraw to allow for navigation of the code file using the drawn AST.
 * March, 2019
 * -- Changed all auxiliary methods to take the the node calling them as input
 *    to pass to the DrawingTree()
 * -- Changed visit() methods to pass the the respective node to the aux methods

 */

package proj13DeGrawHang.bantam.treedrawer;

import proj13DeGrawHang.bantam.ast.*;
import proj13DeGrawHang.bantam.visitor.Visitor;

import java.awt.*;

public class LayoutVisitor extends Visitor
{

    private final int BORDER = 5;
    private final int PARENT_SEP = 30;

    private FontMetrics fontMetrics;

    public LayoutVisitor(FontMetrics fontMetrics)
    {
        this.fontMetrics = fontMetrics;
    }


    // Programs, Classes, Methods, Fields

    public Object visit(Program node)
    {
        return layoutNary(node, "Program", node.getClassList());
    }

    public Object visit(Class_ node)
    {
        return layoutUnary(node, "Class " + node.getName(), node.getMemberList());
    }

    public Object visit(MemberList node) {
        return layoutNary(node, "MemberList", node);
    }

    public Object visit(Field node)
    {
        if(node.getInit() == null)
            return layoutNullary(node, "Field " + node.getName() + ":" + node.getType());
        else
            return layoutUnary(node, "Field " + node.getName() + ":" + node.getType(),
                    node.getInit());
    }

    public Object visit(Method node)
    {
        return layoutBinary(node, "Method " + node.getName() + ":" + node.getReturnType(),
                node.getFormalList(), node.getStmtList());
    }

    public Object visit(StmtList node) {
        return layoutNary(node, "StmtList",node);
    }

    public Object visit(FormalList node) {
        return layoutNary(node, "FormalList", node);
    }

    public Object visit(Formal node) {
        return layoutNullary(node, "Formal " + node.getName()+ ":" + node.getType());
    }

    // Statements

    public Object visit(ReturnStmt node)
    {
        if(node.getExpr() == null)
            return layoutNullary(node, "Return");
        else
            return layoutUnary(node, "Return", node.getExpr());
    }

    public Object visit(WhileStmt node) {
        return layoutBinary(node, "While",node.getPredExpr(),node.getBodyStmt());
    }

    public Object visit(DeclStmt node)
    {
            return layoutUnary(node, "Var Decl " + node.getName(),
                    node.getInit());
    }

    public Object visit(ExprStmt node) {
        return layoutUnary(node, "ExprStmt",node.getExpr());
    }

    public Object visit(IfStmt node)
    {
        if(node.getElseStmt() == null)
            return layoutBinary(node, "If",node.getPredExpr(),node.getThenStmt());
        else
            return layoutTernary(node, "If",node.getPredExpr(),node.getThenStmt(),
                    node.getElseStmt());
    }

    public Object visit(ForStmt node)
    {
        ListNode list = new ExprList(0,0);
        if(node.getInitExpr() != null)
            list.addElement(node.getInitExpr());
        if(node.getPredExpr() != null)
            list.addElement(node.getPredExpr());
        if(node.getUpdateExpr() != null)
            list.addElement(node.getUpdateExpr());
        list.addElement(node.getBodyStmt());
        return layoutNary(node, "For",list);
    }

    public Object visit(BlockStmt node) {
        return layoutNary(node, "Block", node.getStmtList());
    }

    public Object visit(BreakStmt node) {
        return layoutNullary(node, "Break");
    }

    // Expressions

    public Object visit(ExprList node) {
        return layoutNary(node, "ExprList", node);
    }

    public Object visit(AssignExpr node) {
        return layoutUnary(node, "Assign " + (node.getRefName() != null ?
                node.getRefName() + "." : "") + node.getName(), node.getExpr());
    }

    public Object visit(InstanceofExpr node) {
        return layoutUnary(node, "Instanceof " + node.getType(), node.getExpr());
    }

    public Object visit(NewArrayExpr node) {
        return layoutUnary(node, "new " + node.getType() + "[]", node.getSize());
    }

    public Object visit(NewExpr node) {
        return layoutNullary(node, "New " + node.getType());
    }

    public Object visit(DispatchExpr node) {
        if(node.getRefExpr() == null)
            return layoutUnary(node, "Dispatch "+node.getMethodName(),
                    node.getActualList());
        else
            return layoutBinary(node, "Dispatch " + node.getMethodName(),
                    node.getRefExpr(), node.getActualList());
    }

    public Object visit(CastExpr node) {
        return layoutUnary(node, "cast to " + node.getType(), node.getExpr());
    }

    public Object visit(ArrayAssignExpr node) {
        return layoutBinary(node, "Assign " + (node.getRefName() == null ? "" : node.getRefName() + ".")
                + node.getName(), node.getIndex(), node.getExpr());
    }

    // Binary expressions

    public Object visit(BinaryArithDivideExpr node) {
        return layoutBinary(node, "/", node.getLeftExpr(), node.getRightExpr());
    }

    public Object visit(BinaryArithPlusExpr node) {
        return layoutBinary(node, "+", node.getLeftExpr(), node.getRightExpr());
    }

    public Object visit(BinaryArithMinusExpr node) {
        return layoutBinary(node, "-", node.getLeftExpr(), node.getRightExpr());
    }

    public Object visit(BinaryArithTimesExpr node) {
        return layoutBinary(node, "*", node.getLeftExpr(), node.getRightExpr());
    }

    public Object visit(BinaryArithModulusExpr node) {
        return layoutBinary(node, "%", node.getLeftExpr(), node.getRightExpr());
    }

    public Object visit(BinaryCompEqExpr node) {
        return layoutBinary(node, "==", node.getLeftExpr(), node.getRightExpr());
    }

    public Object visit(BinaryCompNeExpr node) {
        return layoutBinary(node, "!=", node.getLeftExpr(), node.getRightExpr());
    }

    public Object visit(BinaryCompGeqExpr node) {
        return layoutBinary(node, ">=", node.getLeftExpr(), node.getRightExpr());
    }

    public Object visit(BinaryCompGtExpr node) {
        return layoutBinary(node, ">", node.getLeftExpr(), node.getRightExpr());
    }

    public Object visit(BinaryCompLeqExpr node) {
        return layoutBinary(node, "<=", node.getLeftExpr(), node.getRightExpr());
    }

    public Object visit(BinaryCompLtExpr node) {
        return layoutBinary(node, "<", node.getLeftExpr(), node.getRightExpr());
    }

    public Object visit(BinaryLogicAndExpr node) {
        return layoutBinary(node, "And", node.getLeftExpr(), node.getRightExpr());
    }

    public Object visit(BinaryLogicOrExpr node) {
        return layoutBinary(node, "Or", node.getLeftExpr(), node.getRightExpr());
    }

    // Other expressions

    public Object visit(UnaryNegExpr node) {
        return layoutUnary(node, "-", node.getExpr());
    }

    public Object visit(UnaryNotExpr node) {
        return layoutUnary(node, "!", node.getExpr());
    }

    public Object visit(UnaryIncrExpr node) {
        return layoutUnary(node, (node.isPostfix()?"Post":"Pre")+"++", node.getExpr());
    }

    public Object visit(UnaryDecrExpr node) {
        return layoutUnary(node, (node.isPostfix()?"Post":"Pre")+"--", node.getExpr());
    }

    public Object visit(ArrayExpr node) {
        if(node.getRef()!=null)
            return layoutBinary(node, "ArrayExpr " + node.getName(),node.getRef(),node.getIndex());
        else
            return layoutUnary(node, "ArrayExpr " + node.getName(),node.getIndex());
    }

    public Object visit(ConstIntExpr node) {
        return layoutNullary(node, "Int:" + node.getConstant());
    }

    public Object visit(ConstBooleanExpr node) {
        return layoutNullary(node, "Bool:" + node.getConstant());
    }

    public Object visit(ConstStringExpr node) {
        return layoutNullary(node, "Str:" + node.getConstant());
    }

    public Object visit(VarExpr node) {
        if(node.getRef() == null)
            return layoutNullary(node, "VarExpr " + node.getName());
        else
            return layoutUnary(node, "VarExpr " + node.getName(),node.getRef());
    }



    //-------- auxilliary methods ---------
    private DrawingTree layoutCaption(ASTNode node, String name)
    {
        int w = fontMetrics.stringWidth(name) + 14;
        int h = fontMetrics.getHeight() + 4;
        return new DrawingTree(node, name, w, h);
    }

    private DrawingTree layoutNullary(ASTNode node, String name)
    {
        DrawingTree dt = layoutCaption(node, name);
        dt.contour.upper_tail = new Polyline(0, dt.height + 2 * BORDER, null);
        dt.contour.upper_head = dt.contour.upper_tail;
        dt.contour.lower_tail = new Polyline(-dt.width - 2 * BORDER, 0, null);
        dt.contour.lower_head = new Polyline(0, dt.height + 2 * BORDER, dt.contour.lower_tail);
        return dt;
    }

    private DrawingTree layoutUnary(ASTNode node, String name, ASTNode child1)
    {
        DrawingTree dt = layoutCaption(node, name);
        DrawingTree d1 = (DrawingTree) child1.accept(this);
        dt.setChildren(new DrawingTree[]{d1});
        attachParent(dt, join(dt));
        return dt;
    }

    private DrawingTree layoutBinary(ASTNode node, String name, ASTNode child1, ASTNode child2)
    {
        DrawingTree dt = layoutCaption(node, name);
        DrawingTree d1 = (DrawingTree) child1.accept(this);
        DrawingTree d2 = (DrawingTree) child2.accept(this);
        dt.setChildren(new DrawingTree[]{d1, d2});
        attachParent(dt, join(dt));
        return dt;
    }

    private DrawingTree layoutTernary(ASTNode node, String name, ASTNode child1, ASTNode child2,
                                      ASTNode child3)
    {
        DrawingTree dt = layoutCaption(node, name);
        DrawingTree d1 = (DrawingTree) child1.accept(this);
        DrawingTree d2 = (DrawingTree) child2.accept(this);
        DrawingTree d3 = (DrawingTree) child3.accept(this);
        dt.setChildren(new DrawingTree[]{d1, d2, d3});
        attachParent(dt, join(dt));
        return dt;
    }

    private DrawingTree layoutQuaternary(ASTNode node, String name, ASTNode child1, ASTNode child2,
                                         ASTNode child3, ASTNode child4)
    {
        DrawingTree dt = layoutCaption(node, name);
        DrawingTree d1 = (DrawingTree) child1.accept(this); // returns a layoutNullary, for example
        DrawingTree d2 = (DrawingTree) child2.accept(this);
        DrawingTree d3 = (DrawingTree) child3.accept(this);
        DrawingTree d4 = (DrawingTree) child4.accept(this);
        dt.setChildren(new DrawingTree[]{d1, d2, d3, d4});
        attachParent(dt, join(dt));
        return dt;
    }

    private DrawingTree layoutQuintenary(ASTNode node, String name, ASTNode child1, ASTNode child2,
                                         ASTNode child3, ASTNode child4, ASTNode child5)
    {
        DrawingTree dt = layoutCaption(node, name);
        DrawingTree d1 = (DrawingTree) child1.accept(this);
        DrawingTree d2 = (DrawingTree) child2.accept(this);
        DrawingTree d3 = (DrawingTree) child3.accept(this);
        DrawingTree d4 = (DrawingTree) child4.accept(this);
        DrawingTree d5 = (DrawingTree) child5.accept(this);
        dt.setChildren(new DrawingTree[]{d1, d2, d3, d4, d5});
        attachParent(dt, join(dt));
        return dt;
    }

    private DrawingTree layoutNary(ASTNode node, String name, ListNode childNodes)
    {
        if(childNodes.getSize() == 0)
            return layoutNullary(node, "Empty" + name);
        DrawingTree dt = layoutCaption(node, name);
        DrawingTree[] childTrees = new DrawingTree[childNodes.getSize()];
        int i = 0;
        for(ASTNode childNode : childNodes) {
            childTrees[i] = (DrawingTree) childNode.accept(this);
            i++;
        }
        dt.setChildren(childTrees);
        attachParent(dt, join(dt));
        return dt;
    }

    private void attachParent(DrawingTree dt, int w)
    {
        int y = PARENT_SEP;
        int x2 = (w - dt.width) / 2 - BORDER;
        int x1 = x2 + dt.width + 2 * BORDER - w;

        dt.children[0].offset.y = y + dt.height;
        dt.children[0].offset.x = x1;
        dt.contour.upper_head = new Polyline(0, dt.height,
                new Polyline(x1, y, dt.contour.upper_head));
        dt.contour.lower_head = new Polyline(0, dt.height,
                new Polyline(x2, y, dt.contour.lower_head));
    }

    private int join(DrawingTree dt)
    {
        int w, sum;

        dt.contour = dt.children[0].contour;
        sum = w = dt.children[0].width + 2 * BORDER;

        for (int i = 1; i < dt.children.length; i++) {
            int d = merge(dt.contour, dt.children[i].contour);
            dt.children[i].offset.x = d + w;
            dt.children[i].offset.y = 0;
            w = dt.children[i].width + 2 * BORDER;
            sum += d + w;
        }
        return sum;
    }

    private int merge(Polygon c1, Polygon c2)
    {
        int x, y, total, d;
        Polyline lower, upper, b;

        x = y = total = 0;
        upper = c1.lower_head;
        lower = c2.upper_head;

        while (lower != null && upper != null) {
            d = offset(x, y, lower.dx, lower.dy, upper.dx, upper.dy);
            x += d;
            total += d;

            if (y + lower.dy <= upper.dy) {
                x += lower.dx;
                y += lower.dy;
                lower = lower.link;
            } else {
                x -= upper.dx;
                y -= upper.dy;
                upper = upper.link;
            }
        }

        if (lower != null) {
            b = bridge(c1.upper_tail, 0, 0, lower, x, y);
            c1.upper_tail = (b.link != null) ? c2.upper_tail : b;
            c1.lower_tail = c2.lower_tail;
        } else {
            b = bridge(c2.lower_tail, x, y, upper, 0, 0);
            if (b.link == null) {
                c1.lower_tail = b;
            }
        }

        c1.lower_head = c2.lower_head;

        return total;
    }

    private int offset(int p1, int p2, int a1, int a2, int b1, int b2)
    {
        int d, s, t;

        if (b2 <= p2 || p2 + a2 <= 0) {
            return 0;
        }

        t = b2 * a1 - a2 * b1;
        if (t > 0) {
            if (p2 < 0) {
                s = p2 * a1;
                d = s / a2 - p1;
            } else if (p2 > 0) {
                s = p2 * b1;
                d = s / b2 - p1;
            } else {
                d = -p1;
            }
        } else if (b2 < p2 + a2) {
            s = (b2 - p2) * a1;
            d = b1 - (p1 + s / a2);
        } else if (b2 > p2 + a2) {
            s = (a2 + p2) * b1;
            d = s / b2 - (p1 + a1);
        } else {
            d = b1 - (p1 + a1);
        }

        if (d > 0) {
            return d;
        } else {
            return 0;
        }
    }

    private Polyline bridge(Polyline line1, int x1, int y1,
                            Polyline line2, int x2, int y2)
    {
        int dy, dx, s;
        Polyline r;

        dy = y2 + line2.dy - y1;
        if (line2.dy == 0) {
            dx = line2.dx;
        } else {
            s = dy * line2.dx;
            dx = s / line2.dy;
        }

        r = new Polyline(dx, dy, line2.link);
        line1.link = new Polyline(x2 + line2.dx - dx - x1, 0, r);

        return r;
    }

}