package test;

import lejos.remote.ev3.RemoteRequestPilot;
import lejos.robotics.Color;
import robot.EasyRequestSampleProvider;
import robot.SaugbotEV3;

public class HaraldStopptAnKante {
	
	private static String farbenname(int color) {
		String answer = "";
		switch (color) {
		case Color.WHITE:
			answer = "WHITE";
			break;
		default:
			answer = Integer.toString(color);
		}
		
		return answer;
	}
	
	public static void main(String[] args) {
		System.out.println("<Main>");

		SaugbotEV3.createInstanceEV3();
		System.out.println(" Instance created.");
		RemoteRequestPilot pilot = SaugbotEV3.createPilot();
		System.out.println(" Pilot created.");
		EasyRequestSampleProvider sample = SaugbotEV3.createSampleProviderForColorSensor();
		System.out.println(" SampleProvider created.");
		pilot.forward();
		System.out.println(" Pilot forwarded.");
		System.out.println(" Cycle start...");
		while(true) {
			float gesehen = sample.getSample();
			if(!farbenname((int)gesehen).equals("WHITE")) {
				pilot.stop();
				System.out.println(" Pilot stopped.");
				break;
			}
		}
		System.out.println(" ...end.");
		sample.close();
		System.out.println(" SampleProvider Closed.");
		pilot.close();
		System.out.println(" Pilot closed.");
		SaugbotEV3.shutDown();
		System.out.println(" Down shut.");
		
		System.out.println("</Main>");
	}

}
