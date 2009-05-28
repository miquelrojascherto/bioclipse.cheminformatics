/*******************************************************************************
 * Copyright (c) 2009  Arvid Berg <goglepox@users.sourceforge.net>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * <http://www.eclipse.org/legal/epl-v10.html>
 *
 * Contact: http://www.bioclipse.net/
 ******************************************************************************/
package net.bioclipse.cdk.ui.sdfeditor.business;

import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.LinkedList;
import java.util.List;

import net.bioclipse.cdk.ui.sdfeditor.editor.SDFIndexEditorModel;
import net.bioclipse.managers.business.IBioclipseManager;

import org.apache.log4j.Logger;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;


public class MoleculeTableManager implements IBioclipseManager {

    Logger logger = Logger.getLogger( MoleculeTableManager.class );

    public String getManagerName() {
        return "molTable";
    }

    public void dummy(String... strings) {
        logger.info( "Dummy on molTable manager has been called with thees argumenst "+strings.toString() );
    }

    public SDFileIndex createSDFIndex(IFile file, IProgressMonitor monitor) {

        SubMonitor progress = SubMonitor.convert( monitor ,100);
        long size = -1;
        try {
            size = EFS.getStore( file.getLocationURI() )
                      .fetchInfo().getLength();
            progress.beginTask( "Parsing SDFile",
                                (int)size);

        }catch (CoreException e) {
            logger.debug( "Failed to get size of file" );
            progress.beginTask( "Parsing SDFile", IProgressMonitor.UNKNOWN );
        }
        long tStart = System.nanoTime();
        List<Long> values = new LinkedList<Long>();
        List<Long> propPos = new LinkedList<Long>();
        int num = 0;
        long pos = 0;
        long start = 0;
        int work = 0;

        try {
            ReadableByteChannel fc = Channels.newChannel( file.getContents() );
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect( 200 );
            int dollarCount = 0;
            boolean firstInLine = true;
            int bytesRead = 0;
            int c;
            while( bytesRead >=0) {
                byteBuffer.rewind();
                bytesRead = fc.read( byteBuffer );
                byteBuffer.rewind();
                for(;bytesRead >0;bytesRead--) {
                    c = byteBuffer.get();
                    pos++;

                    if( c == '\n') {
                        firstInLine = true;
                        if(dollarCount==4) {
                            work = (int) start;
                            start = pos;
                            num++;
                            values.add( start );
                            dollarCount = 0;
                            // progress code
                            progress.worked( (int) (pos-work) );
                            if(size >-1) {
                                progress.subTask(
                                   String.format( "Read: %dMB\\%dMB",
                                   pos/(1048576),size/(1048576)));
                            }else {
                                progress.subTask(
                                   String.format( "Read: %dMB",
                                   pos/(1048576)));
                            }
                            if ( monitor.isCanceled() ) {
                                throw new OperationCanceledException();
                            }
                            // clear builder for the next line
                        }
                    }else if(c == '\r') continue;
                    else if(c == '$') dollarCount++;
                    else if(c== '>' && firstInLine) {
                        propPos.add( pos );
                        firstInLine = false;
                    }else {
                        firstInLine=false;
                        dollarCount = 0;
                    }
                }
            }

            if( (pos-start)>3) {
                values.add(pos);
                num++;
            }
            fc.close();
        }
        catch (Exception exception) {
            // ok, I give up...
            logger.debug( "Could not create index: "
                          + exception.getClass().getSimpleName() + " : "
                          + exception.getMessage(),
                          exception );
        }
        logger.debug( String.format(
                          "createSDFIndex took %d to complete",
                          (int)((System.nanoTime()-tStart)/1e6)) );
        progress.done();
        return new SDFileIndex(file,values);
    }

    public void calculateProperty( SDFIndexEditorModel model,
                                   IPropertyCalculator<?> calculator,
                                   IProgressMonitor monitor) {
        monitor.beginTask( "Calculating properties", model.getNumberOfMolecules() );
        for(int i=0;i<model.getNumberOfMolecules();i++) {
            model.setPropertyFor( i, calculator.getPropertyName(),
                                  calculator.calculate( model.getMoleculeAt( i ) ) );
            monitor.worked( 1 );
            if(i%10 == 0) {
                monitor.subTask( String.format( "%d/%d", i+1
                                             ,model.getNumberOfMolecules() ) );
            }
        }
        monitor.done();
    }
}