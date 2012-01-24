package ca.uwo.owl.ezproxy.logic;

import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;

import ca.uwo.owl.ezproxy.logic.SakaiProxy;

/**
 * Implementation of our SakaiProxy API
 * 
 * @author Brian Jones (bjones86@uwo.ca)
 *
 */
public class SakaiProxyImpl implements SakaiProxy
{
	// Class members
	private static final Logger log 			= Logger.getLogger(SakaiProxyImpl.class);	// The logger
	private static final String TOOL_PERM_NAME 	= "ezproxy.configure";	// The name of the permission used to determine access to EZProxy link configuration page
	
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
