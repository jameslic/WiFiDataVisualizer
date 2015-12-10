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
      Collection<FingerprintingPoint> collection = trainingDataBase.getLikeliestPoints(accessPointList).values();
      ArrayList<FingerprintingPoint> fingerprinting_point_list = new ArrayList<>(collection);
      Collections.sort(fingerprinting_point_list);
      int highest_frequency = fingerprinting_point_list.get(fingerprinting_point_list.size() - 1).getFrequencyCount();
      while (fingerprinting_point_list.get(0).getFrequencyCount() < highest_frequency)
      {
         //Remove all of the lower frequencies
         fingerprinting_point_list.remove(0);
      }//for

      return getWeightedCentroidEstimation(fingerprinting_point_list, lastPointApproximation);
   }//fingerprint

   public static double getWeightedCoordinateXValue(FingerprintingPoint inputPoint)
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

   public static double getWeightedCoordinateYValue(FingerprintingPoint inputPoint)
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
    * @param fingerprintList        the possible fingerprinting points to choose
    *                               from
    * @param lastPointApproximation the last point approximation to use as a
    *                               final decider when
    * @return the final point approximation
    */
   public static FingerprintingPoint chooseBestPoint(ArrayList<FingerprintingPoint> fingerprintList, Point lastPointApproximation)
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
      return fingerprintList.get(best_point_index);
   }//chooseBestPoint

   /**
    * Chooses the best possible point based on the weighted centroid of
    * available points
    *
    * @param fingerprintList        the possible fingerprinting points to choose
    *                               from
    * @param lastPointApproximation the last point approximation to use as a
    *                               final decider when
    * @return the final point approximation
    */
   public static Point getWeightedCentroidEstimation(ArrayList<FingerprintingPoint> fingerprintList, Point lastPointApproximation)
   {
      ArrayList<FingerprintingPoint> point_list = new ArrayList<>(Constants.FINGERPRINTING_K_NEAREST_NEIGHBORS);
      FingerprintingPoint best_training_point = chooseBestPoint(fingerprintList, lastPointApproximation);
      /*while (point_list.size() != Constants.FINGERPRINTING_K_NEAREST_NEIGHBORS)
      {
         int best_point_index = getBestPointIndex(fingerprintList);
         point_list.add(fingerprintList.get(best_point_index));
         fingerprintList.remove(best_point_index);
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

   public static int getBestPointIndex(ArrayList<FingerprintingPoint> fingerprintList)
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
         ++index_counter;
      }//for
      return best_point_index;
   }//getBestPointIndex

   public static Point getWeightedCentroid(FingerprintingPoint dataPoint)
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
