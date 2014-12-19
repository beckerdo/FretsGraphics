package frets.swing.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import swingextensions.swingx.app.Application;


/** 
 * The popup menu for image panels.
 * Allows save (to file) and copy (to clipboard).
 * <p>
 * A good brief <a href="http://stackoverflow.com/questions/766956/how-do-i-create-a-right-click-context-menu-in-java-swing">
 * summary of use</a>. 
 * 
 * @author <a href="mailto:dan@danbecker.info">Dan Becker</a>
 */
@SuppressWarnings("serial")
public class PopupMenuAction extends JPopupMenu implements ActionListener {
	public PopupMenuAction() {
	    ResourceBundle resources = Application.getInstance().getResourceBundle();
		
        JMenuItem saveItem = new JMenuItem( resources.getString("menu.save") );
        saveItem.addActionListener(this);
        add( saveItem );
        
        JMenuItem copyItem = new JMenuItem( resources.getString("menu.copy") );
        copyItem.addActionListener(this);
        add( copyItem );
        
        // JMenuItem varyAllMenuItem = MnemonicHelper.createMenuItem(entryMenu, resources.getString("menu.varyAll"));
        // varyAllMenuItem.addActionListener(new DynamicAction(this, "varyAll"));
        // varyAllMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl shift V"));        
	}
	
	/** Called when a menu item on this element have been clicked/pressed. */
	@Override
	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		String command = event.getActionCommand();
		Component invoker = this.getInvoker();
        // System.out.println( "PopupMenuAction.actionPerformed command=" + command + ", invoker=" + invoker);
        
        if ( JLabel.class.isAssignableFrom( invoker.getClass() )) {
        	JLabel jlabel = (JLabel) invoker;
            // System.out.println( "ImagePopupMenu.actionPerformed command=" + command + ", parent=" +  jlabel.getParent());
            // System.out.println( "ImagePopupMenu.actionPerformed entry name=" + jlabel.getToolTipText() );
            
            String entryName = jlabel.getToolTipText();
    		Icon icon = jlabel.getIcon();            
            switch ( command ) {
            	case "Save":
            		BufferedImage image = SafeIcon.provideImage(icon);
            		String fileName = "frets.png";
            		if ( null !=  entryName )
            			fileName = "frets," + entryName + ".png";
            		try {
            			ImageIO.write(image, "png", new File( fileName ));
                        System.out.println( "PopupMenuAction.actionPerformed save to \"" + fileName + "\"." );
            		} catch ( IOException e ) {
            			System.out.println( "PopupMenuAction exception: " + e );
            		}
            		break; 
            	case "Copy": 
            		BufferedImage cImage = SafeIcon.provideImage(icon);
                    if (cImage == null)
                        throw new IllegalArgumentException ("Image can't be null");

                    ImageTransferable transferable = new ImageTransferable( cImage );
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
            		break;
            	default: System.out.println( "PopupMenuAction warning: unhandled command \"" + command + "\"");
            }
        }
        
        
	}
	
	static class ImageTransferable implements Transferable {
		private Image image;

		public ImageTransferable(Image image) {
			this.image = image;
		}

		public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException {
			if (isDataFlavorSupported(flavor)) {
				return image;
			} else {
				throw new UnsupportedFlavorException(flavor);
			}
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return flavor == DataFlavor.imageFlavor;
		}

		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { DataFlavor.imageFlavor };
		}
	}
}