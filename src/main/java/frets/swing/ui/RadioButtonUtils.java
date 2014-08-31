package frets.swing.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.event.ChangeListener;

/**
Definitive Guide to Swing for Java 2, Second Edition
By John Zukowski     
ISBN: 1-893115-78-X
Publisher: APress
URL: http://www.java2s.com/Code/Java/Swing-JFC/GroupActionRadioButton.htm
*/
public class RadioButtonUtils {
  private RadioButtonUtils() {
    // private constructor so you can't create instances
  }

  public static Enumeration<String> getSelectedElements(Container container) {
    Vector<String> selections = new Vector<String>();
    Component components[] = container.getComponents();
    for (int i = 0 ; i < components.length; i++) {
      if (components[i] instanceof AbstractButton) {
        AbstractButton button = (AbstractButton) components[i];
        if (button.isSelected()) {
          selections.addElement(button.getText());
        }
      }
    }
    return selections.elements();
  }

  // Since Java ButtonGroup is not available, the selection cannot be set to none.
  // Similarly, an illegal item will not change the selection to none.
	public static void setSelectedElement(Container container, String element) {
		if ((null == element) || (element.length() < 1))
			return;
		Component components[] = container.getComponents();
		for (int i = 0; i < components.length; i++) {
			if (components[i] instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) components[i];
				String buttonName = button.getText();
				// System.out.println( "Setting element=" + element + ", button=" + buttonName + ", setting=" + buttonName.equalsIgnoreCase(element) );
				button.setSelected(buttonName.equalsIgnoreCase(element));
			}
		}
	}

  public static Container createRadioButtonGrouping(String elements[]) {
    return createRadioButtonGrouping(elements, null, null, null, null, null);
  }

  public static Container createRadioButtonGrouping(String elements[],String title) {
    return createRadioButtonGrouping(elements, null, title, null, null, null);
  }

  public static Container createRadioButtonGrouping(String elements[],
      String title, ItemListener itemListener) {
	  return createRadioButtonGrouping(elements, null, title, null, itemListener, null);
  }

  public static Container createRadioButtonGrouping(String elements[],
    String title, ActionListener actionListener) {
    return createRadioButtonGrouping(elements, null, title, actionListener, null, null);
  }

  public static Container createRadioButtonGrouping(String elements[],
      String title, ActionListener actionListener,ItemListener itemListener) {
    return createRadioButtonGrouping(elements, null, title, actionListener,itemListener, null);
  }

  public static Container createRadioButtonGrouping(String elements[], boolean selected[],
      String title, ActionListener actionListener,ItemListener itemListener, ChangeListener changeListener) {
    JPanel panel = new JPanel(new GridLayout(0, 1));
    //   If title set, create titled border
    if (title != null) {
      Border border = BorderFactory.createTitledBorder(title);
      panel.setBorder(border);
    }

    //   Create group
    ButtonGroup group = new ButtonGroup();
    JRadioButton aRadioButton;
    //   For each String passed in:
    //   Create button, add to panel, and add to group
    for (int i = 0, n = elements.length; i < n; i++) {
      aRadioButton = new JRadioButton(elements[i]);
      if (( null != selected ) &&  selected[ i ] ) {
    	  aRadioButton.setSelected( selected[ i ]);
      }
      panel.add(aRadioButton);
      group.add(aRadioButton);
      if (actionListener != null) {
        aRadioButton.addActionListener(actionListener);
      }
      if (itemListener != null) {
        aRadioButton.addItemListener(itemListener);
      }
      if (changeListener != null) {
        aRadioButton.addChangeListener(changeListener);
      }
    }
    return panel;
  }

  public static <E extends Enum<E>> Container createEnumGroup( Class<? extends Enum<E>> enumClass, String title, ActionListener actionListener ) {
	  // System.out.println( "EnumObject object=" + enumObject);
	  try {
		  Method pclMethod = enumClass.getMethod( "values" );
		  Object returnObject = pclMethod.invoke(enumClass);
		  // System.out.println( "EnumObject object values=" + value );
		  @SuppressWarnings("unchecked")
		  E [] values = (E []) returnObject;
		  String [] names = new String[ values.length ];
		  int count = 0;
		  for ( E enumElement : values) {
			names[ count++ ] = titleCase( enumElement.toString() );
		  }
		  return createRadioButtonGrouping( names, title, actionListener );
	  } catch (Exception e) {
			throw new IllegalArgumentException( "Reflection invocation exception=" + e);
	  }	  
  }

  public static <E extends Enum<E>> String [] getEnumNames(E defaultValue) {
	  @SuppressWarnings("unchecked")
      Enum<E>[] enumValues= (Enum<E>[]) defaultValue.getClass().getEnumConstants();
      String [] enumNames = new String[ enumValues.length ];
      int count = 0;
      for (Enum<?> value: enumValues )
     	  enumNames[ count++ ] = value.toString();
      return enumNames;
  }
  
  public static <E extends Enum<E>> String [] getEnumNames( Class <? extends Enum<E>> enumClass) {
      Enum<E>[] enumValues= (Enum<E>[]) enumClass.getEnumConstants();
      String [] enumNames = new String[ enumValues.length ];
      int count = 0;
      for (Enum<?> value: enumValues )
     	  enumNames[ count++ ] = value.toString();
      return enumNames;
  }
  
  public static String titleCase( String word ) {
	  List<String> lowerWords = Arrays.asList(
			  new String[]{ "a", "the", "of", "for" });
	  if ( lowerWords.contains( word ))
		  return word;
	  else
		  return word.substring(0,1).toUpperCase() + word.substring(1).toLowerCase(); 	  
  }  

}