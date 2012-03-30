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
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.validator.RangeValidator;

import ca.uwo.owl.ezproxy.model.EZProxyEntry;
import ca.uwo.owl.ezproxy.tool.model.EZProxyInputModel;

/**
 * The 'options' page, so the user can edit the configuration of the EZProxy link.
 * 
 * @author Brian Jones (bjones86@uwo.ca)
 *
 */
public class OptionsPage extends BasePage
{	
	// Class members
	@SuppressWarnings("unused")
	private static final Logger log 				= Logger.getLogger( OptionsPage.class );	// The logger
	private static final int 	MIN_CUSTOM_HEIGHT 	= 20;										// The minimum value allowed for custom frame height
	private static final int 	MAX_CUSTOM_HEIGHT 	= 9999;										// The maximum value allowed for custom frame height
	
	// Constructor
	public OptionsPage() 
	{		
		// Disable the options link, get the siteID, pageID, tool title
		disableLink( optionsLink );
		final String siteID = sakaiProxy.getCurrentSiteId();
		final String pageID = sakaiProxy.getCurrentPageId();
		String toolTitle = sakaiProxy.getToolTitle();
		String pageTitle = sakaiProxy.getPageTitle( siteID, pageID );
		boolean ableToConfig = sakaiProxy.isCurrentUserConfigAuth();
		
		// Create a list of valid protocols for URL validation, create the URL validator object
		String[] schemes = { "http", "https" };
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
				if( "ezproxy.frameHeight".equalsIgnoreCase( e.getName() ) )
					frameHeight = e.getValue();
				else if( "ezproxy.customHeight".equalsIgnoreCase( e.getName() ) )
					customHeight = e.getValue();
				else if( "ezproxy.sourceURL".equalsIgnoreCase( e.getName() ) )
					url = e.getValue();
				else if( "ezproxy.newWindow".equalsIgnoreCase( e.getName() ) )
					newWindow = e.getValue();
			}
		}
		
		// If no entries were returned, or the wrong number was returned, this must be the 'first run'
		else
			firstRunCheck = true;
		final boolean firstRun = firstRunCheck;
		
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
		configForm.add( new RequiredTextField<String>( "txtToolTitle" ).setRequired( true )
				.add( new SimpleAttributeModifier( "value", toolTitle ) ) );
		
		// Add the page title label and input
		configForm.add( new Label( "pageTitleLabel", new ResourceModel( "pageTitle" ) ) );
		configForm.add( new RequiredTextField<String>( "txtPageTitle" ).setRequired( true )
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
			ddModel.setObject( frameHeightOptions.get( frameHeightOptions.indexOf( frameHeight ) ) );
		else		
			ddModel.setObject( frameHeightOptions.get( 0 ) );
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
					container.setVisibilityAllowed( true );
				
				// Otherwise, hide the custom height input section
				else
					container.setVisibilityAllowed( false );
				
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
			container.add( new RequiredTextField<Integer>( "txtCustomHeight" )
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
		RequiredTextField<String> urlField = new RequiredTextField<String>( "txtSourceURL" );
		urlField.setRequired( true );
		urlField.add( ( url != null && !url.isEmpty() ) 				// If...
				? new SimpleAttributeModifier( "value", url )			// True...
				: new SimpleAttributeModifier( "value", "http://" ) );	// False...
		configForm.add( urlField );
		
		// Add the new window input and label
		CheckBox chk = new CheckBox( "chkNewWindow" );
		if( newWindow != null && !newWindow.isEmpty() )
			if( "true".equalsIgnoreCase( newWindow  ) )
				chk.add( new SimpleAttributeModifier( "checked", "checked" ) );
		if( firstRun )
			chk.add( new SimpleAttributeModifier( "checked", "checked" ) );
		configForm.add( chk );
		configForm.add( new Label( "newWindowLabel", new ResourceModel( "newWindow" ) ) );
		
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
				
				// Update the tool title and page title
				sakaiProxy.setToolTitle( siteID, pageID, sakaiProxy.getToolTitle(), model.getTxtToolTitle() );
				sakaiProxy.setPageTitle( siteID, pageID, model.getTxtPageTitle() );
				
				// Set the siteID and pageID
				EZProxyEntry e = new EZProxyEntry();
				e.setSiteID( siteID );
				e.setPageID( pageID );
				
				// Frame height
				e.setName( "ezproxy.frameHeight" );
				e.setValue( model.getDdFrameHeight() );
				sakaiProxy.setEZProxyEntry( e );
				
				// Custom height
				e.setName( "ezproxy.customHeight" );
				e.setValue( model.getTxtCustomHeight() );
				sakaiProxy.setEZProxyEntry( e );
				
				// URL
				e.setName( "ezproxy.sourceURL" );
				e.setValue( model.getTxtSourceURL() );
				sakaiProxy.setEZProxyEntry( e );
				
				// New window
				e.setName( "ezproxy.newWindow" );
				e.setValue( model.getChkNewWindow().toString() );
				sakaiProxy.setEZProxyEntry( e );
				
				// Return to the content page
				setResponsePage( ContentPage.class );
			}
		};
		btnUpdate.add( new SimpleAttributeModifier( "value", new ResourceModel( "update" ).getObject() ) );
		if( urlValidator.isValid( url ) )
			btnUpdate.setEnabled( true );
		else
			btnUpdate.setEnabled( false );
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
		btnCancel.add( new SimpleAttributeModifier( "value", new ResourceModel( "cancel" ).getObject() ) );
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
				
				// If the source URL is valid, enable the update button
				if( urlValidator.isValid( model.getTxtSourceURL() ) )
					btnUpdate.setEnabled( true );
				
				// Otherwise, the source URL is not valid, disable the update button
				else
					btnUpdate.setEnabled( false );
				
				// Re-render the component
				target.addComponent( buttonHolder );
			}
		} );
		
		// Add the form to the page (hide the form if the user is not authorized to configure the EZProxy Link)
		if( !ableToConfig )
			configForm.setVisibilityAllowed( false );
		add( configForm );
	}
}