/*
 * This class attempts to implement the Weighted Centroid algorithm that will be used for the Wifi Data Visualizer
 */
package positioning;

import database.SQLLiteConnection;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import wifidatavisualizer.Constants;

/**
 * Class with static methods for manipulating wifi data using Weighted Centroid
 * techniques
 *
 * @author James Licata
 */
public class WeightedCentroid
{
   /**
    * Returns a relative point coordinate for the fingerprinting algorithm
    * estimation
    *
    * @param accessPointList  the access point observation record list
    * @param trainingDataBase the data base containing the training data points
    * @return
    */
   public static Point weightedCentroid(ArrayList<AccessPointObservationRecord> accessPointList, SQLLiteConnection trainingDataBase, Point lastPointApproximation)
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

      return getWeightedCentroidEstimation(candidate_point_list, lastPointApproximation);
   }//fingerprint

   /**
    * Returns the weighted coordinate X value given the input point and it's
    * relative weight
    *
    * @param inputPoint the candidate point to derive the weighted X coordinate
    *                   from
    * @return
    */
   public static double getWeightedCoordinateXValue(CandidatePoint inputPoint)
   {
      double centroid_x_value = 0;

      for (int i = 0; i < inputPoint.getNumberOfRouters(); ++i)
      {
         String ssid = inputPoint.getRouterSSIDAtIndex(i);
         if (ssid != null)
         {
            centroid_x_value += Constants.getRouterWeightMap().get(ssid) * inputPoint.getCoordinates().getX();
         }//if
      }
      return centroid_x_value;
   }//getWeightedCoordinateXValue

   /**
    * Returns the weighted coordinate Y value given the input point and it's
    * relative weight
    *
    * @param inputPoint the candidate point to derive the weighted Y coordinate
    *                   from
    * @return
    */
   public static double getWeightedCoordinateYValue(CandidatePoint inputPoint)
   {
      double centroid_y_value = 0;
      for (int i = 0; i < inputPoint.getNumberOfRouters(); ++i)
      {
         String ssid = inputPoint.getRouterSSIDAtIndex(i);
         if (ssid != null)
         {
            centroid_y_value += Constants.getRouterWeightMap().get(ssid) * inputPoint.getCoordinates().getY();
         }//if
      }
      return centroid_y_value;
   }//getWeightedCoordinateYValue

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
   public static CandidatePoint chooseBestPoint(ArrayList<CandidatePoint> candidatePointList, Point lastPointApproximation)
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
      return candidatePointList.get(best_point_index);
   }//chooseBestPoint

   /**
    * Chooses the best possible point based on the weighted centroid of
    * available points
    *
    * @param candidatePointList        the possible fingerprinting points to choose
    *                               from
    * @param lastPointApproximation the last point approximation to use as a
    *                               final decider when
    * @return the final point approximation
    */
   public static Point getWeightedCentroidEstimation(ArrayList<CandidatePoint> candidatePointList, Point lastPointApproximation)
   {
      ArrayList<CandidatePoint> point_list = new ArrayList<>(Constants.FINGERPRINTING_K_NEAREST_NEIGHBORS);
      CandidatePoint best_training_point = chooseBestPoint(candidatePointList, lastPointApproximation);
      /*while (point_list.size() != Constants.FINGERPRINTING_K_NEAREST_NEIGHBORS)
      {
         int best_point_index = getBestPointIndex(candidatePointList);
         point_list.add(candidatePointList.get(best_point_index));
         candidatePointList.remove(best_point_index);
      }//while
       */
      Point centroid_point = getWeightedCentroid(best_training_point);

      double distance_from_centroid = lastPointApproximation.distance(centroid_point);
      double distance_from_best_point = lastPointApproximation.distance(best_training_point.getCoordinates());
      if (distance_from_centroid - distance_from_best_point > Constants.FINGERPRINTING_K_NEAREST_NEIGHBORS_DISTANCE_TOLERANCE)
      {
         return best_training_point.getCoordinates();
      }//if
      else
      {
         return centroid_point;
      }//else
   }//chooseBestPoint

   /**
    * Returns the index for the best candidate point
    *
    * @param candidatePointList
    * @return
    */
   public static int getBestPointIndex(ArrayList<CandidatePoint> candidatePointList)
   {
      int best_point_index = 0;
      double average_signal_difference = candidatePointList.get(0).getAverageSignalDiff();
      int index_counter = 0;
      for (CandidatePoint candidate_point : candidatePointList)
      {
         if (candidate_point.getAverageSignalDiff() < average_signal_difference)
         {
            best_point_index = index_counter;
            //Update the average signal difference to get the closest match
            average_signal_difference = candidate_point.getAverageSignalDiff();
         }//if
         ++index_counter;
      }//for
      return best_point_index;
   }//getBestPointIndex

   /**
    *
    * @param dataPoint
    * @return
    */
   public static Point getWeightedCentroid(CandidatePoint dataPoint)
   {
      double centroid_x = getWeightedCoordinateXValue(dataPoint);
      double centroid_y = getWeightedCoordinateYValue(dataPoint);

      centroid_x /= dataPoint.getNumberOfRouters();
      centroid_y /= dataPoint.getNumberOfRouters();

      Point weighted_centroid_point = new Point(0, 0);
      weighted_centroid_point.setLocation(centroid_x, centroid_y);
      return weighted_centroid_point;
   }//getCentroid
}//Fingerprinting
