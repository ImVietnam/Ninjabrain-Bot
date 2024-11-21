package ninjabrainbot.gui.style.theme;

import java.awt.Color;
import java.util.ArrayList;

import ninjabrainbot.event.ISubscribable;
import ninjabrainbot.event.ObservableField;
import ninjabrainbot.gui.themeeditor.ThemeSerializer;
import ninjabrainbot.util.Assert;

public class CustomTheme extends Theme {

	private final ArrayList<ConfigurableColor> configurableColors;

	private final ObservableField<String> themeString = new ObservableField<String>();

	public CustomTheme() {
		this("temp", "", 0);
	}

	public CustomTheme(String name, String themeString, int uid) {
		super(name, uid);
		this.themeString.set(themeString);
		configurableColors = new ArrayList<ConfigurableColor>();

		COLOR_STRONGEST = createColor(Color.decode("#1C1C27"), "Title bar", "a");
		COLOR_STRONG = createColor(Color.decode("#2D3238"), "Header background", "b");
		COLOR_SLIGHTLY_WEAK = createColor(Color.decode("#2B2D44"), "Result background", "c");
		COLOR_NEUTRAL = createColor(Color.decode("#28293D"), "Throws background", "d");
		COLOR_DIVIDER = createColor(Color.decode("#212130"), "Dividers", "e");
		COLOR_DIVIDER_DARK = createColor(Color.decode("#1C1C27"), "Header Dividers", "f");
		TEXT_COLOR_SLIGHTLY_WEAK = createColor(Color.WHITE, "Text", "h");
		TEXT_COLOR_TITLE = createColor(Color.WHITE, "Title text", "n");
		TEXT_COLOR_NEUTRAL = createColor(Color.LIGHT_GRAY, "Throws text", "k");
		TEXT_COLOR_SLIGHTLY_STRONG = createColor(Color.decode("#E5E5E5"), "Divine text", "i");
		TEXT_COLOR_WEAK = createColor(Color.GRAY, "Version text", "j");
		TEXT_COLOR_HEADER = createColor(Color.decode("#E5E5E5"), "Header text", "o");
		COLOR_POSITIVE = createColor(Color.decode("#75CC6C"), "Subpixel +", "l");
		COLOR_NEGATIVE = createColor(Color.decode("#CC6E72"), "Subpixel -", "m");

		COLOR_GRADIENT_100 = createColor(Color.decode("#00CE29"), "Certainty 100%", "r");
		COLOR_GRADIENT_50 = createColor(Color.YELLOW, "Certainty 50%", "q");
		COLOR_GRADIENT_0 = createColor(Color.RED, "Certainty 0%", "p");

		COLOR_SLIGHTLY_STRONG = createColor(Color.decode("#31353A"), COLOR_STRONG);
		COLOR_SATURATED = createColor(Color.decode("#57EBA3"), COLOR_STRONG);

		COLOR_EXIT_BUTTON_HOVER = createColor(Color.decode("#F04747"));
	}

	@Override
	protected void loadTheme() {
		loaded = true;
		setFromTheme(ThemeSerializer.deserialize(themeString.get()));
	}

	public void setFromTheme(Theme theme) {
		setFromTheme(theme, false);
	}

	public void setFromTheme(Theme theme, boolean copyName) {
		if (theme == null)
			return;
		COLOR_STRONGEST.set(theme.COLOR_STRONGEST);
		COLOR_STRONG.set(theme.COLOR_STRONG);
		COLOR_SLIGHTLY_WEAK.set(theme.COLOR_SLIGHTLY_WEAK);
		COLOR_NEUTRAL.set(theme.COLOR_NEUTRAL);
		COLOR_DIVIDER.set(theme.COLOR_DIVIDER);
		COLOR_DIVIDER_DARK.set(theme.COLOR_DIVIDER_DARK);
		COLOR_EXIT_BUTTON_HOVER.set(theme.COLOR_EXIT_BUTTON_HOVER);
		TEXT_COLOR_SLIGHTLY_WEAK.set(theme.TEXT_COLOR_SLIGHTLY_WEAK);
		TEXT_COLOR_SLIGHTLY_STRONG.set(theme.TEXT_COLOR_SLIGHTLY_STRONG);
		TEXT_COLOR_WEAK.set(theme.TEXT_COLOR_WEAK);
		TEXT_COLOR_NEUTRAL.set(theme.TEXT_COLOR_NEUTRAL);
		TEXT_COLOR_HEADER.set(theme.TEXT_COLOR_HEADER);
		COLOR_POSITIVE.set(theme.COLOR_POSITIVE);
		COLOR_NEGATIVE.set(theme.COLOR_NEGATIVE);
		TEXT_COLOR_TITLE.set(theme.TEXT_COLOR_TITLE);

		COLOR_GRADIENT_100.set(theme.COLOR_GRADIENT_100);
		COLOR_GRADIENT_50.set(theme.COLOR_GRADIENT_50);
		COLOR_GRADIENT_0.set(theme.COLOR_GRADIENT_0);

		String newThemeString = ThemeSerializer.serialize(this);
		if (!themeString.get().contentEquals(newThemeString))
			themeString.set(newThemeString);

		if (copyName)
			name.set(theme.name.get());

		whenModified.notifySubscribers(this);
	}

	public ISubscribable<String> whenThemeStringChanged() {
		return themeString;
	}

	public String getThemeString() {
		return themeString.get();
	}

	public ArrayList<ConfigurableColor> getConfigurableColors() {
		return configurableColors;
	}

	public ConfigurableColor getConfigurableColor(String uid) {
		for (ConfigurableColor configurableColor : configurableColors) {
			if (uid.contentEquals(configurableColor.uid)) {
				return configurableColor;
			}
		}
		return null;
	}

	protected WrappedColor createColor(Color color, WrappedColor parent) {
		WrappedColor wc = super.createColor(color);
		int dr = color.getRed() - parent.color().getRed();
		int dg = color.getGreen() - parent.color().getGreen();
		int db = color.getBlue() - parent.color().getBlue();
		parent.whenColorChanged().subscribe(c -> wc.set(new Color(clamp(c.getRed() + dr), clamp(c.getGreen() + dg), clamp(c.getBlue() + db))));
		return wc;
	}

	private static int clamp(int i) {
		if (i < 0)
			return 0;
		if (i > 255)
			return 255;
		return i;
	}

	protected WrappedColor createColor(Color color, String name, String uid) {
		Assert.isTrue(uid.length() == 1);
		Assert.isTrue(configurableColors.stream().noneMatch(cc -> cc.uid == uid));
		WrappedColor wc = super.createColor(color);
		configurableColors.add(new ConfigurableColor(wc, name, uid));
		return wc;
	}

	public boolean isEquivalentTo(CustomTheme other) {
		if (other.configurableColors.size() != this.configurableColors.size())
			return false;
		if (!this.name.get().contentEquals(other.name.get()))
			return false;
		for (ConfigurableColor configurableColor : configurableColors) {
			if (!other.getConfigurableColor(configurableColor.uid).color.isEquivalentTo(configurableColor.color)) {
				return false;
			}
		}
		return true;
	}

	public void setName(String newName) {
		this.name.set(newName);
	}

}