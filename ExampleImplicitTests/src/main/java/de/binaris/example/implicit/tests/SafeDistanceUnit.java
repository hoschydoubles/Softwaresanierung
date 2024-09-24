package de.binaris.example.implicit.tests;

public class SafeDistanceUnit {

	private String name;
	private double factor;

	public SafeDistanceUnit(String name) {

		doDistanceUnitNameCheck(name);
		
		SafeDistanceUnit unit = null;
		for (int i = 0; i < KNOWN_DISTANCES.length; i++) {
			if (KNOWN_DISTANCES[i].getName().equals(name)) {
				unit = KNOWN_DISTANCES[i];
			}
		}
		if (unit != null) {
			this.name = unit.getName();
			this.factor = unit.getFactor();
		} else {
			throw new UnknownDistanceUnitException("Could not find valid DistanceUnit with name: "+name);
		}
	}

	private void doDistanceUnitNameCheck(String name) {
		if(!name.matches("\\w{2}")) {
			throw new WrongDistanceUnitFormatException("Wrong DistanceUnit name passed. Only values of type \\w[2} are allowed. You passed: "+name);
		}
	}

	private SafeDistanceUnit(String name, double factor) {
		this.name = name;
		this.factor = factor;
	}

	private static final SafeDistanceUnit MM = new SafeDistanceUnit("mm", 1);
	private static final SafeDistanceUnit CM = new SafeDistanceUnit("cm", 10);
	private static final SafeDistanceUnit[] KNOWN_DISTANCES = { MM, CM };

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

	
	public class WrongDistanceUnitFormatException extends RuntimeException {
		public WrongDistanceUnitFormatException(String message) {
			super(message);
		}
	}
	
	public class UnknownDistanceUnitException extends RuntimeException {
		public UnknownDistanceUnitException(String message) {
			super(message);
		}
	}
	
}
