package ninjabrainbot.gui.components.preferences;

import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Objects;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;

import ninjabrainbot.Main;

public class CustomCheckbox extends JCheckBox {

	private static final ImageIcon icon = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/checkbox_icon.png")));
	private static final ImageIcon selected_icon = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/checkbox_selected_icon.png")));
	private static final ImageIcon pressed_icon = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/checkbox_pressed_icon.png")));
	private static final ImageIcon rollover_icon = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/checkbox_rollover_icon.png")));
	private static final ImageIcon selected_rollover_icon = new ImageIcon(Objects.requireNonNull(Main.class.getResource("/checkbox_selected_rollover_icon.png")));

	public CustomCheckbox() {
		this(false);
	}

	public CustomCheckbox(boolean ticked) {
		setSelected(ticked);
		setMargin(new Insets(-2, -2, -2, -2));
		setBorderPainted(false);
		setBorderPaintedFlat(false);
		setFocusPainted(false);
		setFocusable(false);
		setOpaque(false);
		setIcon(icon);
		setSelectedIcon(selected_icon);
		setPressedIcon(pressed_icon);
		setRolloverIcon(rollover_icon);
		setRolloverSelectedIcon(selected_rollover_icon);
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean ticked = ((JCheckBox) e.getSource()).isSelected();
				onChanged(ticked);
			}
		});
	}

	public void onChanged(boolean ticked) {
	}

}
