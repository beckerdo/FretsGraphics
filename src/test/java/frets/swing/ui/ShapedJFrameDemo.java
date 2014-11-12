package frets.swing.ui;


import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;

import javax.swing.*;

public class ShapedJFrameDemo extends JFrame {
	public ShapedJFrameDemo() {
		setTitle("Transparent JFrame Demo");
		setLayout(new GridBagLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setUndecorated(true);
		setVisible(true);
		setOpacity(0.8f);

		// Randomly pick a shape
		Random random = new Random();
		Shape [] shapes = new Shape[] {
//			new RoundRectangle2D.Double(0,0,getWidth(),getHeight(),20,40),
//			new Ellipse2D.Double(0,0,400,400),
//			new QuadCurve2D.Double(0,0,400,50,400,400),
//			new CubicCurve2D.Double(0,0,500,5,200,0,500,500),
//			new Arc2D.Double(new Rectangle2D.Double(0,0,500,500),90,270,Arc2D.PIE), // three fourths circle
//			new Arc2D.Double(new Rectangle2D.Double(0, 0, 500, 500), 90, 360, Arc2D.PIE), // round cirlce
			new Polygon( new int [] { 166, 332, 332, 500, 500, 332, 332, 166, 166,   0,   0, 166 }, 
					     new int [] {   0,   0, 166, 166, 332, 332, 500, 500, 332, 332, 166, 166 }, 
					     12 ), // plus sign
		};
		
		Shape shape = shapes[ random.nextInt( shapes.length )];
		setShape( shape );

		JLabel label = new JLabel("I am a label in a shaped frame.");
		add(label);

		setSize(500, 500);
		setLocationRelativeTo(null);
		
        addMouseListener( new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {}		
			@Override
			public void mousePressed(MouseEvent e) {}			
			@Override
			public void mouseExited(MouseEvent e) {}			
			@Override
			public void mouseEntered(MouseEvent e) {}			
			@Override
			public void mouseClicked(MouseEvent e) {
                System.out.println("The window was clicked at " + e.getX() + "," + e.getY());
			}
		});
	}

	public static void main(String args[]) {
		new ShapedJFrameDemo();
	}
}