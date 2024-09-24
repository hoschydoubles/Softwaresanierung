package main;

import lejos.hardware.Button;

public class Knopf {	
	
	private boolean init = false;
	private Thread one;
	private int running;
	public boolean pressed = false;
	
	public void activateReader() {
		one = new Thread() { public void run() {
			running=1;
			while(running==1) {
				if(Button.readButtons()>0) {
					pressed=true;
				}
				if(!init) {
					System.out.println("Buttonreader: Initialized");
					init = true;
				}
				
			}
		}};
		one.start();
	}
	
	public void deactiveReader() {
		running=0;
	}

}
