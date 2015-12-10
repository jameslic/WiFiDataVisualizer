/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package positioning;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author James Licata
 */
public class FingerprintingPoint
        implements Comparable<FingerprintingPoint>
{
   String mLocationID;

   HashMap<String, Integer> mRouterSignalLevelDifferenceMap = new HashMap<>();
   int mFrequencyCount = 0;
   Point mLocationCoordinates = new Point(0, 0);

   public FingerprintingPoint(Point accessPointLocation, String locationID)
   {
      this.setCoordinates(accessPointLocation);
      this.setLocationID(locationID);
      incrementFrequencyCount();
   }//AccessPoint

   public int getNumberOfRouters()
   {
      return this.mRouterSignalLevelDifferenceMap.size();
   }

   public String getRouterSSIDAtIndex(int index)
   {
      if (index < this.mRouterSignalLevelDifferenceMap.size())
      {
         return (String) this.mRouterSignalLevelDifferenceMap.keySet().toArray()[index];
      }//if
      else
      {
         return null;
      }
   }//getRouterSSIDAtIndex

   public void incrementFrequencyCount()
   {
      ++mFrequencyCount;
   }//incrementFrequencyCount

   public int getFrequencyCount()
   {
      return this.mFrequencyCount;
   }//getFrequencyCount

   public HashMap<String, Integer> getSignalLeveDiffMap()
   {
      return mRouterSignalLevelDifferenceMap;
   }//getSignalLevelList

   public void addSignalLevelDiff(String routerSSID, int signalLevelDiff)
   {
      this.mRouterSignalLevelDifferenceMap.put(routerSSID, signalLevelDiff);
   }//addSignalLevel

   public Point getCoordinates()
   {
      return mLocationCoordinates;
   }

   public void setCoordinates(Point coordinates)
   {
      this.mLocationCoordinates = coordinates;
   }

   public String getLocationID()
   {
      return mLocationID;
   }

   public void setLocationID(String locationID)
   {
      mLocationID = locationID;
   }//setLocationID

   public double getAverageSignalDiff()
   {
      double total_value = 0;
      for (Entry<String, Integer> test : mRouterSignalLevelDifferenceMap.entrySet())
      {
         total_value += test.getValue();
      }//for
      return total_value / (double) mRouterSignalLevelDifferenceMap.entrySet().size();
   }

   @Override
   public int compareTo(FingerprintingPoint other)
   {
      // compareTo should return < 0 if this is supposed to be
      // less than other, > 0 if this is supposed to be greater than
      // other and 0 if they are supposed to be equal
      int result = ((Integer) this.mFrequencyCount).compareTo((Integer) other.mFrequencyCount);
      return result;
   }//compareTo
}//AccessPoint
