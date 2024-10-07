package de.binaris.seams.logic;

import de.binaris.seams.robot.RemoteEV3;
import de.binaris.seams.service.ColorSensorService;
import de.binaris.seams.service.seam.SoundService;
import lejos.remote.ev3.RemoteRequestPilot;
import lejos.robotics.Color;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StayInBorderTest {
    @Mock
    RemoteEV3 eV3Mock;
    @Mock
    private ColorSensorService colorSensorMock;

    @Test
    public void testLetsTestSomething() {
        //Arrange
        StayInBorder stayInBorder = new StayInBorderForTest();
        when(eV3Mock.createPilot()).thenReturn(mock(RemoteRequestPilot.class));
        when(eV3Mock.getColorSensorService()).thenReturn(colorSensorMock);
        when(colorSensorMock.readColor()).thenReturn((float) Color.BLACK);
        SoundService.setUseRealImpl(false);

        //Act
        boolean reached = stayInBorder.start(3);

        //Assert
        Assert.assertTrue(reached);
        Assert.assertEquals(3, SoundService.beepCount);
    }


    private class StayInBorderForTest extends StayInBorder {


        @Override
        protected RemoteEV3 createRemoteEV3() {
            return eV3Mock;
        }
    }
}
