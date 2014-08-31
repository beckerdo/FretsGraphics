package frets.swing.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests to validate this class.  
 * @author <a href="mailto:dan@danbecker.info">Dan Becker</a>
 */
public class NoteEditorTest {

	@Before
	public void setup() {
	}

	@Test
	public void testCRUD() {
		NoteEditor editor = new NoteEditor();
		assertNotNull("Editor note", editor.getNote());

		assertEquals("Editor style", NoteEditor.Style.HORIZONTAL, editor.getStyle());
	}

	/** Run as application to do a visual test of this component. */
	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame("EditorDemo");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				// Create and set up the content pane.
				NoteEditor editor = new NoteEditor(NoteEditor.Style.HORIZONTAL);
				// NoteEditor editor = new NoteEditor( NoteEditor.Style.PIANO );
				editor.addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent event) {
						Object source = event.getSource();
						System.out.println("test propertyChange source="
								+ source + ", oldValue=" + event.getOldValue()
								+ ", newValue=" + event.getNewValue());
					}
				});
				// NoteEditor newContentPane = new NoteEditor(
				// NoteEditor.Style.PIANO );
				editor.setOpaque(true); // content panes must be opaque
				frame.setContentPane(editor);

				// Display the window.
				frame.pack();
				frame.setVisible(true);
			}
		});
	}

}