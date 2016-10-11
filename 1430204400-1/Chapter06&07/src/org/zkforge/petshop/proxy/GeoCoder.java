/*
 * Copyright 2006 Sun Microsystems, Inc.  All rights reserved.
 * You may not modify, use, reproduce, or distribute this
 * software except in compliance with the terms of the License at:
 *
 *   http://developer.sun.com/berkeley_license.html
 *
 * $Id: GeoCoder.java,v 1.3 2007/01/11 23:04:53 inder Exp $
 */

package org.zkforge.petshop.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <p>Service object that interacts with the Yahoo GeoCoding service.  For
 * information on the relevant APIs, see <a href="http://developer.yahoo.net/maps/rest/V1/geocode.html">
 * http://developer.yahoo.net/maps/rest/V1/geocode.html</a>.</p>
 */
public class GeoCoder {
    
    private String applicationId = APPLICATION_ID;
    private String proxyHost = null;
    private int proxyPort = 0;
    private boolean proxySet = false;
    
    
    // ------------------------------------------------------ Manifest Constants
    
    /**
     * <p>The default application identifier required by the geocoding
     * service.  This may be overridden by setting the <code>applicationId</code>
     * property.</p>
     */
    static final String APPLICATION_ID =
            "com.sun.javaee.blueprints.components.ui.geocoder";
    
    
    /**
     * <p>The URL of the geocoding service we will be using.</p>
     */
    private static final String SERVICE_URL =
            "http://api.local.yahoo.com/MapsService/V1/geocode";
    
    
    // -------------------------------------------------------------- Properties
    
    /**
     * <p>Return the application identifier to be passed to the geocoding
     * service.</p>
     */
    public String getApplicationId() {
        return this.applicationId;
    }
    
    /**
     * <p>Set the application identifier to be passed to the geocoding
     * service.</p>
     *
     * @param applicationId The new application identifier
     */
    public void setApplicationId(String applicationId) {
        if (applicationId == null) {
            throw new NullPointerException();
        }
        this.applicationId = applicationId;
    }
    
    /**
     * <p>Return the proxy host to use for network connections, or <code>null</code>
     * if the default proxy host for the application server's JVM should be
     * used instead.</p>
     */
    public String getProxyHost() {
        return this.proxyHost;
    }
    
    /**
     * Set the proxy host to use for network connections, or <code>null</code>
     * to use the default proxy host for the application server's JVM.</p>
     *
     * @param proxyHost The new proxy host
     */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
        this.proxySet = false;
    }
    
    /**
     * <p>Return the proxy port to use for network connections, or <code>0</code>
     * if the default proxy port for the application server's JVM should be
     * used instead.</p>
     */
    public int getProxyPort() {
        return this.proxyPort;
    }
    
    /**
     * Set the proxy port to use for network connections, or <code>0</code>
     * to use the default proxy port for the application server's JVM.</p>
     *
     * @param proxyPort The new proxy port
     */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
        this.proxySet = false;
    }
    
    
    // ---------------------------------------------------------- Public Methods
    
    
    /**
     * <p>Return an array of zero or more {@link GeoPoint} instances for results
     * that match a search for the specified location string.  This string can
     * be formatted in any of the following ways:</p>
     * <ul>
     * <li>city, state</li>
     * <li>city, state, zip</li>
     * <li>zip</li>
     * <li>street, city, state</li>
     * <li>street, city, state, zip</li>
     * <li>street, zip</li>
     * </ul>
     *
     * @param location Location string to search for
     *
     * @exception IllegalArgumentException if <code>location</code> does not
     *  conform to one of the specified patterns
     * @exception NullPointerException if <code>location</code> is <code>null</code>
     */
    public GeoPoint[] geoCode(String location) {
        
        // Bail out immediately if no location was specified
        if (location == null) {
            return null;
        }
        
        // Set the proxy configuration (if necessary)
        if (!proxySet) {
            setProxyConfiguration();
            proxySet = false;
        }
        
        // URL encode the specified location
        String applicationId = getApplicationId();
        try {
            applicationId = URLEncoder.encode(applicationId, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
          throw new IllegalArgumentException(e.getMessage());
        }
        
        // URL encode the specified location
        try {
            location = URLEncoder.encode(location, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        
        // Perform the actual service call and parse the response XML document,
        // then format and return the results
        Document document = null;
        StringBuffer sb = new StringBuffer(SERVICE_URL);
        sb.append("?appid=");
        sb.append(applicationId);
        sb.append("&location=");
        sb.append(location);
        try {
            document = parseResponse(sb.toString());
            return convertResults(document);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return null; // FIXME
    }
    
    /**
     * <p>Convert the parsed XML results into the appropriate output from
     * our <code>geoCode()</code> method.  If there were no results (and no
     * exception was thrown), a zero-length array will be returned.</p>
     *
     * @param document Parsed XML document representing the response from
     *  the geocoding service
     *
     * @exception IllegalArgumentException if an unrecognized XML element
     *  is encountered
     */
    private GeoPoint[] convertResults(Document document) {
        
        List<GeoPoint> results = new ArrayList<GeoPoint>();
        GeoPoint point = null;
        
        // Acquire and validate the top level "ResultSet" element
        Element root = document.getDocumentElement();
        if (!"ResultSet".equals(root.getTagName())) {
            throw new IllegalArgumentException(root.getTagName());
        }
        
        // Iterate over the child "Result" components, creating a new
        // GeoPoint instance for each of them
        NodeList outerList = root.getChildNodes();
        for (int i = 0; i < outerList.getLength(); i++) {
            
            // Validate the outer "Result" element
            Node outer = outerList.item(i);
            if (!"Result".equals(outer.getNodeName())) {
                throw new IllegalArgumentException(outer.getNodeName());
            }
            
            // Create a new GeoPoint for this element
            point = new GeoPoint();
            
            // Iterate over the inner elements to set properties
            NodeList innerList = outer.getChildNodes();
            for (int j = 0; j < innerList.getLength(); j++) {
                Node inner = innerList.item(j);
                String name = inner.getNodeName();
                String text = null;
                NodeList bottomList = inner.getChildNodes();
                for (int k = 0; k < bottomList.getLength(); k++) {
                    Node bottom = bottomList.item(k);
                    if ("#text".equals(bottom.getNodeName())) {
                        text = bottom.getNodeValue().trim();
                        if (text.length() < 1) {
                            text = null;
                        }
                        break;
                    }
                }
                if ("Latitude".equals(name)) {
                    if (text != null) {
                        point.setLatitude(Double.valueOf(text).doubleValue());
                    }
                } else if ("Longitude".equals(name)) {
                    if (text != null) {
                        point.setLongitude(Double.valueOf(text).doubleValue());
                    }
                } else if ("Address".equals(name)) {
                    point.setAddress(text);
                } else if ("City".equals(name)) {
                    point.setCity(text);
                } else if ("State".equals(name)) {
                    point.setState(text);
                } else if ("Zip".equals(name)) {
                    point.setZip(text);
                } else if ("Country".equals(name)) {
                    point.setCountry(text);
                }
            }
            results.add(point);
        }
        // Return the accumulated point information
        return (GeoPoint[]) results.toArray(new GeoPoint[results.size()]);
    }
    
    
    /**
     * <p>Parse the XML content at the specified URL into an XML
     * <code>Document</code>, which can be further processed to extract
     * the necessary content.</p>
     *
     * <p><strong>FIXME</strong> - only uses implied JVM-wide proxy,
     * so needs to be modified for Java SE 5.</p>
     *
     * @param url URL of the resource to be parsed
     *
     * @exception IOException if an input/output error occurs
     * @exception MalformedURLException if the specified URL is invalid
     * @exception ParserConfigurationException if thrown by the XML parser
     *  configuration mechanism
     * @exception SAXException if a parsing error occurs
     */
    private Document parseResponse(String url)
    throws IOException, MalformedURLException, ParserConfigurationException, SAXException {
        
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputStream stream = null;
        try {
            stream = new URL(url).openStream();
            return db.parse(stream);
        } finally {
        	if (stream != null) stream.close();
        }
    }
    
    /**
     * <p>Attempt to set the system properties related to the HTTP proxy host
     * and port to be used, but swallow security exceptions if the security
     * policy that our container is running under forbids this.  In a JDK 1.5
     * environment, we'll be able to use the <code>java.net.Proxy</code> class
     * and deal with this on a per-connection basis.  Until then, oh well.</p>
     */
    private synchronized void setProxyConfiguration() {
        
        // NOTE - the system properties API gives no way to unset properties
        // after they have been set.  Therefore, only attempt to set things
        // if we have values for both proxyHost and proxyPort
        if ((proxyHost == null) || (proxyPort == 0)) {
            return;
        }
        
        // Log and swallow any security exception that occurs when attempting
        // to set these system properties.  The subsequent connection failure
        // will be ugly enough
        try {
            System.setProperty("http.proxyHost", proxyHost);
            System.setProperty("http.proxyPort", "" + proxyPort);
        } catch (SecurityException e) {
        }
    }
}
