/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package positioning;

import java.awt.Point;
import java.util.Date;

/**
 *
 * @author James Licata
 */
public class AccessPoint
        implements Comparable<AccessPoint>
{
   public double PIXELS_PER_FOOT = 5.7;
   public double FEET_PER_METER = 0.3048;
   public static final int MIN_SIGNAL_LEVEL = Integer.MIN_VALUE;

   String SSID;

   String macAddress;
   int mRSSdBsm = 0;
   Point mAccessPointCoordinate = new Point(0, 0);
   double signalStrengthToDistanceRatio = 1;

   Date timestamp;

   public AccessPoint(int RSS, Point accessPointLocation, String ssid)
   {
      this.setCoordinates(accessPointLocation);
      this.setSignalLevel(RSS);
      this.setSSID(ssid);
   }//AccessPoint

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
      return mRSSdBsm;
   }

   public void setSignalLevel(int signalLevel)
   {
      this.mRSSdBsm = signalLevel;
   }

   public Point getCoordinates()
   {
      return mAccessPointCoordinate;
   }

   public void setCoordinates(Point coordinates)
   {
      this.mAccessPointCoordinate = coordinates;
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

   public double getDistanceMeters()
   {
      double exp = (27.55 - (20 * Math.log10(2437)) + Math.abs(this.getSignalLevel())) / 20.0;
      return Math.pow(10.0, exp);
   }//getDistanceMeters

   public double getDistance()
   {
      //Return pixel distance
      double distance_meters = getDistanceMeters();
      double distance_feet = distance_meters * this.FEET_PER_METER;

      return distance_feet * this.PIXELS_PER_FOOT;
   }

   @Override
   public int compareTo(AccessPoint other)
   {
      // compareTo should return < 0 if this is supposed to be
      // less than other, > 0 if this is supposed to be greater than
      // other and 0 if they are supposed to be equal
      int result = ((Integer) mRSSdBsm).compareTo((Integer) other.mRSSdBsm);
      return result;
   }//compareTo
}//AccessPoint
