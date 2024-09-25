package main.maeher;

import lejos.robotics.RegulatedMotor;
import robot.Saugbot;

public class MaehwerkSaugbotFacade {

    private Saugbot saugbot;

    public MaehwerkSaugbotFacade(Saugbot saugbot) {
        this.saugbot = saugbot;
    }

    public RegulatedMotor initMotor() {
        return saugbot.createMotor('C');
    }


}
