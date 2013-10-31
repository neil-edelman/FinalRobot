/* Lab 4, Group 51 -- Alex Bhandari-Young and Neil Edelman */

import lejos.nxt.LCD;
import lejos.util.Timer;
import lejos.util.TimerListener;

public class Display implements TimerListener{
	public static final int LCD_REFRESH = 100/*temp: swich back to 100 later*/;
	private Timer displayTimer;
   private Robot robot;
   private Position position;
   private String drawText = "No text to draw";
	
	public Display(Robot robot) {
		this.robot = robot;
		this.displayTimer = new Timer(LCD_REFRESH, this);
		displayTimer.start();// start the timer
	}
	
	public void timedOut() { 
		LCD.clear();
      position = robot.getPosition();
		LCD.drawString("X value: ", 0, 0);
		LCD.drawString("Y value: ", 0, 1);
		LCD.drawString("Theta value: ", 0, 2);
		LCD.drawString("Distance: ", 0, 3);
      LCD.drawString(drawText, 0, 4);
		LCD.drawInt((int)(position.x), 13, 0);
		LCD.drawInt((int)(position.y), 13, 1);
		LCD.drawInt((int)(position.getTheta()), 13, 2);
//      LCD.drawInt(robot.getDistance(), 13, 3);
	}
   public void setText(String text) {
      this.drawText = text; 
   }

}
