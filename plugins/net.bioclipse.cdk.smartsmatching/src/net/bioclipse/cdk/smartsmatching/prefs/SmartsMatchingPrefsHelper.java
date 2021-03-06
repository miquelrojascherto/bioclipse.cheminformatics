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
package net.bioclipse.cdk.smartsmatching.prefs;

import java.util.ArrayList;
import java.util.List;

import net.bioclipse.cdk.smartsmatching.Activator;
import net.bioclipse.cdk.smartsmatching.model.SmartsWrapper;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;


public class SmartsMatchingPrefsHelper {
    
    private static final Logger logger = Logger.getLogger(SmartsMatchingPrefsHelper.class);

    public static final String SMARTSMATCHING_PREFS_SMARTS = "SmartsMatchingSmarts";
    public static final String PREFS_DELIMITER = "-SP-";
    public static final String PREFS_PART_DELIMITER = "-SPSP-";

    public static List<SmartsWrapper> getPreferences(){

        IPreferenceStore store=Activator.getDefault().getPreferenceStore();

        String entireString=store.getString( SMARTSMATCHING_PREFS_SMARTS );

        logger.debug("Read Smarts prefs string: " + entireString);
        
        List<SmartsWrapper> retlist = splitPrefsString( entireString);

        //If no hits, initialize default ones.
        if (retlist.size()<=0){
            logger.debug( "No SMARTS could be read, initializing default prefs." );
            entireString=store.getDefaultString( SMARTSMATCHING_PREFS_SMARTS );
            logger.debug("Read default Smarts prefs string: " + entireString);

            //Store def val
            store.setValue( SMARTSMATCHING_PREFS_SMARTS, entireString );

            //Try again
            retlist = splitPrefsString( entireString);

        }
        
        return retlist;

    }

    private static List<SmartsWrapper> splitPrefsString( String entireString) {

        List<SmartsWrapper> rtlst=new ArrayList<SmartsWrapper>();

        //Split in parts
        String[] ret=entireString.split(PREFS_DELIMITER);
        for (int i = 0; i < ret.length; i++) {
            logger.debug("Part " + i + " extracted: " + ret[i]);

            String[] subparts = ret[i].split(PREFS_PART_DELIMITER);

          if (subparts.length==2){
              SmartsWrapper sw=new SmartsWrapper(subparts[0], subparts[1]);
              rtlst.add(sw);
          }else{
              logger.error( "SmartsMatchingPrefs part: " + ret[i] + "could not " +
                  "be parsed into name and smartsstring. Skipped.");
          }
        }
        
        return rtlst;
    }
    
    public static void setPreferences(List<SmartsWrapper> smartsList){

        IPreferenceStore store=Activator.getDefault().getPreferenceStore();
        
        String prefsToSave="";
        for (SmartsWrapper sw : smartsList){
            if (sw!=null){
                if (sw.getName()==null || "".equals( sw.getName())){
                    logger.error( "SmartsWrapper has no name: " + sw.getName());
                }else{
                    if (sw.getSmartsString()==null || "".equals( sw.getSmartsString())){
                        logger.error( "SmartsWrapper has no SMARST: " + sw.getSmartsString());
                    }else{
                        String substr=sw.getName()+PREFS_PART_DELIMITER + sw.getSmartsString();
                        prefsToSave=prefsToSave + substr + PREFS_DELIMITER;
                    }
                }
            }
        }
        
        //Remove trailing delimiter
        if (prefsToSave.length()>4)
            prefsToSave=prefsToSave.substring( 0, prefsToSave.length()-(PREFS_DELIMITER.length()));
        
        logger.debug( "Saving smartsmatching prefs: " + prefsToSave );

        store.setValue(  SMARTSMATCHING_PREFS_SMARTS , prefsToSave);
        
        Activator.getDefault().savePluginPreferences();

    }
}
