package ninjabrainbot.integrationtests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ninjabrainbot.model.datastate.calculator.ICalculatorResult;
import ninjabrainbot.model.datastate.common.DetailedPlayerPosition;
import ninjabrainbot.model.datastate.common.ResultType;
import ninjabrainbot.model.datastate.endereye.F3IData;
import ninjabrainbot.model.datastate.endereye.MCDimension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataStateIntegrationTests {

	@Test
	void dataComponentsOnlySendsOneEventOnReset() {
		// Arrange
		IntegrationTestBuilder testBuilder = new IntegrationTestBuilder().withProSettings().withAllAdvancementsSettings();

		testBuilder.inputF3I(new F3IData(4, 0, 0));
		testBuilder.inputDetailedPlayerPosition(new DetailedPlayerPosition(0, 80, 0, 12, -31, MCDimension.OVERWORLD));
		testBuilder.inputDetailedPlayerPosition(new DetailedPlayerPosition(0, 80, 1000, 12, -31, MCDimension.NETHER));

		List<ResultType> resultTypeEvents = new ArrayList<>();
		List<ICalculatorResult> calculatorResultEvents = new ArrayList<>();
		testBuilder.dataState.resultType().subscribe(resultTypeEvents::add);
		testBuilder.dataState.calculatorResult().subscribe(calculatorResultEvents::add);

		// Act
		testBuilder.resetCalculator();

		// Assert
		ArrayList<ICalculatorResult> expectedCalculatorResultEvents = new ArrayList<>();
		expectedCalculatorResultEvents.add(null);
		Assertions.assertIterableEquals(expectedCalculatorResultEvents, calculatorResultEvents, "Incorrect CalculatorResult events: " + calculatorResultEvents);
		Assertions.assertIterableEquals(Collections.singletonList(ResultType.NONE), resultTypeEvents, "Incorrect ResultType events: " + resultTypeEvents);
	}

}
