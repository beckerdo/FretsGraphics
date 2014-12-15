package frets.swing.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import swingextensions.beansx.PropertyChange;
import frets.main.Note;

/**
 * A component for editing and display of <@link Note}.
 * Basically 12 buttons for note value. Spinner for octaves. 
 */
public class NoteEditor extends JPanel implements ActionListener, ChangeListener, PropertyChange {
	private static final long serialVersionUID = 1L;
	public static final String EVENT_NAME="Note";
    
    public enum Style {
    	VERTICAL, HORIZONTAL, PIANO
    }
	
    protected Note note; // model data

    protected boolean editable = true;
	protected Style style = Style.HORIZONTAL;
    
    protected JToggleButton [] buttons;
    protected JSpinner spinner;

    protected PropertyChangeSupport handler;

	public NoteEditor() {
		initGUI();
	}

	public NoteEditor( Style style ) {
		setStyle( style );
		initGUI();
    }
		
	protected synchronized void initGUI() {
        note = new Note( 2, Note.Name.C.getValue() ); // middle C

        handler = new PropertyChangeSupport( note );

        LayoutManager layout = null;
        if ( style == NoteEditor.Style.HORIZONTAL ) {
        	layout = new GridLayout( 1, 13 );
        	this.setLayout(layout);
        } else if ( style == NoteEditor.Style.VERTICAL ) {
        	layout = new GridLayout( 13, 1 );
        	this.setLayout(layout);
        } else if ( style == NoteEditor.Style.PIANO ) {
            layout = new GridBagLayout();
        } else {        	
        }
       	this.setLayout(layout);
              
        buttons = new JToggleButton[ 12 ];
        Note incr = new Note( note );
        ButtonGroup mutexButtons = new ButtonGroup();
        for( int i = 0; i < buttons.length; i++) {
           buttons[ i ] = new JToggleButton( incr.getName() );
           mutexButtons.add( buttons[ i ] );
           if ( incr.getName().equals( note.getName() )) 
              buttons[ i ].setSelected( true );
           buttons[ i ].addActionListener( this );
           if ( style == NoteEditor.Style.PIANO) {
        	   if ( incr.hasAccidental() ) {
        	        GridBagConstraints gbc = new GridBagConstraints();
        	        gbc.gridx = incr.getValue();
        	        gbc.gridy = 0;
        	        ((GridBagLayout)layout).setConstraints( buttons[ i ], gbc );
        	        buttons[ i ].setBackground( Color.BLACK );
        	        buttons[ i ].setForeground( Color.WHITE );
        	   } else {
        		   GridBagConstraints gbc = new GridBagConstraints();
        		   gbc.gridx = incr.getValue();
        		   gbc.gridy = 1;
        		   ((GridBagLayout)layout).setConstraints( buttons[ i ], gbc );
        		   buttons[ i ].setBackground( Color.WHITE );        		   
        		   buttons[ i ].setForeground( Color.BLACK );
        	   }
           } else {
           }
    	   add( buttons[ i ] );

           incr.plus( 1 );
        }      
        
        SpinnerModel octaveModel = new SpinnerNumberModel(0, 0, 6, 1);
        spinner = new JSpinner( octaveModel );
        spinner.setValue( note.getOctave() );        
        spinner.addChangeListener( this );
        if ( style == NoteEditor.Style.PIANO) {
   	        GridBagConstraints gbc = new GridBagConstraints();
   	        gbc.gridx = 12;
   	        gbc.gridy = 1;
   	        ((GridBagLayout)layout).setConstraints( spinner, gbc );
        }
     	add( spinner );		
	}
	
	public final Note getNote() {
		return note;
	}

	public void setNote(Note note) {
		this.note = note;
		if ( null != note ) {
			// Update UI
			spinner.setValue( note.getOctave() );
			for( int i = 0; i < buttons.length; i++) {
				if ( note.getName().equals( buttons[ i ].getText() )) {
					buttons[ i ].setSelected( true );
					return;
				}
			}
        }
	}

    public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

    public Style getStyle() {
		return style;
	}

	public void setStyle(Style style) {
		this.style = style;
	}

	// Button ActionListener
	@Override
	public void actionPerformed(ActionEvent event) {
		// Object source = event.getSource();
		String command = event.getActionCommand();
        // System.out.println( "actionPerformed source=" + source + ", command=" + command );					
        // System.out.println( "actionPerformed command=" + command );
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
			// System.out.println( "stateChanged value=" + spinner.getValue() );
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
		System.out.println( "propertyChange source=" + event.getSource() + ", oldValue=" + event.getOldValue() + ", newValue=" + event.getNewValue());					
        handler.firePropertyChange(event);
    }
	
}