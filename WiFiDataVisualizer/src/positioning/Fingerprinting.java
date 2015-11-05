/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package positioning;

import database.SQLLiteConnection;
import java.awt.Point;
import java.util.ArrayList;

/**
 *
 * @author James Licata
 */
public class Fingerprinting
{
   public static Point fingerprint(ArrayList<AccessPoint> accessPointList, SQLLiteConnection trainingDataBase)
   {
      Point return_point = new Point(0, 0);
      trainingDataBase.getLikeliestPoints(accessPointList);
      return return_point;
   }//fingerprint
}//Fingerprinting
