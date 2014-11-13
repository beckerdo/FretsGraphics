package frets.swing.ui;

import java.awt.event.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.*;

import frets.main.Note;

/** 
 * A panel that displays 12 keys and up/down octave controls. 
 * The panel can allow single only or multiple latched presses. 
 */
@SuppressWarnings("serial")
public class KeyboardPanel extends JLayeredPane implements ActionListener {
	
	public KeyComponent [] keys = new KeyComponent[ 12 ];
	public boolean allowMultiple = false;
	
	public KeyboardPanel() {
       addKeys();	
    }
    
	public KeyboardPanel( boolean allowMultiple ) {
       setAllowMultiple( allowMultiple );		   
	   addKeys();	
    }
	    
    protected void addKeys() {
        setLayout(new KeyboardLayout());

        keys[ 0 ] = new KeyComponent( Note.C, true, this ); 
        add( keys[ 0 ], JLayeredPane.DEFAULT_LAYER );
        keys[ 1 ] = new KeyComponent( Note.Cs, true, this ); 
        add( keys[ 1 ], JLayeredPane.PALETTE_LAYER );
        keys[ 2 ] = new KeyComponent( Note.D, true, this ); 
        add( keys[ 2 ], JLayeredPane.DEFAULT_LAYER );
        keys[ 3 ] = new KeyComponent( Note.Ds, true, this ); 
        add( keys[ 3 ], JLayeredPane.PALETTE_LAYER );
        keys[ 4 ] = new KeyComponent( Note.E, true, this ); 
        add( keys[ 4 ], JLayeredPane.DEFAULT_LAYER );
        keys[ 5 ] = new KeyComponent( Note.F, true, this ); 
        add( keys[ 5 ], JLayeredPane.DEFAULT_LAYER );
        keys[ 6 ] = new KeyComponent( Note.Fs, true, this ); 
        add( keys[ 6 ], JLayeredPane.PALETTE_LAYER );
        keys[ 7 ] = new KeyComponent( Note.G, true, this ); 
        add( keys[ 7 ], JLayeredPane.DEFAULT_LAYER );
        keys[ 8 ] = new KeyComponent( Note.Gs, true, this ); 
        add( keys[ 8 ], JLayeredPane.PALETTE_LAYER );
        keys[ 9 ] = new KeyComponent( Note.A, true, this ); 
        add( keys[ 9 ], JLayeredPane.DEFAULT_LAYER );
        keys[ 10 ] = new KeyComponent( Note.As, true, this ); 
        add( keys[ 10 ], JLayeredPane.PALETTE_LAYER );
        keys[ 11 ] = new KeyComponent( Note.B, true, this ); 
        add( keys[ 11 ], JLayeredPane.DEFAULT_LAYER );
        
        // Can also implement with KeyToggle, a form of JToggle
        // add( new KeyToggle( Note.C ), JLayeredPane.DEFAULT_LAYER );

        // add octave controls
    }

    public boolean isAllowMultiple() {
		return allowMultiple;
	}

	public void setAllowMultiple(boolean allowMultiple) {
		this.allowMultiple = allowMultiple;
	}

	/** Set the pressed state of all keys. */
	public void setKeyPressed( boolean [] pressed ) {
		int length = pressed.length > keys.length ? keys.length : pressed.length;
		for( int i = 0; i < length; i++ ) 
			keys[ i ].setPressed( pressed[ i ] );					
	}
	
	/* Return the pressed state of all keys. */
	public boolean [] getKeyPressed() {
		boolean [] pressed = new boolean [ keys.length ];
		for( int i = 0; i < pressed.length; i++ ) 
			pressed[ i ] = keys[ i ].isPressed();					
		return pressed;
	}
		
	/** Set the pressed state of all keys. */
	public void setPressed( Set<Note> notes ) {
		for( int i = 0; i < keys.length; i++ ) {
			KeyComponent key = keys[ i ];
			key.setPressed( notes.contains( key.getNote() ));
		}
	}
	
	/* Return the notes of pressed keys. */
	public Set<Note> getPressedNotes() {
		Set<Note> notes = new HashSet<Note>();
		for ( KeyComponent key : keys ) {
			if ( key.isPressed() ) {
				notes.add( key.getNote() );
			}
		}
		return notes;
	}
		
	/** Set the pressed state of all keys. */
	public void setPressed( Note note ) {
		for( int i = 0; i < keys.length; i++ ) {
			KeyComponent key = keys[ i ];
			key.setPressed( note.equals( key.getNote() ));
		}
	}
	
	/* Return the notes of pressed keys. Only returns first match if any. */
	public Note getPressedNote() {
		Set<Note> notes = new HashSet<Note>();
		for ( KeyComponent key : keys ) {
			if ( key.isPressed() ) {
				 return key.getNote();
			}
		}
		return null;
	}
		
	/** Listen to all keys in the panel. */
    public void actionPerformed(ActionEvent e) {
        Object comp = e.getSource();
        String command = e.getActionCommand();
        // String params = e.paramString();
        // System.out.println( "KeyPanel command=" + command );
        
        if ( !allowMultiple ) {
        	// unpress other components
        	synchronized ( this ) {
        	for( KeyComponent key: keys ){
        		if ( key != comp )
        			key.setPressed( false );
        	}
        	}
        }
        fireActionPerformed(null);
    }

	// Handle action listeners
	List<ActionListener> listeners = new LinkedList<ActionListener>();
    public void addActionListener(ActionListener l) {
        listeners.add(l);
    }
    public void removeActionListener(ActionListener l) {
    	listeners.remove( l );
    }
    public ActionListener[] getActionListeners() {
        return listeners.toArray( new ActionListener [] {} );
    }
    
    /** Broadcast to listeners a note pressed event. */
    protected synchronized void fireActionPerformed(ActionEvent event) {
        ActionEvent e = null;
    	String noteString = "";
        if ( null == e ) {
        	Set<Note> notes = getPressedNotes();
        	for ( Note note : notes ) {
        		if (noteString.length() > 0) noteString += ":";
        		noteString += note.toString();
        	}
            e = new ActionEvent( this,
                    ActionEvent.ACTION_PERFORMED,
                    noteString,
                    System.currentTimeMillis(),
                    0 );
            		// event.getModifiers());
        }

        if (( null != noteString ) && ( noteString.length() > 0)) {
        for( ActionListener listener: getActionListeners() ) {
        	listener.actionPerformed(e);
        }
        }
    }

}