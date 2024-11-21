package ninjabrainbot.model.information;

import ninjabrainbot.io.mcinstance.IActiveInstanceProvider;
import ninjabrainbot.io.mcinstance.MinecraftInstance;
import ninjabrainbot.io.preferences.NinjabrainBotPreferences;
import ninjabrainbot.util.I18n;

public class McVersionWarningProvider extends InformationMessageProvider {

	private final NinjabrainBotPreferences preferences;

	private MinecraftInstance currentMinecraftInstance;

	public McVersionWarningProvider(IActiveInstanceProvider activeInstanceListener, NinjabrainBotPreferences preferences) {
		this.preferences = preferences;
		raiseInformationMessageChanged();
		disposeHandler.add(activeInstanceListener.activeMinecraftInstance().subscribe(this::onActiveMinecraftInstanceChanged));
		disposeHandler.add(preferences.mcVersion.whenModified().subscribe(__ -> raiseInformationMessageChanged()));
	}

	private void onActiveMinecraftInstanceChanged(MinecraftInstance minecraftInstance) {
		currentMinecraftInstance = minecraftInstance;
		raiseInformationMessageChanged();
	}

	@Override
	protected boolean shouldShowInformationMessage() {
		return currentMinecraftInstance != null && currentMinecraftInstance.getMcVersion() != null && currentMinecraftInstance.getMcVersion() != preferences.mcVersion.get();
	}

	private InformationMessage warningMessage = null;

	@Override
	protected InformationMessage getInformationMessage() {
		if (warningMessage == null)
			warningMessage = new InformationMessage(InformationMessageSeverity.WARNING, "MC_VERSION", I18n.get("information.wrong_mc_version"));
		return warningMessage;
	}

}
