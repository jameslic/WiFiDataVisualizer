/*
 * Listener interface for classes to implement in handling Wifi data
 *
 */
package wifidatavisualizer;

import java.awt.Point;

/**
 * An interface to be implemented by everyone interested in "New Wifi Data"
 * events
 *
 * @author James Licata
 */
public interface NewWifiDataListener
{
   /**
    * Main function for handling new wifi 2D point data
    *
    * @param newWifiPoint the 2D point for incoming data
    * @param dataType     the algorithm type associated with the data point
    */
   public void newWifiData(Point newWifiPoint, WifiDataType dataType);

   /**
    * Helper function to control listener to display only a given number of data
    * points
    *
    * @param nPoints  The number of data points for the interface to supply
    * @param dataType The algorithm type associated with the data point
    * @return the number of points returned
    */
   public int displayLastNPoints(int nPoints, WifiDataType dataType);

   /**
    * Enumeration for Wifi Algorithm indicators for data
    */
   enum WifiDataType
   {
      TRILATERATION,
      TRIANGULATION,
      FINGERPRINTING,
      WEIGHTED_CENTROID,
      PATTERN_MATCHING,
      DEFAULT
   }//WifiDataType

}//NewWifiDataListener
