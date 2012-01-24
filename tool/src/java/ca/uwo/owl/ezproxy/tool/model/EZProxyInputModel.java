package ca.uwo.owl.ezproxy.tool.model;

import org.apache.wicket.IClusterable;

/**
 * This provides a model for the EZProxy configuration form.
 * 
 * @author Brian Jones (bjones86@uwo.ca)
 *
 */
public class EZProxyInputModel implements IClusterable
{
	// Class members
	private static final long serialVersionUID = 4311374585295689966L;
	
	// Instance members
	private String txtToolTitle 	= "";		// Holds the value of tool title entered
	private String txtPageTitle		= "";		// Holds the value of page title entered
	private String ddFrameHeight 	= "";		// Holds the value of the selected item from the frame height drop down
	private String txtCustomHeight 	= "";		// Holds the value of the custom height entered
	private String txtSourceURL 	= "";		// Holds the value of the source url entered
	private Boolean chkNewWindow	= false;	// Holds the value of whether the 'open in new window' checkbox was checked
	
	// Constructor(s)
	public EZProxyInputModel() {}
	
	// Getters
	public String getTxtToolTitle()  	{ return txtToolTitle; }
	public String getTxtPageTitle()		{ return txtPageTitle; }
	public String getDdFrameHeight() 	{ return ddFrameHeight; }
	public String getTxtCustomHeight() 	{ return txtCustomHeight; }
	public String getTxtSourceURL()		{ return txtSourceURL; }
	public Boolean getChkNewWindow()	{ return chkNewWindow; }
	
	// Setters
	public void setTxtToolTitle	( String txtToolTitle )  	 { this.txtToolTitle 	= txtToolTitle; }
	public void setTxtPageTitle( String txtPageTitle )		 { this.txtPageTitle 	= txtPageTitle; }
	public void setDdFrameHeight( String ddFrameHeight ) 	 { this.ddFrameHeight 	= ddFrameHeight; }
	public void setTxtCustomHeight( String txtCustomHeight ) { this.txtCustomHeight = txtCustomHeight; }
	public void setTxtSourceURL( String txtSourceURL )		 { this.txtSourceURL 	= txtSourceURL; }
	public void setChkNewWindow( Boolean chkNewWindow )		 { this.chkNewWindow 	= chkNewWindow; }
	
	/** 
     * Returns a string representation of the EZProxy configuration input model object.
     */
    @Override
    public String toString()
    {
    	StringBuilder b = new StringBuilder();
    	b.append( "{ " ).append( "toolTitle=" ).append( txtToolTitle ).append( ", pageTitle=" ).append( txtPageTitle )
    		.append( ", frameHeight=" ).append( ddFrameHeight ).append( ", customHeight=" ).append( txtCustomHeight )
    		.append( ", sourceURL=" ).append( txtSourceURL ).append( ", newWindow=" ).append( chkNewWindow ).append( " }" );
    	return b.toString();
    }
}
