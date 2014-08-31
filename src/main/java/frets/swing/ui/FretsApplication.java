package frets.swing.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;

import swingextensions.swingx.app.Application;
import swingextensions.ui.UIFactory;

/**
 * Swing application information and startup.
 * Does very little other than start up the Controller.
 */
public class FretsApplication extends Application {
	public static final String FRETS_APPLICATION = "frets.swing.ui.application";
	public static final String FRETS_MODEL_STORE_XML = "/frets/swing/ui/modelStore.xml";
	public static final String FRETS_IMAGES = "/frets/images/";

    // Controller in terms of MVC.
    private frets.swing.ui.Controller controller;
    private JFrame frame;

    public static FretsApplication getInstance() {
        return (FretsApplication) Application.getInstance();
    }
    
    public JFrame getFrame() {
        return frame;
    }
    
    // Overridden to return the name of the Application
    @Override
    public String getApplicationName() {
        return getResourceAsString("appName");
    }

    /**
     * Returns the key for loading the resources for the Application.
     */
    @Override
    public String getResourceBundleName() {
        return FRETS_APPLICATION;
    }

    protected void installLookAndFeel() {
        super.installLookAndFeel();
        // Register the factory to get notified when UIs are created. This
        // is used to enable the cut/copy/paste actions.
        UIManager.put("ClassLoader", getClass().getClassLoader());
        UIManager.put("TextFieldUI", UIFactory.class.getName());
        UIManager.put("TextPaneUI", UIFactory.class.getName());
    }

    // Overridden to create the UI and show it.
    protected void init() {
    	String frameTitle = getResourceAsString("frame.title");
        frame = new JFrame( frameTitle );
    	String frameIconLocation = APP_RESOURCES_PREFIX + getResourceAsString("frame.iconLocation");
        File test = new File( frameIconLocation );
        if (!test.canRead()) {
        	System.out.println( "Frame icon \"" + frameIconLocation + "\" is not readable.");
        	System.out.println( "   absolutePath is " + test.getAbsolutePath() );
        } 
        frame.setIconImage(new ImageIcon( frameIconLocation ).getImage());
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
        controller = new Controller(frame);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    // Overridden to delegate to the controller.
    protected boolean canExit() {
        return controller.canExit();
    }
        
    public static void main(String[] args) {
    	
    	System.out.println( "FretsApplication starting at " + new Date() );
        try {
            // Register various properties for OS X
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            // System.setProperty("com.apple.mrj.application.apple.menu.about.name",
        } catch (SecurityException e) {
        }
        new FretsApplication().start();
    }
}