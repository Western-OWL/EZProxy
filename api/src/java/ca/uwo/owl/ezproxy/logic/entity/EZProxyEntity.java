package ca.uwo.owl.ezproxy.logic.entity;

/**
 * Wrapper class to hold only the fields that we want to return to the EntityBroker
 * 
 * @author bjones86
 */
public class EZProxyEntity implements Comparable<Object>
{
	// Instance Members
	private String siteID;
	private String pageID;
	private String pageTitle;
	private String toolTitle;
	private String sourceURL;
	private String title;
	
	// Getters
	public String	getId()								{ return ( siteID != null && pageID != null ) ? siteID + ":" + pageID : null; }
	public String 	getSiteID() 						{ return this.siteID; }
	public String 	getPageID() 						{ return this.pageID; }
	public String 	getPageTitle() 						{ return this.pageTitle; }
	public String 	getToolTitle() 						{ return this.toolTitle; }
	public String	getSourceURL()						{ return this.sourceURL; }
	public String	getTitle()							{ return this.title; }
	
	// Setters
	public void 	setSiteID( String siteID ) 			{ this.siteID = siteID; }
	public void 	setPageID( String pageID ) 			{ this.pageID = pageID; }
	public void 	setPageTitle( String pageTitle ) 	{ this.pageTitle = pageTitle; }
	public void 	setToolTitle( String toolTitle ) 	{ this.toolTitle = toolTitle; }
	public void		setSourceURL( String sourceURL )	{ this.sourceURL = sourceURL; }
	public void		setTitle( String title )			{ this.title = title; }
	
	// Constructors
	public EZProxyEntity() {}
	public EZProxyEntity( String siteID, String pageID, String pageTitle, String toolTitle, String sourceURL, String title )
	{
		this.siteID 	= siteID;
		this.pageID 	= pageID;
		this.pageTitle 	= pageTitle;
		this.toolTitle 	= toolTitle;
		this.sourceURL 	= sourceURL;
		this.title		= title;
	}
	
	/**
	 * Default sort by entity id (siteID:pageID)
	 * 
	 * @param obj the object to compare to
	 * @return an integer representing the exact similarity of the two strings
	 */
	public int compareTo( Object obj )
	{
		EZProxyEntity entity = (EZProxyEntity) obj;
		return nullSafeStringComparator( entity.getId(), this.getId() );
	}
	
	/**
	 * Null safe string comparator. Loosely taken from stackoverflow:
	 * http://stackoverflow.com/questions/481813/how-to-simplify-a-null-safe-compareto-implementation
	 * 
	 * @param one the first string to compare
	 * @param two the second string to compare
	 * @return an integer representing the exact similarity of the two strings
	 */
	private int nullSafeStringComparator( String one, String two )
	{
		if( one == null ^ two == null )
			return ( one == null ) ? -1 : 1;
		
		if( one == null && two == null )
			return 0;
		
		return one.compareToIgnoreCase( two );
	}
}
