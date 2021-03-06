 /*******************************************************************************
 * Copyright (c) 2008 The Bioclipse Project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ola Spjuth
 *     Stefan Kuhn
 *
 ******************************************************************************/
package net.bioclipse.webservices.business;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;

import net.bioclipse.cdk.domain.ICDKMolecule;
import net.bioclipse.core.PublishedClass;
import net.bioclipse.core.PublishedMethod;
import net.bioclipse.core.Recorded;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.managers.business.IBioclipseManager;

@PublishedClass( "Contains webservices related methods")
public interface IWebservicesManager extends IBioclipseManager {

	/**
     * Retrieves a PDB entry with the WSDbfetch EBI Web service.
     * The result is dumped in the Webservice's result folder.
     * @param pdbid The entry identifier
     * @return A path to the saved file
     * @throws BioclipseException
     * @throws CoreException
     */
    @Recorded
    @PublishedMethod( params = "String pdbid, String filename", 
                      methodSummary = "Fetches a PDB entry with ID='pdbid' from " +
                      		"the EBI databases " +
                      		"and saves it")
    public String downloadPDBAsFile(String pdbid)
        throws BioclipseException, CoreException;
	
	/**
     * Retrieves a PDB entry with the WSDbfetch EBI Web service.
     * The result is dumped in the Webservice's result folder.
     * @param pdbid The entry identifier
     * @param filename The name of the file to create
     * @return A path to the saved file
     * @throws BioclipseException
     * @throws CoreException
     */
    @Recorded
    @PublishedMethod( params = "String pdbid, String filename", 
                      methodSummary = "Fetches a PDB entry with ID='pdbid' from " +
                      		"the EBI databases " +
                      		"and saves it under the specified filename")
    public String downloadPDBAsFile(String pdbid, String filename)
        throws BioclipseException, CoreException;
    
    /**
     * Retrieves a PDB entry with the WSDbfetch EBI Web service.
     * @param pdbid The entry identifier
     * @return The PDB parsed into an ICDKMolecule
     * @throws BioclipseException
     * @throws CoreException
     * @throws IOException 
     */
    public ICDKMolecule downloadPDB(String pdbid)
    	throws BioclipseException, CoreException, IOException;

    /**
     * Retrieves an entry from a database with the WSDbfetch EBI Web service.
     * The result is dumped in the Webservice's result folder.
     * @param db The database identifier
     * @param query The entry identifier
     * @param format The format
     * @param filename The name of the file to create
     * @return The resource that contains the result
     * @throws BioclipseException
     * @throws CoreException
     */
    @Recorded
    @PublishedMethod( params = "String db, String query, String format, String filename", 
                      methodSummary = "Fetches an entry from the EBI databases and saves it under the specified filename")
    public String downloadDbEntryAsFile(String db, String query,
    		String format, String filename)
		throws BioclipseException, CoreException;
    
    /**
     * Retrieves an entry from a database with the WSDbfetch EBI Web service.
     * The result is dumped in the Webservice's result folder.
     * @param db The database identifier
     * @param query The entry identifier
     * @param format The format
     * @return The resource that contains the result
     * @throws BioclipseException
     * @throws CoreException
     */
    @Recorded
    @PublishedMethod( params = "String db, String query, String format", 
                      methodSummary = "Fetches an entry from the EBI databases and saves it")
    public String downloadDbEntryAsFile(String db, String query,
    		String format)
		throws BioclipseException, CoreException;
    
    /**
     * Retrieves an entry from a database with the WSDbfetch EBI Web service. 
     * The result is dumped in the virtual folder.
     * @param db The database identifier
     * @param query The entry identifier
     * @param format The format
     * @return The database entry
     * @throws BioclipseException
     * @throws CoreException
     */
    @Recorded
    @PublishedMethod( params = "String db, String query, String format", 
                      methodSummary = "Fetches an entry from the EBI databases")
    public String downloadDbEntry(String db, String query, String format)
		throws BioclipseException, CoreException;
}
