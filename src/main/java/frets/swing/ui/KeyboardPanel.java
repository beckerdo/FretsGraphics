package frets.swing.ui;

import java.awt.event.*;
import javax.swing.*;
import frets.main.Note;

/** A panel that displays 12 keys and up/down octave controls. */
@SuppressWarnings("serial")
public class KeyboardPanel extends JLayeredPane implements ActionListener {
    public KeyboardPanel() {
       addKeys();	
    }
    
    protected void addKeys() {
        setLayout(new KeyboardLayout());

        add( new KeyComponent( Note.C ), JLayeredPane.DEFAULT_LAYER );
        add( new KeyComponent( Note.Cs ), JLayeredPane.PALETTE_LAYER );
        add( new KeyComponent( Note.D ), JLayeredPane.DEFAULT_LAYER );
        add( new KeyComponent( Note.Ds ), JLayeredPane.PALETTE_LAYER  );
        add( new KeyComponent( Note.E ), JLayeredPane.DEFAULT_LAYER );
        add( new KeyComponent( Note.F ), JLayeredPane.DEFAULT_LAYER );
        add( new KeyComponent( Note.Fs ), JLayeredPane.PALETTE_LAYER  );
        add( new KeyComponent( Note.G ), JLayeredPane.DEFAULT_LAYER );
        add( new KeyComponent( Note.Gs ), JLayeredPane.PALETTE_LAYER  );
        add( new KeyComponent( Note.A ), JLayeredPane.DEFAULT_LAYER );
        add( new KeyComponent( Note.As ), JLayeredPane.PALETTE_LAYER  );
        add( new KeyComponent( Note.B ), JLayeredPane.DEFAULT_LAYER );
        
        // add octave controls
    }

    public void actionPerformed(ActionEvent e) {
        String key = null;
        String command = ((JToggleButton)e.getSource()).getActionCommand();
        key = command;
        System.out.println( "You pressed " + key );
    }

    public String getDisplayName() {
        return "Keys";
    }
}