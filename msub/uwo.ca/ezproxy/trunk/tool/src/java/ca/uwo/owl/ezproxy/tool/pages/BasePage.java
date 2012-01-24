package ca.uwo.owl.ezproxy.tool.pages;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import ca.uwo.owl.ezproxy.logic.EZProxyLogic;
import ca.uwo.owl.ezproxy.logic.SakaiProxy;


/**
 * This is the base page for EZProxy. It sets up the containing markup and top navigation.
 * All top level pages should extend from this page so as to keep the same navigation. The content for those pages will
 * be rendered in the main area below the top nav.
 * 
 * It also allows us to setup the API injection and any other common methods, which are then made available in the other pages.
 * 
 * @author Brian Jones (bjones86@uwo.ca)
 *
 */
public class BasePage extends WebPage implements IHeaderContributor
{
	// Logger
	private static final Logger log = Logger.getLogger( BasePage.class ); 
	
	// Bean that provides access to sakai API
	@SpringBean( name="ca.uwo.owl.ezproxy.logic.SakaiProxy" )
	protected SakaiProxy sakaiProxy;
	
	// Bean that provides access to EZProxy DAO
	@SpringBean( name="ca.uwo.owl.ezproxy.logic.EZProxyLogic" )
	protected EZProxyLogic ezProxyLogic;
	
	// The 'Options' link and feedback panel
	Link<Void> optionsLink;	
	FeedbackPanel feedbackPanel;
	
	// Constructor
	public BasePage()
	{
		log.debug( "BasePage()" );		
		
    	// Create the options link
		optionsLink = new Link<Void>( "optionsLink" )
		{
			private static final long serialVersionUID = 1L;
			public void onClick() 
			{
				setResponsePage( new OptionsPage() );
			}
		};
		
		// If the current user does not have the right to configure the EZProxy link, hide the optionsLink
		if( !sakaiProxy.isCurrentUserConfigAuth() )
			optionsLink.setVisibilityAllowed( false );
		else
		{
			optionsLink.add( new Label( "optionsLinkLabel", new ResourceModel( "optionsLink" ) ).setRenderBodyOnly( true ) );
			optionsLink.add( new AttributeModifier( "title", true, new ResourceModel( "optionsLink.tooltip" ) ) );
		}
		add( optionsLink );
		
		
		// Add a FeedbackPanel for displaying our messages
        feedbackPanel = new FeedbackPanel( "feedback" )
        {
        	private static final long serialVersionUID = -4665970672206692563L;

			@Override
        	protected Component newMessageDisplayComponent( final String id, final FeedbackMessage message )
			{
        		final Component newMessageDisplayComponent = super.newMessageDisplayComponent( id, message );

        		if( message.getLevel() == FeedbackMessage.ERROR ||
        			message.getLevel() == FeedbackMessage.DEBUG ||
        			message.getLevel() == FeedbackMessage.FATAL ||
        			message.getLevel() == FeedbackMessage.WARNING )
        			add( new SimpleAttributeModifier( "class", "alertMessage" ) );
        		else if( message.getLevel() == FeedbackMessage.INFO )
        			add( new SimpleAttributeModifier( "class", "success" ) );

        		return newMessageDisplayComponent;
        	}
        };
        add( feedbackPanel );
    }
	
	/**
	 * Helper to clear the feedbackpanel display.
	 * @param f	FeedBackPanel
	 */
	public void clearFeedback( FeedbackPanel f ) 
	{
		if( !f.hasFeedbackMessage() )
			f.add( new SimpleAttributeModifier( "class", "" ) );
	}
	
	/**
	 * This block adds the required wrapper markup to style it like a Sakai tool. 
	 * Add to this any additional CSS or JS references that you need.
	 * 
	 */
	public void renderHead( IHeaderResponse response )
	{
		// Get Sakai skin
		String skinRepo = sakaiProxy.getSkinRepoProperty();
		String toolCSS = sakaiProxy.getToolSkinCSS( skinRepo );
		String toolBaseCSS = skinRepo + "/tool_base.css";
		
		// Sakai additions
		response.renderJavascriptReference( "/library/js/headscripts.js" );
		response.renderCSSReference( toolBaseCSS );
		response.renderCSSReference( toolCSS );
		response.renderOnLoadJavascript( "setMainFrameHeight( window.name )" );
		
		// Tool additions (at end so we can override if required)
		response.renderString( "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />" );
	}
	
	/** 
	 * Helper to disable a link. Add the Sakai class 'current'.
	 */
	protected void disableLink( Link<Void> l ) 
	{
		l.add( new AttributeAppender( "class", new Model<String>( "current" ), " ") );
		l.setRenderBodyOnly( true );
		l.setEnabled( false );
	}
}
