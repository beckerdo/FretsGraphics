package frets.swing.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Unit tests to validate this class.  
 * @author <a href="mailto:dan@danbecker.info">Dan Becker</a>
 */
public class DisplayEntryModelTest {
	@Before
	public void setup() {
	}

	public static void populateFields( DisplayEntryModel dem ) {
	    dem.setRoot( "root" );
	    dem.setFormula( "formula" );
	    dem.setNotes( "notes" );
	    dem.setLocations( "locations" );
	    dem.setVariation( "variation" );
	    dem.setScore( "score" );
	    dem.setComments( "comments" );
	}
	
	@Test
	public void testFields() {
	    DisplayEntryModel dem = new DisplayEntryModel();

	    // Test null defaults
	    assertEquals( dem.getRoot(), null );
	    assertEquals( dem.getFormula(), null );
	    assertEquals( dem.getNotes(), null );
	    assertEquals( dem.getLocations(), null );
	    assertEquals( dem.getVariation(), null );
	    assertEquals( dem.getScore(), null );
	    assertEquals( dem.getComments(), null );

	    populateFields( dem );
	    
	    // Test setter defaults
	    assertEquals( dem.getRoot(), "root" );
	    assertEquals( dem.getFormula(), "formula" );
	    assertEquals( dem.getNotes(), "notes" );
	    assertEquals( dem.getLocations(), "locations" );
	    assertEquals( dem.getVariation(), "variation" );
	    assertEquals( dem.getScore(), "score" );
	    assertEquals( dem.getComments(), "comments" );
	}
}