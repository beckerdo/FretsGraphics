package frets.swing.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

@SuppressWarnings("serial")
public class NoteSpinnerDemo extends JPanel
    implements ActionListener, ChangeListener {

    public NoteSpinnerDemo() {
        super(new BorderLayout());

        NoteSpinner noteEditor = new NoteSpinner();
        add( noteEditor, BorderLayout.CENTER);
    }

    /**
     * Create the GUI and show it. 
     * For thread safety, this should be invoked from the event-dispatching thread.
     */
    protected static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("NoteEditorlDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new NoteSpinnerDemo();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        // Display the window.
        frame.setSize( 100, 60 );
        // frame.pack();
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
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	}
}