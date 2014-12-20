package frets.swing.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
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
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import swingextensions.swingx.CutCopyPasteHelper;
import swingextensions.swingx.DropShadowBorder;
import swingextensions.swingx.DynamicAction;
import swingextensions.swingx.MnemonicHelper;
import swingextensions.swingx.app.Application;
import swingextensions.swingx.text.RegExStyler;
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

// TODO - Find best scoring variation. LEFT DOUBLECLK=random RIGHT DOUBLECLK=minimum? New API to fretboard?
// TODO - Variations are tied to root note. For example C3 chords cannot find C4 as variations. Fix.
// TODO - Add filtering or remove completely.
// TODO - Redo score to be a weighted composite
// TODO - Add all variations. Add 10 best variations. Check to not dup the current row.
// TODO - Proper column sorting. Currently G2, G#2, G3 and variations sort funny. 
/**
 * The Controller for the Frets application. Controller gets its name
 * from the model view controller (MVC) pattern. Controller is responsible
 * for creating the UI, listening for changes to both the model and view
 * and keeping everything in sync.
 */
public class Controller {
	public static final int RANDOM_VARIATION = -1;
    public static final String ENTRY_NAME_DELIM = ",";
    
    // A hack, but better than hard coding.
    public static final int ROOT_COL = 0;
    public static final int FORMULA_COL = 1;
    public static final int NOTES_COL = 2;
    public static final int LOCATIONS_COL = 3;
    public static final int VARIATIONS_COL = 4;
    public static final int SCORE_COL = 5;

    protected static Random random = new Random();
    protected static ResourceBundle resources = Application.getInstance().getResourceBundle();

    private Fretboard fretboard;
    private ChordRank ranker;

    // The list of items displayed in the table.
    protected EntryTableModel entryTableModel;
    protected JTable entryTable;    
    
    // Shared entry fields
    private JTextField fretboardTF;
    private JTextField rankerTF;
    private JTextField filterTF;

    // Image display fields
    private JLabel fretsDetailsPanel;
    private JLabel fretsLargePanel;    
    
    private BarChartVisualizer visualizer;
    private int maxSumScore = Integer.MIN_VALUE;
    private int minSumScore = Integer.MAX_VALUE;

    private JTextPane commentsTP;

    private Display displayOpts = new Display();
    private DisplayEditor displayEditor = new DisplayEditor( displayOpts );
    
    private static AboutBox aboutBox;
    
    public Controller(JFrame host) {
    	System.out.println( "FretsController cons");
        
        createFrameComponents();
        createUI(host);
        createMenu(host);
        disableControls();
        host.pack();
        
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
    
    // Adds a new displayEntry
    public void addEntry() {
        // Create the new entry, adding some default values.
        ExtendedDisplayEntry entry = randomEntry( 10 );
       	System.out.println( "Controller.addEntry entry=" + entry );
        // Add the entry to the end of the list.
        entryTableModel.add(entry);
        validateMaxScore();

        // Set a new focus.
    	int selectedRow = entryTable.getSelectedRow();
    	if ( -1 == selectedRow )
           entryTable.setRowSelectionInterval( 0, 0 );
    }

    public ExtendedDisplayEntry randomEntry( int scoreMax ) {
        // Create the new entry, adding some random values.
        ExtendedDisplayEntry entry = new ExtendedDisplayEntry();
    	int scoreSum = Integer.MAX_VALUE;
        final int RETRY_MAX = 100;
    	int retryCount = 0;
    	LocationList locations = null;
    	List<LocationList> variations = null;
    	int permutations = 0;
    	int variationi = -1;
        
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
            variationi = random.nextInt( permutations );
            locations = Fretboard.getPermutation(variations, variationi);
            scoreSum = ranker.getSum( locations );
            // Essentially this makes the loop end after one try.
            if ( -1 == scoreMax ) scoreMax = Integer.MAX_VALUE;
        }
       	// System.out.println( "Controller.randomEntry retryCount=" + retryCount );

        entry.setMember( "Locations", locations.toString() );      
        entry.setMember( "Variation", Fretboard.getPermutationString(variations, variationi) );
        entry.setMember( "Score", ranker.getScoreString(locations) );
        
        return entry;	   
    }
     
    // Adds variations on selected entry
    public void varyTen() {
    	int [] selectedRows = entryTable.getSelectedRows();
    	if (( null != selectedRows ) && (selectedRows.length > 0)) {
    		// For now, get first one.
    		int firstSelection = selectedRows[ 0 ];
     	   	ExtendedDisplayEntry entry = entryTableModel.get( firstSelection );
     	   	// System.out.println( "   Selected row " + i + "=" + entry );
    		List<ExtendedDisplayEntry> variations = getVariations( entry, 10 );

    		// Add the entry to the end of the list.
    		if (( null != variations ) && (variations.size() > 0 )) {
    			entryTableModel.addAll( variations );
                validateMaxScore();
    		}
    	}
    }

    // Adds variations on selected entry
    public void varyAll() {
    	int [] selectedRows = entryTable.getSelectedRows();
    	if (( null != selectedRows ) && (selectedRows.length > 0)) {
    		// For now, get first one.
    		int firstSelection = selectedRows[ 0 ];
     	   	ExtendedDisplayEntry entry = entryTableModel.get( firstSelection );
    		List<ExtendedDisplayEntry> variations = getVariations( entry, Integer.MAX_VALUE );

    		// Add the entry to the end of the list.
    		if (( null != variations ) && (variations.size() > 0 )) {
    			entryTableModel.addAll( variations );
                validateMaxScore();
    		}
    	}
    }

    public List<ExtendedDisplayEntry> getVariations( ExtendedDisplayEntry entry, int count ) {
    	if ( null == entry) return null;
        // Vary the provided entry.
    	List<ExtendedDisplayEntry> entryVariations = new ArrayList<ExtendedDisplayEntry>();
    	NoteList notes = new NoteList( (String) entry.getMember( "Notes" ) );
    	List<LocationList> variations = fretboard.getEnharmonicVariations( notes );
        int permutations = Fretboard.getPermutationCount( variations );
        String variationString = (String) entry.getMember( "Variation" );
        int variation = -1;
        if (null != variationString) {
        	// Get variation from string.
        	String [] args = variationString.split( "/" );
        	if (( null != args ) && ( args.length > 0))
        		variation = Integer.parseInt(args[0]);
        }
        
        int maxPerm = Math.min( permutations, count );
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
	            entryVariations.add( newEntry );
            }
        }
        
        return entryVariations;	   
    }
     
    public void deleteSelection() {
    	boolean needsUpdate = false;
    	int selectedRow = entryTable.getSelectedRow();
    	while ( -1 != selectedRow ) {
    		entryTableModel.remove( selectedRow );
    		needsUpdate = true;
    		selectedRow = entryTable.getSelectedRow();
    	}
    	if ( needsUpdate ) {
            validateMaxScore();
            disableControls();
            if ( entryTable.getRowCount() > 0 )
               entryTable.setRowSelectionInterval( 0, 0 );
    	}
    }
    
    public void deleteAll() {
		entryTableModel.clear();
        disableControls();       
    }
    
    public void showAbout() {
       aboutBox.show(SwingUtilities.getWindowAncestor(filterTF));
    }

    // Disables the controls (e.g. after deleteAll or if an invalid entry has been selected) 
    public void disableControls() {
        fretboardTF.setEditable(false);
        rankerTF.setEditable(false);
        filterTF.setEditable(false);
        filterTF.setText("");
        
        fretsDetailsPanel.setIcon( null );
        fretsDetailsPanel.setToolTipText( null );
        fretsLargePanel.setIcon( null );
        fretsLargePanel.setToolTipText( null );
        
        visualizer.setColumns( new int [] { 0 } );
        visualizer.setAnimatesTransitions( true );

        commentsTP.setText("");
        commentsTP.setEditable(false);

        displayEditor.setEditable(false);
    }
    
    // Returns true if the app can exit, false otherwise.
    protected boolean canExit() {
    	// Do exit cleanup
        return true;
    }

    private void createFrameComponents() {
        fretboardTF = new JTextField(15);
        rankerTF = new JTextField(15);
        filterTF = new JTextField(15);
        // filterTF.getDocument().addDocumentListener(new FilterDocumentHandler());
                
        fretsDetailsPanel = new JLabel();
        // detailsImagePanel.setBorder( new CompoundBorder(new LineBorder(Color.DARK_GRAY, 1),  new EmptyBorder(2, 2, 2, 2)));
        fretsDetailsPanel.setBorder( new LineBorder(Color.DARK_GRAY, 1) );
        fretsDetailsPanel.setPreferredSize(new Dimension(150, 150));
        fretsDetailsPanel.setBackground( displayOpts.backgroundColor );
        fretsDetailsPanel.setOpaque(true);
        fretsDetailsPanel.setToolTipText( null );
        fretsDetailsPanel.addMouseListener(new PopupMenuListener( ));
        
        visualizer = new BarChartVisualizer();
        visualizer.setOpaque(false);
        // Makes it align with image
        visualizer.setBorder(new EmptyBorder(5, 5, 5, 5));
        visualizer.setColumns( new int [] { 0 });

        commentsTP = new JTextPane();

        fretsLargePanel = new JLabel();
        // largeImagePanel.setBorder( new CompoundBorder(new LineBorder(Color.DARK_GRAY, 1),  new EmptyBorder(2, 2, 2, 2)));
        fretsLargePanel.setBorder( new LineBorder(Color.DARK_GRAY, 1) );
        fretsLargePanel.setPreferredSize(new Dimension(600, 150));
        fretsLargePanel.setBackground( displayOpts.backgroundColor );
        fretsLargePanel.setOpaque(true);
        fretsLargePanel.setToolTipText( null );
        fretsLargePanel.addMouseListener(new PopupMenuListener());
    }
    
    private void createUI(JFrame frame) {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(Application.getResourceAsString("tab.details"), createDetailsPanel());
        tabbedPane.addTab(Application.getResourceAsString("tab.comments"), null);
        tabbedPane.addTab(Application.getResourceAsString("tab.display"), createDisplayEditorPanel());
        tabbedPane.addChangeListener(new TabbedPaneChangeHandler(tabbedPane));

        JPanel fixedPanel = new JPanel();
        JLabel fretboardLabel = new JLabel(resources.getString("label.fretboard"));
        fixedPanel.add( fretboardLabel );
        String defaultFretboard = resources.getString("default.fretboard");
		fretboard = Fretboard.instance.getInstance( defaultFretboard );
		fretboardTF.setText( fretboard.getMetaDescription() );
		fretboardTF.setEditable( false );
        fixedPanel.add( fretboardTF );
        JLabel rankerLabel = new JLabel(resources.getString("label.ranker"));
        fixedPanel.add( rankerLabel );
        String defaultRanker = resources.getString("default.ranker");
        // System.out.println ( "Formula ranker=\"" + defaultRanker + "\".");
		ranker = ChordRank.instance.getInstance( defaultRanker );
		rankerTF.setText( ranker.getMetaName() );
		rankerTF.setEditable( false );
        fixedPanel.add( rankerTF );
        JLabel filterLabel = new JLabel(resources.getString("label.filter"));
        fixedPanel.add( filterLabel );
        fixedPanel.add( filterTF );

        // Create table in a view port.
        entryTableModel = new EntryTableModel();
        entryTableModel.addTableModelListener( new FretsTableModelListener() );
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
    
    // Creates and populates the menu for the app
    private void createMenu(JFrame frame) {
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
        JMenuItem addMenuItem = MnemonicHelper.createMenuItem(entryMenu, resources.getString("menu.newEntry"));
        addMenuItem.addActionListener(new DynamicAction(this, "addEntry"));
        addMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl R"));
        JMenuItem deleteMenuItem = MnemonicHelper.createMenuItem(entryMenu, resources.getString("menu.deleteSelected"));
        deleteMenuItem.addActionListener(new DynamicAction(this, "deleteSelection"));
        deleteMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl D"));
        JMenuItem deleteAllMenuItem = MnemonicHelper.createMenuItem(entryMenu, resources.getString("menu.deleteAll"));
        deleteAllMenuItem.addActionListener(new DynamicAction(this, "deleteAll"));
        deleteAllMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl shift D"));
        JMenuItem varyTenMenuItem = MnemonicHelper.createMenuItem(entryMenu, resources.getString("menu.varyTen"));
        varyTenMenuItem.addActionListener(new DynamicAction(this, "varyTen"));
        varyTenMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl V"));
        JMenuItem varyAllMenuItem = MnemonicHelper.createMenuItem(entryMenu, resources.getString("menu.varyAll"));
        varyAllMenuItem.addActionListener(new DynamicAction(this, "varyAll"));
        varyAllMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl shift V"));

        JMenu helpMenu = MnemonicHelper.createMenu(resources.getString("menu.help"));
        menuBar.add(helpMenu);
        createMenuItem(helpMenu, "menu.about", new DynamicAction(this, "showAbout"));        
    }

    
    // Convenience to create a configure a JMenuItem.
    private JMenuItem createMenuItem(JMenu menu, String key, Action action) {
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
    
    private Component createDetailsPanel() {
        JPanel detailsWrapper = new JPanel(new BorderLayout());
        detailsWrapper.setOpaque(false);
        detailsWrapper.setBorder(new DropShadowBorder(Color.BLACK, 0, 5, .5f, 12, false, true, true, true));
        detailsWrapper.add(fretsDetailsPanel);
        detailsWrapper.add( "South", visualizer);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(fretsLargePanel);
        panel.add( "West", detailsWrapper);
        return panel;
    }
    
    private Component createNotesPanel() {
        createNotesTextPane();
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
    
	private void createNotesTextPane() {
		SimpleAttributeSet urlAttributes = new SimpleAttributeSet();
		StyleConstants.setForeground(urlAttributes, Color.blue);
		StyleConstants.setBackground(urlAttributes, Color.white);
		StyleConstants.setUnderline(urlAttributes, true);
		RegExStyler styler = new RegExStyler(commentsTP);
		styler.addStyle("(http|ftp)://[_a-zA-Z0-9./~\\-]+", urlAttributes,
				Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		// commentsTP.getDocument().addDocumentListener(new DocumentHandler());
		// commentsTP.addMouseListener(new NotesMouseHandler(styler)); // looks up URIs in notes
	}

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
    
    @SuppressWarnings("serial")
	protected  void createEntryTable( EntryTableModel entryTableModel) {
        entryTable = new JTable( entryTableModel ){
            // Implement table cell tool tips.           
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                Point p = e.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                switch ( colIndex ) {
                   case ROOT_COL: return "Click to edit root note";
                   case FORMULA_COL: return "Click to edit formula";
                   case VARIATIONS_COL: return "Left/right click to change. Left double click for random. Right double click for best score.";
                } // colIndex
                return null;
            }
        };
        entryTable.setFillsViewportHeight(true);
        entryTable.setAutoCreateRowSorter(true);
        entryTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        entryTable.getSelectionModel().addListSelectionListener(new RowListener());
        
        TableColumnModel tcm = entryTable.getColumnModel();
        TableColumn rootColumn = tcm.getColumn(0);
        rootColumn.setCellEditor(new NoteTableEditor());

        CutCopyPasteHelper.registerCutCopyPasteBindings(entryTable);
        CutCopyPasteHelper.setPasteEnabled(entryTable, true);
    }
    
    public Image getDetailsImage(ExtendedDisplayEntry selectedEntry) {
    	// System.out.println( "Controller requesting details image size=" + detailsImagePanel.getSize());
    	BufferedImage image = null;
    	String imagePath = (String) selectedEntry.getMember( "ImagePath" );
        if ( imagePath != null) {
    	    try {
    		   image = javax.imageio.ImageIO.read( new File( imagePath ));
    		} catch (IOException e) {
    		  	throw new IllegalArgumentException( e );
    		}
        } else {
        	String locationString = (String) selectedEntry.getMember("Locations");
      	    // System.out.println( "Controller locations=\"" + locationString + "\", detailsImagePanel=" + detailsImagePanel.getSize());
            if (( null!= locationString ) && (locationString.length() > 0)) {
            	LocationList locations = LocationList.parseString( locationString );
                displayOpts.orientation = Orientation.VERTICAL;
            	displayOpts.setDisplayAreaStyleMinAperture( fretboard, locations, 5 ); // set window to 5 frets.
            	image = RasterRenderer.renderImage( fretsDetailsPanel.getSize(), displayOpts, fretboard, selectedEntry );
            }
        }
        return image;
    }

    public Image getLargeImage(ExtendedDisplayEntry selectedEntry) {
    	// System.out.println( "Controller requesting large image size=" + largeImagePanel.getSize());
    	BufferedImage image = null;
    	// Set large display to entire fretboard.
    	Display largeDisplayOpts = new Display( displayOpts );
    	largeDisplayOpts.orientation = Orientation.HORIZONTAL;
    	largeDisplayOpts.setDisplayAreaStyleMaxFretboard(fretboard);
    	largeDisplayOpts.showVariations = true;
       	image = RasterRenderer.renderImage( fretsLargePanel.getSize(), largeDisplayOpts, fretboard, selectedEntry );        	
        return image;
    }

	// Changed from selectionChangedModel to ListSelectionListener
    // Need to unify/refactor with selection model listener
    private class RowListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent event) {
            if (event.getValueIsAdjusting()) {
                return;
            }
            // StringBuffer output = new StringBuffer("RowListener: ");
            // outputSelection( output );
            // System.out.println( output.toString() );
            
            int [] rows = entryTable.getSelectedRows();
            if ((null != rows) && ( rows.length > 0)) {
            	int firstIndex = rows[ 0 ];
            	ExtendedDisplayEntry entry = entryTableModel.get( firstIndex );
      		  	updateVisuals( entry );
            } else {
            	disableControls();
            }
        }
    }
        
    @SuppressWarnings("unused")
	private void outputSelection( StringBuffer output) {
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
	
	// Changed from selectionChangedModel to ListSelectionListener
    // Need to unify/refactor with selection model listener
    private class FretsTableModelListener implements TableModelListener {
        public void tableChanged(TableModelEvent event) {
            // StringBuffer output = new StringBuffer("RowListener: ");
            // outputSelection( output );
            // System.out.println( output.toString() );
        	int type = event.getType();
        	String typeStr = "unknown";
        	switch ( type ) {
        		case TableModelEvent.INSERT: typeStr = "insert"; break;
        		case TableModelEvent.DELETE: typeStr = "delete"; break;
        		case TableModelEvent.UPDATE: typeStr = "update"; break;
        	}
        	System.out.println( "Controller FretsTableModelListener type=" + typeStr +
        			", row=" + event.getFirstRow() + ".." + event.getLastRow() +
        			", col=" + event.getColumn() +
        			", event=" + event );
        	  int col = event.getColumn();
        	  if ((type == TableModelEvent.UPDATE) && ( col >= 0) && ( col <= 1)) {
        		  ExtendedDisplayEntry entry = entryTableModel.get( event.getFirstRow() );
        		  updateEntry( entry );
        		  updateVisuals( entry );
        	  }
            
        }
    }
    
    /** Updates an entry from the root and the formula. */
    protected void updateEntry( ExtendedDisplayEntry entry ){
    	String root = (String) entry.getMember( "Root" );
    	String formula = (String) entry.getMember( "Formula" );
	    
    	NoteList notes = new NoteList();
	    notes.setRelative( new Note( root ), formula );
	    entry.setMember( "Notes", notes.toString() );

	    // Calculate other information fields.
        List<LocationList> variations = fretboard.getEnharmonicVariations( notes );
	    int permutations = Fretboard.getPermutationCount( variations );
	    int variationi = random.nextInt( permutations );
	    LocationList locations = Fretboard.getPermutation(variations, variationi);
	    // scoreSum = ranker.getSum( locations );

	    entry.setMember( "Locations", locations.toString() );      
        entry.setMember( "Variation", Fretboard.getPermutationString(variations, variationi) );
        entry.setMember( "Score", ranker.getScoreString(locations) );    
    }

    /** Updates the main display from an updated entry. */
    protected void updateVisuals( ExtendedDisplayEntry entry ) {
    	String entryText = getShortName( entry);

    	fretsDetailsPanel.setIcon( new ImageIcon( getDetailsImage( entry ) ));
        fretsDetailsPanel.setToolTipText( entryText );
        fretsLargePanel.setIcon( new ImageIcon( getLargeImage( entry )) );
        fretsLargePanel.setToolTipText( entryText );
        
        String scoreString = (String) entry.getMember( "Score" );
        // "Scores sum=22, fret bounds[0,15]=0, fret span=7, skip strings=5, same string=10"
        int [] scores = ChordRank.toScores( scoreString );
        visualizer.setColumns( scores );
        visualizer.repaint();    	
    }
    
    // Ensure the new list items update the max score.
    protected void validateMaxScore() {
        maxSumScore = Integer.MIN_VALUE;
        minSumScore = Integer.MAX_VALUE;
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
  	    visualizer.setMaxValue( maxSumScore );
  	    visualizer.setMaxValue( maxSumScore );
    }

    public final class TabbedPaneChangeHandler implements ChangeListener {
        private final JTabbedPane tp;
        
        public TabbedPaneChangeHandler(JTabbedPane tp) {
            this.tp = tp;
        }
        
        public void stateChanged(ChangeEvent e) {
            if (tp.getSelectedIndex() == 1 && tp.getComponentAt(1) == null) {
                tp.setComponentAt(1, createNotesPanel());
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
    	sb.append( entry.getMember("Root") );
		sb.append( ENTRY_NAME_DELIM );
		sb.append( entry.getMember("Formula"));
		sb.append( ENTRY_NAME_DELIM );
		// Example variation string "6/8 (012/124)"
	    String variation = 	(String) entry.getMember("Variation");
	    if ( null != variation ) {
		    int [] values = Fretboard.getPermutationValues(variation);
		    if ( null != values ) {
		    	sb.append( values[ 0 ] + "-" + values[ 1 ] );		    	
		    }
	    }        	    
		// Example string "G2,R-3-5,6-8"
   	    return sb.toString();
    }
    
    protected class VariationMouseAdapter extends MouseAdapter {
	    @Override
	    public void mouseClicked(MouseEvent evt) {
	        int row = entryTable.rowAtPoint(evt.getPoint());
	        int col = entryTable.columnAtPoint(evt.getPoint());
	        if ((row >= 0 ) && (col == VARIATIONS_COL)) {
	        	// System.out.println( "TableMouseListener.mouseClicked button" + evt.getButton() + ", row/col=" + row + "/" + col );
	        	int modelRow = entryTable.convertRowIndexToModel( row );
	        	if (( modelRow >= 0 ) && ( modelRow <= entryTableModel.size())) {
	        		ExtendedDisplayEntry entry = (ExtendedDisplayEntry) entryTableModel.getRowAt( modelRow );
	        		switch (evt.getButton()) {
	        		case MouseEvent.BUTTON1: case MouseEvent.BUTTON2: case MouseEvent.BUTTON3: {
	        		String variationStr = (String) entry.getMember( VARIATIONS_COL ); // variation
	        		if ( null != variationStr ) {
	        			int [] values = Fretboard.getPermutationValues(variationStr);
	        			int currentVar = values[ 0 ];
	        			if ( evt.getButton() == MouseEvent.BUTTON1 ) {
	        				// Decrement
	        				currentVar -= 1;
	        				if ( currentVar < 0 )
	        					currentVar += values[ 1 ];
	        			} else {
	        				// Increment
	        				currentVar += 1;
	        				currentVar %= values[ 1 ];
	        			}
	        			
	        			NoteList notes = NoteList.parse( (String) entry.getMember( "Notes") );
	                	// Calculate other information fields.
	        	    	List<LocationList> variations = fretboard.getEnharmonicVariations( notes );
	                    LocationList locations = Fretboard.getPermutation(variations, currentVar );
	                    
	                    entry.setMember( "Locations", locations.toString() );      
	                    entry.setMember( "Variation", Fretboard.getPermutationString(variations, currentVar) );
	                    entry.setMember( "Score", ranker.getScoreString(locations) );
	                    // System.out.println( "TableMouseListener.mouseClicked updating vari=" + values[ 0 ] + "/" + currentVar + ", entry=" + entry ); 
                        entryTableModel.set(modelRow, entry);
                        updateVisuals( entry );
	        		} // non-null variations string
	        		} // Mouse button 1 2 3
	        		// case MouseEvent.: {
	        		// } // Mouse button 1 2 3
	        		} // switch
	        	} // valid row col	        	
	        } // VARIATIONS_COL
	    } // mouseClicked
    } // class
}