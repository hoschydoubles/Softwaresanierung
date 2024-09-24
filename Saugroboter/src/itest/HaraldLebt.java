package test;

import lejos.remote.ev3.RemoteRequestPilot;
import robot.SaugbotEV3;

public class HaraldLebt {

	public static void main(String[] args) {
		System.out.println("<Main>");
		
		SaugbotEV3.createInstanceEV3();
		System.out.println(" Instance created.");
		RemoteRequestPilot pilot = SaugbotEV3.createPilot();
		System.out.println(" Pilot created.");
		pilot.travel(1);
		System.out.println(" 1 traveled.");
		pilot.close();
		System.out.println(" Pilot closed.");
		SaugbotEV3.shutDown();
		System.out.println(" Down shut.");
		
		System.out.println("</Main>");
	}

}
