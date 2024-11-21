package ninjabrainbot.model.datastate.common;

import java.util.Locale;

import ninjabrainbot.event.IDisposable;
import ninjabrainbot.event.IObservable;
import ninjabrainbot.event.ISubscribable;
import ninjabrainbot.event.ObservableProperty;
import ninjabrainbot.event.Subscription;
import ninjabrainbot.util.I18n;

public class StructureInformation implements IOverworldPosition, IDisposable {

	private final IOverworldPosition overworldPosition;

	private int overworldDistance;
	private double travelAngle;
	private double travelAngleDifference;
	private boolean playerIsInNether;

	private Subscription playerPosSubscription;

	private final ObservableProperty<StructureInformation> whenRelativePlayerPositionChanged;

	public StructureInformation(IOverworldPosition overworldPosition, IObservable<IPlayerPosition> playerPosition) {
		this.overworldPosition = overworldPosition;
		whenRelativePlayerPositionChanged = new ObservableProperty<>();
		if (playerPosition != null) {
			updateWithPlayerPos(playerPosition.get(), false);
			playerPosSubscription = playerPosition.subscribe(pos -> updateWithPlayerPos(pos, true));
		}
	}

	private void updateWithPlayerPos(IPlayerPosition playerPos, boolean notify) {
		if (playerPos == null)
			return;

		overworldDistance = (int) this.distanceInOverworld(playerPos);
		playerIsInNether = playerPos.isInNether();
		double xDiff = overworldPosition.xInOverworld() - playerPos.xInOverworld();
		double zDiff = overworldPosition.zInOverworld() - playerPos.zInOverworld();
		double angleToStructure = -Math.atan2(xDiff, zDiff) * 180 / Math.PI;
		double angleDifference = (angleToStructure - playerPos.horizontalAngle()) % 360;
		if (angleDifference > 180)
			angleDifference -= 360;
		if (angleDifference < -180)
			angleDifference += 360;

		this.travelAngle = angleToStructure;
		this.travelAngleDifference = angleDifference;
		whenRelativePlayerPositionChanged.notifySubscribers(this);
	}

	public String formatDistanceInPlayersDimension() {
		return String.format(Locale.US, "%d", playerIsInNether ? getNetherDistance() : overworldDistance);
	}

	public String formatTravelAngle(boolean forBasic) {
		if (forBasic) {
			return String.format("%s: %.2f", I18n.get("current_angle"), travelAngle);
		}
		return String.format("%.2f", travelAngle);
	}

	public String formatTravelAngleDiff() {
		double absChange = Math.abs(travelAngleDifference);
		return String.format(" (%s %.1f)", travelAngleDifference > 0 ? "->" : "<-", absChange);
	}

	public float getTravelAngleDiffColor() {
		return (float) (1 - Math.abs(travelAngleDifference) / 180.0);
	}

	@Override
	public double xInOverworld() {
		return overworldPosition.xInOverworld();
	}

	@Override
	public double zInOverworld() {
		return overworldPosition.zInOverworld();
	}

	public double xInNether() {
		return xInOverworld() / 8.0;
	}

	public double zInNether() {
		return zInOverworld() / 8.0;
	}

	public int xInNetherForDisplay() {
		return (int) Math.floor(overworldPosition.xInOverworld() / 8.0);
	}

	public int zInNetherForDisplay() {
		return (int) Math.floor(overworldPosition.zInOverworld() / 8.0);
	}

	public int getOverworldDistance() {
		return overworldDistance;
	}

	public int getNetherDistance() {
		return overworldDistance / 8;
	}

	public double getTravelAngle() {
		return travelAngle;
	}

	public double getTravelAngleDiff() {
		return travelAngleDifference;
	}

	public ISubscribable<StructureInformation> whenRelativePlayerPositionChanged() {
		return whenRelativePlayerPositionChanged;
	}

	@Override
	public void dispose() {
		if (playerPosSubscription != null)
			playerPosSubscription.dispose();
	}

}
