package frets.swing.ui;

import javax.swing.AbstractCellEditor;
import javax.swing.table.TableCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTable;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import frets.main.Note;

/** A Swing JTable editor that can provide a note value. */
public class NoteTableEditor extends AbstractCellEditor
    implements TableCellEditor, ActionListener {
	private static final long serialVersionUID = 1L;
	
    protected static final String EDIT = "edit";

    Note previousNote;
    
    JButton button;
    NotePanel notePanel;
    NoteChooserDialog dialog;

    public NoteTableEditor() {
        // Set up the editor (from the table's point of view), which is a button.
        // This button brings up the NoteEditor dialog,
        // which is the editor from the user's point of view.
        button = new JButton();
        button.setBorderPainted(false);
        button.setActionCommand(EDIT);
        button.addActionListener(this);

        //Set up the dialog that the button brings up.
        notePanel = new NotePanel();
        dialog = NotePanel.createDialog( button, "Root Note",  true,  //modal
                notePanel,
                this,  // OK button handler
                this); // CANCEL button handler
    }

    /**
     * Handles events from the editor button and from the dialog's OK button.
     */
    public void actionPerformed(ActionEvent e) {        
    	if (EDIT.equals(e.getActionCommand())) {
            // The user has clicked the cell, so bring up the dialog.
        	// System.out.println( "NoteTableEditor.actionPerformed EDIT e=" + e);
        	// System.out.println( "NoteTableEditor.actionPerformed EDIT previousNote=" + previousNote);
            notePanel.setNote(previousNote);
            dialog.setVisible(true);

            // Make the renderer reappear.
            String command = dialog.getAction();
            if ( "cancel".equals( command )) {
            	Object value = this.getCellEditorValue();
            	// System.out.println( "NoteTableEditor.actionPerformed EDIT cancel initialNote=" + dialog.getInitialNote());
                fireEditingCanceled();
            } else if ( "enter".equals( command )) {
            	// System.out.println( "NoteTableEditor.actionPerformed EDIT enter initialNote=" + dialog.getInitialNote());
                fireEditingStopped(); // Will call the setter on the table model.
            }
        } else { // User pressed dialog's "OK" button.
        	System.out.println( "NoteTableEditor.actionPerformed notEDIT e=" + e);
            previousNote = notePanel.getNote();
        }
    }

    // Implement the one CellEditor method that AbstractCellEditor doesn't.
    public Object getCellEditorValue() {
        return previousNote;
    }

    // Implement the one method defined by TableCellEditor.
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
        int row, int column) {
    	if ( null != value) {
    	   // System.out.println( "NoteTableEditor.getTableCellEditorComponent type=" + value.getClass().getSimpleName() + ", value=" + value );
    	   String valString = (String) value;
          	if ((null == previousNote) || (!valString.equals( previousNote.toString()  ))) {
          		previousNote = new Note( valString );
          	}
    	} else 
     	   System.out.println( "NoteTableEditor.getTableCellEditorComponent value=null" );
        // currentNote = (Note)value;
        return button;
    }
}