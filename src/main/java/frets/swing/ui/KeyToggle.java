package frets.swing.ui;

import java.awt.Color;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import frets.main.Note;

import javax.swing.BorderFactory;
import javax.swing.JToggleButton;

/** A component that detects hits and has a piano key shape.
 * http://stackoverflow.com/questions/16479936/use-geometric-shapes-as-components
 * 
 * Same as KeyComponent, but implemented with JToggleButton.
 * 
 * @author <a href="mailto:dan@danbecker.info">Dan Becker</a>
 */
@SuppressWarnings("serial")
public class KeyToggle extends JToggleButton {
	/** L cutout of white key. Positioning of black key */
	public static final float OFFSET_13 = 1f/3f;
	public static final float OFFSET_23 = 2f/3f;
	public static final float OFFSET_12 = 1f/2f;
	
    public Note note = null;
    public Shape shape = null;
	
    public KeyToggle() {}
    
	public KeyToggle( final Note note ) {
		this.note = note;
		this.setName( note.getName());
		// System.out.println( "Key note=" + this.note.getName());
		
		// Set sizes
		String name = note.getName();
		switch ( name ) {
			case "C": case "D": case "E": case "F": case "G": case "A": case "B":   
				this.setForeground( Color.WHITE );
		        this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			break;
			default: // black
				this.setForeground( Color.BLACK );
		        this.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		}

		
//        addComponentListener(new ComponentAdapter() {
//            // It is best practice to set the window's shape in
//            // the componentResized method.  Then, if the window
//            // changes size, the shape will be correctly recalculated.
//            @Override
//            public void componentResized(ComponentEvent e) {
//        		// System.out.println( "Key resized=" + e.getComponent() );
//            	
//            	KeyToggle component = ((KeyToggle) e.getComponent());
//        		Dimension s = component.getSize();
//        		String name = component.note.getName();
//        		 
//        		if (( null == s ) || ( s.width < 1 ) || ( s.height < 1 )) {
//        			return;
//        		}
//        		// final int WH_WIDTH = s.width / 7;
//        		// final int WH_HEIGHT = s.height;
//        		final int BL_HEIGHT = 6 * s.height / 10; // 2/3 height
//        		final int BL_WIDTH = 2 * s.width / 21; // 2/3 white width
//
//        		Polygon poly = null;
//    			// Set positions
//    			switch ( name ) {
//    				case "C": 
//    				case "F": 
//    				   int cut = (int)( s.width - OFFSET_23 * BL_WIDTH); 
//    				   poly = new Polygon( new int [] {  0, cut, cut,       s.width,   s.width,  0 }, 
//    						   			   new int [] {  0, 0,   BL_HEIGHT, BL_HEIGHT, s.height, s.height }, 
//    						   			   6 ); 
//    				break;
//    				case "E": 
//    				case "B": 
//    				   cut = (int) (OFFSET_23 * BL_WIDTH); 
//    				   poly = new Polygon( new int [] {  cut, s.width, s.width,  0,        0,         cut }, 
//    						   			   new int [] {  0,   0,       s.height, s.height, BL_HEIGHT, BL_HEIGHT  }, 
//    						   			   6 ); 
//    				break;
//    				case "D": 
//     				   cut = (int) (OFFSET_13 * BL_WIDTH); 
//     				   poly = new Polygon( new int [] {  cut,  s.width - cut, s.width - cut,  s.width,   s.width,  0,        0,         cut  }, 
//     						   			   new int [] {  0,    0, 			  BL_HEIGHT,      BL_HEIGHT, s.height, s.height, BL_HEIGHT, BL_HEIGHT }, 
//     						   			   8 ); 
//    				break;
//    				case "G": 
//      				   cut = (int) (OFFSET_13 * BL_WIDTH);
//      				   int cut2 = (int) (OFFSET_12 * BL_WIDTH );
//      				   poly = new Polygon( new int [] {  cut,  s.width - cut2, s.width - cut2,  s.width,   s.width,  0,        0,         cut  }, 
//      						   			   new int [] {  0,    0, 			   BL_HEIGHT,       BL_HEIGHT, s.height, s.height, BL_HEIGHT, BL_HEIGHT }, 
//      						   			   8 ); 
//     				break;
//    				case "A": 
//       				   cut = (int) (OFFSET_12 * BL_WIDTH);
//       				   cut2 = (int) (OFFSET_13 * BL_WIDTH );
//       				   poly = new Polygon( new int [] {  cut,  s.width - cut2, s.width - cut2,  s.width,   s.width,  0,        0,         cut  }, 
//       						   			   new int [] {  0,    0, 			   BL_HEIGHT,       BL_HEIGHT, s.height, s.height, BL_HEIGHT, BL_HEIGHT }, 
//       						   			   8 ); 
//      				break;
//      				default: // Black keys
//        				   poly = new Polygon( new int [] {  0, s.width, s.width,        0  }, 
//						   			   		   new int [] {  0,       0, s.height, s.height }, 
//						   			   4 ); 
//    			}
//    			component.shape = poly;
//    			revalidate();
//    			repaint();
//            }
//        });

        addMouseListener( new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {}			
			@Override
			public void mousePressed(MouseEvent e) {}			
			@Override
			public void mouseExited(MouseEvent e) {}			
			@Override
			public void mouseEntered(MouseEvent e) {}			
			@Override
			public void mouseClicked(MouseEvent e) {
                System.out.println("The key " + note.getName() + " was clicked at " + e.getX() + "," + e.getY());
			}
		});

	}
	
//	@Override
//	protected void paintComponent(Graphics g) {
//		super.paintComponent(g);
//
//		if ( null != shape ) {
//			Rectangle bounds = shape.getBounds();
//			Insets insets = getInsets();
//	
//			// Do all translations at once
//			// Graphics2D is required for antialiasing and painting Shapes
//			Graphics2D g2d = (Graphics2D) g.create();	
//			g2d.translate(insets.left - bounds.x, insets.top - bounds.y);
//			g2d.fill(shape);	
//			g2d.dispose();
//		}
//	}
//
//	/**
//	 * Determine if the point is in the bounds of the Shape
//	 *
//	 * {@inheritDoc}
//	 */
//	@Override
//	public boolean contains(int x, int y) {
//		if ( null == shape )
//			return false;
//		
//		Rectangle bounds = shape.getBounds();
//		Insets insets = getInsets();
//
//		// Check to see if the Shape contains the point. Take into account
//		// the Shape X/Y coordinates, Border insets and Shape translation.
//
//		int translateX = x + bounds.x - insets.left;
//		int translateY = y + bounds.y - insets.top;
//
//		return shape.contains(translateX, translateY);
//	}
	
	@Override
	public String toString() {
		String name = "null";
		if ( this.note != null) name = this.note.getName();
		return "KeyComponent name=" + name + ", size=" + this.getSize().toString();
	}
}