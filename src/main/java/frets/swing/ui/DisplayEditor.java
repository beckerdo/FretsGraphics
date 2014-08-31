package frets.swing.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

import frets.main.Display;

/**
 * DisplayEditor is a component for editing and display
 * of <@link Display}. 
 */
public final class DisplayEditor extends JPanel {
	private static final long serialVersionUID = 1L;
	public static final String ORIENTATION_LABEL = "Orientation";
	public static final String HAND_LABEL = "Hand";
    

	// model data
    protected Display displayOpts;
	protected boolean editable = true;
	
	// UI data
	JTextField name;
	JTextField fileName;
	JTextField description;
	Container orientationGroup;
	Container handGroup;
	
	protected DisplayEditor() {
        GridBagLayout layout = new GridBagLayout();
        this.setLayout(layout);

    	createStringProperty( "Name", "", this, name = new JTextField(), layout, 0 );
    	createStringProperty( "Filename", "", this, fileName = new JTextField(), layout, 1 );
    	createStringProperty( "Description", "", this, description = new JTextField(), layout, 2 );

    	orientationGroup = createEnumProperty( ORIENTATION_LABEL, Display.Orientation.class, this, layout, 3, 0 );
    	handGroup = createEnumProperty( HAND_LABEL, Display.Hand.class, this, layout, 3, 1 );
    }

	protected DisplayEditor( Display displayOpts ) {
		this();
		setDisplayOpts( displayOpts );		
	}

    public void createStringProperty( String label, String value, JPanel container, JTextComponent textComponent, GridBagLayout layout, int row ) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0; // No resize,
        gbc.weighty = 0.0; // No resize,
        gbc.anchor = GridBagConstraints.EAST;
        JLabel jLabel = new JLabel( label );
        layout.setConstraints( jLabel, gbc );
        container.add( jLabel );
        
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0; // No resize,
        gbc.fill = GridBagConstraints.HORIZONTAL ;
        layout.setConstraints( textComponent, gbc );
        container.add( textComponent );    	
    }

    public final <E extends Enum<E>> Container createEnumProperty( String label, Class<? extends Enum<E>> enumClass, JPanel container, GridBagLayout layout, int row, int col ) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0; // No resize,
        gbc.fill = GridBagConstraints.HORIZONTAL;
        // gbc.gridwidth = GridBagConstraints.REMAINDER; 

        // ButtonListener
		ActionListener enumActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton aButton = (AbstractButton) actionEvent.getSource();
				String groupName = getPanelTitle( aButton.getParent() );
				System.out.println("Selected (src=" + groupName + "):"  + aButton.getText());				
			}
		};

		final Container enumContainer = RadioButtonUtils.createEnumGroup( enumClass, label, enumActionListener );		
        layout.setConstraints( enumContainer, gbc );
        container.add( enumContainer );
        return enumContainer;
    }

    /** Returns name of panel titled border. */
    public static String getPanelTitle( Component component ) {
    	String groupName = "";
		if ( component instanceof JPanel ) {
			JPanel jPanel = (JPanel) component;
			Border border = jPanel.getBorder();
			if (( null != border ) && ( border instanceof TitledBorder ) ) {
				groupName = ((TitledBorder)border).getTitle(); 
			}
		}
		return groupName;    	
    }
    
   	/** Opens a file at the given name, and reads all the properties into an object. */
   	public void readFromFile( String fileName ) throws IOException {
   		this.displayOpts = Display.read( fileName );
   		updateView();
    }
   	
   	public void updateView() {
   		
   	}
   	
    public final Display getDisplayOpts() {
		return displayOpts;
	}

	public void setDisplayOpts(Display displayOpts) {
		this.displayOpts = displayOpts;
		if ( null != displayOpts.getMetaName())
			name.setText( displayOpts.getMetaName() );
		else
			name.setText( "" );
		if ( null != displayOpts.getMetaLocation())
			fileName.setText( displayOpts.getMetaLocation() );
		else
			fileName.setText( "" );
		if ( null != displayOpts.getMetaDescription())
			description.setText( displayOpts.getMetaDescription() );
		else
			description.setText( "" );
		RadioButtonUtils.setSelectedElement( orientationGroup, displayOpts.orientation.toString() );
		RadioButtonUtils.setSelectedElement( handGroup, displayOpts.hand.toString() );
	}

    public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}


}