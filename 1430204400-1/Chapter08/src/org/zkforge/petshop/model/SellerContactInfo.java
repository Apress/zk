/* Copyright 2006 Sun Microsystems, Inc. All rights reserved. You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html
$Id: SellerContactInfo.java,v 1.5 2007/01/09 19:02:11 basler Exp $ */

package org.zkforge.petshop.model;

import org.zkforge.petshop.util.PetstoreUtil;

import java.util.ArrayList;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;

@Entity        
public class SellerContactInfo implements java.io.Serializable {
    
    private Long contactInfoID;
    private String lastName;
    private String firstName;
    private String email;
    
    public SellerContactInfo() { }
    public SellerContactInfo(String firstName, String lastName,
            String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    
    @TableGenerator(name="CONTACTINFO_ID_GEN",
    table="ID_GEN",
    pkColumnName="GEN_KEY",
    valueColumnName="GEN_VALUE",
    pkColumnValue="CONTACT_INFO_ID",
    allocationSize=1)
    @GeneratedValue(strategy=GenerationType.TABLE,generator="CONTACTINFO_ID_GEN")
    @Id
    public Long getContactInfoID() {
        return contactInfoID;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    public void setContactInfoID(Long contactInfoID) {
        this.contactInfoID = contactInfoID;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    
    /**
     * This method checks to make sure the class values are valid
     *
     * @return Message(s) of validation errors or and empty array (zero length) if class is valid
     */
    public String[] validateWithMessage() {
        ArrayList<String> valMess=new ArrayList<String>();
        
        // make sure make and address is entered
        if(firstName == null || firstName.equals("")) {
            // price should be a number
            valMess.add(PetstoreUtil.getMessage("invalid_contact_firstname"));
        }
        if(lastName == null || lastName.equals("")) {
            valMess.add(PetstoreUtil.getMessage("invalid_contact_lastname"));
        }
            
        return valMess.toArray(new String[valMess.size()]);
    }
    
}



