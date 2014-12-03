package ca.uwo.owl.ezproxy.tool.pages;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.validator.RangeValidator;

import ca.uwo.owl.ezproxy.model.EZProxyEntry;
import ca.uwo.owl.ezproxy.tool.model.EZProxyInputModel;
import ca.uwo.owl.ezproxy.utilities.EZProxyConstants;

/**
 * The 'options' page, so the user can edit the configuration of the EZProxy link.
 * 
 * @author Brian Jones (bjones86@uwo.ca)
 *
 */
public class OptionsPage extends BasePage
{
    // Class members
    private static final Logger log 				= Logger.getLogger( OptionsPage.class );	// The logger
    private static final int 	MIN_CUSTOM_HEIGHT 	= 20;										// The minimum value allowed for custom frame height
    private static final int 	MAX_CUSTOM_HEIGHT 	= 9999;										// The maximum value allowed for custom frame height

    // Constructor
    public OptionsPage() 
    {		
        if( log.isDebugEnabled() )
        {
            log.debug( "OptionsPage()" );
        }

        // Disable the options link, get the siteID, pageID, tool title
        disableLink( optionsLink );
        final String siteID = sakaiProxy.getCurrentSiteId();
        final String pageID = sakaiProxy.getCurrentPageId();
        String toolTitle = sakaiProxy.getToolTitle();
        String pageTitle = sakaiProxy.getPageTitle( siteID, pageID );
        boolean ableToConfig = sakaiProxy.isCurrentUserConfigAuth();

        // Create a list of valid protocols for URL validation, create the URL validator object
        final String[] schemes = { EZProxyConstants.URL_PROTOCOL_HTTP, EZProxyConstants.URL_PROTOCOL_HTTPS };
        final UrlValidator urlValidator = new UrlValidator( schemes );

        // Get the EZProxyEntries
        boolean firstRunCheck = false;
        List<EZProxyEntry> entries = sakaiProxy.getEZProxyEntry( siteID, pageID );
        String frameHeight = null;
        String customHeight = null;
        String url = null;
        String newWindow = null;
        if( entries != null && entries.size() == NUM_ENTRIES_PER_LINK )
        {
            // Get the values
            for( EZProxyEntry e : entries )
            {
                if( EZProxyConstants.EZPROXY_PROP_FRAME_HEIGHT.equalsIgnoreCase( e.getName() ) )
                {
                    frameHeight = e.getValue();
                }
                else if( EZProxyConstants.EZPROXY_PROP_CUSTOM_HEIGHT.equalsIgnoreCase( e.getName() ) )
                {
                    customHeight = e.getValue();
                }
                else if( EZProxyConstants.EZPROXY_PROP_SOURCE_URL.equalsIgnoreCase( e.getName() ) )
                {
                    url = e.getValue();
                }
                else if( EZProxyConstants.EZPROXY_PROP_NEW_WINDOW.equalsIgnoreCase( e.getName() ) )
                {
                    newWindow = e.getValue();
                }
            }
        }

        // If no entries were returned, or the wrong number was returned, this must be the 'first run'
        else
        {
            firstRunCheck = true;
        }
        final boolean firstRun = firstRunCheck;
        
        // Default to HTTPS if not previously set
        if( url == null || url.isEmpty() )
        {
            url = "https://";
        }

        // Add the title label
        add( new Label( "heading", 
                ( ableToConfig )									// If...
                ? new ResourceModel( "heading" )					// True...
                : new ResourceModel( "heading.notAuthorized" ) ) );	// False...

        // Create the form and set the model
        final Form<EZProxyInputModel> configForm = new Form<EZProxyInputModel>( "configureEZProxyLinkForm" );
        configForm.setModel( new CompoundPropertyModel<EZProxyInputModel>( new EZProxyInputModel() ) );

        // Add the tool title label and input
        configForm.add( new Label( "toolTitleLabel", new ResourceModel( "toolTitle" ) ) );
        configForm.add( new TextField<String>( "txtToolTitle" ).setRequired( true )
                .add( new SimpleAttributeModifier( "value", toolTitle ) ) );

        // Add the page title label and input
        configForm.add( new Label( "pageTitleLabel", new ResourceModel( "pageTitle" ) ) );
        configForm.add( new TextField<String>( "txtPageTitle" ).setRequired( true )
                .add( new SimpleAttributeModifier( "value", pageTitle ) ) );

        // Get all the frame height drop down options
        List<String> frameHeightOptions = new ArrayList<String>();
        for( int i = 1; i < 10; ++i )
        {
            ResourceModel model = new ResourceModel( "frameHeight.option" + i );
            frameHeightOptions.add( model.getObject() );
        }

        // Add the frame height drop down
        final WebMarkupContainer holder = new WebMarkupContainer( "holder" );
        final WebMarkupContainer container = new WebMarkupContainer( "container" );
        holder.setOutputMarkupId( true );
        container.setOutputMarkupId( true );
        configForm.add( new Label( "frameHeightLabel", new ResourceModel( "frameHeight" ) ) );
        final Model<String> ddModel = new Model<String>();
        if( frameHeight != null && !frameHeight.isEmpty() )
        {
            ddModel.setObject( frameHeightOptions.get( frameHeightOptions.indexOf( frameHeight ) ) );
        }
        else		
        {
            ddModel.setObject( frameHeightOptions.get( 0 ) );
        }
        DropDownChoice<String> ddFrameHeightOptions = new DropDownChoice<String>( "ddFrameHeight", ddModel, frameHeightOptions );
        ddFrameHeightOptions.add( new AjaxFormComponentUpdatingBehavior( "onchange" )
        {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate( AjaxRequestTarget target )
            {
                ResourceModel model = new ResourceModel( "frameHeight.option9" ); // custom height, 'Something else'
                String customOption = model.getObject();

                // If 'something else' is selected, show the custom height input section
                if( customOption.equalsIgnoreCase( ddModel.getObject() ) )
                {
                    container.setVisibilityAllowed( true );
                }

                // Otherwise, hide the custom height input section
                else
                {
                    container.setVisibilityAllowed( false );
                }

                // Re-render the component
                target.addComponent( holder );
            }
        } );
        configForm.add( ddFrameHeightOptions );

        // Add the custom height label and input
        container.add( new Label( "customHeightLabel", new ResourceModel( "frameHeight.custom" ) ) );
        if( frameHeight != null && !frameHeight.isEmpty() 
                && new ResourceModel( "frameHeight.option9" ).getObject().equalsIgnoreCase( frameHeight ) ) // frameHeight.option9 = 'Something else'
        {
            container.add( new TextField<Integer>( "txtCustomHeight" )
                    .setRequired( true ).setType( Integer.class )
                    .add( new RangeValidator<Integer>( MIN_CUSTOM_HEIGHT, MAX_CUSTOM_HEIGHT ) )
                    .add( new SimpleAttributeModifier( "value", customHeight ) ) );
            container.setVisibilityAllowed( true );

        }
        else
        {
            container.add( new TextField<String>( "txtCustomHeight" ) );
            container.setVisibilityAllowed( false );
        }
        container.add( new Label( "customHeightUnitLabel", new ResourceModel( "frameHeight.custom.unit" ) ) ); // unit is pixels
        holder.add( container );
        configForm.add( holder );

        // Add the source url label and input
        configForm.add( new Label( "sourceURLLabel", new ResourceModel( "url" ) ) );
        TextField<String> urlField = new TextField<String>( "txtSourceURL" );
        urlField.setRequired( true );
        urlField.add( new SimpleAttributeModifier( "value", url ) );
        configForm.add( urlField );
        
        // Determine if previously saved URL (or default text) is HTTPS
        boolean isHTTPS = false;
        if( url.toLowerCase().startsWith( EZProxyConstants.URL_PROTOCOL_HTTPS ) )
        {
            isHTTPS = true;
        }

        // Add the new window input and label
        final WebMarkupContainer checkboxHolder = new WebMarkupContainer( "checkboxHolder" );
        checkboxHolder.setOutputMarkupId( true );
        configForm.add( checkboxHolder );
        final CheckBox chk = new CheckBox( "chkNewWindow" );
        final Label chkLabel = new Label( "newWindowLabel", new ResourceModel( "newWindow" ) );
        if( newWindow != null && !newWindow.isEmpty() )
        {
            boolean checked = false;
            boolean disabled = false;
            
            // If the previously saved URL is set to open in a new window AND is HTTPS, check the box and keep it enabled
            if( "true".equalsIgnoreCase( newWindow  ) && isHTTPS )
            {
                checked = true;
            }
            
            // If the previously saved URL is set to open in a new window AND is HTTP, check the box and disable it
            // OR, if the previously saved URL is NOT set to open in a new window AND is HTTP, check the box and disable it
            else if( ("true".equalsIgnoreCase( newWindow ) && !isHTTPS)
                    || ("false".equalsIgnoreCase( newWindow ) && !isHTTPS) )
            {
                checked = true;
                disabled = true;
            }
            
            if( checked )
            {
                chk.add( new SimpleAttributeModifier( "checked", "checked" ) );
            }
            if( disabled )
            {
                chk.setEnabled( false );
            }
        }
        if( firstRun )
        {
            chk.add( new SimpleAttributeModifier( "checked", "checked" ) );
        }
        checkboxHolder.add( chk );
        checkboxHolder.add( chkLabel );
        
        // Plaintext URL warning message
        final WebMarkupContainer warningHolder = new WebMarkupContainer( "warningHolder" );
        warningHolder.setOutputMarkupId( true );
        configForm.add( warningHolder );
        final Label plaintextWarning = new Label( "plaintextWarning", new ResourceModel( "plaintextURL.warning" ) );
        if( isHTTPS )
        {
            plaintextWarning.setVisibilityAllowed( false );
        }
        warningHolder.add( plaintextWarning );

        // Add the update button
        final WebMarkupContainer buttonHolder = new WebMarkupContainer( "buttonHolder" );
        buttonHolder.setOutputMarkupId( true );
        configForm.add( buttonHolder );
        final Button btnUpdate = new Button( "btnUpdate" )
        {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit()
            {
                // Get the modified model, update the value from the drop down
                EZProxyInputModel model = (EZProxyInputModel) configForm.getModelObject();
                model.setDdFrameHeight( ddModel.getObject() );

                // If the source URL IS a valid URL, save all the info
                if( urlValidator.isValid( model.getTxtSourceURL() ) )
                {
                    // Update the tool title and page title
                    sakaiProxy.setToolTitle( siteID, pageID, sakaiProxy.getToolTitle(), model.getTxtToolTitle() );
                    sakaiProxy.setPageTitle( siteID, pageID, model.getTxtPageTitle() );

                    // Set the siteID and pageID
                    EZProxyEntry e = new EZProxyEntry();
                    e.setSiteID( siteID );
                    e.setPageID( pageID );

                    // Frame height
                    e.setName( EZProxyConstants.EZPROXY_PROP_FRAME_HEIGHT );
                    e.setValue( model.getDdFrameHeight() );
                    sakaiProxy.setEZProxyEntry( e );

                    // Custom height
                    e.setName( EZProxyConstants.EZPROXY_PROP_CUSTOM_HEIGHT );
                    e.setValue( model.getTxtCustomHeight() );
                    sakaiProxy.setEZProxyEntry( e );

                    // URL
                    e.setName( EZProxyConstants.EZPROXY_PROP_SOURCE_URL );
                    e.setValue( model.getTxtSourceURL() );
                    sakaiProxy.setEZProxyEntry( e );

                    // New window
                    e.setName( EZProxyConstants.EZPROXY_PROP_NEW_WINDOW );
                    e.setValue( model.getChkNewWindow().toString() );
                    sakaiProxy.setEZProxyEntry( e );

                    // Return to the content page
                    setResponsePage( ContentPage.class );
                }

                // HACK; if it's not valid, just get a new copy of the page
                // (this avoids the empty half-shown feedback panel and the update button being enabled when it shouldn't be)
                else
                {
                    setResponsePage( OptionsPage.class );
                }
            }
        };
        
        btnUpdate.add( new SimpleAttributeModifier( "value", new ResourceModel( "update" ).getObject() ) );
        if( urlValidator.isValid( url ) )
        {
            btnUpdate.setEnabled( true );
        }
        else
        {
            btnUpdate.setEnabled( false );
        }
        buttonHolder.add( btnUpdate );

        // Add the cancel button
        Button btnCancel = new Button( "btnCancel" )
        {
            private static final long serialVersionUID = 248725748846235600L;

            @Override
            public void onSubmit()
            {
                setResponsePage( ContentPage.class );
            }
        };
        
        btnCancel.setDefaultFormProcessing( false );
        if( !firstRun )
        {
            btnCancel.add( new SimpleAttributeModifier( "value", new ResourceModel( "cancel" ).getObject() ) );
        }
        else
        {
            btnCancel.add( new SimpleAttributeModifier( "value", new ResourceModel( "clear" ).getObject() ) );
        }
        buttonHolder.add( btnCancel );

        // Add the onchange AJAX behaviour to the URL text field
        urlField.add( new OnChangeAjaxBehavior()
        {
            private static final long serialVersionUID = -8344894283071902526L;

            @Override
            protected void onUpdate( AjaxRequestTarget target )
            {
                // Get the modified model
                EZProxyInputModel model = (EZProxyInputModel) configForm.getModelObject();
                String URL = model.getTxtSourceURL();
                
                // If a URL is provided, determine if HTTP/HTTPS...
                if( URL != null && !URL.isEmpty() )
                {
                    // If HTTP (and not HTTPS), show warning, force check the checkbox, disable the checkbox
                    if( URL.toLowerCase().startsWith( EZProxyConstants.URL_PROTOCOL_HTTP )
                            && !URL.toLowerCase().startsWith( EZProxyConstants.URL_PROTOCOL_HTTPS ) )
                    {
                        plaintextWarning.setVisibilityAllowed( true );
                        chk.add( new SimpleAttributeModifier( "checked", "checked" ) );
                        chk.setEnabled( false );
                        chkLabel.add( new SimpleAttributeModifier( "class", "disabled" ) );
                    }
                    
                    // Otherwise (for HTTPS, or any invalid protocol/URL), hide the warning and enable the checkbox
                    // (invalid protocols/URLs will not be allowed via browser and server validation)
                    else
                    {
                        plaintextWarning.setVisibilityAllowed( false );
                        chk.setEnabled( true );
                        chkLabel.add( new SimpleAttributeModifier( "class", "" ) );
                    }
                }

                // If the source URL is valid, enable the update button
                if( urlValidator.isValid( URL ) )
                {
                    btnUpdate.setEnabled( true );
                }

                // Otherwise, the source URL is not valid, disable the update button
                else
                {
                    btnUpdate.setEnabled( false );
                }

                // Re-render the components
                target.addComponent( checkboxHolder );
                target.addComponent( warningHolder );
                target.addComponent( buttonHolder );
            }
        } );

        // Add the form to the page (hide the form if the user is not authorized to configure the EZProxy Link)
        if( !ableToConfig )
        {
            configForm.setVisibilityAllowed( false );
        }
        add( configForm );
    }
}