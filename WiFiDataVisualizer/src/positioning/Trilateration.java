/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package positioning;

import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import javafx.scene.shape.Circle;

/**
 *
 * @author James Licata
 */
public class Trilateration
{
   public static Point findCenter(ArrayList<Ellipse2D> apErrorEllipses)
   {
      double top = 0;
      double bot = 0;
      for (int i = 0; i < 3; i++)
      {
         Ellipse2D c = apErrorEllipses.get(i);
         double c_radius = c.getHeight() / 2;
         double c2_radius = 0;
         double c3_radius = 0;
         Ellipse2D c2, c3;
         if (i == 0)
         {
            c2 = apErrorEllipses.get(1);
            c3 = apErrorEllipses.get(2);
         }//if
         else if (i == 1)
         {
            c2 = apErrorEllipses.get(0);
            c3 = apErrorEllipses.get(2);
         }//else if
         else
         {
            c2 = apErrorEllipses.get(0);
            c3 = apErrorEllipses.get(1);
         }//else

         c2_radius = c2.getHeight() / 2;
         c3_radius = c3.getHeight() / 2;

         double d = c2.getCenterX() - c3.getCenterX();

         double v1 = (c.getCenterX() * c.getCenterX() + c.getCenterY() * c.getCenterY()) - (c_radius * c_radius);
         top += d * v1;

         double v2 = c.getCenterY() * d;
         bot += v2;
      }//for

      double y = top / (2 * bot);
      Ellipse2D c1 = apErrorEllipses.get(0);
      Ellipse2D c2 = apErrorEllipses.get(1);
      double c1_radius = c1.getHeight() / 2;
      double c2_radius = c2.getHeight() / 2;
      top = c2_radius * c2_radius + c1.getCenterX() * c1.getCenterX() + c1.getCenterY() * c1.getCenterY() - c1_radius * c1_radius - c2.getCenterX() * c2.getCenterX() - c2.getCenterY() * c2.getCenterY() - 2 * (c1.getCenterY() - c2.getCenterY()) * y;
      bot = c1.getCenterX() - c2.getCenterX();
      double x = top / (2 * bot);

      Point resultant_point = new Point(0, 0);
      resultant_point.setLocation(x, y);
      return resultant_point;
   }//findCenter

   public static Point findCenterPoint(ArrayList<AccessPointObservationRecord> accessPointList)
   {
      double top = 0;
      double bot = 0;
      for (int i = 0; i < 3; i++)
      {
         AccessPointObservationRecord c = accessPointList.get(i);
         double c_radius = c.getDistancePixels();
         double c2_radius = 0;
         double c3_radius = 0;
         AccessPointObservationRecord c2, c3;
         if (i == 0)
         {
            c2 = accessPointList.get(1);
            c3 = accessPointList.get(2);
         }//if
         else if (i == 1)
         {
            c2 = accessPointList.get(0);
            c3 = accessPointList.get(2);
         }//else if
         else
         {
            c2 = accessPointList.get(0);
            c3 = accessPointList.get(1);
         }//else

         c2_radius = c2.getDistancePixels();
         c3_radius = c3.getDistancePixels();

         double d = c2.getCoordinates().getX() - c3.getCoordinates().getX();

         double v1 = (c.getCoordinates().getX() * c.getCoordinates().getX() + c.getCoordinates().getY() * c.getCoordinates().getY()) - (c_radius * c_radius);
         top += d * v1;

         double v2 = c.getCoordinates().getY() * d;
         bot += v2;
      }//for

      double y = top / (2 * bot);
      AccessPointObservationRecord c1 = accessPointList.get(0);
      AccessPointObservationRecord c2 = accessPointList.get(1);
      double c1_radius = c1.getDistancePixels();
      double c2_radius = c2.getDistancePixels();
      top = c2_radius * c2_radius + c1.getCoordinates().getX() * c1.getCoordinates().getX() + c1.getCoordinates().getY() * c1.getCoordinates().getY() - c1_radius * c1_radius - c2.getCoordinates().getX() * c2.getCoordinates().getX() - c2.getCoordinates().getY() * c2.getCoordinates().getY() - 2 * (c1.getCoordinates().getY() - c2.getCoordinates().getY()) * y;
      bot = c1.getCoordinates().getX() - c2.getCoordinates().getX();
      double x = top / (2 * bot);

      Point resultant_point = new Point(0, 0);
      resultant_point.setLocation(x, y);
      return resultant_point;
   }//findCenter
}//Trilateration
