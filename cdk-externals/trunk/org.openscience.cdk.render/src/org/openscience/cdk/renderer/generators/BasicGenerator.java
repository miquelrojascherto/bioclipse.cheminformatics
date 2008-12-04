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

package org.openscience.cdk.renderer.generators;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.elements.ElementGroup;
import org.openscience.cdk.renderer.elements.IRenderingElement;

/**
 * @cdk.module render
 */
public class BasicGenerator {
	
	private BasicAtomGenerator atomGenerator;
	private BasicBondGenerator bondGenerator;
	
	public BasicGenerator(RendererModel model) {
		this.atomGenerator = new BasicAtomGenerator(model);
		this.bondGenerator = new BasicBondGenerator(model);
	}
	
	public IRenderingElement generate(IAtomContainer ac) {
		ElementGroup diagram = new ElementGroup();
		diagram.add(this.bondGenerator.generate(ac));
		diagram.add(this.atomGenerator.generate(ac));
		return diagram;
	}

}
