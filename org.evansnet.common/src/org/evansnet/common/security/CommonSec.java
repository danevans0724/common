package org.evansnet.common.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;

import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.evansnet.common.configuration.Global;



/**
 * This class establishes the common security structure needed.
 * @author Dan
 */
public final class CommonSec {
	
	private static final String SECURITY_PROPERTIES = "security.properties";
	private static final String KEYSTORE = "evansnet.keystore";
	private static final String THIS_CLASS_NAME = CommonSec.class.getName();
	public static Logger javaLogger = Logger.getLogger(THIS_CLASS_NAME);
	public String thePath;
	private final String pubCertFile = "evansnet.cer";		//The name of the public key cert file.
	static boolean instanceExists = false;
	private char[] firstPwd = {'B','B','v','1','0','l','e','t'};
	private char[] newCred;
	@SuppressWarnings("unused")
	private char[] safeCred;	//Write this to the security.properties file. 
	private int minLen = 8;		//Minimum password length
	private boolean capsReqd = true;
	private int minCaps = 1;	//Minimum uppercase characters in a password.
	private boolean specCharReqd = true;
	private int minSpecChar = 1;	//Minimum special non-digit, non-alphabetic characters in a password.
	private boolean digitReqd = true;	//Numeric digit required in password.
	private int minDigit = 1;		//Minimum numeric digits in a password. 
	Global globalConfig;
	char[] storeCrd;
	private Vault vault;
	private Certificate pub;
	static CommonSec theInstance = null;
	
	public static CommonSec getInstance() throws KeyStoreException {
		//Singleton.
		if (instanceExists == false) {
			theInstance = new CommonSec();
			instanceExists = true;
		} 
		return theInstance;
	}
	
	public CommonSec(String s) {
		//Do nothing constructor for helping Junits
	}
	
	private CommonSec() throws KeyStoreException {
		globalConfig = Global.getInstance();
		thePath = globalConfig.getConfigDir();
		vault = Vault.getInstance();
	}
	
	/**
	 * Used to replace the default password for the keystore with one defined by the user.
	 * @param p The password defined by the user. 
	 */
	public void createStorePwd(char[] p) {
		if (storeCrd == null) {
			storeCrd = getOrigCred();
		}			
		try {
				if (vault.authenticate(new FileInputStream(thePath + File.separator + KEYSTORE), storeCrd)) {
					storeCrd = p;
					vault.store(new FileOutputStream(thePath + File.separator + KEYSTORE), storeCrd);	//Set the new one.
					storeCred("storePassword", storeCrd);
				}
			} catch (Exception e) {
				javaLogger.logp(Level.SEVERE, THIS_CLASS_NAME, "createStorePwd()", 
						"Failed to set vault password. " + e.getMessage());
			}
	}
	
	/**
	 * Authenticates the user, and creator of the security store. 
	 * An initial creation of a security store credential is done on the first use of the
	 * application. After that, the user credential is the only thing accepted.
	 * @param p
	 * @return
	 */
	public boolean userAuthenticate(char[] p) throws Exception {
		try {
			if (vault == null) {
				vault = Vault.getInstance();
			}
			javaLogger.logp(Level.INFO, THIS_CLASS_NAME, "userAuthenticate()", "Got keystore instance.");
			if (vault.authenticate(new FileInputStream(thePath + File.separator + KEYSTORE), p)) {
				javaLogger.log(Level.INFO, "Successfully got keystore");
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			String msg = "Error accessing vault. " + e.getMessage();
			javaLogger.logp(Level.SEVERE, THIS_CLASS_NAME, "userAuthenticate()", 
					"Error accessing vault. " + e.getMessage());
			throw new InvalidCredentialsException(e, msg);
		}
	}
		
	/**
	 * Provide the default credential.
	 */
	private char[] getOrigCred() {
		return firstPwd;
	}
	
	/*
	 * Present a dialog box to the user for entering their safe store credentials.
	 * Set the credential as the keystore password. 
	 */
	public void establishVaultCredential() throws InvalidCredentialsException, Exception {
		char [] theCredential = openCredentialDialog();
		if (isCredOk(theCredential)) {
			createStorePwd(theCredential);
			newCred = theCredential;
		} else {
			throw new InvalidCredentialsException("Invalid credential entered.");
		}
	}
	
	/**
	 * Present a password dialog and log in the user. 
	 * @return
	 * @throws Exception 
	 */
	public char[] userLogin() throws Exception {
		Properties prop = fetchSecurityProperties();
		if (prop == null) {
			throw new Exception("UserLogin failed. Unable to open the security properties file.");
		}
		char[] userPassword = openCredentialDialog();
		if (isCredOk(userPassword)) {
			//Get the encrypted password and check it against the one entered.
			char[] storedUserPassword;
			char[] safeUserPwd = prop.getProperty("userPassword").toCharArray();
			storedUserPassword = unDisguise(safeUserPwd, fetchCert());
			if (storedUserPassword.equals(userPassword)) {
				return userPassword;
			}
		} else {
			javaLogger.log(Level.SEVERE, "Invalid credential");
			throw new InvalidCredentialsException("The credential you entered is not valid.");
		}
		return null;
	}
	
	public void createUserLogin() {
		
	}
	
	private Properties fetchSecurityProperties() {
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(globalConfig.getDataSecurityDir() + File.separator + SECURITY_PROPERTIES));
			return prop;
		} catch (IOException e) {
			javaLogger.log(Level.SEVERE, "Unable to load security properties file." + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 
	 * @return The store credential
	 * @throws Exception 
	 */
	public PrivateKey getVaultPasswordFromFile() throws Exception {
		Properties secProp = new Properties();
		secProp.load(new FileInputStream(globalConfig.getConfigDir() + File.separator + SECURITY_PROPERTIES));
		if (pub == null) {
			pub = fetchCert();
		}
		
		return null;
	}
	
	public char[] openCredentialDialog() {
		char[] theResults;
		IWorkbenchWindow activeWindow =  PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWindow != null) {
			VaultDialog credentialDialog = new VaultDialog(activeWindow.getShell(), SWT.NONE);
			theResults = (char[])credentialDialog.open();
			return theResults;
		} else {
			javaLogger.logp(Level.INFO, THIS_CLASS_NAME, "openCredentialDialog()",
					"Failed to create and open vault credential dialog.");
		}
		return null;
	}
	
	private boolean isCredOk(char[] c) {
		int caps = 0;
		int pwdLength = c.length;
		int special = 0;
		int digit = 0;
		int pos = 0;
		
		for ( pos = 0; pos < pwdLength; pos++ ) {
			Character ch = c[pos];
			if (Character.isWhitespace(ch)) {
				return false;  // Not whitespace allowed.
			} else if(digitReqd && Character.isDigit(ch)) {
				digit++;
			} else if(capsReqd && Character.isUpperCase(ch)) {
				caps++;
			} else if (specCharReqd && (
					   ch.equals('`') || 
					   ch.equals('!') ||
					   ch.equals('@') || 
					   ch.equals('#') ||
					   ch.equals('$') ||
					   ch.equals('%') ||
					   ch.equals('^') ||
					   ch.equals('&') ||
					   ch.equals('*') ||
					   ch.equals('(') ||
					   ch.equals(')') ||
					   ch.equals('+')))
				special++;
		}
		if (pwdLength >= minLen && 
		    caps >= minCaps &&
		    digit >= minDigit &&
		    special >= minSpecChar) {
				return true;  // Return true for now. Write the real routine after testing the other methods.
		} 
		return false;
	}
	
	protected void storeCred(String key, char[] c) throws Exception, IOException {
		//Take the user credential, encrypt it with the public key and 
		//write the encrypted credential to the security.properties file. 
		Properties secProp = new Properties();
		secProp.load(new FileInputStream(globalConfig.getConfigDir() + File.separator + SECURITY_PROPERTIES));
		if (pub == null) {
			pub = fetchCert();
		}
		safeCred = disguise(c,pub);
		secProp.setProperty(key, safeCred.toString());
		secProp.store(new FileOutputStream(globalConfig.getConfigDir() + File.separator + SECURITY_PROPERTIES), null);
		if (key.equals("storePassword")) { 
			storeCrd = c;
		}
	}
	
	// Encrypt with the public key. 
	public final char[] disguise(char[] pwd, Certificate c) throws Exception {
		byte[] bPwd = toBytes(pwd);
		PublicKey key = fetchKey(c);
		if (isKeyEqual(key)) {
			//Keys match so safe to encrypt here.
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] encrypted = cipher.doFinal(bPwd);
			char[] safePwd = new char[encrypted.length];
			safePwd = toChar(encrypted);
			return safePwd;
		}
		pwd = null;		//Destroy the clear text password.
		return pwd;
	}
	
	private final byte[] toBytes(char[] c) {
		byte[] bPwd = new byte[c.length];
		for(int i = 0; i < c.length; i++) {
			bPwd[i] = (byte) c[i];
		}
		return bPwd;
	}
	
	private final char[] toChar(byte[] b) {
		char[] theChars = new char[b.length];
		for (int i = 0; i < b.length; i++) {
			theChars[i] = (char)b[i];
		}
		return theChars;
	}
	
	private final PrivateKey fetchPrivate(KeyStore keystore) {
		try {
			KeyStore.ProtectionParameter prot = new KeyStore.PasswordProtection("BBv10let".toCharArray());
			KeyStore.PrivateKeyEntry privEntry = (KeyStore.PrivateKeyEntry)keystore.getEntry("credentials", prot);
			return privEntry.getPrivateKey();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public final char[] unDisguise(char[] p, Certificate c) throws Exception {
		byte[] bPwd = toBytes(p);
		PublicKey key = fetchKey(c);
		KeyStore keystore = getKeystore();
		if (isKeyEqual(key)) {
			//Keys match so safe to encrypt here.
			PrivateKey privKey = fetchPrivate(keystore);		//Fetch the private key from the keystore
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, privKey);
			byte[] decrypted = cipher.doFinal(bPwd);
			char[] thePwd = new char[decrypted.length];
			thePwd = toChar(decrypted);
			return thePwd;
		}
		return null;
	}
	
	private final KeyStore getKeystore() throws Exception {
		return vault.getKeystore(new FileInputStream(globalConfig.getConfigDir() + File.separator + KEYSTORE), fetchStorePw());
	}
	
	//No javadoc because we don't want to expose the method that gets the key from the keystore
	private final Certificate fetchCert() {
		String certFile = globalConfig.getConfigDir() + File.separator + pubCertFile;
		try {
			FileInputStream fis = new FileInputStream(certFile);
			CertificateFactory factory = CertificateFactory.getInstance("X.509");
			Certificate pubCert = factory.generateCertificate(fis);
			certFile = null;
			return pubCert;
		} catch (Exception e) {
			javaLogger.log(Level.SEVERE, "Failed to read certificate from file. Error; " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	// Test the public key passed for equality with the dataconnector's key.
	// If they are the same, we can trust the sender as another piece of our code.
	private final boolean isKeyEqual(Key keyTest ) throws Exception {
		PublicKey trustedKey = pub.getPublicKey();
		if (keyTest.equals(trustedKey) && Arrays.equals(keyTest.getEncoded(), trustedKey.getEncoded())) {
			return true;
		} 
		return false;
	}
	
	private final PublicKey fetchKey(Certificate cert) {
		if (pub == null) {
			pub = fetchCert();
		}
		return pub.getPublicKey();
	}
	
	//Get the keystore password.
	private final char[] fetchStorePw() throws IOException, KeyStoreException {
		return firstPwd;
	}

}
