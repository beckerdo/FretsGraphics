package frets.swing.model;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;

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
    protected String imagePath;
    protected BufferedImage image;

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

    public void setImagePath(String imagePath) {
	    File file = new File( imagePath );
	    if ( !file.canRead() )
	    	throw new IllegalArgumentException( "Cannot read file at \"" + imagePath + "\"" );
	    this.imagePath = imagePath;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
	public Image getImage() {
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File(this.imagePath));
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		return image;
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
        entry.imagePath = imagePath;
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