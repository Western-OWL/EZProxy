package ca.uwo.owl.ezproxy.logic.entity;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.UrlValidator;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityURLRedirect;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.PropertyProvideable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Redirectable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestAware;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

import ca.uwo.owl.ezproxy.logic.SharedSecretAuth;
import ca.uwo.owl.ezproxy.logic.entity.EZProxyEntityProvider;

/**
 * Allows some basic functions on EZProxy instances via the EntityBroker.
 * 
 * @author Brian Jones (bjones86@uwo.ca)
 */
public class EZProxyEntityProviderImpl implements EZProxyEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider, RequestAware, 
										PropertyProvideable, Resolvable, Outputable, RESTful, Redirectable, ActionsExecutable
{
	// Class members
	private static final Log 			log 			= LogFactory.getLog( EZProxyEntityProviderImpl.class );		// The logger
	private static final String 		TOOL_PERM_NAME 	= "ezproxy.configure"; 										// EZProxy configuration permission name
	private static final String 		TOOL_REG_NAME 	= "sakai.ezproxy"; 											// The name of the tool registration
	private static final String 		SERVICE_URL 	= ServerConfigurationService.getString( "ezproxy.url" );	// The EZProxy service URL
	private static final String 		SHARED_SECRET 	= ServerConfigurationService.getString( "ezproxy.secret" );	// The EZProxy shared secret
	private static final String[] 		schemes 		= { "http", "https" };										// The list of valid protocols for URL validation
	private static final UrlValidator 	urlValidator 	= new UrlValidator( schemes );								// The URL validator object
	private static List<String> 		allowedRoles 	= new ArrayList<String>();									// The list of allowed roles from sakai.properties
	
	// Instance members
	private ResourceLoader resourceLoader = new ResourceLoader( "messages" );
	
	/**
	 * {@inheritDoc}
	 */
	public List<String> findEntityRefs( String[] prefixes, String[] name, String[] searchValue, boolean exactMatch ) 
	{
		if( log.isDebugEnabled() )
			log.debug( "findEntityRefs()" );
		
		String siteID = null;
		String userID = null;
		List<String> retVal = new ArrayList<String>();
		
		// If the provided prefix is that of the ezproxy prefix...
		if( ENTITY_PREFIX.equals( prefixes[0] ) )
		{
			// Get the siteID and userID
			for( int i = 0; i < name.length; ++i )
			{
				if( "context".equalsIgnoreCase( name[i] ) || "site".equalsIgnoreCase( name[i] ) )
					siteID = searchValue[i];
				else if( "user".equalsIgnoreCase( name[i] ) || "userId".equalsIgnoreCase( name[i] ) )
					userID = searchValue[i];
			}
			
			// If the siteID and userID are NOT null...
			if( siteID != null && userID != null )
			{
				try
				{
					// If the siteID and userID are the same, it's really trying to access the user's My Workspace, so we need to prepend '~' to the siteID
					if( siteID.equals( userID ) )
						siteID = "~" + siteID;
					
					// Get the site, verify it exists
					Site site = siteService.getSite( siteID );
					if( site != null )
					{
						// Check to make sure the current user has 'ezproxy.configure" permission for the site
						if( !securityService.unlock( userID, TOOL_PERM_NAME, siteService.siteReference( siteID ) ) )
						{
							// Log the message that this user doesn't have the permision for the site, return an empty list
							log.error( "User (" + userID + ") does not have permission (" + TOOL_PERM_NAME + ") for site: " + siteID );
							return retVal;
						}
						
						// Loop through a list of EZProxy instances in this site
						Collection<ToolConfiguration> ezproxyLinks = site.getTools( TOOL_REG_NAME );
						for( Iterator<ToolConfiguration> iter = ezproxyLinks.iterator(); iter.hasNext(); )
						{
							// Get the page that contains this EZProxy instance
							ToolConfiguration config = iter.next();
							SitePage page = config.getContainingPage();
							if( page != null )
							{
								// Get the properties for this EZProxy instance
								ResourceProperties props = page.getProperties();
								if( props != null )
								{
									// Create the ref string
									String refString = "/" + ENTITY_PREFIX + "/" + siteID + ":" + page.getId();
									
									// If the sourceURL for this EZProxy instance is NOT null, NOT 'n/a', NOT 'https://' AND NOT 'http://',
									// this instance has been initialized with a valid URL, so add it to the list of entity refs to return
									String sourceURL = getPropertyValue( refString, "ezproxyURL" );
									if( sourceURL != null && urlValidator.isValid( sourceURL ) )
										retVal.add( refString );
								}
							}
						}
					}
				}
				catch( IdUnusedException ex ) { throw new IllegalArgumentException( "No site found for site ID: " + siteID + " : " + ex.getMessage() ); }
			}
		}
		
		return retVal;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Map<String, String> getProperties( String reference )
	{
		if( log.isDebugEnabled() )
			log.debug( "getProperties()" );
		
		// Return value
		Map<String, String> properties = new HashMap<String, String>();
		
		// If the reference is invalid, throw an exception and exit
		if( reference == null || reference.length() < 1 )
			throw new IllegalArgumentException( "You must provide a valid reference string" );
		
		// Otherwise...
		else
		{
			// Get the ID and the entity
			String id = reference.replaceAll("/" + ENTITY_PREFIX + "/", "" );
			EZProxyEntity entity = getEZProxyEntity( id );
			
			// If the entity is not null, get the properties
			if( entity != null )
			{
				properties.put( "url", "/portal/site/" + entity.getSiteID() + "/page/" + entity.getPageID() );
				properties.put( "pageTitle", entity.getPageTitle() );
				properties.put( "toolTitle", entity.getToolTitle() );
				properties.put( "ezproxyURL", ( entity.getSourceURL() != null ) ? entity.getSourceURL() : "n/a" );
				properties.put( "title", entity.getTitle() );
			}
		}
		
		// Return the properties
		return properties;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getPropertyValue( String reference, String name )
	{
		if( log.isDebugEnabled() )
			log.debug( "getPropertyValue()" );
		
		String retVal = null;
		
		// Get the properties, if they're not null, get the named property
		Map<String, String> properties = getProperties( reference );
		if( properties != null && properties.containsKey( name ) )
			retVal = properties.get( name );
		
		return retVal;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Object getEntity( EntityReference ref )
	{
		if( log.isDebugEnabled() )
			log.debug( "getEntity()" );
		
		try
		{
			// If the reference is invalid, throw an exception and exit
			if( ref == null || ref.getId() == null || ref.getId().length() < 1 )
				throw new IllegalArgumentException( "You must supply a valid EntityReference" );
			
			// Otherwise get the entity
			else if( isCurrentUserViewAuth() )
				requestGetter.getResponse().sendRedirect( "/direct/" + ENTITY_PREFIX + "/" + ref.getId() + "/redirect" );
			else
				requestGetter.getResponse().sendRedirect( "/direct/" + ENTITY_PREFIX + "/" + ref.getId() + "/viewHTML" );
		}
		catch( IOException ex ) { log.error( ex ); }
		
		return ref;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean entityExists( String id )
	{ 
		if( log.isDebugEnabled() )
			log.debug( "entityExists()" );
		
		// If the id is invalid, throw an exception and exit
		if( id == null )
			throw new IllegalArgumentException( "You must supply a valid id" );
		
		// Otherwise get the entity
		else
		{
			// If the entity is null, it doesn't exist
			if( getEZProxyEntity( id ) == null )
				return false;
			
			// Otherwise, it exists
			else
				return true;
		}
	}
	
	/**
     * Returns an HTML string that describes the EZProxyEntity in question;
     * takes into account authentication for viewing EZProxy links.
     * 
     * @param ref the EntityReference object requested
     * @return the HTML string describing the entity
     */
	@EntityCustomAction( action = "viewHTML", viewKey = EntityView.VIEW_SHOW )
	public Object getEZProxyEntityAsHTML( EntityReference ref )
	{
		if( log.isDebugEnabled() )
			log.debug( "getEZProxyEntityAsHTML()" );
		
		// Return the generated HTML
		return new ActionReturn( Formats.UTF_8, Formats.HTML_MIME_TYPE, createEZProxyEntityHTML( (EZProxyEntity) getEZProxyEntity( ref.getId() ) ) );
	}
	
	/**
     * Redirects the user who clicked on an EZProxy entity link to the actual final generated
     * URL of the EZProxy instance, provided the current user passes the validation/authentication
     * required for viewing an EZPRoxy link
     * 
     * @param vars the map of parameters returned from the EntityBroker (contains the siteID:pageID identifier)
     * @return the final generated URL of the EZProxy instance
     */
	@EntityURLRedirect( "/{prefix}/{id}/redirect" )
	public String redirectEZProxyEntity( Map<String, String> vars )
	{
		if( log.isDebugEnabled() )
			log.debug( "redirectEZProxyEntity()" );
		
		// Get the entity & userEid
		EZProxyEntity entity = (EZProxyEntity) getEZProxyEntity( vars.get( "id" ) );
		String userEid = sessionManager.getCurrentSession().getUserEid();
		
		// If the current user is able to view an EZProxy link, generate and return the final URL
		if( isCurrentUserViewAuth() )
			return generateFinalEZProxyURL( entity, userEid );
		
		// Otherwise, redirect to the /viewHTML custom action (which handles the non-authorized presenation)
		else
			return "/direct/" + ENTITY_PREFIX + "/" + vars.get( "id" ) + "/viewHTML";
	}
	
	/**
     * Controls the globally unique prefix for the entities handled by this provider<br/> For
     * example: Announcements might use "annc", Evaluation might use "eval" (if this is not actually
     * unique then an exception will be thrown when Sakai attempts to register this broker)<br/>
     * (the global reference string will consist of the entity prefix and the local id)
     * 
     * @return the string that represents the globally unique prefix for an entity type
     */
	public String getEntityPrefix()
	{
		if( log.isDebugEnabled() )
			log.debug( "getEntityPrefix()" );
		
		return EZProxyEntityProvider.ENTITY_PREFIX;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Object getSampleEntity()
	{
		if( log.isDebugEnabled() )
			log.debug( "getSampleEntity()" );
		
		return new EZProxyEntity();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String[] getHandledOutputFormats()
	{
		if( log.isDebugEnabled() )
			log.debug( "getHandledOutputFormats()" );
		
		return EZProxyEntityProvider.HANDLED_OUTPUT_FORMATS;
	}
	
	/**
	 * Get an EZProxyEntity object by ID (siteID:pageID)
	 * 
	 * @param id the packed ID reference string (siteID:pageID)
	 * @return the EZProxyEntity object requested
	 */
	private EZProxyEntity getEZProxyEntity( String id )
	{
		if( log.isDebugEnabled() )
			log.debug( "getEZProxyEntity()" );
		
		// Return value
		EZProxyEntity retVal = null;
		
		if( id == null || id.length() < 1 )
			throw new IllegalArgumentException( "You must supply a valid reference string" );
		else
		{
			// Get the siteID and pageID from the provided ID
			String[] tokens = id.split( ":" );
			if( tokens.length == 2 )
			{
				String siteID = tokens[0];
				String pageID = tokens[1];
				
				try 
				{ 					
					// Get the site
					Site site = siteService.getSite( siteID );
					if( site != null )
					{
						// Loop through a list of EZProxy instances within this site
						Collection<ToolConfiguration> ezproxyLinks = site.getTools( TOOL_REG_NAME );
						for( Iterator<ToolConfiguration> iter = ezproxyLinks.iterator(); iter.hasNext(); )
						{
							// Get the page that contains this EZProxy instance
							ToolConfiguration config = iter.next();
							SitePage page = config.getContainingPage();
							
							// If the page is NOT null AND the pageID's match, this EZProxy Entity exists
							if( page != null && pageID.equalsIgnoreCase( page.getId() ) )
								retVal = getEZProxyEntity( site, page, config );
						}
					}
				}
				catch( IdUnusedException ex ) { throw new IllegalArgumentException( "No site found for site ID: " + siteID + " : " + ex.getMessage() ); }
			}
		}
		
		return retVal;
	}
	
	/**
	 * Get an EZProxyEntity object by site, page and tool configuration
	 * 
	 * @param site the site object associated with the desired EZProxy instance
	 * @param page the page object associated with the desired EZProxy instance
	 * @param config the tool configuration object associated with the desired EZProxy instance
	 * @return the EZProxyEntity object requested
	 */
	private EZProxyEntity getEZProxyEntity( Site site, SitePage page, ToolConfiguration config )
	{
		if( log.isDebugEnabled() )
			log.debug( "getEZProxyEntity()" );
		
		if( site == null )
			throw new IllegalArgumentException( "You must supply a valid site ID" );
		if( page == null )
			throw new IllegalArgumentException( "You must supply a valid page ID" );
		
		// Return value
		EZProxyEntity retVal = null;
		
		// Get the properties for this EZProxy instance
		ResourceProperties props = page.getProperties();
		if( props != null )
		{
			retVal = new EZProxyEntity();
			retVal.setPageID( page.getId() );
			retVal.setSiteID( site.getId() );
			retVal.setPageTitle( page.getTitle() );
			retVal.setToolTitle( config.getTitle() );
			retVal.setSourceURL( props.getProperty( "ezproxy.sourceURL" ) );
			retVal.setTitle( page.getTitle() );
		}
		
		return retVal;
	}
	
	/**
	 * Determine if the current user should be able to view the EZProxy link
	 * Only student, staff, faculty and admin members are able to view EZProxy links
	 * 
	 * @return true/false if the user is allowed to view EZProxy links
	 */
	private boolean isCurrentUserViewAuth()
	{
		if( log.isDebugEnabled() )
			log.debug( "isCurrentUserViewAuth()" );
		
		// Get the user's type
		boolean retVal = false ;
		String type = userDirectoryService.getCurrentUser().getType().toLowerCase();

		// If the users's type is in the list of allowed roles, they're allowed to view the EZProxy link; otherwise they're not
		if( type != null )
			if( allowedRoles.contains( type ) )
				retVal = true;
		
		return retVal;
	}
	
	/**
	 * Generates the final URL for an EZProxy link, which includes the generated MAC,
	 * userEid, site ID, and the source URL.
	 * 
	 * @return the generated final URL
	 */
	private String generateFinalEZProxyURL( EZProxyEntity entity, String userEid )
	{
		if( log.isDebugEnabled() )
			log.debug( "generateFinalEZProxyURL()" );
		
		// Generate the MAC and the final URL
		String mac = "";
		try { mac = SharedSecretAuth.generateMAC( userEid + entity.getSiteID(), SHARED_SECRET );	}
		catch( NoSuchAlgorithmException ex ) { log.error( ex ); }
		catch( IndexOutOfBoundsException ex ) { log.error( ex ); }
		return SERVICE_URL + "?mac=" + mac + "&pid=" + userEid + "&lcid=" + entity.getSiteID() + "&url=" + entity.getSourceURL();
	}
	
	/**
	 * Creates an HTML string for a given EZProxyEntity object
	 * 
	 * @param entity the EZProxy entity to describe via HTML
	 * @return the generated HTML string based on the provided EZProxyEntity object
	 */
	private String createEZProxyEntityHTML( EZProxyEntity entity )
	{
		if( log.isDebugEnabled() )
			log.debug( "createEZProxyEntityHTML()" );
		
		StringBuilder sb = new StringBuilder();
		sb.append( resourceLoader.getFormattedMessage( "htmlHeader", new Object[] { ServerConfigurationService.getString( "skin.repo" ) + "/tool_base.css" } ) );
		
		// If the user is allowed to view EZProxy links, generate the HTML to view the link
		if( isCurrentUserViewAuth() )
			sb.append( resourceLoader.getFormattedMessage( "htmlIframe", new Object[]
					{ generateFinalEZProxyURL( entity, sessionManager.getCurrentSession().getUserEid() ) } ) );
		
		// Otherwise just build some HTML to tell the user they're not allowed to view EZProxy links
		else
			sb.append( resourceLoader.getFormattedMessage( "htmlH2", new Object[] { resourceLoader.getString( "authFailMsg" ) } ) );
		
		// Return the built HTML string
		sb.append( resourceLoader.getString( "htmlFooter" ) );
		return sb.toString();
	}

	// *************************************************************
	// ******************* Unimplemented Methods *******************
	// *************************************************************
	public String 		createEntity( EntityReference ref, Object entity, Map<String, Object> params ) 	{ return null; }
	public void 		updateEntity( EntityReference ref, Object entity, Map<String, Object> params ) 	{}
	public void 		deleteEntity( EntityReference ref, Map<String, Object> params ) 				{}
	public List<?> 		getEntities ( EntityReference ref, Search search ) 								{ return null; }
	public String[] 	getHandledInputFormats() 														{ return null; }
	public void 		setPropertyValue( String reference, String name, String value ) 				{}
	
	// Sakai API's
	@Getter @Setter private SessionManager 			sessionManager;
	@Getter @Setter private SiteService 			siteService;
	@Getter @Setter private SecurityService 		securityService;
	@Getter @Setter private UserDirectoryService 	userDirectoryService;
	@Setter 		private RequestGetter 			requestGetter;
	
	/**
	 * init - perform any actions required here for when this bean starts up
	 */
	public void init()
	{
		if( log.isDebugEnabled() )
			log.debug( "init" );
		
		// Get the list of allowed system roles to view an ezproxy link
		try { allowedRoles = Arrays.asList( ServerConfigurationService.getStrings( "ezproxy.allow.view" ) ); }
		catch( Exception ex ) 
		{
			log.error( "sakai.property not found: ezproxy.allow.view - " + ex.getMessage() );
			allowedRoles = new ArrayList<String>();
		}
	}
}