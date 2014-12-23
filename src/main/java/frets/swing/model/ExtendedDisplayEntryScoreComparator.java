package frets.swing.model;

import java.util.Comparator;

import frets.main.ChordRank;

/**
 * Compares two entry via score fields.
 */
public class ExtendedDisplayEntryScoreComparator implements Comparator<ExtendedDisplayEntry> {

	/**
	 * 
     * @return a negative integer, zero, or a positive integer as the
     *         first argument is less than, equal to, or greater than the
     *         second. 
	 */
	@Override
	public int compare(ExtendedDisplayEntry o1, ExtendedDisplayEntry o2) {
		if (( o1 == null ) && (o2 == null)) return 0;
		if ( o1 == null ) return Integer.MIN_VALUE;
		if ( o2 == null ) return Integer.MAX_VALUE;
		
		String o1ScoreString = (String) o1.getMember( "Score" ); 
		String o2ScoreString = (String) o2.getMember( "Score" ); 
		if (( o1ScoreString == null ) && (o2ScoreString == null)) return 0;
		if ( o1ScoreString == null ) return Integer.MIN_VALUE;
		if ( o2ScoreString == null ) return Integer.MAX_VALUE;

		int [] o1Score = ChordRank.toScores(o1ScoreString);
		int [] o2Score = ChordRank.toScores(o2ScoreString);
		if (( o1Score == null ) && (o2Score == null)) return 0;
		if ( o1Score == null ) return Integer.MIN_VALUE;
		if ( o2Score == null ) return Integer.MAX_VALUE;
				
		if (( o1Score.length == 0 ) && (o2Score.length == 0 )) return 0;
		if ( o1Score.length == 0 ) return Integer.MIN_VALUE;
		if ( o2Score.length == 0 ) return Integer.MAX_VALUE;

		return o1Score[ 0 ] - o2Score[ 0 ];
	}
}