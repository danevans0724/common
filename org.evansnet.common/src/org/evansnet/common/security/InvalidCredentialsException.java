package org.evansnet.common.security;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This exception is thrown if the credential entered by the user does not meet the 
 * requirements of a valid credential. The user must adhere to the length and format 
 * requirements for a credential.
 * @author Dan Evans
 *
 */
public class InvalidCredentialsException extends Exception {

	private static final long serialVersionUID = 1L;	//Stupid version thing...
	private static final String THIS_CLASS_NAME = InvalidCredentialsException.class.getName();
	public static Logger javaLogger = Logger.getLogger(THIS_CLASS_NAME);

	public InvalidCredentialsException(String string) throws Exception {
		javaLogger.logp(Level.SEVERE, THIS_CLASS_NAME, null, string);
		throw new Exception(string);
	}
	
	public InvalidCredentialsException(Throwable e, String s) throws Exception {
		javaLogger.logp(Level.SEVERE, THIS_CLASS_NAME, null, s + e.getMessage());
		throw new Exception(e);
	}

}
