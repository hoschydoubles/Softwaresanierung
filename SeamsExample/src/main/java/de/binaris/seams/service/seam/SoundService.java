package de.binaris.seams.service.seam;

public class SoundService {

    private static boolean useRealImpl = true;
    public static int beepCount = 0;

    public static void setUseRealImpl(boolean useRealImpl) {
        SoundService.useRealImpl = useRealImpl;
    }

    public static void beep() {
        if (useRealImpl) {
            de.binaris.seams.service.SoundService.beep();
        } else {
            beepCount++;
        }
    }
}
