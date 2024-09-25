package main.maeher;

import lejos.remote.ev3.RemoteRequestSampleProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import robot.EasyRequestSampleProvider;

@RunWith(MockitoJUnitRunner.class)
public class HumanHandProtectionTest {

    HumanHandProtection protection;

    @Mock
    Maehwerk maehwerkMock;

    @Before
    public void setUp() {
        protection = new HumanHandProtection();
    }

    @Test
    public void checkDistanceToHumanHandStopsMotor() {
        boolean hasStoppedMotor = protection.checkHumanHandDistance(() -> maehwerkMock.stop(), 20);
        Assert.assertTrue(hasStoppedMotor);
        Mockito.verify(maehwerkMock).stop();
    }
}
