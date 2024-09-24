package de.binaris.seams.logic;

import java.io.IOException;
import java.util.Random;

import de.binaris.seams.robot.RemoteEV3;
import de.binaris.seams.service.SoundService;
import lejos.hardware.Sound;
import lejos.hardware.motor.Motor;
import lejos.remote.ev3.RemoteRequestEV3;
import lejos.remote.ev3.RemoteRequestPilot;
import lejos.robotics.Color;
import lejos.robotics.navigation.DifferentialPilot;
import de.binaris.seams.service.ColorSensorService;

public class StayInBorder {

    private static final String BRICK_IP = "192.168.0.101";

    public boolean start(int maxBorderHitsTillQuit) {
        boolean thresholdNotReached = true;

        RemoteEV3 ev3 = new RemoteEV3();
        try {
            RemoteRequestPilot pilot = ev3.createPilot();
            ColorSensorService colorSensorService = ev3.getColorSensorService();

            int hitCounter = 0;
            while (thresholdNotReached) {
                pilot.forward();
                while (colorSensorService.readColor() != Color.BLACK) {
                }
                pilot.stop();
                SoundService.beep();

                pilot.rotate(getRandomTurnDirectionWithinRange(60d, 180d));

                hitCounter++;
                thresholdNotReached = hitCounter < maxBorderHitsTillQuit;
            }
            System.out.println("Hit the border " + hitCounter + " times.");
        } finally {
            //Wichtig damit die Verbindungen zum Roboter wieder gekappt werden. Ansonsten muss der Roboter neu
            // gestartet werden.
            ev3.close();
        }

        return !thresholdNotReached;
    }

    private double getRandomTurnDirectionWithinRange(double min, double max) {
        double random = new Random().nextDouble();
        double result = Math.round(min + (random * (max - min)));
        System.out.println(result);
        return result;
    }

}
