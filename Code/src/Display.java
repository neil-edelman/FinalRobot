/* Lab 4, Group 51 -- Alex Bhandari-Young and Neil Edelman */

//import java.lang.IllegalArgumentException;

import lejos.nxt.LCD;
import lejos.util.Timer;
import lejos.util.TimerListener;

public class Display implements TimerListener{
	public static final int LCD_REFRESH = 500;

	private static String drawText1 = "";
	private static String drawText2 = "";

	private Timer displayTimer = new Timer(LCD_REFRESH, this);
	private boolean isStarted = false;
	private Swagbot robot;

	public Display(final Swagbot robot) {
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
//		LCD.drawString("" + robot.getName(), 0, 0, true);
		LCD.drawString("" + robot.getStatus() + " | " + robot.getFindStatus(), 0, 0);
		LCD.drawString("pos: " + position, 0, 1);
      LCD.drawString("SmPing:   " + robot.getSmallestPing(), 0, 2);
      LCD.drawString("TTheta:   " + robot.getTargetTheta(), 0, 3);
      LCD.drawString("UDist:    " + robot.getDistance(), 0, 4);
      LCD.drawString("UFDist:   " + robot.getFilteredDistance(), 0, 5);
		LCD.drawString(drawText1, 0, 6);
		LCD.drawString(drawText2, 0, 7);

//      LCD.drawString("Distance:", 0, 5);
//      LCD.drawString("MedFilter:", 0, 6);

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
