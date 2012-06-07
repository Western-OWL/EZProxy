package ca.uwo.owl.ezproxy.logic.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.PropertyProvideable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;

import ca.uwo.owl.ezproxy.logic.entity.EZProxyEntityProvider;

/**
 * Allows some basic functions on EZProxy instances via the EntityBroker.
 * 
 * @author Brian Jones (bjones86@uwo.ca)
 */
public class EZProxyEntityProviderImpl implements EZProxyEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider, PropertyProvideable, Resolvable, Outputable, RESTful
{
	private static final Log log = LogFactory.getLog( EZProxyEntityProviderImpl.class );
	
	// The name of the permission used to determine access to EZProxy link configuration page
	private static final String TOOL_PERM_NAME 	= "ezproxy.configure";
	
	// The name of the tool registration
	private static final String TOOL_REG_NAME = "sakai.ezproxy";
	
	@EntityCustomAction( action = "entityRefs", viewKey = EntityView.VIEW_LIST )
	public List<String> getEntityRefs( EntityView view, Map<String, Object> params )
	{
		if( log.isDebugEnabled() )
			log.debug( "getEntityRefs() " );
		
		String[] parts = view.getPathSegments();
		for( String str : parts )
			log.info( "part = " + str );
		
		return null;
	}
	
	public List<String> findEntityRefs( String[] prefixes, String[] name, String[] searchValue, boolean exactMatch ) 
	{
//		if( log.isDebugEnabled() )
//			log.debug( "findEntityRefs()" );
//		
//		String siteID = null;
//		String userID = null;
//		List<String> retVal = new ArrayList<String>();
//		
//		// If the provided prefix is that of the ezproxy prefix...
//		if( ENTITY_PREFIX.equals( prefixes[0] ) )
//		{
//			// Get the siteID and userID
//			for( int i = 0; i < name.length; ++i )
//			{
//				if( "context".equalsIgnoreCase( name[i] ) || "site".equalsIgnoreCase( name[i] ) )
//					siteID = searchValue[i];
//				else if( "user".equalsIgnoreCase( name[i] ) || "userId".equalsIgnoreCase( name[i] ) )
//					userID = searchValue[i];
//			}
//			
//			// If the siteID and userID are NOT null...
//			if( siteID != null && userID != null )
//			{
//				try
//				{
//					// Get the site, verify it exists
//					Site site = siteService.getSite( siteID );
//					if( site != null )
//					{
//						// Check to make sure the current user has 'ezproxy.configure" permission for the site
//						if( !securityService.unlock( userID, TOOL_PERM_NAME, siteService.siteReference( siteID ) ) )
//							throw new SecurityException( "You do not have access to site: " + siteID );
//						
//						// Loop through a list of EZProxy instances in this site
//						Collection<ToolConfiguration> ezproxyLinks = site.getTools( TOOL_REG_NAME );
//						for( Iterator<ToolConfiguration> iter = ezproxyLinks.iterator(); iter.hasNext(); )
//						{
//							// Get the page that contains this EZProxy instance
//							ToolConfiguration config = iter.next();
//							SitePage page = config.getContainingPage();
//							if( page != null )
//							{
//								// Get the properties for this EZProxy instance
//								ResourceProperties props = page.getProperties();
//								if( props != null )
//									retVal.add( "/" + ENTITY_PREFIX + "/" + siteID + "/" + page.getId() );
//							}
//						}
//					}
//				}
//				catch( IdUnusedException ex ) { throw new IllegalArgumentException( "No site found for site ID: " + siteID + " : " + ex.getMessage() ); }
//			}
//		}
//		
//		return retVal;
		return null;
	}
	
	public Map<String, String> getProperties( String reference )
	{
		if( log.isDebugEnabled() )
			log.debug( "getProperties()" );
		
		Map<String, String> properties = new HashMap<String, String>();
		
		// Get the siteID and userID from the reference string
		String[] tokens = reference.split( "/" );
		String siteID = tokens[2];
		String pageID = tokens[3];
		
		try
		{
			// Get the site, verify it exists
			Site site = siteService.getSite( siteID );
			if( site != null )
			{
				// Check to make sure the current user has 'ezproxy.configure" permission for the site
				String currentUserID = sessionManager.getCurrentSessionUserId();
				if( !securityService.unlock( currentUserID, TOOL_PERM_NAME, siteService.siteReference( siteID ) ) )
					throw new SecurityException( "You do not have access to site: " + siteID );
				
				// Loop through all instances of EZProxy in this site...
				Collection<ToolConfiguration> ezproxyLinks = site.getTools( TOOL_REG_NAME );
				for( Iterator<ToolConfiguration> iter = ezproxyLinks.iterator(); iter.hasNext(); )
				{
					// Get the page that contains this EZProxy instance (matching page ID)
					ToolConfiguration config = iter.next();
					SitePage page = config.getContainingPage();
					if( page != null && pageID.equalsIgnoreCase( page.getId() ) )
					{
						// Get the properties for this EZProxy instance
						ResourceProperties props = page.getProperties();
						if( props != null )
						{
							String sourceURL = props.getProperty( "ezproxy.sourceURL" );
							properties.put( "url", "/portal/site/" + siteID + "/page/" + pageID );
							properties.put( "pageTitle", page.getTitle() );
							properties.put( "toolTitle", config.getTitle() );
							properties.put( "ezproxyURL", ( sourceURL != null ) ? sourceURL : "n/a" );
							properties.put( "title", page.getTitle() );
						}
					}
				}
			}
		}
		catch( IdUnusedException ex ) { throw new IllegalArgumentException( "No site found for site ID: " + siteID + " : " + ex.getMessage() ); }
		
		return properties;
	}
	
	public String getPropertyValue( String reference, String name )
	{
		if( log.isDebugEnabled() )
			log.debug( "getPropertyValue()" );
		
		String retVal = null;
		Map<String, String> properties = getProperties( reference );
		if( properties != null && properties.containsKey( name ) )
			retVal = properties.get( name );
		return retVal;
	}
	
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
		}
		
		return retVal;
	}
	
	public Object getEntity( EntityReference ref )
	{
		if( log.isDebugEnabled() )
			log.debug( "getEntity()" );
		
		// Get the packed ID
		String id = ref.getId();
		String reference = ref.getReference();
		String originalReference = ref.getOriginalReference();
		if( id != null )
		{
			String[] tokens = id.split( "/" );
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
							
							// Get this EZProxy instance as an entity
							if( page != null && pageID.equalsIgnoreCase( page.getId() ) )
								return getEZProxyEntity( site, page, config );
						}
					}
				}
				catch( IdUnusedException ex ) { throw new IllegalArgumentException( "No site found for site ID: " + siteID + " : " + ex.getMessage() ); }
			}
		}
		
		return null;
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
		return EZProxyEntityProviderImpl.ENTITY_PREFIX;
	}
	
	public Object getSampleEntity()
	{
		return new EZProxyEntity();
	}
	
	public String[] getHandledOutputFormats()
	{
		return EZProxyEntityProviderImpl.HANDLED_OUTPUT_FORMATS;
	}

	// *************************************************************
	// ******************* Unimplemented Methods *******************
	// *************************************************************
	public boolean 		entityExists( String id )														{ return true; }
	public String 		createEntity( EntityReference ref, Object entity, Map<String, Object> params ) 	{ return null; }
	public void 		updateEntity( EntityReference ref, Object entity, Map<String, Object> params ) 	{}
	public void 		deleteEntity( EntityReference ref, Map<String, Object> params ) 				{}
	public List<?> 		getEntities ( EntityReference ref, Search search ) 								{ return null; }
	public String[] 	getHandledInputFormats() 														{ return null; }
	public void 		setPropertyValue( String reference, String name, String value ) 				{}
	
	@Getter @Setter
	private SessionManager sessionManager;
	
	@Getter @Setter
	private SiteService siteService;
	
	@Getter @Setter
	private SecurityService securityService;
	
	/**
	 * Wrapper class to hold only the fields that we want to return to the EntityBroker
	 * 
	 * @author bjones86
	 */
	public class EZProxyEntity implements Comparable<Object>
	{
		private String siteID;
		private String pageID;
		private String pageTitle;
		private String toolTitle;
		private String sourceURL;
		
		public String	getId()								{ return ( siteID != null && pageID != null ) ? siteID + ":" + pageID : null; }
		public String 	getSiteID() 						{ return this.siteID; }
		public String 	getPageID() 						{ return this.pageID; }
		public String 	getPageTitle() 						{ return this.pageTitle; }
		public String 	getToolTitle() 						{ return this.toolTitle; }
		public String	getSourceURL()						{ return this.sourceURL; }
		public void 	setSiteID( String siteID ) 			{ this.siteID = siteID; }
		public void 	setPageID( String pageID ) 			{ this.pageID = pageID; }
		public void 	setPageTitle( String pageTitle ) 	{ this.pageTitle = pageTitle; }
		public void 	setToolTitle( String toolTitle ) 	{ this.toolTitle = toolTitle; }
		public void		setSourceURL( String sourceURL )	{ this.sourceURL = sourceURL; }
		
		public EZProxyEntity() {}
		
		// Default sort by tool title
		public int compareTo( Object obj )
		{
			return ( (EZProxyEntity) obj ).getToolTitle().compareTo( this.getToolTitle() );
		}
	}
}
