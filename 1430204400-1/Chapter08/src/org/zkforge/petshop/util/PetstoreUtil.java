/* Copyright 2006 Sun Microsystems, Inc. All rights reserved. You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html
$Id: PetstoreUtil.java,v 1.7 2007/01/11 22:33:21 basler Exp $ */

package org.zkforge.petshop.util;

import java.io.Closeable;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author basler
 */

public class PetstoreUtil {
    
    private static final Logger _logger=getBaseLogger();
    private static final PropertyResourceBundle _resBundle=getBaseBundle();
    
    
    /** Creates a new instance of PetstoreUtil */
    public PetstoreUtil() {
    }
    
    
    public static Logger getLogger() {
        return _logger;
    }
    
    /**
     * This method returns the default logger for the petstore application
     *
     * @return Default Logger for petstore application
     */
    private static Logger getBaseLogger() {
        return Logger.getLogger(PetstoreConstants.PETSTORE_BASE_LOGGER, PetstoreConstants.PETSTORE_BASE_LOG_STRINGS);
    }
    
    private static PropertyResourceBundle getBaseBundle() {
        try {
            return new PropertyResourceBundle(PetstoreUtil.class.getResourceAsStream("MessageStrings.properties"));
        } catch(IOException io) {
            getLogger().log(Level.WARNING, "resource_bundle_does_not_exist", io);
            return null;
        }
        
    }
    
    
    public static String getMessage(String key) {
        return getMessage(key, (Object[])null);
    }
    
    
    /**
     * This method uses the default message strings property file to resolve
     * resultant string to show to an end user
     * @param Key to use in MessageString.properties file
     *
     * @return Formated message for external display
     */
    public static String getMessage(String key, Object... arguments) {
        String sxRet=null;
        // get resource bundle and retrive message
        sxRet=_resBundle.getString(key);
        
        // see if the message needs to be formatted
        if(arguments != null) {
            // format message
            sxRet=MessageFormat.format(sxRet, arguments);
        }
        return sxRet;
    }
    
    
    public static void closeIgnoringException(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException ex) {
                // There is nothing we can do if close fails
            }
        }
    }
    
    
}
