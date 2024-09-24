package de.binaris.seams.main;

import de.binaris.seams.logic.StayInBorder;

public class Application {

	public static void main(String[] args) {
		StayInBorder stayInBorder = new StayInBorder();
		stayInBorder.start(5);

	}

}
