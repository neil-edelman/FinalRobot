/* Lab 4, Group 51 -- Alex Bhandari-Young and Neil Edelman */

/* LightSensor Locatization File
This class runs when doLocatization is called. It assumes that the robot is oriented at -x,-y 
from the origin, thus rotating allows the light sensor to cross
the x and y axis twice each. Then the correct x and y are computed, 
and theta is corrected. */

import lejos.nxt.LightSensor;
import lejos.nxt.Sound;
public class LightLocalizer {
	private ExtendedRobot robot;
	private LightSensor ls;
   private float d = 10.8f; //light sensor distance
   private int CORRECTION_PERIOD = 100;
	private int blip;
	public LightLocalizer(ExtendedRobot robot, LightSensor ls) {
		this.robot = robot;
		this.ls = ls;
		ls.setFloodlight(true); // turn on the light
	}
	
	public void doLocalization() {

      //setup vaiables
      int correctionStart = (int)System.currentTimeMillis();
      blip = 0; //light sensor detection (should only be triggered by lines)
      Position position;
      Position getPosition;
      float[] theta = new float[4]; //stores the four heading values for blips
      
		// start rotating and clock all 4 gridlines
      robot.rotateConstantly(-10); //rotate left
      try {
         Thread.sleep(50);
      } catch (InterruptedException e) {}
		while(blip<4) { //rotate for 4 "blips"
         robot.getPosition(getPosition);
         if(ls.readValue()<47) { //if blip
            theta[blip] = (float)Math.toDegrees(getPosition.theta); //get theta and change to degrees before setting to theta array
            blip++; //count blip
            Sound.beep();
              try {
                Thread.sleep(2000);
             } catch (InterruptedException e) {}
         }
         int correctionEnd = (int)System.currentTimeMillis();
         try {
            Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
         } catch (InterruptedException e) {}
                                                         
      }
      robot.stop(); //stop
      // do trig to compute (0,0) and 0 degrees
      //compute delta theta x and y
      float thetaY = theta[0]-theta[2];//theta over y-axis (FOR X CORRECTION)
      float thetaX = theta[1]-theta[3];//theta over x-axis (FOR Y CORRECTION)
      float thetaC = 180-theta[0]+thetaY/2;//error in theta
      //apply fomula
      position.x = -d*(float)Math.cos(thetaY/2)/10;//x computed
      position.y = -d*(float)Math.cos(thetaX/2)/10;//y computed
      position.theta = getPosition[2] - thetaC; //theta computed
      robot.setPosition(position);
		// when done travel to (0,0) and turn to 0 degrees
      robot.travelTo(0,0);
      robot.turnTo(0);

   }
   public int getLS() {
      return ls.readValue();
   }
   public int getCount() {
      return blip;
   }

}
