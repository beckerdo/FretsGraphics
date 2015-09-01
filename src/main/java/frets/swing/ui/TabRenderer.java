package frets.swing.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import frets.main.Display;
import frets.main.Fretboard;
import frets.main.Location;
import frets.main.LocationList;
import frets.main.Note;
import frets.swing.model.ExtendedDisplayEntry;

/**
 * Renders graphical representation of tabs on a string
 */
public class TabRenderer { 
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;
	
	/** Less than this will diminish width, show first and last. */
	public static final int MAX_LOCATIONS = 16; 
	public static final int FIRST_LOCATIONS = 11;
	public static final int LAST_LOCATIONS = 4;

	protected BufferedImage bufferedImage;

	public TabRenderer( Dimension size ) {
		setSize( size );
	}
	
	public void setSize( Dimension size ) {
	    bufferedImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);		
	}
		
	// Returns a generated image.
	public static BufferedImage renderImage(Dimension size, Display displayOpts, Fretboard fretboard, ExtendedDisplayEntry entry) {
		if (( null == size) || (size.width < 1) || (size.height < 1))
			return null;
    	// System.out.println( "RasterRenderer.renderImage size=" + size );

		// displayOpts.orientation = Orientation.VERTICAL;
		// displayOpts.orientation = Orientation.HORIZONTAL;
		displayOpts.insets = new Insets( 15, 15, 15, 15  );  // top, left, bottom, right
		// displayOpts.hand = Hand.LEFT;
		
		// Init graphics
		TabRenderer rr = new TabRenderer( size );
	    Graphics2D g2d = rr.bufferedImage.createGraphics();
	    g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
	    g2d.setRenderingHint( RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED );
	    // Background color is made opaque UI component. Make transparent here.
	    g2d.setColor( displayOpts.backgroundColor ); // can have alpha
	    g2d.fillRect(0, 0, size.width, size.height);

	    // Get location list from this particular variation.
		LocationList locations = LocationList.parseString( (String) entry.getMember( "Locations" ));
	    
	    // Calc corners of fret diagram
	    // Point fretMinStringMin = TabRenderer.getLocationPoint( size, displayOpts, new Location( displayOpts.displayAreaMin.getString(), displayOpts.displayAreaMin.getFret() ) );
		Point fretMinStringMin = new Point( displayOpts.insets.left, size.height - displayOpts.insets.bottom ); // RIGHT
	    System.out.println( "Location min string=" + displayOpts.displayAreaMin.getString() + ",fret=" + displayOpts.displayAreaMin.getFret() + ", point=" + fretMinStringMin );
	    // Point fretMaxStringMax = TabRenderer.getLocationPoint( size, displayOpts, new Location( displayOpts.displayAreaMax.getString(), displayOpts.displayAreaMax.getFret() ) ); 
		Point fretMaxStringMax = new Point( size.width - displayOpts.insets.right, displayOpts.insets.top ); // RIGHT	    System.out.println( "Location min string=" + displayOpts.displayAreaMin.getString() + ",fret=" + displayOpts.displayAreaMin.getFret() + ", point=" + fretMinStringMin );
	    System.out.println( "Location max string=" + displayOpts.displayAreaMax.getString() + ",fret=" + displayOpts.displayAreaMax.getFret() + ", point=" + fretMaxStringMax );

		if (null != locations && locations.size() > 0) {
		    // System.out.println( "Location size=" + locations.size() );
		    
			double xCenter = ( fretMaxStringMax.getX() - fretMinStringMin.getX()) / 2.0;
			double xNoteDelta = ( fretMaxStringMax.getX() - fretMinStringMin.getX()) / MAX_LOCATIONS;
			double xHalfDelta = (locations.size() % 2) == 1 ? 0 : (xNoteDelta / 2.0); // 0 for odd, half delta for even
		    // System.out.println( "Tab center=" + xCenter + ",noteDelta=" + xNoteDelta + ", halfDelta=" + xHalfDelta );

		    // Calc corners including location of min and max notes.
		    // Point noteMinStringMin = TabRenderer.getLocationPoint( size, displayOpts, new Location( displayOpts.displayAreaMin.getString(), displayOpts.displayAreaMin.getFret() ) );
		    // if ( locations.size() < MAX_LOCATIONS ) {
		    // 	double xMin =  xCenter - ((locations.size() / 2) * xNoteDelta) + xHalfDelta; 
		    // 	noteMinStringMin.setLocation(xMin, noteMinStringMin.getY());
		    // }
		    Point noteMinStringMin = TabRenderer.getTabPoint( size, displayOpts, displayOpts.displayAreaMin.getString(), 0, MAX_LOCATIONS );
		    System.out.println( "Location noteMinStringMin=" + noteMinStringMin );
		    // Point noteMaxStringMax = TabRenderer.getLocationPoint( size, displayOpts, new Location( displayOpts.displayAreaMax.getString(), displayOpts.displayAreaMax.getFret() ) ); 
		    // if ( locations.size() < MAX_LOCATIONS ) {
		    // 	double xMax =  xCenter + ((locations.size() / 2) * xNoteDelta) - xHalfDelta; 
		    // 	noteMaxStringMax.setLocation(xMax, noteMaxStringMax.getY());
		    // }
		    Point noteMaxStringMax = TabRenderer.getTabPoint( size, displayOpts, displayOpts.displayAreaMax.getString(), locations.size(), locations.size() ); 
		    System.out.println( "Location noteMaxStringMax=" + noteMaxStringMax );
		    
		    // Draw strings
		    // Color brighterString = displayOpts.stringColor.brighter().brighter();
		    // System.out.println( "Brighter=" + brighterString );
		    for( int stringi = displayOpts.displayAreaMin.getString(); stringi <= displayOpts.displayAreaMax.getString();  stringi++ ) {
		    	if (( stringi >= 0 ) && ( stringi < fretboard.getStringCount())) {
			    	// Mod string thickness for low octave strings.
			    	int stringThickness = displayOpts.stringThickness;
			    	// System.out.println( "String " + stringi + ", octave=" + openNoteOctave + ", thickness=" + stringThickness );
				    g2d.setColor( displayOpts.stringColor );
			    	
					Point stringBase = TabRenderer.getTabPoint( size, displayOpts, stringi, 0, MAX_LOCATIONS );
				    // Point stringBase = TabRenderer.getLocationPoint( size, displayOpts, new Location( stringi, displayOpts.displayAreaMin.getFret() ) ); 
		    		for ( int t = -(stringThickness/2); t < (stringThickness/2); t++ ) {
		    			g2d.drawLine( noteMinStringMin.x, stringBase.y+t, noteMaxStringMax.x, stringBase.y+t);
		    		}
		    	} // stringi < getStringCount
		    }
	    
		    Note root = null;
		    String rootName = (String) entry.getMember( "Root" );
		    if (( null != rootName ) && ( rootName.length() > 0)) {
		    	root = new Note( rootName );
		    	root.setOctave( 0 ); // set low so intervals are positive.
		    }
		    
		    // Draw normal locations with no ghosting.
		    paintLocations( fretboard, locations, root, g2d, size, displayOpts,
		       fretMinStringMin, fretMaxStringMax ); 
		} // locations.size() > 0

	    // Graphics context no longer needed so dispose it
	    g2d.dispose();

	    return rr.bufferedImage;
	}    

	/** 
	 * Converts a fretboard location into a point location.
	 * The point location is influenced by display options as documented
	 * in @see {@link RasterRenderer#getLocationPoint(Dimension, Display, float, float)}.
	 */
	public static Point getLocationPoint( Dimension size, Display displayOpts, Location location ) {
		return getLocationPoint( size, displayOpts, location.getString(), location.getFret() );		
	}
	
	/** 
	 * Converts a tab location into a point location.
	 * <p>
	 * This version of the API can handle fractional and
	 * outside the display area strings and notes.
	 * For example, negative string and fret values can be used to
	 * get locations for fret numbering, open string values, and
	 * things that do not appear directly on the fretboard.
	 * <p> 
	 * The point location is influenced by display options:
	 * <li>insets determines open space around fretboard.
	 * <li>display area min and max determine view portal.
	 * </ul>
	 * <p>
	 * Unlike FretRenderer:
	 * <ul>
	 * <li>where it says fret, location number is implied 
	 * <li>this API does not pay attention to left/right, vertical/horizontal display options.
	 * </ul>
	 */
	public static Point getLocationPoint( Dimension size, Display displayOpts, float string, float fret ) {
		// Note: y axis is inverted. 0 at top, height at base.
			// Note: y axis is inverted. 0 at top, height at base.
			Point minStringMinFret = new Point( displayOpts.insets.left, size.height - displayOpts.insets.bottom ); // RIGHT
			Point maxStringMaxFret = new Point( size.width - displayOpts.insets.right, displayOpts.insets.top ); // RIGHT

			float stringDelta = (maxStringMaxFret.y - minStringMinFret.y)
				/ (displayOpts.displayAreaMax.getString() - displayOpts.displayAreaMin.getString());
			float y = minStringMinFret.y
				+ (string - displayOpts.displayAreaMin.getString()) * stringDelta;

			float fretDelta = (maxStringMaxFret.x - minStringMinFret.x)
				/ (displayOpts.displayAreaMax.getFret() - displayOpts.displayAreaMin.getFret());
			float x = minStringMinFret.x
				+ (fret - displayOpts.displayAreaMin.getFret()) * fretDelta;

			return new Point( Math.round( x ), Math.round( y ));
	}
	
	/** 
	 * Converts a tab location into a point location.
	 * <p>
	 * This version of the API can handle fractional and
	 * outside the display area strings and notes.
	 * For example, negative string and fret values can be used to
	 * get locations for fret numbering, open string values, and
	 * things that do not appear directly on the fretboard.
	 * <p> 
	 * The point location is influenced by display options:
	 * <li>insets determines open space around fretboard.
	 * <li>display area min and max determine view portal.
	 * </ul>
	 * <p>
	 * Unlike FretRenderer:
	 * <ul>
	 * <li>does not go by fret, goes by position in location 
	 * <li>this API does not pay attention to left/right, vertical/horizontal display options.
	 * </ul>
	 */
	public static Point getTabPoint( Dimension size, Display displayOpts, float string, int loci, int locations ) {
		// Note: y axis is inverted. 0 at top, height at base.
		Point fretMinStringMin = new Point( displayOpts.insets.left, size.height - displayOpts.insets.bottom ); // RIGHT
		Point fretMaxStringMax = new Point( size.width - displayOpts.insets.right, displayOpts.insets.top ); // RIGHT

		float stringDelta = (fretMaxStringMax.y - fretMinStringMin.y)
			/ (displayOpts.displayAreaMax.getString() - displayOpts.displayAreaMin.getString());
		float y = fretMinStringMin.y
				+ (string - displayOpts.displayAreaMin.getString()) * stringDelta;

		double xCenter = ( fretMaxStringMax.getX() - fretMinStringMin.getX()) / 2.0f;
		double xNoteDelta = ( fretMaxStringMax.getX() - fretMinStringMin.getX()) / locations;
		// System.out.println( "Tab center=" + xCenter + ",noteDelta=" + xNoteDelta + ", halfDelta=" + xHalfDelta );

		int tabLocations = Math.min( locations, MAX_LOCATIONS );
		double tabWidth = tabLocations * xNoteDelta;
		double x = xCenter - (tabWidth/2.0) + (xNoteDelta*loci);
		
		return new Point( (int) Math.round( x ), Math.round( y ) );
	}	

    /** 
     * Draw these locations on the given graphics.
     * <p>
     * Differs from RasterRenderer:
     * <ul>
     * <li>no GHOSTING
     * </ul> 
     */
    public static void paintLocations( final Fretboard fretboard, final LocationList locations, Note root, 
    		Graphics2D g2d, final Dimension size, final Display displayOpts, 
        	final Point fretMinStringMin, final Point fretMaxStringMax ) {
	    if ( null == locations) return;
	    
	    // Infer note and font size from given size.
	    int noteRadius = Math.min( size.width, size.height ) / 20;
		int fontSize = noteRadius * 2;
		Font textFont = new Font( "SansSerif", Font.BOLD, fontSize );
		g2d.setFont( textFont );
	    int locationRadius = noteRadius;
	    int locationDiameter = noteRadius * 2;
    	
    	for ( int locationi = 0; locationi < locations.size(); locationi++ ) {
    		Location location = locations.get(locationi);
            Note thisNote = location.getNote( fretboard );
            if ( null != root ) {
            	int intValue = thisNote.getQuality( root ) % displayOpts.intervalColors.length;
            		g2d.setColor( displayOpts.intervalColors[ intValue ] );
            } else
            	g2d.setColor( displayOpts.defaultNoteColor );

    		// Check for fret greater or less than min or max display fret.
    		if (( location.getFret() >= displayOpts.displayAreaMin.getFret() ) &&
    			( location.getFret() <= displayOpts.displayAreaMax.getFret() )) {
    			// Inside fret window
    			Point point = getLocationPoint( size, displayOpts, location );
    			if ( displayOpts.noteShadows ) {
        			Color previousColor = g2d.getColor();
        			g2d.setColor( displayOpts.noteShadowColor );
		    		Point shadowOffset = new Point( noteRadius / 4, noteRadius / 4 );
	    			g2d.fillOval( point.x + shadowOffset.x - locationRadius, point.y + shadowOffset.y - locationRadius, locationDiameter, locationDiameter);
	    			g2d.setColor( previousColor );
    			}
    			g2d.fillOval( point.x - locationRadius, point.y - locationRadius, locationDiameter, locationDiameter);
    			
    			// Put interval or note name
	    		String noteString = Integer.toString( location.getFret() );
	    		if ( null == root ) {
	    			// noteString = Integer.toString( location.getFret() );
	            	g2d.setColor( displayOpts.defaultNoteTextColor );
	    		} else {
	    			// noteString = Note.getQualityName( thisNote, root );
	    			int intValue = thisNote.getQuality( root ) % displayOpts.intervalColors.length;
	            		g2d.setColor( displayOpts.intervalTextColors[ intValue ] );
	    		}

			    TextLayout layout = new TextLayout( noteString, textFont, g2d.getFontRenderContext() );
				Rectangle2D stringBounds = layout.getBounds();
				Point fretLoc = new Point( 
					point.x -  (int) (stringBounds.getWidth() / 2.0 ) - 1,
					point.y  + (int)(stringBounds.getHeight() / 2.0));
	    		g2d.drawString( noteString, fretLoc.x, fretLoc.y);
    		}
    	}
   }
}