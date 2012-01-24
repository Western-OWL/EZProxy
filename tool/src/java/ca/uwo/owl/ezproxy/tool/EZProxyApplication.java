package ca.uwo.owl.ezproxy.tool;

import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

import ca.uwo.owl.ezproxy.tool.pages.ContentPage;

/**
 * Main application class for EZProxy
 * 
 * @author Brian Jones (bjones86@uwo.ca)
 *
 */
public class EZProxyApplication extends WebApplication
{
	@Override
	protected void init()
	{	
		//Configure for Spring injection
		addComponentInstantiationListener( new SpringComponentInjector( this ) );
		
		//Don't throw an exception if we are missing a property, just fallback
		getResourceSettings().setThrowExceptionOnMissingResource( false );
		
		//Remove the wicket specific tags from the generated markup
		getMarkupSettings().setStripWicketTags( true );
		
		//Don't add any extra tags around a disabled link (default is <em></em>)
		getMarkupSettings().setDefaultBeforeDisabledLink( null );
		getMarkupSettings().setDefaultAfterDisabledLink( null );
				
		// On Wicket session timeout, redirect to main page
		getApplicationSettings().setPageExpiredErrorPage( ContentPage.class );
		getApplicationSettings().setAccessDeniedPage( ContentPage.class );
		
		//to put this app into deployment mode, see web.xml
	}
	
	/**
	 *  Throw RuntimeExceptions so they are caught by the Sakai ErrorReportHandler(non-Javadoc)
	 *  
	 * @see org.apache.wicket.protocol.http.WebApplication#newRequestCycle(org.apache.wicket.Request, org.apache.wicket.Response)
	 */
	@Override
	public RequestCycle newRequestCycle( Request request, Response response )
	{
		return new WebRequestCycle( this, (WebRequest)request, (WebResponse)response )
		{
			@Override
			public Page onRuntimeException( Page page, RuntimeException e )
			{
				throw e;
			}
		};
	}
	
	/**
	 * The main page for our app
	 * 
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	public Class<ContentPage> getHomePage()
	{
		return ContentPage.class;
	}
	
	
	/**
     * Constructor
     */
	public EZProxyApplication()	{}
}