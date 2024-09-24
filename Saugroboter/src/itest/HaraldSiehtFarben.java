package test;

import lejos.robotics.Color;
import robot.EasyRequestSampleProvider;
import robot.SaugbotEV3;

public class HaraldSiehtFarben {
	
	private static String farbenname(int color) {
		String answer = "";
		switch (color) {
		case Color.BLACK:
			answer = "BLACK";
			break;
		case Color.GREEN:
			answer = "GREEN";
			break;
		case Color.NONE:
			answer = "NONE";
			break;
		case Color.WHITE:
			answer = "WHITE";
			break;
		case Color.YELLOW:
			answer = "YELLOW";
			break;
		default:
			answer = Integer.toString(color);
		}
		
		return answer;
	}
	
	public static void main(String[] args) {
		System.out.println("<Main>");

		int durchgaenge = 300;
		SaugbotEV3.createInstanceEV3();
		System.out.println(" Instance created.");
		EasyRequestSampleProvider sample = SaugbotEV3.createSampleProviderForColorSensor();
		System.out.println(" SampleProvider created.");
		System.out.println(" Farbe lesen:");
		for(int i = 0 ; i < durchgaenge ; i++) {
			float gesehen = sample.getSample();
			System.out.println("  "+i+": "+farbenname((int)gesehen));
		}
		sample.close();
		System.out.println(" SampleProvider Closed.");
		SaugbotEV3.shutDown();
		System.out.println(" Down shut.");
		
		System.out.println("</Main>");
	}

}
