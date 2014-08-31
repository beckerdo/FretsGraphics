package frets.util;

import java.io.File;
import java.io.FilenameFilter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A file name filter that works from regular expressions.
 *
* @author <a href="mailto:dan@danbecker.info">Dan Becker</a>
 */
public class FilenameRegExFilter implements FilenameFilter {

	public FilenameRegExFilter() {
		
	}
	public FilenameRegExFilter( String filterRegEx ) {
       setFilterRegEx( filterRegEx );		
	}
	
	public String getFilterRegEx() {
		return filterRegEx;
	}
	public void setFilterRegEx(String filterRegEx) {
		pattern = Pattern.compile(filterRegEx); 
		this.filterRegEx = filterRegEx;
	}
	public boolean accept(File dir, String name) {
		Matcher matcher = pattern.matcher(name); 
		boolean matchFound = matcher.matches();
		// System.out.println( "File name=" + name + ", pattern=" + filterRegEx + ", match=" + matchFound );
		return matchFound;
	}

	protected Pattern pattern;
	protected String filterRegEx;
}