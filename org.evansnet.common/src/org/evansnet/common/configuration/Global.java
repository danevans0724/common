package org.evansnet.common.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides global configuration parameters for evansnet products.
 * The configuration parameters are stored in the user's home directory.
 * 
 * @author pmidce0
 *
 */
public class Global {
	public static final String THIS_CLASS_NAME = Global.class.getName();
	public static final Logger javaLogger = Logger.getLogger(THIS_CLASS_NAME);
	
	private static final String CONFIG_FILE = "configuration.properties";
	private static Global GlobalInstance = null;

	private static final String EVANSNET = "evansnet";
	private static final String CFG_FOLDER = "cfg";
	private static final String DATA_FOLDER = "data";
	private static final String TEST_FOLDER = "TestObjects";
	private static final String DATA_SECURITY_FOLDER = "security";
	private static final String DATA_CERT = "credentials.cer";
	private Properties sysProp;
	private String user;
	private String userHome;
	private String workingDir;
	private String configDir;
	private String dataConnDir;
	private String dataSecurityDir;
	private String testObjectDir;
	private Properties evansnetProp;
	
	//Singleton
	public static Global getInstance() {
		if (GlobalInstance == null) {
			GlobalInstance = new Global();
		}
		return GlobalInstance;
	}
	
	private Global() {
		sysProp = System.getProperties();
		user = sysProp.getProperty("user.name");
		userHome = sysProp.getProperty("user.home");
		setWorkingDir(userHome + File.separator + EVANSNET);
		setConfigFolder(workingDir + File.separator + CFG_FOLDER);
		setDataConnDir(workingDir + File.separator + DATA_FOLDER);
		setDataSecurityDir(dataConnDir + File.separator + DATA_SECURITY_FOLDER);
		setTestObjectDir(workingDir + File.separator + TEST_FOLDER);
		evansnetProp = new Properties();
	}
	
	private void setTestObjectDir(String tod) {
		testObjectDir = tod;
	}

	private void setDataConnDir(String d) {
		dataConnDir = d;
	}
	
	private void setDataSecurityDir(String s) {
		dataSecurityDir = s;
	}
	
	public String getDataSecurityDir() {
		return dataSecurityDir;
	}
	
	public String getDataCert() {
		return dataSecurityDir + File.separator + DATA_CERT;
	}
	
	public String getTestDir() {
		return testObjectDir;
	}

	public String getUser() {
		return user;
	}
	
	public String getWorkingDir() {
		return workingDir;
	}
	public void setWorkingDir(String workingDir) {
		this.workingDir = workingDir;
		setConfigFolder(this.workingDir + File.separator + CFG_FOLDER);
	}
	
	/**
	 * The config directory is always the working directory + "cfg"
	 * @return The working director as a string
	 */
	public String getConfigDir() { 
		return configDir;
	}
	
	private void setConfigFolder(String s) {
		configDir = s;
	}
	
	public Properties getEvansnetProp() throws Exception {
		// if the properties are empty, attempt to retrieve them from disk.
		if (evansnetProp.isEmpty()) {
			evansnetProp = fetchProperties(configDir + File.separator + CONFIG_FILE);
		}
		return evansnetProp;
	}
	
	/**
	 * Used to replace the default configuration properties. 
	 * @param - a properties object populated with the values for replacement.
	 */
	public void setEvansnetProp(Properties replacement	) {
		evansnetProp = replacement;
	}
	
	/**
	 * Get the configuration properties from disk.
	 * @param - a string representing the path to the folder containing the configuration.properties file,
	 * (the "cfg" folder).
	 * @return  Returns a properties object containing the product configuration settings. Can be an empty object.
	 * @throws InvalidPathException
	 * @throws IOException
	 */
	public Properties fetchProperties(String cfgPath) throws IOException {
		String methodName = "fetchProperties()";
		FileSystem fs = getFileSystem();

		if (cfgPath == null || cfgPath.isEmpty()) {
			//get from the default location
			javaLogger.logp(Level.INFO, THIS_CLASS_NAME, methodName, 
					"The configuration path submitted is null or empty. Using default path instead.");
			cfgPath = configDir;
		}
		
		try {
			Path theCfgPath = fs.getPath(cfgPath);
			if (!pathExists(cfgPath, fs)) {
				createDefaultConfigPath();
			}
			if (!theCfgPath.toString().contains(CONFIG_FILE)) {
				theCfgPath = fs.getPath(theCfgPath.toString() + File.separator + CONFIG_FILE);
			}
			if (!isConfigFileExists(theCfgPath.toString())) {
				Files.createFile(fs.getPath(theCfgPath.toString()));
			}
			evansnetProp.load(new FileReader(theCfgPath.toString()));
			return evansnetProp;
			} catch (AccessDeniedException ade) {
				javaLogger.logp(Level.SEVERE, THIS_CLASS_NAME, methodName, 
						"Access denied to " + cfgPath + "\n" + ade.getMessage());
				return null;
			} catch (Exception e) {
				javaLogger.logp(Level.SEVERE, THIS_CLASS_NAME, methodName, 
						"An unexpected exception was thrown while fetching configuration properties: " + e.getMessage() + "\n");
				return null;
		}
	}
	
	/**
	 * Saves the configuration to disk storage. If the parameter cfgPath is null or empty the 
	 * file is saved to the default location in the user's home directory. If the configuration is 
	 * empty, it is set to default values. 
	 * Note that the method assumes that the configuration.properties file name is part of the path.
	 * @param cfgPath - A string representing the path to the desired storage location.
	 * @throws IOException
	 */
	public void saveConfig(String cfgPath) throws IOException {
		FileSystem fs = getFileSystem();
		if (evansnetProp == null || evansnetProp.isEmpty() ) {
			setDefaultProperties();
		}
		try {
			if (!(cfgPath.contains(CONFIG_FILE))) {
				cfgPath = cfgPath + File.separator + CONFIG_FILE;
			}
			if (pathExists(cfgPath, fs)) {
					evansnetProp.store(new FileWriter(cfgPath), 
							"Properties for evansnet products");
			} else {
				//Default to the default evansnet config path. Create it if needed.
				createDefaultConfigPath();
				evansnetProp.store(new FileWriter(configDir + File.separator + CONFIG_FILE), 
						"Properties for evansnet products");
			}
		} catch (IOException eio) {
			javaLogger.log(Level.SEVERE, "IOException occurred while saving configuration " + 
		eio.getMessage());
		}
	}

	/**
	 * Returns the default file system.
	 */
	private FileSystem getFileSystem() {
		return FileSystems.getDefault();
	}
	
	/**
	 * Given a string representing a filesystem path, checks to see if the path exists.
	 * @param thePath - A string representing a path on the filesystem.
	 * @param fs - The file system being checked.
	 * @return True if the path exists, false otherwise.
	 * @throws InvalidPathException
	 */
	private boolean pathExists(String thePath, FileSystem fs) {
		try {
			Path cfgPath = fs.getPath(thePath);
			if (cfgPath.toFile().exists()) {
				return true;
			}
		} catch (InvalidPathException ipe) {
			javaLogger.logp(Level.SEVERE, THIS_CLASS_NAME, "pathExists()", 
					"Caught invalid path exception when checking the path's existence. " + ipe.getMessage());
		}
		return false;
	}
	
	/**
	 * Given a string that represents the path to the string and the "configuration.properties" file name, 
	 * this method confirms the existence of the file on the file system. 
	 * @param thePath
	 * @return true if the configuration.properties file exists, false otherwise.
	 * @throws IOException 
	 */
	private boolean isConfigFileExists(String thePath) throws IOException {
		FileSystem fs = getFileSystem();
		try {
			Path cfgPath = fs.getPath(thePath);
			if (cfgPath.toFile().exists()) {
				return true;
			}
		} catch (Exception e) {
			javaLogger.logp(Level.SEVERE, THIS_CLASS_NAME, "configFileExists()", 
					"Configuration file could not be found. " + e.getMessage());
		}
		return false;
	} 
	
	/**
	 * Call this method to set the default configuration into the properties object. This 
	 * method populates the properties object. It does not save the object to disk.
	 */
	private void setDefaultProperties() {
		if (!evansnetProp.isEmpty()) {
			evansnetProp.clear();
		}
		evansnetProp.setProperty("configuration_file", CONFIG_FILE);
		evansnetProp.setProperty("user", user);
		evansnetProp.setProperty("user_Home", userHome);
		evansnetProp.setProperty("working_directory", workingDir);
	}
	
	/**
	 * Called to create the default configuration folder and file if they 
	 * do not exist.
	 */
	private void createDefaultConfigPath() throws IOException, InvalidPathException, SecurityException, FileAlreadyExistsException {
		FileSystem fs = getFileSystem();
		if (pathExists(userHome, fs)) {
			if (!(pathExists(userHome + File.separator + EVANSNET, fs))) {
				// create the paths to the config file. 
				Files.createDirectories(Paths.get(configDir));
				Files.createFile(Paths.get(configDir + File.separator + CONFIG_FILE));
			}
		} else {
			throw new FileNotFoundException("The user's home directory was not found. ");
		} 
	}
}
