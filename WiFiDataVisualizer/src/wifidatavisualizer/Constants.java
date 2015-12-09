/*
 * File containing constants related to the Wifi Data Visualizer application
 *
 */
package wifidatavisualizer;

/**
 * Constants related to the Wifi Data Visualizer application
 *
 * @author James Licata
 */
public class Constants
{
   final public static String ROUTER_PREFIX_SSID = "CiscoLinksysE120";
   final public static String DEFAULT_DATA_PATH = "data/02012015-5sec/";
   final public static String DEFAULT_DATA_FILE_EXTENSION = ".csv";
   final public static int DEFAULT_NUMBER_OF_ROUTERS = 4;
   final public static int DEFAULT_PIXEL_ADJUSTMENT = 25;
   final public static int DEFAULT_CALIBRATION_START_POINT_X_COORDINATE = 500;
   final public static int DEFAULT_CALIBRATION_START_POINT_Y_COORDINATE = 500;

   //CSV Data File Constants
   final public static String CSV_FILE_RECORD_TIMESTAMP_COLUMN = "Timestamp";
   final public static String CSV_FILE_RECORD_RSS_COLUMN = "RSS";
}//Constants
