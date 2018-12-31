package be.lemonade.timesheet.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigurationReader {

    public static String DATA_SOURCE = "DATA_SOURCE";
    public static String RELEVANT_PROJECTS = "RELEVANT_PROJECTS";
    public static String FRESHBOOK_EXPORT_FILENAME = "FRESHBOOK_EXPORT_FILENAME";
    public static String FRESHBOOK_API_URL = "FRESHBOOK_API_URL";
    public static String FRESHBOOK_API_TOKEN = "FRESHBOOK_API_TOKEN";
    public static String FRESHBOOK_OUTPUT_CSV_NAME = "FRESHBOOK_OUTPUT_CSV_NAME";
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
    public static String WARNING_LIMIT_HR = "WARNING_LIMIT_HR";

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
