/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package positioning;

import java.awt.Point;
import java.util.ArrayList;

/**
 *
 * @author James Licata
 */
public class Triangulation
{
   public static Point triangulate(ArrayList<AccessPointObservationRecord> accessPointList)
   {
      Point resultant_point = new Point(0, 0);
      double A, B, C, D, E, F, DetX, DetY, Det;
      if (accessPointList.size() == 3)
      {
         AccessPointObservationRecord p1 = accessPointList.get(0);
         AccessPointObservationRecord p2 = accessPointList.get(1);
         AccessPointObservationRecord p3 = accessPointList.get(2);
         A = -2 * p1.getCoordinates().getX() + 2 * p2.getCoordinates().getX();
         B = -2 * p1.getCoordinates().getY() + 2 * p1.getCoordinates().getY();
         C = -2 * p2.getCoordinates().getX() + 2 * p3.getCoordinates().getX();
         D = -2 * p2.getCoordinates().getY() + 2 * p3.getCoordinates().getY();
         E = Math.pow(p1.getDistancePixels(), 2) - Math.pow(p2.getDistancePixels(), 2) - Math.pow(p1.getCoordinates().getX(), 2)
                 + Math.pow(p2.getCoordinates().getX(), 2) - Math.pow(p1.getCoordinates().getY(), 2) + Math.pow(p2.getCoordinates().getY(), 2);
         F = Math.pow(p2.getDistancePixels(), 2) - Math.pow(p3.getDistancePixels(), 2) - Math.pow(p2.getCoordinates().getX(), 2)
                 + Math.pow(p3.getCoordinates().getX(), 2) - Math.pow(p2.getCoordinates().getY(), 2) + Math.pow(p3.getCoordinates().getY(), 2);
         // Using Cramers Rule
         Det = A * D - B * C;
         DetX = E * D - B * F;
         DetY = A * F - E * C;

         resultant_point.setLocation(DetX / Det, DetY / Det);
      }//if
      return resultant_point;
   }//triangulate
}//Triangulation
