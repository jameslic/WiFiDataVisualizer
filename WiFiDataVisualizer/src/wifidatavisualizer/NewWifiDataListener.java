/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wifidatavisualizer;

/**
 *
 * @author James Licata
 */
import java.awt.Point;

// An interface to be implemented by everyone interested in "Hello" events
public interface NewWifiDataListener
{
   public void newWifiData(Point newWifiPoint, WifiDataType dataType);

   enum WifiDataType
   {
      TRILATERATION,
      TRIANGULATION,
      FINGERPRINTING
   }

}
