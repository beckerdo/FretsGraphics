package frets.swing.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

public class NotePanelDemo extends JPanel
    implements ActionListener, ChangeListener {
    public JLabel banner;

    public NotePanelDemo() {
        super(new BorderLayout());

        //Set up banner to use as custom preview panel
        banner = new JLabel("Welcome to the Tutorial Zone!",JLabel.CENTER);
        banner.setOpaque(true);
        banner.setFont(new Font("SansSerif", Font.BOLD, 24));
        banner.setPreferredSize(new Dimension(200, 65));

        JPanel bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.add(banner, BorderLayout.CENTER);
        bannerPanel.setBorder(BorderFactory.createTitledBorder("Banner"));

        add(bannerPanel, BorderLayout.NORTH );
        add(new NotePanel(), BorderLayout.CENTER);
    }


    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("NotePanelDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new NotePanelDemo();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }


	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
}
