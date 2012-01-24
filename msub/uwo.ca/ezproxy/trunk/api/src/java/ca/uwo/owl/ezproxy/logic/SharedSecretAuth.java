package ca.uwo.owl.ezproxy.logic;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Provides utilities for handling shared secret authentication using Message Authentication Code (MAC) with MD5 hash.
 * 
 * @author Brian Jones (bjones86@uwo.ca)
 * @author Paul Lukasewych (plukasew@uwo.ca)
 *
 */
public class SharedSecretAuth
{
    /**
     * Generates a message authentication code (MAC) from a string of data and a key. Uses an MD5 hash.
     * @param data the data that will be hashed to get the MAC
     * @param secretKey the shared secret
     * @return the message authentication code
     * @throws NoSuchAlgorithmException
     * @throws IndexOutOfBoundsException
     */
    public static String generateMAC( String data, String secretKey ) throws NoSuchAlgorithmException, IndexOutOfBoundsException
    {
		byte[] bytes = data.getBytes();
	
		// Sum the bytes in the data
		long sum = 0;
		for( int i = 0; i < bytes.length; ++i )
		    sum = bytes[i];
	
		// Combine secret key with the sum to get our message
		String message = Long.toString( sum ) + secretKey;
		byte[] messageBytes = message.getBytes();
	
		// Generate the message digest (MD5 hash)
		MessageDigest digest = MessageDigest.getInstance( "MD5" ); // This will throw an exception if the MD5 algorithm is not supported
		digest.update( messageBytes, 0, messageBytes.length );
		byte[] digestBytes = digest.digest();
	
		// Convert digest bytes to hex string
		StringBuilder builder = new StringBuilder();
		for( int j = 0; j < digestBytes.length; ++j )
		{
		    int digestChar = digestBytes[j];
		    if( digestChar < 0 )
		    	digestChar += 256;
		
		    String hexChar = Integer.toHexString( digestChar );  
		    if( hexChar.length() == 1 ) // Need to add leading zero
		    	hexChar = "0" + hexChar;
	
		    builder.append( hexChar ); // This could throw IndexOutOfBoundsException
		}
	
		return builder.toString();

    } // End generateMac()
    
} // End class