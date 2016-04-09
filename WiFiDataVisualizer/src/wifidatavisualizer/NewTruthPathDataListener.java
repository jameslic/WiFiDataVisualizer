/*
 * Listener interface for classes to implement in handling Wifi data
 *
 */
package wifidatavisualizer;

import java.awt.Point;
import java.util.ArrayList;

/**
 * An interface to be implemented by everyone interested in "New Wifi Data"
 * events
 *
 * @author James Licata
 */
public interface NewTruthPathDataListener
{
   /**
    * Main function for handling new wifi 2D point data
    *
    * @param newWifiPoint the 2D point for incoming data
    * @param dataType     the algorithm type associated with the data point
    */
   public void newTruthPath(ArrayList<Point> newTruthPath);

}//NewWifiDataListener
