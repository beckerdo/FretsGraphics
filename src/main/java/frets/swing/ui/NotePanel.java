package frets.swing.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
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
        // System.out.println( "NotePanel.actionPerformed keyboard command=" + command );
		Note oldNote = new Note( this.note );
		Note newNote = new Note( command );
		// Strange bug where note is null, not inited by initGUI
		if ( null == note)
	        note = new Note( 2, Note.Name.C.getValue() ); // middle C
        note.setValue( newNote.getValue() );
		PropertyChangeEvent propEvent = new PropertyChangeEvent( note, EVENT_NAME, oldNote, note);
		firePropertyChange( propEvent );
	}
	
    // Spinner ChangeListener
	@Override
	public void stateChanged(ChangeEvent event) {
		Object source = event.getSource();
		if ( JSpinner.class.isAssignableFrom( source.getClass())) {
			JSpinner spinner = (JSpinner) source;
			// System.out.println( "NotePanel.stateChanged spinner value=" + spinner.getValue() );
			Note oldNote = new Note( note );
			note.setOctave( ((SpinnerNumberModel)spinner.getModel()).getNumber().intValue() );
			PropertyChangeEvent propEvent = new PropertyChangeEvent( note, EVENT_NAME, oldNote, note);
			firePropertyChange( propEvent );
		} else
			System.out.println( "NotePanel.stateChanged source=" + source );				
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
		protected static final long serialVersionUID = 1L;

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


	@Override
	/** Returns a minimum size for this component. */
	public Dimension getMinimumSize() {
		return new Dimension( 100, 75 );
	}

	@Override
	/** Returns a minimum size for this component. */
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

	/** 
	 * Create a dialog that can be used to edit tables, lists, etc. 
     * @param parent              the parent component for the dialog
     * @param title          the title for the dialog
     * @param modal          a boolean. When true, the remainder of the program
     *                       is inactive until the dialog is closed.
     * @param chooserPane    the note-editor to be placed inside the dialog. (This NoteEditor
     * @param okListener     the ActionListener invoked when "OK" is pressed
     * @param cancelListener the ActionListener invoked when "Cancel" is pressed
     * @return a new dialog containing the color-chooser pane
     * @exception HeadlessException if GraphicsEnvironment.isHeadless()
     * returns true.	 * 
	 * */
    public static NoteChooserDialog createDialog(Component parent, String title, boolean modal,
        NotePanel chooserPane, 
        ActionListener okListener, ActionListener cancelListener) throws HeadlessException {

        Window window = NotePanel.getWindowForComponent(parent);
        NoteChooserDialog dialog;
        if (window instanceof Frame) {
        	// System.out.println( "NoteEditor creating dialog for window");
            dialog = new NoteChooserDialog((Frame)window, title, modal, parent, chooserPane, okListener, cancelListener);
        } else {
        	// System.out.println( "NoteEditor creating dialog for frame");
            dialog = new NoteChooserDialog((Dialog)window, title, modal, parent, chooserPane, okListener, cancelListener);
        }
        dialog.getAccessibleContext().setAccessibleDescription(title);
        return dialog;
    }
    
    public static Window getWindowForComponent(Component parentComponent)
            throws HeadlessException {
            if (parentComponent == null)
                return JOptionPane.getRootFrame();
            if (parentComponent instanceof Frame || parentComponent instanceof Dialog)
                return (Window)parentComponent;
            return NotePanel.getWindowForComponent(parentComponent.getParent());
    }    	
}

/**
 * Builds a note chooser dialog consisting of a NotePanel and actions.
 */
class NoteChooserDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private Note initialNote;
	private NotePanel chooserPane;
	private String action;

	public NoteChooserDialog(Dialog owner, String title, boolean modal,
        Component c, NotePanel chooserPane,
        ActionListener okListener, ActionListener cancelListener)
        throws HeadlessException {
        super(owner, title, modal);
        initNoteChooserDialog(c, chooserPane);
    }

    public NoteChooserDialog(Frame owner, String title, boolean modal,
        Component relativeTo, NotePanel chooserPane,
        ActionListener okListener, ActionListener cancelListener)
        throws HeadlessException {
        super(owner, title, modal);
        initNoteChooserDialog(relativeTo, chooserPane);
    }

    @SuppressWarnings("serial")
	protected void initNoteChooserDialog(Component relativeTo, NotePanel chooserPane ) {
        //setResizable(false);

        this.chooserPane = chooserPane;

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(chooserPane, BorderLayout.CENTER);

        // The following few lines are used to register enter and escape to close the dialog
        Action cancelKeyAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
                // System.out.println( "AbstractAction cancel fire e=" + e );
                setVisible( false );
                action = "cancel";
            }
        };
        KeyStroke cancelKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);        
        Action enterKeyAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // System.out.println( "AbstractAction enter fire e=" + e );
                setVisible( false );      
                action = "enter";
            }
        };
        KeyStroke enterKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);                
        InputMap inputMap = chooserPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);        
        ActionMap actionMap = chooserPane.getActionMap();
        if (inputMap != null && actionMap != null) {
            inputMap.put(cancelKeyStroke, "cancel");
            actionMap.put("cancel", cancelKeyAction);
            inputMap.put(enterKeyStroke, "enter");
            actionMap.put("enter", enterKeyAction);
        }

        pack();
        setLocationRelativeTo(relativeTo);
        this.addWindowListener(new Closer());
    }

    public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

    public Note getInitialNote() {
		return initialNote;
	}

	public void setInitialNote(Note initialNote) {
		this.initialNote = initialNote;
	}

    @SuppressWarnings("deprecation")
	public void show() {
        initialNote = chooserPane.getNote();
        super.show(); // using setVisible causes stack overflow.
    }

    @SuppressWarnings("serial")
    class Closer extends WindowAdapter implements Serializable{
        public void windowClosing(WindowEvent e) {
            Window w = e.getWindow();
            w.setVisible( false );
        }
    }

    @SuppressWarnings("serial")
	static class DisposeOnClose extends ComponentAdapter implements Serializable{
        public void componentHidden(ComponentEvent e) {
            Window w = (Window)e.getComponent();
            w.dispose();
        }
    }
}