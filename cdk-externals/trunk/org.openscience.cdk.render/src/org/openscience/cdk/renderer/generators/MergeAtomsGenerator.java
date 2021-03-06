/* $Revision$ $Author$ $Date$
 *
 *  Copyright (C) 2009  Stefan Kuhn
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
package org.openscience.cdk.renderer.generators;


import java.awt.Color;

import javax.vecmath.Point2d;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.elements.OvalElement;

/**
 * @cdk.module render
 */
public class MergeAtomsGenerator extends BasicAtomGenerator 
                                implements IGenerator {

    public MergeAtomsGenerator() {}
    
    public IRenderingElement generate(IAtomContainer ac, RendererModel model) {
    	ElementGroup selectionElements = new ElementGroup();
    	for(IAtom atom : model.getMerge().keySet()){
    		Point2d p = atom.getPoint2d();
            
            // the element size has to be scaled to model space 
            // so that it can be scaled back to screen space...
            double radius = model.getHighlightDistance() / model.getScale();
            Color highlightColor = model.getHoverOverColor(); 
            selectionElements.add(new OvalElement(p.x, p.y, radius*2, false, highlightColor));
        }
        
        return selectionElements;
    }
}
