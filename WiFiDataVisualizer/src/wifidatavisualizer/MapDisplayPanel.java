/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import java.io.IOException;
import java.util.ArrayList;
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
 *
 * @author James Licata
 */
public class MapDisplayPanel
        extends LayerUI<JLabel>
        implements NewWifiDataListener
{
   private final Map<NewWifiDataListener.WifiDataType, ArrayList<Point>> mMapPointsOfInterestList;
   private final ArrayList<Point> mRouterPointList;
   private final ArrayList<Image> mRouterImageList;
   private final ArrayList<Rectangle2D> mRouterImageBoundedRectangleList;
   private final ArrayList<Point> mTrainingDataPointList;
   public boolean mStartPointChosen = false;
   private Point mCalibrationStartPoint = new Point(500, 500);
   private Ellipse2D mBoundsCheckOval = null;
   JFrame mParentFrame;
   int mNumberOfPointsToDisplay = 0;

   public MapDisplayPanel(ArrayList<Point> trainingDataPointList, ArrayList<Point> routerPointList, ArrayList<String> routerResourcePath)
   {
      mMapPointsOfInterestList = new WeakHashMap<>(25);
      mRouterImageList = new ArrayList<>();
      mRouterPointList = routerPointList;
      mTrainingDataPointList = trainingDataPointList;
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
   }//MapDisplayPanel

   @Override
   public void installUI(JComponent c)
   {
      System.out.println("install");
      super.installUI(c);
      JLayer layer = (JLayer) c;
      layer.setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK);
   }//installUI

   @Override
   public void uninstallUI(JComponent c)
   {
      super.uninstallUI(c);
      mMapPointsOfInterestList.clear();
      this.mMapPointsOfInterestList.clear();
      this.mRouterImageBoundedRectangleList.clear();
      this.mRouterPointList.clear();
   }//uninstallUI

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
         }//if
         layer.repaint();

      }//if
      if (SwingUtilities.isLeftMouseButton(mouseEvent))
      {
         Point test_point = mouseEvent.getPoint();
         for (int i = 0; i < mRouterPointList.size(); ++i)
         {
            if (this.mRouterImageBoundedRectangleList.get(i).contains(test_point))
            {
               int router_index = 1200 + i;
               JOptionPane.showMessageDialog(null, "Router: CiscoLinksysE" + router_index + " X,Y: ("
                                             + mRouterPointList.get(i).x + "," + mRouterPointList.get(i).y + ")");
            }//if
         }//for
      }//if
   }//processMouseEvent

   public ArrayList<Point> getRouterPointList()
   {
      return this.mRouterPointList;
   }

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

   @Override
   public void paint(Graphics g, JComponent c)
   {
      Graphics2D graphics_2d_utility = (Graphics2D) g.create();
      super.paint(graphics_2d_utility, c);
      graphics_2d_utility.setColor(Color.BLUE);
      graphics_2d_utility.drawRect(0, 0, c.getWidth() - 1, c.getHeight() - 1);
      ///ArrayList<Point> map_points_of_interest_list = mMapPointsOfInterestList.get((JLayer) c);
      for (NewWifiDataListener.WifiDataType type : NewWifiDataListener.WifiDataType.values())
      {
         ArrayList<Point> points_to_draw = new ArrayList<Point>();
         //Choose the color based on the algorithm
         switch (type)
         {
            case FINGERPRINTING:
               graphics_2d_utility.setColor(Color.MAGENTA);
               points_to_draw = this.mMapPointsOfInterestList.get(type);
               break;
            case TRIANGULATION:
               graphics_2d_utility.setColor(Color.GREEN);
               points_to_draw = this.mMapPointsOfInterestList.get(type);
               break;
            case TRILATERATION:
               graphics_2d_utility.setColor(Color.DARK_GRAY);
               points_to_draw = this.mMapPointsOfInterestList.get(type);
               break;
            default:
               graphics_2d_utility.setColor(Color.RED);
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
                  graphics_2d_utility.fillOval(p.x - 16, p.y - 16, 32, 32);
                  this.mBoundsCheckOval = new Ellipse2D.Double(p.x - 125, p.y - 125, 250, 250);
                  if (points_to_draw.size() == point_counter)
                  {
                     graphics_2d_utility.setStroke(new BasicStroke(5));
                     graphics_2d_utility.draw(mBoundsCheckOval);
                  }//if
                  Font f = new Font("Dialog", Font.BOLD, 24);
                  graphics_2d_utility.setFont(f);
                  graphics_2d_utility.drawString(String.valueOf(point_counter), p.x, p.y - 10);
               }//if
            }//for
         }//if
      }//for

      paintTrainingData(graphics_2d_utility, c);
      paintRouters(graphics_2d_utility, c);
      paintCalibrationStartingPoint(graphics_2d_utility, c);

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
               Rectangle2D.Double rectangle2d = new Rectangle2D.Double(p.getX() - 21, p.getY() - 12, 52, 31);
               this.mRouterImageBoundedRectangleList.add(rectangle2d);
            }
            graphics2DUtility.drawImage(this.mRouterImageList.get(image_number), p.x - 21, p.y - 12, mainComponent);
            ++image_number;
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
         for (Point p : mTrainingDataPointList)
         {
            graphics2DUtility.fillOval(p.x - 8, p.y - 8, 16, 16);
         }//for
      }//if
   }//paintTrainingData

   public void paintCalibrationStartingPoint(Graphics2D graphics2DUtility, JComponent mainComponent)
   {
      if (this.mCalibrationStartPoint != null)
      {
         graphics2DUtility.setColor(Color.ORANGE);
         graphics2DUtility.fillOval(this.mCalibrationStartPoint.x - 8, this.mCalibrationStartPoint.y - 8, 16, 16);
         this.mBoundsCheckOval = new Ellipse2D.Double(this.mCalibrationStartPoint.x - 125, this.mCalibrationStartPoint.y - 125, 250, 250);
         graphics2DUtility.setColor(Color.RED);
         graphics2DUtility.setStroke(new BasicStroke(5));
         graphics2DUtility.draw(mBoundsCheckOval);
      }//if
   }//paintCalibrationStartingPoint

   @Override
   public void newWifiData(Point newWifiPoint, NewWifiDataListener.WifiDataType dataType)
   {
      ArrayList<Point> point_list = this.mMapPointsOfInterestList.get(dataType);
      if (point_list == null)
      {
         point_list = new ArrayList<Point>();
      }//if
      point_list.add(newWifiPoint);
      if (this.mNumberOfPointsToDisplay == 0)
      {
         this.mNumberOfPointsToDisplay = point_list.size();
      }//if
      this.mMapPointsOfInterestList.put(dataType, point_list);
      //mMapPointsOfInterestList.p
   }//newWifiData

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
}//MapDisplayPanel
