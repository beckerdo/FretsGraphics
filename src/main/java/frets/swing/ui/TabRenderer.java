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
 * Renders graphical respresentation of tabs on a fretboard
 */
public class TabRenderer { 
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;

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
		displayOpts.notPlayed = EnumSet.of( NotPlayedLocation.HEAD );
		
		// Init graphics
		TabRenderer rr = new TabRenderer( size );
	    Graphics2D g2d = rr.bufferedImage.createGraphics();
	    g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
	    g2d.setRenderingHint( RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED );
	    // Background color is made opaque UI component. Make transparent here.
	    g2d.setColor( displayOpts.backgroundColor ); // can have alpha
	    g2d.fillRect(0, 0, size.width, size.height);

	    // Calc corners of fret diagram
	    Point fretMinStringMin = RasterRenderer.getLocationPoint( size, displayOpts, new Location( displayOpts.displayAreaMin.getString(), displayOpts.displayAreaMin.getFret() ) );
	    System.out.println( "Location min string=" + displayOpts.displayAreaMin.getString() + ",fret=" + displayOpts.displayAreaMin.getFret() + ", point=" + fretMinStringMin );
	    Point fretMaxStringMax = RasterRenderer.getLocationPoint( size, displayOpts, new Location( displayOpts.displayAreaMax.getString(), displayOpts.displayAreaMax.getFret() ) ); 
	    System.out.println( "Location max string=" + displayOpts.displayAreaMax.getString() + ",fret=" + displayOpts.displayAreaMax.getFret() + ", point=" + fretMaxStringMax );
	    
	    // Draw strings
	    // Color brighterString = displayOpts.stringColor.brighter().brighter();
	    // System.out.println( "Brighter=" + brighterString );
	    for( int stringi = displayOpts.displayAreaMin.getString(); stringi <= displayOpts.displayAreaMax.getString();  stringi++ ) {
	    	if (( stringi >= 0 ) && ( stringi < fretboard.getStringCount())) {
		    	Note openNote = fretboard.getString( stringi ).getOpenNote();
		    	int openNoteOctave = openNote.getOctave();
		    	// Mod string thickness for low octave strings.
		    	int stringThickness = displayOpts.stringThickness;
		    	// System.out.println( "String " + stringi + ", octave=" + openNoteOctave + ", thickness=" + stringThickness );
			    g2d.setColor( displayOpts.stringColor );
		    	
			    Point stringBase = RasterRenderer.getLocationPoint( size, displayOpts, new Location( stringi, displayOpts.displayAreaMin.getFret() ) ); 
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
    
	    Note root = null;
	    String rootName = (String) entry.getMember( "Root" );
	    if (( null != rootName ) && ( rootName.length() > 0)) {
	    	root = new Note( rootName );
	    	root.setOctave( 0 ); // set low so intervals are positive.
	    }

	    // Get location list from this particular variation.
		LocationList locations = LocationList.parseString( (String) entry.getMember( "Locations" ));

	    // Graphics context no longer needed so dispose it
	    g2d.dispose();

	    return rr.bufferedImage;
	}    
	

}