package frets.swing.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.awt.geom.Ellipse2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static java.awt.GraphicsDevice.WindowTranslucency.*;

public class ShapedWindowDemo extends JFrame {
    public ShapedWindowDemo() {
        super("ShapedWindow");
        setLayout(new GridBagLayout());

        addComponentListener(new ComponentAdapter() {
            // It is best practice to set the window's shape in
            // the componentResized method.  Then, if the window
            // changes size, the shape will be correctly recalculated.
            @Override
            public void componentResized(ComponentEvent e) {
                setShape(new Ellipse2D.Double(0,0,getWidth(),getHeight()));
            }
        });

        addWindowFocusListener(new WindowFocusListener() {			
			@Override
			public void windowLostFocus(WindowEvent e) {
                System.out.println("The ellipse lost the focus");
			}
			
			@Override
			public void windowGainedFocus(WindowEvent e) {
                System.out.println("The ellipse gained the focus");
			}
		}
        );
        
        addMouseListener( new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
			}			
			@Override
			public void mousePressed(MouseEvent e) {
			}			
			@Override
			public void mouseExited(MouseEvent e) {
			}			
			@Override
			public void mouseEntered(MouseEvent e) {
			}			
			@Override
			public void mouseClicked(MouseEvent e) {
                System.out.println("The ellipse was clicked at " + e.getX() + "," + e.getY());
			}
		}
        );

        setUndecorated(true);
        setSize(300,200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JButton button = new JButton("I am a Button");
        button.addActionListener(new ActionListener() {        	 
            public void actionPerformed(ActionEvent e) {
                System.out.println("You clicked the button");
            }
        });
        add( button );
        
    }

    public static void main(String[] args) {
        // Determine what the GraphicsDevice can support.
        GraphicsEnvironment ge = 
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        final boolean isTranslucencySupported = 
            gd.isWindowTranslucencySupported(TRANSLUCENT); // required for uniform transparent windows
        if (!isTranslucencySupported) {
            System.out.println("Translucency is not supported.");
        }

        final boolean isPerPixelTranslucencySupported = 
                gd.isWindowTranslucencySupported(PERPIXEL_TRANSLUCENT); // required for transparent window fades
        if (!isPerPixelTranslucencySupported) {
            System.out.println("Per Pixel Translucency is not supported.");
        }
        
        // If shaped windows aren't supported, exit.
        final boolean isWindowTranslucencySupported = gd.isWindowTranslucencySupported(PERPIXEL_TRANSPARENT);
        System.out.println( "Window translucency supported=" + isWindowTranslucencySupported );        
        if (!isWindowTranslucencySupported) {
            System.exit(0);
        }

        // Create the GUI on the event-dispatching thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ShapedWindowDemo sw = new ShapedWindowDemo();

                // Set the window to 70% translucency, if supported.
                if (isTranslucencySupported) {
                    sw.setOpacity(0.7f);
                }

                // Display the window.
                sw.setVisible(true);
            }
        });
    }
}
