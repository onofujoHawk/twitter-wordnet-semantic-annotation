package net.didion.jwnl;

import java.io.*;

/**
 * Contains static variables that the JUnit tests need to access. Controls which wordnet
 * is accessed for the test scripts. 
 * @author brett
 *
 */
public class TestDefaults {


    /**
     * The location of the configuration file.
     */
    public static String CONFIG_PATH = "/config/";

    /**
     * The name of the file configuration.
     */
    public static String FILE_CONFIG_NAME = "file_properties.xml";

    /**
     * The name of the database configuration.
     */
    public static String DATABASE_CONFIG_NAME = "database_properties.xml";

    /**
     * The name of the map configuration.
     */
    public static String MAP_CONFIG_NAME = "map_properties.xml";

    /**
     * The flag to use file backed wordnet.
     */
    public static String FILE = "Use File";

    /**
     * The flag to use database backed wordnet.
     */
    public static String DB = "Use DB";

    /**
     * The flag to use a map backed wordnet.
     */
    public static String MAP = "Use Map";

    /**
     * The testing type. Currently either FILE, DB, or MAP.
     */
    public static String testingType = TestDefaults.MAP;


    /**
     * Gets the input stream based on the type.
     *
     * @return input stream
     */
    public static InputStream getInputStream() {
        try {
            if (testingType.equals(TestDefaults.FILE)) {
                return TestDefaults.class.getResourceAsStream(CONFIG_PATH + FILE_CONFIG_NAME);
            } else if (testingType.equals(TestDefaults.DB)) {
                return TestDefaults.class.getResourceAsStream(CONFIG_PATH + DATABASE_CONFIG_NAME);
            } else if (testingType.equals(TestDefaults.MAP)) {
                return TestDefaults.class.getResourceAsStream(CONFIG_PATH + MAP_CONFIG_NAME);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
