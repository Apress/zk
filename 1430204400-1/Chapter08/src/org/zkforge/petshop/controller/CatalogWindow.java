package org.zkforge.petshop.controller;

import org.zkforge.petshop.model.*;
import org.zkoss.zul.*;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkplus.hibernate.HibernateUtil;

import java.util.List;
import java.util.Iterator;

public class CatalogWindow extends Window {
	private static final long serialVersionUID = 1L;
	private EventListener selectItemListener = new EventListener() {
		public void onEvent(Event evt) {
			Image img = (Image) evt.getTarget();
			_item = (Item) img.getAttribute("item");
			CatalogWindow.this.setVariable("item", _item, true);
			CatalogWindow.this.refreshItem(_item);
		}
		
		public boolean isAsap() {
			return true;
		}
	};
	private List _items;
	private Item _item;

	public CatalogWindow() {
		String catID = (String) Executions.getCurrent().getParameter("catid");
		if (catID == null) {
			catID = "BIRDS";
		}
		List products = new CatalogFacade().getProducts(catID);
		setVariable("products", products, true);
		Product product = (Product) products.get(0);
		_items = getItems(product.getProductID());
		setVariable("items", _items, true);
		List categories = new CatalogFacade().getCategories();
		setVariable("item", _items.get(0), true);
		setVariable("categories", categories, true);
		setVariable("selectItemListener", selectItemListener, true);
	}
	
	public void onBookmarkChanged(BookmarkEvent event) {
		String pID = null;
		try {
			String iID = event.getBookmark();
			_item = new CatalogFacade().getItem(new Long(iID));
			pID = _item.getProductID();
			_items = getItems(pID);
			refreshThumbs(_items);
			setVariable("items", _items, true);
		} catch(org.hibernate.ObjectNotFoundException ex) {
			//get default
			_item = (Item) _items.get(0);
			pID = _item.getProductID();
		}
		setVariable("item", _item, true);
		Product product = new CatalogFacade().getProduct(pID);
		String catID = product.getCategoryID();
		selectCategory(catID);
		refreshItem(_item);
	}
  
	public List getItems(String pID) {
		List items =  HibernateUtil.currentSession()
			.getNamedQuery("Item.getItemsPerProductCategory")
			.setString("pID", pID)
			.list();
			
		return items;
	}

	public void selectCategory(String catid) {
		Tabbox cattbx = (Tabbox) getFellow("cattbx");
		Tab tab = (Tab) cattbx.getFellow(catid);
		cattbx.setSelectedTab(tab);
	}
	
	public void refreshThumbs(List items) {
		Hbox inid = (Hbox) getFellow("inid");
		Div leftid = (Div) getFellow("leftid");
		Div rightid = (Div) getFellow("rightid");
		inid.getChildren().clear();
		String style = inid.getStyle();
		inid.setStyle("");
		inid.setStyle(style);
		leftid.setVisible(true);
		leftid.setVisible(false);
		rightid.setVisible(false);
		rightid.setVisible(true);
		for(final Iterator it = items.iterator(); it.hasNext();) {
			Item tmpitem = (Item) it.next();
			Image image = new Image();
			image.setSrc(tmpitem.getImageThumbURL());
			image.setHeight("70px");
			image.setAttribute("item", tmpitem);
			image.setStyle("cursor:pointer");
			image.addEventListener("onClick", selectItemListener);
			image.setParent(inid);
		}
	}
	
	public void refreshItem(Item item) {
		Image bigimage = (Image) getFellow("bigimage");
		Label itemName = (Label) getFellow("itemName");
		Decimalbox price = (Decimalbox) getFellow("price");
		Html shortDesc = (Html) getFellow("shortDesc");
		Html longDesc = (Html) getFellow("longDesc");
		bigimage.setSrc(item.getImageURL());
		itemName.setValue(item.getName());
		price.setValue(item.getPrice());
		shortDesc.setContent(item.getShortDescription());
		longDesc.setContent(item.getDescription());
		
		getDesktop().setBookmark(item.getItemID().toString());
		refreshRating(item);
	}
	
	public void refreshRating(Item item) {
		Hbox stars = (Hbox) getFellow("stars");
		Label starDesc = (Label) getFellow("starDesc");
		Clients.evalJavaScript("setgrade("+item.checkAverageRating()+");starClear($e('"+stars.getUuid()+"'),$e('"+starDesc.getUuid()+"'));");
	}
}