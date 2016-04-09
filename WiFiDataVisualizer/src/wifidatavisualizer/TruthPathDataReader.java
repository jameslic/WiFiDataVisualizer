/*
 * Reads in Wifi data from the Android applications CSV output format
 */
package wifidatavisualizer;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import java.util.ArrayList;

/**
 * Class to read in wifi data from recorded CSV files
 *
 * @author James Licata
 */
public class TruthPathDataReader
{
   /**
    * The CSV file map
    */
   Reader mTruthPathFileReader = null;

   /**
    * Default constructor, creates the CSV file map
    */
   public TruthPathDataReader()
   {
   }//WifiDataReader

   /**
    * Given a list of CSV files, it creates file readers for them
    *
    * @param csvFileList the input csv data file list
    */
   public void openCSVFile(String csvFile)
   {
      try
      {
         mTruthPathFileReader = new FileReader(csvFile);
      }//try
      catch (FileNotFoundException ex)
      {
         Logger.getLogger(TruthPathDataReader.class.getName()).log(Level.SEVERE, null, ex);
      }//catch//catch
   }//openCSVFiles

   /**
    * Closes open CSV file
    */
   public void closeFile()
   {
      for (int i = 0; i < 4; ++i)
      {
         String ssid_id = Constants.ROUTER_PREFIX_SSID + i;
         try
         {
            mTruthPathFileReader.close();
         }//try
         catch (IOException ex)
         {
            Logger.getLogger(TruthPathDataReader.class.getName()).log(Level.SEVERE, null, ex);
         }//catch//catch
      }//for
   }//closeFiles

   /**
    * Parses the CSV data for the indicated router given its SSID
    *
    * @param ssid the SSID of the router data to parse
    * @return iterable collection of records parsed by the CSV file reader
    */
   public ArrayList<Point> parseDataPoints()
   {
      Reader csv_reader = mTruthPathFileReader;
      Iterable<CSVRecord> data_points = null;
      ArrayList<Point> truth_data_points = new ArrayList<>();
      if (csv_reader != null)
      {
         try
         {
            data_points = CSVFormat.EXCEL.withHeader().parse(csv_reader);
         }
         catch (IOException ex)
         {
            Logger.getLogger(TruthPathDataReader.class.getName()).log(Level.SEVERE, null, ex);
         }//catch//catch

      }//if
      for (CSVRecord csv_record : data_points)
      {
         truth_data_points.add(new Point(Integer.valueOf(csv_record.get("X")), Integer.valueOf(csv_record.get("Y"))));
      }
      return truth_data_points;
   }//parseRecords

}//WifiDataReader
