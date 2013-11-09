/* Lab 4, Group 51 -- Alex Bhandari-Young and Neil Edelman */

//import java.lang.IllegalArgumentException;

import lejos.nxt.LCD;
import lejos.util.Timer;
import lejos.util.TimerListener;

public class Display implements TimerListener{
	public static final int LCD_REFRESH = 500;

	private static String drawText1 = "Exterminate.";
	private static String drawText2 = "Exterminate.";

	private Timer displayTimer = new Timer(LCD_REFRESH, this);
	private boolean isStarted = false;
	private Robot robot;

	public Display(final Robot robot) {
		this.robot = robot;
		displayTimer.start();
		isStarted = true;
	}

	public void start() {
		if(isStarted) return;
		displayTimer.start();
	}

	public void stop() {
		if(!isStarted) return;
		displayTimer.stop();
	}

	public void timedOut() {
		Position position = robot.getPosition();
		LCD.clear();
		LCD.drawString("" + robot.getName(), 0, 0, true);
		LCD.drawString("" + robot.getStatus(), 0, 1);
		LCD.drawString("" + position, 0, 2, true);
		LCD.drawString(drawText1, 0, 3);
		LCD.drawString(drawText2, 0, 4);
	}

	/** fixme: some sort of bounds check */
	public static void setText(String text) {
		drawText1 = text; 
	}

	/** fixme: some sort of bounds check */
	public static void setText2(String text) {
		drawText2 = text; 
	}
}
