/* $Revision$ $Author$ $Date$
*
*  Copyright (C) 2008 Gilleain Torrance <gilleain.torrance@gmail.com>
*
*  Contact: cdk-devel@list.sourceforge.net
*
*  This program is free software; you can redistribute it and/or
*  modify it under the terms of the GNU Lesser General Public License
*  as published by the Free Software Foundation; either version 2.1
*  of the License, or (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU Lesser General Public License for more details.
*
*  You should have received a copy of the GNU Lesser General Public License
*  along with this program; if not, write to the Free Software
*  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
*/

package org.openscience.cdk.renderer.visitor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ArrowElement;
import org.openscience.cdk.renderer.elements.AtomSymbolElement;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.LineElement;
import org.openscience.cdk.renderer.elements.OvalElement;
import org.openscience.cdk.renderer.elements.PathElement;
import org.openscience.cdk.renderer.elements.RectangleElement;
import org.openscience.cdk.renderer.elements.TextElement;
import org.openscience.cdk.renderer.elements.WedgeLineElement;
import org.openscience.cdk.renderer.font.AWTFontManager;
import org.openscience.cdk.renderer.font.IFontManager;


/**
 * @cdk.module render
 */
public class AWTDrawVisitor extends AbstractAWTDrawVisitor {
	
    /**
     * The font manager cannot be set by the constructor as it needs to
     * be managed by the Renderer.
     */
    private AWTFontManager fontManager;

    /**
     * The renderer model cannot be set by the constructor as it needs to
     * be managed by the Renderer.
     */
	private RendererModel rendererModel;
	
	private final Map<Integer, BasicStroke> strokeMap = 
	    new HashMap<Integer, BasicStroke>();
	
	private final Graphics2D g;
	
	public AWTDrawVisitor(Graphics2D g) {
		this.g = g;
		this.fontManager = null;
		this.rendererModel = null;
	}
	
	public void visitElementGroup(ElementGroup elementGroup) {
		elementGroup.visitChildren(this);
	}
	
    public void visit(ElementGroup elementGroup) {
        elementGroup.visitChildren(this);
    }

    public void visit(ArrowElement line) {
        Stroke savedStroke = this.g.getStroke();
        
        int w = (int) (line.width * this.rendererModel.getScale());
        if (strokeMap.containsKey(w)) {
            this.g.setStroke(strokeMap.get(w));
        } else {
            BasicStroke stroke = new BasicStroke(w);
            this.g.setStroke(stroke);
            strokeMap.put(w, stroke);
        }
        
        this.g.setColor(line.color);
        int[] a = this.transformPoint(line.x1, line.y1);
        int[] b = this.transformPoint(line.x2, line.y2);
        this.g.drawLine(a[0], a[1], b[0], b[1]);
        double aW = rendererModel.getArrowHeadWidth() / rendererModel.getScale();
        if (line.direction) {
            int[] c = this.transformPoint(line.x1 - aW, line.y1 - aW);
            int[] d = this.transformPoint(line.x1 - aW, line.y1 + aW);
            this.g.drawLine(a[0], a[1], c[0], c[1]);
            this.g.drawLine(a[0], a[1], d[0], d[1]);
        } else {
            int[] c = this.transformPoint(line.x2 + aW, line.y2 - aW);
            int[] d = this.transformPoint(line.x2 + aW, line.y2 + aW);
            this.g.drawLine(b[0], b[1], c[0], c[1]);
            this.g.drawLine(b[0], b[1], d[0], d[1]);
        }        
        this.g.setStroke(savedStroke);
    }
    
    
    public void visit(LineElement line) {
        Stroke savedStroke = this.g.getStroke();
        
        int w = (int) (line.width * this.rendererModel.getScale());
        if (strokeMap.containsKey(w)) {
            this.g.setStroke(strokeMap.get(w));
        } else {
            BasicStroke stroke = new BasicStroke(w);
            this.g.setStroke(stroke);
            strokeMap.put(w, stroke);
        }
        
        this.g.setColor(line.color);
        int[] a = this.transformPoint(line.x1, line.y1);
        int[] b = this.transformPoint(line.x2, line.y2);
        this.g.drawLine(a[0], a[1], b[0], b[1]);
        
        this.g.setStroke(savedStroke);
    }

    public void visit(OvalElement oval) {
        this.g.setColor(oval.color);
        int[] min = 
            this.transformPoint(oval.x - oval.radius, oval.y - oval.radius);
        int[] max = 
            this.transformPoint(oval.x + oval.radius, oval.y + oval.radius);
        int w = max[0] - min[0];
        int h = max[1] - min[1];
        if (oval.fill) {
            this.g.fillOval(min[0], min[1], w, h);
        } else {
            this.g.drawOval(min[0], min[1], w, h);
        }
    }

    public void visit(TextElement textElement) {
        this.g.setFont(this.fontManager.getFont());
        Point p = this.getTextBasePoint(
                textElement.text, textElement.x, textElement.y, g);
        Rectangle2D textBounds =
                this.getTextBounds(
                        textElement.text, textElement.x, textElement.y, g);
        
        this.g.setColor(this.rendererModel.getBackColor());
        this.g.fill(textBounds);
        this.g.setColor(textElement.color);
        this.g.drawString(textElement.text, p.x, p.y);
    }
    
    public void visit(WedgeLineElement wedge) {
        // make the vector normal to the wedge axis
        Vector2d normal = 
            new Vector2d(wedge.y1 - wedge.y2, wedge.x2 - wedge.x1);
        normal.normalize();
        normal.scale(rendererModel.getWedgeWidth() / rendererModel.getScale());  
        
        // make the triangle corners
        Point2d vertexA = new Point2d(wedge.x1, wedge.y1);
        Point2d vertexB = new Point2d(wedge.x2, wedge.y2);
        Point2d vertexC = new Point2d(vertexB);
        vertexB.add(normal);
        vertexC.sub(normal);
        this.g.setColor(wedge.color);
        if (wedge.isDashed) {
            this.drawDashedWedge(vertexA, vertexB, vertexC);
        } else {
            this.drawFilledWedge(vertexA, vertexB, vertexC);
        }
    }
    
    private void drawFilledWedge(
            Point2d vertexA, Point2d vertexB, Point2d vertexC) {
        int[] pB = this.transformPoint(vertexB.x, vertexB.y);
        int[] pC = this.transformPoint(vertexC.x, vertexC.y);
        int[] pA = this.transformPoint(vertexA.x, vertexA.y);
        
        int[] xs = new int[] { pB[0], pC[0], pA[0] };
        int[] ys = new int[] { pB[1], pC[1], pA[1] };
        this.g.fillPolygon(xs, ys, 3);
    }
    
    private void drawDashedWedge(
            Point2d vertexA, Point2d vertexB, Point2d vertexC) {
        // store the current stroke
        Stroke storedStroke = this.g.getStroke();
        this.g.setStroke(new BasicStroke(1));
        
        // calculate the distances between lines
        double distance = vertexB.distance(vertexA);
        double gapFactor = 0.1;
        double gap = distance * gapFactor;
        double numberOfDashes = distance / gap;
        double d = 0;
        
        // draw by interpolating along the edges of the triangle
        for (int i = 0; i < numberOfDashes; i++) {
            Point2d p1 = new Point2d();
            p1.interpolate(vertexA, vertexB, d);
            Point2d p2 = new Point2d();
            p2.interpolate(vertexA, vertexC, d);
            int[] p1T = this.transformPoint(p1.x, p1.y);
            int[] p2T = this.transformPoint(p2.x, p2.y);
            this.g.drawLine(p1T[0], p1T[1], p2T[0], p2T[1]);
            if (distance * (d + gapFactor) >= distance) {
                break;
            } else {
                d += gapFactor;
            }
        }
        this.g.setStroke(storedStroke);
    }
    
    public void visit(AtomSymbolElement atomSymbol) {
        this.g.setFont(this.fontManager.getFont());
        Point p = 
            super.getTextBasePoint(
                    atomSymbol.text, atomSymbol.x, atomSymbol.y, g);
        Rectangle2D textBounds = 
            this.getTextBounds(atomSymbol.text, atomSymbol.x, atomSymbol.y, g);
        this.g.setColor(this.rendererModel.getBackColor());
        this.g.fill(textBounds);
        this.g.setColor(atomSymbol.color);
        this.g.drawString(atomSymbol.text, p.x, p.y);
        
        int offset = 10;    // XXX
        String chargeString;
        if (atomSymbol.formalCharge == 0) {
            return;
        } else if (atomSymbol.formalCharge == 1) {
            chargeString = "+";
        } else if (atomSymbol.formalCharge > 1) {
            chargeString = atomSymbol.formalCharge + "+";
        } else if (atomSymbol.formalCharge == -1) {
            chargeString = "-";
        } else if (atomSymbol.formalCharge < -1) {
            int absCharge = Math.abs(atomSymbol.formalCharge);
            chargeString = absCharge + "-";
        } else {
            return;
        }
       
        int x = (int) textBounds.getCenterX();
        int y = (int) textBounds.getCenterY();
        if (atomSymbol.alignment == 1) {           // RIGHT
            this.g.drawString(
                    chargeString, x + offset, (int)textBounds.getMinY());
        } else if (atomSymbol.alignment == -1) {   // LEFT
            this.g.drawString(
                    chargeString, x - offset, (int)textBounds.getMinY());
        } else if (atomSymbol.alignment == 2) {    // TOP
            this.g.drawString(
                    chargeString, x, y - offset);
        } else if (atomSymbol.alignment == -2) {   // BOT
            this.g.drawString(
                    chargeString, x, y + offset);
        }
        
    }
    
    public void visit(RectangleElement rectangle) {
        int[] p1 = this.transformPoint(rectangle.x, rectangle.y);
        int[] p2 = this.transformPoint(
                rectangle.x + rectangle.width, rectangle.y + rectangle.height);
        this.g.setColor(rectangle.color);
        if (rectangle.filled) {
            this.g.fillRect(p1[0], p1[1], p2[0] - p1[0], p2[1] - p1[1]);
        } else {
            this.g.drawRect(p1[0], p1[1], p2[0] - p1[0], p2[1] - p1[1]);
        }
    }
    
    public void visit(PathElement path) {
        this.g.setColor(path.color);
        for (int i = 1; i < path.points.size(); i++) {
            Point2d point1 = path.points.get(i - 1);
            Point2d point2 = path.points.get(i);
            int[] p1 = this.transformPoint(point1.x, point1.y);
            int[] p2 = this.transformPoint(point2.x, point2.y);
            this.g.drawLine(p1[0], p1[1], p2[0], p2[1]);
        }
    }

    public void visit(IRenderingElement element) {
        Color savedColor = this.g.getColor();
        if (element instanceof ElementGroup)
            visit((ElementGroup) element);
        else if (element instanceof WedgeLineElement)
            visit((WedgeLineElement) element);
        else if (element instanceof LineElement)
            visit((LineElement) element);
        else if (element instanceof ArrowElement)
            visit((ArrowElement) element);
        else if (element instanceof OvalElement)
            visit((OvalElement) element);
        else if (element instanceof AtomSymbolElement)
            visit((AtomSymbolElement) element);
        else if (element instanceof TextElement)
            visit((TextElement) element);
        else if (element instanceof RectangleElement)
            visit((RectangleElement) element);
        else if (element instanceof PathElement)
            visit((PathElement) element);
        else
            System.err.println("Visitor method for "
                    + element.getClass().getName() + " is not implemented");
        this.g.setColor(savedColor);
    }

    /**
     * The font manager must be set by any renderer that uses this class! 
     */
    public void setFontManager(IFontManager fontManager) {
        this.fontManager = (AWTFontManager) fontManager;
    }

    public void setRendererModel(RendererModel rendererModel) {
        this.rendererModel = rendererModel;
        if (rendererModel.getUseAntiAliasing()) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
//            g.setStroke(new BasicStroke((int)rendererModel.getBondWidth()));
        }
    }
}
