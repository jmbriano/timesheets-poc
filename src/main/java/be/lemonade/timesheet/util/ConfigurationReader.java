package be.lemonade.timesheet.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by nemo on 5/1/17.
 */
public class ConfigurationReader {

    public static String RELEVANT_PROJECTS = "RELEVANT_PROJECTS";
    public static String FRESHBOOK_EXPORT_FILENAME = "FRESHBOOK_EXPORT_FILENAME";
    public static String TEMPLATE_FILENAME = "TEMPLATE_FILENAME";
    public static String OUTPUT_FILENAME = "OUTPUT_FILENAME";
    public static String OUTPUT_DIR = "OUTPUT_DIR";
    public static String SPLIT_MODE = "SPLIT_MODE";
    public static String EXPAND = "EXPAND";
    public static String MONTH = "MONTH";
    public static String YEAR = "YEAR";
    public static String FRESHBOOK_DATE_FORMAT = "FRESHBOOK_DATE_FORMAT";
    public static String FIRST_PROJECT_ROW = "FIRST_PROJECT_ROW";
    public static String FIRST_DATE_COLUMN = "FIRST_DATE_COLUMN";
    public static String HOURS_FORMAT = "HOURS_FORMAT";
    public static String FORECAST_TAG_ROW = "FORECAST_TAG_ROW";

    private Properties properties;

    public ConfigurationReader() throws IOException {
        properties = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream("configuration/config");

            // load a properties file
            properties.load(input);

        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getValue(String configKey){
        return properties.getProperty(configKey,null);
    }
}
