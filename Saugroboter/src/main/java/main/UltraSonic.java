package main;

public class UltraSonic {
	
	private float min_range;
	
	public UltraSonic(float min_range) {
		this.min_range = min_range;
	}

	public boolean checkForObstacles() {
		float range = 0;
		// TODO: Sensor auslesen und in range schreiben
		if(range<min_range)
			return true;
		else
			return false;
	}

}
