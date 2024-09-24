package main;

import external_libs.EdgeReverse;
import lejos.remote.ev3.RemoteRequestPilot;
import robot.EasyRequestSampleProvider;
import robot.Saugbot;

public class Sauger {

    /*
     * Lejos Staubsauger-Roboter v1.0.21
     * 14.04.2017 by John Doe
     * * * last change: 03.09.2017
     * * * TODO: + variable for MaxPaths
     * - constants for turning * -
     * Erkennung für "Kein Untergrund"
     * - Sensor für Hindernisse und *
     * Hindernissumfahrung *
     */
    private final int GRADZAHL = 85;
    private final int WENDEWEG = 5;
    private final int MIN_DISTANCE = 25;

    private Knopf knopf;

    // Robot Connection
    private RemoteRequestPilot pilot;
    private EasyRequestSampleProvider sample;
    private EasyRequestSampleProvider sampleIR;
    private Saugbot saugbot;

    private boolean stopWeilKeinUntergrundErkannt = false;

    private String farbenname(int color) {
        String answer = "";
        switch (color) {
            case 7:
                answer = "BLACK";
                break;
//             case Color.GREEN:
            // answer = "GREEN";
            // break;
            case -1:
                answer = "NONE";
                break;
            case 6:
                answer = "WHITE";
                break;
            case 13:
                answer = "BROWN";
                break;
            default:
                answer = Integer.toString(color);
        }

        return answer;
    }

    private int start() {
        /* Initialisierung vom Roboter */
        try {

            // neue Instanz von EV3 muss erstellt werden:
            saugbot.createInstance();
            // Piloten erstellen:
            // Wird für Bewegungen des Roboters gebraucht
            pilot = saugbot.createPilot();
            // Gesdchwindigkeit vom Pilot runtersetzen:
            pilot.setLinearSpeed(15f);
            // SampleProvider erstellen:
            // Wird für das Auslesen des Farbsensors gebraucht
            sample = saugbot.createSampleProviderForColorSensor();
            sampleIR = saugbot.createSensorModeForInfraredSensor();
            // Zusatzmotor erstellen:
            // Wird zum Saugen gebraucht
            /** motor = saugbot.createMotor('C'); **/
            // Bei Erfolg wird eins returned:
            return 1;
        } catch (Exception e) {
            // Fehlermeldung in Konsole ausgeben:
            System.out.println(e.getMessage());
            // Roboter beenden:
            end();
        }
        // Wird nur erreicht, wenn etwas fehlschlägt:
        return -1;
    }

    private int end() {
        /* Schliessen der Roboterverbindung */
        try {
            // SampleProvider schließen:
            // Wird close() nicht ausgeführt, muss der Roboter
            // neu gestartet werden.
            sample.close();
        } catch (Exception e) {
            // Fehlermeldung in Konsole ausgeben:
            System.out.println(e.getMessage());
        }
        try {
            // Piloten schließen:
            // Wird close() nicht ausgeführt, muss der Roboter
            // neu gestartet werden.
            pilot.close();
        } catch (Exception e) {
            // Fehlermeldung in Konsole ausgeben:
            System.out.println(e.getMessage());
        }
        try {
            // Motor schließen:
            // Wird close() nicht ausgeführt, muss der Roboter
            // neu gestartet werden.
            /** motor.close(); **/
        } catch (Exception e) {
            // Fehlermeldung in Konsole ausgeben:
            System.out.println(e.getMessage());
        }
        try {
            sampleIR.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try {
            saugbot.shutDown();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        // Wird nur erreicht, wenn etwas fehlschlägt:
        return -1;
    }

    private void forward() {
        /* Roboter fährt bis zum Ende des Raums */
        pilot.forward();
        float gesehen, distance;
        while (true) {
            if (knopf.pressed) {
                pilot.stop();
                break;
            }

            gesehen = sample.getSample();
            if (!farbenname((int) gesehen).equals("WHITE")) {
                pilot.stop();
                stopWeilKeinUntergrundErkannt = farbenname((int) gesehen).equals("NONE");
                break;
            }
            distance = sampleIR.getSample();
            if (checkWall(distance)) {
                pilot.stop();
                turnRight();
                pilot.travel(30);
                pilot.rotate(-85);
                distance = sampleIR.getSample();
                if (checkWall(distance)) {
                    break;
                } else {
                    pilot.forward();
                }
            }
        }

        if (stopWeilKeinUntergrundErkannt) {
            while (!EdgeReverse.run(pilot)) ;
            gesehen = sample.getSample();
            if (farbenname((int) gesehen).equals("WHITE")) stopWeilKeinUntergrundErkannt = false;
        }
    }

    private boolean checkWall(float distance) {
        return distance < MIN_DISTANCE;
    }

    private void turnRight() {
        if (!knopf.pressed) {
            pilot.rotate(GRADZAHL);
        }
    }

    private boolean checkKante() {
        float gesehen = sample.getSample();
        if (gesehen == -1.0f) stopWeilKeinUntergrundErkannt = true;
        return stopWeilKeinUntergrundErkannt;
    }

    private void turn() {
        /* Roboter wendet (linksdrehung) */
        if (!knopf.pressed) {
            pilot.rotate(GRADZAHL * -1);
            checkKante();
            pilot.travel(WENDEWEG);
            checkKante();
            pilot.rotate(GRADZAHL * -1);
            checkKante();
        }
    }

    private void turn_RIGHT() {
        /* Roboter wendet nach Rechts */
        if (!knopf.pressed) {
            pilot.rotate(GRADZAHL);
            checkKante();
            pilot.travel(WENDEWEG);
            checkKante();
            pilot.rotate(GRADZAHL);
            checkKante();
        }
    }

    public void main() {

        int maximale_wege_des_roboters_bevor_stopp = 3;

        saugbot = new Saugbot();
        if (start() > 0) {
            knopf = new Knopf();
            knopf.activateReader();

            /** motor.forward(); **/
            do {
                if (stopWeilKeinUntergrundErkannt || knopf.pressed) maximale_wege_des_roboters_bevor_stopp = -1;

                maximale_wege_des_roboters_bevor_stopp--;

                forward();
                if (!stopWeilKeinUntergrundErkannt) {
                    turn();
                    if (stopWeilKeinUntergrundErkannt) break;
                    forward();
                    if (!stopWeilKeinUntergrundErkannt) turn_RIGHT();
                }

                if (stopWeilKeinUntergrundErkannt || knopf.pressed) maximale_wege_des_roboters_bevor_stopp = -1;
            } while (maximale_wege_des_roboters_bevor_stopp > 0);
            /** motor.stop(); **/

            knopf.deactiveReader();
            end();
        }
    }

}
