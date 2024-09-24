package test;

import lejos.remote.ev3.RemoteRequestPilot;
import robot.SaugbotEV3;

public class HaraldDreht {

	public static void main(String[] args) throws Exception {
		System.out.println("<Main>");
		
		SaugbotEV3.createInstanceEV3();
		System.out.println(" Instance created.");
		RemoteRequestPilot pilot = SaugbotEV3.createPilot();
		System.out.println(" Pilot created.");
		pilot.rotate(90);
		System.out.println(" 90 rotated.");
		Thread.sleep(1000);
		pilot.rotate(-90);
		System.out.println(" -90 rotated.");
		pilot.close();
		System.out.println(" Pilot closed.");
		SaugbotEV3.shutDown();
		System.out.println(" Down shut.");
		
		System.out.println("</Main>");
	}
}
