package ca.uwo.owl.ezproxy.dao;

import java.util.List;

import ca.uwo.owl.ezproxy.model.EZProxyEntry;

/**
 * DAO interface for EZProxy
 * 
 * @author Brian Jones (bjones86@uwo.ca)
 *
 */
public interface EZProxyDao
{
	/**
	 * Gets a single EZProxyEntry from the db
	 * 
	 * @return an item or null if no result
	 */
	public List<EZProxyEntry> getEZProxyEntry( String siteID, String pageID );
	
	/**
	 * Get all EZProxy Entries
	 * @return a list of items, an empty list if no items
	 */
	public List<EZProxyEntry> getAllEZProxyEntries();
		
	/**
	 * Add a new EZProxyEntry to the database.
	 * @param e	EZProxyEntry
	 * @return	true if success, false if not
	 */
	public boolean addEZProxyEntry( EZProxyEntry e );
	
	/**
	 * Get the 'tool title' of an EZProxy tool instance
	 * @return
	 */
	public String getToolTitle( String siteID, String pageID );
	
	/**
	 * Get the 'page title' of an EZProxy tool instance
	 * @return
	 */
	public String getPageTitle( String siteID, String pageID );
	
	/**
	 * Set the 'tool title' of an EZProxy tool instance
	 * @return
	 */
	public boolean updateToolTitle( String siteID, String pageID, String newToolTitle );
	
	/**
	 * Set the 'page title' of an EZProxy tool instance
	 * @return
	 */
	public boolean updatePageTitle( String siteID, String pageID, String newPageTitle );
	
	/**
	 * Set the value of a given EZProxy entry
	 * @return
	 */
	public boolean updateEZProxyEntry( String newValue, String siteID, String pageID, String name );
}