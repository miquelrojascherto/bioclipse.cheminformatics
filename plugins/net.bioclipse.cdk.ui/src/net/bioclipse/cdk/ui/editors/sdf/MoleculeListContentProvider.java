/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth
 *     
 ******************************************************************************/
package net.bioclipse.cdk.ui.editors.sdf;

import net.bioclipse.cdk.domain.CDKMoleculeList;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class MoleculeListContentProvider implements IStructuredContentProvider {

	public Object[] getElements(Object inputElement) {
		
		if (inputElement instanceof CDKMoleculeList) {
			CDKMoleculeList moleculeList = (CDKMoleculeList) inputElement;
			return moleculeList.toArray();
		}
		
		return new Object[0];
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub
		
	}


}
