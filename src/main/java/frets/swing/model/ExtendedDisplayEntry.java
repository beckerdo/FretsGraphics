package frets.swing.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

import swingextensions.beansx.ExtendedBean;
import swingextensions.beansx.ExtendedBeanHandler;

/**
 * Represents a simple bean with items to display via Swing.
 * The bean uses {@link ExtendedBeanHandler} to implement {@link ExtendedBean} interface.
 */
public class ExtendedDisplayEntry implements ExtendedBean, Serializable {
	protected static final long serialVersionUID = 1L;
	protected DisplayEntryModel bean;
	protected final ExtendedBeanHandler handler;
    
	public ExtendedDisplayEntry() {
		this.bean = new DisplayEntryModel();
	    this.handler = new ExtendedBeanHandler( bean  );
	}

	public ExtendedDisplayEntry( DisplayEntryModel bean ) {
		this.bean = bean;
	    this.handler = new ExtendedBeanHandler( bean  );
	}

    public ExtendedDisplayEntry clone() {
    	DisplayEntryModel bean = this.bean.clone();
    	return new ExtendedDisplayEntry( bean );
    }    

    // Property support
    public void addPropertyChangeListener(PropertyChangeListener listener) {
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
    
    public void firePropertyChange(PropertyChangeEvent event) {
        handler.firePropertyChange(event);
    }
	
	// MultiValue interface
    public int getMemberCount() {
    	return handler.getMemberCount();
    }
    
    public Object getMember(String memberName) {
    	return handler.getMember( memberName );
    }
    
    public Object getMember(int memberIndex) {
    	return handler.getMember( memberIndex );
    }
    
    public void setMember(String memberName,Object value) {
    	Object oldValue = getMember( memberName );
    	handler.setMember(memberName, value);
        firePropertyChange( new PropertyChangeEvent(this,memberName, oldValue, value ));
     }
     
    public void setMember(int memberIndex, Object value) {
    	setMember( getMemberNames()[ memberIndex ], value ); // returns name in property change
     }
     
    public Class<?> getMemberClass(String memberName) {
    	return handler.getMemberClass( memberName );
    }

    public Class<?> getMemberClass(int memberIndex) {
    	return handler.getMemberClass( memberIndex );
    }

    public final String [] getMemberNames() {
    	return handler.getMemberNames();
    }
    
    public String getMemberName(int memberIndex) {
    	return handler.getMemberName(memberIndex);
    }

    /** Semantic equals tests all field values. */
	public boolean equals(ExtendedDisplayEntry other) {
		if (this == other)
			return true;
		if (other == null)
			return false;
		if (bean == null) {
			if (other.bean != null)
				return false;
		} else if (!bean.equals(other.bean))
			return false;
		if (handler == null) {
			if (other.handler != null)
				return false;
		} else if (!handler.equals(other.handler))
			return false;
		return true;
	}
   
    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return equals( (ExtendedDisplayEntry) obj );
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bean == null) ? 0 : bean.hashCode());
		result = prime * result + ((handler == null) ? 0 : handler.hashCode());
		return result;
	}
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder( "ExtendedDisplayEntry[");
    	sb.append( "root=" + bean.getRoot() );
    	sb.append( ",formula=" + bean.getFormula() );
    	sb.append( ",notes=" + bean.getNotes() );
    	sb.append( ",locations=" + bean.getLocations() );
    	sb.append( ",variation=" + bean.getVariation() );
    	sb.append( ",score=" + bean.getScore() );
    	sb.append( "]" );     	
    	return sb.toString();
    }
}