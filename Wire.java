//-------------------------------------------------------------------
//
//  Ryan Castillo
//  Computer Graphics
//  Fall 2002
//
//-------------------------------------------------------------------

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;
import java.io.*;

public class Wire extends Frame implements ActionListener, MouseListener, Runnable
  {
  CvWire Picture;
  Dialog dialog;
  Frame f;
  Label eyeLabel, XLabel, YLabel, ZLabel, BlankLabel;
  TextField eyeText, XText, YText, ZText;
  Panel DialogPanel, Left, Middle, Right;
  Button drawButton, resetButton;
  Choice Transform;
  boolean threadSuspended = true;

  //-----------------------------------------------------------------
  //
  //  Main method, that calls the GUI constructor and initializes
  //  the thread for animation.
  //
  //-----------------------------------------------------------------

  public static void main (String [] args) throws IOException
    {

    Wire Pic = new Wire ();
    Thread t = new Thread (Pic);
    t.start();
    Pic.show();
    }//end of main

    //-----------------------------------------------------------------
    //
    //  Wire constructor, that constructs the dialog box for input and
    //  calls the canvas constructor.
    //
    //-----------------------------------------------------------------

    Wire () throws IOException
      {

      super ("Wire");
      addWindowListener (new WindowAdapter()
         {public void windowClosing (WindowEvent e){System.exit(0);}
          public void windowActivated (WindowEvent e){dialog.toFront();}});

      setSize(725, 725);
      Picture = new CvWire ();
      Picture.addMouseListener(this);

      add(Picture);

      Transform = new Choice ();
      Transform.addItem(new String ("Scale"));
      Transform.addItem(new String ("Translation"));
      Transform.addItem(new String ("Rotate"));

      eyeLabel = new Label ("Eye");
      eyeText = new TextField (0);

      XLabel = new Label ("X");
      XText = new TextField (0);

      YLabel = new Label ("Y");
      YText = new TextField (0);

      ZLabel = new Label ("Z");
      ZText = new TextField (0);

      BlankLabel = new Label ();

      drawButton = new Button ("Draw");
      drawButton.addActionListener(this);

      resetButton = new Button ("Reset");
      resetButton.addActionListener(this);

      Left = new Panel (new GridLayout (3, 0));
      Left.add (BlankLabel);
      Left.add (Transform);
      Left.add (eyeLabel);

      Middle = new Panel (new GridLayout (0, 3));
      Middle.add (XLabel);
      Middle.add (YLabel);
      Middle.add (ZLabel);
      Middle.add (XText);
      Middle.add (YText);
      Middle.add (ZText);
      Middle.add (eyeText);

      Right = new Panel (new GridLayout (2, 0));
      Right.add (drawButton);
      Right.add (resetButton);

      DialogPanel = new Panel();
      DialogPanel.add (Left);
      DialogPanel.add (Middle);
      DialogPanel.add (Right);

      f = new Frame ();
      dialog = new Dialog (f, "Control Panel");
      dialog.addWindowListener (new WindowAdapter()
       {public void windowClosing (WindowEvent e)
      {((Window)e.getSource()).setVisible(false);
      ((Window)e.getSource()).dispose();
      System.exit(0);
      }});

      dialog.add (DialogPanel);
      dialog.pack();
      dialog.show();
      }//end of Wire

  public void actionPerformed (ActionEvent ae)
    {

    //-----------------------------------------------------------------
    //
    //  Defined actions that are performed when the draw button is
    //  pressed.
    //
    //-----------------------------------------------------------------

    if (ae.getSource() == drawButton)
      {
      if (eyeText.getText().compareTo("") != 0)
        Picture.updateValues (Double.parseDouble(eyeText.getText()));

      Picture.drawPic (Transform.getSelectedItem(), XText.getText(),
                       YText.getText(), ZText.getText());

      eyeText.setText ("");
      XText.setText ("");
      YText.setText ("");
      ZText.setText ("");
      }

    //-----------------------------------------------------------------
    //
    //  Defined actions that are performed when the reset button is
    //  pressed.
    //
    //-----------------------------------------------------------------
                                                         
    if (ae.getSource() == resetButton)
      Picture.resetPic();
      
    }

  //-----------------------------------------------------------------
  //
  //  Has the animation perform when the canvas is clicked with the
  //  mouse.
  //
  //-----------------------------------------------------------------

  public synchronized void mousePressed (MouseEvent e)
    {

    e.consume();
    threadSuspended = !threadSuspended;

    if (!threadSuspended)
      notify();

    }//end of mousePressed

  public void mouseClicked (MouseEvent e){}
  public void mouseReleased (MouseEvent e){}
  public void mouseEntered (MouseEvent e){}
  public void mouseExited (MouseEvent e){}

  public void run()
    {

    while (true)
      {

      try
        {

        Thread.currentThread().sleep(40);
        synchronized(this)
          {

          while (threadSuspended)
            wait();

          }
        }

      catch (InterruptedException e){}

      Picture.rotateZ(2);
      Picture.repaint();

      }//end of while
    }//end of run

  }//end of Wire class

//-----------------------------------------------------------------
//
//  Class for constructing the canvas.
//
//-----------------------------------------------------------------

class CvWire extends Canvas
  {

  private double [][] tvertex, oldvertex;
  private int [][] tedge;
  private double Eye = 500.0;
        
  CvWire () throws IOException
    {
    ReadFile f = new ReadFile ("tiefighter.dat");

    tvertex = f.getVertices ();

    oldvertex = new double [tvertex.length][tvertex[0].length];

    for (int i = 0; i < tvertex.length; i++)
      for (int j = 0; j < tvertex[i].length; j++)
        oldvertex[i][j] = tvertex[i][j];
      
    tedge = f.getEdges ();
    }

  //-----------------------------------------------------------------
  //
  //  Method that paints the wireframe image on the canvas.
  //
  //-----------------------------------------------------------------

  public void paint (Graphics g)
    {
    Dimension d = getSize();

    int maxX = d.width - 1, maxY = d.height - 1,
        originX = maxX / 2,
        originY = maxY / 2;

    double t = 0.0;

    double View [][] = new double [tvertex.length][2];

    for (int i = 0; i < tvertex.length; i++)
      for (int j = 0; j < 3; j++)
        {

        if (j == 0)
          t = 1.0 / (1.0 - (tvertex[i][j] / Eye));
        else
          View[i][j - 1] = t * tvertex [i][j];
        }

    for (int i = 0; i < tedge.length; i++)
      g.drawLine ((int) Math.round (originX + View[tedge[i][0] - 1][0]),
                  (int) Math.round (originY - View[tedge[i][0] - 1][1]),
                  (int) Math.round (originX + View[tedge[i][1] - 1][0]),
                  (int) Math.round (originY - View[tedge[i][1] - 1][1]));
    }//end of paint

  public void updateValues (double e)
    {
    Eye = e;
    repaint();
    }

  public void drawPic (String transType, String x, String y, String z)
    {
    double a = 0, b = 0, c = 0;

    if (transType.compareTo("Scale") == 0)
      {
      if (x.compareTo("") == 0)
        a = 1;
      else
        a = Double.parseDouble (x);

      if (y.compareTo("") == 0)
        b = 1;
      else
        b = Double.parseDouble (y);

      if (z.compareTo("") == 0)
        c = 1;
      else
        c = Double.parseDouble (z);
      
      scalePic (a, b, c);
      }//end of scalePic if

    if (transType.compareTo("Translation") == 0)
      {
      if (x.compareTo("") == 0)
        a = 0;
      else
        a = Double.parseDouble (x);

      if (y.compareTo("") == 0)
        b = 0;
      else
        b = Double.parseDouble (y);

      if (z.compareTo("") == 0)
        c = 0;
      else
        c = Double.parseDouble (z);
      
      transPic (a, b, c);
      }//end of transPic if

    if (transType.compareTo("Rotate") == 0)
      {
      if (x.compareTo("") != 0)
        rotateX (Double.parseDouble (x));

      if (y.compareTo("") != 0)
        rotateY (Double.parseDouble (y));


      if (z.compareTo("") != 0)
        rotateZ (Double.parseDouble (z));
      }//end of Rotate if

    repaint();
    }

  public void scalePic (double sx, double sy, double sz)
    {

    for (int i = 0; i < tvertex.length; i++)
      {
      tvertex[i][0] = tvertex[i][0] * sx;
      tvertex[i][1] = tvertex[i][1] * sy;
      tvertex[i][2] = tvertex[i][2] * sz;
      }

    }//end of scalePic

  public void transPic (double sx, double sy, double sz)
    {

    for (int i = 0; i < tvertex.length; i++)
      {
      tvertex[i][0] = tvertex[i][0] + sx;
      tvertex[i][1] = tvertex[i][1] + sy;
      tvertex[i][2] = tvertex[i][2] + sz;
      }

    }//end of transPic

  public void rotateX (double rx)
    {
    double radAngle = (rx * Math.PI) / 180,
    newX = 0,
    newY = 0,
    newZ = 0;

    for (int i = 0; i < tvertex.length; i++)
      {
      newX = tvertex[i][0] * 1;
      newY = (tvertex[i][1] * Math.cos (radAngle)) +
             (tvertex[i][2] * Math.sin (radAngle));
      newZ = (tvertex[i][1] * ( - Math.sin (radAngle))) +
             (tvertex[i][2] * Math.cos (radAngle));

      tvertex[i][0] = newX;
      tvertex[i][1] = newY;
      tvertex[i][2] = newZ;
      }

    }

  public void rotateY (double ry)
    {
    double radAngle = (ry * Math.PI) / 180,
    newX = 0,
    newY = 0,
    newZ = 0;

    for (int i = 0; i < tvertex.length; i++)
      {
      newX = (tvertex[i][0] * Math.cos (radAngle)) +
             (tvertex[i][2] * (- Math.sin (radAngle)));
      newY = tvertex[i][1];
      newZ = (tvertex[i][0] * Math.sin (radAngle)) +
             (tvertex[i][2] * Math.cos (radAngle));

      tvertex[i][0] = newX;
      tvertex[i][1] = newY;
      tvertex[i][2] = newZ;
      }
                                           
    }

  public void rotateZ (double rz)
    {
    double radAngle = (rz * Math.PI) / 180,
    newX = 0,
    newY = 0,
    newZ = 0;

    for (int i = 0; i < tvertex.length; i++)
      {
      newX = (tvertex[i][0] * Math.cos (radAngle)) +
             (tvertex[i][1] * Math.sin (radAngle));
      newY = (tvertex[i][0] * (- Math.sin (radAngle))) +
             (tvertex[i][1] * Math.cos (radAngle));
      newZ = tvertex[i][2];

      tvertex[i][0] = newX;
      tvertex[i][1] = newY;
      tvertex[i][2] = newZ;
      }
      
    }

  public void resetPic ()
    {
    Eye = 500.0;

    for (int i = 0; i < tvertex.length; i++)
      for (int j = 0; j < tvertex[i].length; j++)
        tvertex[i][j] = oldvertex[i][j];

    repaint();
    }

  }//end of CvWire

//-------------------------------------------------------------------
//
//  Class for reading text files.
//
//-------------------------------------------------------------------

class ReadFile
{

  private BufferedReader inFile;
  private StringTokenizer tokenizer;
  private String line;
  private int Num_Vert, Num_Edge;
  private int Edge[][];
  private double Vertex [][];

  ReadFile (String file) throws IOException
  {

    inFile = new BufferedReader (new FileReader (file));

    //***************************************************************

    //---------------------------------------------------------------
    //
    //  Code for reading the coordinates of the vertices.
    //
    //---------------------------------------------------------------

    Num_Vert = Integer.parseInt (inFile.readLine());

    Vertex = new double [Num_Vert][3];

    for (int i = 0; i < Num_Vert; i++)
      {

      line = inFile.readLine();
      tokenizer = new StringTokenizer (line);

      for (int j = 0; j < 3; j++)
        {

        Vertex [i][j] = Double.parseDouble (tokenizer.nextToken());
        }//end of j for

      }//end of i for

    //***************************************************************

    //---------------------------------------------------------------
    //
    //  Code for reading the edges to connect the vertices
    //
    //---------------------------------------------------------------

    Num_Edge = Integer.parseInt (inFile.readLine());

    Edge = new int [Num_Edge][2];

    for (int i = 0; i < Num_Edge; i++)
      {

      line = inFile.readLine();
      tokenizer = new StringTokenizer (line);

      for (int j = 0; j < 2; j++)
        {

        Edge [i][j] = Integer.parseInt (tokenizer.nextToken());
        }//end of j for

      }//end of i for

    //***************************************************************

    inFile.close();
  }//end of ReadFile Constructor

  public int [][] getEdges ()
  {
    return Edge;

  }

  public double [][] getVertices ()
  {
    return Vertex;
  }
}
