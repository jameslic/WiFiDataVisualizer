/*
 * Class is an implementation of a "glass" panel in which items can be drawn ontop of an existing image
 */
package wifidatavisualizer;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.WeakHashMap;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.LayerUI;

/**
 * Displays the algorithm visualizations overtop of a displayed image,
 * such as range rings, position estimations, router placements, and training
 * data points.
 *
 * @author James Licata
 */
public class MapDisplayPanel
        extends LayerUI<JLabel>
        implements NewWifiDataListener, NewTruthPathDataListener, ExportAlgorithmEstimatePointsListener
{
   private final Map<NewWifiDataListener.WifiDataType, ArrayList<Point>> mMapPointsOfInterestList;
   private final ArrayList<Point> mRouterPointList;
   private final ArrayList<Image> mRouterImageList;
   /**
    * List of rectangles that represent the bounded area around routers
    */
   private final ArrayList<Rectangle2D> mRouterImageBoundedRectangleList;
   /**
    * The training data point list
    */
   private final ArrayList<Point> mTrainingDataPointList;
   private ArrayList<Point> mRecordedTruthPathPointList;
   public boolean mStartPointChosen = false;
   /**
    * The default spot for the calibration starting point
    */
   private Point mCalibrationStartPoint = new Point(Constants.DEFAULT_CALIBRATION_START_POINT_X_COORDINATE,
                                                    Constants.DEFAULT_CALIBRATION_START_POINT_Y_COORDINATE);
   /**
    * A bounds check oval member variable
    */
   private Ellipse2D mBoundsCheckOval = null;
   /**
    * Member variable to store a parent JFrame
    */
   JFrame mParentFrame;
   /**
    * The total number of point estimations to display -
    * when controlled by DVR this is the threshold limiter
    */
   int mNumberOfPointsToDisplay = 0;
   private boolean mStartPathRecord = false;
   private ArrayList<Point> mTruthPathDataPointList;

   /**
    * Main constructor for creating the Map Display Panel
    *
    * @param trainingDataPointList list of training data points used as
    *                              references for position estimation
    * @param routerPointList       list of coordinates for the routers (access
    *                              points) used to collect data
    * @param routerResourcePath    the path to router image files
    */
   public MapDisplayPanel(ArrayList<Point> trainingDataPointList, ArrayList<Point> routerPointList, ArrayList<String> routerResourcePath)
   {
      mMapPointsOfInterestList = new WeakHashMap<>();
      mRouterImageList = new ArrayList<>();
      mRecordedTruthPathPointList = new ArrayList<>();
      mTrainingDataPointList = trainingDataPointList;
      mRouterPointList = routerPointList;
      mTruthPathDataPointList = new ArrayList<>();
      mRouterImageBoundedRectangleList = new ArrayList<>();

      try
      {
         for (String router_resource : routerResourcePath)
         {
            mRouterImageList.add(ImageIO.read(new File(router_resource)));
         }//for
      }//try
      catch (IOException e)
      {
      }//catch
      //this.addMouseMotionListener(this);
   }//MapDisplayPanel

   /**
    * Creates the glass layer and the event mask for mouse events
    *
    * @param inputLayer the layer to install glass over
    */
   @Override
   public void installUI(JComponent inputLayer)
   {
      System.out.println("install");
      super.installUI(inputLayer);
      JLayer layer = (JLayer) inputLayer;
      layer.setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
      //layer.setLayerEventMask();
   }//installUI

   /**
    * Removes the glass layer and clears stored member variable data lists
    *
    * @param inputLayer the layer to uninstall glass from
    */
   @Override
   public void uninstallUI(JComponent inputLayer)
   {
      super.uninstallUI(inputLayer);
      mMapPointsOfInterestList.clear();
      this.mMapPointsOfInterestList.clear();
      this.mRouterImageBoundedRectangleList.clear();
      this.mRouterPointList.clear();
   }//uninstallUI

   /**
    * Returns the calibration starting point
    *
    * @return the calibration starting point
    */
   public Point getCalibrationStartingPoint()
   {
      return this.mCalibrationStartPoint;
   }//getCalibrationStartingPoint

   private void saveRecordedTruthPath()
   {
      try
      {
         SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
         FileWriter writer = new FileWriter("RecordedTruthPath" + sdf.format(new Date()) + ".csv");

         writer.append("X");
         writer.append(',');
         writer.append("Y");
         writer.append('\n');

         for (Point truth_point : this.mRecordedTruthPathPointList)
         {
            writer.append(Integer.toString(truth_point.x));
            writer.append(',');
            writer.append(Integer.toString(truth_point.y));
            writer.append('\n');
         }//for

         writer.flush();
         writer.close();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }//saveRecordedTruthPath

   private void exportAlgorithmEstimatePointData(WifiDataType dataType, ArrayList<Point> pointList)
   {
      try
      {
         SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
         FileWriter writer = new FileWriter("Algorithm_Estimate_" + dataType.name() + sdf.format(new Date()) + ".csv");

         writer.append("X");
         writer.append(',');
         writer.append("Y");
         writer.append('\n');

         for (Point point_estimate : pointList)
         {
            writer.append(Integer.toString(point_estimate.x));
            writer.append(',');
            writer.append(Integer.toString(point_estimate.y));
            writer.append('\n');
         }//for

         writer.flush();
         writer.close();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }//saveRecordedTruthPath

   /**
    * Event fired when mouse event occur ontop of the glass layer
    *
    * @param mouseEvent the applicable mouse event (click, move, release, etc.)
    * @param layer      the layer component affected by the mouse movement
    */
   @Override
   protected void processMouseEvent(MouseEvent mouseEvent, JLayer<? extends JLabel> layer)
   {
      if (mouseEvent.getID() == MouseEvent.MOUSE_CLICKED && SwingUtilities.isRightMouseButton(mouseEvent) && mStartPointChosen == false)
      {
         Point mouse_point = mouseEvent.getPoint();
         mouse_point = SwingUtilities.convertPoint(mouseEvent.getComponent(), mouse_point, layer);
         int dialog_choice_result = JOptionPane.showConfirmDialog(null, "Choose the following point as the starting calibration point?" + "\nX: " + mouse_point.x + " Y: " + mouse_point.y
         );
         if (dialog_choice_result == JOptionPane.YES_OPTION)
         {
            mCalibrationStartPoint = mouse_point;
            mStartPointChosen = true;
         }//if
         layer.repaint();

      }//if
      if (mouseEvent.getID() == MouseEvent.MOUSE_CLICKED && SwingUtilities.isRightMouseButton(mouseEvent) && mouseEvent.getClickCount() == 2)
      {
         if (mStartPathRecord)
         {
            mStartPathRecord = false;
            saveRecordedTruthPath();
            JOptionPane.showMessageDialog(null, "Ending path record.");
         }
         else
         {
            mStartPathRecord = true;
            mRecordedTruthPathPointList.clear();
            JOptionPane.showMessageDialog(null, "Starting path record.");
         }
      }//if

      if (SwingUtilities.isLeftMouseButton(mouseEvent))
      {
         Point test_point = mouseEvent.getPoint();
         for (int i = 0; i < mRouterPointList.size(); ++i)
         {
            if (this.mRouterImageBoundedRectangleList.get(i).contains(test_point))
            {
               JOptionPane.showMessageDialog(null, "Router: " + Constants.ROUTER_PREFIX_SSID + i + " X,Y: ("
                                             + mRouterPointList.get(i).x + "," + mRouterPointList.get(i).y + ")");
            }//if
         }//for
      }//if
   }//processMouseEvent

   @Override
   protected void processMouseMotionEvent(MouseEvent mouseEvent, JLayer<? extends JLabel> layer)
   {
      if (mouseEvent.getID() == MouseEvent.MOUSE_DRAGGED && SwingUtilities.isRightMouseButton(mouseEvent) && mStartPathRecord)
      {
         Point mouse_point = mouseEvent.getPoint();
         mouse_point = SwingUtilities.convertPoint(mouseEvent.getComponent(), mouse_point, layer);
         this.mRecordedTruthPathPointList.add(mouse_point);
         //System.out.println("New point to record: " + mouse_point.toString());
      }//if
   }//processMouseMotionEvent

   /**
    * Returns the member variable with the stored Router coordinate list
    *
    * @return an array list with the router coordinates
    */
   public ArrayList<Point> getRouterPointList()
   {
      return this.mRouterPointList;
   }//getRouterPointList

   /**
    * Adds a point to the glass layer
    *
    * @param wifiLocalizationPoint the point to add
    * @param layer                 the parent layer
    */
   public void addNewWiFiLocalizationPoint(Point wifiLocalizationPoint, JLayer<? extends JLabel> layer)
   {
      /*ArrayList<Point> points = mMapPointsOfInterestList.get(layer);
       if (points == null)
       {
       points = new ArrayList<>();
       }//if
       points.add(wifiLocalizationPoint);
       layer.repaint();
       */
   }//addNewWiFiLocalizationPoint

   /**
    * Overridden paint function for the glass layer. Renders the appropriate
    * position estimations and range rings
    *
    * @param graphicsComponent      the GUI graphics component to create the @D
    *                               graphics from
    * @param inputMapLayerComponent the main map view image
    */
   @Override
   public void paint(Graphics graphicsComponent, JComponent inputMapLayerComponent)
   {
      Graphics2D graphics_2d_utility = (Graphics2D) graphicsComponent.create();
      super.paint(graphics_2d_utility, inputMapLayerComponent);
      graphics_2d_utility.setColor(Color.BLUE);
      graphics_2d_utility.drawRect(0, 0, inputMapLayerComponent.getWidth() - 1, inputMapLayerComponent.getHeight() - 1);
      ///ArrayList<Point> map_points_of_interest_list = mMapPointsOfInterestList.get((JLayer) inputMapLayerComponent);
      for (NewWifiDataListener.WifiDataType type : NewWifiDataListener.WifiDataType.values())
      {
         ArrayList<Point> points_to_draw = new ArrayList<>();
         //Choose the color based on the algorithm
         switch (type)
         {
            case FINGERPRINTING:
               graphics_2d_utility.setColor(Color.MAGENTA);
               points_to_draw = this.mMapPointsOfInterestList.get(type);
               break;
            case WEIGHTED_CENTROID:
               graphics_2d_utility.setColor(Color.GREEN);
               points_to_draw = this.mMapPointsOfInterestList.get(type);
               break;
            case TRILATERATION:
               graphics_2d_utility.setColor(Color.DARK_GRAY);
               points_to_draw = this.mMapPointsOfInterestList.get(type);
               break;
            case TRIANGULATION:
               graphics_2d_utility.setColor(Color.ORANGE);
               points_to_draw = this.mMapPointsOfInterestList.get(type);
               break;
            case PATTERN_MATCHING:
               graphics_2d_utility.setColor(Color.RED);
               points_to_draw = this.mMapPointsOfInterestList.get(type);
               break;
            default:
               graphics_2d_utility.setColor(Color.BLACK);
               points_to_draw = this.mMapPointsOfInterestList.get(type);
               break;
         }//switch
         if (points_to_draw != null && points_to_draw.size() > 0)
         {
            int point_counter = 0;
            for (Point p : points_to_draw)
            {
               ++point_counter;
               if (point_counter < mNumberOfPointsToDisplay)
               {
                  graphics_2d_utility.fillOval(p.x - Constants.DEFAULT_POINT_OFFSET,
                                               p.y - Constants.DEFAULT_POINT_OFFSET,
                                               Constants.DEFAULT_POINT_CIRCLE_SIZE,
                                               Constants.DEFAULT_POINT_CIRCLE_SIZE);
                  this.mBoundsCheckOval = new Ellipse2D.Double(p.x - Constants.DEFAULT_RANGE_RING_OFFSET,
                                                               p.y - Constants.DEFAULT_RANGE_RING_OFFSET,
                                                               Constants.DEFAULT_RANGE_RING_SIZE,
                                                               Constants.DEFAULT_RANGE_RING_SIZE);
                  if (points_to_draw.size() == point_counter)
                  {
                     graphics_2d_utility.setStroke(new BasicStroke(Constants.DEFAULT_RANGE_RING_LINE_WIDTH));
                     graphics_2d_utility.draw(mBoundsCheckOval);
                  }//if
                  Font f = new Font("Dialog", Font.BOLD, Constants.DEFAULT_POINT_LABEL_FONT_SIZE);
                  graphics_2d_utility.setFont(f);
                  graphics_2d_utility.drawString(String.valueOf(point_counter), p.x, p.y - Constants.DEFAULT_POINT_LABEL_FONT_Y_OFFSET);
               }//if
            }//for
         }//if
      }//for

      paintTruthPathData(graphics_2d_utility, inputMapLayerComponent);
      paintTrainingData(graphics_2d_utility, inputMapLayerComponent);
      paintRouters(graphics_2d_utility, inputMapLayerComponent);
      paintCalibrationStartingPoint(graphics_2d_utility, inputMapLayerComponent);
      inputMapLayerComponent.repaint();

      graphics_2d_utility.dispose();
   }//paint

   /**
    * When a drawing event occurs, this occurs to ensure that
    * the routers are drawn appropriately on the map
    *
    * @param graphics2DUtility the 2D graphics drawing utility
    * @param mainComponent     the main component container
    */
   public void paintRouters(Graphics2D graphics2DUtility, JComponent mainComponent)
   {
      if (mRouterPointList != null && mRouterPointList.size() > 0)
      {
         graphics2DUtility.setColor(Color.BLUE);
         int image_number = 0;

         for (Point p : mRouterPointList)
         {
            if (this.mRouterImageBoundedRectangleList.size() < mRouterPointList.size())
            {
               Rectangle2D.Double rectangle2d = new Rectangle2D.Double(p.getX() - Constants.DEFAULT_ROUTER_POINT_X_OFFSET,
                                                                       p.getY() - Constants.DEFAULT_ROUTER_POINT_Y_OFFSET,
                                                                       Constants.DEFAULT_ROUTER_POINT_WIDTH,
                                                                       Constants.DEFAULT_ROUTER_POINT_HEIGHT);
               this.mRouterImageBoundedRectangleList.add(rectangle2d);
            }
            graphics2DUtility.drawImage(this.mRouterImageList.get(image_number),
                                        p.x - Constants.DEFAULT_ROUTER_POINT_X_OFFSET,
                                        p.y - Constants.DEFAULT_ROUTER_POINT_Y_OFFSET,
                                        mainComponent);
            ++image_number;
         }//for
      }//if
   }//paintRouters

   /**
    * When a drawing event occurs, this occurs to ensure that
    * the routers are drawn appropriately on the map
    *
    * @param graphics2DUtility the 2D graphics drawing utility
    * @param mainComponent     the main component container
    */
   public void paintTruthPathData(Graphics2D graphics2DUtility, JComponent mainComponent)
   {
      if (mTruthPathDataPointList != null && mTruthPathDataPointList.size() > 0)
      {
         graphics2DUtility.setColor(Color.BLACK);
         boolean paint_point = false;
         for (Point truth_point : mTruthPathDataPointList)
         {
            graphics2DUtility.fillOval(truth_point.x - Constants.DEFAULT_TRUTH_DATA_POINT_MIDPOINT,
                                       truth_point.y - Constants.DEFAULT_TRUTH_DATA_POINT_MIDPOINT,
                                       Constants.DEFAULT_TRUTH_DATA_POINT_SIZE, Constants.DEFAULT_TRUTH_DATA_POINT_SIZE);
         }//for
      }//if
   }//paintRouters

   /**
    * When a drawing event occurs, this occurs as an auxillary to ensure that
    * the routers are drawn appropriately on the map
    *
    * @param graphics2DUtility the 2D graphics drawing utility
    * @param mainComponent     the main component container
    */
   public void paintTrainingData(Graphics2D graphics2DUtility, JComponent mainComponent)
   {
      if (mTrainingDataPointList != null && mTrainingDataPointList.size() > 0)
      {
         graphics2DUtility.setColor(Color.BLUE);
         for (Point training_point : mTrainingDataPointList)
         {
            graphics2DUtility.fillOval(training_point.x - Constants.DEFAULT_TRAINING_DATA_POINT_MIDPOINT,
                                       training_point.y - Constants.DEFAULT_TRAINING_DATA_POINT_MIDPOINT,
                                       Constants.DEFAULT_TRAINING_DATA_POINT_SIZE, Constants.DEFAULT_TRAINING_DATA_POINT_SIZE);
         }//for
      }//if
   }//paintTrainingData

   /**
    * Draws the calibration start point on the map indicating the general start
    * area
    *
    * @param graphics2DUtility the 2D graphics drawing utility
    * @param mainComponent     the main component container
    */
   public void paintCalibrationStartingPoint(Graphics2D graphics2DUtility, JComponent mainComponent)
   {
      if (this.mCalibrationStartPoint != null)
      {
         graphics2DUtility.setColor(Color.ORANGE);
         graphics2DUtility.fillOval(this.mCalibrationStartPoint.x - Constants.DEFAULT_CALIBRATION_POINT_CIRCLE_OFFSET,
                                    this.mCalibrationStartPoint.y - Constants.DEFAULT_CALIBRATION_POINT_CIRCLE_OFFSET,
                                    Constants.DEFAULT_CALIBRATION_POINT_CIRCLE_SIZE,
                                    Constants.DEFAULT_CALIBRATION_POINT_CIRCLE_SIZE);
         this.mBoundsCheckOval = new Ellipse2D.Double(this.mCalibrationStartPoint.x - 125, this.mCalibrationStartPoint.y - 125, 250, 250);
         graphics2DUtility.setColor(Color.ORANGE);
         graphics2DUtility.setStroke(new BasicStroke(Constants.DEFAULT_RANGE_RING_LINE_WIDTH));
         graphics2DUtility.draw(mBoundsCheckOval);
      }//if
   }//paintCalibrationStartingPoint

   /**
    * Overridden new wifi data listener function. Acts on incoming wifi data by
    * adding to
    * the point of interest list
    *
    * @param newWifiPoint the new wifi data point
    * @param dataType     the data type (algorithm used to create it)
    */
   @Override
   public void newWifiData(Point newWifiPoint, NewWifiDataListener.WifiDataType dataType)
   {
      ArrayList<Point> point_list = this.mMapPointsOfInterestList.get(dataType);
      if (point_list == null)
      {
         point_list = new ArrayList<>();
      }//if
      point_list.add(newWifiPoint);
      if (this.mNumberOfPointsToDisplay == 0)
      {
         this.mNumberOfPointsToDisplay = point_list.size();
      }//if
      this.mMapPointsOfInterestList.put(dataType, point_list);
      //mMapPointsOfInterestList.training_point
   }//newWifiData

   /**
    * Overridden new wifi data listener function. Acts on direction of number of
    * points to display,
    * given the # of points and the algorithm type, stores the number of points
    * in
    * the class member variable for repainting
    *
    * @param nPoints  the targeted number of points to display
    * @param dataType the data type to check on number of points
    * @return the total number of points available for the given data type
    */
   @Override
   public int displayLastNPoints(int nPoints, NewWifiDataListener.WifiDataType dataType)
   {
      int total_points_available = 0;
      if (nPoints != 0)
      {
         this.mNumberOfPointsToDisplay = nPoints;
      }//if
      if (this.mMapPointsOfInterestList.get(dataType) != null)
      {
         total_points_available = this.mMapPointsOfInterestList.get(dataType).size();
      }//if
      return total_points_available;
   }//displayLastNPoints

   @Override
   public void newTruthPath(ArrayList<Point> newTruthPath)
   {
      this.mTruthPathDataPointList = newTruthPath;
   }

   @Override
   public void exportAlgorithmEstimatePoints(WifiDataType dataType)
   {
      ArrayList<Point> points_to_draw = new ArrayList<>();
      //Choose the color based on the algorithm
      switch (dataType)
      {
         case FINGERPRINTING:
            points_to_draw = this.mMapPointsOfInterestList.get(dataType);
            break;
         case WEIGHTED_CENTROID:
            points_to_draw = this.mMapPointsOfInterestList.get(dataType);
            break;
         case TRILATERATION:
            points_to_draw = this.mMapPointsOfInterestList.get(dataType);
            break;
         case TRIANGULATION:
            points_to_draw = this.mMapPointsOfInterestList.get(dataType);
            break;
         case PATTERN_MATCHING:
            points_to_draw = this.mMapPointsOfInterestList.get(dataType);
            break;
         default:
            points_to_draw = this.mMapPointsOfInterestList.get(dataType);
            break;
      }//switch
   }
}//MapDisplayPanel
