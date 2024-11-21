package ninjabrainbot.simulations;

import java.util.Random;

import ninjabrainbot.event.ObservableField;
import ninjabrainbot.io.preferences.enums.McVersion;
import ninjabrainbot.model.datastate.calculator.Calculator;
import ninjabrainbot.model.datastate.calculator.ICalculatorResult;
import ninjabrainbot.model.datastate.common.DetachedDomainModel;
import ninjabrainbot.model.datastate.common.IOverworldPosition;
import ninjabrainbot.model.datastate.divine.DivineContext;
import ninjabrainbot.model.datastate.divine.IDivineContext;
import ninjabrainbot.model.datastate.endereye.IEnderEyeThrow;
import ninjabrainbot.model.datastate.stronghold.Chunk;
import ninjabrainbot.model.datastate.stronghold.Ring;
import ninjabrainbot.model.domainmodel.IListComponent;
import ninjabrainbot.model.domainmodel.ListComponent;
import ninjabrainbot.model.environmentstate.CalculatorSettings;
import ninjabrainbot.model.environmentstate.StandardDeviationSettings;
import ninjabrainbot.util.Logger;
import ninjabrainbot.util.TestEnderEyeThrow;

public class OneEyeAccuracySimulation {

	private static final int ITERATIONS = 1000;
	private static final double std = 0.005;

	private static Calculator calculator;
	private static IDivineContext divineContext;
	private static final Random random = new Random();
	private static final Ring ring = Ring.get(0);

	public static void main(String[] args) {
		Logger.enabled = false;
		calculator = new Calculator(new CalculatorSettings(true, McVersion.PRE_119), new StandardDeviationSettings(std, std, std, std));
		divineContext = new DivineContext(new DetachedDomainModel());

		for (int r = 0; r < 3000; r += 100) {
			int successes = 0;
			int total = 0;
			for (int j = 0; j < ITERATIONS; j++) {
				double phi = random.nextDouble() * Math.PI * 2.0;
				double x = r * Math.sin(phi);
				double z = r * Math.cos(phi);

				IEnderEyeThrow position = new TestEnderEyeThrow(x, z, 0, std);
				Chunk closestStronghold = getClosestStronghold(sampleThreeStrongholdsFirstRing(), position);

				double phiToStronghold = -closestStronghold.getAngleError(McVersion.PRE_119, position);
				double sampledError = (random.nextDouble() - random.nextDouble()) * 0.01;
				IEnderEyeThrow eyeThrow = new TestEnderEyeThrow(x, z, phiToStronghold + sampledError, std);

				ICalculatorResult calculatorResult = calculateOneEye(eyeThrow);
				total += 1;
				successes += calculatorResult.getBestPrediction().chunk.equals(closestStronghold) ? 1 : 0;
				calculatorResult.dispose();
			}
			System.out.println(r + ": " + (float) successes / total);
		}
	}

	private static ICalculatorResult calculateOneEye(IEnderEyeThrow eyeThrow) {
		IListComponent<IEnderEyeThrow> throwSet = new ListComponent<>("throw_list", new DetachedDomainModel(), 10);
		throwSet.add(eyeThrow);
		return calculator.triangulate(throwSet, new ObservableField<>(eyeThrow.getPlayerPosition()), divineContext);
	}

	private static Chunk[] sampleThreeStrongholdsFirstRing() {
		double phi = random.nextDouble() * Math.PI * 2.0;
		double r = ring.innerRadius + random.nextDouble() * (ring.outerRadius - ring.innerRadius);
		Chunk stronghold0 = new Chunk((int) (r * Math.sin(phi)), (int) (r * Math.cos(phi)));
		phi += Math.PI * 2.0 / 3.0;
		Chunk stronghold1 = new Chunk((int) (r * Math.sin(phi)), (int) (r * Math.cos(phi)));
		phi += Math.PI * 2.0 / 3.0;
		Chunk stronghold2 = new Chunk((int) (r * Math.sin(phi)), (int) (r * Math.cos(phi)));
		return new Chunk[] { stronghold0, stronghold1, stronghold2 };
	}

	private static Chunk getClosestStronghold(Chunk[] strongholds, IOverworldPosition position) {
		Chunk closestStronghold = null;
		double minDistance = Double.MAX_VALUE;
		for (Chunk stronghold : strongholds) {
			double distance = stronghold.getOverworldDistance(McVersion.PRE_119, position);
			if (distance < minDistance) {
				minDistance = distance;
				closestStronghold = stronghold;
			}
		}
		return closestStronghold;
	}

}
