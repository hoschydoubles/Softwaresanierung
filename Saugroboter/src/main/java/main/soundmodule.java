package main;

import lejos.hardware.Sound;

public class soundmodule {
	/** Könnte Fehler verursachen **/
	
	private Boolean play=false,loop=false;
	private Integer freq=440,dur=200,vol=100;
	// freq = Frequency
	// dur = duration in Millis
	// vol = volume in Percent	
	
/* ----------------------------------------------------------------------
                                 INIT                                   */
	public boolean INIT = false;
	
	private Thread one;
	private int running;
	
	public void load() {
		one = new Thread() { public void run() {
			running=1;
			while(running==1) {
				if(play) {
					play=loop;
					Sound.playTone(freq,dur,vol);
				}
			}
			INIT = false;
		}};
		one.start();
		System.out.println("Test1");
		while(!INIT);
		System.out.println("Test2");
	}
	
	public void unload() {
		running=0;
		while(INIT);
	}
	
/* ----------------------------------------------------------------------
                                TONES                                   */
	
	public void a_short() {
		freq = 880;
		dur = 250;
		vol=100;
		loop=false;
		play=true;
	}		
	public void a_long() {
		freq = 880;
		dur = 1000;
		vol=100;
		loop=false;
		play=true;
	}
	
	public void vacuumtone() {
		freq = 100;
		dur = 5000;
		vol=30;
		loop=true;
		play=true;
	}
	
/* ----------------------------------------------------------------------
                                TONES                                   */
	public void unloop() {
		loop=false;
	}

}
