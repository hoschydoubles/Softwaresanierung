package test;

import robot.EasyRequestSampleProvider;
import robot.SaugbotEV3;

public class HaraldDerTerminator {
	
	
	public static void main(String[] args) {
		System.out.println("<Main>");

		int durchgaenge = 300;
		SaugbotEV3.createInstanceEV3();
		System.out.println(" Instance created.");
		EasyRequestSampleProvider sm = SaugbotEV3.createSensorModeForInfraredSensor();
		System.out.println(" SampleProvider created.");
		System.out.println(" Distanz lesen:");
		for(int i = 0 ; i < durchgaenge ; i++) {
			float gesehen = sm.getSample();
			System.out.println("  "+i+": "+gesehen);
		}
		sm.close();
		System.out.println(" SampleProvider closed.");
		SaugbotEV3.shutDown();
		System.out.println(" Down shut.");
		
		System.out.println("</Main>");
	}

}
