package ca.uwo.owl.ezproxy.dao.impl;

import java.net.URL;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.InvariantReloadingStrategy;
import org.apache.log4j.Logger;

import org.sakaiproject.component.cover.ServerConfigurationService;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import ca.uwo.owl.ezproxy.dao.EZProxyDao;
import ca.uwo.owl.ezproxy.model.EZProxyEntry;


/**
 * Implementation of EZProxyDao
 * 
 * @author Brian Jones (bjones86@uwo.ca)
 *
 */
public class EZProxyDaoImpl extends JdbcDaoSupport implements EZProxyDao
{
	// Class members
	private static final Logger log = Logger.getLogger(EZProxyDaoImpl.class);	// The logger
	
	// Instance members
	private PropertiesConfiguration statements;	// Contains the prepared database statements
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<EZProxyEntry> getEZProxyEntry( String siteID, String pageID )
	{
		// Debug log
		if( log.isDebugEnabled() )
			log.debug( "getEZProxyEntry( " + siteID + ", " + pageID + " )" );
		
		try
		{
			// Run the query, providing arguments and a RowMapper
			return getJdbcTemplate().query( getStatement( "select.entry" ), 
					new Object[]{ siteID, pageID }, 
					new EZProxyEntryMapper() );
		}
		catch( DataAccessException ex )
		{
			// Error log
			log.error( "Error executing query: " + ex.getClass() + ":" + ex.getMessage() );
			StringBuilder errorMsg = new StringBuilder();
			errorMsg.append( "ERROR EZProxyDaoImpl.getEZProxyEntry( siteID=" ).append( siteID ).append(", pageID=" )
				.append( pageID ).append( " )" );
			log.error( errorMsg.toString() );
			return null;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public List<EZProxyEntry> getAllEZProxyEntries()
	{
		// Debug log
		if( log.isDebugEnabled() )
			log.debug( "getAllEZProxyEntries()" );
		
		try
		{
			// Run the query, providing a RowMapper
			return getJdbcTemplate().query( getStatement( "select.entries" ), new EZProxyEntryMapper() );
		} 
		catch( DataAccessException ex )
		{
			// Error log
			log.error( "Error executing query: " + ex.getClass() + ":" + ex.getMessage() );
			log.error( "ERROR EZProxyDaoImpl.getAllEZProxyEntries()" );
			return null;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean addEZProxyEntry( EZProxyEntry e )
	{
		// Debug log
		if( log.isDebugEnabled() )
			log.debug( "addEZProxyEntry( " + e.toString() + ")" );
		
		try
		{
			// Run the query, providing arguments
			getJdbcTemplate().update( getStatement( "insert.entry" ), 
					new Object[]{ e.getSiteID(), e.getPageID(), e.getName(), e.getValue() } );
			return true;
		}
		catch( DataAccessException ex )
		{
			// Error log
           log.error( "Error executing query: " + ex.getClass() + ":" + ex.getMessage() );
           StringBuilder errorMsg = new StringBuilder();
			errorMsg.append( "ERROR EZProxyDaoImpl.addEZProxyEntry( siteID=" ).append( e.getSiteID() ).append(", pageID=" )
				.append( e.getPageID() ).append( ", name=" ).append( e.getName() ).append( ", value=" ).append( e.getValue() )
				.append( ")" );
			log.error( errorMsg.toString() );
           return false;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getToolTitle( String siteID, String pageID )
	{
		// Debug log
		if( log.isDebugEnabled() )
			log.debug( "getToolTitle( " + siteID + ", " + pageID + " )" );
		
		try
		{
			// Run the query providing arguments
			SqlRowSet rs = getJdbcTemplate().queryForRowSet( getStatement( "select.toolTitle" ),
					new Object[]{ pageID, siteID } );
			
			// Get the result from the RowSet
			String retVal = null;
			if( rs != null )
				while( rs.next() )
					retVal = rs.getString( "TITLE" );
			
			// Return the result
			return retVal;
		}
		catch( DataAccessException ex )
		{
			// Error log
			log.error( "Error executing query: " + ex.getClass() + ":" + ex.getMessage() );
			StringBuilder errorMsg = new StringBuilder();
			errorMsg.append( "ERROR EZProxyDaoImpl.getToolTitle( siteID=" ).append( siteID ).append(", pageID=" )
				.append( pageID ).append( " )" );
			log.error( errorMsg.toString() );
	        return null;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getPageTitle( String siteID, String pageID )
	{
		// Debug log
		if( log.isDebugEnabled() )
			log.debug( "getPageTitle( " + siteID + ", " + pageID + " )" );
		
		try
		{
			// Run the query providing arguments
			SqlRowSet rs = getJdbcTemplate().queryForRowSet( getStatement( "select.pageTitle" ),
					new Object[]{ pageID, siteID } );
			
			// Get the result from the RowSet
			String retVal = null;
			if( rs != null )
				while( rs.next() )
					retVal = rs.getString( "TITLE" );
			
			// Return the result
			return retVal;
		}
		catch( DataAccessException ex )
		{
			// Error log
			log.error( "Error executing query: " + ex.getClass() + ":" + ex.getMessage() );
			StringBuilder errorMsg = new StringBuilder();
			errorMsg.append( "ERROR EZProxyDaoImpl.getPageTitle( siteID=" ).append( siteID ).append(", pageID=" )
				.append( pageID ).append( " )" );
			log.error( errorMsg.toString() );
	        return null;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean updateToolTitle( String siteID, String pageID, String newToolTitle )
	{
		// Debug log
		if( log.isDebugEnabled() )
			log.debug( "updateToolTitle( " + siteID + ", " + pageID + ", " + newToolTitle + " )" );
		
		try
		{
			// Run the query, providing arguments
			getJdbcTemplate().update( getStatement( "update.toolTitle" ), 
					new Object[]{ newToolTitle, pageID, siteID } );
			return true;
		}
		catch( DataAccessException ex )
		{
			// Error log
			log.error( "Error executing query: " + ex.getClass() + ":" + ex.getMessage() );
			StringBuilder errorMsg = new StringBuilder();
			errorMsg.append( "ERROR EZProxyDaoImpl.updateToolTitle( siteID=" ).append( siteID ).append(", pageID=" )
				.append( pageID ).append( ", newToolTitle=" ).append( newToolTitle ).append( " )" );
			log.error( errorMsg.toString() );
	        return false;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean updatePageTitle( String siteID, String pageID, String newPageTitle )
	{
		// Debug log
		if( log.isDebugEnabled() )
			log.debug( "updatePageTitle( " + siteID + ", " + pageID + ", " + newPageTitle + " )" );
		
		try
		{
			// Run the query providing arguments
			getJdbcTemplate().update( getStatement( "update.pageTitle" ), 
					new Object[]{ newPageTitle, pageID, siteID } );
			return true;
		}
		catch( DataAccessException ex )
		{
			// Error log
			log.error( "Error executing query: " + ex.getClass() + ":" + ex.getMessage() );
			StringBuilder errorMsg = new StringBuilder();
			errorMsg.append( "ERROR EZProxyDaoImpl.updatePageTitle( siteID=" ).append( siteID ).append(", pageID=" )
				.append( pageID ).append( ", newPageTitle=" ).append( newPageTitle ).append( " )" );
			log.error( errorMsg.toString() );
	        return false;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean updateEZProxyEntry( String newValue, String siteID, String pageID, String name )
	{
		// Debug log
		if( log.isDebugEnabled() )
			log.debug( "updateEZProxyEntry( " + newValue + ", " + siteID + ", " + pageID + ", " + name + " )" );
		
		try
		{
			// Run the query providing arguments
			getJdbcTemplate().update( getStatement( "update.entry" ),
					new Object[]{ newValue, siteID, pageID, name } );
			return true;
		}
		catch( DataAccessException ex )
		{
			// Error log
			log.error( "Error executing query: " + ex.getClass() + ":" + ex.getMessage() );
			StringBuilder errorMsg = new StringBuilder();
			errorMsg.append( "ERROR EZProxyDaoImpl.updateEZProxyEntry( siteID=" ).append( siteID ).append(", pageID=" )
				.append( pageID ).append( ", newValue=" ).append( newValue ).append( ", name=" ).append( name ).append( " )" );
			log.error( errorMsg.toString() );
	        return false;
		}
	}
	
	/**
	 * Initialization method (bean instantiation)
	 */
	public void init()
	{
		log.info( "init()" );
		
		// Setup the vendor
		String vendor = ServerConfigurationService.getInstance().getString( "vendor@org.sakaiproject.db.api.SqlService", null );
		
		// Initialise the statements
		initStatements( vendor );
		
		// Setup tables if we have auto.ddl enabled.
		boolean autoddl = ServerConfigurationService.getInstance().getBoolean( "auto.ddl", true );
		if( autoddl )
			initTables();
	}
	
	/**
	 * Sets up the table for EZProxy
	 */
	private void initTables()
	{
		try { getJdbcTemplate().execute( getStatement( "create.table" ) ); }
		catch( DataAccessException ex ) { log.info( "Error creating tables: " + ex.getClass() + ":" + ex.getMessage() ); }
	}
	
	/**
	 * Loads our SQL statements from the appropriate properties file
	 
	 * @param vendor	DB vendor string. Must be one of mysql, oracle, hsqldb
	 */
	private void initStatements( String vendor )
	{
		// Get the appropriate properties file (MySQL, Oracle or HSQL)
		URL url = getClass().getClassLoader().getResource( vendor + ".properties" ); 
		
		try
		{
			// Must use blank constructor so it doesn't parse just yet (as it will split)
			statements = new PropertiesConfiguration();
			
			// Don't watch for reloads
			statements.setReloadingStrategy( new InvariantReloadingStrategy() );
			
			// Throw exception if no prop
			statements.setThrowExceptionOnMissing( true );
			
			// Don't split properties
			statements.setDelimiterParsingDisabled( true );
			
			// Now load our file
			statements.load( url );
		}
		catch( ConfigurationException e ) { log.error( e.getClass() + ": " + e.getMessage() ); }
	}
	
	/**
	 * Get an SQL statement for the appropriate vendor from the bundle
	
	 * @param key
	 * @return statement or null if none found. 
	 */
	private String getStatement( String key )
	{
		try { return statements.getString( key ); }
		catch( NoSuchElementException e )
		{
			log.error( "Statement: '" + key + "' could not be found in: " + statements.getFileName() );
			return null;
		}
	}
}
