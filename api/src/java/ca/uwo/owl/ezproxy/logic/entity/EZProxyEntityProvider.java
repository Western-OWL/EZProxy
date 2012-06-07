package ca.uwo.owl.ezproxy.logic.entity;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;

/**
 * Beans may implement this interface directly to provide "extension" capabilities to an existing
 * {@link CoreEntityProvider}. If you are the provider for a set of entities then you will want to
 * implement {@link CoreEntityProvider}, this interface is primarily for extending an existing
 * entity provider (adding extra functionality to one that is already registered
 * 
 * Usage:<br/> 1) Implement this interface<br/> 2) Implement any additional capabilities
 * interfaces (optional, but it would be crazy not to do at least one)<br/> 3) Create a spring bean
 * definition in the Sakai application context (components.xml)<br/> 4) Implement
 * {@link AutoRegisterEntityProvider} or register this implementation some other way
 * 
 * @author Brian Jones (bjones86@uwo.ca)
 */
public interface EZProxyEntityProvider extends EntityProvider
{
	// Publicly available static string defining the entity prefix for EZProxy entities
	public final static String ENTITY_PREFIX = "ezproxy";
	
	// Publicly available static string array defining the handled output formats
	public final static String[] HANDLED_OUTPUT_FORMATS = new String[] { Formats.HTML };
}
