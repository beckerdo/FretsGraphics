package frets.swing.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;


/**
 * Unit tests to validate this class.  
 * @author <a href="mailto:dan@danbecker.info">Dan Becker</a>
 */
public class ExtendedDisplayEntryTest {
	@Before
	public void setup() {
	}

	@Test
	public void testMultiValue() {
	    DisplayEntryModel dem = new DisplayEntryModel();
	    DisplayEntryModelTest.populateFields(dem);

	    ExtendedDisplayEntry ede = new ExtendedDisplayEntry( dem );
	    
	    // Test MultiValue interface
	    assertEquals( 8, ede.getMemberCount() );
	    assertEquals( "root", ede.getMember( "Root" )); // concat of "get" plus member name.
	    assertEquals( "comments", ede.getMember( "Comments" ));
	    
	    String [] memberNames = ede.getMemberNames();
	    assertNotNull( memberNames );
	    assertEquals( 8, memberNames.length );
	    assertEquals( "Formula", memberNames[ 1 ] );
	    assertEquals( "Notes", memberNames[ 2 ] );
	    
	    assertEquals( "Locations", ede.getMemberName( 3 ) );
	    assertEquals( "Variation", ede.getMemberName( 4 ) );

	    assertEquals( String.class, ede.getMemberClass( 3 ) );
	    assertEquals( String.class, ede.getMemberClass( 4 ) );
	    
	    assertEquals( String.class, ede.getMemberClass( "Score" ) );
	    assertEquals( String.class, ede.getMemberClass( "Comments" ) );
	    
	    assertEquals( "score", ede.getMember( 5 ) );
	    assertEquals( "comments", ede.getMember( 6 ) );

	    assertEquals( ".", ede.getMember( "ImagePath" ) );
	    ede.setMember( "ImagePath", ".." );
	    assertEquals( "..", ede.getMember( "ImagePath" ) );
	    ede.setMember( 7, "." );
	    assertEquals( ".", ede.getMember( "ImagePath" ) );
	}

	@Test
	public void testPropertyChange() {
	    DisplayEntryModel dem = new DisplayEntryModel();
	    DisplayEntryModelTest.populateFields(dem);

	    ExtendedDisplayEntry ede = new ExtendedDisplayEntry( dem );
	    BeanListener bl = new BeanListener();
	    
	    assertEquals( null, bl.getChange() );
	    ede.setMember( "Comments", "change 1" );
	    assertEquals( null, bl.getChange() );

	    
	    ede.addPropertyChangeListener( bl );
	    ede.setMember( "Comments", "change 2" );
	    
	    PropertyChangeEvent actual = bl.getChange();
	    PropertyChangeEvent expected = new PropertyChangeEvent( ede, "Comments", "change 1", "change 2" );
	    // PropertyChangeEvent does not have a semantic equals.
	    // assertEquals( expected, e );
	    assertEquals( expected.toString(), actual.toString() );
	    
	    ede.removePropertyChangeListener( bl );
	    ede.setMember( "Comments", "change 3" );
	    assertEquals( expected.toString(), bl.getChange().toString() );
	}
	
	public class BeanListener implements PropertyChangeListener {   
	   // PropertyChangeListener
		public void propertyChange(PropertyChangeEvent evt) {
            this.change = evt;
		}
			
        public PropertyChangeEvent getChange(){
        	return this.change;
        }
        protected PropertyChangeEvent change;
   }	

}