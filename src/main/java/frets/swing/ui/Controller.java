package frets.swing.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import swingextensions.swingx.CutCopyPasteHelper;
import swingextensions.swingx.DropShadowBorder;
import swingextensions.swingx.DynamicAction;
import swingextensions.swingx.JImagePanel;
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

// TODO - Add filtering or remove completely.
// TODO - Add custom controls/renderers for table entries.
// TODO - Redo score to be a weighted composite
// TODO - All variations visible on one row (or perhaps hierarchy twister?). Up down variation controls. (Easiest ranking?)
// TODO - Add default table 0 select after add or delete of element

/**
 * The Controller for the Frets application. Controller gets its name
 * from the model view controller (MVC) pattern. Controller is responsible
 * for creating the UI, listening for changes to both the model and view
 * and keeping everything in sync.
 */
public class Controller {
	public static final int RANDOM_VARIATION = -1;

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

    // Instance entry fields
    private JImagePanel detailsImagePanel;
    private NoteEditor rootEditor;
    private JTextField formulaTF;
    private JTextField notesTF;
    private JTextField locationsTF;
    private JTextField variationTF;
    private JButton variationUp;
    private JButton variationDown;
    private JTextField scoreTF;
    private BarChartVisualizer visualizer;
    private int maxSumScore = Integer.MIN_VALUE;
    private int minSumScore = Integer.MAX_VALUE;

    private JTextPane commentsTP;

    private Display displayOpts = new Display();
    private DisplayEditor displayEditor = new DisplayEditor( displayOpts );
    
    private JImagePanel largeImagePanel;    
    
    /** Disables cascading change updates when multiple controls need updating. */
    protected boolean programmaticChange = false;
    
    private static AboutBox aboutBox ;
    
    public Controller(JFrame host) {
    	System.out.println( "FretsController cons");
        
        programmaticChange = true;
        createFrameComponents();
        createUI(host);
        createMenu(host);
        disableControls();
        host.pack();
        programmaticChange = false;
        
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
        
        // And give the root editor.
        rootEditor.requestFocus();
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
    		}
			// Focus on the root editor.
			rootEditor.requestFocus();    		
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
    		}
			// Focus on the root editor.
			rootEditor.requestFocus();
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
            disableControls();        	  		
    	}
		// Focus on the root editor.
		rootEditor.requestFocus();
    }
    
    public void deleteAll() {
		entryTableModel.clear();

        disableControls();       
        // And give the root editor.
        rootEditor.requestFocus();
    }
    
    public void showAbout() {
       aboutBox.show(SwingUtilities.getWindowAncestor(filterTF));
    }

    // Disables the controls (e.g. after deleteAll or if an invalid entry has been selected) 
    public void disableControls() {
        programmaticChange = true;
        fretboardTF.setEditable(false);
        rankerTF.setEditable(false);
        filterTF.setEditable(false);
        filterTF.setText("");
        
        rootEditor.setEditable(false);
        formulaTF.setEditable(false);
        formulaTF.setText("");
        notesTF.setEditable(false);
        notesTF.setText("");
        locationsTF.setEditable(false);
        locationsTF.setText("");
        variationTF.setEditable(false);
        variationTF.setText("");
        scoreTF.setEditable(false);
        scoreTF.setText("");
        detailsImagePanel.setImage(null);
        // detailsImagePanel.setEditable(false);
        visualizer.setColumns( new int [] { 0 } );
        visualizer.setAnimatesTransitions( true );

        commentsTP.setText("");
        commentsTP.setEditable(false);

        displayEditor.setEditable(false);

        largeImagePanel.setImage(null);
        largeImagePanel.setEditable(false);
        
        programmaticChange = false;
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
        
        rootEditor = new NoteEditor( NoteEditor.Style.HORIZONTAL );
        formulaTF = createTF();
        notesTF = createTF();
        locationsTF = createTF();
        variationTF = createTF();
        variationUp = new JButton( "+" );
        variationUp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
       			String oldVariationText = variationTF.getText();
       			// Only ask for change if the button was pressed and there is variation text.
       			if (( null != oldVariationText ) && ( oldVariationText.length() > 0)) {
       				// entryChanged( (ExtendedDisplayEntry) selectedEntry, "variationUp", oldVariationText, e.getActionCommand() );
       			}
            }
        }); 
        variationUp.setPreferredSize( new Dimension( 10, 10 ));
        variationDown = new JButton( "-" );
        variationDown.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
       			String oldVariationText = variationTF.getText();
       			// Only ask for change if the button was pressed and there is variation text.
       			if (( null != oldVariationText ) && ( oldVariationText.length() > 0)) {
       				// entryChanged( (ExtendedDisplayEntry) selectedEntry, "variationDown", oldVariationText, e.getActionCommand() );
       			}
            }
        }); 
        variationDown.setPreferredSize( new Dimension( 10, 10 ));
        scoreTF = createTF();
        
        detailsImagePanel = new JImagePanel();
        // detailsImagePanel.setBorder( new CompoundBorder(new LineBorder(Color.DARK_GRAY, 1),  new EmptyBorder(2, 2, 2, 2)));
        detailsImagePanel.setBorder( new LineBorder(Color.DARK_GRAY, 1) );
        detailsImagePanel.setPreferredSize(new Dimension(150, 150));
        detailsImagePanel.setBackground(Color.WHITE);
        detailsImagePanel.setOpaque(true);
        // detailsImagePanel.addPropertyChangeListener( new ImagePanelPropertyChangeHandler() );
        
        visualizer = new BarChartVisualizer();
        visualizer.setOpaque(false);
        // Makes it align with image
        visualizer.setBorder(new EmptyBorder(5, 5, 5, 5));
        visualizer.setColumns( new int [] { 0 });

        commentsTP = new JTextPane();

        largeImagePanel = new JImagePanel();
        // largeImagePanel.setBorder( new CompoundBorder(new LineBorder(Color.DARK_GRAY, 1),  new EmptyBorder(2, 2, 2, 2)));
        largeImagePanel.setBorder( new LineBorder(Color.DARK_GRAY, 1) );
        largeImagePanel.setPreferredSize(new Dimension(600, 150));
        largeImagePanel.setBackground(Color.WHITE);
        largeImagePanel.setOpaque(true);
        // largeImagePanel.addPropertyChangeListener( new ImagePanelPropertyChangeHandler() );        
    }
    
    private void createUI(JFrame frame) {
        JTabbedPane tp = new JTabbedPane();
        tp.addTab(Application.getResourceAsString("tab.details"), createDetailsPanel());
        tp.addTab(Application.getResourceAsString("tab.large"), createLargePanel());
        tp.addTab(Application.getResourceAsString("tab.comments"), null);
        tp.addTab(Application.getResourceAsString("tab.display"), createDisplayEditorPanel());
        tp.addChangeListener(new TabbedPaneChangeHandler(tp));

        GroupLayout frameLayout = new GroupLayout(frame.getContentPane());
        frame.setLayout(frameLayout);
        frameLayout.setAutoCreateContainerGaps(true);
        frameLayout.setAutoCreateGaps(true);

        JLabel fretboardLabel = new JLabel(resources.getString("label.fretboard"));
        String defaultFretboard = resources.getString("default.fretboard");
		fretboard = Fretboard.instance.getInstance( defaultFretboard );
		fretboardTF.setText( fretboard.getMetaDescription() );
		fretboardTF.setEditable( false );
        JLabel rankerLabel = new JLabel(resources.getString("label.ranker"));
        String defaultRanker = resources.getString("default.ranker");
        // System.out.println ( "Formula ranker=\"" + defaultRanker + "\".");
		ranker = ChordRank.instance.getInstance( defaultRanker );
		rankerTF.setText( ranker.getMetaName() );
		rankerTF.setEditable( false );
        JLabel filterLabel = new JLabel(resources.getString("label.filter"));

        // Create table in a view port.
        entryTableModel = new EntryTableModel();
		createEntryTable( entryTableModel ) ;		
        JScrollPane entrySP = new JScrollPane(entryTable);
        entrySP.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        entrySP.setViewportView( entryTable );             

        GroupLayout.ParallelGroup hGroup = 
        	frameLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        hGroup.
          addGroup(GroupLayout.Alignment.TRAILING, frameLayout.createSequentialGroup().
            addComponent(fretboardLabel).
            addComponent(fretboardTF, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Integer.MAX_VALUE). // min, pref, max
            addComponent(rankerLabel).
            addComponent(rankerTF, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Integer.MAX_VALUE).
            addComponent(filterLabel).
            addComponent(filterTF, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, Integer.MAX_VALUE)).
          addComponent(entrySP, 100, 600, Integer.MAX_VALUE ).
          addComponent(tp);
        frameLayout.setHorizontalGroup(hGroup);
        
        GroupLayout.SequentialGroup vGroup = frameLayout.createSequentialGroup();
        vGroup.
          addGroup(frameLayout.createParallelGroup(GroupLayout.Alignment.BASELINE).
            addComponent(fretboardLabel).
            addComponent(fretboardTF).
            addComponent(rankerLabel).
            addComponent(rankerTF).
            addComponent(filterLabel).
            addComponent(filterTF)).
          addComponent(entrySP, 100, 280, Integer.MAX_VALUE).
          addComponent(tp, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Integer.MAX_VALUE);
        frameLayout.setVerticalGroup(vGroup);
        // ideally want 3 or more entries high.
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
        
        JMenu editMenu = MnemonicHelper.createMenu( resources.getString("menu.edit"));
        menuBar.add(editMenu);
        JMenuItem cutMI = createMenuItem(editMenu, "menu.cut", CutCopyPasteHelper.getCutAction());
        cutMI.setAccelerator(KeyStroke.getKeyStroke("ctrl X"));
        editMenu.add(cutMI);
        JMenuItem copyMI = createMenuItem(editMenu, "menu.copy", CutCopyPasteHelper.getCopyAction());
        copyMI.setAccelerator(KeyStroke.getKeyStroke("ctrl C"));
        editMenu.add(copyMI);
        JMenuItem pasteMI = createMenuItem(editMenu, "menu.paste",CutCopyPasteHelper.getPasteAction());
        pasteMI.setAccelerator(KeyStroke.getKeyStroke("ctrl V"));
        editMenu.add(pasteMI);
                
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
        JPanel imageWrapper = new JPanel(new BorderLayout());
        imageWrapper.setOpaque(false);
        imageWrapper.add(detailsImagePanel);
        imageWrapper.setBorder(new DropShadowBorder(Color.BLACK, 0, 5, .5f, 12, false, true, true, true));
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        
        JLabel rootLabel = new JLabel(Application.getResourceAsString("label.root"));
        JLabel formulaLabel = new JLabel(Application.getResourceAsString("label.formula"));
        JLabel notesLabel = new JLabel(Application.getResourceAsString("label.notes"));
        JLabel locationsLabel = new JLabel(Application.getResourceAsString("label.locations"));
        JLabel variationLabel = new JLabel(Application.getResourceAsString("label.variation"));
        JLabel scoreLabel = new JLabel(Application.getResourceAsString("label.score"));
        
        GroupLayout layout = new GroupLayout(panel);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        panel.setLayout(layout);
        GroupLayout.SequentialGroup hg = layout.createSequentialGroup();
        layout.setHorizontalGroup(hg);
        hg.
          addGroup(layout.createParallelGroup().
             addComponent(imageWrapper, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Integer.MAX_VALUE ).
             addComponent(visualizer )).
          addGroup(layout.createParallelGroup().
            addComponent(rootLabel).
            addComponent(formulaLabel).
            addComponent(notesLabel).
            addComponent(locationsLabel).
            addComponent(variationLabel).
            addComponent(scoreLabel)).
          addGroup(layout.createParallelGroup().
            addComponent(rootEditor, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Integer.MAX_VALUE ).
            addComponent(formulaTF, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Integer.MAX_VALUE ).
            addComponent(notesTF, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Integer.MAX_VALUE ).
            addComponent(locationsTF, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Integer.MAX_VALUE ).
            addGroup( layout.createSequentialGroup().
            		addComponent(variationTF, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Integer.MAX_VALUE ).
            		addComponent(variationUp ).
            		addComponent(variationDown )
            ).
            addComponent(scoreTF, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Integer.MAX_VALUE )
        );
        
        GroupLayout.ParallelGroup vg = layout.createParallelGroup();
        layout.setVerticalGroup(vg);
        vg.
          addGroup(layout.createSequentialGroup().
       		addComponent(imageWrapper, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, Integer.MAX_VALUE).
       		addComponent(visualizer)).
          addGroup(layout.createSequentialGroup().
            addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
              addComponent(rootLabel).
              addComponent(rootEditor)).
            addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
              addComponent(formulaLabel).
              addComponent(formulaTF)).
           addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
              addComponent(notesLabel).
              addComponent(notesTF)).
           addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
              addComponent(locationsLabel).
              addComponent(locationsTF)).
            addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
              addComponent(variationLabel).
              addComponent(variationTF).
              addComponent(variationUp).
              addComponent(variationDown)
              ).
           addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE).
              addComponent(scoreLabel).
              addComponent(scoreTF))
        );
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
		commentsTP.addMouseListener(new NotesMouseHandler(styler));
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
    
    private Component createLargePanel() {
        JPanel imageWrapper = new JPanel(new BorderLayout());
        imageWrapper.setOpaque(false);
        imageWrapper.add(largeImagePanel);
        imageWrapper.setBorder(new DropShadowBorder(Color.BLACK, 0, 5, .5f, 12, false, true, true, true));
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        
        GroupLayout layout = new GroupLayout(panel);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        panel.setLayout(layout);
        GroupLayout.SequentialGroup hg = layout.createSequentialGroup();
        layout.setHorizontalGroup(hg);
        hg.addComponent(imageWrapper, 1, 1, Integer.MAX_VALUE );
        
        GroupLayout.ParallelGroup vg = layout.createParallelGroup();
        layout.setVerticalGroup(vg);
        vg.addComponent(imageWrapper, 1, 1, Integer.MAX_VALUE );
        return panel;
    }
    
    protected  void createEntryTable( EntryTableModel entryTableModel) {
        entryTable = new JTable( entryTableModel );
        entryTable.setFillsViewportHeight(true);
        entryTable.setAutoCreateRowSorter(true);
        entryTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        entryTable.getSelectionModel().addListSelectionListener(new RowListener());
        entryTable.setSize( 400, 100 );
        
        CutCopyPasteHelper.registerCutCopyPasteBindings(entryTable);
        CutCopyPasteHelper.setPasteEnabled(entryTable, true);
    }
    
    private JTextField createTF() {
        JTextField tf = new JTextField(10);
        return tf;
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
            	image = RasterRenderer.renderImage( detailsImagePanel.getSize(), displayOpts, fretboard, selectedEntry );
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
       	image = RasterRenderer.renderImage( largeImagePanel.getSize(), largeDisplayOpts, fretboard, selectedEntry );        	
        return image;
    }

	// Changed from selectionChangedModel to ListSelectionListener
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
            	ExtendedDisplayEntry firstSelection = entryTableModel.get( firstIndex );
            	// System.out.println( "Row Listener firstSelection=" + firstSelection.toString());
                detailsImagePanel.setImage( getDetailsImage( firstSelection ) );
                largeImagePanel.setImage( getLargeImage( firstSelection ) );
                
                validateMaxScore();
                String scoreString = (String) firstSelection.getMember( "Score" );
                // "Scores sum=22, fret bounds[0,15]=0, fret span=7, skip strings=5, same string=10"
                int [] scores = ChordRank.toScores( scoreString );
                visualizer.setColumns( scores );
                visualizer.repaint();
            } else {
            	disableControls();
                maxSumScore = Integer.MIN_VALUE;
                minSumScore = Integer.MAX_VALUE;
            }
        }
    }
    
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
	
    // Ensure the new list items update the max score.
    protected void validateMaxScore() {
    	for ( ExtendedDisplayEntry entry : entryTableModel ) {
     	    String scoreString = (String) entry.getMember( "Score" );
     	    int [] scores = ChordRank.toScores(scoreString);
     	    int sumScore = scores[ 0 ];
            if ( sumScore > maxSumScore ) { 
         	   maxSumScore = sumScore;
         	   visualizer.setMaxValue( maxSumScore );
            }    		
            if ( sumScore < minSumScore ) { 
          	   minSumScore = sumScore;
          	   visualizer.setMaxValue( maxSumScore );
             }    		
    	}
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
    
    public static final class NotesMouseHandler extends MouseAdapter {
        private final RegExStyler styler;
        
        public NotesMouseHandler(RegExStyler styler) {
            this.styler = styler;
        }
        
        public void mouseClicked(MouseEvent e) {
            String text = styler.getMatchingText(e, styler.getStyles().get(0));
            // PENDING: make sure this works in webstart!
            if (text != null && Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI(text));
                } catch (IOException ex) {
                } catch (URISyntaxException ex) {
                } catch (UnsupportedOperationException ex) {
                }
            }
        }
    }
}