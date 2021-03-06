/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Arvid Berg
 *
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.handlers;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.openscience.cdk.controller.IChemModelRelay;
import org.openscience.cdk.controller.undoredo.IUndoRedoable;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.renderer.selection.AbstractSelection;
import org.openscience.cdk.tools.manipulator.ChemModelManipulator;

public class RemoveHandler extends AbstractJChemPaintHandler {
    Logger logger = Logger.getLogger(RemoveHandler.class);
    public Object execute(ExecutionEvent event) throws ExecutionException {

        IChemModelRelay relay = getChemModelRelay( event );
        if ( relay != null ) {

            Collection<?> selection = getSelection( event );

            IAtomContainer removedStuff=relay.getIChemModel().getBuilder().newAtomContainer();

            for(Object o:selection) {
                if( o instanceof IAdaptable) {
                    IAtom atom = (IAtom)((IAdaptable)o)
                                    .getAdapter( IAtom.class );

                    if(atom != null && ChemModelManipulator.getRelevantAtomContainer( relay.getIChemModel(), atom )!=null) {
                        Iterator<IBond> it = ChemModelManipulator.getRelevantAtomContainer( relay.getIChemModel(), atom ).getConnectedBondsList( atom ).iterator();
                        while(it.hasNext())
                            removedStuff.addBond(it.next());
                        removedStuff.addAtom( atom );
                        relay.removeAtomWithoutUndo( atom );
                        continue;
                    }
                    IBond bond = (IBond)((IAdaptable)o)
                                    .getAdapter( IBond.class );
                    if(bond != null) {
                        relay.removeBondWithoutUndo(  bond );
                        removedStuff.addBond( bond );
                        continue;
                    }
                }
                if(o instanceof IAtom) {
                    relay.removeAtomWithoutUndo( (IAtom )o);
                    removedStuff.addAtom( (IAtom )o );
                }
                else if(o instanceof IBond) {
                    relay.removeBondWithoutUndo( (IBond)o);
                    removedStuff.addBond( (IBond)o );
                }
            }
            relay.getRenderer()
            .getRenderer2DModel()
            .setSelection(AbstractSelection.EMPTY_SELECTION);

            if(relay.getUndoRedoFactory()!=null && relay.getUndoRedoHandler()!=null){
                IUndoRedoable undoredo = relay.getUndoRedoFactory().getRemoveAtomsAndBondsEdit( relay.getIChemModel(), removedStuff, "Delete",relay);
                relay.getUndoRedoHandler().postEdit(undoredo);
            }        }
        return null;
    }
}

