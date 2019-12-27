package org.evansnet.common.test;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.KeyStore;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.Mock;
import org.evansnet.common.configuration.Global;
import org.evansnet.common.security.CommonSec;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestCommonSec {
	
	private Logger javaLogger = Logger.getLogger(TestCommonSec.class.getName());
	
//	private Global global;
	private CommonSec sec;
	private FileOutputStream os;
	private char[] firstPwd = {'B','B','v','1','0','l','e','t'};
	char[] newPwd = {'T','e','s','t','$','i','n','g'};
	private Certificate cert;
	private byte[] disguised;
	
	//Mocks
	@Mock private Global globalMock = createNiceMock(Global.class);
	String currentDir = "C:\\Users\\pmidce0\\git\\common\\org.evansnet.common";
	String workingDir = currentDir + File.separator + "evansnet";
	String testDir = workingDir + File.separator + "TestObjects";
			
	@Before
	public void setUp() throws Exception {
		//Mock expects
		expect(globalMock.getWorkingDir()).andReturn(workingDir);
		expect(globalMock.getTestDir()).andReturn(testDir).anyTimes();
		expect(globalMock.getConfigDir()).andReturn(testDir).anyTimes();	//Redirect to the test objects
		replay(globalMock);

		os = new FileOutputStream(globalMock.getTestDir() + File.separator + "credstore.keystore");
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(null, null);		// Creates a new keystore for test. 
		keyStore.store(os, firstPwd);
		sec = CommonSec.getInstance();
		
		// Now that the CommSec instance is available, set the actual working directory using reflection
		// Use reflexive call to the singleton instance of Global.class to redirect the test to the test objects folder. 
		Global g = Global.getInstance(); 
		Method method;
		try {
			method = Global.class.getDeclaredMethod("setConfigFolder", String.class);
			method.setAccessible(true);
			method.invoke(g, testDir);
		} catch (NoSuchMethodException e1) {
			fail("testGetVaultPasswordFromFile() failed! No such method exception thrown. " + e1.getMessage());
			printStackTrace(e1.getStackTrace());
		} catch (SecurityException e1) {
			fail("testGetVaultPasswordFromFile() failed! Security exception thrown. " + e1.getMessage());
			printStackTrace(e1.getStackTrace());
		} catch (IllegalAccessException e) {
			fail("testGetVaultPasswordFromFile() failed! Illegal Access exception thrown. " + e.getMessage());
			printStackTrace(e.getStackTrace());
		} catch (IllegalArgumentException e) {
			fail("testGetVaultPasswordFromFile() failed! Illegal argument exception thrown. " + e.getMessage());
			printStackTrace(e.getStackTrace());
		} catch (InvocationTargetException e) {
			fail("testGetVaultPasswordFromFile() failed! Ivocation target exception thrown. " + e.getMessage());
			printStackTrace(e.getStackTrace());
		}
		
		// Establish the test certificate file
		String certFile = globalMock.getTestDir() + File.separator + "credentials.cer";
		try (FileInputStream fis = new FileInputStream(certFile)) {
			CertificateFactory factory = CertificateFactory.getInstance("X.509");
			cert = factory.generateCertificate(fis);
		} catch (Exception e) {
			printStackTrace(e.getStackTrace());
		}
		
	}

	@After
	public void tearDown() throws Exception {
		sec = null;
		globalMock = null;
		firstPwd = null;
		os = null;
	}


	@Test
	public void testCreateStorePwd() {
		try {
			boolean isAuthenticated = false;
			Method method = CommonSec.class.getDeclaredMethod("vaultUnlock");
			method.setAccessible(true);
			method.invoke(sec); // Make sure the vault is open otherwise the test will fail.
			
			// First authenticate with the original password. Confirm the vault is good.
			isAuthenticated = sec.userAuthenticate(firstPwd);
			assertTrue(isAuthenticated);
			sec.createStorePwd(newPwd);
			
			// Now authenticate with the new password.
			isAuthenticated = sec.userAuthenticate(newPwd);
			assertTrue(isAuthenticated);
			
			// Now reset to the original password.
			sec.createStorePwd(firstPwd);
			isAuthenticated = sec.userAuthenticate(firstPwd);
			assertTrue(isAuthenticated);
			return;
		} catch (Exception e) {
			javaLogger.log(Level.SEVERE,"Failed password set test!" + "\n" + e.getMessage());
			printStackTrace(e.getStackTrace());
			fail("Exception thrown during password set test.");
		}
		fail("Failed to set password.");
	}

	@Test
	public void testBadPasswordReject() {
		char[] badPwd = {'B','a','d','t','e','s','t'};
		boolean isAuthenticated;
		try {
			isAuthenticated = sec.userAuthenticate(badPwd);
			assertFalse(isAuthenticated);
			return;
		} catch (Exception e) {
			javaLogger.log(Level.SEVERE, "Threw an exception" + e.getMessage());
		}
		fail("Failed to reject bad password.");
	} 
	
	
	public void testDisguise() {
		try {
			char[] encrypted = sec.disguise(newPwd, cert);
			disguised = toBytes(encrypted);
		} catch (Exception e) {
			fail("Exception thrown during encryption attempt " + e.getMessage());
		}
	}
	
	@Test
	public void testUndisguise() {
		Method method;
		try {
			method = CommonSec.class.getDeclaredMethod("vaultUnlock");
			method.setAccessible(true);
			method.invoke(sec); // Make sure the vault is open otherwise the test will fail.
		} catch (NoSuchMethodException | SecurityException e1) {
			printStackTrace(e1.getStackTrace());
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			printStackTrace(e.getStackTrace());
		}
		testDisguise();
		assertTrue((toBytes(newPwd)).length != disguised.length); 
		char[] encrypted = toChar(disguised);
		try {
			char[] unDisguised = sec.unDisguise(encrypted, cert);
			assertArrayEquals(newPwd, unDisguised);
		} catch (Exception e) {
			printStackTrace(e.getStackTrace());
		}
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
	

	@Test
	public void testFetchCert() {
		Method method = null;
		try {
			method = CommonSec.class.getDeclaredMethod("fetchCert");
			method.setAccessible(true);
			Certificate fetchedCert = (Certificate)method.invoke(sec);
			String certType = fetchedCert.getType();
			assertTrue(certType.equals("X.509"));
			byte[] fetchedEncoded = fetchedCert.getEncoded();
			byte[] testEncoded = cert.getEncoded();
			assertArrayEquals(testEncoded, fetchedEncoded);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | CertificateEncodingException e) {
			javaLogger.log(Level.SEVERE, "fetched certificate does not equal test! ");
			printStackTrace(e.getStackTrace());
		}
	}
	
	@Test
	public void testIsCredOk() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method method = CommonSec.class.getDeclaredMethod("isCredOk", char[].class);
		method.setAccessible(true);
		char [] testPwd = {'T','e','$','t','3','w','d'}; 		//Less than min char
		boolean result = (boolean) method.invoke(sec, testPwd);
		assertFalse(result);
		char [] testNoCapsPwd = {'t','e','s','t','3','n','g','$'};	//No caps.
		result = (boolean) method.invoke(sec, testNoCapsPwd);
		assertFalse(result);
		char [] testNoDigitPwd = {'T','e','$','t','i','n','g','s'};	//No numeric digits
		result = (boolean) method.invoke(sec,testNoDigitPwd);
		assertFalse(result);
		char[] testNoSpecPwd = {'T','e','s','t','3','n','g','s'};	//No special chars.
		result = (boolean) method.invoke(sec, testNoSpecPwd);
		assertFalse(result);
		char[] testGoodPwd = {'T','e','$','t','1','n','g','s'};
		result = (boolean) method.invoke(sec, testGoodPwd);
		assertTrue(result);
	}
	
	@Test
	public void testGetVaultPasswordFromFile() {
		try {
			char[] retrievedPwd = sec.getVaultPasswordFromFile();
			char[] thePwd = sec.unDisguise(retrievedPwd, cert);
			assertArrayEquals(thePwd, newPwd);
		} catch (Exception e) {
			javaLogger.log(Level.SEVERE, "testGetVaultPasswordFrom File failed! " + e.getMessage());
			printStackTrace(e.getStackTrace());
		}
	}
	
	@Test
	public void testStoreCred() {	
		try {
			Method method = CommonSec.class.getDeclaredMethod("storeCred", char[].class);
			method.setAccessible(true);
			method.invoke(sec, newPwd);

//			Use the next lines to set the shippable initial password. 
//			Once done, copy the security.properties file to the cfg folder
//			method.invoke(sec, new char[] {'B','B','v','1','0','l','e','t'});

		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException e) {
			fail("testStorCred(): Test failed! Exception thrown: " + e.getMessage());
			printStackTrace(e.getStackTrace());
		} catch (InvocationTargetException ite) {
			fail("testStorCred(): Test failed! Invocation Target Exception thrown: " + ite.getMessage());
			printStackTrace(ite.getStackTrace());
		}
	}
	
	private void printStackTrace(StackTraceElement[] stackTrace) {
		int member = 0;
		StringBuilder stack = new StringBuilder();
		while(member < stackTrace.length) {
			stack.append(stackTrace[member] + "\n");
			member++;
		}
		javaLogger.log(Level.SEVERE, stack.toString());	
	}

 }
