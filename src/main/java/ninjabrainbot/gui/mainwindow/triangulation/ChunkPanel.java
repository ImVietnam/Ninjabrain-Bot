package ninjabrainbot.gui.mainwindow.triangulation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Locale;

import javax.swing.border.MatteBorder;

import ninjabrainbot.event.IDisposable;
import ninjabrainbot.event.Subscription;
import ninjabrainbot.gui.components.labels.ColorMapLabel;
import ninjabrainbot.gui.components.labels.ColoredLabel;
import ninjabrainbot.gui.components.labels.ILabel;
import ninjabrainbot.gui.components.labels.ThemedLabel;
import ninjabrainbot.gui.components.panels.ThemedPanel;
import ninjabrainbot.gui.style.SizePreference;
import ninjabrainbot.gui.style.StyleManager;
import ninjabrainbot.gui.style.theme.ColumnLayout;
import ninjabrainbot.gui.style.theme.WrappedColor;
import ninjabrainbot.io.preferences.MultipleChoicePreference;
import ninjabrainbot.io.preferences.NinjabrainBotPreferences;
import ninjabrainbot.io.preferences.enums.StrongholdDisplayType;
import ninjabrainbot.model.datastate.stronghold.Chunk;
import ninjabrainbot.model.datastate.stronghold.ChunkPrediction;

/**
 * JComponent for showing a Throw.
 */
public class ChunkPanel extends ThemedPanel implements IDisposable {

	private final MultipleChoicePreference<StrongholdDisplayType> strongholdDisplayType;
	private final NinjabrainBotPreferences preferences;

	private ChunkPrediction currentPrediction;

	final ThemedLabel location;
	final ColoredLabel certainty;
	final ThemedLabel distance;
	final ThemedLabel nether;
	final ColorMapLabel angle;
	private final ILabel[] labels;

	final StyleManager styleManager;
	double lastColor;

	private Subscription chunkPredictionSubscription;
	private final Subscription strongholdDisplayTypeChangedSubscription;
	private final Subscription colorCodeNegativeCoordsSubscription;

	private final WrappedColor borderCol;

	public ChunkPanel(StyleManager styleManager, NinjabrainBotPreferences preferences) {
		this(styleManager, preferences, null);
	}

	public ChunkPanel(StyleManager styleManager, NinjabrainBotPreferences preferences, ChunkPrediction p) {
		super(styleManager);
		this.styleManager = styleManager;
		this.preferences = preferences;
		strongholdDisplayType = preferences.strongholdDisplayType;
		setOpaque(true);
		location = new ThemedLabel(styleManager, true);
		certainty = new ColoredLabel(styleManager, true);
		distance = new ThemedLabel(styleManager, true);
		nether = new ThemedLabel(styleManager, true);
		angle = new ColorMapLabel(styleManager, true, true);
		labels = new ILabel[] { location, certainty, distance, nether, angle };
		ColumnLayout layout = new ColumnLayout(0);
		layout.setRelativeWidth(location, 2f);
		layout.setRelativeWidth(nether, 1.8f);
		layout.setRelativeWidth(angle, 2.5f);
		setLayout(layout);
		add(location);
		add(certainty);
		add(distance);
		add(nether);
		add(angle);
		setPrediction(p);
		setAngleUpdatesEnabled(preferences.showAngleUpdates.get());
		strongholdDisplayTypeChangedSubscription = preferences.strongholdDisplayType.whenModified().subscribeEDT(__ -> setPrediction(currentPrediction));
		colorCodeNegativeCoordsSubscription = preferences.colorCodeNegativeCoords.whenModified().subscribeEDT(__ -> setPrediction(currentPrediction));

		borderCol = styleManager.currentTheme.COLOR_DIVIDER;
		setBackgroundColor(styleManager.currentTheme.COLOR_SLIGHTLY_WEAK);
		setForegroundColor(styleManager.currentTheme.TEXT_COLOR_SLIGHTLY_WEAK);

		location.setForegroundColor(styleManager.currentTheme.TEXT_COLOR_SLIGHTLY_WEAK);
		certainty.setForegroundColor(styleManager.currentTheme.TEXT_COLOR_SLIGHTLY_WEAK);
		distance.setForegroundColor(styleManager.currentTheme.TEXT_COLOR_SLIGHTLY_WEAK);
		nether.setForegroundColor(styleManager.currentTheme.TEXT_COLOR_SLIGHTLY_WEAK);
		angle.setForegroundColor(styleManager.currentTheme.TEXT_COLOR_SLIGHTLY_WEAK);
	}

	public void setAngleUpdatesEnabled(boolean b) {
		angle.setVisible(b);
	}

	public void setPrediction(ChunkPrediction chunkPrediction) {
		currentPrediction = chunkPrediction;
		if (chunkPredictionSubscription != null) {
			chunkPredictionSubscription.dispose();
			chunkPredictionSubscription = null;
		}
		if (chunkPrediction == null) {
			for (ILabel l : labels) {
				if (l != null) {
					l.setText("");
					if (l instanceof ColorMapLabel) {
						((ColorMapLabel) l).setColoredText("", 0);
					}
				}
			}
		} else {
			setText(chunkPrediction);
			chunkPredictionSubscription = chunkPrediction.whenRelativePlayerPositionChanged().subscribeEDT(__ -> setText(chunkPrediction));
		}
	}

	public String getLocationText() {
		return location.getText();
	}

	public String getNetherText() {
		return nether.getText();
	}

	@Override
	public void setFont(Font font) {
		super.setFont(font);
		if (labels != null) {
			for (ILabel l : labels) {
				l.setFont(font);
			}
		}
	}

	@Override
	public void setForeground(Color fg) {
		super.setForeground(fg);
		if (labels != null) {
			for (ILabel l : labels) {
				if (l != null)
					l.setForeground(fg);
			}
		}
	}

	@Override
	public void updateColors() {
		setBorder(new MatteBorder(0, 0, 1, 0, borderCol.color()));
		super.updateColors();
		angle.updateColor();
		certainty.updateColors();
		setPrediction(currentPrediction);
	}

	@Override
	public void updateSize(StyleManager styleManager) {
		super.updateSize(styleManager);
		setPreferredSize(new Dimension(styleManager.size.WIDTH, styleManager.size.TEXT_SIZE_MEDIUM + styleManager.size.PADDING_THIN * 2));
	}

	@Override
	public int getTextSize(SizePreference p) {
		return p.TEXT_SIZE_MEDIUM;
	}

	@Override
	public void dispose() {
		if (chunkPredictionSubscription != null)
			chunkPredictionSubscription.dispose();
		strongholdDisplayTypeChangedSubscription.dispose();
		colorCodeNegativeCoordsSubscription.dispose();
	}

	private void setText(ChunkPrediction chunkPrediction) {
		location.setText(formatStrongholdCoords(chunkPrediction.chunk, strongholdDisplayType.get()));
		certainty.setText(chunkPrediction.formatCertainty(), (float) chunkPrediction.chunk.weight);
		distance.setText(chunkPrediction.formatDistanceInPlayersDimension());
		nether.setText(getFormattedCoords(chunkPrediction.xInNetherForDisplay(), chunkPrediction.zInNetherForDisplay()));
		angle.setText(chunkPrediction.formatTravelAngle(false));
		angle.setColoredText(chunkPrediction.formatTravelAngleDiff(), chunkPrediction.getTravelAngleDiffColor());
		lastColor = chunkPrediction.chunk.weight;
	}

	private String formatStrongholdCoords(Chunk chunk, StrongholdDisplayType strongholdDisplayType) {
		switch (strongholdDisplayType) {
			case FOURFOUR:
				return getFormattedCoords(chunk.fourFourX(), chunk.fourFourZ());
			case EIGHTEIGHT:
				return getFormattedCoords(chunk.eightEightX(), chunk.eightEightZ());
			case CHUNK:
				return getFormattedCoords(chunk.x, chunk.z);
			default:
				break;
		}
		return getFormattedCoords(chunk.x, chunk.z);
	}

	private String getFormattedCoords(int x, int z) {
		if (preferences.colorCodeNegativeCoords.get()) {
			String xColor = x < 0 ? styleManager.currentTheme.COLOR_NEGATIVE.hex() : styleManager.currentTheme.TEXT_COLOR_SLIGHTLY_WEAK.hex();
			String zColor = z < 0 ? styleManager.currentTheme.COLOR_NEGATIVE.hex() : styleManager.currentTheme.TEXT_COLOR_SLIGHTLY_WEAK.hex();
			return String.format(Locale.US, "<html>(<font color='%s'>%d</font>, <font color='%s'>%d</font>)</html>", xColor, x, zColor, z);
		}
		return String.format(Locale.US, "(%d, %d)", x, z);
	}
}
