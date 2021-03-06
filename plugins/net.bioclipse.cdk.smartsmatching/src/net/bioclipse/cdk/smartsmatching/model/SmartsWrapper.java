/*******************************************************************************
 * Copyright (c) 2009 Ola Spjuth.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ola Spjuth - initial API and implementation
 ******************************************************************************/
package net.bioclipse.cdk.smartsmatching.model;

import java.util.List;

import org.eclipse.ui.views.properties.IPropertySource;

import net.bioclipse.cdk.domain.CDKChemObject;
import net.bioclipse.cdk.domain.ISubStructure;
import net.bioclipse.core.domain.BioObject;

/**
 * Wraps a smarts string and holds a list of hits in a molecule
 * @author ola
 *
 */
public class SmartsWrapper extends BioObject{
    
    private String name;
    private String smartsString;
    private List<SmartsHit> hits;
    private IPropertySource propertySource;
    
    public SmartsWrapper(String name, String smartsString) {

        super();
        this.name = name;
        this.smartsString = smartsString;
    }

    public SmartsWrapper() {
    }

    public String getName() {
        return name;
    }
    
    public void setName( String name ) {
        this.name = name;
    }
    
    public String getSmartsString() {
        return smartsString;
    }
    
    public void setSmartsString( String smartsString ) {
        this.smartsString = smartsString;
    }
    
    @Override
    public Object getAdapter( Class adapter ) {

        if (adapter == IPropertySource.class){
            return propertySource!=null 
                ? propertySource : new SmartsWrapperPropertySource(this);
        }

        return super.getAdapter( adapter );
    }

    public void setHits( List<SmartsHit> hits ) {

        this.hits = hits;
    }

    public List<SmartsHit> getHits() {

        return hits;
    }
    
}
