package frets.swing.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import swingextensions.swingx.CutCopyPasteHelper;
import swingextensions.swingx.DropShadowBorder;
import swingextensions.swingx.DynamicAction;
import swingextensions.swingx.MnemonicHelper;
import swingextensions.swingx.app.Application;
import swingextensions.ui.AboutBox;
import swingextensions.ui.BarChartVisualizer;

import frets.main.ChordRank;
import frets.main.Display;
import frets.main.Fretboard;
import frets.main.Location;
import frets.main.LocationList;
import frets.main.Note;
import frets.main.NoteList;
import frets.main.Display.Orientation;
import frets.swing.model.ExtendedDisplayEntry;
import frets.swing.model.ExtendedDisplayEntryScoreComparator;

// TODO - Support fret spacing progression. Measure real guitar fretboard for width, progression.
// TODO - Factor out point to location, location to point from RasterRenderer.java
// TODO - Better layout. Resize to take advantage of large screens. Maintain fretboard aspect ratio.
// TODO - Variations are tied to root note. For example C3 chords cannot find C4 as variations. Fix.
// TODO - User interface to perform inversions
// TODO - Better short name algorithm. Shorten scale name such as "F3,R-b3-4-5-b7-R-b3-4-5-b7-b3-4,14138-15552" to "F3,R-b3-4-5-b7,14138-15552"
// TODO - Pentatonic box formulas. Shorten formula G2 R-b3-4-5-b7-R-b3-4-5-b7-R-b3	
// TODO - Match location list to root and formula. Populate common chord name in comments.
// TODO - Redo score to be a weighted composite. Also add string span as a metric.
// TODO - Consider score for enharmonics. For instance, although 1 entry, this is the best/worst of 6 enharmonics.
// TODO - Proper column sorting. Currently G2, G#2, G3 and variations sort funny.
// TODO - Tabbed pane bug. Touching DEtails > Comments > Display > Comments > Details moves Entry panel to Comments.
// TODO - EntryTable mouse clicks (e.g. variation up/down) work on different columns when columns rearranged.
/**
 * The Controller for the Frets application. Controller gets its name
 * from the model view controller (MVC) pattern. Controller is responsible
 * for creating the UI, listening for changes to both the model and view
 * and keeping everything in sync.
 */
public class Controller {
	public static final ExtendedDisplayEntry NULL_ENTRY = new ExtendedDisplayEntry();
	
	public static final int RANDOM_VARIATION = -1;
    public static final String ENTRY_NAME_DELIM = ",";
    
	public static final int NONE_SELECTED = -1;
	
    // A hack, but better than hard coding.
    public static final int ROOT_COL = 0;
    public static final int FORMULA_COL = 1;
    public static final int NOTES_COL = 2;
    public static final int LOCATIONS_COL = 3;
    public static final int VARIATIONS_COL = 4;
    public static final int SCORE_COL = 5;

    // A hack, but want to preserve score columns with no score available
    public static final int [] BARCHART_EMPTY_SCORE = new int [] { 0, 0, 0, 0, 0 };
    
    protected final static Random random = new Random();
    protected final static ResourceBundle resources = Application.getInstance().getResourceBundle();

    private Fretboard fretboard;
    private JComboBox<String> fretboardChooser;
    private JLabel fretboardDescription;
    private ChordRank ranker;
    private JTextField rankerTF;

    // The list of items displayed in the table.
    protected EntryTableModel entryTableModel;
    protected JTable entryTable;    
    
    // Image display fields
    private JLabel fretsDetailsPanel;
    private JLabel fretsLargePanel;    
    
    private BarChartVisualizer scoreBarChart;
    private int maxSumScore = Integer.MIN_VALUE;
    private int minSumScore = Integer.MAX_VALUE;

    private JTextPane commentsTP;

    private Display displayOpts = new Display();
    private Display largeDisplayOpts;
    private DisplayEditor displayEditor = new DisplayEditor( displayOpts );
    
    private static AboutBox aboutBox;
    
    public Controller(JFrame host) {
    	System.out.println( "Controller cons");

        // Some post init
        String defaultFretboard = resources.getString( "default.fretboard" );
		fretboard = Fretboard.getInstanceFromName( defaultFretboard );
    	displayOpts.orientation = Orientation.VERTICAL;
    	largeDisplayOpts = new Display( displayOpts );
    	largeDisplayOpts.orientation = Orientation.HORIZONTAL;
    	largeDisplayOpts.setDisplayAreaStyleMaxFretboard(fretboard);
    	largeDisplayOpts.showEnharmonicVariations = true;
    	largeDisplayOpts.showOctaveVariations = true;

    	createFrameComponents();
        createUI(host);
        createMenu(host);
        host.pack();
        addEntry( randomEntry( 10 )); // add a random entry        
    }

    /** Adds a blank entry */
    public void addBlank() {
        addEntry( new ExtendedDisplayEntry());        
    }

    /** Adds a random entry */
    public void addRandom() {
        addEntry( randomEntry( 10 ));        
    }

    /** Adds an entry to the entry table. */
    public void addEntry( ExtendedDisplayEntry entry ) {
       	System.out.println( "Controller.addEntry entry=" + entry );
        // Add the entry to the end of the list.
        entryTableModel.add(entry);
        // Set a new focus.
    	int selectedRow = entryTable.getSelectedRow();
    	if ( NONE_SELECTED == selectedRow )
           entryTable.setRowSelectionInterval( 0, 0 );
    	else
            entryTable.setRowSelectionInterval( selectedRow, selectedRow );
    }

    /** 
     * Returns a random entry that is below the given max score.
     * RETRY_MAX limits the number of tries.
     */
    public ExtendedDisplayEntry randomEntry( int scoreMax ) {
        final int RETRY_MAX = 100;
        // Create the new entry, adding some random values.
        ExtendedDisplayEntry entry = new ExtendedDisplayEntry();
    	int scoreSum = Integer.MAX_VALUE;
    	int retryCount = 0;
    	LocationList locations = null;
    	List<LocationList> variations = null;
    	long permutations = 0;
    	long variationi = -1;
        
    	// Get an entry that is below the max and limited by retry count.
       	while (( scoreSum > scoreMax ) && ( retryCount++ < RETRY_MAX)) {
       		// Pick a random root and formula.
        	Note randomRoot = Note.plus( Note.GuitarLowE, random.nextInt( 12 ) );
        	entry.setMember( "Root", randomRoot.toString() );

        	String randomFormula = "R-" + (random.nextInt( 4 ) + 2) + "-" + (random.nextInt( 4 ) + 5);
        	entry.setMember( "Formula", randomFormula );
        
        	NoteList notes = new NoteList();
        	notes.setRelative( randomRoot, randomFormula );
        	entry.setMember( "Notes", notes.toString() );

        	// Calculate other information fields.
            variations = fretboard.getEnharmonicVariations( notes );
            permutations = Fretboard.getPermutationCount( variations );
            variationi = nextLong( permutations );
            locations = Fretboard.getPermutation(variations, variationi);
            scoreSum = ranker.getSum( locations );
            // Essentially this makes the loop end after one try.
            if ( -1 == scoreMax ) scoreMax = Integer.MAX_VALUE;
        }
       	// System.out.println( "Controller.randomEntry retryCount=" + retryCount );

        entry.setMember( "Locations", locations.toString() );      
        entry.setMember( "Variation", Fretboard.getPermutationString(variations, variationi) );
        entry.setMember( "Score", ranker.getScoreString(locations) );
        // entry.setMember( "Comment", getCommentFromFormula( entry )); // make comment with nearest formula, variation
        
        return entry;	   
    }
     
    /** 
     * Returns the index of the first selected entry in the entry table.
     * Returns NONE_SELECTED if no entries are selected.
     * Convert table index to model index with entryTable.convertRowIndexToModel( selection );
     */
    public static int getFirstSelected(JTable entryTable) {
    	int [] selectedRows = entryTable.getSelectedRows();
    	if (( null != selectedRows ) && (selectedRows.length > 0)) {
    		// For now, get first one.
    		int firstSelection = selectedRows[ 0 ];
    		return firstSelection;
    	}
    	return NONE_SELECTED;
    }
    
    /** 
     * Returns the first selected entry in the entry table.
     * Returns null if no entries are selected.
     */
    public static ExtendedDisplayEntry getFirstSelected(JTable entryTable, EntryTableModel entryTableModel) {
    	int [] selectedRows = entryTable.getSelectedRows();
    	if (( null != selectedRows ) && (selectedRows.length > 0)) {
    		// For now, get first one.
    		int firstSelection = selectedRows[ 0 ];
     	   	ExtendedDisplayEntry entry = entryTableModel.get( firstSelection );
    		return entry;
    	}
    	return null;
    }
    
    /** Adds to the entry table N best score variations on first selected entry */
    public void varyN( int N ) {
      	ExtendedDisplayEntry entry = getFirstSelected( entryTable, entryTableModel );
      	if ( null != entry ) {
   	   	   // System.out.println( "   Selected row=" + entry );
   		   List<ExtendedDisplayEntry> variations = getVariations( entry, Integer.MAX_VALUE );

      		// Add the entry to the end of the list.
  	      	if (( null != variations ) && (variations.size() > 0 )) {
   			   // Check to not duplicate
   			   variations.remove( entry );
   			   Collections.sort( variations, new ExtendedDisplayEntryScoreComparator() );
   			   int i = 0;
   			   while(( i < N) && (i < variations.size())) {
   			      ExtendedDisplayEntry vary = variations.get( i );
   			      if ( entry.equals( vary )) {
   			         entryTableModel.add( variations.get( i ) );
   				     i++;
   			      }
   			   }
   	  	    }
            validateMaxScore();
   		} //null
    } // varyN

    /** Adds to the entry table ten best score variations on selected entry */
    public void varyTen() {
    	varyN( 10 );
    }

    /** Add to the entry table ALL variations on selected entry. */
    public void varyAll() {
    	varyN( Integer.MAX_VALUE );
    }

    /** Returns the first count variations of the given entry sorted by score. */
    public List<ExtendedDisplayEntry> getVariations( ExtendedDisplayEntry entry, int count ) {
    	if ( null == entry) return null;
        // Vary the provided entry.
    	List<ExtendedDisplayEntry> entryVariations = new ArrayList<ExtendedDisplayEntry>();
    	NoteList notes = new NoteList( (String) entry.getMember( "Notes" ) );
    	List<LocationList> variations = fretboard.getEnharmonicVariations( notes );
        long permutations = Fretboard.getPermutationCount( variations );
        String variationStr = (String) entry.getMember( "Variation" );
		long [] values = Fretboard.getPermutationValues(variationStr);
    	long variation = values[ 0 ];
        
        long maxPerm = Math.min( permutations, count );
        for ( int variationi = 0; variationi < maxPerm; variationi++ ) {
        	if ( variationi != variation ) {
        		ExtendedDisplayEntry newEntry = new ExtendedDisplayEntry();
            	LocationList locations = Fretboard.getPermutation(variations, variationi);
            	NoteList newNotes = new NoteList( locations.getNoteList( fretboard ));
            
	            Location lowest = locations.get( 0 );
	            Note root = lowest.getNote( fretboard );
	            
	        	newEntry.setMember( "Root", root.toString() );
	        	newEntry.setMember( "Formula", locations.getFormula(fretboard, root) );
	        	newEntry.setMember( "Notes", newNotes.toString() );
	
	            newEntry.setMember( "Locations", locations.toString() );      
	            newEntry.setMember( "Variation", Fretboard.getPermutationString(variations, variationi) );
	            newEntry.setMember( "Score", ranker.getScoreString(locations) );
	            // entry.setMember( "Comment", getCommentFromFormula( entry )); // make comment with nearest formula, variation
	            entryVariations.add( newEntry );
            }
        }
        
        return entryVariations;	   
    }
     
    /** Deletes the selected rows from the table and model. */
    public void deleteSelection() {
    	boolean needsUpdate = false;    	
    	int [] selectedRows = entryTable.getSelectedRows();
    	if (( null != selectedRows ) && ( selectedRows.length > 0)) {
	    	for ( int i = selectedRows.length - 1; i >= 0; i-- ) {
	    		int selectedRow = selectedRows[ i ];
	    		int modelRow = entryTable.convertRowIndexToModel( selectedRow );
	    		// System.out.println( "Controller.deleteSelection rows=" + entryTableModel.size() + ", tRow=" + selectedRow + ", mRow=" + modelRow);
	    		entryTableModel.remove( modelRow );
	    		needsUpdate = true;
	    	}
    	}
    	if ( needsUpdate ) {
    		validateMaxScore();
    		if ( entryTable.getRowCount() >  0 )
    			entryTable.setRowSelectionInterval( 0, 0 );
    	}
    }
    
    /** Deletes the selected rows from the table and model. */
    public void deleteAll() {
		entryTableModel.clear();
        disableControls();       
    }

    /** Get the selected entries and export to files. */
	public void exportImages( boolean details ) {
		int imageCount = 0;
		int [] selectedRows = entryTable.getSelectedRows();
		if ((null != selectedRows) && (selectedRows.length > 0)) {
			for (int i = 0; i < selectedRows.length; i++) {
				int selectedRow = selectedRows[i];
				int modelRow = entryTable.convertRowIndexToModel(selectedRow);
				ExtendedDisplayEntry entry = entryTableModel.get(modelRow);
   			    String entryDetails = getShortName( entry ); 
   			    BufferedImage image = details ? getDetailsImage(entry) : getLargeImage(entry);
   			    try { 
   			    	@SuppressWarnings("unused")
					String fileName = writeImage( image, entryDetails );
   			    } catch( IOException e ) {
        			System.out.println( "Controller.exportImage exception=" + e );
        			return;
   			    }
				imageCount++;
			}
			System.out.println("Controller.exportImage wrote " + imageCount + " images to \"" + resources.getString("export.path") + "\"." );

		}
	}

	public void exportDetail( ) {
		exportImages( true );
	}

    public void exportFretboard() {
		exportImages( false );
     }
    
    /** Writes the image to a file name of "<export.path>/frets<entryDetails>,<fileCount>.png" */
	public static String writeImage(BufferedImage image, String entryDetails) 
		throws IOException {
		// BufferedImage image = SafeIcon.provideImage(icon);
		String path = resources.getString("export.path");
		File pathDir = new File(path);
		if (!pathDir.exists()) {
			pathDir.mkdirs();
		}
		String sep = System.getProperty("file.separator");
		String fileName = path + sep + "frets.png";
		// System.out.println( "Controller.writeImage entryDetails=" + entryDetails);
		if ((null != entryDetails) && (entryDetails.length() > 0))
			fileName = path + sep + "frets," + entryDetails + ".png";
		else
			fileName = path + sep + "frets.png";
		File file = new File( fileName );
		int fileCount = 0;
		while (( file.exists() ) && ( fileCount <= 100 )) {
			if ( fileCount >= 100 ) {
				System.out.println( "Controller.writeImage could not write. Too many file versions in \"" + path + "\" path.");
				return null;
			}
			if ((null != entryDetails) && (entryDetails.length() > 0))
				fileName = path + sep + "frets," + entryDetails + "," + ++fileCount + ".png";
			else
				fileName = path + sep + "frets" + "," + ++fileCount + ".png";
			file = new File( fileName );
		}
		
		ImageIO.write(image, "png", file);
		return fileName;
	}
    
    public void showAbout() {
    	if ( null == aboutBox ) {
            aboutBox = new AboutBox(
            	resources.getString( "aboutBox.dialog.title" ),
                resources.getString( "aboutBox.info.title" ),
                resources.getString( "aboutBox.info.subtitle1" ),
                resources.getString( "aboutBox.info.subtitle2" ),
                resources.getString( "aboutBox.info.subtitle3" ),
                resources.getString( "aboutBox.icon.location" ),
                resources.getString( "aboutBox.background.location" )
          	);    		
    	}
       aboutBox.show(SwingUtilities.getWindowAncestor(fretboardChooser));
    }

    // Disables the controls (e.g. after deleteAll or if an invalid entry has been selected) 
    public void disableControls() {
        // fretboardChooser.setEditable(false);
        // fretboardDescription.setText( "" );
        rankerTF.setEditable(false);
        
        fretsDetailsPanel.setIcon( null );
        fretsDetailsPanel.setToolTipText( null );
        fretsLargePanel.setIcon( null );
        fretsLargePanel.setToolTipText( null );
        
  	    scoreBarChart.setMaxValue( 0 );
        scoreBarChart.setColumns( BARCHART_EMPTY_SCORE );
        scoreBarChart.setAnimatesTransitions( true );

        commentsTP.setText("");
        commentsTP.setEditable(false);

        displayEditor.setEditable(false);
    }
    
    // Returns true if the app can exit, false otherwise.
    protected boolean canExit() {
    	// Do exit cleanup
        return true;
    }

    /** Creates all the components in the main frame. */
    protected void createFrameComponents() {    	
        fretboardChooser = new JComboBox<String>( Fretboard.getFretboardNames() );
        fretboardChooser.setSelectedItem( fretboard.getMetaName());
        fretboardDescription = new JLabel( fretboard.getMetaDescription() );
        fretboardChooser.addActionListener( new FretboardChanger() );
        rankerTF = new JTextField(15);
                
        fretsDetailsPanel = new JLabel();
        // detailsImagePanel.setBorder( new CompoundBorder(new LineBorder(Color.DARK_GRAY, 1),  new EmptyBorder(2, 2, 2, 2)));
        fretsDetailsPanel.setBorder( new LineBorder(Color.DARK_GRAY, 1) );
        fretsDetailsPanel.setPreferredSize(new Dimension(150, 150));
        // Provide an opaque background
        Color bg = displayOpts.backgroundColor;
        Color opaquebg = new Color( bg.getRed(), bg.getGreen(), bg.getBlue(), 0xff );
        fretsDetailsPanel.setBackground( opaquebg );
        fretsDetailsPanel.setOpaque(true);
        fretsDetailsPanel.setToolTipText( null );
        fretsDetailsPanel.addMouseListener(new PopupMenuListener( ));
        fretsDetailsPanel.addMouseListener(new LocationClickListener( ));
        
        scoreBarChart = new BarChartVisualizer();
  	    scoreBarChart.setMaxValue( 0 );
        scoreBarChart.setColumns( BARCHART_EMPTY_SCORE );
        scoreBarChart.setOpaque(false);
        // Makes it align with image
        scoreBarChart.setBorder(new EmptyBorder(5, 5, 5, 5));

        commentsTP = new JTextPane();

        fretsLargePanel = new JLabel();
        // largeImagePanel.setBorder( new CompoundBorder(new LineBorder(Color.DARK_GRAY, 1),  new EmptyBorder(2, 2, 2, 2)));
        fretsLargePanel.setBorder( new LineBorder(Color.DARK_GRAY, 1) );
        fretsLargePanel.setPreferredSize(new Dimension(800, 150));
        fretsLargePanel.setBackground( opaquebg );
        fretsLargePanel.setOpaque(true);
        fretsLargePanel.setToolTipText( null );
        fretsLargePanel.addMouseListener(new PopupMenuListener());
        fretsLargePanel.addMouseListener(new LocationClickListener());
    }
    
    /** Creates all the UI components in the given frame. */
    protected void createUI(JFrame frame) {
        JTabbedPane tabbedPane = new JTabbedPane();
        // Panels greater than 0 are lazy instantiated by the TabbedPaneChangeHandler ChangeListener.        
        tabbedPane.addTab(Application.getResourceAsString("tab.details"), createDetailsPanel());
        tabbedPane.addTab(Application.getResourceAsString("tab.comments"), null);
        tabbedPane.addTab(Application.getResourceAsString("tab.display"), null);
        tabbedPane.addChangeListener(new TabbedPaneChangeHandler(tabbedPane));

        JPanel fixedPanel = new JPanel( new BorderLayout() );
        JPanel fretPanel = new JPanel( new BorderLayout());
        fretPanel.add( "West", new JLabel(resources.getString("label.fretboard")));
        fretPanel.add( "Center", fretboardChooser );
        fretboardDescription.setText( fretboard.getMetaDescription() );
        fretPanel.add( "East", fretboardDescription );
        fixedPanel.add( "West", fretPanel );
        
        JPanel rankerPanel = new JPanel( new BorderLayout());
        rankerPanel.add( "West", new JLabel(resources.getString("label.ranker")));
        String defaultRanker = resources.getString("default.ranker");
        // System.out.println ( "Formula ranker=\"" + defaultRanker + "\".");
		ranker = ChordRank.instance.getInstance( defaultRanker );
		rankerTF.setText( ranker.getMetaName() );
		rankerTF.setEditable( false );
        rankerPanel.add( "Center", rankerTF );
        fixedPanel.add( "East", rankerPanel );

        // Create table in a view port.
        entryTableModel = new EntryTableModel();
        entryTableModel.addTableModelListener( new EntryTableModelListener() );
		createEntryTable( entryTableModel ) ;
		// Add variation column listener
		entryTable.addMouseListener( new VariationMouseAdapter() );
        JScrollPane entrySP = new JScrollPane(entryTable);
        entrySP.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        entrySP.setViewportView( entryTable );
        entrySP.setPreferredSize(new Dimension( 600, 200 ));
        
        JPanel topHalf = new JPanel();
        topHalf.setLayout( new BorderLayout() );
        topHalf.add( "North", fixedPanel );
        topHalf.add( "Center", entrySP );
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topHalf, tabbedPane);
        splitPane.setResizeWeight( 1.0 );
        frame.getContentPane().add( splitPane );
    }
    
    /** Creates and populates the main menu bar for the app. */
    protected void createMenu(JFrame frame) {
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        if (!Application.isOSX()) {
            JMenu fileMenu = MnemonicHelper.createMenu(resources.getString("menu.file"));
            menuBar.add(fileMenu);
            JMenuItem exitMI = MnemonicHelper.createMenuItem(fileMenu,resources.getString("menu.exit"));
            exitMI.addActionListener(new DynamicAction(Application.getInstance(), "exit"));
        }
        
        // JMenu editMenu = MnemonicHelper.createMenu( resources.getString("menu.edit"));
        // menuBar.add(editMenu);
        // JMenuItem cutMI = createMenuItem(editMenu, "menu.cut", CutCopyPasteHelper.getCutAction());
        // cutMI.setAccelerator(KeyStroke.getKeyStroke("ctrl X"));
        // editMenu.add(cutMI);
        // JMenuItem copyMI = createMenuItem(editMenu, "menu.copy", CutCopyPasteHelper.getCopyAction());
        // copyMI.setAccelerator(KeyStroke.getKeyStroke("ctrl C"));
        // editMenu.add(copyMI);
        // JMenuItem pasteMI = createMenuItem(editMenu, "menu.paste",CutCopyPasteHelper.getPasteAction());
        // pasteMI.setAccelerator(KeyStroke.getKeyStroke("ctrl V"));
        // editMenu.add(pasteMI);
                
        JMenu entryMenu = MnemonicHelper.createMenu( resources.getString("menu.entry"));
        menuBar.add(entryMenu);
        JMenuItem addBlankItem = MnemonicHelper.createMenuItem(entryMenu, resources.getString("menu.newBlank"));
        addBlankItem.addActionListener(new DynamicAction(this, "addBlank"));
        addBlankItem.setAccelerator(KeyStroke.getKeyStroke("ctrl B"));
        JMenuItem addRandomItem = MnemonicHelper.createMenuItem(entryMenu, resources.getString("menu.newRandom"));
        addRandomItem.addActionListener(new DynamicAction(this, "addRandom"));
        addRandomItem.setAccelerator(KeyStroke.getKeyStroke("ctrl R"));
        JMenuItem deleteMenuItem = MnemonicHelper.createMenuItem(entryMenu, resources.getString("menu.deleteSelected"));
        deleteMenuItem.addActionListener(new DynamicAction(this, "deleteSelection"));
        deleteMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl D"));
        JMenuItem deleteAllMenuItem = MnemonicHelper.createMenuItem(entryMenu, resources.getString("menu.deleteAll"));
        deleteAllMenuItem.addActionListener(new DynamicAction(this, "deleteAll"));
        deleteAllMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl shift D"));
        entryMenu.addSeparator();
        JMenuItem varyTenMenuItem = MnemonicHelper.createMenuItem(entryMenu, resources.getString("menu.varyTen"));
        varyTenMenuItem.addActionListener(new DynamicAction(this, "varyTen"));
        varyTenMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl T"));
        JMenuItem varyAllMenuItem = MnemonicHelper.createMenuItem(entryMenu, resources.getString("menu.varyAll"));
        varyAllMenuItem.addActionListener(new DynamicAction(this, "varyAll"));
        varyAllMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl A"));
        entryMenu.addSeparator();
        JMenuItem exportDetailMenuItem = MnemonicHelper.createMenuItem(entryMenu, resources.getString("menu.exportDetail"));
        exportDetailMenuItem.addActionListener(new DynamicAction(this, "exportDetail"));
        exportDetailMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl E"));
        JMenuItem exportFretboardMenuItem = MnemonicHelper.createMenuItem(entryMenu, resources.getString("menu.exportFretboard"));
        exportFretboardMenuItem.addActionListener(new DynamicAction(this, "exportFretboard"));
        exportFretboardMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl shift E"));

        JMenu helpMenu = MnemonicHelper.createMenu(resources.getString("menu.help"));
        menuBar.add(helpMenu);
        createMenuItem(helpMenu, "menu.about", new DynamicAction(this, "showAbout"));        
    }

    
    /** Convenience to create a configure a JMenuItem. */
    protected JMenuItem createMenuItem(JMenu menu, String key, Action action) {
        JMenuItem mi;
        if (action != null) {
            mi = new JMenuItem(action);
        } else {
            mi = new JMenuItem();
        }
        MnemonicHelper.configureTextAndMnemonic(mi, Application.getResourceAsString(key));
        menu.add(mi);
        return mi;
    }
    
    /** Creates the details panel, tiny panel with five fret port. */
    protected Component createDetailsPanel() {
        JPanel detailsWrapper = new JPanel(new BorderLayout());
        detailsWrapper.setOpaque(false);
        detailsWrapper.setBorder(new DropShadowBorder(Color.BLACK, 0, 5, .5f, 12, false, true, true, true));
        detailsWrapper.add(fretsDetailsPanel);
        detailsWrapper.add( "South", scoreBarChart);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(fretsLargePanel);
        panel.add( "West", detailsWrapper);
        return panel;
    }
    
    /** Return a big panel that can handle comments and notes editing. */
    protected Component createCommentsPanel() {
		JPanel panel = new JPanel();
        panel.setOpaque(false);
        GroupLayout layout = new GroupLayout(panel);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        panel.setLayout(layout);
        JScrollPane notesSP = new JScrollPane(commentsTP);
        notesSP.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        GroupLayout.ParallelGroup hg = layout.createParallelGroup();
        layout.setHorizontalGroup(hg);
        hg.addComponent(notesSP, 1, 1, Integer.MAX_VALUE);
        
        GroupLayout.ParallelGroup vg = layout.createParallelGroup();
        layout.setVerticalGroup(vg);
        vg.addComponent(notesSP, 1, 1, Integer.MAX_VALUE);
        return panel;
    }
    
    /** Create a big panel that can edit display options. */
    private Component createDisplayEditorPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        GroupLayout layout = new GroupLayout(panel);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        panel.setLayout(layout);
        JScrollPane scrollPane = new JScrollPane( displayEditor );
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setViewportView(  entryTable );       

        GroupLayout.ParallelGroup hg = layout.createParallelGroup();
        layout.setHorizontalGroup(hg);
        hg.addComponent(scrollPane, 1, 1, Integer.MAX_VALUE );
        
        GroupLayout.ParallelGroup vg = layout.createParallelGroup();
        layout.setVerticalGroup(vg);
        vg.addComponent(scrollPane, 1, 1, Integer.MAX_VALUE );

        return panel;
    }
    
    /** Creates main table to display entry model. */
    @SuppressWarnings("serial")
	protected  void createEntryTable( EntryTableModel entryTableModel) {
        entryTable = new JTable( entryTableModel ){
            // Implement table cell tool tips.           
            public String getToolTipText(MouseEvent e) {
                Point p = e.getPoint();
                int colIndex = columnAtPoint(p);
                switch ( colIndex ) {
                   case ROOT_COL: return "Click to edit root note";
                   case FORMULA_COL: return "Dbl click to edit formula";
                   case VARIATIONS_COL: return "Left/right click to change. Left dbl click for random. Right dbl click for best score.";
                } // colIndex
                return null;
            }
        };
        entryTable.setFillsViewportHeight(true);
        entryTable.setAutoCreateRowSorter(true);
        entryTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        entryTable.getSelectionModel().addListSelectionListener(new EntryTableSelectionListener());
        
        TableColumnModel tcm = entryTable.getColumnModel();
        TableColumn rootColumn = tcm.getColumn(0);
        rootColumn.setCellEditor(new NoteTableEditor());

        // Set column preferred widths
        // entryTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setColWidth( entryTable, ROOT_COL, null, true );
        setColWidth( entryTable, FORMULA_COL, null, true );
        setColWidth( entryTable, VARIATIONS_COL, "16/24 (0123/2345)", true );
        setColWidth( entryTable, SCORE_COL, "Scores sum=22, fret bounds[0,15]=0, fret span=3", false );
        
        CutCopyPasteHelper.registerCutCopyPasteBindings(entryTable);
        CutCopyPasteHelper.setPasteEnabled(entryTable, true);
    }
    
    /** Sets the table column width based on column header or provided text. */
    public static void setColWidth( JTable table, int col, String text, boolean preferredElseMin ) {
    	if ( null == table ) return;
        JTableHeader tableHeader = table.getTableHeader();        
        if( null == tableHeader ) return;
        FontMetrics headerFontMetrics = tableHeader.getFontMetrics(tableHeader.getFont());

        
        int headerWidth = headerFontMetrics.stringWidth( table.getColumnName(col) );
        if ((null != text) && (text.length() > 0))
            headerWidth = headerFontMetrics.stringWidth( text );
        if ( preferredElseMin )
        	table.getColumnModel().getColumn( col ).setPreferredWidth( headerWidth );
        else
        	table.getColumnModel().getColumn( col ).setMinWidth( headerWidth );
    }    
    
    public BufferedImage getDetailsImage(ExtendedDisplayEntry entry) {
    	String locationString = (String) entry.getMember("Locations");
  	    // System.out.println( "Controller locations=\"" + locationString + "\", detailsImagePanel=" + fretsDetailsPanel.getSize());
    	// Locations may be null or empty list.
        if (( null != locationString ) && (locationString.length() > 0)) {
        	LocationList locations = LocationList.parseString( locationString );
        	displayOpts.setDisplayAreaStyleMinAperture( fretboard, locations, 5 ); // set window to 5 frets.
        } else {
        	// Null image
        	displayOpts.setDisplayAreaStyleMinAperture( fretboard, null, 5 ); // set window to 5 frets.
        }
    	BufferedImage image = RasterRenderer.renderImage( fretsDetailsPanel.getSize(), displayOpts, fretboard, entry );
        return image;
    }

    public BufferedImage getLargeImage(ExtendedDisplayEntry entry) {
    	// System.out.println( "Controller requesting large image size=" + largeImagePanel.getSize());
    	// Set large display to entire fretboard.
    	BufferedImage image = RasterRenderer.renderImage( fretsLargePanel.getSize(), largeDisplayOpts, fretboard, entry );        	
        return image;
    }

    /** Listens for changes in selection. Updates visuals. */
    public class EntryTableSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent event) {
            if (event.getValueIsAdjusting()) {
                return;
            }
          	ExtendedDisplayEntry entry = getFirstSelected( entryTable, entryTableModel );
          	if( null != entry )
      		  	updateVisuals( entry );
          	else
          		disableControls();            
        }
    }
        
	public void outputSelection( StringBuffer output) {
        output.append(String.format("Lead: %d, %d. ",
            entryTable.getSelectionModel().getLeadSelectionIndex(),
            entryTable.getColumnModel().getSelectionModel().getLeadSelectionIndex()));
        output.append("Rows:");
        for (int c : entryTable.getSelectedRows()) {
            output.append(String.format(" %d", c));
        }
        output.append(". Columns:");
        for (int c : entryTable.getSelectedColumns()) {
            output.append(String.format(" %d", c));
        }
        output.append(".");
    }    
	
    /** Listens for changes in data model. Updates current entry. Updates visuals. */
    public class EntryTableModelListener implements TableModelListener {
        public void tableChanged(TableModelEvent event) {
        	if ( entryTableModel.size() == 0 ) {
        		disableControls();
        		return;
        	}
        	int type = event.getType();
        	int col = event.getColumn();
        	// String typeStr = "unknown";
        	// switch ( type ) {
        	// 	case TableModelEvent.INSERT: typeStr = "insert"; break;
        	// 	case TableModelEvent.DELETE: typeStr = "delete"; break;
        	// 	case TableModelEvent.UPDATE: typeStr = "update"; break;
        	// }
        	// System.out.println( "Controller EntryTableModelListener type=" + typeStr + ", row=" + event.getFirstRow() + ".." + 
        	//    event.getLastRow() + ", col=" + col + ", event=" + event );
    		if ((type == TableModelEvent.UPDATE) && ( col >= 0) && ( col <= 1)) {
        		ExtendedDisplayEntry entry = entryTableModel.get( event.getFirstRow() );
    			updateEntryFromFormula( entry );
        		updateVisuals( entry );
    		}
    		validateMaxScore();
        }
    }
    
    /** Updates an entry from the root and the formula. */
    protected void updateEntryFromFormula( ExtendedDisplayEntry entry ){
    	String root = (String) entry.getMember( "Root" );
    	String formula = (String) entry.getMember( "Formula" );
	    
    	if ((null != root) && (root.length() > 0)) {
        	if ((null != formula) && (formula.length() > 0)) {
	    		NoteList notes = new NoteList();
	    		notes.setRelative( new Note( root ), formula );
	    		entry.setMember( "Notes", notes.toString() );
	    		
	    	    // Calculate other information fields.
	            List<LocationList> variations = fretboard.getEnharmonicVariations( notes );
	    	    long permutations = Fretboard.getPermutationCount( variations );
	    	    if ( permutations > 0) { // Can happen when bass notes have no locations on soprano ukelele
	    	    	long variationi = nextLong( permutations );
	    	    	LocationList locations = Fretboard.getPermutation(variations, variationi);
	    	    	// scoreSum = ranker.getSum( locations );
	    	    	entry.setMember( "Locations", locations.toString() );      
	    	    	entry.setMember( "Variation", Fretboard.getPermutationString(variations, variationi) );
	    	    	entry.setMember( "Score", ranker.getScoreString(locations) );    
	    	    	// 	entry.setMember( "Comment", getCommentFromFormula( entry )); // make comment with nearest formula, variation
	    	    	entry.setMember( "Locations", locations.toString() );      
	    	    } else {
	    	    	entry.setMember( "Locations", null );      
	    	    	entry.setMember( "Variation", null );
	    	    	entry.setMember( "Score", null );    
	    	    }
	    	    return;
    	    }

    	} 
    	entry.setMember( "Locations", null );      
    	entry.setMember( "Variation", null );
    	entry.setMember( "Score", null );    
   		entry.setMember( "Notes", null );    		
    }

    /** Updates the main display from an updated entry. Works with null or blank entries. */
    protected void updateVisuals( ExtendedDisplayEntry entry ) {
    	String entryText = getShortName( entry );
        // System.out.println( "Controller.updateVisuals entry=" + entry );
    	
    	fretsDetailsPanel.setIcon( new ImageIcon( getDetailsImage( entry ) ));
        if (( null != entryText ) && ( entryText.length() > 0) )
        	fretsDetailsPanel.setToolTipText( entryText );
        else
        	fretsDetailsPanel.setToolTipText( null );
        fretsLargePanel.setIcon( new ImageIcon( getLargeImage( entry )) );
        if (( null != entryText ) && ( entryText.length() > 0) )
        	fretsLargePanel.setToolTipText( entryText );
        else
        	fretsLargePanel.setToolTipText( null );
        
        String scoreString = (String) entry.getMember( "Score" );
        // "Scores sum=22, fret bounds[0,15]=0, fret span=7, skip strings=5, same string=10"
        int [] scores = ChordRank.toScores( scoreString );
        if (( null != scores ) && (scores.length > 1))
        	scoreBarChart.setColumns( scores );
        else
            scoreBarChart.setColumns( BARCHART_EMPTY_SCORE );        	
        scoreBarChart.repaint();    	
    }
    
    // Ensure the new list items update the max score.
    protected void validateMaxScore() {
        maxSumScore = 0;
        minSumScore = 0;
        for ( ExtendedDisplayEntry entry : entryTableModel ) {
     	    String scoreString = (String) entry.getMember( "Score" );
     	    int [] scores = ChordRank.toScores(scoreString);
     	    int sumScore = scores[ 0 ];
            if ( sumScore > maxSumScore ) { 
         	   maxSumScore = sumScore;
            }    		
            if ( sumScore < minSumScore ) { 
          	   minSumScore = sumScore;
             }    		
    	}
  	    scoreBarChart.setMaxValue( maxSumScore );
        scoreBarChart.repaint();    	
    }

    /** Support lazy instantiation of panels greater than 0. */
    public final class TabbedPaneChangeHandler implements ChangeListener {
        private final JTabbedPane tp;
        
        public TabbedPaneChangeHandler(JTabbedPane tp) {
            this.tp = tp;
        }
        
        public void stateChanged(ChangeEvent e) {
            if (tp.getSelectedIndex() == 1 && tp.getComponentAt(1) == null) {
                tp.setComponentAt(1, createCommentsPanel());
            }
            if (tp.getSelectedIndex() == 2 && tp.getComponentAt(2) == null) {
                tp.setComponentAt(1, createDisplayEditorPanel());
            }
            // int selectedIndex = tp.getSelectedIndex();
        }
    }
    
    /** 
     * Returns a short string for the entry or null.
     * Keeps the name file system safe by not using special chars "/" or " ".
     */
    public final static String getShortName( ExtendedDisplayEntry entry ) {
    	if ( null == entry )
    		return null;
    	StringBuilder sb = new StringBuilder( );
    	String root = (String) entry.getMember("Root") ;
    	if ( null != root ) 
    		sb.append( root );
    	if ( sb.length() > 0 )
    		sb.append( ENTRY_NAME_DELIM );
    	String formula = (String) entry.getMember("Formula");
    	if ( null != formula )
    		sb.append( entry.getMember("Formula"));
    	if ( sb.length() > 0 )
    		sb.append( ENTRY_NAME_DELIM );
		// Example variation string "6/8 (012/124)"
	    String variation = 	(String) entry.getMember("Variation");
	    if ( null != variation ) {
		    long [] values = Fretboard.getPermutationValues(variation);
		    if ( null != values ) {
		    	sb.append( values[ 0 ] + "-" + values[ 1 ] );		    	
		    }
	    }        	    
		// Example string "G2,R-3-5,6-8", or if entry is empty ""
   	    return sb.toString();
    }
 
    /** Listens to entryTable. Handles single and double clicks on the variations column. */
    public class VariationMouseAdapter extends MouseAdapter {
	    @Override
	    public void mouseClicked(MouseEvent evt) {
	        int row = entryTable.rowAtPoint(evt.getPoint());
	        int col = entryTable.columnAtPoint(evt.getPoint());
	        if ((row >= 0 ) && (col == VARIATIONS_COL)) {
	        	// System.out.println( "TableMouseListener.mouseClicked button=" + evt.getButton() + ", count=" + evt.getClickCount() + ", row/col=" + row + "/" + col );
	        	int modelRow = entryTable.convertRowIndexToModel( row );
	        	if (( modelRow >= 0 ) && ( modelRow <= entryTableModel.size())) {
	        		ExtendedDisplayEntry entry = (ExtendedDisplayEntry) entryTableModel.getRowAt( modelRow );
	        		switch (evt.getButton()) {
		        		case MouseEvent.BUTTON1: case MouseEvent.BUTTON2: case MouseEvent.BUTTON3: {
		        		String notesString = (String) entry.getMember( "Notes");
		        		if (( null != notesString ) && ( notesString.length() > 0 )) {
	        			NoteList notes = NoteList.parse( notesString );
		        		String variationStr = (String) entry.getMember( VARIATIONS_COL ); // variation
		        		if ( null != variationStr ) {
		        			long [] values = Fretboard.getPermutationValues(variationStr);
		        			long updateVariation = values[ 0 ];
		        			long maxVar = values[ 1 ];
		        			if ( evt.getClickCount() == 1 ) {
			        			if ( evt.getButton() == MouseEvent.BUTTON1 ) {
			        				// Decrement
			        				updateVariation -= 1;
			        				if ( updateVariation < 0 )
			        					updateVariation += maxVar;
			        			} else {
			        				// Increment
			        				updateVariation += 1;
			        				updateVariation %= maxVar;
			        			}
		        			} else if ( evt.getClickCount() == 2 ) {
		        				// It is strange that Java double click calls single click first.
		        				// Hence you will see the single click logic execute before dbl click is called.
			        			if ( evt.getButton() == MouseEvent.BUTTON1 ) {
			        				// random
                                    updateVariation = nextLong( maxVar );			        				
			        			} else {
				        	    	List<LocationList> variations = fretboard.getEnharmonicVariations( notes );
				        	    	int bestScore = Integer.MAX_VALUE;
			        				// best score
			        				for ( int i = 0; i < maxVar; i++ ) {
					                    LocationList locations = Fretboard.getPermutation(variations, i );
			        					int [] score = ranker.compositeScore( locations );
			        					if ((null != score) && (score[ 0 ] < bestScore)) {
			        						bestScore = score[ 0 ];
			        						updateVariation = i;
			        					}
			        				}
			        			}		        				
		        			}
		        			
		                	// Calculate other information fields.
		        	    	List<LocationList> variations = fretboard.getEnharmonicVariations( notes );
		                    LocationList locations = Fretboard.getPermutation(variations, updateVariation );
		                    
		                    entry.setMember( "Locations", locations.toString() );      
		                    entry.setMember( "Variation", Fretboard.getPermutationString(variations, updateVariation) );
		                    entry.setMember( "Score", ranker.getScoreString(locations) );
		                    // System.out.println( "TableMouseListener.mouseClicked updating vari=" + values[ 0 ] + "/" + currentVar + ", entry=" + entry ); 
		                    // entry.setMember( "Comment", getCommentFromFormula( entry )); // make comment with nearest formula, variation
		                    entryTableModel.set(modelRow, entry);
		                    updateVisuals( entry );
		                    evt.consume();
		        		} // non-null variations string
		        		} // non-null notes String
		        		} // case mouse button 1 2 3
	        		} // switch
	        	} // valid row col	        	
	        } // VARIATIONS_COL
	    } // mouseClicked
    } // class
    
    /** Listens to image icons. Will report on nearest location click. */
    protected class LocationClickListener extends MouseAdapter {
	    @Override
	    public void mouseClicked(MouseEvent evt) {
    		switch (evt.getButton()) {
    		case MouseEvent.BUTTON1: {
    			if (evt.getClickCount() == 1) {
    				Component component = evt.getComponent();
    				if ( JLabel.class.isAssignableFrom(component.getClass() )) {
   					    boolean detailsPanel = component == fretsDetailsPanel;
   					    boolean largePanel = component == fretsLargePanel;
        				// Point screenLoc = evt.getLocationOnScreen();
        				int x = evt.getX(); int y = evt.getY();
        				// System.out.println( "LocationClickListener loc=(" + x + "," + y + "), details?=" + detailsPanel + ", large?=" + largePanel + ", component=" + (JLabel) component );

        				Location nearest = null;
        				if ( detailsPanel ) {
        					nearest = RasterRenderer.getNearestLocation( new Point( x, y ), fretsDetailsPanel.getSize(), displayOpts, fretboard );
        				} else if ( largePanel ) {
        					nearest = RasterRenderer.getNearestLocation( new Point( x, y ), fretsLargePanel.getSize(), largeDisplayOpts, fretboard );
        				}
        				// System.out.println(  "LocationClickListener nearest=" + nearest );
        				if ( null != nearest ) {
        					updateEntryFromLocation( nearest );        					
        				}
    				} // JLabel
    			} // click count 1
    		} // case mouse button 1 2 3
    		} // switch
	    } // mouseClicked
    } // class
 
    public void updateEntryFromLocation( Location location ) {
        int selection = getFirstSelected( entryTable );
      
        int modelIndex = -1;
        ExtendedDisplayEntry entry = null;
        if ( NONE_SELECTED == selection ) {
        	entry = new ExtendedDisplayEntry();
        } else {
            modelIndex = entryTable.convertRowIndexToModel( selection );
     	   	entry = entryTableModel.get( selection );
       }
        String locationString = (String) entry.getMember( "Locations" );
        LocationList locations = LocationList.parseString( locationString );
        if ( locations.contains( location )) 
        	locations.remove( location );
        else {
        	locations.add( location );
        	locations.sort();
        }
        entry.setMember( "Locations", locations.toString() );

        if ( locations.size() > 0 ) {
	    	NoteList notes = new NoteList( locations.getNoteList( fretboard ));
	    	entry.setMember( "Notes", notes.toString() );
	
	    	Location lowest = locations.get( 0 );
	        Note root = lowest.getNote( fretboard );
	    	entry.setMember( "Root", root.toString() );
	    	entry.setMember( "Formula", locations.getFormula(fretboard, root) );
	  
	        // Find variationi
	    	// long startTime = System.currentTimeMillis();
	        List<LocationList> variations = fretboard.getEnharmonicVariations( notes );
		    // long permutations = Fretboard.getPermutationCount( variations );
	        // System.out.println( "Controller.updateEntryFromLocation variations=" + variations.size() + ", permutations=" + permutations);
	        long variationi = Fretboard.getPermutationNumber( variations, locations );
        	// System.out.println( "   Controller.updateEntryFromLocation variationi=" + variationi);
	        if ( -1 != variationi ) {
		        entry.setMember( "Variation", Fretboard.getPermutationString(variations, variationi) );
		    }
	        // System.out.println( "   Controller.updateEntryFromLocation time=" + (System.currentTimeMillis() - startTime) + "mS");
		    
	        entry.setMember( "Score", ranker.getScoreString(locations) );
	        // entry.setMember( "Comments", getCommentFromFormula( entry )); // make comment with nearest formula, variation
	        
	        if ( -1 == modelIndex )
	        	entryTableModel.add( 0, entry );
	        else
	        	entryTableModel.set( modelIndex, entry );
        } else {
	    	entry.setMember( "Notes", "" );
	    	entry.setMember( "Root", "" );
	    	entry.setMember( "Formula", "" );
            entry.setMember( "Variation", "" );
	        entry.setMember( "Score", "" );
            entry.setMember( "Comments", "" );
        	entryTableModel.set( modelIndex, entry );
        } // location list size
        
		updateVisuals( entry );
		validateMaxScore();
    }
    
    /** Handles the notification that a new fretboard has been selected. */
    public class FretboardChanger implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            @SuppressWarnings("rawtypes")
			JComboBox cb = (JComboBox) e.getSource();
            String fretboardName = (String) cb.getSelectedItem();
            
            System.out.println( "FretboardChanger.actionPerformed selected=" + fretboardName ); 
    		fretboard = Fretboard.getInstanceFromName( fretboardName );
            fretboardDescription.setText( fretboard.getMetaDescription() );
            // Update large display based on String count.
        	largeDisplayOpts.setDisplayAreaStyleMaxFretboard(fretboard);

    		// Need to update all entries from root and formula 
        	int modelCount = entryTableModel.getRowCount();
       		for ( int i = 0; i < modelCount; i++ ) {
       	   		ExtendedDisplayEntry entry = entryTableModel.get( i );         	   		
         	    updateEntryFromFormula( entry );
         	    entryTableModel.set( i, entry);
        	}
       		if ( modelCount > 0 ) {
       			ExtendedDisplayEntry entry = getFirstSelected(entryTable, entryTableModel);
       		    if ( null != entry )
       		    	updateVisuals( entry );
        		validateMaxScore();
        	}
      }
    }
    
    /** Returns a positive long between 0 and bounds. */ 
    public static long nextLong( long bounds ) {
    	if (bounds <= 0)
            throw new IllegalArgumentException("bounds must be positive");
    	long val;
    	do {
    	   val = random.nextLong();
    	   if ( val > 0 )
    		   val %= bounds;    		
    	} while ( val < 0 );
    	return val;
    }
}