package ninjabrainbot.data.calculator.endereye;

public class ManualEnderEyeThrow extends EnderEyeThrow {

	public ManualEnderEyeThrow(double x, double z, double horizontalAngle, double verticalAngle, IStandardDeviationHandler standardDeviationHandler) {
		this(x, z, horizontalAngle, verticalAngle, standardDeviationHandler, 0);
	}

	private ManualEnderEyeThrow(double x, double z, double horizontalAngle, double verticalAngle, IStandardDeviationHandler standardDeviationHandler, double correction) {
		super(x, z, horizontalAngle, verticalAngle, standardDeviationHandler, correction);
	}

	@Override
	public IEnderEyeThrow withCorrection(double correction) {
		return new ManualEnderEyeThrow(x, z, horizontalAngleWithoutCorrection, verticalAngle, standardDeviationHandler, correction);
	}

	@Override
	public IEnderEyeThrow withToggledAltStd() {
		return this;
	}

	@Override
	protected double getStandardDeviation(IStandardDeviationHandler standardDeviationHandler) {
		return standardDeviationHandler.getManualStandardDeviation();
	}

	@Override
	public EnderEyeThrowType getType() {
		return EnderEyeThrowType.Manual;
	}

}