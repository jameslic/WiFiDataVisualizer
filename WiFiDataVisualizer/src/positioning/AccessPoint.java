/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package positioning;

import java.util.Date;

/**
 *
 * @author James Licata
 */
public class AccessPoint
{
   public static final int MIN_SIGNAL_LEVEL = Integer.MIN_VALUE;

   String SSID;

   String macAddress;
   int signalLevel = 0;
   Point2D coordinates = new Point2D(0, 0);
   double signalStrengthToDistanceRatio = 1;

   Date timestamp;

   public Date getTimestamp()
   {
      return timestamp;
   }

   public void setTimestamp(Date timestamp)
   {
      this.timestamp = timestamp;
   }

   public String getMacAddress()
   {
      return macAddress;
   }

   public void setMacAddress(String macAddress)
   {
      this.macAddress = macAddress.toUpperCase();
   }

   public int getSignalLevel()
   {
      return signalLevel;
   }

   public void setSignalLevel(int signalLevel)
   {
      this.signalLevel = signalLevel;
   }

   public Point2D getCoordinates()
   {
      return coordinates;
   }

   public void setCoordinates(Point2D coordinates)
   {
      this.coordinates = coordinates;
   }

   public double getSignalStrengthToDistanceRatio()
   {
      return signalStrengthToDistanceRatio;
   }

   public void setSignalStrengthToDistanceRatio(
           double signalStrengthToDistanceRatio)
   {
      this.signalStrengthToDistanceRatio = signalStrengthToDistanceRatio;
   }

   public String getSSID()
   {
      return SSID;
   }

   public void setSSID(String sSID)
   {
      SSID = sSID;
   }

   public double getDistance()
   {
      return signalStrengthToDistanceRatio * (100 + signalLevel);
   }
}
