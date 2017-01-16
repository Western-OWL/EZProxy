package ca.uwo.owl.ezproxy.tool.pages;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.devutils.debugbar.DebugBar;

import ca.uwo.owl.ezproxy.logic.SakaiProxy;

/**
 * This is the base page for EZProxy. It sets up the containing markup and top navigation.
 * All top level pages should extend from this page so as to keep the same navigation. The content for those pages will
 * be rendered in the main area below the top nav.
 * 
 * It also allows us to setup the API injection and any other common methods, which are then made available in the other pages.
 * 
 * @author Brian Jones (bjones86@uwo.ca)
 * @author plukasew
 *
 */
public class BasePage extends WebPage implements IHeaderContributor
{
    // Class members
    private static final Logger LOG = Logger.getLogger( BasePage.class );   // The logger 
    protected static final int NUM_ENTRIES_PER_LINK = 4;                    // The number of rows per EZProxy link in the database
    protected static final String EZPROXY_CSS = "styles/ezproxy.css";

    // Bean that provides access to sakai API
    @SpringBean( name="ca.uwo.owl.ezproxy.logic.SakaiProxy" )
    protected SakaiProxy sakaiProxy;

    // The 'Options' link and feedback panel
    Link<Void> optionsLink;	
    FeedbackPanel feedbackPanel;

    // Constructor
    public BasePage()
    {
        LOG.debug( "BasePage()" );
        add(new DebugBar("debug"));

        // Create the options link
        optionsLink = new Link<Void>( "optionsLink" )
        {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() 
            {
                setResponsePage( new OptionsPage() );
            }
        };

        // If the current user does not have the right to configure the EZProxy link, hide the optionsLink
        if( !sakaiProxy.isCurrentUserConfigAuth() )
        {
            optionsLink.setVisibilityAllowed( false );
        }
        else
        {
            optionsLink.add( new Label( "optionsLinkLabel", new ResourceModel( "optionsLink" ) ).setRenderBodyOnly( true ) );
            optionsLink.add( new AttributeModifier( "title", new ResourceModel( "optionsLink.tooltip" ) ) );
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
                {
                    add( AttributeModifier.replace( "class", "alertMessage" ) );
                }
                else if( message.getLevel() == FeedbackMessage.INFO )
                {
                    add( AttributeModifier.replace( "class", "success" ) );
                }

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
        {
            f.add( AttributeModifier.remove("class"));
        }
    }

    /**
     * This block adds the required wrapper markup to style it like a Sakai tool. 
     * Add to this any additional CSS or JS references that you need.
     * 
     * @param response
     */
    @Override
    public void renderHead( IHeaderResponse response )
    {
        super.renderHead(response);

        //get the Sakai skin header fragment from the request attribute
        HttpServletRequest request = (HttpServletRequest)getRequest().getContainerRequest();

        response.render(StringHeaderItem.forString((String)request.getAttribute("sakai.html.head")));
        response.render(OnLoadHeaderItem.forScript("setMainFrameHeight( window.name )"));

        // Tool additions (at end so we can override if required)
        response.render(StringHeaderItem.forString( "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />" ));
        
        // Tool CSS
        response.render(CssHeaderItem.forUrl(EZPROXY_CSS));
    }

    /** 
     * Helper to disable a link. Add the Sakai class 'current'.
     * @param l
     */
    protected void disableLink( Link<Void> l ) 
    {
        l.add( new AttributeAppender( "class", new Model<>( "current" ), " ") );
        l.setRenderBodyOnly( true );
        l.setEnabled( false );
    }
}
