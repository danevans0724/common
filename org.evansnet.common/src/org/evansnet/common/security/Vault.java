package org.evansnet.common.security;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper for a KeyStore object that allows for encapsulation and testing. 
 * @author Dan Evans
 *
 */
public final class Vault {
	
	public static Logger javaLogger = Logger.getLogger(Vault.class.getName());
	private static Vault vault;
	private KeyStore keystore;
	private final char[] firstPwd = {'B','B','v','1','0','l','e','t'};
	private static boolean instance = false;
	private static boolean isOpen = false;
	
	private Vault() throws KeyStoreException {
		keystore = KeyStore.getInstance("JKS");
	}

	public static Vault getInstance() throws KeyStoreException {
		if (instance == false) {
			vault = new Vault();
			instance = true;
		}
		return vault;
	}
	
	public boolean authenticate(InputStream is, char[] p) {
		try {
			keystore.load(is, p);
			setOpen(true);
			return true;
		} catch (NoSuchAlgorithmException | CertificateException | IOException e) {
			javaLogger.logp(Level.SEVERE, Vault.class.getName(), "authenticalte()",
					"Authentication failed. ");
			lock();		//If authentication failed, lock the vault.
			return false;
		}
	}
	
	private void setOpen(boolean s) {
		isOpen = s;
	}
	
	public boolean isOpen() { 
		return isOpen;
	}
	
	public boolean store(OutputStream os, char[] p) throws InvalidCredentialsException, Exception {
		try {
			if (keystore != null) {
				keystore.store(os, p);
				return true;
			} else {
				javaLogger.logp(Level.SEVERE, Vault.class.getName(), "store()", 
						"Keystore is not available. ");
				return false;
			}
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			javaLogger.logp(Level.SEVERE, Vault.class.getName(), "store()",
					"Vault.store() failed. ");
			throw new InvalidCredentialsException(e.getMessage());
		}
	}
	
	public KeyStore getKeystore(FileInputStream f, char[] p) throws Exception {
		if (authenticate(f,p)) {
			return keystore;
		}
		return null;
	}
	
	/**
	 * Closes the vault and sets the object = null.
	 */
	public void lock() {
		keystore = null;
		setOpen(false);
		vault = null;
		instance = false;
	}
}
