
import database.SQLLiteConnection;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.LayerUI;
import wifidatavisualizer.MapDisplayPanel;

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
{

   JLabel mIndoorMap = new JLabel();
   //SQLLite Connection for connecting to the training data set
   SQLLiteConnection mSqlLiteConnection = new SQLLiteConnection();
   LayerUI<JLabel> mMapDisplayPanel;
   JLayer<JLabel> mMapDisplayLayer;

   /**
    * Creates new form MapView
    */
   public MapView()
   {
      initComponents();
      this.setLayout(new GridBagLayout());
      ArrayList<String> router_resource_path = new ArrayList<>();
      for (int i = 0; i < 4; ++i)
      {
         String resource_string = "resources/router120" + i + ".PNG";
         router_resource_path.add((getClass().getResource(resource_string)).getPath());
      }//for
      String url_to_database = "jdbc:sqlite:" + this.getClass().getResource("").getPath() + "/resources/bld2_ap_data.db";
      mSqlLiteConnection.connect(url_to_database, "bld2_ap_data");
      mMapDisplayPanel = new MapDisplayPanel(mSqlLiteConnection.loadTrainingPointLocations(), mSqlLiteConnection.loadRouterPointLocations(), router_resource_path);
      mIndoorMap.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Bld2_ULQuadrantLabelsRemoved.PNG"))); // NOI18N
      mMapDisplayLayer = new JLayer<>(this.mIndoorMap, mMapDisplayPanel);
      this.add(mMapDisplayLayer);
      this.pack();

   }//MapView

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

      jMenuBar1 = new javax.swing.JMenuBar();
      jMenu1 = new javax.swing.JMenu();
      mSelectMapViewItem = new javax.swing.JMenuItem();

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
      jMenu1.add(mSelectMapViewItem);

      jMenuBar1.add(jMenu1);

      setJMenuBar(jMenuBar1);

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGap(0, 1400, Short.MAX_VALUE)
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGap(0, 929, Short.MAX_VALUE)
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
            }
         }
      }
      catch (ClassNotFoundException ex)
      {
         java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
      }
      catch (InstantiationException ex)
      {
         java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
      }
      catch (IllegalAccessException ex)
      {
         java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
      }
      catch (javax.swing.UnsupportedLookAndFeelException ex)
      {
         java.util.logging.Logger.getLogger(MapView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
      }
      //</editor-fold>

      /* Create and display the form */
      java.awt.EventQueue.invokeLater(new Runnable()
      {
         public void run()
         {
            new MapView().setVisible(true);
         }
      });
   }

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JMenu jMenu1;
   private javax.swing.JMenuBar jMenuBar1;
   private javax.swing.JMenuItem mSelectMapViewItem;
   // End of variables declaration//GEN-END:variables
}
