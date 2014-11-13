package frets.swing.ui;

import java.awt.event.*;
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

    public void actionPerformed(ActionEvent e) {
        Object comp = e.getSource();
        String command = e.getActionCommand();
        // String params = e.paramString();
        System.out.println( "KeyPanel command=" + command );
        
        if ( !allowMultiple ) {
        	// unpress other components
        	synchronized ( this ) {
        	for( KeyComponent key: keys ){
        		if ( key != comp )
        			key.setPressed( false );
        	}
        	}
        }
    }

    public String getDisplayName() {
        return "Keys";
    }
}