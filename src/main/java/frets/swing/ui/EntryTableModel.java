package frets.swing.ui;

import java.util.LinkedList;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import frets.swing.model.DisplayEntryModel;
import frets.swing.model.ExtendedDisplayEntry;

/** A class that maintains a linked list of extendedDisplayEntry */
public class EntryTableModel extends LinkedList<ExtendedDisplayEntry> implements TableModel {
	private static final long serialVersionUID = 1L;
	
	protected ExtendedDisplayEntry prototype = new ExtendedDisplayEntry(new DisplayEntryModel());

    protected LinkedList<TableModelListener> listenerList = new LinkedList<TableModelListener>();

	@Override
	public int getRowCount() {
		return super.size();
	}

	@Override
	public int getColumnCount() {
		return prototype.getMemberCount();
	}

	@Override
	public String getColumnName(int columnIndex) {
		return prototype.getMemberName(columnIndex);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return prototype.getMemberClass(columnIndex);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (( rowIndex < 0 ) || ( rowIndex >= super.size() ))
	       throw new IllegalArgumentException( "EntryTableModel rowIndex=" + rowIndex );
		if ( columnIndex < 0 ) 
	       throw new IllegalArgumentException( "EntryTableModel colIndex=" + columnIndex );
		ExtendedDisplayEntry value = super.get( rowIndex );
		if ( columnIndex >= value.getMemberCount() )
	       throw new IllegalArgumentException( "EntryTableModel colIndex=" + columnIndex );
		return value.getMember(columnIndex );			
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (( rowIndex < 0 ) || ( rowIndex >= super.size() ))
	       throw new IllegalArgumentException( "EntryTableModel rowIndex=" + rowIndex );
		if ( columnIndex < 0 ) 
	       throw new IllegalArgumentException( "EntryTableModel colIndex=" + columnIndex );
		ExtendedDisplayEntry value = super.get( rowIndex );
		if ( columnIndex >= value.getMemberCount() )
	       throw new IllegalArgumentException( "EntryTableModel colIndex=" + columnIndex );
		value.setMember(columnIndex, aValue);			
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
        listenerList.add(l);
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
        listenerList.remove(l);
	}

	// Table changes
    public void fireTableChanged(TableModelEvent e) {
    	synchronized( listenerList ) {
        for( TableModelListener listener : listenerList ) {
        	listener.tableChanged( e );
        }
    	}
    }	
}