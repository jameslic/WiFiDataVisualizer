/*
 * Class representing a known RSS data point in reference to a known Access Point location
 */
package positioning;

import java.awt.Point;
import java.util.Date;
import wifidatavisualizer.Constants;

/**
 *
 * @author James Licata
 */
public class AccessPointObservationRecord
        implements Comparable<AccessPointObservationRecord>
{
   /*
   The router's SSID
    */
   String SSID;

   /**
    * The router's MAC address
    */
   String macAddress;
   /**
    * The router's Received Signal Strength in dBsm
    */
   int mRSSdBsm = 0;
   /**
    * The estimate for the relative image location of the router in X, Y
    * coordinates
    */
   Point mAccessPointCoordinate = new Point(0, 0);

   /**
    * Ratio to use for signal strength to distance calculations
    */
   double signalStrengthToDistanceRatio = 1;

   /**
    * The timestamp associated with the accesspoint
    */
   Date timestamp;

   /**
    * Main constructor for Access Point class creation
    *
    * @param RSS                 the received signal strength
    * @param accessPointLocation the X, Y access point location
    * @param ssid                the Access Point SSID
    */
   public AccessPointObservationRecord(int RSS, Point accessPointLocation, String ssid)
   {
      setCoordinates(accessPointLocation);
      setSignalLevel(RSS);
      setSSID(ssid);
   }//AccessPoint

   /**
    * Returns the timestamp associated with the access point
    *
    * @return The date formatted timestamp
    */
   public Date getTimestamp()
   {
      return timestamp;
   }//getTimestamp

   /**
    *
    * @param timestamp
    */
   public void setTimestamp(Date timestamp)
   {
      this.timestamp = timestamp;
   }//setTimestamp

   /**
    *
    * @return
    */
   public String getMacAddress()
   {
      return macAddress;
   }//getMacAddress

   /**
    *
    * @param macAddress
    */
   public void setMacAddress(String macAddress)
   {
      this.macAddress = macAddress.toUpperCase();
   }//setMacAddress

   /**
    * Returns the RSS of the access point
    *
    * @return RSS in dBsm
    */
   public int getSignalLevel()
   {
      return mRSSdBsm;
   }//getSignalLevel

   /**
    * Sets the RSS for the access point
    *
    * @param signalLevel the input signal level in dBsm
    */
   public void setSignalLevel(int signalLevel)
   {
      this.mRSSdBsm = signalLevel;
   }//setSignalLevel

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

   /**
    * Returns the distance in pixels given the RSS and known coordinates of the
    * Access Point
    *
    * @return distance in pixels
    */
   public double getDistancePixels()
   {
      //Return pixel distance
      double distance_meters = getDistanceMeters();
      double distance_feet = distance_meters * Constants.FEET_PER_METER;

      return distance_feet * Constants.PIXELS_PER_FOOT;
   }//getDistancePixels

   @Override
   public int compareTo(AccessPointObservationRecord other)
   {
      // compareTo should return < 0 if this is supposed to be
      // less than other, > 0 if this is supposed to be greater than
      // other and 0 if they are supposed to be equal
      int result = ((Integer) mRSSdBsm).compareTo((Integer) other.mRSSdBsm);
      return result;
   }//compareTo
}//AccessPoint
