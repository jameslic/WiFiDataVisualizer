/*
 * This class attempts to implement the pattern matching algorithm that will be used for the Wifi Data Visualizer
 */
package positioning;

import database.SQLLiteConnection;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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
    * @param accessPointList        the access point observation record list
    * @param trainingDataBase       the data base containing the training data
    *                               points
    * @param lastPointApproximation the last point approximation from previous
    *                               estimation
    * @return
    */
   public static Point patternMatching(ArrayList<AccessPointObservationRecord> accessPointList, SQLLiteConnection trainingDataBase, Point lastPointApproximation)
   {
      Collection<CandidatePoint> collection = trainingDataBase.getLikeliestPoints(accessPointList).values();
      ArrayList<CandidatePoint> candidate_point_list = new ArrayList<>(collection);
      Collections.sort(candidate_point_list);
      int highest_frequency = candidate_point_list.get(candidate_point_list.size() - 1).getFrequencyCount();
      while (candidate_point_list.get(0).getFrequencyCount() < highest_frequency)
      {
         //Remove all of the lower frequencies
         candidate_point_list.remove(0);
      }//for

      return chooseBestPoint(candidate_point_list, lastPointApproximation);
   }//patternMatching

   /**
    * Chooses the best possible point based on the average signal differential
    * and distance if applicable
    *
    * @param candidatePointList        the possible fingerprinting points to choose
    *                               from
    * @param lastPointApproximation the last point approximation to use as a
    *                               final decider when
    * @return the final point approximation
    */
   public static Point chooseBestPoint(ArrayList<CandidatePoint> candidatePointList, Point lastPointApproximation)
   {
      int best_point_index = 0;
      double average_signal_difference = candidatePointList.get(0).getAverageSignalDiff();
      int index_counter = 0;
      for (CandidatePoint f_point : candidatePointList)
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
            double old_point_distance = lastPointApproximation.distance(candidatePointList.get(best_point_index).getCoordinates());
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
      return candidatePointList.get(best_point_index).getCoordinates();
   }//chooseBestPoint
}//Fingerprinting
