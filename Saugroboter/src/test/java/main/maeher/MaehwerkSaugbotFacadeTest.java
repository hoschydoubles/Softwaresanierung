package main.maeher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import robot.Saugbot;

@RunWith(MockitoJUnitRunner.class)
public class MaehwerkSaugbotFacadeTest {

    @Mock
    Saugbot saugbotMock;

    @Test
    public void getMotorSuccessful() {
        MaehwerkSaugbotFacade facade = new MaehwerkSaugbotFacade(saugbotMock);

        facade.initMotor();

        Mockito.verify(saugbotMock).createMotor(Mockito.anyChar());
    }
}
