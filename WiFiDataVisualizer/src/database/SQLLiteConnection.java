/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import java.awt.Point;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

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

}//SQLLiteConnection
