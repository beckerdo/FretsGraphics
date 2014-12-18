package frets.swing.ui;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** 
 * The popup menu listener for image panels.
 * Allows save (to file) and copy (to clipboard).
 * <p>
 * Add this to JComponents that need a popupmenu
 *    component.addMouseListener(new ImagePopupListener());
 * <p>
 * A good brief <a href="http://stackoverflow.com/questions/766956/how-do-i-create-a-right-click-context-menu-in-java-swing">
 * summary of use</a>. 
 * 
 * @author <a href="mailto:dan@danbecker.info">Dan Becker</a>
 */
public class PopupMenuListener extends MouseAdapter {
    public void mousePressed(MouseEvent e){
        if (e.isPopupTrigger())
            doPop(e);
    }

    public void mouseReleased(MouseEvent e){
        if (e.isPopupTrigger())
            doPop(e);
    }

    /** Pops up and shows the menu. */
    private void doPop(MouseEvent e){
		Component component = e.getComponent();
		// Object source = e.getSource();
        // System.out.println( "ImagePopupListener.actionPerformed source=" + source + ", component=" + component );
        PopupMenuAction menu = new PopupMenuAction();
        menu.show(component, e.getX(), e.getY());
    }
}