package ca.uwo.owl.ezproxy.logic;

import java.util.List;

import ca.uwo.owl.ezproxy.model.EZProxyEntry;

/**
 * An interface to abstract all Sakai related API calls in a central method that can be injected into our app.
 * 
 * @author Brian Jones (bjones86@uwo.ca)
 *
 */
public interface SakaiProxy
{
	/**
	 * Get the tool title of the current tool (ezproxy)
	 * @param siteID the ID of the site in question
	 * @param pageID the ID of the page in question
	 * @param oldToolTitle the old title of the tool
	 * @param newToolTitle the new title of the tool to be set
	 * @return
	 */
	public void setToolTitle( String siteID, String pageID, String oldToolTitle, String newToolTitle );
	
	/**
	 * Get the tool title of the current tool (ezproxy)
	 * @return
	 */
	public String getToolTitle();
	
	/**
	 * Get the page title of the given site+page
	 * @param siteID the ID of the site in question
	 * @param pageID the ID of the page in question
	 * @param newPageTitle the new title of the page to be set
	 * @return
	 */
	public void setPageTitle( String siteID, String pageID, String newPageTitle );
	
	/**
	 * Get the page title of the given site+page
	 * @param siteID the ID of the site in question
	 * @param pageID the ID of the page in question
	 * @return
	 */
	public String getPageTitle( String siteID, String pageID );
	
	/**
	 * Determine if the current user has the ability to configure the current EZProxy link
	 * @return
	 */
	public boolean isCurrentUserConfigAuth();
	
	/**
	 * Set an EZProxy entry
	 * @param entry the EZProxy entry to add/update in the database
	 * @return
	 */
	public void setEZProxyEntry( EZProxyEntry entry );
	
	/**
	 * Get a list of EZProxy entries for the given siteID and pageID
	 * @param siteID the ID of the site in question
	 * @param pageID the ID of the page in question
	 * @return
	 */
	public List<EZProxyEntry> getEZProxyEntry( String siteID, String pageID );
	
	/**
	 * Get current siteid
	 * @return
	 */
	public String getCurrentSiteId();
	
	/**
	 * Get current toolid
	 * @return
	 */
	public String getCurrentPageId();
	
	/**
	 * Get current user
	 * @return
	 */
	public String getCurrentUserType();
	
	/**
	 * Get current user eid
	 * @return
	 */
	public String getCurrentUserEid();
	
	/**
	 * Get current user id
	 * @return
	 */
	public String getCurrentUserId();
	
	/**
	 * Get current user display name
	 * @return
	 */
	public String getCurrentUserDisplayName();
	
	/**
	 * Is the current user a superUser? (anyone in admin realm)
	 * @return
	 */
	public boolean isSuperUser();
	
	/**
	 * Post an event to Sakai
	 * 
	 * @param event			name of event
	 * @param reference		reference
	 * @param modify		true if something changed, false if just access
	 * 
	 */
	public void postEvent( String event, String reference, boolean modify );
	
	/**
	 * Wrapper for ServerConfigurationService.getString("skin.repo")
	 * @return
	 */
	public String getSkinRepoProperty();
	
	/**
	 * Gets the tool skin CSS first by checking the tool, otherwise by using the default property.
	 * @param	the location of the skin repo
	 * @return
	 */
	public String getToolSkinCSS( String skinRepo );
	
	/**
	 * Get a configuration parameter as a boolean
	 * 
	 * @param	dflt the default value if the param is not set
	 * @return
	 */
	public boolean getConfigParam( String param, boolean dflt );
	
	/**
	 * Get a configuration parameter as a String
	 * 
	 * @param	dflt the default value if the param is not set
	 * @return
	 */
	public String getConfigParam( String param, String dflt );
}