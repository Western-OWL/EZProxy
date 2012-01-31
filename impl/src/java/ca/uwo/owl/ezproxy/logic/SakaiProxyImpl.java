package ca.uwo.owl.ezproxy.logic;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;

import ca.uwo.owl.ezproxy.logic.SakaiProxy;
import ca.uwo.owl.ezproxy.model.EZProxyEntry;

/**
 * Implementation of our SakaiProxy API
 * 
 * @author Brian Jones (bjones86@uwo.ca)
 *
 */
public class SakaiProxyImpl implements SakaiProxy
{
	// Class members
	private static final Logger log 			= Logger.getLogger( SakaiProxyImpl.class );	// The logger
	private static final String TOOL_PERM_NAME 	= "ezproxy.configure";	// The name of the permission used to determine access to EZProxy link configuration page
	
	/**
 	* {@inheritDoc}
 	*/
	public void setToolTitle( String siteID, String pageID, String oldToolTitle, String newToolTitle )
	{		
		try
		{
			// Get the page from the site
			Site site = siteService.getSite( siteID );
			SitePage page = null;
			if( site != null )
				for( SitePage p : site.getPages() )
					if( p.getId().equalsIgnoreCase( pageID ) )
						page = p;
			
			// Get the tool
			if( page != null )
			{
				ToolConfiguration tool = null;
				for( ToolConfiguration t : page.getTools() )
					if( oldToolTitle.equalsIgnoreCase( t.getTitle() ) )
						tool = t;
				
				// Update the tool title
				if( tool != null )
				{
					tool.setTitle( newToolTitle );
					siteService.save( site );
				}
			}
		}
		catch( Exception ex )
		{
			// Error log
			log.error( "Error: " + ex.getClass() + ":" + ex.getMessage() );
			log.error( "SakaiProxyImpl.setToolTitle( siteID=" + siteID + ", pageID=" + pageID + ", newToolTitle=" + newToolTitle + " )" );
		}
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getToolTitle()
	{
		return toolManager.getCurrentPlacement().getTitle();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public void setPageTitle( String siteID, String pageID, String newPageTitle )
	{
		try
		{
			// Get the page from the site
			Site site = siteService.getSite( siteID );
			SitePage page = null;
			if( site != null )
				for( SitePage p : site.getPages() )
					if( p.getId().equalsIgnoreCase( pageID ) )
						page = p;
			
			// Set the page title
			if( page != null )
			{
				page.setTitleCustom( true );
				page.setTitle( newPageTitle );
				siteService.save( site );
			}
		}
		catch( Exception ex )
		{
			// Error log
			log.error( "Error: " + ex.getClass() + ":" + ex.getMessage() );
			log.error( "SakaiProxyImpl.setPageTitle( siteID=" + siteID + ", pageID=" + pageID + ", newPageTitle=" + newPageTitle + " )" );
		}
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getPageTitle( String siteID, String pageID )
	{
		try
		{
			// Get the page from the site
			Site site = siteService.getSite( siteID );
			SitePage page = null;
			if( site != null )
				for( SitePage p : site.getPages() )
					if( p.getId().equalsIgnoreCase( pageID ) )
						page = p;
			
			// Get the page title
			if( page != null )
				return page.getTitle();
			else
				return "";
		}
		catch( Exception ex )
		{
			// Error log
			log.error( "Error: " + ex.getClass() + ":" + ex.getMessage() );
			log.error( "SakaiProxyImpl.getPageTitle( siteID=" + siteID + ", pageID=" + pageID + " )" );
			return "";
		}
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public void setEZProxyEntry( EZProxyEntry entry )
	{
		try
		{
			// Get the page from the site
			Site site = siteService.getSite( entry.getSiteID() );
			SitePage page = null;
			if( site != null )
				for( SitePage p : site.getPages() )
					if( p.getId().equalsIgnoreCase( entry.getPageID() ) )
						page = p;
			
			// Add/update the EZProxy 'property' (entry)
			if( page != null )
			{
				// Put in some dummy data for the customHeight field (won't save an empty string)
				if( "ezproxy.customHeight".equalsIgnoreCase( entry.getName() ) )
					if( entry.getValue() == null || entry.getValue().isEmpty() )
						entry.setValue( "n/a" );
				
				// Save/update
				ResourcePropertiesEdit props = page.getPropertiesEdit();
				props.addProperty( entry.getName(), entry.getValue() );
				siteService.save( site );
			}
		}
		catch( Exception ex )
		{
			// Error log
			log.error( "Error: " + ex.getClass() + ":" + ex.getMessage() );
			log.error( "SakaiProxyImpl.setEZProxyEntry( " + entry.toString() + " )" );
		}
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public List<EZProxyEntry> getEZProxyEntry( String siteID, String pageID )
	{
		try
		{
			// Get the page from the site
			Site site = siteService.getSite( siteID );
			SitePage page = null;
			if( site != null )
				for( SitePage p : site.getPages() )
					if( p.getId().equalsIgnoreCase( pageID ) )
						page = p;
			
			// Get the values
			boolean configured = true;
			String frameHeight = "";
			String customHeight = "";
			String sourceURL = "";
			String newWindow = "";
			if( page != null )
			{
				ResourceProperties props 	= page.getProperties();
				if( props != null )
				{
					frameHeight 				= props.getProperty( "ezproxy.frameHeight" );
					customHeight 				= props.getProperty( "ezproxy.customHeight" );
					sourceURL 					= props.getProperty( "ezproxy.sourceURL" );
					newWindow 					= props.getProperty( "ezproxy.newWindow" );
					
					// Null checks
					if( frameHeight == null )
						frameHeight = "";
					if( customHeight == null )
						customHeight = "";
					if( sourceURL == null )
						sourceURL = "";
					if( newWindow == null )
						newWindow = "";
				}
			}
			
			// Make sure all values are present
			if( frameHeight.isEmpty() || customHeight.isEmpty() || sourceURL.isEmpty() || newWindow.isEmpty() )
				configured = false;
			
			// If all values present, return the List<EZProxyEntry>
			List<EZProxyEntry> entries = null;
			if( configured )
			{
				// Frame height
				entries = new ArrayList<EZProxyEntry>();
				EZProxyEntry e = new EZProxyEntry();
				e.setSiteID( siteID );
				e.setPageID( pageID );
				e.setName( "ezproxy.frameHeight" );
				e.setValue( frameHeight );
				entries.add( e );
				
				// Custom height
				e = new EZProxyEntry();
				e.setSiteID( siteID );
				e.setPageID( pageID );
				e.setName( "ezproxy.customHeight" );
				e.setValue( customHeight );
				entries.add( e );
				
				// Source URL
				e = new EZProxyEntry();
				e.setSiteID( siteID );
				e.setPageID( pageID );
				e.setName( "ezproxy.sourceURL" );
				e.setValue( sourceURL );
				entries.add( e );
				
				// New window
				e = new EZProxyEntry();
				e.setSiteID( siteID );
				e.setPageID( pageID );
				e.setName( "ezproxy.newWindow" );
				e.setValue( newWindow );
				entries.add( e );
			}
			
			// Return the entries
			return entries;
		}
		catch( Exception ex )
		{
			// Error log
			log.error( "Error: " + ex.getClass() + ":" + ex.getMessage() );
			log.error( "SakaiProxyImpl.getEZProxyEntry( siteID=" + siteID + ", pageID=" + pageID + " )" );
			return null;
		}
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isCurrentUserConfigAuth()
	{
		return securityService.unlock( getCurrentUserId(), TOOL_PERM_NAME, siteService.siteReference( getCurrentSiteId() ) );
	}
    
	/**
 	* {@inheritDoc}
 	*/
	public String getCurrentSiteId()
	{
		return toolManager.getCurrentPlacement().getContext();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getCurrentPageId()
	{
		return siteService.findTool( sessionManager.getCurrentToolSession().getPlacementId() ).getPageId();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getCurrentUserType()
	{
		return userDirectoryService.getCurrentUser().getType();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getCurrentUserEid()
	{
		return sessionManager.getCurrentSession().getUserEid();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getCurrentUserId()
	{
		return sessionManager.getCurrentSessionUserId();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getCurrentUserDisplayName()
	{
	   return userDirectoryService.getCurrentUser().getDisplayName();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean isSuperUser()
	{
		return securityService.isSuperUser();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public void postEvent( String event, String reference, boolean modify )
	{
		eventTrackingService.post( eventTrackingService.newEvent( event, reference, modify ) );
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getSkinRepoProperty()
	{
		return serverConfigurationService.getString( "skin.repo" );
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getToolSkinCSS( String skinRepo )
	{
		String skin = siteService.findTool( sessionManager.getCurrentToolSession().getPlacementId() ).getSkin();			
		
		if( skin == null )
			skin = serverConfigurationService.getString( "skin.default" );
		
		return skinRepo + "/" + skin + "/tool.css";
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public boolean getConfigParam( String param, boolean dflt )
	{
		return serverConfigurationService.getBoolean( param, dflt );
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getConfigParam( String param, String dflt )
	{
		return serverConfigurationService.getString( param, dflt );
	}
	
	/**
	 * init - perform any actions required here for when this bean starts up
	 */
	public void init()
	{
		log.info( "init" );
		
		// Register the EZProxy configuration permission
		functionManager.registerFunction( TOOL_PERM_NAME );
	}
	
	@Getter @Setter
	private ToolManager toolManager;
	
	@Getter @Setter
	private SessionManager sessionManager;
	
	@Getter @Setter
	private UserDirectoryService userDirectoryService;
	
	@Getter @Setter
	private SecurityService securityService;
	
	@Getter @Setter
	private EventTrackingService eventTrackingService;
	
	@Getter @Setter
	private ServerConfigurationService serverConfigurationService;
	
	@Getter @Setter
	private SiteService siteService;
	
	@Getter @Setter
	private FunctionManager functionManager;
}
