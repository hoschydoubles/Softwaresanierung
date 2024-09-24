package robot;

import lejos.remote.ev3.RemoteRequestEV3;
import lejos.remote.ev3.RemoteRequestPilot;
import lejos.remote.ev3.RemoteRequestSampleProvider;
import lejos.robotics.RegulatedMotor;

import java.io.IOException;

public class SaugbotEV3 {

    private static RemoteRequestEV3 ev3;

    public static void createInstanceEV3() {

        String EV3BRICK_IP = "192.168.178.126";

        try {
            ev3 = new RemoteRequestEV3(EV3BRICK_IP);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static RegulatedMotor createMotor(char port) {
        return ev3.createRegulatedMotor(String.valueOf(port), 'M');
    }

    public static RemoteRequestPilot createPilot() {
        return (RemoteRequestPilot) ev3.createPilot(3.5, 17.5, "A", "D");
    }

    public static EasyRequestSampleProvider createSampleProviderForColorSensor() {
    	RemoteRequestSampleProvider remoteRequestSampleProvider = (RemoteRequestSampleProvider) ev3.createSampleProvider("S1", "lejos.hardware.sensor.EV3ColorSensor", "ColorID");
        return new EasyRequestSampleProvider(remoteRequestSampleProvider);
    }
    
    public static EasyRequestSampleProvider createSensorModeForInfraredSensor() {
    	RemoteRequestSampleProvider remoteRequestSampleProvider = (RemoteRequestSampleProvider) ev3.createSampleProvider("S3", "lejos.hardware.sensor.EV3IRSensor", "Distance");
        return new EasyRequestSampleProvider(remoteRequestSampleProvider);
    }

    public static void shutDown() {
        ev3.disConnect();
    }
}
