package frets.swing.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import frets.main.Note;


/** Lays out components in the shape of a keyboard.
 * <p>
 * Ideally the parent container should contain multiples of 12 keys.
 * <ul>
 * <li>Children 0, 5 will be sized like a C key (left straight, right L shaped).
 * <li>Children 2, 7, 9 will be sized like a D key (left and right L shaped).
 * <li>Children 4, 11 will be sized like a E key (left L shaped, right straight).
 * <li>Children 1, 3, 6, 8, 10 will be sized like black keys.
 * </ul>
 * <p>
 * It is the responsibility of the children to reshape after being resized.
 * This is usually done in a componentResized method.
 * 
 * @author <a href="mailto:dan@danbecker.info">Dan Becker</a>
 */
public class KeyboardLayout implements LayoutManager {
	/** L cutout of white key. Positioning of black key */
	public static final float OFFSET_13 = 1f/3f;
	public static final float OFFSET_23 = 2f/3f;
	public static final float OFFSET_12 = 1f/2f;
	
	@Override
	public void addLayoutComponent(String name, Component comp) {}

	@Override
	public void removeLayoutComponent(Component comp) {	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return null;
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return null;
	}

	@Override
	public void layoutContainer(Container parent) {
		Dimension size = parent.getSize();
		// System.out.println( "Keyboard layout size =" + size );
		if (( null == size ) || ( size.width < 1 ) || ( size.height < 1 )) {
			return;
		}
		final int WH_WIDTH = size.width / 7;
		final int WH_HEIGHT = size.height;
		final int BL_HEIGHT = 6 * size.height / 10; // 2/3 height
		final int BL_WIDTH = 2 * size.width / 21; // 2/3 white width
		int componentCount = parent.getComponentCount();
		// System.out.println( "Keyboard layout component count=" + componentCount );
		for ( int i = 0; i < componentCount; i++ ) {
			// Remove dependency on specific component. Instead rely on component name,
			// KeyComponent component = (KeyComponent) parent.getComponent(i);
			// KeyToggle component = (KeyToggle) parent.getComponent(i);
			Component component = parent.getComponent(i);
			String noteName = component.getName();
			if ((null == noteName) || (noteName.length() < 1))
				throw new IllegalArgumentException( "KeyboardLayout only designed to work with notes.");
			Note note = Note.parse( noteName );
			// Set sizes
			switch ( note.getName() ) {
				case "C": case "D": case "E": case "F": case "G": case "A": case "B": // white
					component.setSize( WH_WIDTH, WH_HEIGHT);
				break;
				case "C#": case "D#": case "F#": case "G#": case "A#": // black
					component.setSize( BL_WIDTH, BL_HEIGHT);
				break;
				default:
					System.out.println( "Unknown key note name=" + note.getName());
			}
			// Set positions
			switch ( note.getName() ) {
				case "C": component.setLocation( 0, 0 ); break;	// C
				case "D": component.setLocation( WH_WIDTH, 0 ); break; // D
				case "E": component.setLocation( 2 * WH_WIDTH, 0 ); break; // E
				case "F": component.setLocation( 3 * WH_WIDTH, 0 ); break; // F
				case "G": component.setLocation( 4 * WH_WIDTH, 0 ); break; // G
				case "A": component.setLocation( 5  * WH_WIDTH, 0 ); break; // A
				case "B": component.setLocation( 6  * WH_WIDTH, 0 ); break;// B
				case "C#": component.setLocation( (int) ( 1f * WH_WIDTH - OFFSET_23 * BL_WIDTH), 0 ); break; // Cs
				case "D#": component.setLocation( (int) ( 2f * WH_WIDTH - OFFSET_13 * BL_WIDTH), 0 ); break; // Ds
				case "F#": component.setLocation( (int) ( 4f * WH_WIDTH - OFFSET_23 * BL_WIDTH), 0 ); break; // Fs
				case "G#": component.setLocation( (int) ( 5f * WH_WIDTH - OFFSET_12 * BL_WIDTH), 0 ); break; // Gs
				case "A#": component.setLocation( (int) ( 6f * WH_WIDTH - OFFSET_13 * BL_WIDTH), 0 ); break; // As
				default:
					System.out.println( "Unknown key note name=" + note.getName());
			}
			
		}
	}

}