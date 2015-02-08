package frets.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;


/**
 * Unit tests to validate this class.
 * 
 * Want to make sure dependency property files are available
 * whether open dependency project with target file system or dependency in a JAR.
 * 
 * @author <a href="mailto:dan@danbecker.info">Dan Becker</a>
 */
public class FretboardTest {
	@Test
	public void testProperties() {
		String location = "fretboard.guitar.properties";
		Fretboard instance = Fretboard.getInstanceFromFileName(location);
		
		Fretboard test = instance.getInstance(Fretboard.STANDARD);
		assertEquals("Name", "Guitar, Standard", test.getMetaName());
		assertEquals("Description", "Guitar, Standard, E-A-D-G-B-E", test.getMetaDescription());

		test = instance.getInstance(Fretboard.OPEN_D);
		assertEquals("Name", "Guitar, Open D", test.getMetaName());
		assertEquals("Description", "Guitar, Open D, D-A-D-F#-A-D", test.getMetaDescription());

		test = instance.getInstance(Fretboard.OPEN_G);
		assertEquals("Name", "Guitar, Open G", test.getMetaName());
		assertEquals("Description", "Guitar, Open G, D-G-D-G-B-D", test.getMetaDescription());
	}

	@Test
	public void testGetResourceListing() {
		try {
			String[] gNames = Fretboard.getResourceListing(Fretboard.PROP_PATH, "fretboard[.]guitar.*[.]properties");
			assertEquals("Name", 7, gNames.length);
			String[] bassNames = Fretboard.getResourceListing(Fretboard.PROP_PATH, "fretboard[.]bass.*[.]properties");
			assertEquals("Name", 3, bassNames.length);
			String[] bariNames = Fretboard.getResourceListing(Fretboard.PROP_PATH, "fretboard[.]bari.*[.]properties");
			assertEquals("Name", 3, bariNames.length);
			String[] ukeNames = Fretboard.getResourceListing(Fretboard.PROP_PATH, "fretboard[.]uke.*[.]properties");
			assertEquals("Name", 6, ukeNames.length);
		} catch (Exception e) {
			assertNull("Exception=" + e, e);
		}
	}
}