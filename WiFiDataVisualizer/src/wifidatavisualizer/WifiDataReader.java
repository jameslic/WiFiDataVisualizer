/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wifidatavisualizer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author James Licata
 */
public class WifiDataReader
{
   HashMap<String, Reader> mWifiDataCSVFileMap;
   ArrayList<Reader> mWifiDataCSVFilesList;

   public WifiDataReader()
   {
      mWifiDataCSVFileMap = new HashMap<>();
      mWifiDataCSVFilesList = new ArrayList<>();
   }//WifiDataReader

   public void openCSVFiles(HashMap<String, String> csvFileList)
   {
      Reader csv_reader = null;
      try
      {
         for (int i = 0; i < csvFileList.size(); ++i)
         {
            String ssid_id = "CiscoLinksysE120" + i;
            csv_reader = new FileReader(csvFileList.get(ssid_id));
            mWifiDataCSVFileMap.put(ssid_id, csv_reader);
         }//for
      }
      catch (FileNotFoundException ex)
      {
         Logger.getLogger(WifiDataReader.class.getName()).log(Level.SEVERE, null, ex);
      }
   }//openCSVFiles

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

   public int getClosestTimestampRecordRSS(Iterable<CSVRecord> csvRecord, int relativeTimestampMilliseconds)
   {
      int returned_rss = 105;
      Iterator<CSVRecord> csv_iterator = csvRecord.iterator();
      while (csv_iterator.hasNext())
      {
         CSVRecord record = csv_iterator.next();
         String test = record.get("Timestamp");
         if (abs(Integer.parseInt(record.get("Timestamp")) - relativeTimestampMilliseconds) < 500)
         {
            return Integer.parseInt(record.get("RSS"));
         }//if
      }//for

      return returned_rss;
   }//getClosestTimestampRecord

   public int getFirstRecordedTimestampMilliseconds(Iterable<CSVRecord> csvRecord)
   {
      int timestamp_milliseconds = Integer.parseInt(csvRecord.iterator().next().get("Timestamp"));
      return timestamp_milliseconds;
   }//getFirstRecordedTimestamp

   public int getLastRecordedTimestampMilliseconds(Iterable<CSVRecord> csvRecord)
   {
      Iterator<CSVRecord> csv_iterator = csvRecord.iterator();
      CSVRecord csv_record = null;
      while (csv_iterator.hasNext())
      {
         csv_record = csv_iterator.next();
      }
      int timestamp_milliseconds = Integer.parseInt(csv_record.get("Timestamp"));
      return timestamp_milliseconds;
   }//getFirstRecordedTimestamp

   /**
    *
    * @param csvRecord
    * @return
    */
   public TreeMap getSortedTreeMap(Iterable<CSVRecord> csvRecord)
   {
      TreeMap tree_map = new TreeMap();
      Iterator<CSVRecord> csv_iterator = csvRecord.iterator();
      while (csv_iterator.hasNext())
      {
         CSVRecord csv_record = csv_iterator.next();
         int timestamp = Integer.parseInt(csv_record.get("Timestamp"));
         int rss = Integer.parseInt(csv_record.get("RSS"));
         tree_map.put(timestamp, rss);
      }
      return tree_map;
   }

}//WifiDataReader
