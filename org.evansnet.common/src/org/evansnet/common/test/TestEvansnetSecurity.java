package org.evansnet.common.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.KeyStore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.evansnet.common.configuration.Global;
import org.evansnet.common.security.CommonSec;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestEvansnetSecurity {
	
	private Logger javaLogger = Logger.getLogger(TestEvansnetSecurity.class.getName());
	
	private Global global;
	private CommonSec sec;
	private FileOutputStream os;
	private char[] firstPwd = {'B','B','v','1','0','l','e','t'};
	
	@Before
	public void setUp() throws Exception {
		global = Global.getInstance();
		String currentDir = System.getProperties().getProperty("user.dir");
		global.setWorkingDir(currentDir + File.separator + "evansnet");
		FileInputStream is = new FileInputStream(global.getConfigDir() + File.separator + "evansnet.keystore");
		os = new FileOutputStream(global.getConfigDir() + File.separator + "evansnet.keystore");
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(null, null);		// Creates a new keystore for test. 
		keyStore.store(os, firstPwd);
		sec = CommonSec.getInstance();
	}

	@After
	public void tearDown() throws Exception {
		sec = null;
		global = null;
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
			char[] newPwd = {'T','e','s','t','$','i','n','g'};
			sec.createStorePwd(newPwd);
			
			// Now authenticate with the new password.
			isAuthenticated = false; 	
			isAuthenticated = sec.userAuthenticate(newPwd);
			assertTrue(isAuthenticated);
			
			// Now reset to the original password.
			sec.createStorePwd(firstPwd);
			isAuthenticated = sec.userAuthenticate(firstPwd);
			assertTrue(isAuthenticated);
			return;
		} catch (Exception e) {
			System.out.println("Failed password set test!" + "\n" + e.getMessage());
			e.printStackTrace();
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
			e.printStackTrace();
		}
		fail("Failed to reject bad password.");
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
	
 }
