package ca.uwo.owl.ezproxy.dao.impl;


import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import ca.uwo.owl.ezproxy.model.EZProxyEntry;

/**
 * RowMapper to handle EZProxyEntry(s). This RowMapper is designed to handle one row at a time.
 * It should NEVER call ResultSet.next() or equivalent. When used properly, mapRow()
 * will be called for EACH row sequentially by the JdbcTemplate.
 *
 * @author Brian Jones (bjones86@uwo.ca)
 *
 */
public class EZProxyEntryMapper implements RowMapper
{	
	/* (non-Javadoc)
	 * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
	 */
	@Override
	public EZProxyEntry mapRow( ResultSet rs, int rowNum ) throws SQLException
	{
		// Dump the data into an EZProxyEntry object and return it
		EZProxyEntry e = new EZProxyEntry();
		e.setSiteID( rs.getString( "SITE_ID" ) );
		e.setPageID( rs.getString( "PAGE_ID" ) );
		e.setName( 	 rs.getString( "NAME" ) );
		e.setValue(  rs.getString( "VALUE" ) );
		return e;
	}
}
