package frets.swing.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
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
		// System.out.println( "propertyChange source=" + event.getSource() + ", oldValue=" + event.getOldValue() + ", newValue=" + event.getNewValue());					
        handler.firePropertyChange(event);
    }
	
	/** 
	 * Create a dialog that can be used to edit tables, lists, etc. 
     * @param c              the parent component for the dialog
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
    public static JDialog createDialog(Component c, String title, boolean modal,
        NoteEditor chooserPane, ActionListener okListener,
        ActionListener cancelListener) throws HeadlessException {

        Window window = NoteEditor.getWindowForComponent(c);
        NoteChooserDialog dialog;
        if (window instanceof Frame) {
        	System.out.println( "NoteEditor creating dialog for window");
             dialog = new NoteChooserDialog((Frame)window, title, modal, c, chooserPane, okListener, cancelListener);
        } else {
        	System.out.println( "NoteEditor creating dialog for frame");
             dialog = new NoteChooserDialog((Dialog)window, title, modal, c, chooserPane, okListener, cancelListener);
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
            return NoteEditor.getWindowForComponent(parentComponent.getParent());
    }    
}

/*
 * Class which builds a note chooser dialog consisting of
 * a NoteEditor with "Ok", "Cancel", and "Reset" buttons.
 */
class NoteChooserDialog extends JDialog {
    private Note initialNote;
    private NoteEditor chooserPane;
    private JButton cancelButton;

    public NoteChooserDialog(Dialog owner, String title, boolean modal,
        Component c, NoteEditor chooserPane,
        ActionListener okListener, ActionListener cancelListener)
        throws HeadlessException {
        super(owner, title, modal);
        initNoteChooserDialog(c, chooserPane, okListener, cancelListener);
    }

    public NoteChooserDialog(Frame owner, String title, boolean modal,
        Component c, NoteEditor chooserPane,
        ActionListener okListener, ActionListener cancelListener)
        throws HeadlessException {
        super(owner, title, modal);
        initNoteChooserDialog(c, chooserPane, okListener, cancelListener);
    }

    protected void initNoteChooserDialog(Component c, NoteEditor chooserPane,
        ActionListener okListener, ActionListener cancelListener) {
        //setResizable(false);

        this.chooserPane = chooserPane;

        Locale locale = getLocale();
        String okString = UIManager.getString("ColorChooser.okText", locale);
        String cancelString = UIManager.getString("ColorChooser.cancelText", locale);
        String resetString = UIManager.getString("ColorChooser.resetText", locale);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(chooserPane, BorderLayout.CENTER);

        /*
         * Create Lower button panel
         */
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton okButton = new JButton(okString);
        getRootPane().setDefaultButton(okButton);
        okButton.getAccessibleContext().setAccessibleDescription(okString);
        okButton.setActionCommand("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });
        if (okListener != null) {
            okButton.addActionListener(okListener);
        }
        buttonPane.add(okButton);

        cancelButton = new JButton(cancelString);
        cancelButton.getAccessibleContext().setAccessibleDescription(cancelString);

        // The following few lines are used to register esc to close the dialog
        Action cancelKeyAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                AbstractButton ab = ((AbstractButton)e.getSource());
                System.out.println( "AbstractButton fire e=" + e );
                // ab.fireActionPerformed(e);
            }
        };
        KeyStroke cancelKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        InputMap inputMap = cancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = cancelButton.getActionMap();
        if (inputMap != null && actionMap != null) {
            inputMap.put(cancelKeyStroke, "cancel");
            actionMap.put("cancel", cancelKeyAction);
        }
        // end esc handling

        cancelButton.setActionCommand("cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });
        if (cancelListener != null) {
            cancelButton.addActionListener(cancelListener);
        }
        buttonPane.add(cancelButton);

        JButton resetButton = new JButton(resetString);
        resetButton.getAccessibleContext().setAccessibleDescription(resetString);
        resetButton.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               reset();
           }
        });
        buttonPane.add(resetButton);
        contentPane.add(buttonPane, BorderLayout.SOUTH);

        if (JDialog.isDefaultLookAndFeelDecorated()) {
            boolean supportsWindowDecorations =
            UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if (supportsWindowDecorations) {
                getRootPane().setWindowDecorationStyle(JRootPane.COLOR_CHOOSER_DIALOG);
            }
        }
        applyComponentOrientation(((c == null) ? getRootPane() : c).getComponentOrientation());

        pack();
        setLocationRelativeTo(c);

        this.addWindowListener(new Closer());
    }

    public void show() {
        initialNote = chooserPane.getNote();
        super.show();
    }

    public void reset() {
        chooserPane.setNote(initialNote);
    }

    class Closer extends WindowAdapter implements Serializable{
        public void windowClosing(WindowEvent e) {
            cancelButton.doClick(0);
            Window w = e.getWindow();
            w.hide();
        }
    }

    static class DisposeOnClose extends ComponentAdapter implements Serializable{
        public void componentHidden(ComponentEvent e) {
            Window w = (Window)e.getComponent();
            w.dispose();
        }
    }

}

