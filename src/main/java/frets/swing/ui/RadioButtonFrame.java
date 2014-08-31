package frets.swing.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Definitive Guide to Swing for Java 2, Second Edition 
 * By John Zukowski 
 * ISBN: 1-893115-78-X 
 * Publisher: APress 
 * URL: http://www.java2s.com/Code/Java/Swing-JFC/GroupActionRadioButton.htm
 */
public class RadioButtonFrame {
	private static final String sliceOptions[] = 
		{ "4 slices", "8 slices", "12 slices", "16 slices" };

	private static final String crustOptions[] = 
		{ "Sicilian", "Thin Crust",	"Thick Crust", "Stuffed Crust" };

	public enum Topping {
		Mushroom("M"), Pepperoni("P"), Jalapenos("J");
		final String abbreviation;
		Topping(String abbreviation) {
			this.abbreviation = abbreviation;
		}
	};
	
	public static void main(String args[]) {

		String title = (args.length == 0 ? "Grouping Example" : args[0]);
		JFrame frame = new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Topping Parts
		ActionListener toppingsActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				System.out.println( "Action event=" + actionEvent);
				AbstractButton aButton = (AbstractButton) actionEvent.getSource();
				System.out.println("Selected: " + aButton.getText());
			}
		};

		final Container toppingsContainer = 
			RadioButtonUtils.createEnumGroup( Topping.class, "Toppings", toppingsActionListener );		

		// Slice Parts
		ActionListener sliceActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton aButton = (AbstractButton) actionEvent.getSource();
				System.out.println("Selected: " + aButton.getText());
			}
		};
		final Container sliceContainer = 
			RadioButtonUtils.createRadioButtonGrouping(	sliceOptions, "Slice Count", sliceActionListener);

		// Crust Parts
		ActionListener crustActionListener = new ActionListener() {
			String lastSelected;

			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton aButton = (AbstractButton) actionEvent
						.getSource();
				String label = aButton.getText();
				String msgStart;
				if (label.equals(lastSelected)) {
					msgStart = "Reselected: ";
				} else {
					msgStart = "Selected: ";
				}
				lastSelected = label;
				System.out.println(msgStart + label);
			}
		};
		
		ItemListener itemListener = new ItemListener() {
			String lastSelected;

			public void itemStateChanged(ItemEvent itemEvent) {
				AbstractButton aButton = (AbstractButton) itemEvent.getSource();
				int state = itemEvent.getStateChange();
				String label = aButton.getText();
				String msgStart;
				if (state == ItemEvent.SELECTED) {
					if (label.equals(lastSelected)) {
						msgStart = "Reselected -> ";
					} else {
						msgStart = "Selected -> ";
					}
					lastSelected = label;
				} else {
					msgStart = "Deselected -> ";
				}
				System.out.println(msgStart + label);
			}
		};
		
		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changEvent) {
				AbstractButton aButton = (AbstractButton) changEvent.getSource();
				ButtonModel aModel = aButton.getModel();
				boolean armed = aModel.isArmed();
				boolean pressed = aModel.isPressed();
				boolean selected = aModel.isSelected();
				System.out.println("Changed: armed/pressed/selected=" + armed
						+ "/" + pressed + "/" + selected);
			}
		};
		
		final Container crustContainer = 
			RadioButtonUtils.createRadioButtonGrouping(crustOptions, null, "Crust Type",
				crustActionListener, itemListener, changeListener);

		// Button Parts
		ActionListener buttonActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				Enumeration<String> selected = RadioButtonUtils.getSelectedElements(crustContainer);
				while (selected.hasMoreElements()) {
					System.out.println("Selected -> " + selected.nextElement());
				}
			}
		};
		
		JButton button = new JButton("Order Pizza");
		button.addActionListener(buttonActionListener);

		Container contentPane = frame.getContentPane();
		contentPane.add(toppingsContainer, BorderLayout.WEST);
		contentPane.add(sliceContainer, BorderLayout.CENTER);
		contentPane.add(crustContainer, BorderLayout.EAST);
		contentPane.add(button, BorderLayout.SOUTH);
		frame.setSize(400, 200);
		frame.setVisible(true);
	}
}
