package de.binaris.example.implicit.tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.function.Executable;

import de.binaris.example.implicit.tests.SafeDistanceUnit.UnknownDistanceUnitException;
import de.binaris.example.implicit.tests.SafeDistanceUnit.WrongDistanceUnitFormatException;

class DistanceUnitDemoWorstCaseTest {

	@org.junit.jupiter.api.Test
	public void testShowFalseUnitConversionWorstCase() {
		DistanceUnit distanceUnit = new DistanceUnit("dm");

		// We assume that we stop at a value of 1 dm (10 cm) to avoid any injuries
		// Now we start to calculate with the wrong build obj.
		double iExpectToStopAtDm = 1;
		double readSensorValueInMm = 1;
		double expectedDistanceToHumanHandInDm = readSensorValueInMm / distanceUnit.getFactor();

		assertEquals(iExpectToStopAtDm, expectedDistanceToHumanHandInDm,
				"You will pass your expected Test at this point because you still think everything is ok.");

		double realFactorWhichShouldHaveBeenUsed = 100;
		double realDistanceStopedInDm = readSensorValueInMm / realFactorWhichShouldHaveBeenUsed;
		assertEquals(iExpectToStopAtDm, realDistanceStopedInDm,
				"In reallity you only left 1mm instead of 1 dm to the human hand.");
	}

	// JUnit 4 way. No Java8 Lambda needed.
	// Instead you only need to init the obj in this case.
	// @org.junit.Test(expected = WrongDistanceUnitFormatException.class)
	public void testShowProperHandledInitWhenNameHasWrongPattern() {
		new SafeDistanceUnit("decimeters");
	}

	// JUnit 4 way. No Java8 Lambda needed. Instead you only need to init the obj in
	// this case.
	// @org.junit.Test(expected=UnknownDistanceUnitException.class)
	public void testShowProperHandledInitWhenUnitIsUnknonwn() {
		// In this case format is valid but the unit is unknown
		new SafeDistanceUnit("dm");
	}

	/**
	 * With the new Java 8 way of exception testing you are now able to test several
	 * Exception cases in on test instead of an single test for every Exception You
	 * could also do stubbing before calling your sut in the executable if necessary
	 */
	@org.junit.jupiter.api.Test // new JUnit 5 annotation.
	public void testInitNotSuccessfulIfParamsNotProper() {
		// Wrong format for unit provided
		Executable testWronInputFormat = () -> new SafeDistanceUnit("decimeters");
		assertThrows(WrongDistanceUnitFormatException.class, testWronInputFormat);

		// In this case format is valid but the unit is unknown
		Executable testUnknownUnit = () -> new SafeDistanceUnit("dm");
		assertThrows(UnknownDistanceUnitException.class, testUnknownUnit);
	}

}
