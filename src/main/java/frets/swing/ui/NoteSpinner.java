package frets.swing.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpinnerListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import swingextensions.beansx.PropertyChange;
import frets.main.Note;

/**
 * A component for editing and display of <@link Note}.
 * The note values and octave are controlled by spinners
 * Provides a keyboard for note and an octave spinner.
 */
public class NoteSpinner extends JPanel implements ActionListener, ChangeListener, PropertyChange {
	private static final long serialVersionUID = 1L;
	public static final String EVENT_NAME="Note";
    
   	public static final String [] values = new String [] {
        	   Note.C.toString(), Note.Cs.toString(), Note.D.toString(), Note.Ds.toString(), Note.E.toString(),
        	   Note.F.toString(), Note.Fs.toString(), Note.G.toString(), Note.Gs.toString(), Note.A.toString(), Note.As.toString(), Note.B.toString() 
        			
    };
    protected Note note; // model data
    
    protected JSpinner value;
    protected JSpinner octave;

    protected PropertyChangeSupport handler;

	public NoteSpinner() {
		initGUI();
	}

	protected synchronized void initGUI() {
        note = new Note( 2, Note.Name.C.getValue() ); // middle C

        handler = new PropertyChangeSupport( note );

        LayoutManager layout = new GridLayout( 1, 2 );
        // LayoutManager layout = new BorderLayout( 1, 2 );
       	this.setLayout(layout);

        SpinnerModel valueModel = new SpinnerListModel( Arrays.asList( values ) );
        value = new JSpinner( valueModel );
        value.setValue( values[ note.getValue() ]);        
        value.addChangeListener( this );
     	add( value );
     	
        // public SpinnerNumberModel(int value, int minimum, int maximum, int stepSize) 
        SpinnerModel octaveModel = new SpinnerNumberModel(0, 0, 6, 1);
        octave = new JSpinner( octaveModel );
        octave.setValue( note.getOctave() );        
        octave.addChangeListener( this );
     	add( octave );		
	}
	
	public final Note getNote() {
		return note;
	}

	public void setNote(Note note) {
		this.note = note;
		if ( null != note ) {
			// Update UI
			value.setValue( values[ note.getValue() ]  );
			octave.setValue( note.getOctave() );
        }
	}

	/** Listen to changes in buttons pressed. */
	@Override
	public void actionPerformed(ActionEvent event) {
		// Object source = event.getSource();
		String command = event.getActionCommand();
        // System.out.println( "actionPerformed source=" + source + ", command=" + command );					
        System.out.println( "actionPerformed command=" + command );
	}
	
    // Spinner ChangeListener
	@Override
	public void stateChanged(ChangeEvent event) {
		Object source = event.getSource();
		if ( source.getClass().isAssignableFrom( JSpinner.class )) {
			JSpinner spinner = (JSpinner) source;
			System.out.println( "stateChanged value=" + spinner.getValue() );
			Note oldNote = new Note( note );
			System.out.println( "Value=" + value.getValue() + ", octave=" + octave.getValue() );
	        note = new Note( (String)value.getValue() + Integer.toString( (int) octave.getValue() ) );
			PropertyChangeEvent propEvent = new PropertyChangeEvent( note, EVENT_NAME, oldNote, note);
			firePropertyChange( propEvent );
		} else
			System.out.println( "stateChanged source=" + source );				
	}

    // Property support
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    	if ( null != handler ) // Funky init issue where Swing adds listener before cons completed.
        handler.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        handler.removePropertyChangeListener(listener);
    }
    
    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        handler.addPropertyChangeListener(property, listener);
    }
    
    public void removePropertyChangeListener(String property, PropertyChangeListener listener) {
        handler.removePropertyChangeListener(property, listener);
    }
    
	@Override
    public void firePropertyChange(PropertyChangeEvent event) {
		// System.out.println( "propertyChange source=" + event.getSource() + ", oldValue=" + event.getOldValue() + ", newValue=" + event.getNewValue());					
        handler.firePropertyChange(event);
    }
}