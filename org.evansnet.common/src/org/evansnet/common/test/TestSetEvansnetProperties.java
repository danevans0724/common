package org.evansnet.common.test;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.evansnet.common.configuration.Global;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestSetEvansnetProperties {
	
	String defaultFile;
	String defaultPath;
	String theDefault;
	String fileName;
	String userPath;

	@Before
	public void setUp() throws Exception {
		Properties sysProp = System.getProperties();
		fileName = "TestFile.txt";
		userPath = sysProp.getProperty("user.home");
		defaultFile = "configuration.properties";
		defaultPath = userPath + File.separator + "evansnet\\cfg";
		theDefault = defaultPath + File.separator + defaultFile;		
		
		// Create the test file in the user's directory.
		try {
			if (Files.notExists(Paths.get(userPath, fileName), LinkOption.NOFOLLOW_LINKS)) {
				Files.createFile(Paths.get(userPath, fileName));
			}
			//Delete any existing config folder & file for tests.
			Path defaultTestPath = Paths.get(theDefault);
			if (Files.exists(defaultTestPath)) {
				Files.delete(defaultTestPath);
			}
		} catch (Exception e) {
			fail("Could not set up test file in user's directory. " + e.getMessage());
			e.printStackTrace();
		}
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testGlobal() {
		//The config path and file do not exist for the start of this test.
		//In the test process, we will create the path, file and default configuration

		try {
			// Test creation of the singleton.
			Global global = Global.getInstance();
			assertTrue(global != null);
			//Test null path. The return should be the default path with empty properties file.
			global.fetchProperties(null);
			assertTrue(global.getEvansnetProp().isEmpty());
			// Test fetch with empty path. The return will still be an empty properties file.
			global.fetchProperties("");
			assertTrue(global.getEvansnetProp().isEmpty());

			// Test save of properties. The path and file now exist.
			
			//Test save providing a path that does not include the configuration.properties file name.
			global.saveConfig(theDefault);
			assertTrue(Files.exists(Paths.get(theDefault)));
			assertTrue((global.getEvansnetProp()).size() == 4);

			// Test fetch with file present. Clear the existing properties object first.
			Properties theProp = new Properties();
			global.setEvansnetProp(theProp);
			
			//Now read the properties from disk.
			global.fetchProperties(theDefault);
			assertTrue(global.getEvansnetProp().size() == 4);	
			} catch (Exception e) {
				fail("Exception was thrown during testGlobal(). Message is " + e.getMessage());
		}
	}

}
