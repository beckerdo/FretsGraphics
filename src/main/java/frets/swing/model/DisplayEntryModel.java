package frets.swing.model;

import java.io.Serializable;

/**
 * Represents a simple bean with items to display via Swing.
 */
public class DisplayEntryModel implements Serializable {
	private static final long serialVersionUID = 1L;
	
	protected String root;
    protected String formula;
    protected String notes;
    protected String locations;
    protected String variation;
	protected String score;
	protected String comments;

    public DisplayEntryModel() {
    }
       
	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setLocations(String locations) {
        this.locations = locations;
    }
    
    public String getLocations() {
        return locations;
    }
    
    public String getVariation() {
		return variation;
	}

	public void setVariation(String variation) {
		this.variation = variation;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public String getComments() {
		return comments;
	}

	public void setComments( String comments) {
		this.comments = comments;
	}

    
    public DisplayEntryModel clone() {
        DisplayEntryModel entry = new DisplayEntryModel();
        entry.root = root;
        entry.formula = formula;
        entry.notes = notes;
        entry.locations = locations;
        entry.variation = variation;
        entry.score = score;
        entry.comments = comments;
        return entry;
    }
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder( "DisplayEntryModel[");
    	sb.append( "root=" + root );
    	sb.append( ",formula=" + formula );
    	sb.append( ",notes=" + notes );
    	sb.append( ",locations=" + locations );
    	sb.append( ",variation=" + variation );
    	sb.append( ",score=" + score );
    	sb.append( "]" );     	
    	return sb.toString();
    }
}