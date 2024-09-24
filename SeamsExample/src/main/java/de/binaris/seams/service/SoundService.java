package de.binaris.seams.service;

import lejos.hardware.Audio;
import lejos.hardware.BrickFinder;

public final class SoundService {

    private static final Audio audio = BrickFinder.getDefault().getAudio();;

    private SoundService() {}

    public static void beep() {
        audio.systemSound(0);
    }
}
