/* Copyright 2006 Sun Microsystems, Inc. All rights reserved. You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html
$Id: ZipLocation.java,v 1.4 2006/05/05 20:15:25 inder Exp $ */

package org.zkforge.petshop.model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * This class represents the data used for autocomplete of a 
 * user input for zipcode, city, state.
*/
@Entity
public class ZipLocation implements java.io.Serializable {

    private int zipCode;
    private String city;
    private String state;
         
    public ZipLocation() { }   

    @Id
    public int getZipCode() {
        return zipCode;
    }
    
    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public void setZipCode(int zipCode) {
        this.zipCode = zipCode;
    }
    
    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }
  }



