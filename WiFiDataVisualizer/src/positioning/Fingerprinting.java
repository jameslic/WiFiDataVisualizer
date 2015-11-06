/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package positioning;

import database.SQLLiteConnection;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author James Licata
 */
public class Fingerprinting
{
   public static Point fingerprint(ArrayList<AccessPoint> accessPointList, SQLLiteConnection trainingDataBase)
   {
      Point return_point = new Point(0, 0);
      Collection<FingerprintingPoint> collection = trainingDataBase.getLikeliestPoints(accessPointList).values();
      ArrayList<FingerprintingPoint> fingerprinting_point_list = new ArrayList<>(collection);
      Collections.sort(fingerprinting_point_list);
      int highest_frequency = fingerprinting_point_list.get(fingerprinting_point_list.size() - 1).getFrequencyCount();
      while (fingerprinting_point_list.get(0).getFrequencyCount() < highest_frequency)
      {
         //Remove all of the lower frequencies
         fingerprinting_point_list.remove(0);
      }//for
      Logger.getLogger(SQLLiteConnection.class.getName()).log(Level.INFO, "Total num of points to choose from: " + fingerprinting_point_list.size());
      return chooseBestPoint(fingerprinting_point_list);
   }//fingerprint

   public static Point chooseBestPoint(ArrayList<FingerprintingPoint> fingerprintList)
   {
      int best_point_index = 0;
      double initial_average_diff = fingerprintList.get(0).getAverageSignalDiff();
      int index_counter = 0;
      for (FingerprintingPoint f_point : fingerprintList)
      {
         if (f_point.getAverageSignalDiff() < initial_average_diff)
         {
            best_point_index = index_counter;
         }//if
         ++index_counter;
      }//for
      return fingerprintList.get(best_point_index).getCoordinates();
   }//chooseBestPoint
}//Fingerprinting
