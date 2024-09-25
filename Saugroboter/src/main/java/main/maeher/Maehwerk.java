package main.maeher;

import lejos.robotics.RegulatedMotor;

public class Maehwerk {

    private RegulatedMotor motor;

    public Maehwerk(MaehwerkSaugbotFacade facadeMock) {
        this.motor = facadeMock.initMotor();
    }


    public void start() {
        motor.forward();
    }

    public void stop() {
        motor.stop();
    }

    public void shutdown() {
        if(motor.isMoving()) {
            motor.stop();
        }
        motor.close();
    }
}
