package frets.swing.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import swingextensions.ui.VerticalPercentileBar;
import frets.main.Display;
import frets.main.Fretboard;
import frets.swing.model.ExtendedDisplayEntry;

/**
 * ListCellRenderer for the DisplayEntrys. 
 * @deprecated - Too busy and too "dissociated" from data.
 */
public final class EntryListCellRenderer extends JPanel implements ListCellRenderer {
	private static final long serialVersionUID = 1L;

    public static int MIN_IMAGE_SIZE = 50;
    
    private JLabel rootLabel;
    private JLabel formulaLabel;
    private JLabel notesLabel;
    private JLabel locationsLabel;
    private JLabel variationLabel;
    private JLabel scoreLabel;
    private JLabel imageLabel;
    private VerticalPercentileBar percentileBar;

    protected int maxScore = Integer.MIN_VALUE;
    protected int minScore = Integer.MAX_VALUE;
    
    // Needed to help render entry
    protected Fretboard fretboard;
    protected Display displayOpts;
    
    protected Map<ExtendedDisplayEntry,ImageIcon> cachedIcons = new HashMap<ExtendedDisplayEntry,ImageIcon>(); 
    
    protected EntryListCellRenderer() {
        rootLabel = new JLabel(" ");
        formulaLabel = new JLabel(" ");
        notesLabel = new JLabel(" ");
        locationsLabel = new JLabel(" ");
        variationLabel = new JLabel(" ");
        scoreLabel = new JLabel(" ");        
        imageLabel = new JLabel();
        imageLabel.setOpaque(true);
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageLabel.setVerticalAlignment(JLabel.CENTER);
        imageLabel.setBackground(Color.WHITE);
        imageLabel.setBorder(new CompoundBorder(
        		new LineBorder(Color.BLACK, 1),new EmptyBorder(1, 1, 1, 1)));
        imageLabel.setSize( new Dimension( 2 * MIN_IMAGE_SIZE, 2 * MIN_IMAGE_SIZE) );
        imageLabel.setPreferredSize( new Dimension( MIN_IMAGE_SIZE, MIN_IMAGE_SIZE) );
        percentileBar = new VerticalPercentileBar();
        percentileBar.setOpaque(true);
        percentileBar.setBackground(Color.WHITE);
        percentileBar.setBorder(new LineBorder(Color.BLACK));

        GroupLayout layout = new GroupLayout(this);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        this.setLayout(layout);        
        GroupLayout.SequentialGroup hg = layout.createSequentialGroup();
        layout.setHorizontalGroup(hg);
        hg.
        	addComponent(imageLabel, MIN_IMAGE_SIZE, MIN_IMAGE_SIZE, 2 * MIN_IMAGE_SIZE ). // min, pref, max
            // addComponent(imageLabel).
            addGroup(layout.createParallelGroup().
            addComponent(rootLabel, 10, 12, Integer.MAX_VALUE).
            addComponent(formulaLabel, 10, 12, Integer.MAX_VALUE).
            addComponent(notesLabel, 10, 12, Integer.MAX_VALUE).
            addComponent(locationsLabel, 10, 12, Integer.MAX_VALUE).
            addComponent(variationLabel, 10, 12, Integer.MAX_VALUE).
            addComponent(scoreLabel, 10, 12, Integer.MAX_VALUE)).
            addComponent(percentileBar, 18, 18, 18);
        
        GroupLayout.ParallelGroup vg = layout.createParallelGroup();
        GroupLayout.SequentialGroup stackedFields = layout.createSequentialGroup();
        layout.setVerticalGroup(vg);
        vg.
        	addComponent(imageLabel, GroupLayout.Alignment.CENTER, MIN_IMAGE_SIZE, MIN_IMAGE_SIZE, 2 * MIN_IMAGE_SIZE ).
        	// addComponent(imageLabel).
            addGroup(stackedFields.
            addComponent(rootLabel).
            addComponent(formulaLabel).
            addComponent(notesLabel).
            addComponent(locationsLabel).
            addComponent(variationLabel).
            addComponent(scoreLabel)).
            addComponent(percentileBar);

        // this.validate(); // causes a layout,sizing operation.
        // this.getPreferredSize(); // causes a layout,sizing operation.        
        setOpaque(true);
    }

    public EntryListCellRenderer( Display displayOpts, Fretboard fretboard ) {
    	this();
    	this.displayOpts = displayOpts;
    	this.fretboard = fretboard;    	
        this.getPreferredSize(); // causes a layout,sizing operation.
    }

    // This method is called by the Swing paint event dispatcher.
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        ExtendedDisplayEntry entry = (ExtendedDisplayEntry)value;
        if (null == entry) {
        	System.out.println( "EntryListCellRenderer null entry");
        	return this;
        }
        rootLabel.setText( (String) entry.getMember( "Root" ));
        formulaLabel.setText( (String) entry.getMember( "Formula" ));
        notesLabel.setText( (String) entry.getMember( "Notes" ));
        locationsLabel.setText( (String) entry.getMember( "Locations" ));
        variationLabel.setText( (String) entry.getMember( "Variation" ));
        scoreLabel.setText( (String) entry.getMember( "Score" ));
        
        if (( null != displayOpts) && (null != fretboard ) ){
       	   Dimension size = imageLabel.getSize();
       	   String formula = (String) entry.getMember( "Formula" );
       	   // System.out.println( "EntryListCellRenderer formula=\"" + entry.getMember( "Formula" ) + "\", imageLabel=" + size );
   		   // Don't waste time with imageLabel not sized or null entry.
       	   if ((size.width > 0) && (size.height > 0) && (null != formula)) {
       		   ImageIcon icon = cachedIcons.get(entry);
       		   if (null == icon) {
       			   icon = new ImageIcon( RasterRenderer.renderImage( size, displayOpts, fretboard, entry));
       			   cachedIcons.put(entry, icon);
       		   }
       		   imageLabel.setIcon( icon);
       	   }
        }
        	
        String scoreString = (String) entry.getMember( "Score" );
        // "Scores sum=22, fret bounds[0,15]=0, fret span=7, skip strings=5, same string=10"
        int [] scores = Controller.scanScore( scoreString );
        int sumScore = scores[ 0 ];
        // The percentile is inverted since min is the best score, max is the worst score.
        // System.out.println( "EntryListCellRenderer sum=" + sumScore + ", min=" + minScore + ", max=" + maxScore);
        if (( sumScore > maxScore ) || ( sumScore < minScore )) {
            // Error condition. Min and max might not have been set yet.
            percentileBar.setPercentile( 0.0f );
        } else {
            percentileBar.setPercentile( 1.0f * ( maxScore - sumScore ) / ( maxScore - minScore ) );
        }
        if (isSelected) {
            adjustColors(list.getSelectionBackground(),list.getSelectionForeground(), 
               this, formulaLabel, variationLabel);
        } else {
            adjustColors(list.getBackground(),list.getForeground(), 
               this, formulaLabel, variationLabel);
        }
        return this;
    }
    
    private void adjustColors(Color bg, Color fg, Component...components) {
        for (Component c : components) {
            c.setForeground(fg);
            c.setBackground(bg);
        }
    }

	public int getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(int maxScore) {
		this.maxScore = maxScore;
	}

	public int getMinScore() {
		return minScore;
	}

	public void setMinScore(int minScore) {
		this.minScore = minScore;
	}
}