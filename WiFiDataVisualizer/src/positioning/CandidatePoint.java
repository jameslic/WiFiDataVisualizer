/*
 * Class the contains an X, Y point that is a candidate for positioning estimation.
 * Also contains the frequency with which the candiate point appears as a candidate and
 * the signal differences from observed RSS from the known Access Points relative to the
 * actual observec RSS values
 */
package positioning;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Container class representing an X, Y point that is a candidate
 *
 * @author James Licata
 */
public class CandidatePoint
        implements Comparable<CandidatePoint>
{
   //The location ID for the candidate point
   String mLocationID;

   //The router signal level difference map
   HashMap<String, Integer> mRouterSignalLevelDifferenceMap = new HashMap<>();

   //The frequency count for the likelihood of this candidate point
   int mFrequencyCount = 0;

   //The 2D location coordinates
   Point mLocationCoordinates = new Point(0, 0);

   /**
    * Main constructor
    *
    * @param candidatePointLocation the candidate point location
    * @param locationID             the string location id
    */
   public CandidatePoint(Point candidatePointLocation, String locationID)
   {
      this.setCoordinates(candidatePointLocation);
      this.setLocationID(locationID);
      incrementFrequencyCount();
   }//AccessPoint

   /**
    * Returns the number of routers that have associated signal differentials
    * recorded in this container
    *
    * @return total number of routers (access points) with recorded signal
    *         differentials
    */
   public int getNumberOfRouters()
   {
      return this.mRouterSignalLevelDifferenceMap.size();
   }//getNumberOfRouters

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

   /**
    * Adds a signal level differential (difference between training data point
    * RSS and observed RSS) for
    * the indicated SSID
    *
    * @param routerSSID      the router SSID associated with the RSS
    *                        differential
    * @param signalLevelDiff the signal level differential (Training - Observed)
    */
   public void addSignalLevelDiff(String routerSSID, int signalLevelDiff)
   {
      this.mRouterSignalLevelDifferenceMap.put(routerSSID, signalLevelDiff);
   }//addSignalLevel

   /**
    * Returns the 2D point representing the Candidates coordinates
    *
    * @return the Candidate point's 2D coordinates
    */
   public Point getCoordinates()
   {
      return mLocationCoordinates;
   }//getCoordinates

   /**
    * Sets the 2D point coordinates
    *
    * @param coordinates
    */
   public void setCoordinates(Point coordinates)
   {
      this.mLocationCoordinates = coordinates;
   }//setCoordinates

   /**
    * Returns the string representing the candidate point's location identifier
    *
    * @return the Location ID string
    */
   public String getLocationID()
   {
      return mLocationID;
   }//getLocationID

   /**
    * Sets the location ID of the candidate point (ex. May be the office name or
    * identifier)
    *
    * @param locationID
    */
   public void setLocationID(String locationID)
   {
      mLocationID = locationID;
   }//setLocationID

   /**
    * Adds all of the differentials for the RSS of senses access points and
    * takes the average
    *
    * @return The average signal differential
    */
   public double getAverageSignalDiff()
   {
      double total_value = 0;
      for (Entry<String, Integer> test : mRouterSignalLevelDifferenceMap.entrySet())
      {
         total_value += test.getValue();
      }//for
      return total_value / (double) mRouterSignalLevelDifferenceMap.entrySet().size();
   }//getAverageSignalDiff

   /**
    * Overridden comparison function to compare points against each other for
    * frequency
    *
    * @param other the other Candidate point to compare
    * @return
    */
   @Override
   public int compareTo(CandidatePoint other)
   {
      // compareTo should return < 0 if this is supposed to be
      // less than other, > 0 if this is supposed to be greater than
      // other and 0 if they are supposed to be equal
      int result = ((Integer) this.mFrequencyCount).compareTo((Integer) other.mFrequencyCount);
      return result;
   }//compareTo
}//AccessPoint
