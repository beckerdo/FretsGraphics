package frets.swing.ui;

import java.util.Collection;
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
	public boolean isCellEditable(int row, int col) {
		if ( col == 0 )
			return true;
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
		return value.getMember( columnIndex );			
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int colIndex) {
		if (( rowIndex < 0 ) || ( rowIndex >= super.size() ))
	       throw new IllegalArgumentException( "EntryTableModel rowIndex=" + rowIndex );
		if ( colIndex < 0 ) 
	       throw new IllegalArgumentException( "EntryTableModel colIndex=" + colIndex );
		ExtendedDisplayEntry value = super.get( rowIndex );
		if ( colIndex >= value.getMemberCount() )
	       throw new IllegalArgumentException( "EntryTableModel colIndex=" + colIndex );
		System.out.println( "EntryTableModel.setValueAt row,col=" + rowIndex + "," + colIndex + ", type=" + aValue.getClass().getSimpleName() + ", val=" + aValue );
		value.setMember(colIndex, aValue.toString());			
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
        listenerList.add(l);
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
        listenerList.remove(l);
	}

	// List interface (notifies JTable  of changes ). 
	@Override
	public boolean add(ExtendedDisplayEntry e) {
		int row = this.size();
		boolean val = super.add( e );
	    fireTableRowsInserted( row, row );
	    return val;
	}
	@Override 
	public void add(int index, ExtendedDisplayEntry e) {
		super.add( index, e );
	    fireTableRowsInserted( index, index );	
	}
	@Override
	public boolean addAll(Collection<? extends ExtendedDisplayEntry> c) {
		if (( null == c ) || ( c.size() < 1 ))
			return false;
		int row = this.size();
		boolean val = super.addAll( c );
	    fireTableRowsInserted( row, row + c.size() - 1 );
	    return val;
	}
	@Override
	public boolean addAll(int index, Collection<? extends ExtendedDisplayEntry> c) {
		if (( null == c ) || ( c.size() < 1 ))
			return false;
		boolean val = super.addAll( index, c );
	    fireTableRowsInserted( index, index + c.size() - 1 );
	    return val;
	}
	@Override 
	public void addFirst(ExtendedDisplayEntry e) {
		add( 0, e);
	}
	@Override 
	public void addLast(ExtendedDisplayEntry e){
		add( this.size(), e);
		
	}
	@Override 
	public void push(ExtendedDisplayEntry e) {
		add( 0, e);
	}

	@Override
    public void clear() {
		super.clear();
		fireTableDataChanged();
	}
	@Override
	public boolean offer(ExtendedDisplayEntry e) {
	   return add( e );
	}
	@Override
	public boolean offerFirst(ExtendedDisplayEntry e) {
	   add( 0, e );
	   return true;
	}
	@Override
	public boolean offerLast(ExtendedDisplayEntry e) {
	   return add( e );
	}
	@Override
	public ExtendedDisplayEntry poll() {
		return remove( 0 );
	}
	@Override
	public ExtendedDisplayEntry pollFirst() {
		return remove( 0 );
	}
	@Override
	public ExtendedDisplayEntry pollLast() {
		return remove( this.size() - 1 );
	}
	@Override
	public ExtendedDisplayEntry pop() {
		return remove( 0 );
	}
	@Override
	public ExtendedDisplayEntry remove() {
		return remove( 0 );
	}
	@Override
	public ExtendedDisplayEntry remove( int index) {
		ExtendedDisplayEntry e = super.remove( index );
	    fireTableRowsDeleted( index, index );
		return e;
	}
	@Override
	public ExtendedDisplayEntry removeFirst() {
		return remove( 0 );
	}
	// @Override
	public boolean removeFirstOccurence( Object o ) {
		int loc = this.indexOf( o );
		if ( -1 != loc ) {
			remove( loc );
			return true;
		}			
		return false;
	}
	@Override
	public ExtendedDisplayEntry removeLast() {
		return remove( this.size() - 1 );
	}
	// @Override
	public boolean removeLastOccurence( Object o ) {
		int loc = this.lastIndexOf( o );
		if ( -1 != loc ) {
			remove( loc );
			return true;
		}			
		return false;
	}

	@Override
	public ExtendedDisplayEntry set(int index, ExtendedDisplayEntry e ) {
	   ExtendedDisplayEntry val = super.set( index, e );
       fireTableRowsUpdated( index, index );
	   return val;
	}
	
	// Table changes
    public void fireTableChanged(TableModelEvent e) {
    	synchronized( listenerList ) {
        for( TableModelListener listener : listenerList ) {
        	// Really should be on its own thread so as not to slow the main table.
        	listener.tableChanged( e );
        }
    	}
    }	
    
    public void fireTableDataChanged() {
        fireTableChanged(new TableModelEvent(this));
    }

    public void fireTableStructureChanged() {
        fireTableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
    }

    public void fireTableRowsInserted(int firstRow, int lastRow) {
        fireTableChanged(new TableModelEvent(this, firstRow, lastRow,
                             TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
    }

    public void fireTableRowsUpdated(int firstRow, int lastRow) {
        fireTableChanged(new TableModelEvent(this, firstRow, lastRow,
                             TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
    }

    public void fireTableRowsDeleted(int firstRow, int lastRow) {
        fireTableChanged(new TableModelEvent(this, firstRow, lastRow,
                             TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
    }

    public void fireTableCellUpdated(int row, int column) {
        fireTableChanged(new TableModelEvent(this, row, row, column));
    }

}