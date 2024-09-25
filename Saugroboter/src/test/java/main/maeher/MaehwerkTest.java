package main.maeher;

import lejos.robotics.RegulatedMotor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MaehwerkTest {

    Maehwerk maehwerk;

    @Mock
    MaehwerkSaugbotFacade facadeMock;

    @Mock
    RegulatedMotor motorMock;

    @Before
    public void setUp() {
        Mockito.when(facadeMock.initMotor()).thenReturn(motorMock);
        maehwerk = new Maehwerk(facadeMock);
    }

    @Test
    public void testInitMotorSuccessful() {
        //In diesem Fall ist Arrange (Stubbing) sowie Act (Init der Instanz) in den SetUp gewandert.
        Mockito.verify(facadeMock).initMotor();
    }

    @Test
    public void startMotorSuccessful() {
        maehwerk.start();
        Mockito.verify(motorMock).forward();
    }

    @Test
    public void shutDownRunningMaehwerk() {
        Mockito.when(motorMock.isMoving()).thenReturn(Boolean.TRUE);

        maehwerk.shutdown();

        Mockito.verify(motorMock).stop();
        Mockito.verify(motorMock).close();
    }

    @Test
    public void shutDownAlreadyStoppedMaehwerk() {
        Mockito.when(motorMock.isMoving()).thenReturn(Boolean.FALSE);

        maehwerk.shutdown();

        Mockito.verify(motorMock,Mockito.never()).stop();
        Mockito.verify(motorMock).close();
    }
}
