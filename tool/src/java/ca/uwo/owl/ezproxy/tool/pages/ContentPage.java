package ca.uwo.owl.ezproxy.tool.pages;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.component.cover.ServerConfigurationService;

import ca.uwo.owl.ezproxy.logic.SharedSecretAuth;
import ca.uwo.owl.ezproxy.model.EZProxyEntry;

/**
 * The 'content' page, displaying the content of the EZProxy link.
 * 
 * @author Brian Jones (bjones86@uwo.ca)
 *
 */
public class ContentPage extends BasePage implements IHeaderContributor
{
	// Class memebers
	private static final Logger log = Logger.getLogger( ContentPage.class );	// The logger
	
	// Instance members
	boolean isConfigured 	= false;	// Is this EZProxy link configured
	boolean newWindow 		= false;	// Is this EZProxy link set to open in a new window/tab
	boolean propsNotFound 	= true;		// Were the EZProxy sakai.properties present/valid
	String  finalURL 		= "";		// Holds the final (generated) URL
	
	// Constructor
	public ContentPage()
	{
		// Get any generic info needed
		final String siteID = sakaiProxy.getCurrentSiteId();
		final String pageID = sakaiProxy.getCurrentPageId();
		List<EZProxyEntry> entries = sakaiProxy.getEZProxyEntry( siteID, pageID );
		boolean ableToConfig = sakaiProxy.isCurrentUserConfigAuth();
		
		// If this EZProxy Link has not yet been setup (has no entries), inform the user
		Label notConfiguredHeading = new Label( "notConfiguredHeading", 
				( ableToConfig )												// If...
				? new ResourceModel( "heading.notConfigured" )					// True...
				: new ResourceModel( "heading.notConfigured.notAuthorized" ) );	// False...
		MultiLineLabel configuredPopup = new MultiLineLabel( "configuredPopupHeading", new ResourceModel( "heading.configuredPopup" ) );
		configuredPopup.setVisibilityAllowed( false );
		
		// If there were the right amount of entries returned...
		if( entries != null && entries.size() == NUM_ENTRIES_PER_LINK )
		{
			// It has been configured
			isConfigured = true;
			
			// Determine if it was configured for a new window/tab
			notConfiguredHeading.setVisibilityAllowed( false );
			for( EZProxyEntry e : entries )
				if( "ezproxy.newWindow".equalsIgnoreCase( e.getName() ) )
					if( "true".equalsIgnoreCase( e.getValue() ) )
						newWindow = true;
			
			// If it's a new window link, show the info label
			if( newWindow )
				configuredPopup.setVisibilityAllowed( true );
		}
		
		// Add the labels and Iframe component
		add( notConfiguredHeading );
		add( configuredPopup );
		WebMarkupContainer iframe = new WebMarkupContainer( "iframe" );
		iframe.setVisibilityAllowed( false );
		add( iframe );
		
		// If the link has been configured...
		if( isConfigured )
		{
			// Get all the necessary pieces of info
			String serviceURL = ServerConfigurationService.getString( "ezproxy.url" );
			String sharedSecret = ServerConfigurationService.getString( "ezproxy.secret" );
			String userEid = sakaiProxy.getCurrentUserEid();
			String destinationURL = "";
			String mac = "";
			for( EZProxyEntry e : entries )
				if( "ezproxy.sourceURL".equalsIgnoreCase( e.getName() ) )
					destinationURL = e.getValue();
			
			// Make sure the service URL and shared secret were in sakai.properties
			if( serviceURL != null && !serviceURL.isEmpty() && sharedSecret != null && !sharedSecret.isEmpty() )
				propsNotFound = false;
			
			// If the properties were there, continue...
			if( !propsNotFound )
			{
				// Generate the MAC, and the final URL
				try { mac = SharedSecretAuth.generateMAC( userEid + siteID, sharedSecret ); }
				catch( NoSuchAlgorithmException ex ) { log.error( ex ); }
				catch( IndexOutOfBoundsException ex ) { log.error( ex ); }
				StringBuilder b = new StringBuilder();
				b.append( serviceURL ).append( "?mac=" ).append( mac ).append( "&pid=" ).append( userEid ).append( "&lcid=" )
					.append( siteID ).append( "&url=" ).append( destinationURL );
				finalURL = b.toString();
				
				// If it's configured to open in the iframe...
				if( !newWindow )
				{
					// Get the frame height and custom height values
					String frameHeight = "";
					String customHeight = "";
					for( EZProxyEntry e : entries )
					{
						if( "ezproxy.frameHeight".equalsIgnoreCase( e.getName() ) )
							frameHeight = e.getValue();
						if( "ezproxy.customHeight".equalsIgnoreCase( e.getName() ) )
							customHeight = e.getValue();
					}
					
					// Setup the iframe; 'frameHeight.option9' = 'Something else', which means the user provided a custom height
					iframe.setVisibilityAllowed( true );
					iframe.add( new SimpleAttributeModifier( "src", finalURL ) );
					iframe.add( new SimpleAttributeModifier( "height", 
							( new ResourceModel( "frameHeight.option9" ).getObject().equalsIgnoreCase( frameHeight ) ) 	// If... (frameHeight.option9 = 'Something else')
							? customHeight 																				// True...
							: frameHeight ) );																			// False...
				}
			}
			
			// Otherwise, notify the user that the properties were missing in sakai.properties
			else
			{
				configuredPopup = new MultiLineLabel( "configuredPopupHeading", new ResourceModel( "heading.propsNotFound" ) );
				replace( configuredPopup );
			}
		}
	}
	
	/**
	 * Adds a javascript snippet to the page to open the rendered link in a new window
	 * only if the link has been configured, and it was set to open in a new window.
	 */
	@Override
	public void renderHead( IHeaderResponse response )
	{
		// Include any styles/javascript from the BasePage
		super.renderHead( response );
		
		// Add the javascript to popup the link in a new window if it has been configured to do so
		String javascript = "window.open(\"" + finalURL + "\");";
		if( isConfigured && newWindow && !propsNotFound )
			response.renderOnLoadJavascript( javascript );
	}
}
