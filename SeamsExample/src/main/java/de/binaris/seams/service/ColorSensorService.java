package de.binaris.seams.service;

import lejos.remote.ev3.RemoteRequestEV3;
import lejos.remote.ev3.RemoteRequestSampleProvider;

public class ColorSensorService {

	private static ColorSensorService instance;

	private RemoteRequestSampleProvider remoteRequestSampleProvider;

	private ColorSensorService(){}

	public static synchronized ColorSensorService getInstance(RemoteRequestEV3 ev3) {
		if(instance == null) {
			instance = new ColorSensorService();
		}
		 instance.remoteRequestSampleProvider = (RemoteRequestSampleProvider) ev3.createSampleProvider("S1", "lejos.hardware.sensor" +
				 ".EV3ColorSensor", "ColorID");

		return instance;
	}

	public Float readColor() {
		float[] colorFarbe = { 0 };
		remoteRequestSampleProvider.fetchSample(colorFarbe, 0);
		System.out.println("sample  " + colorFarbe[0]);
		return Float.valueOf(colorFarbe[0]);
	}

	public void close() {
		try {
			remoteRequestSampleProvider.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
