/*
 * File containing constants related to the Wifi Data Visualizer application
 *
 */
package wifidatavisualizer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
   final public static int DEFAULT_CALIBRATION_START_POINT_X_COORDINATE = 1081;
   final public static int DEFAULT_CALIBRATION_START_POINT_Y_COORDINATE = 518;

   final public static int DEFAULT_WIFI_DATA_COLLECTION_INTERVAL_MILLISECONDS = 5000;

   //Map conversions
   final public static double PIXELS_PER_FOOT = 5.7;
   final public static double FEET_PER_METER = 0.3048;
   public static final int MIN_SIGNAL_LEVEL = Integer.MIN_VALUE;

   //CSV Data File Constants
   final public static String CSV_FILE_RECORD_TIMESTAMP_COLUMN = "Timestamp";
   final public static String CSV_FILE_RECORD_RSS_COLUMN = "RSS";

   //Fingerprinting
   final public static int FINGERPRINTING_K_NEAREST_NEIGHBORS = 4;
   final public static int FINGERPRINTING_K_NEAREST_NEIGHBORS_DISTANCE_TOLERANCE = 90;

   //Weighted Centroid
   final public static int WEIGHTED_CENTROID_NUMBER_OF_POINTS = 4;

   //Training Data Points
   final public static int DEFAULT_TRAINING_DATA_POINT_SIZE = 16;
   final public static int DEFAULT_TRAINING_DATA_POINT_MIDPOINT = 8;

   //Truth Data Points
   final public static int DEFAULT_TRUTH_DATA_POINT_SIZE = 8;
   final public static int DEFAULT_TRUTH_DATA_POINT_MIDPOINT = 4;

   //Router (Access Point) Points
   final public static int DEFAULT_ROUTER_POINT_X_OFFSET = 21;
   final public static int DEFAULT_ROUTER_POINT_Y_OFFSET = 12;
   final public static int DEFAULT_ROUTER_POINT_WIDTH = 52;
   final public static int DEFAULT_ROUTER_POINT_HEIGHT = 31;

   //Range Rings
   final public static int DEFAULT_RANGE_RING_SIZE = 250;
   final public static int DEFAULT_RANGE_RING_OFFSET = 125;
   final public static int DEFAULT_RANGE_RING_LINE_WIDTH = 5;

   //Point Font
   final public static int DEFAULT_POINT_LABEL_FONT_SIZE = 24;
   final public static int DEFAULT_POINT_LABEL_FONT_Y_OFFSET = 10;

   //Points
   final public static int DEFAULT_POINT_CIRCLE_SIZE = 24;
   final public static int DEFAULT_POINT_OFFSET = 12;

   //Calibration Point
   final public static int DEFAULT_CALIBRATION_POINT_CIRCLE_SIZE = 16;
   final public static int DEFAULT_CALIBRATION_POINT_CIRCLE_OFFSET = 8;

   public static Map<String, Double> getRouterWeightMap()
   {
      Map<String, Double> result = new HashMap<>();
      result.put(ROUTER_PREFIX_SSID + "0", 0.85);
      result.put(ROUTER_PREFIX_SSID + "1", 0.75);
      result.put(ROUTER_PREFIX_SSID + "2", 0.80);
      result.put(ROUTER_PREFIX_SSID + "3", 0.95);
      return Collections.unmodifiableMap(result);
   }//getRouterWeightMap
}//Constants
