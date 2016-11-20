/*
 * Università degli Studi di Napoli Parthenope
 */
package ita.parthenope.twitternlp.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import java.util.Vector;
import java.lang.SecurityException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;




/**
 * Crittografia delle password con Jasypt API.
 * Vengono crittografate le password presenti nei file .properties
 * @author onofrio
 *
 */
public class Hashing
{
	String oraclePropertiesFile;
	String scribePropertiesFile;
	String apiKeySecretProperty;
	String accessTokenSecretProperty;
	String oraclePasswordProperty;
	String isOraclePasswordEncrypted;
	String isScribePropertiesEncrypted;
	
	
	public Hashing() {
		super();
	}
	

	/**
	 * This constructor is used for password encryption.
	 * 
	 * @param oraclePropertiesFile
	 * @param scribePropertiesFile
	 * @param apiKeySecretProperty
	 * @param accessTokenSecretProperty
	 * @param oraclePasswordProperty
	 * @param isOraclePasswordEncrypted
	 * @param isScribePropertiesEncrypted
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	public Hashing(String oraclePropertiesFile, String scribePropertiesFile, String apiKeySecretProperty,
			String accessTokenSecretProperty, String oraclePasswordProperty, String isOraclePasswordEncrypted,
			String isScribePropertiesEncrypted) throws ConfigurationException, IOException {
		super();
		this.oraclePropertiesFile = oraclePropertiesFile;
		this.scribePropertiesFile = scribePropertiesFile;
		this.apiKeySecretProperty = apiKeySecretProperty;
		this.accessTokenSecretProperty = accessTokenSecretProperty;
		this.oraclePasswordProperty = oraclePasswordProperty;
		this.isOraclePasswordEncrypted = isOraclePasswordEncrypted;
		this.isScribePropertiesEncrypted = isScribePropertiesEncrypted;
		
		OraclePropertiesEncryptor();
		ScribePropertiesEncryptor();
	}
	

	/**
     * The method that encrypt oracle password in the properties file.
     * This method will first check if the password is already encrypted or not.
     * If not then only it will encrypt the password.
     *
     * @throws ConfigurationException
     * @throws IOException
     */
    private void OraclePropertiesEncryptor() throws ConfigurationException, IOException
    {
        System.out.println("Starting encryption operation");
        System.out.println("Start reading properties file");

        //Apache Commons Configuration
        Configuration config = new PropertiesConfiguration(oraclePropertiesFile);

        //Retrieve boolean properties value to see if password is already encrypted or not
        String isEncrypted = config.getString(isOraclePasswordEncrypted);

        //Check if password is encrypted?
        if(isEncrypted.equals("false")) 
        {
            String tmpPwd = config.getString(oraclePasswordProperty);
            System.out.println(tmpPwd);
            //Encrypt
            StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
            // This is a required password for Jasypt. You will have to use the same password to
            // retrieve decrypted password later. 
            // This password is not the password we are trying to encrypt taken from properties file.
            encryptor.setPassword("jasypt");
            String encryptedPassword = encryptor.encrypt(tmpPwd);
            System.out.println("\nEncryption done and encrypted Oracle password is : " + encryptedPassword );

            String driver = (String) config.getProperty("oracle.driverClassName");
            String url = (String) config.getProperty("oracle.url");
            String username = (String) config.getProperty("oracle.username"); 
            
            
        	try {
        		File f = new File("config/oracle.properties").getAbsoluteFile();
        		if (!f.isDirectory()) {
        			f.delete();
        		}
        	} catch (SecurityException sec) {
        	    sec.printStackTrace();
        	}

            Properties prop = new Properties();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(oraclePropertiesFile);
            HashMap<String, String> mp = new HashMap<String,String>();
            prop.load(inputStream);
            mp.put("oracle.driverClassName", driver);
            mp.put("oracle.url", url);
            mp.put("oracle.username", username);
            mp.put("oracle.password", encryptedPassword);
            mp.put("is.database.password.encrypted", "true");
            new PropertiesFileCreator("config/"+oraclePropertiesFile, "Oracle Properties").createNewFile(mp);
            System.out.println();
        } 
        else {
             System.out.println("Oracle password is already encrypted.\n ");
        }
    }
    
    
    /**
     * The method that encrypt scribe properties in the properties file.
     * This method will first check if the two properties is already encrypted or not.
     * If not then only it will encrypt the oauth properties.
     *
     * @throws ConfigurationException
     * @throws IOException
     */
    private void ScribePropertiesEncryptor() throws IOException, ConfigurationException 
    {
    	System.out.println("Starting encryption operation");
        System.out.println("Start reading properties file");

        //Apache Commons Configuration
        Configuration config = new PropertiesConfiguration(scribePropertiesFile);

        //Retrieve boolean properties value to see if password is already encrypted or not
        String isEncrypted = config.getString(isScribePropertiesEncrypted);

        //Check if password is encrypted?
        if(isEncrypted.equals("false")) 
        {
            String apiKey = config.getString(apiKeySecretProperty);
            String accessToken = config.getString(accessTokenSecretProperty);
            System.out.println(apiKey + " , " + accessToken);
            //Encrypt
            StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
            // This is a required password for Jasypt. You will have to use the same password to
            // retrieve decrypted password later. 
            // This password is not the password we are trying to encrypt taken from properties file.
            encryptor.setPassword("jasypt");
            String encryptedApiKeySecret = encryptor.encrypt(apiKey);
            String encryptedAccessTokenSecret = encryptor.encrypt(accessToken);
            System.out.println("\nEncryption done and encrypted OAuth credentials are: " + encryptedApiKeySecret + " , " + encryptedAccessTokenSecret);

            String apiOAuth = (String) config.getProperty("oauth.apiKey");
            String tokenOAuth = (String) config.getProperty("oauth.accessToken");
            String waitingSecs = (String) config.getProperty("oauth.secondiAttesa");
            String minsAnalysis = (String) config.getProperty("oauth.minutiAnalisi");
            
            
        	try {
        		File f = new File("config/scribe.properties").getAbsoluteFile();
        		if (!f.isDirectory()) {
        			f.delete();
        		}
        	} catch (SecurityException sec) {
        		Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, sec);
        	}

            Properties prop = new Properties();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(scribePropertiesFile);
            HashMap<String, String> mp = new HashMap<String,String>();
            prop.load(inputStream);
            mp.put("oauth.apiKey", apiOAuth);
            mp.put("oauth.apiSecret", encryptedApiKeySecret);
            mp.put("oauth.accessToken", tokenOAuth);
            mp.put("oauth.accessTokenSecret", encryptedAccessTokenSecret);
            mp.put("oauth.secondiAttesa", waitingSecs);
            mp.put("oauth.minutiAnalisi", minsAnalysis);
            mp.put("is.oauth.properties.encrypted", "true");
            new PropertiesFileCreator("config/"+scribePropertiesFile, "Scribe Properties").createNewFile(mp);
            System.out.println();
        } 
        else {
             System.out.println("OAuth credentials are already encrypted.\n ");
        }
    }
    
    
    /**
     * This method perform Oracle database password decryption.
     * 
     * @return the decrypted password
     * @throws ConfigurationException
     */
    public String OraclePasswordDecryptor() throws ConfigurationException 
    {
         //Apache Commons Configuration
         Configuration config = new PropertiesConfiguration("oracle.properties");

         //Retrieve boolean properties value to see if password is already encrypted or not
         String isEncrypted = config.getString("is.database.password.encrypted");

         //Check if password is decrypted?
         if(isEncrypted.equals("true")) 
         {
         	String encryptedPropertyValue = config.getString("oracle.password");
         	StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
            encryptor.setPassword("jasypt");
            //Decrypt the password
            String decryptedOracleProperty = encryptor.decrypt(encryptedPropertyValue);
             
            return decryptedOracleProperty;
         }
         else {
        	 System.out.println("Oracle password is already decrypted.");
        	 return "";
         }
    }

    
    /**
     * The method that decrypt oracle properties in the properties file
     * also create a brand new .properties file.
     * 
     * @throws ConfigurationException
     * @throws IOException
     */
    public void OraclePropertiesDecryptor() throws ConfigurationException, IOException
    {
        System.out.println("Starting decryption");
        System.out.println("Start reading properties file");
        
        //Apache Commons Configuration
        Configuration config = new PropertiesConfiguration("oracle.properties");

        //Retrieve boolean properties value to see if password is already encrypted or not
        String isEncrypted = config.getString("is.database.password.encrypted");

        //Check if password is decrypted?
        if(isEncrypted.equals("true")) 
        {
        	String encryptedPropertyValue = config.getString("oracle.password");
            System.out.println(encryptedPropertyValue);

            StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
            encryptor.setPassword("jasypt");
            //Decrypt the password
            String decryptedPropertyValue = encryptor.decrypt(encryptedPropertyValue);
            System.out.println("Oracle password decrypted:" + decryptedPropertyValue);
            
            String driver = (String) config.getProperty("oracle.driverClassName");
            String url = (String) config.getProperty("oracle.url");
            String username = (String) config.getProperty("oracle.username"); 
            
            
            try {
            	File f = new File("config/oracle.properties").getAbsoluteFile();
            	if (!f.isDirectory()) {
            		f.delete();
            	}
        	} catch (SecurityException sec) {
        	    sec.printStackTrace();
        	}
            
            Properties prop = new Properties();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("oracle.properties");
            HashMap<String, String> mp = new HashMap<String,String>();
            prop.load(inputStream);
                
            mp.put("oracle.driverClassName", driver);
            mp.put("oracle.url", url);
            mp.put("oracle.username", username);
            mp.put("oracle.password", decryptedPropertyValue);
            mp.put("is.database.password.encrypted", "false");
            new PropertiesFileCreator("config/oracle.properties", "Oracle Properties").createNewFile(mp);
        } 
        else {
        	System.out.println("Oracle password is already decrypted.\n ");
        }
        
    }
    

    /**
     * The method that decrypt scribe properties in the properties file
     * also create a brand new .properties file.
     * 
     * @throws ConfigurationException
     * @throws IOException
     */
    public void ScribePropertiesDecryptor() throws ConfigurationException, IOException
    {
    	System.out.println("Starting decryption");
        System.out.println("Start reading properties file");
        
        //Apache Commons Configuration
        Configuration config = new PropertiesConfiguration("scribe.properties");

        //Retrieve boolean properties value to see if password is already encrypted or not
        String isEncrypted = config.getString("is.oauth.properties.encrypted");

        //Check if password is decrypted?
        if(isEncrypted.equals("true")) 
        {
        	String encryptedPropertyApiSecret = config.getString("oauth.apiSecret");
        	String encryptedPropertyAccessTokenSecret = config.getString("oauth.accessTokenSecret");
            System.out.println(encryptedPropertyApiSecret + " , " + encryptedPropertyAccessTokenSecret);

            StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
            encryptor.setPassword("jasypt");
            //Decrypt the credentials
            String decryptedPropertyApiSecret = encryptor.decrypt(encryptedPropertyApiSecret);
            String decryptedPropertyAccessTokenSecret = encryptor.decrypt(encryptedPropertyAccessTokenSecret);
            System.out.println("OAuth credentials decrypted:" + decryptedPropertyApiSecret + " , " + decryptedPropertyAccessTokenSecret);;
            
            String apiOAuth = (String) config.getProperty("oauth.apiKey");
            String tokenOAuth = (String) config.getProperty("oauth.accessToken");
            String waitingSecs = (String) config.getProperty("oauth.secondiAttesa");
            String minsAnalysis = (String) config.getProperty("oauth.minutiAnalisi");
            
            
        	try {
        		File f = new File("config/scribe.properties").getAbsoluteFile();
        		if (!f.isDirectory()) {
        			f.delete();
        		}
        	} catch (SecurityException sec) {
        	    sec.printStackTrace();
        	}
        	
            Properties prop = new Properties();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("scribe.properties");
            HashMap<String, String> mp = new HashMap<String,String>();
            prop.load(inputStream);
            mp.put("oauth.apiKey", apiOAuth);
            mp.put("oauth.apiSecret", decryptedPropertyApiSecret);
            mp.put("oauth.accessToken", tokenOAuth);
            mp.put("oauth.accessTokenSecret", decryptedPropertyAccessTokenSecret);
            mp.put("oauth.secondiAttesa", waitingSecs);
            mp.put("oauth.minutiAnalisi", minsAnalysis);
            mp.put("is.oauth.properties.encrypted", "true");
            new PropertiesFileCreator("config/scribe.properties", "Scribe Properties").createNewFile(mp);
        } 
        else {
        	System.out.println("OAuth credentials are already decrypted.\n ");
        }
    }
    
    
    /**
     * This method perform OAuth properties decryption.
     * 
     * @return the vector with decrypted oauth properties
     * @throws ConfigurationException
     */
    public Vector<String> ScribeOAuthDecryptor() throws ConfigurationException 
    {
    	//Apache Commons Configuration
        Configuration config = new PropertiesConfiguration("scribe.properties");

        //Retrieve boolean properties value to see if password is already encrypted or not
        String isEncrypted = config.getString("is.oauth.properties.encrypted");

        //Check if password is decrypted?
        if(isEncrypted.equals("true")) 
        {
        	String encryptedPropertyApiKeySecret = config.getString("oauth.apiSecret");
        	String encryptedPropertyAccessTokenSecret = config.getString("oauth.accessTokenSecret");
        	StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        	encryptor.setPassword("jasypt");
        	
        	//Decrypt the password
        	String decryptedApiSecretProperty = encryptor.decrypt(encryptedPropertyApiKeySecret);
        	String decryptedAccessTokenSecretProperty = encryptor.decrypt(encryptedPropertyAccessTokenSecret);
        	
        	Vector<String> credentials = new Vector<String>();
        	credentials.add(0, decryptedApiSecretProperty);
        	credentials.add(1, decryptedAccessTokenSecretProperty);
        	
        	return credentials;
        }
        else {
       	 System.out.println("OAuth credentials are already decrypted.");
       	 return null;
        }
    }


	/**
	 * @return the oraclePropertiesFile
	 */
	public String getOraclePropertiesFile() {
		return oraclePropertiesFile;
	}


	/**
	 * @param oraclePropertiesFile the oraclePropertiesFile to set
	 */
	public void setOraclePropertiesFile(String oraclePropertiesFile) {
		this.oraclePropertiesFile = oraclePropertiesFile;
	}


	/**
	 * @return the scribePropertiesFile
	 */
	public String getScribePropertiesFile() {
		return scribePropertiesFile;
	}


	/**
	 * @param scribePropertiesFile the scribePropertiesFile to set
	 */
	public void setScribePropertiesFile(String scribePropertiesFile) {
		this.scribePropertiesFile = scribePropertiesFile;
	}


	/**
	 * @return the apiKeySecretProperty
	 */
	public String getApiKeySecretProperty() {
		return apiKeySecretProperty;
	}


	/**
	 * @param apiKeySecretProperty the apiKeySecretProperty to set
	 */
	public void setApiKeySecretProperty(String apiKeySecretProperty) {
		this.apiKeySecretProperty = apiKeySecretProperty;
	}


	/**
	 * @return the accessTokenSecretProperty
	 */
	public String getAccessTokenSecretProperty() {
		return accessTokenSecretProperty;
	}


	/**
	 * @param accessTokenSecretProperty the accessTokenSecretProperty to set
	 */
	public void setAccessTokenSecretProperty(String accessTokenSecretProperty) {
		this.accessTokenSecretProperty = accessTokenSecretProperty;
	}


	/**
	 * @return the oraclePasswordProperty
	 */
	public String getOraclePasswordProperty() {
		return oraclePasswordProperty;
	}


	/**
	 * @param oraclePasswordProperty the oraclePasswordProperty to set
	 */
	public void setOraclePasswordProperty(String oraclePasswordProperty) {
		this.oraclePasswordProperty = oraclePasswordProperty;
	}


	/**
	 * @return the isOraclePasswordEncrypted
	 */
	public String getIsOraclePasswordEncrypted() {
		return isOraclePasswordEncrypted;
	}


	/**
	 * @param isOraclePasswordEncrypted the isOraclePasswordEncrypted to set
	 */
	public void setIsOraclePasswordEncrypted(String isOraclePasswordEncrypted) {
		this.isOraclePasswordEncrypted = isOraclePasswordEncrypted;
	}


	/**
	 * @return the isScribePropertiesEncrypted
	 */
	public String getIsScribePropertiesEncrypted() {
		return isScribePropertiesEncrypted;
	}


	/**
	 * @param isScribePropertiesEncrypted the isScribePropertiesEncrypted to set
	 */
	public void setIsScribePropertiesEncrypted(String isScribePropertiesEncrypted) {
		this.isScribePropertiesEncrypted = isScribePropertiesEncrypted;
	}
    
    
}



class PropertiesFileCreator
{
	String propertiesName;
	String description;
	
	
	public PropertiesFileCreator(String propertiesName, String description) {
		this.propertiesName = propertiesName;
		this.description = description;
	}

	
	/**
	 * This method create a new Java .properties file.
	 * @param mp
	 *            the input HashMap
	 */
	public void createNewFile(HashMap<String,String> mp)
	{
		System.setProperty("file.encoding", "UTF-8");
		try {
			Properties properties = new Properties();
			Iterator<Entry<String, String>> it = mp.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> pair = it.next();
				System.out.println(pair.getKey() + " : " + pair.getValue());
				properties.setProperty(pair.getKey().toString(), pair.getValue().toString());
			}

			File file = new File(propertiesName);
			FileOutputStream fileOut = new FileOutputStream(file);
			properties.store(fileOut, description);
			fileOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	/**
	 * @return the propertiesName
	 */
	public String getPropertiesName() {
		return propertiesName;
	}


	/**
	 * @param propertiesName the propertiesName to set
	 */
	public void setPropertiesName(String propertiesName) {
		this.propertiesName = propertiesName;
	}


	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}


	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	

}

