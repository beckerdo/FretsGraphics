package frets.swing.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import swingextensions.swingx.CutCopyPasteHelper;
import swingextensions.swingx.DropShadowBorder;
import swingextensions.swingx.DynamicAction;
import swingextensions.swingx.JImagePanel;
import swingextensions.swingx.MnemonicHelper;
import swingextensions.swingx.app.Application;
import swingextensions.swingx.binding.JListListControllerAdapter;
import swingextensions.swingx.binding.JTableListControllerAdapter;
import swingextensions.swingx.binding.ListController;
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

/**
 * The Controller for the Frets application. Controller gets its name
 * from the model view controller (MVC) pattern. Controller is responsible
 * for creating the UI, listening for changes to both the model and view
 * and keeping everything in sync.
 * Based on the Sun Swing PasswordStore demo.
 */
public class Controller {
	public static final int RANDOM_VARIATION = -1;

	protected static Random random = new Random();
    protected static ResourceBundle resources = Application.getInstance().getResourceBundle();

    private Fretboard fretboard;
    private ChordRank ranker;

    // Model for collection of ExtendedDisplayEntrys.
    private ListController<ExtendedDisplayEntry> listController;    
    
    // TODO Gut the listController and the entryTableAdapter
    // Big model change
    protected EntryTableModel entryTableModel = new EntryTableModel();
    
    // The selected entry
    private ExtendedDisplayEntry selectedEntry;
    // PropertyChangeListener attached to each displayEntry
    private PropertyChangeListener selectedEntryChangeListener;

    private JTable entryTable;    
    private JTableListControllerAdapter<ExtendedDisplayEntry> entryTableAdapter;

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
        
        listController = new FilteredEntryListController();
        List<ExtendedDisplayEntry> list = new LinkedList<ExtendedDisplayEntry>();
        listController.setEntries( list );
        listController.addPropertyChangeListener( new ListControllerPropertyChangeListener() );
        selectedEntryChangeListener = new SelectedEntryPropertyChangeHandler();

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

        // Add the entry to the end of the list.
        add(Arrays.asList(entry), listController.getEntries().size());
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
       	System.out.println( "Controller.randomEntry retryCount=" + retryCount );

        entry.setMember( "Locations", locations.toString() );      
        entry.setMember( "Variation", Fretboard.getPermutationString(variations, variationi) );
        entry.setMember( "Score", ranker.getScoreString(locations) );
        
        return entry;	   
    }
     
    // Adds variations on selected entry
    public void varyTen() {
    	List<ExtendedDisplayEntry> selected = listController.getSelection();
    	if (( null != selected ) && (selected.size() > 0 )) {
    		// For now, get first one.
    		ExtendedDisplayEntry entry = selected.get( 0 );
    		List<ExtendedDisplayEntry> variations = getVariations( entry, 10 );

    		// Add the entry to the end of the list.
    		if (( null != variations ) && (variations.size() > 0 )) {
    			add( variations , listController.getEntries().size());
    			entryTableModel.addAll( variations );
        
    			// And give the root editor.
    			rootEditor.requestFocus();
    		}
    	}
    }

    // Adds variations on selected entry
    public void varyAll() {
    	List<ExtendedDisplayEntry> selected = listController.getSelection();
    	if (( null != selected ) && (selected.size() > 0 )) {
    		// For now, get first one.
    		ExtendedDisplayEntry entry = selected.get( 0 );
    		List<ExtendedDisplayEntry> variations = getVariations( entry, Integer.MAX_VALUE );

    		// Add the entry to the end of the list.
    		if (( null != variations ) && (variations.size() > 0 )) {
    			add( variations , listController.getEntries().size());
    			entryTableModel.addAll( variations );
        
    			// And give the root editor.
    			rootEditor.requestFocus();
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
     
    // Adds entries at the specified index.
    // Notice that we do this here as List does not provide notification.
    private void add(List<ExtendedDisplayEntry> entries, int index) {
        // Add the entries to the model
        validateMaxScore( entries );
        listController.getEntries().addAll(index, entries);
        listController.setSelection( entries );
       	entryTable.revalidate();
        entryTable.scrollRectToVisible(entryTable.getCellRect(index, 0, true));
    }
    
    public void deleteSelection() {
        // Add the entry to the end of the list.
        listController.deleteSelection();

        if ( listController.getEntryCount() > 0 ) {
          	entryTable.revalidate();
            entryTable.scrollRectToVisible(entryTable.getCellRect(0, 0, true));
        } else {
            disableControls();        	
           	entryTable.revalidate();
        }

        // And give the root editor.
        rootEditor.requestFocus();
    }
    
    public void deleteAll() {
        // Add the entry to the end of the list.
        listController.deleteAll();
		entryTableModel.retainAll( Collections.emptyList() );

        disableControls();
        
       	entryTable.revalidate();
        
        // And give the root editor.
        rootEditor.requestFocus();
    }
    
    // Deletes an item at the specified index.
    // Notice that we do this here as List does not provide change notification.
    private void delete(int index, int count) {
        List<ExtendedDisplayEntry> entries = listController.getEntries();
        for (int i = 0; i < count; i++) {
            entries.remove(index);
        }
    }
    
    public void showAbout() {
       aboutBox.show(SwingUtilities.getWindowAncestor(filterTF));
    }

    // Ensure the new list items update the max score.
    protected void validateMaxScore( List<ExtendedDisplayEntry> entries ) {
    	for ( ExtendedDisplayEntry entry : entries ) {
     	    String scoreString = (String) entry.getMember( "Score" );
     	    int [] scores = Controller.scanScore(scoreString);
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
        detailsImagePanel.setEditable(false);
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
        DocumentListener documentListener = new DocumentHandler();

        fretboardTF = new JTextField(15);
        rankerTF = new JTextField(15);
        filterTF = new JTextField(15);
        filterTF.getDocument().addDocumentListener(new FilterDocumentHandler());
        
        rootEditor = new NoteEditor( NoteEditor.Style.HORIZONTAL );
        // NoteEditor editor = new NoteEditor( NoteEditor.Style.PIANO );
        rootEditor.addPropertyChangeListener( 
        	new PropertyChangeListener() {
        		@Override
        		public void propertyChange(PropertyChangeEvent event) {
        			Object source = event.getSource();
        			System.out.println( "root note propertyChange source=" + source + ", oldValue=" + event.getOldValue() + ", newValue=" + event.getNewValue());
        			// String propName = event.getPropertyName();
                    entryChanged( (ExtendedDisplayEntry) selectedEntry, event.getPropertyName(), event.getOldValue(), event.getNewValue() );
        		}                	
        	}
        );
        
        formulaTF = createTF(documentListener);
        notesTF = createTF(documentListener);
        locationsTF = createTF(documentListener);
        variationTF = createTF(documentListener);
        variationUp = new JButton( "+" );
        variationUp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
       			String oldVariationText = variationTF.getText();
       			// Only ask for change if the button was pressed and there is variation text.
       			if (( null != oldVariationText ) && ( oldVariationText.length() > 0)) {
       				entryChanged( (ExtendedDisplayEntry) selectedEntry, "variationUp", oldVariationText, e.getActionCommand() );
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
       				entryChanged( (ExtendedDisplayEntry) selectedEntry, "variationDown", oldVariationText, e.getActionCommand() );
       			}
            }
        }); 
        variationDown.setPreferredSize( new Dimension( 10, 10 ));
        scoreTF = createTF(documentListener);
        
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

        JScrollPane entrySP = new JScrollPane(entryTable);
        entrySP.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		// createEntryTable();		
        entryTable = new JTable( entryTableModel );
        entryTable.setFillsViewportHeight(true);
        entryTable.setAutoCreateRowSorter(true);
        entryTable.setSize( 400, 100 );
//         entryTableAdapter = new JTableListControllerAdapter<ExtendedDisplayEntry>(listController, entryTable);
		
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
		if (selectedEntry == null) {
			commentsTP.setEditable(false);
		} else {
			commentsTP.setText((String) selectedEntry.getMember("Comments"));
		}
		commentsTP.getDocument().addDocumentListener(new DocumentHandler());
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
    
    private void createEntryTable() {
        entryTable = new JTable();
        entryTable.setFillsViewportHeight(true);
        entryTable.setAutoCreateRowSorter(true);
        entryTable.setSize( 400, 100 );
        
    	// JTableListControllerAdapter(ListController<T> controller, JTable table) {
        entryTableAdapter = new JTableListControllerAdapter<ExtendedDisplayEntry>(listController, entryTable);
        
        CutCopyPasteHelper.registerCutCopyPasteBindings(entryTable);
        CutCopyPasteHelper.setPasteEnabled(entryTable, true);
    }
    
    private JTextField createTF(DocumentListener documentListener) {
        JTextField tf = new JTextField(10);
        tf.getDocument().addDocumentListener(documentListener);
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
      	    // System.out.println( "Controller formula=\"" + selectedEntry.getMember( "Formula" ) + "\", detailsImagePanel=" + detailsImagePanel.getSize());
            String locationString = locationsTF.getText();
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

    private void filterChanged() {
        listController.setFilter(filterTF.getText());
    }
    
    // Listener attached to JTextField's model. Takes a callback when appropriate.
    public class DocumentHandler implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            edited(e);
        }

        public void removeUpdate(DocumentEvent e) {
            edited(e);
        }

        public void changedUpdate(DocumentEvent e) {
            // TextFields can ignore this one.
            System.out.println( "Document changed e=" + e );
        }
        
        private void edited(DocumentEvent e) {
            Document source = e.getDocument();
            // Only propagate the change if the user is responsible.
            if ( programmaticChange ) return;
            if (fretboardTF.getDocument() == source) {
               selectedEntry.setMember( "Fretboard", fretboardTF.getText());
            } else if (rankerTF.getDocument() == source) {
               selectedEntry.setMember( "Formula", formulaTF.getText());
            } else if (formulaTF.getDocument() == source) {
               selectedEntry.setMember( "Formula", formulaTF.getText());
            } else if (notesTF.getDocument() == source) {
               selectedEntry.setMember( "Notes", notesTF.getText());
            } else if (locationsTF.getDocument() == source) {
               selectedEntry.setMember( "Locations", locationsTF.getText());
            } else if (variationTF.getDocument() == source) {
               selectedEntry.setMember( "Variation", variationTF.getText());
            } else if (scoreTF.getDocument() == source) {
               selectedEntry.setMember( "Score", scoreTF.getText());
            } else if (commentsTP != null && commentsTP.getDocument() == source) {
            }
        }
    }

    // PropertyChangeListener attached to the selected entry to track changes made to it.
    public class SelectedEntryPropertyChangeHandler implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
        	if ( e.getSource().getClass().isAssignableFrom( ExtendedDisplayEntry.class ) ) {
               Object source = e.getSource();
               entryChanged( (ExtendedDisplayEntry) source, e.getPropertyName(), e.getOldValue(), e.getNewValue());
        	}
        }
    }

    // Invoked when a property on the entry has changed.
    // This may have triggered in one of two ways:
    // 1. From the textfields we're displaying
    // 2. From some other portion of the app
    //
    // Case 1 can be identified by one of the changingXXX fields. If it's true,
    // we know the edit originated from us and there is no need to reset the 
    // text in the textfield.
    private void entryChanged(ExtendedDisplayEntry displayEntry, String propertyChanged, Object oldValue, Object newValueOrCommand ) {
        if ( null == displayEntry ) return; // banging on empty UI        
        assert (selectedEntry == displayEntry);
        // Ignore unidentified property changes.
        if ( null == propertyChanged ) return;        
        // Ignore updates while some part of the program is updating.
        if ( programmaticChange ) return;
        
        // A value in the selected entry has changed, update the UI.
        programmaticChange = true;
        System.out.println( "entryChanged progammaticChanged=" + programmaticChange + ", propertyChange=" + propertyChanged + ", oldValue=" + oldValue + ", newValue=" + newValueOrCommand );
        if ("Fretboard".equals( propertyChanged )) {
            fretboardTF.setText((String) displayEntry.getMember( "Fretboard" ));
            // update all other fields
        } else if (( "variationUp".equals( propertyChanged )) || ( "variationDown".equals( propertyChanged ))) {
        	int delta = "variationUp".equals( propertyChanged ) ? 1 : -1;
   			String variationText = variationTF.getText();
   			// Only change if the button was pressed and there is variation text.
   			if (( null != variationText ) && ( variationText.length() > 0)) {  	        	
   	        	// Example variation String = "4/18 (011/233)"
   	        	StringTokenizer st = new StringTokenizer( variationText, " /()" );
   	        	int variationi = Integer.parseInt( st.nextToken() );
   	        	int variationMax = Integer.parseInt( st.nextToken() );
                int newvariationi = variationi + delta;
                if ( newvariationi < 0 ) newvariationi += variationMax;
                if ( newvariationi >= variationMax ) newvariationi -= variationMax;
   	        	// System.out.println( "Requested " + propertyChanged + ", delta=" + delta + ", new value=" + newvariationi );
   	            updateControls( displayEntry, 
   	            	(String) displayEntry.getMember( "Root" ), (String) displayEntry.getMember( "Formula" ), 
   	            	newvariationi );
   			}
        } else if (( NoteEditor.EVENT_NAME.equals( propertyChanged )) || ( "Formula".equals( propertyChanged ))) {
        	// The note editor root note changed, or the formula text changed
        	Note rootNote = rootEditor.getNote();
        	if ( null != rootNote ) {
        		String rootText = rootNote.toString();
        		String formula = (String) displayEntry.getMember( "Formula" );
        		updateControls( displayEntry, rootText, formula, RANDOM_VARIATION );
        	}
        } else if ("Variation".equals( propertyChanged )) {
            variationTF.setText((String) displayEntry.getMember( "Variation" ));
        } else if ("Comments".equals( propertyChanged )) {
            commentsTP.setText((String) displayEntry.getMember( "Comments" ));
            String imagePath = (String) displayEntry.getMember( "ImagePath" );
            if (( null != imagePath ) && ( imagePath.length() > 0 )) {
            	try {
            		detailsImagePanel.setImagePath( new URI( imagePath ));
            	} catch (URISyntaxException e) {
            		e.printStackTrace();
            	}
        	} else {
        		detailsImagePanel.setImage( null );
        	}
        }
        programmaticChange = false;
        // System.out.println( "entryChanged progammaticChanged=" + programmaticChange );
    }

    public void updateControls( ExtendedDisplayEntry displayEntry, String rootText, String formula, int variationi ) {
    	// Guard against changes with nothing in view.
    	if ((null == rootText ) || ( rootText.length() <= 0 ) || 
    		( null == formula ) || ( formula.length() <= 0 )) return;
    	System.out.println( "updateControls root=" + rootText + ", formula=" + formula );
        Note root = new Note( rootText );

        // Update other controls
        NoteList notes = new NoteList();
        notes.setRelative( root, formula );
        String newNotes = notes.toString();
        String oldNotes = notesTF.getText();
        if ( !newNotes.equals( oldNotes )) {
            displayEntry.setMember( "Notes", newNotes );
            notesTF.setText( newNotes );            	
        }

        List<LocationList> variations = fretboard.getEnharmonicVariations( notes );
        int permutations = Fretboard.getPermutationCount( variations );
        if ( variationi == RANDOM_VARIATION ) {
        	variationi = random.nextInt( permutations );
        }
        LocationList locations = Fretboard.getPermutation(variations, variationi);
        String newLocations = locations.toString();
        if ( !newLocations.equals( locationsTF.getText() )) {
        	displayEntry.setMember( "Locations", newLocations );
        	locationsTF.setText( newLocations );
        }
        
        String newVariation = Fretboard.getPermutationString(variations, variationi);
        if ( ! newVariation.equals( variationTF.getText() )) {
            displayEntry.setMember( "Variation", newVariation );
            variationTF.setText( newVariation );            	
        }
        		
        String newScore = ranker.getScoreString(locations);
        if ( !newScore.equals( scoreTF.getText() )) {
            displayEntry.setMember( "Score", newScore );
            scoreTF.setText( newScore );            	
        }
        
        detailsImagePanel.setImage( getDetailsImage( selectedEntry ) );
        largeImagePanel.setImage( getLargeImage( selectedEntry ) );    
        if (null != selectedEntry) {
        	selectedEntry.setMember( "Root", rootText );
        	selectedEntry.setMember( "Formula", formula );        	
        }
	}
    
    // PropertyChangeListener attached to the ListController. 
    // Invokes selectionChanged when the 'selection' property changes.
	@SuppressWarnings( "unchecked" )
    public class ListControllerPropertyChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == "selection") {
                selectionChanged((List<ExtendedDisplayEntry>)evt.getOldValue());
            }
        }
    }

    // Invoked when the selection in the list has changed
    private void selectionChanged(List<ExtendedDisplayEntry> oldSelection) {
        if (selectedEntry != null) {
            selectedEntry.setMember("Comments", commentsTP.getText());
            selectedEntry.removePropertyChangeListener(selectedEntryChangeListener);
        }
        List<ExtendedDisplayEntry> selection = listController.getSelection();
        
        // We're about to change the textfields.
        // Ignore any change events originating from the text fields.
        programmaticChange = true;
        
        if (selection.size() < 1) {
            // Only allow editing one value.
            disableControls();
            selectedEntry = null;
           	entryTable.repaint();
            
            // And give the root editor.
            rootEditor.requestFocus();
        } else {
            // Only one value is selected, update the fields appropriately
            selectedEntry = selection.get(0);
            rootEditor.setEditable(true);
            rootEditor.setNote( new Note( (String) selectedEntry.getMember( "Root" ) ));
            formulaTF.setEditable(true);
            formulaTF.setText((String) selectedEntry.getMember( "Formula"));
            notesTF.setEditable(false);
            notesTF.setText((String) selectedEntry.getMember( "Notes"));
            locationsTF.setEditable(false);
            locationsTF.setText((String) selectedEntry.getMember( "Locations"));
            variationTF.setEditable(false);
            variationTF.setText((String) selectedEntry.getMember( "Variation" ));
            scoreTF.setEditable(false);
            scoreTF.setText((String) selectedEntry.getMember( "Score" ));
            String scoreString = (String) selectedEntry.getMember( "Score" );
            // "Scores sum=22, fret bounds[0,15]=0, fret span=7, skip strings=5, same string=10"
            int [] scores = scanScore( scoreString );
            visualizer.setColumns( scores );
            detailsImagePanel.setBackground(Color.WHITE);
            detailsImagePanel.setImage( getDetailsImage(selectedEntry));
            largeImagePanel.setBackground(Color.WHITE);
            largeImagePanel.setImage( getLargeImage(selectedEntry));
            commentsTP.setEditable(true);
            commentsTP.setText((String) selectedEntry.getMember( "Comments" ));
            displayEditor.setEditable(true);

            selectedEntry.addPropertyChangeListener(selectedEntryChangeListener);            
        }
        
        // textfields are now in sync with selection, any changes from the UI
        programmaticChange = false;
    }

    public static int [] scanScore( String scoreString ) {
        // "Scores sum=22, fret bounds[0,15]=0, fret span=7, skip strings=5, same string=10"
    	if (( null == scoreString ) || ( scoreString.length() < 1 )) return new int [] { 0 };
        StringTokenizer st = new StringTokenizer( scoreString, "=," );
        st.nextToken();
        int sum = Integer.parseInt( st.nextToken() );
        // String sum = scanner.next();
        st.nextToken();
        st.nextToken();
        int fret = Integer.parseInt( st.nextToken() );
        st.nextToken();
        int span = Integer.parseInt( st.nextToken() );
        st.nextToken();
        int skip = Integer.parseInt( st.nextToken() );
        st.nextToken();
        int same = Integer.parseInt( st.nextToken() );
        int [] scores  = new int [] { sum, fret, span, skip, same }; 
        return scores;
    }

//    public final class ImagePanelPropertyChangeHandler implements PropertyChangeListener {
//        public void propertyChange(PropertyChangeEvent e) {
//            if (e.getPropertyName() == "imagePath") {
//                imageChanged();
//            }
//        }
//    }
//    
    private final class FilterDocumentHandler implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            filterChanged();
        }

        public void removeUpdate(DocumentEvent e) {
            filterChanged();
        }

        public void changedUpdate(DocumentEvent e) {
        }
    }
        
    public final static class FilteredEntryListController extends ListController<ExtendedDisplayEntry> {

    	protected boolean includeEntry(ExtendedDisplayEntry entry, String filter) {
            String formula = (String) entry.getMember( "Formula" );
            if (formula != null && formula.toLowerCase().contains(filter)) {
                return true;
            }
            return false;
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