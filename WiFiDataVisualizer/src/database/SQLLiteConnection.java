/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.awt.Point;
import static java.lang.Math.abs;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import positioning.AccessPoint;
import positioning.FingerprintingPoint;

/**
 *
 * @author James Licata
 */
public class SQLLiteConnection
{
   Connection mDatabaseConnection = null;
   boolean mDatabaseConnected = false;
   String mDatabaseName = "";

   public SQLLiteConnection()
   {
      try
      {
         DriverManager.registerDriver(new org.sqlite.JDBC());
      }//try
      catch (SQLException ex)
      {
         Logger.getLogger(SQLLiteConnection.class.getName()).log(Level.SEVERE, null, ex);
      }//catch
   }//SQLLiteConnection

   /**
    *
    * @param databaseUrl
    */
   public boolean connect(String databaseUrl, String databaseName)
   {
      if (mDatabaseConnected == false)
      {
         try
         {
            mDatabaseConnection = DriverManager.getConnection(databaseUrl);
            if (mDatabaseConnection != null)
            {
               mDatabaseConnected = true;
               mDatabaseName = databaseName;
               Logger.getLogger(SQLLiteConnection.class.getName()).log(Level.INFO, "Database connected");

            }//if
         }//try
         catch (SQLException ex)
         {
            Logger.getLogger(SQLLiteConnection.class.getName()).log(Level.SEVERE, null, ex);
         }
      }//if
      return mDatabaseConnected;
   }//connect

   public boolean isDatabaseConnected()
   {
      return mDatabaseConnected;
   }

   public void closeDatabase()
   {
      try
      {
         mDatabaseConnection.close();
         mDatabaseConnected = false;
      }//try
      catch (SQLException ex)
      {
         Logger.getLogger(SQLLiteConnection.class.getName()).log(Level.SEVERE, null, ex);
      }//catch
   }//closeDatabase

   public ArrayList<Point> loadTrainingPointLocations()
   {
      ArrayList<Point> training_point_location_list = new ArrayList<>();
      if (isDatabaseConnected())
      {
         Statement stmt = null;
         String query =
                 "SELECT x, y FROM TrainingPointLocations";
         //String query2 = "SELECT * FROM *;";

         try
         {
            stmt = mDatabaseConnection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next())
            {
               int x = rs.getInt("x");
               int y = rs.getInt("y");
               Point training_point = new Point(x, y);
               System.out.println("Read in point: " + training_point.toString());
               training_point_location_list.add(training_point);
            }//while
         }//try//try
         catch (SQLException ex)
         {
            Logger.getLogger(SQLLiteConnection.class.getName()).log(Level.SEVERE, null, ex);
         }//catch
         finally
         {
            if (stmt != null)
            {
               try
               {
                  stmt.close();
               }
               catch (SQLException ex)
               {
                  Logger.getLogger(SQLLiteConnection.class.getName()).log(Level.SEVERE, null, ex);
               }//catch
            }//if
         }//finally
      }//if (database is connected)

      return training_point_location_list;
   }//loadTrainingPointLocations

   public ArrayList<Point> loadRouterPointLocations()
   {
      ArrayList<Point> router_point_location_list = new ArrayList<>();
      if (isDatabaseConnected())
      {
         for (int i = 0; i < 4; ++i)
         {
            Statement stmt = null;
            String query =
                    "SELECT SSID, x, y FROM APLocations WHERE SSID='CiscoLinksysE120" + i + "'";
            //String query2 = "SELECT * FROM *;";

            try
            {
               stmt = mDatabaseConnection.createStatement();
               ResultSet query_result_set = stmt.executeQuery(query);
               while (query_result_set.next())
               {
                  int x = query_result_set.getInt("x");
                  int y = query_result_set.getInt("y");
                  Point router_point = new Point(x, y);
                  System.out.println("Read in router point: " + router_point.toString());
                  router_point_location_list.add(router_point);
               }//while
            }//try//try
            catch (SQLException ex)
            {
               Logger.getLogger(SQLLiteConnection.class.getName()).log(Level.SEVERE, null, ex);
            }//catch
            finally
            {
               if (stmt != null)
               {
                  try
                  {
                     stmt.close();
                  }
                  catch (SQLException ex)
                  {
                     Logger.getLogger(SQLLiteConnection.class.getName()).log(Level.SEVERE, null, ex);
                  }//catch
               }//if
            }//finally
         }//for
      }//if (database is connected)

      return router_point_location_list;
   }//loadRouterPointLocations

   public HashMap<String, FingerprintingPoint> getLikeliestPoints(ArrayList<AccessPoint> accessPointList)
   {
      ArrayList<String> possible_office_list = new ArrayList<>();
      ArrayList<Integer> possible_office_signal_list_diff = new ArrayList<>();
      ArrayList<String> originating_ssid = new ArrayList<>();
      if (isDatabaseConnected())
      {
         for (AccessPoint access_point : accessPointList)
         {
            int rss_lower_bound = abs(access_point.getSignalLevel() - 2);
            int rss_upper_bound = abs(access_point.getSignalLevel() + 2);
            Statement stmt = null;
            String query =
                    "SELECT * FROM " + access_point.getSSID()
                    + " WHERE RSS BETWEEN " + rss_upper_bound
                    + " AND " + rss_lower_bound;
            try
            {
               stmt = mDatabaseConnection.createStatement();
               ResultSet query_result_set = stmt.executeQuery(query);
               while (query_result_set.next())
               {
                  int rss = query_result_set.getInt("RSS");
                  String office_id = query_result_set.getString("Office");
                  possible_office_list.add(office_id);
                  //Add a factor if the signal level is weak to help prioritize later
                  //Find out how weak the signal is and add a differential factor to it
                  int signal_weakness_factor = abs(access_point.getSignalLevel()) - 55;
                  if (signal_weakness_factor > 0)
                  {
                     int signal_weakness_adder = signal_weakness_factor / 5;
                     possible_office_signal_list_diff.add(abs(abs(access_point.getSignalLevel()) - rss) + signal_weakness_adder);
                  }//if
                  else
                  {
                     possible_office_signal_list_diff.add(abs(abs(access_point.getSignalLevel()) - rss));
                  }
                  originating_ssid.add(access_point.getSSID());
               }//while
            }//try//try
            catch (SQLException ex)
            {
               Logger.getLogger(SQLLiteConnection.class.getName()).log(Level.SEVERE, null, ex);
            }//catch
            finally
            {
               if (stmt != null)
               {
                  try
                  {
                     stmt.close();
                  }
                  catch (SQLException ex)
                  {
                     Logger.getLogger(SQLLiteConnection.class.getName()).log(Level.SEVERE, null, ex);
                  }//catch
               }//if
            }//finally
         }//for
      }//if (database is connected)
      return getPointsFromOfficeList(possible_office_list, possible_office_signal_list_diff, originating_ssid);
   }//getLikeliestPoints

   private HashMap<String, FingerprintingPoint> getPointsFromOfficeList(ArrayList<String> officeIdList,
           ArrayList<Integer> officeSignalLevelDiff, ArrayList<String> originatingSSID)
   {
      HashMap<String, FingerprintingPoint> resultant_point_list = new HashMap<>();
      if (isDatabaseConnected())
      {
         int signal_diff_list_index = 0;
         for (String office_id_string : officeIdList)
         {
            Statement stmt = null;
            String query = "SELECT x, y FROM TrainingPointLocations WHERE Office='" + office_id_string + "'";
            try
            {
               stmt = mDatabaseConnection.createStatement();
               ResultSet query_result_set = stmt.executeQuery(query);
               while (query_result_set.next())
               {
                  if (resultant_point_list.get(office_id_string) != null)
                  {
                     resultant_point_list.get(office_id_string).incrementFrequencyCount();
                     System.out.println("Read in Office Point " + resultant_point_list.get(office_id_string).getFrequencyCount()
                             + " times! OfficeID: " + resultant_point_list.get(office_id_string).getLocationID()
                     );
                     resultant_point_list.get(office_id_string).addSignalLevelDiff(originatingSSID.get(signal_diff_list_index),
                                                                                   officeSignalLevelDiff.get(signal_diff_list_index));
                  }//if
                  else
                  {
                     int x = query_result_set.getInt("x");
                     int y = query_result_set.getInt("y");
                     Point office_point = new Point(x, y);
                     FingerprintingPoint fingerprint_point = new FingerprintingPoint(office_point, office_id_string);
                     fingerprint_point.addSignalLevelDiff(originatingSSID.get(signal_diff_list_index),
                                                          officeSignalLevelDiff.get(signal_diff_list_index));
                     resultant_point_list.put(office_id_string, fingerprint_point);
                  }//else
               }//while
               ++signal_diff_list_index;
            }//for
            catch (SQLException ex)
            {
               Logger.getLogger(SQLLiteConnection.class.getName()).log(Level.SEVERE, null, ex);
            }//catch
            finally
            {
               if (stmt != null)
               {
                  try
                  {
                     stmt.close();
                  }
                  catch (SQLException ex)
                  {
                     Logger.getLogger(SQLLiteConnection.class.getName()).log(Level.SEVERE, null, ex);
                  }//catch
               }//if
            }//finally
         }//for
      }//if (database is connected)
      return resultant_point_list;
   }//getPointFromOfficeList

}//SQLLiteConnection
