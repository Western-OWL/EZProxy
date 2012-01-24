package ca.uwo.owl.ezproxy.model;

import java.io.Serializable;

import lombok.Data;

/**
 * This class models a single entry (row) in the EZProxy database table.
 * 
 * @author Brian Jones (bjones86@uwo.ca)
 *
 */
@Data
public class EZProxyEntry implements Serializable
{
	// Class members
	private static final long serialVersionUID = -2666053598559704253L;
	
	// Instance members
	private String siteID;	// The siteID of the EZProxy link
	private String pageID;	// The pageID of the EZProxy link
	private String name;	// The name of the field this entry contains
	private String value;	// The value of the field this entry contains
	
	// Constructors
	public EZProxyEntry() {}
	public EZProxyEntry( String siteID, String pageID, String name, String value )
	{
		this.siteID = siteID;
		this.pageID = pageID;
		this.name   = name;
		this.value  = value;
	}
	
	// Getters
	public String getSiteID() { return siteID; }	
	public String getPageID() {	return pageID; }
	public String getName  () {	return name;   }	
	public String getValue () {	return value;  }
	
	// Setters
	public void setSiteID( String siteID ) { this.siteID = siteID; }
	public void setPageID( String pageID ) { this.pageID = pageID; }	
	public void setName  ( String name   ) { this.name = name;     }	
	public void setValue ( String value  ) { this.value = value;   }
	
	/** 
     * Returns a string representation of the EZProxy entry object.
     */
	@Override
	public String toString()
	{
		StringBuilder b = new StringBuilder();
		b.append( "{ siteID=" ).append( siteID ).append( ", pageID=" ).append( pageID ).append( ", name=" )
			.append( name ).append( ", value=" ).append( value ).append( " }" );
		return b.toString();
	}
}
