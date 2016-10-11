/* Copyright 2006 Sun Microsystems, Inc. All rights reserved. You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html
$Id: FileUploadBean.java,v 1.51 2007/01/16 22:48:28 inder Exp $ */

package org.zkforge.petshop.controller;

import org.zkforge.petshop.model.*;
import org.zkforge.petshop.proxy.GeoCoder;
import org.zkforge.petshop.proxy.GeoPoint;
import org.zkforge.petshop.util.ImageScaler;
import org.zkforge.petshop.util.PetstoreUtil;
import org.zkforge.fckez.FCKeditor;

import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zkplus.databind.AnnotateDataBinder;
import org.zkoss.zul.*;
import org.zkoss.image.AImage;

import java.io.FileOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;

import javax.persistence.Transient;
import javax.servlet.ServletContext;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class SellerWindow extends Window {
	private static final long serialVersionUID = 1706347578924259616L;
    private static final String comma=", ";
    private String _verify;
    private AImage _img;
    private Product _product;
    private Item _item;
    
    /** Creates a new instance of FileUploadBean */
    public SellerWindow() {
    	_item = newItem();
    }
    
    private Item newItem() {
    	//prepare an empty Item to be used by DataBinder
    	Item item = new Item();
    	item.setContactInfo(new SellerContactInfo());
    	item.setAddress(new Address());
    	return item;
    }
    
    public Item getItem() {
    	return _item;
    }
    
    public void onCreate(Event evt) {
    	Listbox lbx = (Listbox)getFellow("products"); 
/*    	ListModel model = new ListModelList(getProducts());
    	lbx.setModel(model);
    	lbx.setSelectedIndex(0);
*/    	
    	lbx.focus();
    }
    
    public List getProducts() {
    	List products = new CatalogFacade().getProducts();
    	_product = (Product) products.get(0);
    	return products;
    }

    public Product getProduct() {
    	return _product;
    }
    
    public void setProduct(Product product) {
    	_product = product;
    }

	public void next() {
		getFellow("pane1").setVisible(false);
		getFellow("pane2").setVisible(true);
		((HtmlBasedComponent) getFellow("firstName")).focus();
	}
	
	public void prev() {
		getFellow("pane1").setVisible(true);
		getFellow("pane2").setVisible(false);
		((HtmlBasedComponent) getFellow("petName")).focus();
	}
	
	public void upload() {
		Image picture = (Image) getFellow("picture");
		try {
			Object img = Fileupload.get();
			if (img instanceof org.zkoss.image.AImage) {
				picture.setContent((AImage)img);
			} else if (img != null) {
				Messagebox.show("Not an image: "+img, "Error", Messagebox.OK, Messagebox.ERROR);
			}
			_img = (AImage) img;
		} catch (InterruptedException e) {
			throw UiException.Aide.wrap(e);
		}
	}
	
	public void setVerify(String verify) {
		_verify = verify;
	}
	
	public String getVerify() {
		return _verify;
	}

    public void submit() {
/*    	Textbox tbxverify = (Textbox)getFellow("verify"); 
    	String verify = tbxverify.getValue();
*/
    	Captcha captcha = (Captcha)getFellow("captcha");
    	
    	if (!captcha.getValue().equalsIgnoreCase(_verify)) {
    		try {
				Messagebox.show("Incorrect verify string, try type the charcter in the image.", "Error", Messagebox.OK, Messagebox.ERROR);
			} catch (InterruptedException e) {
				throw UiException.Aide.wrap(e);
			} finally {
				captcha.randomValue(); //change a value for next try
			}
    		return;
    	}
    	//validate image
		if (_img == null) {
			try {
				Messagebox.show("Please upload your pet picture.", "Error", Messagebox.OK, Messagebox.ERROR);
			} catch (InterruptedException e) {
				throw UiException.Aide.wrap(e);
			}
			return;
		}

		//pet info
/*    	Listbox lbx = (Listbox)getFellow("products");
    	Product product = (Product) getProducts().get(lbx.getSelectedIndex());
*/    	String productId = _product.getProductID();
/*		String petName = ((Textbox)getFellow("petName")).getValue();
		String description = ((FCKeditor)getFellow("description")).getValue();
		BigDecimal price = ((Decimalbox)getFellow("price")).getValue().setScale(2, BigDecimal.ROUND_HALF_UP);
		String tags = ((Textbox)getFellow("tags")).getValue();
*/		
        //contact
/*        String firstName = ((Textbox)getFellow("firstName")).getValue();
        String lastName = ((Textbox)getFellow("lastName")).getValue();
        String email = ((Textbox)getFellow("email")).getValue();
*/
        //Address
/*        String street1 = ((Textbox)getFellow("street")).getValue();
        String city = ((Textbox)getFellow("city")).getValue();
        String state = ((Textbox)getFellow("state")).getValue();
        String zip = ((Textbox)getFellow("zipCode")).getValue();
*/        
        //get latitude & longitude of address via GeoCode service
        String addressx = _item.getAddress().addressToString();
/*        if(street1 != null && street1.length() > 0) {
            addressx.append(street1);
        }
        
        if(city != null && city.length() > 0) {
            addressx.append(comma);
            addressx.append(city);
        }
        
        if(state != null && state.length() > 0) {
            addressx.append(comma);
            addressx.append(state);
        }
        
        if(zip != null && zip.length() > 0) {
            addressx.append(comma);
            addressx.append(zip);
        }
*/
    	// get proxy host and port from servlet context
    	ServletContext ctx = (ServletContext) getDesktop().getWebApp().getNativeContext();
        String proxyHost=ctx.getInitParameter("proxyHost");
        String proxyPort=ctx.getInitParameter("proxyPort");

    	GeoCoder geoCoder=new GeoCoder();
        if(proxyHost != null && proxyPort != null) {
            // set proxy host and port if it exists
            // NOTE: This may require write permissions for java.util.PropertyPermission to be granted
            PetstoreUtil.getLogger().log(Level.INFO, "Setting proxy to " + proxyHost + ":" + proxyPort + ".  Make sure server.policy is updated to allow setting System Properties");
            geoCoder.setProxyHost(proxyHost);
            try {
                geoCoder.setProxyPort(Integer.parseInt(proxyPort));
            } catch (NumberFormatException ee) {
                ee.printStackTrace();
            }
        }
        
        // use component to get points based on location (this uses Yahoo's map service
        String totAddr=addressx;
        double latitude=0;
        double longitude=0;
        if(totAddr.length() > 0) {
            GeoPoint points[] = geoCoder.geoCode(totAddr);
       
            // grab first address if more than one came back
            if(points != null && points.length > 0) {
                // set values to used for map location
                latitude = points[0].getLatitude();
                longitude = points[0].getLongitude();
            }
        }
        
        //save image
    	String fileName = _img.getName();
		int j = fileName.lastIndexOf(".");
		final String ext = fileName.substring(j);
		fileName = fileName.substring(0, j) + System.currentTimeMillis();
		final String thumbURL = "/images/"+fileName+"_thumb"+ext;
		final String imageURL = "/images/"+fileName+ext;
		final String thumb = ctx.getRealPath(thumbURL);
		fileName = ctx.getRealPath(imageURL);
		
		final byte[] bytes = _img.getByteData(); 

		//scale image to thumb and write to file
		try {
			ImageScaler scaler = new ImageScaler(new ByteArrayInputStream(bytes));
			scaler.keepAspectWithWidth();
			scaler.resizeWithGraphics(thumb);
			
			//save picture
			FileOutputStream fos = new FileOutputStream(fileName);
			fos.write(bytes);
			fos.close();
		} catch(IOException ex) {
			throw UiException.Aide.wrap(ex);
		}
		_item.setProductID(_product.getProductID());
		_item.setImageURL(imageURL);
		_item.setImageThumbURL(thumbURL);
		
                
/*        Address addr = new Address(street1,"",city,state,zip,
                        latitude,longitude);
        SellerContactInfo contactInfo = new SellerContactInfo(firstName, lastName, email);
        Item item = new Item(productId,petName,description,imageURL,thumbURL, price,
                        addr,contactInfo,0,0);
*/                
        // now parse tags for item
/*        CatalogFacade catalogFacade = new CatalogFacade();
        StringTokenizer stTags = new StringTokenizer(tags, " ");
        String tagx=null;
        while(stTags.hasMoreTokens()) {
            tagx=stTags.nextToken().toLowerCase();

            // see if tag is already in item
            if(!_item.containsTag(tagx)) {
                // add correct tag reference to item
                _item.getTags().add(catalogFacade.addTag(tagx));
            }
        }
*/        
        
        Long itemId = new CatalogFacade().addItem(_item);
        Map args = new HashMap();
        args.put("item", _item);
        
        clearPetInfo();
        captcha.randomValue();
        
		final Window win = (Window) Executions.createComponents(
				"sellerStatus.zul", null, args);
		try {
			win.doModal();
			this.prev();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}               
    }
    
    public void clearPetInfo(){
    	_verify = "";
    	SellerContactInfo orgContact = _item.getContactInfo();
    	Address orgAddr = _item.getAddress();
    	
    	_item = newItem();
    	_item.setAddress(new Address(orgAddr.getStreet1(), orgAddr.getStreet2(), orgAddr.getCity(),
    			orgAddr.getState(), orgAddr.getZip(), orgAddr.getLatitude(), orgAddr.getLongitude()));
    	_item.setContactInfo(new SellerContactInfo(orgContact.getFirstName(), orgContact.getLastName(), orgContact.getEmail()));
    	AnnotateDataBinder binder = (AnnotateDataBinder) getVariable("binder", false);
    	binder.loadAll();
    	
        ((Image) getFellow("picture")).setContent(null);
    }
}
