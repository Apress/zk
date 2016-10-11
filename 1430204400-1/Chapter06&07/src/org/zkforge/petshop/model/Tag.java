/* Copyright 2006 Sun Microsystems, Inc. All rights reserved. You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html
$Id: Tag.java,v 1.5 2007/01/03 23:11:26 inder Exp $ */

package org.zkforge.petshop.model;

import static javax.persistence.CascadeType.REMOVE;
import java.util.Collection;
import java.util.Vector;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.TableGenerator;

@Entity
public class Tag implements java.io.Serializable {

    private int tagID=-1;
    private Collection<Item> items= new Vector<Item>();
    private String tag;
    private int refCount=0;
      
    public Tag() {
    }
    
    public Tag(String Tag) {
        this.tag=Tag;
    }
    
    @TableGenerator(name="TAG_ID_GEN",
            table="ID_GEN",
            pkColumnName="GEN_KEY",
            valueColumnName="GEN_VALUE",
            pkColumnValue="TAG_ID",
            allocationSize=1)
    @GeneratedValue(strategy=GenerationType.TABLE,generator="TAG_ID_GEN")
    @Id
    public int getTagID() {
        return tagID;
    }
    public void setTagID(int tagID) {
        this.tagID=tagID;
    }

    
    public String getTag() {
        return tag;
    }
    public void setTag(String tag) {
        this.tag=tag;
    }
    
    public int getRefCount() {
        return refCount;
    }
    public void setRefCount(int refCount) {
        this.refCount=refCount;
    }
    public synchronized void incrementRefCount() {
        refCount++;
    }
    
    
    @ManyToMany(targetEntity=Item.class)
    @JoinTable(name = "TAG_ITEM", joinColumns = @JoinColumn(name = "TAGID", referencedColumnName = "TAGID")
    , inverseJoinColumns = @JoinColumn(name = "ITEMID", referencedColumnName = "ITEMID")
    )
    public Collection<Item> getItems() {
        return items;
    }
    public void setItems(Collection<Item> items) {
        this.items=items;
    }
    
    public boolean itemExists(Item item) {
        return this.getItems().contains(item);
    }
}



