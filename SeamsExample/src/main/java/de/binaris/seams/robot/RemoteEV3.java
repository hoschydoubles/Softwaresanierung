package de.binaris.seams.robot;

import de.binaris.seams.service.ColorSensorService;
import lejos.remote.ev3.RemoteRequestEV3;
import lejos.remote.ev3.RemoteRequestPilot;

import java.io.IOException;

public class RemoteEV3 {

    private static RemoteRequestEV3 ev3;

    private RemoteRequestPilot pilot;

    private ColorSensorService colorSensorService;

    public RemoteEV3() {
        String EV3BRICK_IP = "192.168.0.100";

        try {
            ev3 = new RemoteRequestEV3(EV3BRICK_IP);
            ev3.setDefault();
            this.colorSensorService = ColorSensorService.getInstance(ev3);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public RemoteRequestPilot createPilot() {
        pilot = (RemoteRequestPilot) ev3.createPilot(3.5, 17.5, "A", "D");
        return pilot;
    }

    public ColorSensorService getColorSensorService() {
        return this.colorSensorService;
    }

    public void close() {
        try {
            pilot.close();
            colorSensorService.close();
            ev3.disConnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
