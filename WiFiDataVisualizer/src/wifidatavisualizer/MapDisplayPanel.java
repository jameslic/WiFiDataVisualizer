/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wifidatavisualizer;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
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
{
   private final Map<JLayer, ArrayList<Point>> mMapPointsOfInterestList;
   private final ArrayList<Point> mRouterPointList;
   private final ArrayList<Image> mRouterImageList;
   private final ArrayList<Rectangle2D> mRouterImageBoundedRectangleList;
   private final ArrayList<Point> mTrainingDataPointList;

   public MapDisplayPanel(ArrayList<Point> trainingDataPointList)
   {
      mMapPointsOfInterestList = new WeakHashMap<>(25);
      mRouterImageList = new ArrayList<>();
      mRouterPointList = new ArrayList<>();
      mTrainingDataPointList = trainingDataPointList;
      mRouterImageBoundedRectangleList = new ArrayList<>();
      mRouterPointList.add(new Point(1131, 171));
      mRouterPointList.add(new Point(445, 232));
      mRouterPointList.add(new Point(559, 529));
      mRouterPointList.add(new Point(1076, 520));
      try
      {
         mRouterImageList.add(ImageIO.read(new File("C:\\Workspace\\Thesis\\WiFiDataVisualizer\\src\\router1200.PNG")));
         mRouterImageList.add(ImageIO.read(new File("C:\\Workspace\\Thesis\\WiFiDataVisualizer\\src\\router1201.PNG")));
         mRouterImageList.add(ImageIO.read(new File("C:\\Workspace\\Thesis\\WiFiDataVisualizer\\src\\router1202.PNG")));
         mRouterImageList.add(ImageIO.read(new File("C:\\Workspace\\Thesis\\WiFiDataVisualizer\\src\\router1203.PNG")));
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
      mMapPointsOfInterestList.remove((JLayer) c);
      this.mMapPointsOfInterestList.clear();
      this.mRouterImageBoundedRectangleList.clear();
      this.mRouterPointList.clear();
   }//uninstallUI

   @Override
   protected void processMouseEvent(MouseEvent mouseEvent, JLayer<? extends JLabel> layer)
   {
      if (mouseEvent.getID() == MouseEvent.MOUSE_CLICKED && SwingUtilities.isRightMouseButton(mouseEvent))
      {
         ArrayList<Point> points = mMapPointsOfInterestList.get(layer);
         if (points == null)
         {
            points = new ArrayList<>();
            mMapPointsOfInterestList.put(layer, points);
         }//if
         Point mouse_point = mouseEvent.getPoint();
         mouse_point = SwingUtilities.convertPoint(mouseEvent.getComponent(), mouse_point, layer);
         JOptionPane.showMessageDialog(null, "X: " + mouse_point.x + " Y: " + mouse_point.y);
         points.add(mouse_point);
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

   @Override
   public void paint(Graphics g, JComponent c)
   {
      Graphics2D graphics_2d_utility = (Graphics2D) g.create();
      super.paint(graphics_2d_utility, c);
      graphics_2d_utility.setColor(Color.BLUE);
      graphics_2d_utility.drawRect(0, 0, c.getWidth() - 1, c.getHeight() - 1);
      ArrayList<Point> map_points_of_interest_list = mMapPointsOfInterestList.get((JLayer) c);
      if (map_points_of_interest_list != null && map_points_of_interest_list.size() > 0)
      {
         graphics_2d_utility.setColor(Color.RED);
         for (Point p : map_points_of_interest_list)
         {
            graphics_2d_utility.fillOval(p.x - 4, p.y - 4, 8, 8);
         }//for
      }//if
      paintTrainingData(graphics_2d_utility, c);
      paintRouters(graphics_2d_utility, c);

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
         int image_number = 0;

         for (Point p : mTrainingDataPointList)
         {
            graphics2DUtility.fillOval(p.x - 8, p.y - 8, 16, 16);
         }//for
      }//if
   }//paintRouters
}//MapDisplayPanel