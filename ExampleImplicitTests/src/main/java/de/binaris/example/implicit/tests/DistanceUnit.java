package de.binaris.example.implicit.tests;
/**
 * Exercise: implicit tests
 * Based on a legth of mm.
 * We assume the sensor will deliver the values in mm by default.
 */
public class DistanceUnit {
	
	private String name;
	private double factor;

	public DistanceUnit(String name) {
		DistanceUnit unit = null;
		for (int i = 0; i < KNOWN_DISTANCES.length; i++) {
			if (KNOWN_DISTANCES[i].getName().equals(name)) {
				unit = KNOWN_DISTANCES[i];
			}
		}
		if (unit != null) {
			this.name = unit.getName();
			this.factor = unit.getFactor();
		} else {
			this.name = null;
			this.factor = 1;
		}
	}

	private DistanceUnit(String name, double factor) {
		this.name = name;
		this.factor = factor;
	}

	private static final DistanceUnit MM = new DistanceUnit("mm", 1);
	private static final DistanceUnit CM = new DistanceUnit("cm", 10);
//	private static final DistanceUnit DM = new DistanceUnit("dm", 100);
	private static final DistanceUnit[] KNOWN_DISTANCES = { MM, CM };

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getFactor() {
		return factor;
	}

	public void setFactor(double factor) {
		this.factor = factor;
	}

}
