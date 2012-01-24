package ca.uwo.owl.ezproxy.logic;

import java.util.List;

import ca.uwo.owl.ezproxy.model.EZProxyEntry;

/**
 * EZProxy Entry logic interface
 * 
 * @author Brian Jones (bjones86@uwo.ca)
 *
 */
public interface EZProxyLogic
{
	/**
	 * Get an EZProxy Entry by tool ID and name
	 * @return
	 */
	public List<EZProxyEntry> getEZProxyEntry( String siteID, String pageID );
	
	/**
	 * Get all EZProxy Entries
	 * @return
	 */
	public List<EZProxyEntry> getAllEZProxyEntries();
	
	/**
	 * Add a new EZProxy Entry
	 * @param e	EZProxyEntry
	 * @return boolean if success, false if not
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
