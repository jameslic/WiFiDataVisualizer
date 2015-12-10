/*
 * Reads in Wifi data from the Android applications CSV output format
 */
package wifidatavisualizer;

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

/**
 * Class to read in wifi data from recorded CSV files
 *
 * @author James Licata
 */
public class WifiDataReader
{
   /**
    * The CSV file map
    */
   HashMap<String, Reader> mWifiDataCSVFileMap;

   /**
    * Default constructor, creates the CSV file map
    */
   public WifiDataReader()
   {
      mWifiDataCSVFileMap = new HashMap<>();
   }//WifiDataReader

   /**
    * Given a list of CSV files, it creates file readers for them
    *
    * @param csvFileList the input csv data file list
    */
   public void openCSVFiles(HashMap<String, String> csvFileList)
   {
      Reader csv_reader = null;
      try
      {
         for (int i = 0; i < csvFileList.size(); ++i)
         {
            String ssid_id = Constants.ROUTER_PREFIX_SSID + i;
            csv_reader = new FileReader(csvFileList.get(ssid_id));
            mWifiDataCSVFileMap.put(ssid_id, csv_reader);
         }//for
      }//try
      catch (FileNotFoundException ex)
      {
         Logger.getLogger(WifiDataReader.class.getName()).log(Level.SEVERE, null, ex);
      }//catch
   }//openCSVFiles

   /**
    * Closes all open CSV file readers in the map
    */
   public void closeFiles()
   {
      for (int i = 0; i < 4; ++i)
      {
         String ssid_id = Constants.ROUTER_PREFIX_SSID + i;
         try
         {
            mWifiDataCSVFileMap.get(ssid_id).close();
         }//try
         catch (IOException ex)
         {
            Logger.getLogger(WifiDataReader.class.getName()).log(Level.SEVERE, null, ex);
         }//catch
      }//for
      mWifiDataCSVFileMap.clear();
   }//closeFiles

   /**
    * Parses the CSV data for the indicated router given its SSID
    *
    * @param ssid the SSID of the router data to parse
    * @return iterable collection of records parsed by the CSV file reader
    */
   public Iterable<CSVRecord> parseRecords(String ssid)
   {
      Reader csv_reader = mWifiDataCSVFileMap.get(ssid);
      Iterable<CSVRecord> records = null;
      if (csv_reader != null)
      {
         try
         {
            records = CSVFormat.EXCEL.withHeader().parse(csv_reader);
         }
         catch (IOException ex)
         {
            Logger.getLogger(WifiDataReader.class.getName()).log(Level.SEVERE, null, ex);
         }//catch

      }//if
      return records;
   }//parseRecords

   /**
    * Given an iterable collection of records and a target timestamp, looks to
    * return the closest timestamped RSS value
    *
    * @param csvRecordCollection           collection of csv records
    * @param relativeTimestampMilliseconds relative timestamp to search for the
    *                                      closest one
    * @return the closest time stamped RSS value
    */
   public int getClosestTimestampRecordRSS(Iterable<CSVRecord> csvRecordCollection, int relativeTimestampMilliseconds)
   {
      int returned_rss = 105;
      Iterator<CSVRecord> csv_record_iterator = csvRecordCollection.iterator();
      while (csv_record_iterator.hasNext())
      {
         CSVRecord record = csv_record_iterator.next();
         String test = record.get(Constants.CSV_FILE_RECORD_TIMESTAMP_COLUMN);
         if (abs(Integer.parseInt(record.get(Constants.CSV_FILE_RECORD_TIMESTAMP_COLUMN)) - relativeTimestampMilliseconds) < 500)
         {
            return Integer.parseInt(record.get(Constants.CSV_FILE_RECORD_RSS_COLUMN));
         }//if
      }//for

      return returned_rss;
   }//getClosestTimestampRecord

   /**
    * Returns the first recorded timestamp in milliseconds given the csv records
    *
    * @param csvRecord a collection of csv records
    * @return the first timestamp in milliseconds
    */
   public int getFirstRecordedTimestampMilliseconds(Iterable<CSVRecord> csvRecord)
   {
      int timestamp_milliseconds = Integer.parseInt(csvRecord.iterator().next().get(Constants.CSV_FILE_RECORD_TIMESTAMP_COLUMN));
      return timestamp_milliseconds;
   }//getFirstRecordedTimestamp

   /**
    * Returns the last recorded timestamp in milliseconds given the csv records
    *
    * @param csvRecordCollection a collection of csv records
    * @return the last timestamp in milliseconds
    */
   public int getLastRecordedTimestampMilliseconds(Iterable<CSVRecord> csvRecordCollection)
   {
      Iterator<CSVRecord> csv_iterator = csvRecordCollection.iterator();
      CSVRecord csv_record = null;
      while (csv_iterator.hasNext())
      {
         csv_record = csv_iterator.next();
      }//while
      int timestamp_milliseconds = Integer.parseInt(csv_record.get(Constants.CSV_FILE_RECORD_TIMESTAMP_COLUMN));
      return timestamp_milliseconds;
   }//getFirstRecordedTimestamp

   /**
    * Returns a sorted tree map version of the input csv record collection
    *
    * @param csvRecordCollection
    * @return
    */
   public TreeMap getSortedTreeMap(Iterable<CSVRecord> csvRecordCollection)
   {
      TreeMap tree_map = new TreeMap();
      Iterator<CSVRecord> csv_iterator = csvRecordCollection.iterator();
      while (csv_iterator.hasNext())
      {
         CSVRecord csv_record = csv_iterator.next();
         int timestamp = Integer.parseInt(csv_record.get(Constants.CSV_FILE_RECORD_TIMESTAMP_COLUMN));
         int rss = Integer.parseInt(csv_record.get(Constants.CSV_FILE_RECORD_RSS_COLUMN));
         tree_map.put(timestamp, rss);
      }
      return tree_map;
   }//getSortedTreeMap

}//WifiDataReader
