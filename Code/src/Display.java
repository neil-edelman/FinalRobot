/* Lab 4, Group 51 -- Alex Bhandari-Young and Neil Edelman */

//import java.lang.IllegalArgumentException;

import lejos.nxt.LCD;
import lejos.util.Timer;
import lejos.util.TimerListener;

public class Display implements TimerListener{
	public static final int LCD_REFRESH = 500;

	private static String  drawText = "exterminate";

	private static Display owner = null;

	private Timer displayTimer = new Timer(LCD_REFRESH, this);
	private Swagbot robot;

	/** displays some relevant information on the robot; only one Display can
	 be active; if no display is using it, it starts the automatically
	 @param robot a robot to associate with the display */
	public Display(final Swagbot robot) {
		//if(owner != null) throw new IllegalArgumentException("already a display");
		this.robot = robot;
		if(owner == null) {
			owner = this;
			displayTimer.start();
		}
	}

	/** stops the TimerListener, freeing the display */
	public void stop() {
		if(owner != this) return;
		displayTimer.stop();
		owner = null;
	}

	/** restarts the TimerListener if nothing is using the display */
	public void start() {
		if(owner != null) return;
		displayTimer.start();
		owner = this;
	}

	/** TimerListener function that updates the text
	 @author Alex */
	public void timedOut() {
		Position position = robot.getPosition();
		LCD.clear();
		LCD.drawString("" + robot.getName(), 0, 0, true);
		LCD.drawString("" + robot.getStatus() + " | " + robot.getFindStatus(), 0, 1);
		LCD.drawString("pos: " + position, 0, 2);
		LCD.drawString(drawText, 0, 3, true);
      LCD.drawString("SmPing:   " + robot.getSmallestPing(), 0, 4);
      LCD.drawString("TTheta:   " + robot.getTargetTheta(), 0, 5);
      LCD.drawString("UDist:    " + robot.getDistance(), 0, 6);
      LCD.drawString("UFDist:   " + robot.getFilteredDistance(), 0, 7);

//      LCD.drawString("Distance:", 0, 5);
//      LCD.drawString("MedFilter:", 0, 6);

	}

	/** sets the user-specified text string
	 fixme: some sort of bounds check?
	 @author Neil
	 @param text text string */
	public static void setText(String text) {
		drawText = text; 
	}

}
