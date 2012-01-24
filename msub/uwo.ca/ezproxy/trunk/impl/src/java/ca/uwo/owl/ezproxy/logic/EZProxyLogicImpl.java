package ca.uwo.owl.ezproxy.logic;

import java.util.List;

import lombok.Setter;

import org.apache.log4j.Logger;

import ca.uwo.owl.ezproxy.dao.EZProxyDao;
import ca.uwo.owl.ezproxy.logic.EZProxyLogic;
import ca.uwo.owl.ezproxy.model.EZProxyEntry;

/**
 * Implementation of {@link EZProxyLogic}.
 * This is where you would implement any caching.
 * 
 * Caching was avoided for this project because of problems with database entries being out of sync with cached entries.
 * 
 * @author Brian Jones (bjones86@uwo.ca)
 *
 */
public class EZProxyLogicImpl implements EZProxyLogic
{
	// Class members
	private static final Logger log = Logger.getLogger( EZProxyLogicImpl.class );
	
	// Instance members
	@Setter
	private EZProxyDao dao;
	
	/**
	 * init - perform any actions required here for when this bean starts up
	 */
	public void init()
	{
		log.info("init");
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<EZProxyEntry> getEZProxyEntry( String siteID, String pageID )
	{
		return dao.getEZProxyEntry( siteID, pageID );
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<EZProxyEntry> getAllEZProxyEntries()
	{
		return dao.getAllEZProxyEntries();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean addEZProxyEntry( EZProxyEntry e )
	{
		return dao.addEZProxyEntry( e );
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getToolTitle( String siteID, String pageID )
	{
		return dao.getToolTitle( siteID, pageID );
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getPageTitle( String siteID, String pageID )
	{
		return dao.getPageTitle( siteID, pageID );
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean updateToolTitle( String siteID, String pageID, String newToolTitle )
	{
		return dao.updateToolTitle( siteID, pageID, newToolTitle );
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean updatePageTitle( String siteID, String pageID, String newPageTitle )
	{
		return dao.updatePageTitle( siteID, pageID, newPageTitle );
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean updateEZProxyEntry( String newValue, String siteID, String pageID, String name )
	{		
		return dao.updateEZProxyEntry( newValue, siteID, pageID, name );
	}
}