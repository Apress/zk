package org.zkforge.petshop.controller;

import org.zkforge.petshop.proxy.*;
import org.zkforge.petshop.model.*;

import org.zkforge.gmaps.*;
import org.zkforge.gmaps.event.*;

import org.zkoss.zul.*;
import org.zkoss.zhtml.*;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkplus.hibernate.HibernateUtil;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class MapWindow extends Window {
    private List<Item> items = null;

    public void showItems() {
    	findAllByCategory();
    }
    
    public void findAllByCategory() {
        // clear old locations
        //clearValues();
        
        // get items from catalog
        CatalogFacade cf = new CatalogFacade();
        String category = null;
        
        Radiogroup rgroup = (Radiogroup) getFellow("rgroup");
        Radio radio = rgroup.getSelectedItem();
        if (radio != null) {
        	category = radio.getId();
        }

        // should always have a value
        if(category == null) category="CATS";
        
        // check to see if radius set with centerpoint
        GeoPoint[] geoCenterPoint=null;
        Textbox caddr = (Textbox) getFellow("caddr");
        String centerx = (String) caddr.getValue();
        Intbox dbox = (Intbox) getFellow("radius");
        int radius = dbox.getValue();

        if(centerx != null && centerx.length() > 0) {
            // set center so use to/from lat & long to retrieve data
            geoCenterPoint = lookUpAddress();
            if(geoCenterPoint != null) {
                // have center point
                double dLatRadius=calculateLatitudeRadius(radius);
                double dLatitude=geoCenterPoint[0].getLatitude();
                double dLongRadius=calculateLongitudeRadius(radius);
                double dLongitude=geoCenterPoint[0].getLongitude();
                items = cf.getItemsByCategoryByRadiusVLH(category, 0, 100, dLatitude - dLatRadius,
                        dLatitude + dLatRadius, dLongitude - dLongRadius, dLongitude + dLongRadius);
            }
        }
        
        if(geoCenterPoint == null) {
            // no center point or center point error so look up just ids
            items=cf.getItemsByCategoryVLH(category, 0, 100);
        }
        
        mapItems(items, geoCenterPoint, centerx, radius);
    }
    public void whenPopup(OpenEvent evt){
    	if (evt.isOpen()) {
    		Component x = (Component) evt.getReference();
    		Item loc = (Item) x.getParent().getAttribute("item");
    		Popup popup = (Popup) evt.getTarget();
    		popup.getChildren().clear();
    		Map arg = new HashMap(1);
    		arg.put("item", loc);
    		Executions.createComponents("/popup.zul", popup, arg);
    	}
    }
    public void mapItems(List<Item> items, GeoPoint[] geoCenterPoint, String centerx, int radius) {
        if(items != null && (items.size() > 0 || geoCenterPoint != null)) {
            // Set up markers for the center and information window
            double dLatitude=0;
            double dLongitude=0;
            String infoBalloon="";
            int startPos=0;
            
            if(geoCenterPoint != null) {
                // set values to used from centerAddress lookup
                dLatitude=geoCenterPoint[0].getLatitude();
                dLongitude=geoCenterPoint[0].getLongitude();
                infoBalloon="<b>Center Point</b><br/>" + centerx;
            } else {
                // use first item that as center point
                Item centerItem=items.get(0);
                dLatitude=centerItem.getAddress().getLatitude();
                dLongitude=centerItem.getAddress().getLongitude();
                infoBalloon="<b>" + centerItem.getName() + "</b><br/>" + centerItem.getAddress().addressToString();
                startPos=1;
            }
            
            // lat and long of the center point
            Gmaps maps = (Gmaps) getFellow("maps");
            maps.setCenter(dLatitude, dLongitude);
            
            // add center point in the marker points so it will show
            Gmarker marker = new Gmarker(changeSpaces(infoBalloon), dLatitude, dLongitude);
            marker.setId("center");
            
            marker.setOpen(true);
            marker.setParent(maps);

            Rows rows = (Rows) getFellow("items");
            rows.getChildren().clear();
            
            {
	            Row row = new Row();
	            org.zkoss.zul.Div div = new org.zkoss.zul.Div();
	            div.setParent(row);
	            //title
	            MarkerListener listener = new MarkerListener();
	            org.zkoss.zul.Label label = new org.zkoss.zul.Label("Center Point");
	            label.addEventListener("onClick", listener);
	            label.setStyle("cursor: pointer");
	            label.setAttribute("marker", "center");
	            label.setParent(div);
	            
	            //change line
	            Br br = new Br();
	            br.setParent(div);
	
	            //address
	            label = new org.zkoss.zul.Label(centerx);
	            label.addEventListener("onClick", listener);
	            label.setStyle("font-size:xx-small;cursor: pointer");
	            label.setAttribute("marker", "center");
	            label.setParent(div);
	            
	            //attach to rows
	            row.setParent(rows);
            }
            
            int zoomLevel = 12;
            // check area and set initial zoom level
            if(radius < 5) {
                zoomLevel=4;
            } else if(radius < 21) {
                zoomLevel=7;
            } else if(radius < 41) {
                zoomLevel=8;
            } else if(radius < 61) {
                zoomLevel=9;
            } else if(radius < 81) {
                zoomLevel=10;
            } else if(radius < 101) {
                zoomLevel=11;
            } else {
                zoomLevel=12;
            }
            maps.setZoom(zoomLevel+1);
            
            // add other locations
            Gmarker mm=null;
            Item loc=null;
            String info = null;
            for(int ii=startPos; ii < items.size(); ii++) {
                loc=items.get(ii);
                if(loc.getAddress() != null && !loc.getAddress().addressToString().equals("")) {
                	String title = loc.getName();
                	String addressstr = loc.getAddress().addressToString();
                	info = "<b>" + changeSpaces(title) + "</b><br/>" + changeSpaces(addressstr);
                    mm=new Gmarker(info, loc.getAddress().getLatitude(), loc.getAddress().getLongitude());
                    mm.setParent(maps);
                    mm.setId("marker"+ii);

                    Row row = new Row();
                    org.zkoss.zul.Div div = new org.zkoss.zul.Div();
                    div.setAttribute("item", loc);
                    div.setParent(row);
                    //title
                    MarkerListener listener = new MarkerListener();
                    org.zkoss.zul.Label label = new org.zkoss.zul.Label(title);
                    label.addEventListener("onClick", listener);
                    label.setStyle("cursor: pointer");
                    label.setAttribute("marker", "marker"+ii);
    	            label.setTooltip("info");
                    label.setParent(div);
                    
                    //detail
                    Toolbarbutton tb = new Toolbarbutton("(detail)");
                    tb.setHref("catalog.zul#"+loc.getItemID());
                    tb.setStyle("font-style: italic;");
                    tb.setParent(div);
                    
                    //change line
                    Br br = new Br();
                    br.setParent(div);

                    //address
                    label = new org.zkoss.zul.Label(addressstr);
                    label.addEventListener("onClick", listener);
                    label.setStyle("font-size:xx-small;cursor: pointer");
                    label.setAttribute("marker", "marker"+ii);
    	            label.setTooltip("info");
                    label.setParent(div);
                    
                    //attach to rows
                    row.setParent(rows);
                }
            }
            marker.setOpen(true);
        }
        
        //Haversine formula: for distance
        //R = earth’s radius (mean radius = 6,371km)
        //?lat = lat2 ? lat1
        //?long = long2 ? long1
        //a = sin??lat/2) + cos(lat1).cos(lat2).sin??long/2)
        //c = 2.atan2(?a, ?(1?a))
        //d = R.c
    }
    
    public GeoPoint[] lookUpAddress() {
        // look up lat and long of center point
        // get proxy host and port from servlet context
        String proxyHost=(String) getDesktop().getWebApp().getInitParameter("proxyHost");
        String proxyPort=(String) getDesktop().getWebApp().getInitParameter("proxyPort");
        
        // get latitude & longitude
        GeoCoder geoCoder=new GeoCoder();
        if(proxyHost != null && proxyPort != null) {
            // set proxy host and port if it exists
            geoCoder.setProxyHost(proxyHost);
            try {
                geoCoder.setProxyPort(Integer.parseInt(proxyPort));
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
        
        // use component to get points based on location (this uses Yahoo's map service
        GeoPoint points[]=null;
        try {
            Textbox caddr = (Textbox) getFellow("caddr");
            String centerx = caddr.getValue();
            points = geoCoder.geoCode(centerx);
            if ((points == null) || (points.length < 1)) {
                // invalid address, need to set to something or erase center point
                // decided that putting in dummy coord and notifying user best approach
                points=new GeoPoint[]{ new GeoPoint() };
                points[0].setLatitude(37.395908d);
                points[0].setLongitude(-121.952735d);
            }
            
        } catch (Exception ee) {
            // fail to lookup the points.
        }
        return points;
    }
 
    public double calculateLatitudeRadius(int radius) {
        // 1 latitude degree = 68.70795454545454 miles
        // 1 latitude mile = 0.014554355556290625173426834100111 degrees
        return (0.014554d * radius);
    }
    
    public double calculateLongitudeRadius(int radius) {
        // 1 logitude degree = 69.16022727272727 miles
        // 1 logitude mile = 0.014459177469972560994758974186 degrees
        return (0.014459d * radius);
    }

    public String changeSpaces(String text) {
        return text.replaceAll(" ", "&nbsp;");
    }
    
    public void showInfo(Event event) {
 		Gmarker marker = (Gmarker) ((MapClickEvent)event).getGmarker();
   		if (marker != null) {
   			marker.setOpen(true);
   		}
    }
    
    private static class MarkerListener implements EventListener {
    	public boolean isAsap() {
    		return true;
    	}
    	public void onEvent(Event event) {
    		org.zkoss.zul.Label label = (org.zkoss.zul.Label) event.getTarget();
    		String id = (String)label.getAttribute("marker");
    		if (id != null) {
    			Gmarker marker = (Gmarker) label.getFellow(id);
    			if (marker != null) {
    				marker.setOpen(true);
    			}
    		}
    	}
    }
}
