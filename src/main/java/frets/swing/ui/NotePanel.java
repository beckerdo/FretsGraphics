package frets.swing.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import swingextensions.beansx.PropertyChange;
import frets.main.Note;

/**
 * A component for editing and display of <@link Note}.
 * Provides a keyboard for note and an octave spinner.
 */
public class NotePanel extends JPanel implements ActionListener, ChangeListener, PropertyChange {
	private static final long serialVersionUID = 1L;
	public static final String EVENT_NAME="Note";
    
    protected Note note; // model data
    protected boolean editable = true;
    
    protected KeyboardPanel keys;
    protected JSpinner spinner;

    protected PropertyChangeSupport handler;

	public NotePanel() {
		initGUI();
	}

	protected synchronized void initGUI() {
        note = new Note( 2, Note.Name.C.getValue() ); // middle C

        handler = new PropertyChangeSupport( note );

        // LayoutManager layout = new GridLayout( 1, 2 );
        LayoutManager layout = new BorderLayout( 1, 2 );
       	this.setLayout(layout);

       	keys = new KeyboardPanel( false ); // No multiple keys
       	keys.addActionListener( this );
       	add( keys, BorderLayout.CENTER );
       	
        // public SpinnerNumberModel(int value, int minimum, int maximum, int stepSize) 
        SpinnerModel octaveModel = new SpinnerNumberModel(0, 0, 6, 1);
        spinner = new JSpinner( octaveModel ) {
        	// Provide new layout so that buttons stack vertically in number spinner.
            @Override 
            public void setLayout(LayoutManager mgr) {
                super.setLayout(new SpinnerVerticalLayout());
             }        	
        };
        
        spinner.setValue( note.getOctave() );        
        spinner.addChangeListener( this );
     	add( spinner, BorderLayout.EAST );		
	}
	
	public final Note getNote() {
		return note;
	}

	public void setNote(Note note) {
		this.note = note;
		if ( null != note ) {
			// Update UI
			keys.setPressed( note  );
			spinner.setValue( note.getOctave() );
        }
	}

    public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	/** Listen to changes in buttons pressed. */
	@Override
	public void actionPerformed(ActionEvent event) {
		// Object source = event.getSource();
		String command = event.getActionCommand();
        // System.out.println( "actionPerformed source=" + source + ", command=" + command );					
        System.out.println( "actionPerformed command=" + command );
		Note oldNote = new Note( note );
        note.setValue( (new Note( command )).getValue() );
		PropertyChangeEvent propEvent = new PropertyChangeEvent( note, EVENT_NAME, oldNote, note);
		firePropertyChange( propEvent );
	}
	
    // Spinner ChangeListener
	@Override
	public void stateChanged(ChangeEvent event) {
		Object source = event.getSource();
		if ( source.getClass().isAssignableFrom( JSpinner.class )) {
			JSpinner spinner = (JSpinner) source;
			System.out.println( "stateChanged value=" + spinner.getValue() );
			Note oldNote = new Note( note );
			note.setOctave( ((SpinnerNumberModel)spinner.getModel()).getNumber().intValue() );
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

	/** Stacks buttons vertically in number spinner. */
	class SpinnerVerticalLayout extends BorderLayout {
		  @Override 
		  public void addLayoutComponent(Component comp, Object constraints) {
		    if("Editor".equals(constraints)) {
		      constraints = "Center";
		    } else if("Next".equals(constraints)) {
		      constraints = "North";
		    } else if("Previous".equals(constraints)) {
		      constraints = "South";
		    }
		    super.addLayoutComponent(comp, constraints);
		  }
		}
}