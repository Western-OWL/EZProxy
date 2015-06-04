package ca.uwo.owl.ezproxy.tool.pages;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.AttributeModifier;
import org.sakaiproject.component.cover.ServerConfigurationService;

import ca.uwo.owl.ezproxy.logic.SharedSecretAuth;
import ca.uwo.owl.ezproxy.model.EZProxyEntry;
import ca.uwo.owl.ezproxy.utilities.EZProxyConstants;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.request.resource.ContextRelativeResource;

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
    
    private static final String SAK_PROP_EZPROXY_SERVICE_URL = "ezproxy.url";
    private static final String SAK_PROP_EZPROXY_SHARED_SECTRET = "ezproxy.secret";

    // Constructor
    public ContentPage()
    {
        // Get any generic info needed
        final String siteID = sakaiProxy.getCurrentSiteId();
        final String pageID = sakaiProxy.getCurrentPageId();
        List<EZProxyEntry> entries = sakaiProxy.getEZProxyEntry( siteID, pageID );
        boolean ableToConfig = sakaiProxy.isCurrentUserConfigAuth();
        boolean ableToView = sakaiProxy.isCurrentUserViewAuth();
        Image imageIcon = new Image( "icon", new ContextRelativeResource( "images/error.png" ) );

        // If the user has the authorization to view an EZProxy link...
        if( ableToView )
        {
            // If this EZProxy Link has not yet been setup (has no entries), inform the user
            Label notConfiguredHeading = new Label( "notConfiguredHeading", 
                    ( ableToConfig )												// If...
                    ? new ResourceModel( "heading.notConfigured" )					// True...
                    : new ResourceModel( "heading.notConfigured.notAuthorized" ) );	// False...
            MultiLineLabel configuredPopup = new MultiLineLabel( "configuredPopupHeading", new ResourceModel( "heading.configuredPopup" ) );
            configuredPopup.setVisibilityAllowed( false );

            // If there were the right amount of entries returned...
            String destinationURL = "";
            if( entries != null && entries.size() == NUM_ENTRIES_PER_LINK )
            {
                // It has been configured
                isConfigured = true;
                
                // Get the destination URL
                for( EZProxyEntry e : entries )
                {
                    if( EZProxyConstants.EZPROXY_PROP_SOURCE_URL.equalsIgnoreCase( e.getName() ) )
                    {
                        destinationURL = e.getValue();
                    }
                }

                // Determine if it was configured for a new window/tab
                notConfiguredHeading.setVisibilityAllowed( false );
                for( EZProxyEntry e : entries )
                {
                    if( EZProxyConstants.EZPROXY_PROP_NEW_WINDOW.equalsIgnoreCase( e.getName() ) )
                    {
                        // If the config options were set to open in a new window OR the destination URL is NOT https...
                        if( "true".equalsIgnoreCase( e.getValue() ) || !destinationURL.toLowerCase().startsWith( "https" ) )
                        {
                            newWindow = true;
                        }
                    }
                }

                // If it's a new window link, show the info label, change to the info icon
                if( newWindow )
                {
                    configuredPopup.setVisibilityAllowed( true );
                    imageIcon = new Image( "icon", new ContextRelativeResource( "images/info.png" ) );
                }
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
                String serviceURL = ServerConfigurationService.getString( SAK_PROP_EZPROXY_SERVICE_URL );
                String sharedSecret = ServerConfigurationService.getString( SAK_PROP_EZPROXY_SHARED_SECTRET );
                String userEid = sakaiProxy.getCurrentUserEid();
                String mac = "";

                // Make sure the service URL and shared secret were in sakai.properties
                if( serviceURL != null && !serviceURL.isEmpty() && sharedSecret != null && !sharedSecret.isEmpty() )
                {
                    propsNotFound = false;
                }

                // If the properties were there, continue...
                if( !propsNotFound )
                {
                    // Generate the MAC, and the final URL
                    try { mac = SharedSecretAuth.generateMAC( userEid + siteID, sharedSecret );	}
                    catch( NoSuchAlgorithmException ex ) { log.error( ex ); }
                    catch( IndexOutOfBoundsException ex ) { log.error( ex ); }
                    finalURL = serviceURL + "?mac=" + mac + "&pid=" + userEid + "&lcid=" + siteID + "&url=" + destinationURL;

                    // If it's configured to open in the iframe...
                    if( !newWindow )
                    {
                        // Get the frame height and custom height values
                        String frameHeight = "";
                        String customHeight = "";
                        for( EZProxyEntry e : entries )
                        {
                            if( EZProxyConstants.EZPROXY_PROP_FRAME_HEIGHT.equalsIgnoreCase( e.getName() ) )
                            {
                                frameHeight = e.getValue();
                            }
                            if( EZProxyConstants.EZPROXY_PROP_CUSTOM_HEIGHT.equalsIgnoreCase( e.getName() ) )
                            {
                                customHeight = e.getValue();
                            }
                        }

                        // Setup the iframe; 'frameHeight.option9' = 'Something else', which means the user provided a custom height
                        iframe.setVisibilityAllowed( true );
                        iframe.add( AttributeModifier.replace( "src", finalURL ) );
                        iframe.add( AttributeModifier.replace( "height", 
                                ( new ResourceModel( "frameHeight.option9" ).getObject().equalsIgnoreCase( frameHeight ) ) 	// If... (frameHeight.option9 = 'Something else')
                                ? customHeight 																				// True...
                                : frameHeight ) );																			// False...

                        // Set the image icon to blank (it's been configured, they're allowed to see it, and it opens in the same page)
                        imageIcon = new NonCachingImage( "icon" );
                        imageIcon.setVisibilityAllowed( false );
                    }
                }

                // Otherwise, notify the user that the properties were missing in sakai.properties
                else
                {
                    configuredPopup = new MultiLineLabel( "configuredPopupHeading", new ResourceModel( "heading.propsNotFound" ) );
                    replace( configuredPopup );
                }
            }

            // If the link has not been configured, AND they have the permission to configure it; force them to the config page
            else if( !isConfigured && ableToConfig )
            {
                this.setResponsePage( OptionsPage.class );
            }

            // Add the image
            add( imageIcon );
        }

        // Otherwise, the user does not have the authority to view (or configure) EZProxy links
        else
        {
            // Generate the appropriate labels
            Label notConfiguredHeading = new Label( "notConfiguredHeading", new ResourceModel( "heading.notAuthorized" ) );
            MultiLineLabel configuredPopup = new MultiLineLabel( "configuredPopupHeading", new ResourceModel( "heading.notAutorized.view" ) );

            // Add the labels and Iframe component
            add( notConfiguredHeading );
            add( configuredPopup );
            WebMarkupContainer iframe = new WebMarkupContainer( "iframe" );
            iframe.setVisibilityAllowed( false );
            add( iframe );
            add( imageIcon );
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
        {
            response.render(OnLoadHeaderItem.forScript(javascript));
        }
    }
}
