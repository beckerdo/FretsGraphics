package frets.swing.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class NotePanel extends JPanel implements ActionListener {
    JToggleButton c;
    JToggleButton cs;
    JToggleButton d;
    JToggleButton ds;
    JToggleButton e;
    JToggleButton f;
    JToggleButton fs;
    JToggleButton g;
    JToggleButton gs;
    JToggleButton a;
    JToggleButton as;
    JToggleButton b;

//    public void updateChooser() {
//        Color color = getColorFromModel();
//        if (Color.red.equals(color)) {
//            redCrayon.setSelected(true);
//        } else if (Color.yellow.equals(color)) {
//            yellowCrayon.setSelected(true);
//        } else if (Color.green.equals(color)) {
//            greenCrayon.setSelected(true);
//        } else if (Color.blue.equals(color)) {
//            blueCrayon.setSelected(true);
//        }
//    }

    public NotePanel() {
       buildKeys();	
    }
    
    protected JToggleButton createKey(String name, Border normalBorder) {
        JToggleButton key = new JToggleButton();
        key.setActionCommand(name);
        key.addActionListener(this);

        //Set the image or, if that's invalid, equivalent text.
        ImageIcon icon = createImageIcon("images/" + name + ".gif");
        if (icon != null) {
            key.setIcon(icon);
            key.setToolTipText("The " + name + " crayon");
            key.setBorder(normalBorder);
        } else {
            key.setText( name + " key");
            key.setFont(key.getFont().deriveFont(Font.ITALIC));
            key.setHorizontalAlignment(JButton.HORIZONTAL);
            key.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }

        return key;
    }

    protected void buildKeys() {
        setLayout(new GridLayout(1, 0));

        ButtonGroup keys = new ButtonGroup();
        Border border = BorderFactory.createEmptyBorder(2,2,2,2);

        c = createKey("C", border);
        keys.add(c);
        add(c);
        cs = createKey("C#", border);
        keys.add(cs);
        add(cs);
        d = createKey("D", border);
        keys.add(d);
        add(d);
        ds = createKey("D#", border);
        keys.add(ds);
        add(ds);
        e = createKey("E", border);
        keys.add(e);
        add(e);
        f = createKey("F", border);
        keys.add(f);
        add(f);
        fs = createKey("F#", border);
        keys.add(fs);
        add(fs);
        g = createKey("G", border);
        keys.add(g);
        add(g);
        gs = createKey("G#", border);
        keys.add(gs);
        add(gs);
        a = createKey("A", border);
        keys.add(a);
        add(a);
        as = createKey("A#", border);
        keys.add(as);
        add(as);
        b = createKey("B", border);
        keys.add(b);
        add(b);
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = NotePanel.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
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

    public Icon getSmallDisplayIcon() {
        return null;
    }

    public Icon getLargeDisplayIcon() {
        return null;
    }
}