
import database.SQLLiteConnection;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static java.lang.Integer.max;
import static java.lang.Integer.min;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JLayeredPane;
import javax.swing.Timer;
import org.apache.commons.csv.CSVRecord;
import positioning.AccessPoint;
import positioning.Fingerprinting;
import positioning.Triangulation;
import positioning.Trilateration;
import wifidatavisualizer.MapDisplayPanel;
import wifidatavisualizer.WifiDataReader;
import wifidatavisualizer.NewWifiDataListener;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author James Licata
 */
public class MapView
        extends javax.swing.JFrame
        implements ActionListener
{

   final public static String ROUTER_PREFIX_SSID = "CiscoLinksysE120";
   JLabel mIndoorMap = new JLabel();
   //SQLLite Connection for connecting to the training data set
   SQLLiteConnection mSqlLiteConnection = new SQLLiteConnection();
   //LayerUI<JLabel> mMapDisplayPanel;
   MapDisplayPanel mMapDisplayPanel;
   JLayer<JLabel> mMapDisplayLayer;
   WifiDataReader mWifiDataReader = new WifiDataReader();
   HashMap<String, Iterable<CSVRecord>> mSsidCsvRecordMap = new HashMap<>();
   TreeMap<Integer, Integer> mRouter0TimestampRSSPairs = new TreeMap();
   TreeMap<Integer, Integer> mRouter1TimestampRSSPairs = new TreeMap();
   TreeMap<Integer, Integer> mRouter2TimestampRSSPairs = new TreeMap();
   TreeMap<Integer, Integer> mRouter3TimestampRSSPairs = new TreeMap();
   String mCsvInputFilePathPrefix = "";
   ArrayList<Point> mRouterPointList;
   ArrayList<NewWifiDataListener> mWifiDataListeners;
   int mSliderValue = 0;
   Timer mPlaybackTimer;
   int mTotalNumberOfDataPoints = 0;

   /**
    * Creates new form MapView
    */
   public MapView()
   {
      this.mWifiDataListeners = new ArrayList<>();
      initComponents();
      this.mStopPlaybackButton.setEnabled(false);

      this.mSliderValue = this.mNumberOfDataPointsSlider.getValue();
      this.setLayout(new GridBagLayout());
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
      this.addListener(mMapDisplayPanel);
      mMapDisplayLayer = new JLayer<>(this.mIndoorMap, mMapDisplayPanel);
      this.add(mMapDisplayLayer);
      this.pack();
      displayNPoints(mSliderValue);

   }//MapView

   public void addListener(NewWifiDataListener toAdd)
   {
      mWifiDataListeners.add(toAdd);
   }

   public void newWifiData(Point newData, NewWifiDataListener.WifiDataType dataType)
   {
      // Notify everybody that may be interested.
      for (NewWifiDataListener hl : mWifiDataListeners)
      {
         hl.newWifiData(newData, dataType);
      }//for
   }//newWifiData

   public void displayNPoints(int sliderValue)
   {
      // Notify everybody that may be interested.
      NewWifiDataListener.WifiDataType data_type = NewWifiDataListener.WifiDataType.DEFAULT;
      if (this.triangulationMenuItem.isSelected())
      {
         data_type = NewWifiDataListener.WifiDataType.TRIANGULATION;
      }//if
      else if (this.trilaterationMenuItem.isSelected())
      {
         data_type = NewWifiDataListener.WifiDataType.TRILATERATION;
      }
      else if (this.fingerprintingMenuItem3.isSelected())
      {
         data_type = NewWifiDataListener.WifiDataType.FINGERPRINTING;
      }
      else
      {

      }//else
      for (NewWifiDataListener wifi : mWifiDataListeners)
      {
         mTotalNumberOfDataPoints = wifi.displayLastNPoints(sliderValue, data_type);
         this.repaint();
      }//for
   }

   private void testLayer()
   {
      JLayeredPane test_pane = this.getLayeredPane();
      int component_count = test_pane.getComponentCount();
      Component[] components = test_pane.getComponents();
      Component test_comp = components[0].getComponentAt(500, 500);
      //(JLayer) test_comp.get
      Component test_comp2 = test_comp.getComponentAt(500, 500);
      System.out.println("TEST");
   }

   private void generateCSVInputFileList(String pathPrefix)
   {
      HashMap<String, String> csv_input_map = new HashMap<>();
      for (int i = 0; i < 4; ++i)
      {
         String resource_string_path = pathPrefix + "data/02012015-5sec/" + this.ROUTER_PREFIX_SSID + i + ".csv";
         csv_input_map.put(ROUTER_PREFIX_SSID + i, resource_string_path);
      }//for
      this.mWifiDataReader.openCSVFiles(csv_input_map);
   }//generateCSVInputFileList

   private void parseCSVRecords()
   {
      generateCSVInputFileList(mCsvInputFilePathPrefix);
      for (int i = 0; i < 4; ++i)
      {
         mSsidCsvRecordMap.put(ROUTER_PREFIX_SSID + i, mWifiDataReader.parseRecords(ROUTER_PREFIX_SSID + i));
      }//for
      int i = 0;
      mRouter0TimestampRSSPairs = this.mWifiDataReader.getSortedTreeMap(mSsidCsvRecordMap.get(ROUTER_PREFIX_SSID + i));
      ++i;
      mRouter1TimestampRSSPairs = this.mWifiDataReader.getSortedTreeMap(mSsidCsvRecordMap.get(ROUTER_PREFIX_SSID + i));
      ++i;
      mRouter2TimestampRSSPairs = this.mWifiDataReader.getSortedTreeMap(mSsidCsvRecordMap.get(ROUTER_PREFIX_SSID + i));
      ++i;
      mRouter3TimestampRSSPairs = this.mWifiDataReader.getSortedTreeMap(mSsidCsvRecordMap.get(ROUTER_PREFIX_SSID + i));
      this.mWifiDataReader.closeFiles();
   }//parseCSVRecords

   private int getLatestTimestampFromRouters()
   {
      return max(max(max(mRouter0TimestampRSSPairs.lastKey().intValue(), mRouter1TimestampRSSPairs.lastKey().intValue()),
                     mRouter2TimestampRSSPairs.lastKey().intValue()), mRouter3TimestampRSSPairs.lastKey().intValue());
   }//getLatestTimestampFromRouters

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
      jPanel2 = new javax.swing.JPanel();
      jLabel3 = new javax.swing.JLabel();
      jLabel4 = new javax.swing.JLabel();
      jLabel1 = new javax.swing.JLabel();
      jLabel2 = new javax.swing.JLabel();
      jPanel3 = new javax.swing.JPanel();
      mStopPlaybackButton = new javax.swing.JButton();
      mPlaybackDataButton = new javax.swing.JButton();
      mPlaybackSpeedSecondsChooser = new javax.swing.JComboBox();
      jLabel5 = new javax.swing.JLabel();
      jMenuBar1 = new javax.swing.JMenuBar();
      jMenu1 = new javax.swing.JMenu();
      mSelectMapViewItem = new javax.swing.JMenuItem();
      jMenuItem1 = new javax.swing.JMenuItem();
      jMenu2 = new javax.swing.JMenu();
      triangulationMenuItem = new javax.swing.JCheckBoxMenuItem();
      trilaterationMenuItem = new javax.swing.JCheckBoxMenuItem();
      fingerprintingMenuItem3 = new javax.swing.JCheckBoxMenuItem();

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

      mNumberOfDataPointsSlider.setMajorTickSpacing(2);
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

      jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Legend", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 14))); // NOI18N

      jLabel3.setBackground(new java.awt.Color(255, 255, 51));
      jLabel3.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
      jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/yellow_circle.png"))); // NOI18N
      jLabel3.setText("Start Point");

      jLabel4.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
      jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/green_circle.png"))); // NOI18N
      jLabel4.setText("Triangulation");

      jLabel1.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
      jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/dark_gray_circle.png"))); // NOI18N
      jLabel1.setText("Trilateration");

      jLabel2.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
      jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/magenta_circle.png"))); // NOI18N
      jLabel2.setText("Fingerprinting");

      javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
      jPanel2.setLayout(jPanel2Layout);
      jPanel2Layout.setHorizontalGroup(
         jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel2Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addComponent(jLabel4)
               .addComponent(jLabel1)
               .addComponent(jLabel2)
               .addComponent(jLabel3))
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );
      jPanel2Layout.setVerticalGroup(
         jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(jLabel2)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel1)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel4)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jLabel3)
            .addContainerGap())
      );

      jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Playback Controls", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 13))); // NOI18N

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

      jLabel5.setText("Speed (s)");

      javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
      jPanel3.setLayout(jPanel3Layout);
      jPanel3Layout.setHorizontalGroup(
         jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel3Layout.createSequentialGroup()
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(jPanel3Layout.createSequentialGroup()
                  .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addComponent(mStopPlaybackButton))
                     .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(mPlaybackDataButton)))
                  .addGap(0, 0, Short.MAX_VALUE))
               .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                  .addGap(0, 0, Short.MAX_VALUE)
                  .addComponent(jLabel5)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                  .addComponent(mPlaybackSpeedSecondsChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addContainerGap())
      );
      jPanel3Layout.setVerticalGroup(
         jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
               .addComponent(mPlaybackSpeedSecondsChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
               .addComponent(jLabel5))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
            .addComponent(mPlaybackDataButton)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(mStopPlaybackButton)
            .addContainerGap())
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
               .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
               .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap(56, Short.MAX_VALUE))
      );
      jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel1Layout.createSequentialGroup()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
               .addGroup(jPanel1Layout.createSequentialGroup()
                  .addGap(58, 58, 58)
                  .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                  .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addGap(0, 451, Short.MAX_VALUE))
               .addGroup(jPanel1Layout.createSequentialGroup()
                  .addContainerGap()
                  .addComponent(mNumberOfDataPointsSlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addContainerGap())
      );

      jMenu1.setText("File");

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
      jMenu1.add(mSelectMapViewItem);

      jMenuItem1.setText("Load Wifi Data");
      jMenuItem1.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            jMenuItem1ActionPerformed(evt);
         }
      });
      jMenu1.add(jMenuItem1);

      jMenuBar1.add(jMenu1);

      jMenu2.setText("Algorithm");

      triangulationMenuItem.setText("Triangulation");
      triangulationMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/triangulation.png"))); // NOI18N
      triangulationMenuItem.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            triangulationMenuItemActionPerformed(evt);
         }
      });
      jMenu2.add(triangulationMenuItem);

      trilaterationMenuItem.setText("Trilateration");
      trilaterationMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/trilateration.png"))); // NOI18N
      trilaterationMenuItem.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            trilaterationMenuItemActionPerformed(evt);
         }
      });
      jMenu2.add(trilaterationMenuItem);

      fingerprintingMenuItem3.setSelected(true);
      fingerprintingMenuItem3.setText("Fingerprinting");
      fingerprintingMenuItem3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/fingerprinting.png"))); // NOI18N
      fingerprintingMenuItem3.addActionListener(new java.awt.event.ActionListener()
      {
         public void actionPerformed(java.awt.event.ActionEvent evt)
         {
            fingerprintingMenuItem3ActionPerformed(evt);
         }
      });
      jMenu2.add(fingerprintingMenuItem3);

      jMenuBar1.add(jMenu2);

      setJMenuBar(jMenuBar1);

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(0, 909, Short.MAX_VALUE))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      );

      pack();
   }// </editor-fold>//GEN-END:initComponents

   private void mSelectMapViewItemMenuKeyPressed(javax.swing.event.MenuKeyEvent evt)//GEN-FIRST:event_mSelectMapViewItemMenuKeyPressed
   {//GEN-HEADEREND:event_mSelectMapViewItemMenuKeyPressed
      // TODO add your handling code here:
      //Bring up the Map View choices dialog
   }//GEN-LAST:event_mSelectMapViewItemMenuKeyPressed

   private void formWindowClosed(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosed
   {//GEN-HEADEREND:event_formWindowClosed
      // TODO add your handling code here:
      this.mSqlLiteConnection.closeDatabase();
   }//GEN-LAST:event_formWindowClosed

   private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem1ActionPerformed
   {//GEN-HEADEREND:event_jMenuItem1ActionPerformed
      //Read in the input data
      parseCSVRecords();

      //After it is read to process, start looking through it at the input interval
      if (mRouter0TimestampRSSPairs.size() > 0)
      {
         int start_timestamp = getEarliestTimestampFromRouters();
         int end_timestamp = getLatestTimestampFromRouters();
         for (int timestamp_reference = start_timestamp; timestamp_reference < end_timestamp + 500; timestamp_reference += 5000)
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
   }//GEN-LAST:event_jMenuItem1ActionPerformed

   private void mSelectMapViewItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mSelectMapViewItemActionPerformed
   {//GEN-HEADEREND:event_mSelectMapViewItemActionPerformed
      // THis is a test for adding the new wifi data
      //this.newWifiData(new Point(300, 300), NewWifiDataListener.WifiDataType.TRILATERATION);
   }//GEN-LAST:event_mSelectMapViewItemActionPerformed

   private void fingerprintingMenuItem3ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_fingerprintingMenuItem3ActionPerformed
   {//GEN-HEADEREND:event_fingerprintingMenuItem3ActionPerformed
      // TODO add your handling code here:
      if (fingerprintingMenuItem3.isSelected())
      {
         triangulationMenuItem.setSelected(false);
         trilaterationMenuItem.setSelected(false);
      }//if
      else
      {
         fingerprintingMenuItem3.setSelected(true);
      }
   }//GEN-LAST:event_fingerprintingMenuItem3ActionPerformed

   private void triangulationMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_triangulationMenuItemActionPerformed
   {//GEN-HEADEREND:event_triangulationMenuItemActionPerformed
      // TODO add your handling code here:
      if (triangulationMenuItem.isSelected())
      {
         fingerprintingMenuItem3.setSelected(false);
         trilaterationMenuItem.setSelected(false);
      }//if
      else
      {
         triangulationMenuItem.setSelected(true);
      }
   }//GEN-LAST:event_triangulationMenuItemActionPerformed

   private void trilaterationMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_trilaterationMenuItemActionPerformed
   {//GEN-HEADEREND:event_trilaterationMenuItemActionPerformed
      // TODO add your handling code here:
      if (trilaterationMenuItem.isSelected())
      {
         fingerprintingMenuItem3.setSelected(false);
         triangulationMenuItem.setSelected(false);
      }//if
      else
      {
         trilaterationMenuItem.setSelected(true);
      }
   }//GEN-LAST:event_trilaterationMenuItemActionPerformed

   private void mNumberOfDataPointsSliderStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_mNumberOfDataPointsSliderStateChanged
   {//GEN-HEADEREND:event_mNumberOfDataPointsSliderStateChanged
      // TODO add your handling code here:
      int slider_value = this.mNumberOfDataPointsSlider.getValue();
      if (slider_value != mSliderValue)
      {
         mSliderValue = slider_value;
         this.displayNPoints(mSliderValue);
         this.repaint();
      }//if
      java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.INFO, "*********Slider Stuff********");

   }//GEN-LAST:event_mNumberOfDataPointsSliderStateChanged

   private void mPlaybackDataButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mPlaybackDataButtonActionPerformed
   {//GEN-HEADEREND:event_mPlaybackDataButtonActionPerformed
      // TODO add your handling code here:
      this.mStopPlaybackButton.setEnabled(true);
      this.mPlaybackDataButton.setEnabled(false);
      this.mSliderValue = 0;
      this.mNumberOfDataPointsSlider.setValue(mSliderValue);
      mPlaybackTimer = new Timer((this.mPlaybackSpeedSecondsChooser.getSelectedIndex() + 1) * 1000, this);
      mPlaybackTimer.start();
   }//GEN-LAST:event_mPlaybackDataButtonActionPerformed

   private void mStopPlaybackButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mStopPlaybackButtonActionPerformed
   {//GEN-HEADEREND:event_mStopPlaybackButtonActionPerformed
      // TODO add your handling code here:
      this.mPlaybackTimer.stop();
      this.mPlaybackDataButton.setEnabled(true);
      this.mStopPlaybackButton.setEnabled(false);
   }//GEN-LAST:event_mStopPlaybackButtonActionPerformed

   private void printSubMap(SortedMap<Integer, Integer> submap, int index)
   {
      if (submap.size() > 0)
      {
         java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.INFO, "*********\nCiscoLinksysE120{0}\n********", index);
         Entry<Integer, Integer> router0_entry = submap.entrySet().iterator().next();
         java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.INFO, "Timestamp: {0}", router0_entry.getKey());
         java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.INFO, "RSS: {0}", router0_entry.getValue());
      }//if
   }//printSubmMap

   private void makeApproximation(ArrayList<SortedMap<Integer, Integer>> accessPointArrayList)
   {
      ArrayList<AccessPoint> access_point_list = new ArrayList<>();
      int active_router_index = 0;
      for (int i = 0; i < accessPointArrayList.size(); ++i)
      {
         if (accessPointArrayList.get(i).size() == 1 && mRouterPointList.size() == 4 && accessPointArrayList.get(i).isEmpty() == false)
         {
            access_point_list.add(new AccessPoint(accessPointArrayList.get(i).entrySet().iterator().next().getValue(), mRouterPointList.get(i), "CiscoLinksysE120" + i));
            java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.INFO, "RSS: {0}", access_point_list.get(active_router_index).getSignalLevel());
            java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.INFO, "SSID: {0}", access_point_list.get(active_router_index).getSSID());
            java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.INFO, "Point: {0}", access_point_list.get(active_router_index).getCoordinates());
            ++active_router_index;
         }//if
      }//for
      getThreeBestRouters(access_point_list);
      if (access_point_list.size() == 3)
      {

         if (triangulationMenuItem.isSelected())
         {
            Point resultingPoint = Triangulation.triangulate(access_point_list);
            this.newWifiData(resultingPoint, NewWifiDataListener.WifiDataType.TRIANGULATION);
            java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.INFO, "Triangulation Point: {0}", resultingPoint.toString());
         }//if
         else if (trilaterationMenuItem.isSelected())
         {
            Point resultingPoint2 = Trilateration.findCenterPoint(access_point_list);
            this.newWifiData(resultingPoint2, NewWifiDataListener.WifiDataType.TRILATERATION);
            java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.INFO, "Trilateration Point: {0}", resultingPoint2.toString());
         }//else if
         else if (fingerprintingMenuItem3.isSelected())
         {
            Point resultingPoint3 = Fingerprinting.fingerprint(access_point_list, this.mSqlLiteConnection);
            this.newWifiData(resultingPoint3, NewWifiDataListener.WifiDataType.FINGERPRINTING);
            java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.INFO, "Fingerprinting Point: {0}", resultingPoint3.toString());
         }//else if
         else
         {

         }//else
      }//if
   }//makeApproximation

   private ArrayList<AccessPoint> getThreeBestRouters(ArrayList<AccessPoint> arrayList)
   {
      ArrayList<AccessPoint> access_points_final_trio = new ArrayList<>();
      if (arrayList.size() == 4)
      {
         int minIndex = arrayList.indexOf(Collections.min(arrayList));
         arrayList.remove(minIndex);
         java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.INFO, "Distance in pixels: {0}", arrayList.get(0).getDistance());
         java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.INFO, "Distance in pixels: {0}", arrayList.get(1).getDistance());
         java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.INFO, "Distance in pixels: {0}", arrayList.get(2).getDistance());
      }//if

      return access_points_final_trio;
   }//getThreeBestRouters

   /**
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
   private javax.swing.JCheckBoxMenuItem fingerprintingMenuItem3;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel3;
   private javax.swing.JLabel jLabel4;
   private javax.swing.JLabel jLabel5;
   private javax.swing.JMenu jMenu1;
   private javax.swing.JMenu jMenu2;
   private javax.swing.JMenuBar jMenuBar1;
   private javax.swing.JMenuItem jMenuItem1;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JPanel jPanel2;
   private javax.swing.JPanel jPanel3;
   private javax.swing.JSlider mNumberOfDataPointsSlider;
   private javax.swing.JButton mPlaybackDataButton;
   private javax.swing.JComboBox mPlaybackSpeedSecondsChooser;
   private javax.swing.JMenuItem mSelectMapViewItem;
   private javax.swing.JButton mStopPlaybackButton;
   private javax.swing.JCheckBoxMenuItem triangulationMenuItem;
   private javax.swing.JCheckBoxMenuItem trilaterationMenuItem;
   // End of variables declaration//GEN-END:variables

   @Override
   public void actionPerformed(ActionEvent e)
   {
      ++mSliderValue;
      if (this.mSliderValue < mTotalNumberOfDataPoints)
      {
         this.mNumberOfDataPointsSlider.setValue(mSliderValue);
         this.displayNPoints(mSliderValue);
         this.repaint();
      }//if
      else
      {
         this.mPlaybackTimer.stop();
      }//else
   }//actionPerformed
}//MapView
