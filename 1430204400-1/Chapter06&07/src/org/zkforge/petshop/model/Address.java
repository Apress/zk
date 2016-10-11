/* Copyright 2006 Sun Microsystems, Inc. All rights reserved. You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html
$Id: Address.java,v 1.10 2007/01/09 19:02:11 basler Exp $ */

package org.zkforge.petshop.model;

import org.zkforge.petshop.util.PetstoreUtil;

import java.util.ArrayList;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;

@Entity
public class Address implements java.io.Serializable {
	private static final long serialVersionUID = 20070323L;
	
	private Long addressID;
    private String street1;
    private String street2;
    private String city;
    private String state;
    private String zip;
    private double latitude;
    private double longitude;
    private static final String COMMA=", ";
    
    public Address() { }
    public Address(String street1, String street2, String city,
            String state, String zip, double latitude,
            double longitude){
        this.street1 = street1;
        this.street2 = street2;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    @TableGenerator(name="ADDRESS_ID_GEN",
    table="ID_GEN",
    pkColumnName="GEN_KEY",
    valueColumnName="GEN_VALUE",
    pkColumnValue="ADDRESS_ID",
    allocationSize=1)
    @GeneratedValue(strategy=GenerationType.TABLE,generator="ADDRESS_ID_GEN")
    @Id
    public Long getAddressID() {
        return addressID;
    }
    
    public String getStreet1() {
        return street1;
    }
    
    public String getStreet2() {
        return street2;
    }
    
    public String getCity() {
        return city;
    }
    
    public String getState() {
        return state;
    }
    public String getZip() {
        return zip;
    }
    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    
    public void setStreet1(String street1) {
        this.street1 = street1;
    }
    public void setStreet2(String street2) {
        this.street2 = street2;
    }
    public void setAddressID(Long addressID) {
        this.addressID = addressID;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public void setState(String state) {
        this.state = state;
    }
    public void setZip(String zip) {
        this.zip = zip;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public String addressToString() {
        StringBuffer sb=new StringBuffer();
        if(street1 != null) sb.append(street1);
        if(street2 != null && !street2.equals("")) sb.append(" " + street2);
        if(city != null) sb.append(COMMA + city);
        if(state != null) sb.append(COMMA + state);
        if(zip != null) sb.append(COMMA + zip);
        return sb.toString();
    }

    
    /**
     * This method checks to make sure the class values are valid
     *
     * @return Message(s) of validation errors or and empty array (zero length) if class is valid
     */
    public String[] validateWithMessage() {
        ArrayList<String> valMess=new ArrayList<String>();
        
        if(street1 == null || street1.equals("")) {
            valMess.add(PetstoreUtil.getMessage("invalid_address_street1"));
        }
        if(city == null || city.equals("")) {
            valMess.add(PetstoreUtil.getMessage("invalid_address_city"));
        }
        if(state == null || state.equals("")) {
            valMess.add(PetstoreUtil.getMessage("invalid_address_state"));
        }
        if(zip == null || zip.equals("")) {
            valMess.add(PetstoreUtil.getMessage("invalid_address_zip"));
        }
            
        return valMess.toArray(new String[valMess.size()]);
    }
    
}



