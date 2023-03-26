package ninjabrainbot.data.information;

import java.util.ArrayList;
import java.util.List;

import ninjabrainbot.data.IDataState;
import ninjabrainbot.data.calculator.ICalculatorResult;
import ninjabrainbot.data.common.IPosition;
import ninjabrainbot.data.common.Position;
import ninjabrainbot.data.endereye.IThrow;
import ninjabrainbot.data.endereye.IThrowSet;
import ninjabrainbot.data.stronghold.Chunk;
import ninjabrainbot.io.preferences.NinjabrainBotPreferences;
import ninjabrainbot.util.Coords;

public class NextThrowDirectionInformationProvider extends InformationMessageProvider {

	public NextThrowDirectionInformationProvider(IDataState dataState, NinjabrainBotPreferences preferences) {
		updateInformationMessage(dataState);
		dataState.calculatorResult().subscribeEDT(__ -> updateInformationMessage(dataState));
		preferences.sigma.whenModified().subscribeEDT(__ -> updateInformationMessage(dataState));
		preferences.sigmaAlt.whenModified().subscribeEDT(__ -> updateInformationMessage(dataState));
	}

	private void updateInformationMessage(IDataState dataState) {
		IThrowSet throwSet = dataState.getThrowSet();
		ICalculatorResult calculatorResult = dataState.calculatorResult().get();
		InformationMessage informationMessageToShow = shouldShowInfoMessage(calculatorResult) ? createInformationMessage(calculatorResult, throwSet.size() != 0 ? throwSet.getLast() : null) : null;
		setInformationMessage(informationMessageToShow);
	}

	private boolean shouldShowInfoMessage(ICalculatorResult calculatorResult) {
		if (calculatorResult == null || !calculatorResult.success())
			return false;
		return calculatorResult.getBestPrediction().chunk.weight < 0.95;
	}

	private InformationMessage createInformationMessage(ICalculatorResult calculatorResult, IThrow lastThrow) {
		List<Chunk> predictions = new ArrayList<Chunk>();
		for (Chunk predictedChunk : calculatorResult.getTopChunks()) {
			if (predictedChunk.weight < 0.01)
				break;
			predictions.add(predictedChunk);
		}
		double phiRight = (lastThrow.alpha() + 90.0) * Math.PI / 180.0;
		double phiLeft = (lastThrow.alpha() - 90.0) * Math.PI / 180.0;
		int rightDistance = (int) Math.ceil(binarySearchSidewaysDistanceFor99PercentLowestPossibleCertainty(predictions, lastThrow, phiRight));
		int leftDistance = (int) Math.ceil(binarySearchSidewaysDistanceFor99PercentLowestPossibleCertainty(predictions, lastThrow, phiLeft));
		return new InformationMessage(InformationType.Info, "Go left " + leftDistance + " blocks, or right " + rightDistance + " blocks, for guaranteed 99% certainty.");
	}

	private double binarySearchSidewaysDistanceFor99PercentLowestPossibleCertainty(List<Chunk> predictions, IThrow lastThrow, double phiSideways) {
		double lowestPossibleCertainty = 0;
		double sidewaysDistance = 0;
		double sidewaysDistanceIncrement = 5.0;
		boolean binarySearching = false;
		while (sidewaysDistanceIncrement > 0.1) {
			sidewaysDistance += sidewaysDistanceIncrement * (lowestPossibleCertainty < 0.99 ? 1.0 : -1.0);
			double newX = lastThrow.xInOverworld() + Coords.getX(sidewaysDistance, phiSideways);
			double newZ = lastThrow.zInOverworld() + Coords.getZ(sidewaysDistance, phiSideways);
			lowestPossibleCertainty = getLowestPossibleCertainty(predictions, new Position(newX, newZ), lastThrow.getStd());
			if (lowestPossibleCertainty > 0.99)
				binarySearching = true;
			if (binarySearching)
				sidewaysDistanceIncrement *= 0.5;
		}
		return sidewaysDistance;
	}

	private double getLowestPossibleCertainty(List<Chunk> predictions, IPosition lastThrow, double standardDeviation) {
		double lowestPossibleCertainty = 1.0;
		for (Chunk assumedStrongholdChunk : predictions) {
			double phiToStronghold = Coords.getPhi(assumedStrongholdChunk.eighteightX() - lastThrow.xInOverworld(), assumedStrongholdChunk.eighteightZ() - lastThrow.zInOverworld());
			double originalCertainty = assumedStrongholdChunk.weight;
			double totalCertaintyAfterSecondThrow = 0;
			for (Chunk otherChunk : predictions) {
				if (otherChunk == assumedStrongholdChunk) {
					// 0.9 = approximate expected likelihood (if this is not included the
					// calculation assumes 0 measurement error for the chunk that is assumed to
					// contain the stronghold, but that is not realistic). A bit hacky but there's
					// no need to be super precise in this calculation.
					originalCertainty *= 0.9;
					totalCertaintyAfterSecondThrow += originalCertainty;
					continue;
				}
//				if (Math.abs(otherChunk.x - assumedStrongholdChunk.x) <= 1 && Math.abs(otherChunk.z - assumedStrongholdChunk.z) <= 1)
//					continue;
				double phiToPrediction = Coords.getPhi(otherChunk.eighteightX() - lastThrow.xInOverworld(), otherChunk.eighteightZ() - lastThrow.zInOverworld());
				double errorLikelihood = measurementErrorPdf(phiToPrediction - phiToStronghold, standardDeviation);
				totalCertaintyAfterSecondThrow += otherChunk.weight * errorLikelihood;
			}
			double newCertaintyForRealStronghold = originalCertainty / totalCertaintyAfterSecondThrow;
			if (newCertaintyForRealStronghold < lowestPossibleCertainty)
				lowestPossibleCertainty = newCertaintyForRealStronghold;
		}
		return lowestPossibleCertainty;
	}

	private double measurementErrorPdf(double errorInRadians, double sigma) {
		double error = errorInRadians * 180.0 / Math.PI;
		return Math.exp(-error * error / (2 * sigma * sigma));
	}

}