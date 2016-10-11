/* Copyright 2006 Sun Microsystems, Inc. All rights reserved. You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: http://developer.sun.com/berkeley_license.html
$Id: CatalogFacade.java,v 1.57 2007/01/17 18:00:07 basler Exp $ */

package org.zkforge.petshop.model;

//import org.zkforge.zkPetShop.search.IndexDocument;
//import org.zkforge.zkPetShop.search.Indexer;
//import org.zkforge.zkPetShop.search.UpdateIndex;
import org.zkforge.petshop.util.PetstoreConstants;
import org.zkforge.petshop.util.PetstoreUtil;
import org.zkoss.zkplus.hibernate.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;
//import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
//import javax.persistence.Query;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.transaction.UserTransaction;
import java.util.logging.Level;


public class CatalogFacade {
    // This class uses @SuppressWarnings annotation to supress the following kind of warnings: 
    // 
    // petstore/src/java/com/sun/javaee/blueprints/petstore/model/CatalogFacade.java:240: warning: [unchecked] unchecked conversion
    // found   : java.util.List
    // required: java.util.List<org.zkforge.zkPetShop.model.Product>
    //    .setParameter("categoryID", catID).getResultList();
    //
    // This is needed because the Query.getResultList() does not returns a generics version of objects. 
    // But since we are expecting a generic version (for example, List<Categories>), we need to
    // typecast the result appropriately. However, since generics information is lost at the runtime, 
    // there is no way to avoid a warning. Hence we use SuppressWarnings in this case
    
/*    @PersistenceUnit(unitName="PetstorePu") 
    private EntityManagerFactory emf;
   
    @Resource 
    private UserTransaction utx;
*/    
    private static final boolean bDebug=false;
    
    public CatalogFacade(){ }
    
/*    public void contextDestroyed(ServletContextEvent sce) {
        //close the factory and all entity managers associated with it
        if (emf.isOpen()) emf.close();
    }
    
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        context.setAttribute("CatalogFacade", this);
    }
*/    
    @SuppressWarnings("unchecked") 
    public List<Category> getCategories(){
	    List<Category> categories = HibernateUtil.currentSession()
		   	.createQuery("FROM Category").list();
        return categories;
    }
    
    @SuppressWarnings("unchecked") 
    public List<Product> getProducts(){
        List<Product> products = HibernateUtil.currentSession() 
        	.createQuery("FROM Product").list();
        return products;
    }
    
    @SuppressWarnings("unchecked") 
    public List<Item> getAllItemsFromCategory(String catID){
        List<Item> items = HibernateUtil.currentSession() 
        	.createQuery("SELECT i FROM Item i, Product p WHERE i.productID = p.productID AND p.categoryID LIKE :catID AND i.disabled = 0")
        	.setString("catID", catID).list();
        return items;
    }
    
    /**
     * Value List Handler for items. The Chunk return contains an item with iID or nothing is returned. Uses the Java Persistence query language.
     * @param pID is the product id that the item belongs to
     * @param start position of the first result, numbered from 0
     * @param chunkSize the maximum number of results to retrieve
     * @returns a List of Item objects
     */
    @SuppressWarnings("unchecked") 
    public List<Item> getItemInChunkVLH(String pID, String iID, int chunkSize){
        Query query = HibernateUtil.currentSession()
        	.createQuery("SELECT i FROM Item i WHERE i.productID = :pID AND i.disabled = 0");
        List<Item>  items;
        // scroll through these till we find the set with the itemID we are loooking for
        int index = 0;
        while (true) {
            items = query.setString("pID",pID)
            	.setFirstResult(index++ * chunkSize)
            	.setMaxResults(chunkSize)
            	.list();
            if ((items == null) || items.size() <= 0) {
                break;
            }
            for (Item i : items) {
                // return this chunk if it contains the id we are looking for
                if (i.getItemID().equals(iID)) {
                    return items;
                }
            }
        }
        return null;
    }
    
    /**
     * Value List Handler for items. Uses the Java Persistence query language.
     * @param pID is the product id that the item belongs to
     * @param start position of the first result, numbered from 0
     * @param chunkSize the maximum number of results to retrieve
     * @returns a List of Item objects
     */
    @SuppressWarnings("unchecked") 
    public List<Item> getItemsVLH(String pID, int start, int chunkSize){
        List<Item>  items = HibernateUtil.currentSession()
        	.createQuery("SELECT i FROM Item i WHERE i.productID = :pID AND i.disabled = 0")
        	.setString("pID",pID)
        	.setFirstResult(start)
        	.setMaxResults(chunkSize)
        	.list();
        return items;
    }
    
    /**
     * Value List Handler for items. Found by item ID
     * @param IDs is an array of item ids for specific items that need to be returned
     * @returns a List of Item objects
     */
    @SuppressWarnings("unchecked") 
    public List<Item> getItemsByItemID(Long[] itemIDs){
        List<Item> items = new ArrayList<Item>();
        if(itemIDs.length !=0) {
        	items = HibernateUtil.currentSession()
        		.createQuery("FROM Item i WHERE i.itemID IN (:itemIDs) AND i.disabled = 0 ORDER BY i.name")
        		.setParameterList("itemIDs", itemIDs)
        		.list();
        }
        return items;
    }
    
    /**
     * Value List Handler for items. Found by item ID and radius
     * @param IDs is an array of item ids for specific items that need to be returned
     * @returns a List of Item objects
     */
    @SuppressWarnings("unchecked") 
    public List<Item> getItemsByItemIDByRadius(Long[] itemIDs, double fromLat,
        double toLat, double fromLong, double toLong){
        List<Item> items = new ArrayList<Item>();
        if(itemIDs.length !=0) {
        	items = HibernateUtil.currentSession()
        		.createQuery("FROM Item i WHERE ((i.itemID IN (:itemIDs) "
        			+" AND ((i.address.latitude BETWEEN :fromLat AND :toLat) AND "
                    +"(i.address.longitude BETWEEN :fromLong AND :toLong ))) AND i.disabled = 0"
                    +" ORDER BY i.name")
                .setParameterList("itemIDs", itemIDs)
                .setDouble(":fromLat", fromLat)
                .setDouble(":toLat", toLat)
                .setDouble(":fromLong", fromLong)
                .setDouble(":toLong", toLong)
                .list(); 
        }
        return items;
    }
    
    /**
     * Value List Handler for items. Found by category
     * @param categoryID is the category id that the item belongs to
     * @param start position of the first result, numbered from 0
     * @param chunkSize the maximum number of results to retrieve
     * @returns a List of Item objects
     */
    @SuppressWarnings("unchecked") 
    public List<Item> getItemsByCategoryVLH(String catID, int start,
        int chunkSize){
    	List<Item>  items = HibernateUtil.currentSession()
    		.createQuery("SELECT i FROM Item i, Product p WHERE " 
    			+ "i.productID=p.productID AND p.categoryID = :catID AND i.disabled = 0"
    			+ " ORDER BY i.name")
            .setString("catID", catID)
            .setFirstResult(start)
            .setMaxResults(chunkSize)
            .list();
        return items;
    }
    /**
     * Value List Handler for items. Found by category and location radius
     * @param categoryID is the category id that the item belongs to
     * @param start position of the first result, numbered from 0
     * @param chunkSize the maximum number of results to retrieve
     * @returns a List of Item objects
     */
    @SuppressWarnings("unchecked") 
    public List<Item> getItemsByCategoryByRadiusVLH(String catID, int start,
            int chunkSize,double fromLat,double toLat,double fromLong,
            double toLong){
    	List<Item>  items = HibernateUtil.currentSession()
        	.createQuery("SELECT i FROM Item i, Product p WHERE " 
        		+ "i.productID=p.productID AND p.categoryID = :catID " 
        		+ "AND((i.address.latitude BETWEEN :fromLat AND :toLat) AND " 
        		+ "(i.address.longitude BETWEEN :fromLong AND :toLong )) AND i.disabled = 0" 
        		+ " ORDER BY i.name")
        		.setParameter("catID",catID)
        		.setParameter("fromLat",fromLat)
        		.setParameter("toLat",toLat)
        		.setParameter("fromLong",fromLong)
        		.setParameter("toLong",toLong)
        		.setFirstResult(start)
        		.setMaxResults(chunkSize)
        		.list();
        return items;
    }
    
    /**
     * Gets a list of all the zipcode/city/state for autocomplete on user forms
     * Need to enhance so that returned list is cached for reuse at application scope
     * and held as member field of facade.
     * @returns a List of ZipLocation objects
     */
    @SuppressWarnings("unchecked") 
    public List<ZipLocation> getZipCodeLocations(String city, int start, int chunkSize){
        String pattern = "'"+city.toUpperCase()+"%'";
        List<ZipLocation>  zipCodeLocations = HibernateUtil.currentSession()
        	.createQuery("SELECT  z FROM ZipLocation z where UPPER(z.city) LIKE :city")
        	.setString("city", pattern)
        	.setFirstResult(start)
        	.setMaxResults(chunkSize)
        	.list();
        return zipCodeLocations;
    }
    
    @SuppressWarnings("unchecked") 
    public List<Product> getProducts(String catID){
        List<Product> products = HibernateUtil.currentSession()
        	.createQuery("FROM Product p WHERE p.categoryID LIKE :catID")
        	.setString("catID", catID)
        	.list();
        return products;
    }
    
    @SuppressWarnings("unchecked") 
    public List<Item> getItems(String prodID){
        List<Item> items = HibernateUtil.currentSession()
        	.createQuery("FROM Item i WHERE i.productID LIKE :productID AND i.disabled = 0")
        	.setParameter("productID", prodID)
        	.list();
        return items;
    }
    
    public Category getCategory(String categoryID){
        Category result = (Category) HibernateUtil.currentSession()
        	.load(Category.class, categoryID);
        return result;
    }
    
    public Product getProduct(String productID){
        Product result = (Product) HibernateUtil.currentSession()
        	.load(Product.class, productID);
        return result;
    }

    public Item getItem(Long itemID){
        Item result = (Item) HibernateUtil.currentSession()
        	.load(Item.class, itemID);
        return result;
    }
    
    /**
     * Method to add an item with tags that are added using the addTag method
     *
     */
    public Long addItem(Item item){
    	Session session = HibernateUtil.currentSession();
        for(Tag tag : item.getTags()) {
            tag.incrementRefCount();
            tag.getItems().add(item);
//            session.merge(tag);
        }
        session.persist(item);
//      indexItem(new IndexDocument(item));
        return item.getItemID();
    }
    
    public void updateItem(Item item){
    	Session session = HibernateUtil.currentSession();
		session.merge(item);

       // update index using delete/insert method (only one available)
//      UpdateIndex.deleteIndex(PetstoreConstants.PETSTORE_INDEX_DIRECTORY, item.getItemID());
//      indexItem(new IndexDocument(item));
    }
    
    public Collection doSearch(String query){
    	final String query0 = "%"+query+"%";
    	Collection results = HibernateUtil.currentSession()
        	.createQuery("FROM Item WHERE (name LIKE :name OR description LIKE :desc) AND disabled = 0")
        	.setString("name", query0)
        	.setString("desc", query0)
        	.list();
        return results;
    }
    
    public void addTagsToItemId(String sxTags, Long itemId) {
        // now parse tags for item
        Item item=getItem(itemId);
        StringTokenizer stTags=new StringTokenizer(sxTags, " ");
        String tagx=null;
        Tag tag=null;
        while(stTags.hasMoreTokens()) {
            tagx=stTags.nextToken().toLowerCase();
            if(!item.containsTag(tagx)) {
                // tag doesn't exist so add tag
                if(bDebug) System.out.println("Adding TAG = " + tagx);
                tag=addTag(tagx);
                tag.getItems().add(item);
                tag.incrementRefCount();
                item.getTags().add(tag);
            }
        }
        // persist data
        Session session = HibernateUtil.currentSession();
        session.merge(item);
        for( Tag tagz : item.getTags()) {
           if(bDebug) System.out.println("\n***Merging tag = " + tagz.getTag());
           session.merge(tagz);
        }

        // update indexes
//      UpdateIndex update=new UpdateIndex();
//      update.updateDocTag(PetstoreConstants.PETSTORE_INDEX_DIRECTORY, "tag" , item.tagsAsString(), item.getItemID(), UpdateIndex.REPLACE_FIELD);
    }
    
    
    @SuppressWarnings("unchecked") 
    public Tag addTag(String sxTag){
    	Session session = HibernateUtil.currentSession();
        Tag tag=null;
        List<Tag> tags= session
        	.createQuery("FROM Tag t WHERE t.tag = :tag")
        	.setString("tag", sxTag)
        	.list();
        if(tags.isEmpty()) {
          // need to create tag and set flag to add reference item
          tag=new Tag(sxTag);
          // persist data
          session.persist(tag);
        } else {
            // see if item already exists in tag
            tag=tags.get(0);
        }
        return tag;
    }
    
    
    @SuppressWarnings("unchecked") 
    public List<Tag> getTagsInChunk(int start, int chunkSize) {
    	List<Tag> tags = HibernateUtil.currentSession()
        	.createQuery("FROM Tag t ORDER BY t.refCount DESC, t.tag")
        	.setFirstResult(start)
        	.setMaxResults(chunkSize)
        	.list();
        return tags;
    }

    
    @SuppressWarnings("unchecked") 
    public Tag getTag(String sxTag) {
        Tag tag=null;
        List<Tag> tags=HibernateUtil.currentSession()
        	.createQuery("SELECT t FROM Tag t WHERE t.tag = :tag")
        	.setString("tag", sxTag)
        	.list();
        if(tags != null && !tags.isEmpty()) {
            tag=tags.get(0);
        }
        return tag;
    }


/*    private void indexItem(IndexDocument indexDoc) {
        // Add document to index
        if(bDebug) System.out.println("\n*** document to index - " + indexDoc);
        Indexer indexer=null;
        try {
            indexer=new Indexer(PetstoreConstants.PETSTORE_INDEX_DIRECTORY, false);
            PetstoreUtil.getLogger().log(Level.FINE, "Adding document to index: " + indexDoc.toString());
            indexer.addDocument(indexDoc);
        } catch (Exception e) {
            PetstoreUtil.getLogger().log(Level.WARNING, "index.exception", e);
            e.printStackTrace();
        } finally {
            try {
                // must close file or will not be able to reindex
                if(indexer != null) {
                    indexer.close();
                }
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }
*/    
}

