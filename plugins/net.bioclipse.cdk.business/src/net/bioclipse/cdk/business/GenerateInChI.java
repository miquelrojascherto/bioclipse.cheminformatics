/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth - ospjuth@sourceforge.net
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.cdk.business;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bioclipse.cdk.domain.CDKMoleculeSelectionHelper;
import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.inchi.ui.InChIDialog;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class GenerateInChI extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {

		ISelection sel =
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
			.getSelectionService().getSelection();

		List<ICDKMolecule> mols=CDKMoleculeSelectionHelper.
		getMoleculesFromSelection(sel);

		if (mols==null || mols.size()<=0){
			System.out.println("No mols in list, exiting Inchi generation");

			Shell shell=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			InChIDialog.openInformation(shell, "InChI",
					"No molecules in selection.");
			return null;
		}
		
		Map<String,String> inchiMolPairs = new HashMap<String, String>();
		StringBuffer buffer=new StringBuffer(256);
		buffer.append( "Inchi generation for " + mols.size()
		             + " molecule"+(mols.size()<=1?"":"s")+":\n\n");
		for (ICDKMolecule cdkmol : mols){
			String ret;
			try {
				ret = cdkmol.getInChI(
				    net.bioclipse.core.domain.IMolecule
				        .Property.USE_CACHED_OR_CALCULATED
				);
			} catch (Exception e) {
				ret="ERROR: " + e.getMessage();
			}
			inchiMolPairs.put( cdkmol.toString(), ret );
			//buffer.append("  * " + cdkmol.toString() + " -- " + ret + "\n");
		}
		
		System.out.println(buffer.toString());

		Shell shell=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		InChIDialog.openInformation(shell, "InChI", inchiMolPairs,buffer.toString());

		System.out.println("INCHI generation ended");

		return null;
	}

}
