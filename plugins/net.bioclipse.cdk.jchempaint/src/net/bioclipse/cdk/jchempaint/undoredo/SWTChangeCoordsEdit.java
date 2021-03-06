/*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Kuhn
 *
 ******************************************************************************/
package net.bioclipse.cdk.jchempaint.undoredo;

import java.util.Map;

import javax.vecmath.Point2d;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.openscience.cdk.controller.undoredo.ChangeCoordsEdit;
import org.openscience.cdk.interfaces.IAtom;

/**
 * A swing undo-redo implementation for clean up
 */
public class SWTChangeCoordsEdit extends ChangeCoordsEdit implements
        IUndoableOperation {

    private IUndoContext context;

    public SWTChangeCoordsEdit(Map<IAtom, Point2d[]> atomCoordsMap,
            String type, IUndoContext context) {

        super( atomCoordsMap, type );
        this.context = context;
    }

    public IStatus redo( IProgressMonitor monitor, IAdaptable info )
                                                                    throws ExecutionException {

        super.redo();
        return Status.OK_STATUS;
    }

    public void removeContext( IUndoContext context ) {

        // TODO Auto-generated method stub

    }

    public IStatus undo( IProgressMonitor monitor, IAdaptable info )
                                                                    throws ExecutionException {

        super.undo();
        return Status.OK_STATUS;
    }

    public IStatus execute( IProgressMonitor monitor, IAdaptable info )
                                                                       throws ExecutionException {

        // TODO Auto-generated method stub
        return Status.OK_STATUS;
    }

    public void addContext( IUndoContext context ) {

    }

    public boolean canExecute() {

        // TODO Auto-generated method stub
        return false;
    }

    public void dispose() {

        // TODO Auto-generated method stub

    }

    public IUndoContext[] getContexts() {

        return new IUndoContext[] { this.context };
    }

    public String getLabel() {
        return super.getPresentationName();
    }

    public boolean hasContext( IUndoContext context ) {

        // TODO Auto-generated method stub
        return context.matches( this.context );
    }

}
