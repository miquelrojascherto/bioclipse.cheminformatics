/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>.
 *
 * Contributors:
 *     Arvid Berg
 *
 ******************************************************************************/
package net.bioclipse.cdk.ui.views;

import net.bioclipse.cdk.domain.ICDKMolecule;

/**
 * @author arvid
 *
 */
public interface IMoleculesEditorModel {

    public ICDKMolecule getMoleculeAt(int index);

    public int getNumberOfMolecules();

    public void markDirty(int index,ICDKMolecule moleculeToSave);

    public void save();
}
