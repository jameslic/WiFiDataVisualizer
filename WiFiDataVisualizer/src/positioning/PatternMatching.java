/*
 * This class attempts to implement the pattern matching algorithm that will be used for the Wifi Data Visualizer
 */
package positioning;

import database.SQLLiteConnection;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import wifidatavisualizer.Constants;

/**
 * Class with static methods for manipulating wifi data using pattern matching
 * techniques
 *
 * @author James Licata
 */
public class PatternMatching
{
   /**
    * Returns a relative point coordinate for the pattern matching algorithm
    * estimation
    *
    * @param accessPointList  the access point observation record list
    * @param trainingDataBase the data base containing the training data points
    * @return
    */
   public static Point patternMatching(ArrayList<AccessPointObservationRecord> accessPointList, SQLLiteConnection trainingDataBase, Point lastPointApproximation)
   {
      Collection<FingerprintingPoint> collection = trainingDataBase.getLikeliestPoints(accessPointList).values();
      ArrayList<FingerprintingPoint> fingerprinting_point_list = new ArrayList<>(collection);
      Collections.sort(fingerprinting_point_list);
      int highest_frequency = fingerprinting_point_list.get(fingerprinting_point_list.size() - 1).getFrequencyCount();
      while (fingerprinting_point_list.get(0).getFrequencyCount() < highest_frequency)
      {
         //Remove all of the lower frequencies
         fingerprinting_point_list.remove(0);
      }//for

      return chooseBestPoint(fingerprinting_point_list, lastPointApproximation);
   }//patternMatching

   /**
    * Chooses the best possible point based on the average signal differential
    * and distance if applicable
    *
    * @param fingerprintList        the possible fingerprinting points to choose
    *                               from
    * @param lastPointApproximation the last point approximation to use as a
    *                               final decider when
    * @return the final point approximation
    */
   public static Point chooseBestPoint(ArrayList<FingerprintingPoint> fingerprintList, Point lastPointApproximation)
   {
      int best_point_index = 0;
      double average_signal_difference = fingerprintList.get(0).getAverageSignalDiff();
      int index_counter = 0;
      for (FingerprintingPoint f_point : fingerprintList)
      {
         if (f_point.getAverageSignalDiff() < average_signal_difference)
         {
            best_point_index = index_counter;
            //Update the average signal difference to get the closest match
            average_signal_difference = f_point.getAverageSignalDiff();
         }//if
         else if (f_point.getAverageSignalDiff() == average_signal_difference)
         {
            double new_point_distance = lastPointApproximation.distance(f_point.getCoordinates());
            double old_point_distance = lastPointApproximation.distance(fingerprintList.get(best_point_index).getCoordinates());
            if (new_point_distance < old_point_distance)
            {
               best_point_index = index_counter;
               //Update the average signal difference to get the closest match
               average_signal_difference = f_point.getAverageSignalDiff();
            }//if
         }//else if
         else
         {
            //Do nothing
         }//else
         //Increment the counter
         ++index_counter;
      }//for
      return fingerprintList.get(best_point_index).getCoordinates();
   }//chooseBestPoint
}//Fingerprinting
