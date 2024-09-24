package external_libs;

import lejos.remote.ev3.RemoteRequestPilot;

public class EdgeReverse {

	public static Boolean run(RemoteRequestPilot pilot) {
		pilot.travel(-12);
		return true;
	}
	
	public static Boolean run(RemoteRequestPilot pilot, int distance) {
		pilot.travel(distance * -1);
		return true;
	}
	
}
