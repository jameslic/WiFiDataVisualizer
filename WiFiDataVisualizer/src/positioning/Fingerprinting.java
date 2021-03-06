/*
 * This class attempts to implement the fingerprinting algorithm that will be used for the Wifi Data Visualizer
 */
package positioning;

import database.SQLLiteConnection;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import wifidatavisualizer.Constants;

/**
 * Class with static methods for manipulating wifi data using fingerprinting
 * techniques
 *
 * @author James Licata
 */
public class Fingerprinting
{
   /**
    * Returns a relative point coordinate for the fingerprinting algorithm
    * estimation
    *
    * @param accessPointList  the access point observation record list
    * @param trainingDataBase the data base containing the training data points
    * @return
    */
   public static Point fingerprint(ArrayList<AccessPointObservationRecord> accessPointList, SQLLiteConnection trainingDataBase, Point lastPointApproximation)
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
      if (candidate_point_list.size() < Constants.FINGERPRINTING_K_NEAREST_NEIGHBORS)
      {
         return chooseBestPoint(candidate_point_list, lastPointApproximation);
      }//if
      else
      {
         return getPointEstimationFromKNearestNeighbors(candidate_point_list, lastPointApproximation);
      }//else
   }//fingerprint

   /**
    * Chooses the best possible point based on the average signal differential
    * and distance if applicable
    *
    * @param candidatePointList     the possible fingerprinting points to choose
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
      for (CandidatePoint candidate_point : candidatePointList)
      {
         if (candidate_point.getAverageSignalDiff() < average_signal_difference)
         {
            best_point_index = index_counter;
            //Update the average signal difference to get the closest match
            average_signal_difference = candidate_point.getAverageSignalDiff();
         }//if
         else if (candidate_point.getAverageSignalDiff() == average_signal_difference)
         {
            double new_point_distance = lastPointApproximation.distance(candidate_point.getCoordinates());
            double old_point_distance = lastPointApproximation.distance(candidatePointList.get(best_point_index).getCoordinates());
            if (new_point_distance < old_point_distance)
            {
               best_point_index = index_counter;
               //Update the average signal difference to get the closest match
               average_signal_difference = candidate_point.getAverageSignalDiff();
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

   /**
    * Chooses the best possible point based on the average signal differential
    * and distance if applicable
    *
    * @param candidatePointList     the possible fingerprinting points to choose
    *                               from
    * @param lastPointApproximation the last point approximation to use as a
    *                               final decider when
    * @return the final point approximation
    */
   public static Point getPointEstimationFromKNearestNeighbors(ArrayList<CandidatePoint> candidatePointList, Point lastPointApproximation)
   {
      ArrayList<Point> point_list = new ArrayList<Point>(Constants.FINGERPRINTING_K_NEAREST_NEIGHBORS);
      Point best_training_point = chooseBestPoint(candidatePointList, lastPointApproximation);
      while (point_list.size() != Constants.FINGERPRINTING_K_NEAREST_NEIGHBORS)
      {
         int best_point_index = getBestPointIndex(candidatePointList);
         point_list.add(candidatePointList.get(best_point_index).getCoordinates());
         candidatePointList.remove(best_point_index);
      }//while
      Point centroid_point = getCentroid(point_list);

      double distance_from_centroid = lastPointApproximation.distance(centroid_point);
      double distance_from_best_point = lastPointApproximation.distance(best_training_point);
      if (distance_from_centroid - distance_from_best_point > Constants.FINGERPRINTING_K_NEAREST_NEIGHBORS_DISTANCE_TOLERANCE)
      {
         return best_training_point;
      }//if
      else
      {
         return centroid_point;
      }//else
   }//chooseBestPoint

   /**
    * Returns the index of the best candidate point (lowest signal differential)
    *
    * @param candidatePointList the candidate point list
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
    * Returns the equal centroid given the point list
    *
    * @param pointList the input point list to derive the centroid point from
    * @return
    */
   public static Point getCentroid(ArrayList<Point> pointList)
   {
      int centroid_x = 0;
      int centroid_y = 0;
      for (Point data_point : pointList)
      {
         centroid_x += data_point.x;
         centroid_y += data_point.y;
      }//for

      return new Point(centroid_x / pointList.size(), centroid_y / pointList.size());
   }//getCentroid
}//Fingerprinting
