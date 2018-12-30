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
    public static String MONTH = "MONTH";
    public static String YEAR = "YEAR";
    public static String FRESHBOOK_DATE_FORMAT = "FRESHBOOK_DATE_FORMAT";
    public static String FIRST_PROJECT_ROW = "FIRST_PROJECT_ROW";
    public static String NATIONAL_HOLIDAYS = "NATIONAL_HOLIDAYS";
    public static String NAT_HOLIDAYS_ROW = "NAT_HOLIDAYS_ROW";
    public static String HOURS_FORMAT = "HOURS_FORMAT";
    public static String WARNING_WHEN_WDAY_TOTAL_ABOVE = "WARNING_WHEN_WDAY_TOTAL_ABOVE";
    public static String WARNING_WHEN_HOURS_ON_WEEKENDS = "WARNING_WHEN_HOURS_ON_WEEKENDS";

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
