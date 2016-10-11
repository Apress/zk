/* Copyright 2006 Sun Microsystems, Inc. All rights reserved. You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html
$Id: Category.java,v 1.5 2006/05/05 20:15:24 inder Exp $ */

package org.zkforge.petshop.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity

public class Category implements java.io.Serializable {
    
    private String categoryID;
    private String name;
    private String description;
    private String imageURL;
    
    public Category() { }
    
    @Id
    public String getCategoryID() {
        return categoryID;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getImageURL() {
        return imageURL;
    }
    
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
    public void setCategoryID(String categoryID) {
        this.categoryID = categoryID;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    
}



