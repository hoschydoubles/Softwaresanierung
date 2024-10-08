package lejos.hardware;

import java.io.*;

import lejos.utility.Delay;

/**
 * Class that provides access methods for the local audio device
 *
 */
public class Sound implements Sounds
{
    protected final static Audio audio = BrickFinder.getDefault().getAudio();
    // TODO: We should probably not have the following. Add a saveProperties method?
    public final static String VOL_SETTING = "lejos.volume";

    private Sound()
    {
    }
    
    public static int C2 = 523;
    
    /**
     * Play a system sound.
     * <TABLE BORDER=1>
     * <TR><TH>aCode</TH><TH>Resulting Sound</TH></TR>
     * <TR><TD>0</TD><TD>short beep</TD></TR>
     * <TR><TD>1</TD><TD>double beep</TD></TR>
     * <TR><TD>2</TD><TD>descending arpeggio</TD></TR>
     * <TR><TD>3</TD><TD>ascending  arpeggio</TD></TR>
     * <TR><TD>4</TD><TD>long, low buzz</TD></TR>
     * </TABLE>
     */
    public static void systemSound(boolean aQueued, int aCode)
    {
        audio.systemSound(aCode);

    }

    /**
     * Beeps once.
     */
    public static void beep()
    {
        systemSound(true, BEEP);
    }

    /**
     * Beeps twice.
     */
    public static void twoBeeps()
    {
        systemSound(true, DOUBLE_BEEP);
    }

    /**
     * Downward tones.
     */
    public static void beepSequence()
    {
        systemSound(true, DESCENDING);
    }

    /**
     * Upward tones.
     */
    public static void beepSequenceUp()
    {
        systemSound(true, ASCENDING);
    }

    /**
     * Low buzz 
     */
    public static void buzz()
    {
        systemSound(true, BUZZ);
    }

    public static void pause(int t)
    {
        Delay.msDelay(t);
    }

    /**
     * Returns the number of milliseconds remaining of the current tone or sample.
     * @return milliseconds remaining
     */
    public static int getTime()
    {
        return 0;
    }
    

    /**
     * Plays a tone, given its frequency and duration. 
     * @param aFrequency The frequency of the tone in Hertz (Hz).
     * @param aDuration The duration of the tone, in milliseconds.
     * @param aVolume The volume of the playback 100 corresponds to 100%
     */
    public static void playTone(int aFrequency, int aDuration, int aVolume)
    {
        audio.playTone(aFrequency, aDuration, aVolume);
    }
    

    public static void playTone(int freq, int duration)
    {
        audio.playTone(freq, duration);
    }


    

    /**
     * Play a wav file
     * @param file the 8-bit PWM (WAV) sample file
     * @param vol the volume percentage 0 - 100
     * @return The number of milliseconds the sample will play for or < 0 if
     *         there is an error.
     * @throws FileNotFoundException 
     */
    public static int playSample(File file, int vol)
    {
        return audio.playSample(file, vol);
    }


    /**
     * Play a wav file
     * @param file the 8-bit PWM (WAV) sample file
     * @return The number of milliseconds the sample will play for or < 0 if
     *         there is an error.
     * @throws FileNotFoundException 
     */
    public static int playSample(File file)
    {
        return audio.playSample(file);
    }


    /**
     * Queue a series of PCM samples to play at the
     * specified volume and sample rate.
     * 
     * @param data Buffer containing the samples
     * @param offset Offset of the first sample in the buffer
     * @param len Number of samples to queue
     * @param freq Sample rate
     * @param vol playback volume
     * @return Number of samples actually queued
     */
    public static int playSample(byte [] data, int offset, int len, int freq, int vol)
    {
        return audio.playSample(data, offset, len, freq, vol);

    }

    /**
     * Play a note with attack, decay, sustain and release shape. This function
     * allows the playing of a more musical sounding note. It uses a set of
     * supplied "instrument" parameters to define the shape of the notes 
     * envelope.
     * @param inst Instrument definition
     * @param freq The note to play (in Hz)
     * @param len  The duration of the note (in ms)
     */
    public static void playNote(int[] inst, int freq, int len)
    {
        audio.playNote(inst, freq, len);

    }

    /**
     * Set the master volume level
     * @param vol 0-100
     */
    public static void setVolume(int vol)
    {
        audio.setVolume(vol);
    }

    /**
     * Get the current master volume level
     * @return the current master volume 0-100
     */
    public static int getVolume()
    {
        return audio.getVolume();
    }
    
    /**
     * Load the current system settings associated with this class. Called
     * automatically to initialize the class. May be called if it is required
     * to reload any settings.
     */
    public static void loadSettings()
    {
        audio.loadSettings();
    }
}
