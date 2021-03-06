package frets.swing.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import frets.main.Display;
import frets.main.Display.Hand;
import frets.main.Display.NotPlayedLocation;
import frets.main.Fretboard;
import frets.main.Location;
import frets.main.LocationList;
import frets.main.Note;
import frets.main.NoteList;
import frets.swing.model.ExtendedDisplayEntry;

/**
 * Renders graphical representation of fretboard, strings, frets, locations. 
 */
// TODO - Offset note so it appears slightly above the fret, not on it.
// TODO - String thicknesses, but it might need anti aliasing. 
// TODO - Wound strings?
public class RasterRenderer { 
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;

	/** Used for changine color alpha values. */
	public static final int NOT_GHOSTED = -1;
	
	public static final Shape upArrow = createArrow( 8, 8, 30, 270.0, 1.0 ); // 0 right, 90 down, 180 left, 270 up
	public static final Shape downArrow = createArrow( 8, 8, 30, 90.0, 1.0 ); // 0 right, 90 down, 180 left, 270 up
	public static final Shape leftArrow = createArrow( 8, 8, 30, 0.0, 1.0 ); // 0 right, 90 down, 180 left, 270 up
	public static final Shape rightArrow = createArrow( 8, 8, 30, 180.0, 1.0 ); // 0 right, 90 down, 180 left, 270 up
 
	protected BufferedImage bufferedImage;

	public RasterRenderer( Dimension size ) {
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
		displayOpts.notPlayed = EnumSet.of( NotPlayedLocation.HEAD );
		
		// Init graphics
		RasterRenderer rr = new RasterRenderer( size );
	    Graphics2D g2d = rr.bufferedImage.createGraphics();
	    g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
	    g2d.setRenderingHint( RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED );
	    // Background color is made opaque UI component. Make transparent here.
	    g2d.setColor( displayOpts.backgroundColor ); // can have alpha
	    g2d.fillRect(0, 0, size.width, size.height);
	    
		// Need to replace by dynamic calculations
	    int displayFretCount = displayOpts.getDisplayAreaFretAperture();

	    // Calc corners of fret diagram
	    Point fretMinStringMin = getLocationPoint( size, displayOpts, new Location( displayOpts.displayAreaMin.getString(), displayOpts.displayAreaMin.getFret() ) );
	    // System.out.println( "Location min string=" + displayOpts.displayAreaMin.getString() + ",fret=" + displayOpts.displayAreaMin.getFret() + ", point=" + fretMinStringMin );
	    Point fretMaxStringMax = getLocationPoint( size, displayOpts, new Location( displayOpts.displayAreaMax.getString(), displayOpts.displayAreaMax.getFret() ) ); 
	    // System.out.println( "Location max string=" + displayOpts.displayAreaMax.getString() + ",fret=" + displayOpts.displayAreaMax.getFret() + ", point=" + fretMaxStringMax );

	    // Draw fretboard background
	    Color [] colors = new Color[ 3 ];
	    colors[ 0 ] = changeBrightness( displayOpts.fretboardColor, -0.3f ); // 20% darker
	    colors[ 1 ] = displayOpts.fretboardColor;
	    colors[ 2 ] = changeBrightness( displayOpts.fretboardColor, -0.1f ); // 10% darker
	    paintGradient( g2d, Display.Orientation.VERTICAL == displayOpts.orientation, displayOpts.hand == Hand.RIGHT,
	    	fretMinStringMin, fretMaxStringMax, colors );

	    // Draw frets
	    g2d.setColor( displayOpts.fretColor);    
	    for( int freti = 0; freti <= displayFretCount;  freti++ ) {
		    Point fretBase = getLocationPoint( size, displayOpts, new Location( displayOpts.displayAreaMin.getString(), displayOpts.displayAreaMin.getFret() + freti ) ); 
	    	if (( 0 == freti ) && ( 0 == displayOpts.displayAreaMin.getFret() )) {
	    	    // Draw nut
	    	    g2d.setColor( displayOpts.nutColor);
	    		if (Display.Orientation.VERTICAL == displayOpts.orientation ) {
	    			for ( int t = -(displayOpts.nutThickness/2); t < (displayOpts.nutThickness/2); t++ ) {
	    	    	    // g2d.setColor( changeBrightness( displayOpts.nutColor, (float) t / 2.0f ));
	    				g2d.drawLine( fretMinStringMin.x, fretBase.y+t, fretMaxStringMax.x, fretBase.y+t);
	    			}
	    		} else if (Display.Orientation.HORIZONTAL == displayOpts.orientation ) {
	    			for ( int t = -(displayOpts.nutThickness/2); t < (displayOpts.nutThickness/2); t++ ) {
	    	    	    // g2d.setColor( changeBrightness( displayOpts.nutColor, (float) t / 2.0f ));
	    				g2d.drawLine( fretBase.x+t, fretMinStringMin.y, fretBase.x+t, fretMaxStringMax.y);
	    			}	    			
	    		}
	    	    g2d.setColor( displayOpts.fretColor);	    		
	    	} else {
	    		if (Display.Orientation.VERTICAL == displayOpts.orientation ) {
	    			for ( int t = -(displayOpts.fretThickness/2); t < (displayOpts.fretThickness/2); t++ ) {
	    	    	    // g2d.setColor( changeBrightness( displayOpts.fretColor, (float) t / 5.0f ));
	    				g2d.drawLine( fretMinStringMin.x, fretBase.y+t, fretMaxStringMax.x, fretBase.y+t);
	    			}
	    		} else if (Display.Orientation.HORIZONTAL == displayOpts.orientation ) {
	    			for ( int t = -(displayOpts.fretThickness/2); t < (displayOpts.fretThickness/2); t++ ) {
	    	    	    // g2d.setColor( changeBrightness( displayOpts.fretColor, (float) t / 5.0f ));
	    				g2d.drawLine( fretBase.x+t, fretMinStringMin.y, fretBase.x+t, fretMaxStringMax.y);
	    			}
	    		}
	    	}
	    }
	    
	    // Draw strings
	    // Color brighterString = displayOpts.stringColor.brighter().brighter();
	    // System.out.println( "Brighter=" + brighterString );
	    for( int stringi = displayOpts.displayAreaMin.getString(); stringi <= displayOpts.displayAreaMax.getString();  stringi++ ) {
	    	if (( stringi >= 0 ) && ( stringi < fretboard.getStringCount())) {
		    	Note openNote = fretboard.getString( stringi ).getOpenNote();
		    	int openNoteOctave = openNote.getOctave();
		    	// Mod string thickness for low octave strings.
		    	int stringThickness = displayOpts.stringThickness;
		    	switch ( openNoteOctave ) {
		    		case 0: stringThickness *= 3; break; 
		    		case 1: stringThickness *= 2; break; 
		    		case 2: stringThickness *= 1.5; break; 
		    	}
		    	// System.out.println( "String " + stringi + ", octave=" + openNoteOctave + ", thickness=" + stringThickness );
			    g2d.setColor( displayOpts.stringColor );
		    	
			    Point stringBase = getLocationPoint( size, displayOpts, new Location( stringi, displayOpts.displayAreaMin.getFret() ) ); 
	    		if (Display.Orientation.VERTICAL == displayOpts.orientation ) {
	    			for ( int t = -(stringThickness/2); t < (stringThickness/2); t++ ) {
	    				// if ((stringThickness > 2) && (t == 1)) { 
	    				//    g2d.setColor( brighterString );
	    				// }
	    				g2d.drawLine( stringBase.x+t, fretMinStringMin.y, stringBase.x+t, fretMaxStringMax.y);
	    			}
	    		} else if (Display.Orientation.HORIZONTAL == displayOpts.orientation ) {
	    			for ( int t = -(stringThickness/2); t < (stringThickness/2); t++ ) {
	    				// if ((stringThickness > 2) && (t == 1)) { 
	    				//    g2d.setColor( brighterString );
	    				// }
	    				
	    				g2d.drawLine( fretMinStringMin.x, stringBase.y+t, fretMaxStringMax.x, stringBase.y+t);
	    			}
	    		}
	    	} // stringi < getStringCount
	    }

	    // Draw dots on fretboard.
	    int middleString = fretboard.getStringCount() / 2 - 1;
	    // System.out.println( "RasterRenderer middle string=" + middleString );
	    if ( middleString > 0) {
	    	String locString = "ml3,ml5,ml7,ml9,X12,Y12,ml15,ml17,ml19,ml21";
	    	String midLoc = Integer.toString( middleString ) + "-";
	    	locString = locString.replaceAll( "ml", midLoc );
	    	String loLoc = Integer.toString( middleString - 1 ) + "-";
	    	locString = locString.replaceAll( "X", loLoc );
	    	String hiLoc = Integer.toString( middleString + 1 ) + "-";
	    	locString = locString.replaceAll( "Y", hiLoc );
	    	LocationList dots = new LocationList( locString ); 
	    	paintDots( fretboard, dots, g2d, size, displayOpts, 0x40, fretMinStringMin, fretMaxStringMax ); // Dots are ghosted to pick up fretboard color.
	    }
    
	    Note root = null;
	    String rootName = (String) entry.getMember( "Root" );
	    if (( null != rootName ) && ( rootName.length() > 0)) {
	    	root = new Note( rootName );
	    	root.setOctave( 0 ); // set low so intervals are positive.
	    }

	    // Get location list from this particular variation.
		LocationList locations = LocationList.parseString( (String) entry.getMember( "Locations" ));

	    // Draw octave variation locations with octaveAlpha ghosting
	    if ( displayOpts.showOctaveVariations ) {
	    	// NoteList fmin = new NoteList( lowF, "R-b3-5-b7" );
	    	String notesString = (String) entry.getMember( "Notes" );
	    	if (( null != notesString) && ( notesString.length() > 0 )) {
	    		NoteList notes =  new NoteList( notesString );
		    	if ( null != notes ) {
		    		List<LocationList> variations = fretboard.getOctaveVariations( notes );
	
		    		// Make a location list from all locations.
		    		LocationList variationLocations = new LocationList();
		    		for ( LocationList enharmonics : variations ) {
		    			variationLocations.addAll( enharmonics );	
		    		}
		    		int octaveAlpha = displayOpts.octavesAlpha.getAlpha();
		    		paintLocations( fretboard, variationLocations, root, g2d, size, displayOpts, octaveAlpha,
			 	       fretMinStringMin, fretMaxStringMax );
		    	}
	    	}
	    }
	    
	    // Draw enharmonic variation locations with enharmonicAlpha ghosting
	    if ( displayOpts.showEnharmonicVariations ) {
	    	// NoteList fmin = new NoteList( lowF, "R-b3-5-b7" );
	    	String notesString = (String) entry.getMember( "Notes" );
	    	if (( null != notesString) && ( notesString.length() > 0 )) {
	    		NoteList notes =  new NoteList( notesString );
		    	if ( null != notes ) {
		    		List<LocationList> variations = fretboard.getEnharmonicVariations( notes );
	
		    		// Make a location list from all locations.
		    		LocationList variationLocations = new LocationList();
		    		for ( LocationList enharmonics : variations ) {
		    			variationLocations.addAll( enharmonics );	
		    		}
		    		int enharmonicAlpha = displayOpts.enharmonicAlpha.getAlpha();
		    		paintLocations( fretboard, variationLocations, root, g2d, size, displayOpts, enharmonicAlpha,
			 	       fretMinStringMin, fretMaxStringMax );
		    	}
	    	}
	    }
	    
	    // Draw normal locations with no ghosting.
	    paintLocations( fretboard, locations, root, g2d, size, displayOpts, NOT_GHOSTED,
	       fretMinStringMin, fretMaxStringMax ); 

	    // Draw min fret numbers
	    if (!displayOpts.fretNumbering.isEmpty()) {
    		g2d.setColor( displayOpts.fretNumberColor );
		    int noteRadius = Math.min( size.width, size.height ) / 20;
    		Font font = new Font( "SansSerif", Font.PLAIN, noteRadius * 2 );
    		g2d.setFont( font );
		
    		String fretNum = Integer.toString(displayOpts.displayAreaMin.getFret());
    		TextLayout layout = new TextLayout( fretNum, font, g2d.getFontRenderContext() );
    		Rectangle2D stringBounds = layout.getBounds();

    		// System.out.println( "RasterRenderer string layout bounds=" + stringBounds );
	    	if ((0 != displayOpts.displayAreaMin.getFret()) || displayOpts.openStringDisplay ) {
    			if (displayOpts.fretNumbering.contains(Display.FretNumbering.FIRSTLEFT)) {
	    			Point loc = getLocationPoint( size, displayOpts, 0f, (float) displayOpts.displayAreaMin.getFret() );
   					g2d.drawString( fretNum, (int)(loc.x - stringBounds.getWidth())/2 - 1, 
   							                 loc.y + (int)(stringBounds.getHeight()/2) );
    			}
    			if (displayOpts.fretNumbering.contains(Display.FretNumbering.FIRSTRIGHT)) {
	    			Point loc = getLocationPoint( size, displayOpts, fretboard.getStringCount() - 1, (float) displayOpts.displayAreaMin.getFret() );
   					g2d.drawString( fretNum, (int)(loc.x + size.width - stringBounds.getWidth())/2, 
   							                 loc.y + (int)(stringBounds.getHeight()/2) );
    			}
	    	}
	    }

	    // Draw not played strings
	    if (!displayOpts.notPlayed.isEmpty()) {
	    	List<Integer> notPlayed = fretboard.getNotPlayedSet( locations );
    		if ( !notPlayed.isEmpty() ) {
	    		if ( null == displayOpts.notPlayedString )
	    			displayOpts.notPlayedString = "x";
    			g2d.setColor( displayOpts.fretNumberColor );
    		    int noteRadius = Math.min( size.width, size.height ) / 20;
    			Font font = new Font( "SansSerif", Font.BOLD, noteRadius * 2 );
    			g2d.setFont( font );
		
    			TextLayout layout = new TextLayout( displayOpts.notPlayedString, font, g2d.getFontRenderContext() );
    			Rectangle2D stringBounds = layout.getBounds();
    			// System.out.println( "RasterRenderer string layout bounds=" + stringBounds );
   				for ( int stringi : notPlayed ) {
   					if ( displayOpts.notPlayed.contains( NotPlayedLocation.HEAD )) {
       					Point loc = getLocationPoint( size, displayOpts, (float) stringi, displayOpts.displayAreaMin.getFret() );
       					// Point2D.Float point2d = new Point2D.Float( 0.0f, 0.0f );
       					if ( Display.Orientation.VERTICAL == displayOpts.orientation ) {
       						g2d.drawString( displayOpts.notPlayedString, 
       							loc.x - (float) stringBounds.getWidth()/2.0f, 
       							(float)loc.y/2.0f + (float)stringBounds.getHeight()/2.0f );
       					} else {
       						// horizontal
       						g2d.drawString( displayOpts.notPlayedString, 
           						(loc.x - (float)stringBounds.getWidth() - 3.0f)/2.0f, 
           						(float)loc.y + ((float) stringBounds.getHeight() / 2.0f) );       						
       					}
   					}  
   					if ( displayOpts.notPlayed.contains( NotPlayedLocation.FIRST )) {
       					Point loc = getLocationPoint( size, displayOpts, (float) stringi, displayOpts.displayAreaMin.getFret() + 0.5f );
       					if ( Display.Orientation.VERTICAL == displayOpts.orientation ) {
       						g2d.drawString( displayOpts.notPlayedString, 
       							loc.x - (float)stringBounds.getWidth()/2.0f, 
       							loc.y + (float)stringBounds.getHeight()/2.0f );
       					} else {
       						// horizontal
       						g2d.drawString( displayOpts.notPlayedString, 
           						(loc.x + (float)stringBounds.getWidth())/2.0f, 
           						(float)loc.y + ((float) stringBounds.getHeight() / 2.0f) );
       					}
   					}  
   				}
    		}	    	
	    }
	    
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
	 * Converts a fretboard location into a point location.
	 * <p>
	 * This version of the API can handle fractional and
	 * outside the display area strings and frets.
	 * For example, negative string and fret values can be used to
	 * get locations for fret numbering, open string values, and
	 * things that do not appear directly on the fretboard.
	 * <p> 
	 * The point location is influenced by display options:
	 * <li>orientation determines vertical or horizontal placement.
	 * <li>hand determines string and fret mirroring.
	 * <li>insets determines open space around fretboard.
	 * <li>display area min and max determine view portal.
	 * </ul>
	 */
	public static Point getLocationPoint( Dimension size, Display displayOpts, float string, float fret ) {
		// Note: y axis is inverted. 0 at top, height at base.
		if (Display.Orientation.VERTICAL == displayOpts.orientation ) {
			// right         left
			// mm----      ----mm
			// ------  vs. ------
			// ----MM      MM----
			Point minStringMinFret = new Point( displayOpts.insets.left, displayOpts.insets.top); // RIGHT
			Point maxStringMaxFret = new Point( size.width - displayOpts.insets.right, size.height - displayOpts.insets.bottom); // RIGHT
			if (Display.Hand.LEFT == displayOpts.hand) {
				minStringMinFret.x = size.width - displayOpts.insets.right;
				maxStringMaxFret.x = displayOpts.insets.left;
			}

			float stringDelta = (maxStringMaxFret.x - minStringMinFret.x)
				/ (displayOpts.displayAreaMax.getString() - displayOpts.displayAreaMin.getString());
			float x = minStringMinFret.x
				+ (string - displayOpts.displayAreaMin.getString()) * stringDelta;

			float fretDelta = (maxStringMaxFret.y - minStringMinFret.y)
				/ (displayOpts.displayAreaMax.getFret() - displayOpts.displayAreaMin.getFret());
			float y = minStringMinFret.y
				+ (fret - displayOpts.displayAreaMin.getFret()) * fretDelta;

			return new Point( Math.round( x ), Math.round( y ));
		} else if (Display.Orientation.HORIZONTAL == displayOpts.orientation ) { 
			// Note: y axis is inverted. 0 at top, height at base.
			// right       left
			// | | M      M | |
			// | | M  vs. M | |
			// m | |      | | m
			// m | |      | | m
			Point minStringMinFret = new Point( displayOpts.insets.left, size.height - displayOpts.insets.bottom ); // RIGHT
			Point maxStringMaxFret = new Point( size.width - displayOpts.insets.right, displayOpts.insets.top ); // RIGHT
			if (Display.Hand.LEFT == displayOpts.hand) {
				minStringMinFret.x = size.width - displayOpts.insets.right;
				maxStringMaxFret.x = displayOpts.insets.left;
			}

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
		return new Point( 0, 0 );		
	}

	/** 
	 * Converts a point location into the nearest fretboard location.
	 * The location may be occupied or unoccupied by a note.
	 * <p>
	 * This version of the API can handle points off the fretboard and finds
	 * the nearest location on the fretboard.
	 * <p> 
	 * The point location is influenced by display options:
	 * <li>orientation determines vertical or horizontal placement.
	 * <li>hand determines string and fret mirroring.
	 * <li>insets determines open space around fretboard.
	 * <li>display area min and max determine view portal.
	 * </ul>
	 *  
	 */
	public static Location getNearestLocation( Point p1, Dimension size, Display displayOpts, Fretboard fretboard)  {
		// System.out.println( "RasterRenderer loc=(" + p1.x + "," + p1.y + "), size=(" + size.getWidth() + "," + size.getHeight() + ")" );
		if ( null == fretboard )
			return null;

	    // Find every location and point on fretboard within the portal.
		// Consider caching location to point and point to location for each fretboard/size/displayOpts.
    	Map<Location,Point> locationToPoint = new HashMap<Location,Point>();
    	Map<Point,Location> pointToLocation = new HashMap<Point,Location>();
	    // Iterate through strings.
	    for( int stringi = displayOpts.displayAreaMin.getString(); stringi <= displayOpts.displayAreaMax.getString(); stringi++ ) {
	    	// Valid x location for vertical. Valid y location for horizontal.
		    Point stringBase = getLocationPoint( size, displayOpts, new Location( stringi, displayOpts.displayAreaMin.getFret() ) );
		    
    		// Iterate through frets
    	    int displayFretCount = displayOpts.getDisplayAreaFretAperture();
    	    for( int freti = 0; freti <= displayFretCount; freti++ ) {
    	    	// Valid x location for horizontal. Valid y location for vertical.
    		    Point fretBase = getLocationPoint( size, displayOpts, new Location( displayOpts.displayAreaMin.getString(), displayOpts.displayAreaMin.getFret() + freti ) );
    		    
    	    	Location location = new Location( stringi, freti );
    	    	Point point = null;
    	    	if (Display.Orientation.VERTICAL == displayOpts.orientation ) {
     	    	   point = new Point( stringBase.x, fretBase.y );
    	    	} else if (Display.Orientation.HORIZONTAL == displayOpts.orientation ) {
    	    	   point = new Point( fretBase.x, stringBase.y );
    	        }
    	    	
    	    	locationToPoint.put( location, point );
    	    	// System.out.println ( "RasterRenderer location=" + location + ", point=" + point );
    	    	pointToLocation.put( point, location );
    	    } // for each fret
	    } // for each string
        // System.out.println( "RasterRenderer location count=" + locationToPoint.size());
	    
        // Figure out min distance
	    double minDistance = Double.MAX_VALUE;
	    Location nearestLocation = null;
	    for ( Location location : locationToPoint.keySet()) {
	    	Point p2 = locationToPoint.get( location );
	    	if ( null != p2 ) {
	    		double distance = Math.sqrt( (p2.x-p1.x)*(p2.x-p1.x) + (p2.y-p1.y)*(p2.y-p1.y)  );
	    		if ( distance < minDistance ) {
	    			minDistance = distance;
	    			nearestLocation = location;
	    	    	// System.out.println ( "RasterRenderer min location=" + location + ", point=" + p2 );
	    		}
	    	}
	    }
		return nearestLocation;
	}
	
	/** Create an arrow shape. */
    public static Shape createArrow( int length, int barb, double barbAngleDegrees, double rotateDegrees, double scale ) {
        double barbAngle = Math.toRadians( barbAngleDegrees );
        Path2D.Double path = new Path2D.Double();
        path.moveTo(-length/2, 0);
        path.lineTo(length/2, 0);
        double x = length/2 - barb*Math.cos(barbAngle);
        double y = barb*Math.sin(barbAngle);
        path.lineTo(x, y);
        x = length/2 - barb*Math.cos(-barbAngle);
        y = barb*Math.sin(-barbAngle);
        path.moveTo(length/2, 0);
        path.lineTo(x, y);

        // Rotate and scale
        double rotateRadians = Math.toRadians( rotateDegrees );
		// AffineTransform at = AffineTransform.getTranslateInstance(x, y);
		AffineTransform at = AffineTransform.getRotateInstance(rotateRadians);  
		at.scale( scale, scale );
		Shape shape = at.createTransformedShape(path);
        return shape;
    }
    
    public static Shape translate( Shape shape, int x, int y ) {
		AffineTransform at = AffineTransform.getTranslateInstance(x, y);
		return at.createTransformedShape(shape);
    }
    
    /** Draw a fancy gradient color bar from one of the colorSets. */
    public static void paintGradient( Graphics2D g2, boolean vertical, boolean right,
    	final Point fretMinStringMin, final Point fretMaxStringMax, final Color [] colors ) {
    	// vertical
		// right         left
		// mm----      ----mm
		// ------  vs. ------
		// ----MM      MM----
    	// c0  cN      cN  c0
    	// horizontal
		// right       left
		// | | M cN    M | | cN
		// | | M   vs. M | |
		// m | |       | | m
		// m | | c0    | | m c0
        int w = fretMaxStringMax.x - fretMinStringMin.x;

        if ( vertical ) {
            int h = fretMaxStringMax.y - fretMinStringMin.y;
        	// Draw a number of gradients equal to the number of colors in the set.
        	float sliceWidth = w / (colors.length - 1);
        	if ( right ) {
        		for( int i = 0; i < colors.length - 1; i++ ) {
        			g2.setPaint( new GradientPaint( fretMinStringMin.x + sliceWidth * i, 		0, colors[ i ], 
        											fretMinStringMin.x + sliceWidth * ( i + 1 ), 0, colors[ i + 1 ]));
        			g2.fillRect( fretMinStringMin.x + ((int) sliceWidth * i),   	  fretMinStringMin.y,  
        						 (int) sliceWidth,                                    h );        	
        		}        		
        	} else {
        		for( int i = 0; i < colors.length - 1; i++ ) {
           			g2.setPaint( new GradientPaint( fretMinStringMin.x + sliceWidth * i, 		0, colors[ colors.length - 1 - i ], 
           											fretMinStringMin.x + sliceWidth * ( i + 1 ), 0, colors[colors.length - 2 - i ]));
           			g2.fillRect( fretMinStringMin.x + ((int) sliceWidth * i),   	  fretMinStringMin.y,  
           						(int) sliceWidth,                                     h );        	
        		}        		
        	}
        } else {
            int h = fretMinStringMin.y - fretMaxStringMax.y;
        	// Draw a number of gradients equal to the number of colors in the set.
        	float sliceHeight = h / (colors.length - 1);
        	for( int i = 0; i < colors.length - 1; i++ ) {
       			g2.setPaint( new GradientPaint( 0, fretMaxStringMax.y + sliceHeight * i, 		 colors[ colors.length - 1 - i ], 
												0, fretMaxStringMax.y + sliceHeight * ( i + 1 ), colors[colors.length - 2 - i ]));
       			g2.fillRect( fretMinStringMin.x, fretMaxStringMax.y + ((int) sliceHeight * i),  
       						 w, 				(int) sliceHeight);        	
        	}
        	
        }
    }
    
    /** Changes brightness by +/- percent (0.0 to 1.0 range). */
    public static Color changeBrightness( Color color, float percent ) {
       int newRed = color.getRed() + (int)(color.getRed() * percent);
       if (newRed < 0) newRed = 0;
       if (newRed >255) newRed = 255;
       int newGreen = color.getGreen() + (int)(color.getGreen() * percent);
       if (newGreen < 0) newGreen = 0;
       if (newGreen >255) newGreen = 255;
       int newBlue = color.getBlue() + (int)(color.getBlue() * percent);
       if (newBlue < 0) newBlue = 0;
       if (newBlue >255) newBlue = 255;
       Color newColor = new Color ( newRed, newGreen, newBlue );
       return newColor;
    }
    
    /** Changes color to monochromatic and given alpha. */
    public static Color changeMonoAlpha( Color color, float alpha ) {
       float mono = (0.2125f * color.getRed()) + (0.7154f * color.getGreen()) + (0.0721f * color.getBlue());
       if (mono < 0.0f) mono = 0.0f;
       if (mono > 1.0f) mono = 1.0f;
       Color newColor = new Color ( mono, mono, mono, alpha );
       return newColor;
    }
    
    /** Changes color to given alpha value. */
    public static Color changeAlpha( Color color, int alpha ) {
       Color newColor = new Color ( color.getRed(), color.getGreen(), color.getBlue(), alpha );
       return newColor;
    }
    
    /** Draw dots on fretboard. Similar to paintLocations except dots are moved up 1/2 string, down 1/2 fret.
     * If ghostAlpha is NOT_GHOSTED, displayOpts colors are used.
     * If ghostAlpha is not NOT_GHOSTED, displayOpts colors plus the new alpha value is used.
     */
    public static void paintDots( final Fretboard fretboard, final LocationList locations,  
    		Graphics2D g2d, final Dimension size, final Display displayOpts, int ghostAlpha,
        	final Point fretMinStringMin, final Point fretMaxStringMax ) {
	    if ( null == locations) return; 
	    	
	    // Infer note and font size from given size.
	    int noteRadius = Math.min( size.width, size.height ) / 30;
	    int locationRadius = noteRadius;
	    int locationDiameter = noteRadius * 2;
    	
    	for ( int locationi = 0; locationi < locations.size(); locationi++ ) {
    		Location location = locations.get(locationi);
            if ( NOT_GHOSTED == ghostAlpha )
            	g2d.setColor( Color.DARK_GRAY );
            else
            	g2d.setColor( changeAlpha( Color.DARK_GRAY, ghostAlpha ));

    		// Check for fret greater or less than min or max display fret.
    		if (( location.getFret() > displayOpts.displayAreaMin.getFret() ) &&
    			( location.getFret() <= displayOpts.displayAreaMax.getFret() )) {
    			// Inside fret window
    			Point point = getLocationPoint( size, displayOpts, location.getString() + 0.5f, location.getFret() - 0.5f );	    			
    			g2d.fillOval( point.x - locationRadius, point.y - locationRadius, locationDiameter, locationDiameter);

    			// Put interval or note name
    		} else {
    			// Outside fret window
    		}
    	}
   }

    /** 
     * Draw these locations on the given graphics. 
     * If ghostAlpha is NOT_GHOSTED, displayOpts colors are used.
     * If ghostAlpha is not NOT_GHOSTED, displayOpts colors plus the new alpha value is used.
     */
    public static void paintLocations( final Fretboard fretboard, final LocationList locations, Note root, 
    		Graphics2D g2d, final Dimension size, final Display displayOpts, int ghostAlpha,
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
            	if ( NOT_GHOSTED == ghostAlpha )
            		g2d.setColor( displayOpts.intervalColors[ intValue ] );
            	else
            		g2d.setColor( changeAlpha( displayOpts.intervalColors[ intValue ], ghostAlpha ));
            } else
            	g2d.setColor( displayOpts.defaultNoteColor );

    		// Check for fret greater or less than min or max display fret.
    		if (( location.getFret() >= displayOpts.displayAreaMin.getFret() ) &&
    			( location.getFret() <= displayOpts.displayAreaMax.getFret() )) {
    			// Inside fret window
    			Point point = getLocationPoint( size, displayOpts, location );
    			if ( displayOpts.noteShadows ) {
        			Color previousColor = g2d.getColor();
    				if ( NOT_GHOSTED == ghostAlpha )
    					g2d.setColor( displayOpts.noteShadowColor );
    				else
    					g2d.setColor( changeAlpha( displayOpts.noteShadowColor, ghostAlpha ));
		    		Point shadowOffset = new Point( noteRadius / 4, noteRadius / 4 );
	    			g2d.fillOval( point.x + shadowOffset.x - locationRadius, point.y + shadowOffset.y - locationRadius, locationDiameter, locationDiameter);
	    			g2d.setColor( previousColor );
    			}
    			g2d.fillOval( point.x - locationRadius, point.y - locationRadius, locationDiameter, locationDiameter);
    			
    			// Put interval or note name
	    		String noteString = null;
	    		if ( null == root ) {
	    			noteString = thisNote.toString();
	            	g2d.setColor( displayOpts.defaultNoteTextColor );
	    		} else {
	    			noteString = Note.getQualityName( thisNote, root );
	            	int intValue = thisNote.getQuality( root ) % displayOpts.intervalColors.length;
	            	if ( NOT_GHOSTED == ghostAlpha )
	            		g2d.setColor( displayOpts.intervalTextColors[ intValue ] );
	            	else
	            		g2d.setColor( changeAlpha( displayOpts.intervalTextColors[ intValue ], ghostAlpha ));
	    		}

			    TextLayout layout = new TextLayout( noteString, textFont, g2d.getFontRenderContext() );
				Rectangle2D stringBounds = layout.getBounds();
				Point fretLoc = new Point( 
					point.x -  (int) (stringBounds.getWidth() / 2.0 ) - 1,
					point.y  + (int)(stringBounds.getHeight() / 2.0));
	    		g2d.drawString( noteString, fretLoc.x, fretLoc.y);
    		} else {
    			// Outside fret window
    			if ( location.getFret() < displayOpts.displayAreaMin.getFret() ) {
	    			Location virtualLocation = new Location( location.getString(), displayOpts.displayAreaMin.getFret() );
	    			Point point = getLocationPoint( size, displayOpts, virtualLocation );
	        		if (Display.Orientation.VERTICAL == displayOpts.orientation ) {
	        			g2d.draw( translate( upArrow, point.x, point.y - 5 ) );
	        		} else if (Display.Orientation.HORIZONTAL == displayOpts.orientation ) {
	    				if ( displayOpts.hand == Hand.RIGHT )
	    					g2d.draw( translate( rightArrow, point.x + 5, point.y ) );
	    				else if ( displayOpts.hand == Hand.LEFT )
	    					g2d.draw( translate( leftArrow, point.x - 5, point.y ) );

	        		}
    			} else if ( location.getFret() > displayOpts.displayAreaMax.getFret() ) {
	    			Location virtualLocation = new Location( location.getString(), displayOpts.displayAreaMax.getFret() );
	    			Point point = getLocationPoint( size, displayOpts, virtualLocation );
	        		if (Display.Orientation.VERTICAL == displayOpts.orientation ) {
	        			g2d.draw( translate( downArrow, point.x, point.y - 5 ) );
	        		} else if (Display.Orientation.HORIZONTAL == displayOpts.orientation ) {
	    				if ( displayOpts.hand == Hand.RIGHT )
	    					g2d.draw( translate( leftArrow, point.x - 5, point.y ) );
	    				else if ( displayOpts.hand == Hand.LEFT )
	    					g2d.draw( translate( rightArrow, point.x + 5, point.y ) );

	        		}
    			}	    			
    		}
    	}
   }
}