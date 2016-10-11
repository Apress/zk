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
import org.zkoss.zul.*;
import org.zkoss.image.AImage;

import java.io.FileOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.ServletContext;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class SellerWindow extends Window {
	private static final long serialVersionUID = 1706347578924259616L;
    private static final String comma=", ";
    private AImage _img;
    
    /** Creates a new instance of FileUploadBean */
    public SellerWindow() {
    }
    
    public void onCreate(Event evt) {
    	ListModel model = new ListModelList(getProducts());
    	Listbox lbx = (Listbox)getFellow("products"); 
    	lbx.setModel(model);
    	lbx.setSelectedIndex(0);
    	
    	lbx.focus();
    }
    
    public List getProducts() {
    	return  new CatalogFacade().getProducts();
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

    public void submit() {
    	Textbox tbxverify = (Textbox)getFellow("verify"); 
    	String verify = tbxverify.getValue();
    	Captcha captcha = (Captcha)getFellow("captcha");
    	
    	if (!captcha.getValue().equalsIgnoreCase(verify)) {
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
    	Listbox lbx = (Listbox)getFellow("products");
    	Product product = (Product) getProducts().get(lbx.getSelectedIndex());
    	String productId = product.getProductID();
		String petName = ((Textbox)getFellow("petName")).getValue();
		String description = ((FCKeditor)getFellow("description")).getValue();
		BigDecimal price = ((Decimalbox)getFellow("price")).getValue().setScale(2, BigDecimal.ROUND_HALF_UP);
		String tags = ((Textbox)getFellow("tags")).getValue();
		
        //contact
        String firstName = ((Textbox)getFellow("firstName")).getValue();
        String lastName = ((Textbox)getFellow("lastName")).getValue();
        String email = ((Textbox)getFellow("email")).getValue();

        //Address
        String street1 = ((Textbox)getFellow("street")).getValue();
        String city = ((Textbox)getFellow("city")).getValue();
        String state = ((Textbox)getFellow("state")).getValue();
        String zip = ((Textbox)getFellow("zipCode")).getValue();
        
        //get latitude & longitude of address via GeoCode service
        StringBuffer addressx = new StringBuffer(128);
        if(street1 != null && street1.length() > 0) {
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
        String totAddr=addressx.toString();
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
		final String pictureURL = "/images/"+fileName+ext;
		final String thumb = ctx.getRealPath(thumbURL);
		fileName = ctx.getRealPath(pictureURL);
		
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
                
        Address addr = new Address(street1,"",city,state,zip,
                        latitude,longitude);
        SellerContactInfo contactInfo = new SellerContactInfo(firstName, lastName, email);
        Item item = new Item(productId,petName,description,pictureURL,thumbURL, price,
                        addr,contactInfo,0,0);
                
        // now parse tags for item
        CatalogFacade catalogFacade = new CatalogFacade();
        StringTokenizer stTags = new StringTokenizer(tags, " ");
        String tagx=null;
        while(stTags.hasMoreTokens()) {
            tagx=stTags.nextToken().toLowerCase();

            // see if tag is already in item
            if(!item.containsTag(tagx)) {
                // add correct tag reference to item
                item.getTags().add(catalogFacade.addTag(tagx));
            }
        }
        
        
        Long itemId = catalogFacade.addItem(item);
        Map args = new HashMap();
        args.put("firstName", firstName);
        args.put("petName", petName);
        args.put("itemID", itemId);
        args.put("imageURL",thumbURL);
        
        captcha.randomValue();
        tbxverify.setRawValue("");
        
		final Window win = (Window) Executions.createComponents(
				"sellerStatus.zul", null, args);
		System.out.println("window created!");
		try {
			win.doModal();
			this.prev();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
        clearPetInfo();        
    }
    
    public void clearPetInfo(){
        ((Listbox) getFellow("products")).getItemAtIndex(1);
        ((Textbox)getFellow("petName")).setRawValue("");
        ((FCKeditor)getFellow("description")).setValue("");
        ((Decimalbox)getFellow("price")).setRawValue(null);
        ((Image)getFellow("picture")).setContent(null);            	
    }
}
