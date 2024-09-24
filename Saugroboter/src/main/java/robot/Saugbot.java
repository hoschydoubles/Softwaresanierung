package robot;

import lejos.remote.ev3.RemoteRequestPilot;
import lejos.robotics.RegulatedMotor;

public class Saugbot {

    public void createInstance() {
    	SaugbotEV3.createInstanceEV3();
    }
    
    public RegulatedMotor createMotor(char port) {
        return SaugbotEV3.createMotor(port);
    }

    public RemoteRequestPilot createPilot() {
        return SaugbotEV3.createPilot();
    }

    public EasyRequestSampleProvider createSampleProviderForColorSensor() {
        return SaugbotEV3.createSampleProviderForColorSensor();
    }
    
    public EasyRequestSampleProvider createSensorModeForInfraredSensor() {
    	return SaugbotEV3.createSensorModeForInfraredSensor();
    }

    public void shutDown() {
        SaugbotEV3.shutDown();
    }
}
