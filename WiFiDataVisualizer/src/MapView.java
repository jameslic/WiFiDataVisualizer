
/**
 * Main GUI class, displays map, location predictions, provides DVR controls
 */
import database.SQLLiteConnection;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import static java.lang.Integer.max;
import static java.lang.Integer.min;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import org.apache.commons.csv.CSVRecord;
import positioning.AccessPointObservationRecord;
import positioning.Fingerprinting;
import positioning.PatternMatching;
import positioning.Triangulation;
import positioning.Trilateration;
import positioning.WeightedCentroid;
import wifidatavisualizer.MapDisplayPanel;
import wifidatavisualizer.WifiDataReader;
import wifidatavisualizer.NewWifiDataListener;
import wifidatavisualizer.Constants;
import wifidatavisualizer.ExportAlgorithmEstimatePointsListener;
import wifidatavisualizer.NewTruthPathDataListener;
import wifidatavisualizer.NewWifiDataListener.WifiDataType;
import wifidatavisualizer.TruthPathDataReader;

/**
 * Main GUI class, displays map, location predictions, provides DVR controls
 *
 * @author James Licata
 */
public class MapView
        extends javax.swing.JFrame
        implements ActionListener
{
   //The main image in the Map View
   JLabel mIndoorMap = new JLabel();
   //SQLLite Connection for connecting to the training data set
   SQLLiteConnection mSqlLiteConnection = new SQLLiteConnection();
   //Glass display panel
   MapDisplayPanel mMapDisplayPanel;
   JLayer<JLabel> mMapDisplayLayer;
   //Wifi Data Reader
   WifiDataReader mWifiDataReader = new WifiDataReader();
   //Router SSID to parsed CSV record map
   HashMap<String, Iterable<CSVRecord>> mSsidCsvRecordMap = new HashMap<>();
   //Router Timestamp to RSS value maps
   TreeMap<Integer, Integer> mRouter0TimestampRSSPairs = new TreeMap();
   TreeMap<Integer, Integer> mRouter1TimestampRSSPairs = new TreeMap();
   TreeMap<Integer, Integer> mRouter2TimestampRSSPairs = new TreeMap();
   TreeMap<Integer, Integer> mRouter3TimestampRSSPairs = new TreeMap();
   //CSV Input file path prefix member variable
   String mCsvInputFilePathPrefix = "";
   //Router point array list
   ArrayList<Point> mRouterPointList;
   //Wifi data listeners array list
   ArrayList<NewWifiDataListener> mWifiDataListeners;
   ArrayList<NewTruthPathDataListener> mTruthPathDataListeners;
   ArrayList<ExportAlgorithmEstimatePointsListener> mExportAlgorithmEstimatePointsListeners;
   //Slider Value for DVR playback member variable
   int mSliderValue = 0;
   //Playback timer used for firing events related to the DVR
   Timer mPlaybackTimer;
   int mTotalNumberOfDataPoints = 0;
   //Member variables to keep track of the last point estimation using the given algorithm
   Point mLastFingerprintingPoint = new Point(0, 0);
   Point mLastWeightedCentroidPoint = new Point(0, 0);
   Point mLastPatternMatchingPoint = new Point(0, 0);
   TruthPathDataReader mTruthPathDataReader = new TruthPathDataReader();
   int mWifiDataTimeStampIntervalMilliseconds = Constants.DEFAULT_WIFI_DATA_COLLECTION_INTERVAL_MILLISECONDS;

   /**
    * Creates new form MapView, default constructor
    */
   public MapView()
   {
      mWifiDataListeners = new ArrayList<>();
      mTruthPathDataListeners = new ArrayList<>();
      mExportAlgorithmEstimatePointsListeners = new ArrayList<>();
      initComponents();
      setLayout(new GridBagLayout());
      mStopPlaybackButton.setEnabled(false);

      mSliderValue = this.mNumberOfDataPointsSlider.getValue();
      ArrayList<String> router_resource_path = new ArrayList<>();
      for (int i = 0; i < 4; ++i)
      {
         String resource_string = "resources/router120" + i + ".PNG";
         router_resource_path.add((getClass().getResource(resource_string)).getPath());
      }//for
      mCsvInputFilePathPrefix = this.getClass().getResource("").getPath();
      String url_to_database = "jdbc:sqlite:" + this.getClass().getResource("").getPath() + "/resources/bld2_ap_data.db";
      mSqlLiteConnection.connect(url_to_database, "bld2_ap_data");
      mRouterPointList = mSqlLiteConnection.loadRouterPointLocations();
      mMapDisplayPanel = new MapDisplayPanel(mSqlLiteConnection.loadTrainingPointLocations(), mRouterPointList, router_resource_path);
      mIndoorMap.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Bld2_ULQuadrantLabelsRemoved.PNG"))); // NOI18N
      this.addListener(mMapDisplayPanel, mMapDisplayPanel, mMapDisplayPanel);
      mMapDisplayLayer = new JLayer<>(this.mIndoorMap, mMapDisplayPanel);
      this.add(mMapDisplayLayer);
      this.pack();
      displayNPoints(mSliderValue);

   }//MapView

   /**
    * Function to add a new wifi data listener to the Map View
    *
    * @param listenerToAdd the listener to add
    */
   public void addListener(NewWifiDataListener wifiListener, NewTruthPathDataListener truthPathListener, ExportAlgorithmEstimatePointsListener exportAlgorithmEstimatePointsListener)
   {
      mWifiDataListeners.add(wifiListener);
      mTruthPathDataListeners.add(truthPathListener);
      mExportAlgorithmEstimatePointsListeners.add(exportAlgorithmEstimatePointsListener);
   }//addListener

   /**
    * Function to handle new wifi data. Notifies applicable listeners
    *
    * @param newData  point indicating new 2D data
    * @param dataType The wifi data type
    */
   public void newWifiData(Point newData, NewWifiDataListener.WifiDataType dataType)
   {
      //If it is fingerprinting, save off the last point
      if (dataType == NewWifiDataListener.WifiDataType.FINGERPRINTING)
      {
         mLastFingerprintingPoint = newData;
      }//if
      if (dataType == NewWifiDataListener.WifiDataType.WEIGHTED_CENTROID)
      {
         mLastWeightedCentroidPoint = newData;
      }//if
      if (dataType == NewWifiDataListener.WifiDataType.PATTERN_MATCHING)
      {
         mLastPatternMatchingPoint = newData;
      }//if
      // Notify everybody that may be interested.
      for (NewWifiDataListener hl : mWifiDataListeners)
      {
         hl.newWifiData(newData, dataType);
      }//for
   }//newWifiData

   public void newTruthPathData(ArrayList<Point> truthPathData)
   {
      for (NewTruthPathDataListener h1 : this.mTruthPathDataListeners)
      {
         h1.newTruthPath(truthPathData);
      }//for
   }//newTruthPathData

   public void newExportAlgorithmEstimateRequest(WifiDataType dataType)
   {
      for (ExportAlgorithmEstimatePointsListener h1 : this.mExportAlgorithmEstimatePointsListeners)
      {
         h1.exportAlgorithmEstimatePoints(dataType);
      }//for
   }//newTruthPathData

   /**
    * Function used by DVR functionality to display a certain number of data
    * points
    *
    * @param sliderValue The number of data points to show as indicated by the
    *                    DVR slider
    */
   public void displayNPoints(int sliderValue)
   {
      // Notify everybody that may be interested.
      NewWifiDataListener.WifiDataType data_type = NewWifiDataListener.WifiDataType.DEFAULT;
      if (this.mWeightedCentroidMenuItem.isSelected())
      {
         data_type = NewWifiDataListener.WifiDataType.TRIANGULATION;
      }//if
      else if (this.mTrilaterationMenuItem.isSelected())
      {
         data_type = NewWifiDataListener.WifiDataType.TRILATERATION;
      }//else if
      else if (this.mFingerprintingMenuItem.isSelected())
      {
         data_type = NewWifiDataListener.WifiDataType.FINGERPRINTING;
      }//else if
      else if (this.mPatternMatchingMenuItem.isSelected())
      {
         data_type = NewWifiDataListener.WifiDataType.PATTERN_MATCHING;
      }//else if
      else
      {
      }//else
      for (NewWifiDataListener wifi : mWifiDataListeners)
      {
         mTotalNumberOfDataPoints = wifi.displayLastNPoints(sliderValue, data_type);
         this.repaint();
      }//for
   }//displayNPoints

   /**
    * Finds the CSV input files for the given directory and reads them into the
    * Wifi Data Reader
    *
    * @param pathPrefix      The path prefix for the data
    * @param dataPath        The path string to the data
    * @param routerPrefix    the SSID prefix of the routers
    * @param numberOfRouters the number of CSV router files
    */
   private void generateDefaultCSVInputFileList(String pathPrefix, String dataPath, String routerPrefix, int numberOfRouters)
   {
      HashMap<String, String> csv_input_map = new HashMap<>();
      for (int i = 0; i < numberOfRouters; ++i)
      {
         String resource_string_path = pathPrefix + dataPath + routerPrefix + i + Constants.DEFAULT_DATA_FILE_EXTENSION;
         csv_input_map.put(routerPrefix + i, resource_string_path);
      }//for
      mLoadedWifiDataPathLabel.setText(pathPrefix + dataPath);
      this.mWifiDataReader.openCSVFiles(csv_input_map);
   }//generateCSVInputFileList

   private void generateCSVInputFileList(File[] fileList, String routerPrefix)
   {
      HashMap<String, String> csv_input_map = new HashMap<>();
      for (File csv_file : fileList)
      {
         String file_name = csv_file.getName();
         if (file_name.contains(routerPrefix + "0"))
         {
            csv_input_map.put(routerPrefix + "0", csv_file.getAbsolutePath());
            mLoadedWifiDataPathLabel.setText(csv_file.getPath());
         }
         else if (file_name.contains(routerPrefix + "1"))
         {
            csv_input_map.put(routerPrefix + "1", csv_file.getAbsolutePath());
         }
         else if (file_name.contains(routerPrefix + "2"))
         {
            csv_input_map.put(routerPrefix + "2", csv_file.getAbsolutePath());
         }
         else if (file_name.contains(routerPrefix + "3"))
         {
            csv_input_map.put(routerPrefix + "3", csv_file.getAbsolutePath());
         }
         else
         {

         }
      }
      this.mWifiDataReader.openCSVFiles(csv_input_map);
   }//generateCSVInputFileList

   /**
    * Parses the CSV file records
    */
   private void parseCSVRecords(File[] chosenFiles)
   {
      if (chosenFiles.length != Constants.DEFAULT_NUMBER_OF_ROUTERS)
      {
         generateDefaultCSVInputFileList(mCsvInputFilePathPrefix, Constants.DEFAULT_DATA_PATH, Constants.ROUTER_PREFIX_SSID, Constants.DEFAULT_NUMBER_OF_ROUTERS);
      }//if
      else
      {
         generateCSVInputFileList(chosenFiles, Constants.ROUTER_PREFIX_SSID);
      }
      for (int i = 0; i < 4; ++i)
      {
         mSsidCsvRecordMap.put(Constants.ROUTER_PREFIX_SSID + i, mWifiDataReader.parseRecords(Constants.ROUTER_PREFIX_SSID + i));
      }//for
      int i = 0;
      mRouter0TimestampRSSPairs = this.mWifiDataReader.getSortedTreeMap(mSsidCsvRecordMap.get(Constants.ROUTER_PREFIX_SSID + i));
      ++i;
      mRouter1TimestampRSSPairs = this.mWifiDataReader.getSortedTreeMap(mSsidCsvRecordMap.get(Constants.ROUTER_PREFIX_SSID + i));
      ++i;
      mRouter2TimestampRSSPairs = this.mWifiDataReader.getSortedTreeMap(mSsidCsvRecordMap.get(Constants.ROUTER_PREFIX_SSID + i));
      ++i;
      mRouter3TimestampRSSPairs = this.mWifiDataReader.getSortedTreeMap(mSsidCsvRecordMap.get(Constants.ROUTER_PREFIX_SSID + i));
      this.mWifiDataReader.closeFiles();
   }//parseCSVRecords

   /**
    * Returns the last known timestamp from the router timestamp data
    *
    * @return latest known timestamp from all router data
    */
   private int getLatestTimestampFromRouters()
   {
      return max(max(max(mRouter0TimestampRSSPairs.lastKey().intValue(), mRouter1TimestampRSSPairs.lastKey().intValue()),
                     mRouter2TimestampRSSPairs.lastKey().intValue()), mRouter3TimestampRSSPairs.lastKey().intValue());
   }//getLatestTimestampFromRouters

   /**
    * Returns the earliest known timestamp from the router timestamp data
    *
    * @return earliest known timestamp from all router data
    */
   private int getEarliestTimestampFromRouters()
   {
      return min(min(min(mRouter0TimestampRSSPairs.firstKey().intValue(), mRouter1TimestampRSSPairs.firstKey().intValue()),
                     mRouter2TimestampRSSPairs.firstKey().intValue()), mRouter3TimestampRSSPairs.firstKey().intValue());
   }//getEarliestTimestampFromRouters

   /**
    * This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents()
   {

      jPanel1 = new javax.swing.JPanel();
      mNumberOfDataPointsSlider = new javax.swing.JSlider();
      mMapLegendPanel = new javax.swing.JPanel();
      jLabel3 = new javax.swing.JLabel();
      jLabel4 = new javax.swing.JLabel();
      jLabel1 = new javax.swing.JLabel();
      jLabel2 = new javax.swing.JLabel();
      mPatternMatchingLabel = new javax.swing.JLabel();
      mPlaybackControlsPanel = new javax.swing.JPanel();
      mStopPlaybackButton = new javax.swing.JButton();
      mPlaybackDataButton = new javax.swing.JButton();
      mPlaybackSpeedSecondsChooser = new javax.swing.JComboBox();
      mPlaybackSpeedLabel = new javax.swing.JLabel();
      mLoadedTruthPathPanel = new javax.swing.JPanel();
      mLoadedTruthPathFileName = new javax.swing.JLabel();
      mLoadedWifiDataPathPanel = new javax.swing.JPanel();
      jScrollPane1 = new javax.swing.JScrollPane();
      mLoadedWifiDataPathLabel = new javax.swing.JTextArea();
      jMenuBar1 = new javax.swing.JMenuBar();
      mFileMenu = new javax.swing.JMenu();
      mSelectMapViewItem = new javax.swing.JMenuItem();
      mLoadWifiDataMenuItem = new javax.swing.JMenuItem();
      mLoadPathMenuItem = new javax.swing.JMenuItem();
      mExportCurrentAlgorithmOutput = new javax.swing.JMenuItem();
      mAlgorithmSelectorMenu = new javax.swing.JMenu();
      mFingerprintingMenuItem = new javax.swing.JCheckBoxMenuItem();
      mPatternMatchingMenuItem = new javax.swing.JCheckBoxMenuItem();
      mTrilaterationMenuItem = new javax.swing.JCheckBoxMenuItem();
      mWeightedCentroidMenuItem = new javax.swing.JCheckBoxMenuItem();

      setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
      setTitle("Wifi Data Visualizer");
      setResizable(false);
      addWindowListener(new java.awt.event.WindowAdapter()
      {
         public void windowClosed(java.awt.event.WindowEvent evt)
         {
            formWindowClosed(evt);
         }
      });

      mNumberOfDataPointsSlider.setMajorTickSpacing(5);
      mNumberOfDataPointsSlider.setMaximum(125);
      mNumberOfDataPointsSlider.setMinorTickSpacing(1);
      mNumberOfDataPointsSlider.setOrientation(javax.swing.JSlider.VERTICAL);
      mNumberOfDataPointsSlider.setPaintLabels(true);
      mNumberOfDataPointsSlider.setPaintTicks(true);
      mNumberOfDataPointsSlider.setSnapToTicks(true);
      mNumberOfDataPointsSlider.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "# Data Points", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 13))); // NOI18N
      mNumberOfDataPointsSlider.setInverted(true);
      mNumberOfDataPointsSlider.addChangeListener(new javax.swing.event.ChangeListener()
      {
         public void stateChanged(javax.swing.event.ChangeEvent evt)
         {
            mNumberOfDataPointsSliderStateChanged(evt);
         }
      });

      mMapLegendPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Legend", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14))); // NOI18N

      jLabel3.setBackground(new java.awt.Color(255, 255, 51));
      jLabel3.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
      jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/yellow_circle.png"))); // NOI18N
      jLabel3.setText("Start Point");

      jLabel4.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
      jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/green_circle.png"))); // NOI18N
      jLabel4.setText("Weighted Centroid");

      jLabel1.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
      jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/dark_gray_circle.png"))); // NOI18N
      jLabel1.setText("Trilateration");

      jLabel2.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
      jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/magenta_circle.png"))); // NOI18N
      jLabel2.setText("Fingerprinting");

      mPatternMatchingLabel.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
      mPatternMatchingLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/red_circle.png"))); // NOI18N
      mPatternMatchingLabel.setText("Pattern Matching");

      javax.swing.GroupLayout mMapLegendPanelLayout = new javax.swing.GroupLayout(mMapLegendPanel);
      mMapLegendPanel.setLayout(mMapLegendPanelLayout);
      mMapLegendPanelLayout.setHorizontalGroup(
         mMapLegendPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(mMapLegendPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addGroup(mMapLegendPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(jLabel4)
               .addComponent(jLabel1)
               .addComponent(jLabel2)
               .addComponent(jLabel3)
               .addComponent(mPatternMatchingLabel))
            .addContainerGap(39, Short.MAX_VALUE))
      );
      mMapLegendPanelLayout.setVerticalGroup(
         mMapLegendPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mMapLegendPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel2)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(mPatternMatchingLabel)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel4)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel3)
            .addGap(54, 54, 54))
      );

      mPlaybackControlsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Playback Controls", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 13))); // NOI18N

      mStopPlaybackButton.setText("Stop");
      mStopPlaybackButton.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            mStopPlaybackButtonActionPerformed(evt);
         }
      });

      mPlaybackDataButton.setText("Playback");
      mPlaybackDataButton.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            mPlaybackDataButtonActionPerformed(evt);
         }
      });

      mPlaybackSpeedSecondsChooser.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5" }));
      mPlaybackSpeedSecondsChooser.setToolTipText("Playback speed in seconds");

      mPlaybackSpeedLabel.setText("Speed (s)");

      javax.swing.GroupLayout mPlaybackControlsPanelLayout = new javax.swing.GroupLayout(mPlaybackControlsPanel);
      mPlaybackControlsPanel.setLayout(mPlaybackControlsPanelLayout);
      mPlaybackControlsPanelLayout.setHorizontalGroup(
         mPlaybackControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mPlaybackControlsPanelLayout.createSequentialGroup()
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(mPlaybackSpeedLabel)
            .addGap(18, 18, 18)
            .addComponent(mPlaybackSpeedSecondsChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(34, 34, 34))
         .addGroup(mPlaybackControlsPanelLayout.createSequentialGroup()
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(mPlaybackControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(mPlaybackDataButton)
               .addGroup(mPlaybackControlsPanelLayout.createSequentialGroup()
                  .addGap(10, 10, 10)
                  .addComponent(mStopPlaybackButton)))
            .addGap(0, 0, Short.MAX_VALUE))
      );
      mPlaybackControlsPanelLayout.setVerticalGroup(
         mPlaybackControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mPlaybackControlsPanelLayout.createSequentialGroup()
            .addGroup(mPlaybackControlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(mPlaybackSpeedSecondsChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(mPlaybackSpeedLabel))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(mPlaybackDataButton)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
            .addComponent(mStopPlaybackButton)
            .addContainerGap())
      );

      mLoadedTruthPathPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Loaded Truth Path", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 13))); // NOI18N

      mLoadedTruthPathFileName.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
      mLoadedTruthPathFileName.setText("None");

      javax.swing.GroupLayout mLoadedTruthPathPanelLayout = new javax.swing.GroupLayout(mLoadedTruthPathPanel);
      mLoadedTruthPathPanel.setLayout(mLoadedTruthPathPanelLayout);
      mLoadedTruthPathPanelLayout.setHorizontalGroup(
         mLoadedTruthPathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(mLoadedTruthPathPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(mLoadedTruthPathFileName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );
      mLoadedTruthPathPanelLayout.setVerticalGroup(
         mLoadedTruthPathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(mLoadedTruthPathPanelLayout.createSequentialGroup()
            .addGap(29, 29, 29)
            .addComponent(mLoadedTruthPathFileName)
            .addContainerGap(55, Short.MAX_VALUE))
      );

      mLoadedWifiDataPathPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Loaded Wifi Data Path", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 13))); // NOI18N

      mLoadedWifiDataPathLabel.setBackground(new java.awt.Color(240, 240, 240));
      mLoadedWifiDataPathLabel.setColumns(20);
      mLoadedWifiDataPathLabel.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
      mLoadedWifiDataPathLabel.setLineWrap(true);
      mLoadedWifiDataPathLabel.setRows(5);
      mLoadedWifiDataPathLabel.setAutoscrolls(false);
      jScrollPane1.setViewportView(mLoadedWifiDataPathLabel);

      javax.swing.GroupLayout mLoadedWifiDataPathPanelLayout = new javax.swing.GroupLayout(mLoadedWifiDataPathPanel);
      mLoadedWifiDataPathPanel.setLayout(mLoadedWifiDataPathPanelLayout);
      mLoadedWifiDataPathPanelLayout.setHorizontalGroup(
         mLoadedWifiDataPathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(mLoadedWifiDataPathPanelLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jScrollPane1)
            .addContainerGap())
      );
      mLoadedWifiDataPathPanelLayout.setVerticalGroup(
         mLoadedWifiDataPathPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
      );

      javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
      jPanel1.setLayout(jPanel1Layout);
      jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel1Layout.createSequentialGroup()
            .addGap(7, 7, 7)
            .addComponent(mNumberOfDataPointsSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
               .addComponent(mMapLegendPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .addComponent(mPlaybackControlsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .addComponent(mLoadedTruthPathPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .addComponent(mLoadedWifiDataPathPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap(56, Short.MAX_VALUE))
      );
      jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel1Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(mNumberOfDataPointsSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 765, Short.MAX_VALUE)
            .addContainerGap())
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(mLoadedWifiDataPathPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(18, 18, 18)
            .addComponent(mLoadedTruthPathPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(18, 18, 18)
            .addComponent(mPlaybackControlsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(18, 18, 18)
            .addComponent(mMapLegendPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(89, 89, 89))
      );

      mFileMenu.setText("File");

      mSelectMapViewItem.setText("Select Map View");
      mSelectMapViewItem.addMenuKeyListener(new javax.swing.event.MenuKeyListener()
      {
         public void menuKeyPressed(javax.swing.event.MenuKeyEvent evt)
         {
            mSelectMapViewItemMenuKeyPressed(evt);
         }
         public void menuKeyReleased(javax.swing.event.MenuKeyEvent evt)
         {
         }
         public void menuKeyTyped(javax.swing.event.MenuKeyEvent evt)
         {
         }
      });
      mSelectMapViewItem.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            mSelectMapViewItemActionPerformed(evt);
         }
      });
      mFileMenu.add(mSelectMapViewItem);

      mLoadWifiDataMenuItem.setText("Load Wifi Data");
      mLoadWifiDataMenuItem.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            mLoadWifiDataMenuItemActionPerformed(evt);
         }
      });
      mFileMenu.add(mLoadWifiDataMenuItem);

      mLoadPathMenuItem.setText("Load Truth Path");
      mLoadPathMenuItem.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            mLoadPathMenuItemActionPerformed(evt);
         }
      });
      mFileMenu.add(mLoadPathMenuItem);

      mExportCurrentAlgorithmOutput.setText("Export Current Algorithm Output");
      mExportCurrentAlgorithmOutput.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            mExportCurrentAlgorithmOutputActionPerformed(evt);
         }
      });
      mFileMenu.add(mExportCurrentAlgorithmOutput);

      jMenuBar1.add(mFileMenu);

      mAlgorithmSelectorMenu.setText("Algorithm");

      mFingerprintingMenuItem.setSelected(true);
      mFingerprintingMenuItem.setText("Fingerprinting");
      mFingerprintingMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/fingerprinting.png"))); // NOI18N
      mFingerprintingMenuItem.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            mFingerprintingMenuItemActionPerformed(evt);
         }
      });
      mAlgorithmSelectorMenu.add(mFingerprintingMenuItem);

      mPatternMatchingMenuItem.setText("Pattern Matching");
      mPatternMatchingMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/pattern_matching.png"))); // NOI18N
      mPatternMatchingMenuItem.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            mPatternMatchingMenuItemActionPerformed(evt);
         }
      });
      mAlgorithmSelectorMenu.add(mPatternMatchingMenuItem);

      mTrilaterationMenuItem.setText("Trilateration");
      mTrilaterationMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/trilateration.png"))); // NOI18N
      mTrilaterationMenuItem.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            mTrilaterationMenuItemActionPerformed(evt);
         }
      });
      mAlgorithmSelectorMenu.add(mTrilaterationMenuItem);

      mWeightedCentroidMenuItem.setText("Weighted Centroid");
      mWeightedCentroidMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/triangulation.png"))); // NOI18N
      mWeightedCentroidMenuItem.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            mWeightedCentroidMenuItemActionPerformed(evt);
         }
      });
      mAlgorithmSelectorMenu.add(mWeightedCentroidMenuItem);

      jMenuBar1.add(mAlgorithmSelectorMenu);

      setJMenuBar(jMenuBar1);

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 867, Short.MAX_VALUE))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

   /**
    * Key pressed accessing the Select Map View Menu Item
    *
    * @param evt Menu Key Event
    */
   private void mSelectMapViewItemMenuKeyPressed(javax.swing.event.MenuKeyEvent evt)//GEN-FIRST:event_mSelectMapViewItemMenuKeyPressed
   {//GEN-HEADEREND:event_mSelectMapViewItemMenuKeyPressed
      // TODO add your handling code here:
      //Bring up the Map View choices dialog
   }//GEN-LAST:event_mSelectMapViewItemMenuKeyPressed

   /**
    * The main form window closed event. Cleans up open connections (i.e. SQL)
    *
    * @param evt Window Event
    */
   private void formWindowClosed(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosed
   {//GEN-HEADEREND:event_formWindowClosed
      this.mSqlLiteConnection.closeDatabase();
   }//GEN-LAST:event_formWindowClosed

   /**
    * Event fired when Load Wifi Data menu item is selected. Loads the data and
    * processes it for the currently selected algorithm.
    *
    * @param evt
    */
   private void mLoadWifiDataMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mLoadWifiDataMenuItemActionPerformed
   {//GEN-HEADEREND:event_mLoadWifiDataMenuItemActionPerformed
      //Read in the input data
      JFileChooser csv_data_collect_chooser = new JFileChooser();
      csv_data_collect_chooser.setMultiSelectionEnabled(true);
      csv_data_collect_chooser.showOpenDialog(this);
      File[] files = csv_data_collect_chooser.getSelectedFiles();
      parseCSVRecords(files);
      int ans = Integer.parseInt(JOptionPane.showInputDialog(null, "Please input the wifi data collection interval in seconds"));
      if (ans <= 10 && ans >= 0)
      {
         this.mWifiDataTimeStampIntervalMilliseconds = ans * 1000;
      }
      mLastFingerprintingPoint = this.mMapDisplayPanel.getCalibrationStartingPoint();
      mLastWeightedCentroidPoint = this.mMapDisplayPanel.getCalibrationStartingPoint();
      mLastPatternMatchingPoint = this.mMapDisplayPanel.getCalibrationStartingPoint();
      if (mRouter0TimestampRSSPairs.size() > 0)
      {
         int start_timestamp = getEarliestTimestampFromRouters();
         int end_timestamp = getLatestTimestampFromRouters();
         for (int timestamp_reference = start_timestamp; timestamp_reference < end_timestamp + 500; timestamp_reference += mWifiDataTimeStampIntervalMilliseconds)
         {
            ArrayList<SortedMap<Integer, Integer>> mSubmapArrayList = new ArrayList<>();
            mSubmapArrayList.add(mRouter0TimestampRSSPairs.subMap(timestamp_reference - 500, timestamp_reference + 500));
            mSubmapArrayList.add(mRouter1TimestampRSSPairs.subMap(timestamp_reference - 500, timestamp_reference + 500));
            mSubmapArrayList.add(mRouter2TimestampRSSPairs.subMap(timestamp_reference - 500, timestamp_reference + 500));
            mSubmapArrayList.add(mRouter3TimestampRSSPairs.subMap(timestamp_reference - 500, timestamp_reference + 500));
            makeApproximation(mSubmapArrayList);
         }//for
         this.repaint();

      }//if
   }//GEN-LAST:event_mLoadWifiDataMenuItemActionPerformed

   /**
    * Event fired when Select Map View menu item is selected. Allows user to
    * upload a new map for the input data to be displayed on
    *
    * @param evt
    */
   private void mSelectMapViewItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mSelectMapViewItemActionPerformed
   {//GEN-HEADEREND:event_mSelectMapViewItemActionPerformed
      // TODO: Add ability to load in other maps and supply distance conversion parameters
   }//GEN-LAST:event_mSelectMapViewItemActionPerformed

   /**
    * Event fired when Fingerprinting menu item is selected. Moves the indicator
    * to the menu item
    *
    * @param evt Action Event
    */
   private void mFingerprintingMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mFingerprintingMenuItemActionPerformed
   {//GEN-HEADEREND:event_mFingerprintingMenuItemActionPerformed
      if (mFingerprintingMenuItem.isSelected())
      {
         mWeightedCentroidMenuItem.setSelected(false);
         mTrilaterationMenuItem.setSelected(false);
         mPatternMatchingMenuItem.setSelected(false);
      }//if
      else
      {
         mFingerprintingMenuItem.setSelected(true);
      }//else
   }//GEN-LAST:event_mFingerprintingMenuItemActionPerformed

   /**
    * Event fired when Weighted Centroid menu item is selected. Moves the
    * indicator
    * to the menu item
    *
    * @param evt Action Event
    */
   private void mWeightedCentroidMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mWeightedCentroidMenuItemActionPerformed
   {//GEN-HEADEREND:event_mWeightedCentroidMenuItemActionPerformed
      if (mWeightedCentroidMenuItem.isSelected())
      {
         mFingerprintingMenuItem.setSelected(false);
         mTrilaterationMenuItem.setSelected(false);
         mPatternMatchingMenuItem.setSelected(false);
      }//if
      else
      {
         mWeightedCentroidMenuItem.setSelected(true);
      }//else
   }//GEN-LAST:event_mWeightedCentroidMenuItemActionPerformed

   /**
    * Event fired when Trilateration menu item is selected. Moves the indicator
    * to the menu item
    *
    * @param evt Action Event
    */
   private void mTrilaterationMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mTrilaterationMenuItemActionPerformed
   {//GEN-HEADEREND:event_mTrilaterationMenuItemActionPerformed
      if (mTrilaterationMenuItem.isSelected())
      {
         mFingerprintingMenuItem.setSelected(false);
         mWeightedCentroidMenuItem.setSelected(false);
         mPatternMatchingMenuItem.setSelected(false);
      }//if
      else
      {
         mTrilaterationMenuItem.setSelected(true);
      }//else
   }//GEN-LAST:event_mTrilaterationMenuItemActionPerformed

   /**
    * Event fired when the state of the slider changes
    *
    * @param evt Change Event
    */
   private void mNumberOfDataPointsSliderStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_mNumberOfDataPointsSliderStateChanged
   {//GEN-HEADEREND:event_mNumberOfDataPointsSliderStateChanged
      int slider_value = this.mNumberOfDataPointsSlider.getValue();
      if (slider_value != mSliderValue)
      {
         mSliderValue = slider_value;
         this.displayNPoints(mSliderValue);
         this.repaint();
      }//if
      java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.INFO, "*********Slider Stuff********");
   }//GEN-LAST:event_mNumberOfDataPointsSliderStateChanged

   /**
    * Event fired when the playback button is clicked
    *
    * @param evt Action Event
    */
   private void mPlaybackDataButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mPlaybackDataButtonActionPerformed
   {//GEN-HEADEREND:event_mPlaybackDataButtonActionPerformed
      this.mStopPlaybackButton.setEnabled(true);
      this.mPlaybackDataButton.setEnabled(false);
      this.mSliderValue = 0;
      this.mNumberOfDataPointsSlider.setValue(mSliderValue);
      int timer_value = (this.mPlaybackSpeedSecondsChooser.getSelectedIndex() + 1) * 1000;
      mPlaybackTimer = new Timer(timer_value, this);
      mPlaybackTimer.start();
   }//GEN-LAST:event_mPlaybackDataButtonActionPerformed

   /**
    * Event fired when the stop playback button is clicked
    *
    * @param evt Action Event
    */
   private void mStopPlaybackButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mStopPlaybackButtonActionPerformed
   {//GEN-HEADEREND:event_mStopPlaybackButtonActionPerformed
      this.mPlaybackTimer.stop();
      this.mPlaybackDataButton.setEnabled(true);
      this.mStopPlaybackButton.setEnabled(false);
   }//GEN-LAST:event_mStopPlaybackButtonActionPerformed

   private void mPatternMatchingMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mPatternMatchingMenuItemActionPerformed
   {//GEN-HEADEREND:event_mPatternMatchingMenuItemActionPerformed
      if (mPatternMatchingMenuItem.isSelected())
      {
         mFingerprintingMenuItem.setSelected(false);
         mWeightedCentroidMenuItem.setSelected(false);
         mTrilaterationMenuItem.setSelected(false);
      }//if
      else
      {
         mPatternMatchingMenuItem.setSelected(true);
      }//else
   }//GEN-LAST:event_mPatternMatchingMenuItemActionPerformed

   private void mLoadPathMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mLoadPathMenuItemActionPerformed
   {//GEN-HEADEREND:event_mLoadPathMenuItemActionPerformed
      // TODO add your handling code here:
      JFileChooser truth_path_chooser = new JFileChooser();
      int result = truth_path_chooser.showOpenDialog(this);
      if (result == JFileChooser.APPROVE_OPTION)
      {
         File selectedFile = truth_path_chooser.getSelectedFile();
         mTruthPathDataReader.openCSVFile(selectedFile.getAbsolutePath());
         newTruthPathData(mTruthPathDataReader.parseDataPoints());
         mLoadedTruthPathFileName.setText(selectedFile.getName());
         System.out.println("Selected file: " + selectedFile.getAbsolutePath());
      }//if

   }//GEN-LAST:event_mLoadPathMenuItemActionPerformed

   private void mExportCurrentAlgorithmOutputActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mExportCurrentAlgorithmOutputActionPerformed
   {//GEN-HEADEREND:event_mExportCurrentAlgorithmOutputActionPerformed
      // TODO add your handling code here:
      WifiDataType wifi_data_type = WifiDataType.DEFAULT;
      if (mFingerprintingMenuItem.isSelected())
      {
         wifi_data_type = WifiDataType.FINGERPRINTING;
      }
      if (mTrilaterationMenuItem.isSelected())
      {
         wifi_data_type = WifiDataType.TRILATERATION;
      }
      if (mPatternMatchingMenuItem.isSelected())
      {
         wifi_data_type = WifiDataType.PATTERN_MATCHING;
      }
      if (mTrilaterationMenuItem.isSelected())
      {
         wifi_data_type = WifiDataType.TRILATERATION;
      }
      if (mWeightedCentroidMenuItem.isSelected())
      {
         wifi_data_type = WifiDataType.WEIGHTED_CENTROID;
      }
      newExportAlgorithmEstimateRequest(wifi_data_type);
   }//GEN-LAST:event_mExportCurrentAlgorithmOutputActionPerformed

   /**
    * Given the known data points, makes an approximation using the available
    * algorithm techniques
    *
    * @param accessPointArrayList
    */
   private void makeApproximation(ArrayList<SortedMap<Integer, Integer>> accessPointArrayList)
   {
      ArrayList<AccessPointObservationRecord> access_point_list = new ArrayList<>();
      int active_router_index = 0;
      for (int i = 0; i < accessPointArrayList.size(); ++i)
      {
         if (accessPointArrayList.get(i).size() == 1 && mRouterPointList.size() == Constants.DEFAULT_NUMBER_OF_ROUTERS && accessPointArrayList.get(i).isEmpty() == false)
         {
            access_point_list.add(new AccessPointObservationRecord(accessPointArrayList.get(i).entrySet().iterator().next().getValue(), mRouterPointList.get(i), "CiscoLinksysE120" + i));
            java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.INFO, "RSS: {0}", access_point_list.get(active_router_index).getSignalLevel());
            java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.INFO, "SSID: {0}", access_point_list.get(active_router_index).getSSID());
            java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.INFO, "Point: {0}", access_point_list.get(active_router_index).getCoordinates());
            ++active_router_index;
         }//if
      }//for
      ArrayList<AccessPointObservationRecord> router_trio_list = getThreeBestRouters(access_point_list);
      if (router_trio_list.size() == 3)
      {

         if (mWeightedCentroidMenuItem.isSelected())
         {
            Point resultingPoint = WeightedCentroid.weightedCentroid(access_point_list, mSqlLiteConnection, mLastWeightedCentroidPoint);
            normalizePoint(resultingPoint);
            this.newWifiData(resultingPoint, NewWifiDataListener.WifiDataType.WEIGHTED_CENTROID);
            java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.INFO, "Weighted Centroid Point: {0}", resultingPoint.toString());
         }//if
         else if (mTrilaterationMenuItem.isSelected())
         {
            Point resultingPoint2 = Trilateration.findCenterPoint(router_trio_list);
            normalizePoint(resultingPoint2);
            this.newWifiData(resultingPoint2, NewWifiDataListener.WifiDataType.TRILATERATION);
            java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.INFO, "Trilateration Point: {0}", resultingPoint2.toString());
         }//else if
         else if (mFingerprintingMenuItem.isSelected())
         {
            Point resultingPoint3 = Fingerprinting.fingerprint(access_point_list, this.mSqlLiteConnection, mLastFingerprintingPoint);
            normalizePoint(resultingPoint3);
            this.newWifiData(resultingPoint3, NewWifiDataListener.WifiDataType.FINGERPRINTING);
            java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.INFO, "Fingerprinting Point: {0}", resultingPoint3.toString());
         }//else if
         else if (mPatternMatchingMenuItem.isSelected())
         {
            Point resultingPoint4 = PatternMatching.patternMatching(access_point_list, this.mSqlLiteConnection, mLastFingerprintingPoint);
            normalizePoint(resultingPoint4);
            this.newWifiData(resultingPoint4, NewWifiDataListener.WifiDataType.PATTERN_MATCHING);
            java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.INFO, "Pattern Matching Point: {0}", resultingPoint4.toString());
         }//else if
         else
         {

         }//else
      }//if
   }//makeApproximation

   /**
    * Tests whether a points X, Y coordinates are out of bounds
    *
    * @param testPoint
    * @return whether or not the point is out of bounds
    */
   private boolean isPointOutOfBounds(Point testPoint)
   {
      boolean is_point_oob = false;
      if (testPoint.x < 0 || testPoint.x > mIndoorMap.getWidth())
      {
         is_point_oob = true;
      }//if
      if (testPoint.y < 0 || testPoint.y > mIndoorMap.getHeight())
      {
         is_point_oob = true;
      }//if
      return is_point_oob;
   }//isPointNegative

   /**
    * Removes negative aspects of a point to keep in view of the canvas
    *
    * @param testPoint the input point to normalize
    */
   private void normalizePoint(Point testPoint)
   {
      if (isPointOutOfBounds(testPoint))
      {
         if (testPoint.x < 0)
         {
            testPoint.x = Constants.DEFAULT_PIXEL_ADJUSTMENT;
         }//if
         if (testPoint.x > mIndoorMap.getWidth())
         {
            testPoint.x = mIndoorMap.getWidth() - Constants.DEFAULT_PIXEL_ADJUSTMENT;
         }//if
         if (testPoint.y > mIndoorMap.getHeight())
         {
            testPoint.y = mIndoorMap.getHeight() - Constants.DEFAULT_PIXEL_ADJUSTMENT;
         }//if
         if (testPoint.y < 0)
         {
            testPoint.y = Constants.DEFAULT_PIXEL_ADJUSTMENT;
         }//if
      }//if
   }//normalizePoint

   /**
    * Returns the 3 strongest router (access point) candidates based on reported
    * RSS
    *
    * @param accessPointList list of all the access points
    * @return the list of the 3 best router candidates
    */
   private ArrayList<AccessPointObservationRecord> getThreeBestRouters(ArrayList<AccessPointObservationRecord> accessPointList)
   {
      ArrayList<AccessPointObservationRecord> access_points_final_trio = new ArrayList<>();
      for (int i = 0; i < accessPointList.size(); ++i)
      {
         access_points_final_trio.add(null);
      }//for
      Collections.copy(access_points_final_trio, accessPointList);
      if (access_points_final_trio.size() == Constants.DEFAULT_NUMBER_OF_ROUTERS)
      {
         int minIndex = access_points_final_trio.indexOf(Collections.min(access_points_final_trio));
         access_points_final_trio.remove(minIndex);
         java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.INFO, "Distance in pixels: {0}", access_points_final_trio.get(0).getDistancePixels());
         java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.INFO, "Distance in pixels: {0}", access_points_final_trio.get(1).getDistancePixels());
         java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.INFO, "Distance in pixels: {0}", access_points_final_trio.get(2).getDistancePixels());
      }//if

      return access_points_final_trio;
   }//getThreeBestRouters

   /**
    * Map View main function
    *
    * @param args the command line arguments
    */
   public static void main(String args[])
   {
      /* Set the Nimbus look and feel */
      //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
      /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
       * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
       */
      try
      {
         for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
         {
            if ("Nimbus".equals(info.getName()))
            {
               javax.swing.UIManager.setLookAndFeel(info.getClassName());
               break;
            }//if
         }//for
      }//try
      catch (ClassNotFoundException ex)
      {
         java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
      }//catch
      catch (InstantiationException ex)
      {
         java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
      }//catch
      catch (IllegalAccessException ex)
      {
         java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
      }//catch
      catch (javax.swing.UnsupportedLookAndFeelException ex)
      {
         java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
      }//catch
      //</editor-fold>

      /* Create and display the form */
      java.awt.EventQueue.invokeLater(new Runnable()
      {
         public void run()
         {
            new MapView().setVisible(true);
         }//run
      });
   }//main

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel3;
   private javax.swing.JLabel jLabel4;
   private javax.swing.JMenuBar jMenuBar1;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JScrollPane jScrollPane1;
   private javax.swing.JMenu mAlgorithmSelectorMenu;
   private javax.swing.JMenuItem mExportCurrentAlgorithmOutput;
   private javax.swing.JMenu mFileMenu;
   private javax.swing.JCheckBoxMenuItem mFingerprintingMenuItem;
   private javax.swing.JMenuItem mLoadPathMenuItem;
   private javax.swing.JMenuItem mLoadWifiDataMenuItem;
   private javax.swing.JLabel mLoadedTruthPathFileName;
   private javax.swing.JPanel mLoadedTruthPathPanel;
   private javax.swing.JTextArea mLoadedWifiDataPathLabel;
   private javax.swing.JPanel mLoadedWifiDataPathPanel;
   private javax.swing.JPanel mMapLegendPanel;
   private javax.swing.JSlider mNumberOfDataPointsSlider;
   private javax.swing.JLabel mPatternMatchingLabel;
   private javax.swing.JCheckBoxMenuItem mPatternMatchingMenuItem;
   private javax.swing.JPanel mPlaybackControlsPanel;
   private javax.swing.JButton mPlaybackDataButton;
   private javax.swing.JLabel mPlaybackSpeedLabel;
   private javax.swing.JComboBox mPlaybackSpeedSecondsChooser;
   private javax.swing.JMenuItem mSelectMapViewItem;
   private javax.swing.JButton mStopPlaybackButton;
   private javax.swing.JCheckBoxMenuItem mTrilaterationMenuItem;
   private javax.swing.JCheckBoxMenuItem mWeightedCentroidMenuItem;
   // End of variables declaration//GEN-END:variables

   /**
    * Event fired when the slider is moved
    *
    * @param e Action Event for slider
    */
   @Override
   public void actionPerformed(ActionEvent e)
   {
      ++mSliderValue;
      if (this.mSliderValue <= mTotalNumberOfDataPoints + 1)
      {
         this.mNumberOfDataPointsSlider.setValue(mSliderValue);
         this.displayNPoints(mSliderValue + 1);
         this.repaint();
      }//if
      else
      {
         this.mPlaybackTimer.stop();
         this.mPlaybackDataButton.setEnabled(true);
         this.mStopPlaybackButton.setEnabled(false);
      }//else
   }//actionPerformed
}//MapView
