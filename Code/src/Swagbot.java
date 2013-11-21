/* Swagbot extends Locobot; obstacle avoidence and gameplay logic
 @author Alex */

import lejos.nxt.SensorPort;
import lejos.util.Timer;
import lejos.nxt.Sound;

import java.util.ArrayList;

public class Swagbot extends Locobot {//Swagbot extends Localisingbot

   private UltrasonicListener uListener;
   private Timer uTimer;
   private float targetTheta = -1f;
   private int smallestPing = 254;
   private boolean findingFirst = true;
   private float DESTINATION_X;
   private float DESTINATION_Y;
   private static final int   SCAN_THRESHOLD = 70; 
   private static final float FRONT_EXTENSION_LENGTH = 22.5f; //bumper length from wheel base
   private static final float X_LOW_BOUND  = 0f      + FRONT_EXTENSION_LENGTH; //used as mins and maxes in determining target
   private static final float Y_LOW_BOUND  = 0f      + FRONT_EXTENSION_LENGTH; //origin in corner
   private static final float X_HIGH_BOUND = 8*30.48f - FRONT_EXTENSION_LENGTH; //4 tiles by
   private static final float Y_HIGH_BOUND = 8*30.48f - FRONT_EXTENSION_LENGTH; //8 tiles
   private float adjust_x = 30.48f; //designates the point on the field to be searched from
   private float adjust_y = 30.48f; //values are (0,0);(30,30);(30,60);(30,90)...etc
   private float targetX,targetY;
   private Position storeTarget;
   private FindStatus storeFindStatus;
	private ArrayList<Ping> pingsList = new ArrayList<Ping>(128);

	private Colour colour;

	/**
	 @author Neil
	 @return the colour as enum Value (STYROFOAM/WOOD) */
	public Colour.Value getColour() {
		return colour.getColourValue();
	}

	/** works reliably to about 10cm
	 @author Neil, Alex
	 @return a range that indicates our certaitanty that it's styrofoam
	 in [0, 1] */
	public float getStyrofoam() {
		return colour.getStyrofoamProbability();
	}

	/** the constructor */
   public Swagbot(final float x, final float y) {
      super();
      uListener = new UltrasonicListener(this.sonic);
      uTimer = new Timer(10/*round-up to int 9.375*/,uListener); //timeout value in ms
      uTimer.start();
      Sound.setVolume(100);
      this.DESTINATION_X = x;
      this.DESTINATION_Y = y;
	   colour = new Colour(Hardware.colourPort);
   }

   //**********************************
   //override color hack -- for demo, color in locobot not working -- TODO:can remove this when we get it working
	/* Neil: you were calling super() with the parameter order reversed; trying
	 to read colour from a light sensor . . . I have stuck the colour in here
	 instead of in Locobot so it's all Good */
/*   Colour colour = new Colour(SensorPort.S3); 
   public Colour.Value getColour() {
      return colour.getColourValue();
   }*/
   //**********************************

   /** returns the ultrasonic sensor's unfiltered distance */
   public int getDistance() {
      return uListener.getDistance();
   }
   /** returns the ultrasonic sensor's filtered distance */
   public int getFilteredDistance() {
      return uListener.getFilteredDistance();
   }
   /** returns the smallest distance found by the ultrasonic sensor during the most recent scan*/
   public int getSmallestPing() {
      return smallestPing;
   }
   /** returns the target theta for a turn*/
   public float getTargetTheta() {
      return targetTheta;
   }

   /** Causes the robot to search the entire field for blocks and return them to the destination.
	 @author Alex
	 @return void
         set's the robot status to finding and the finding status to scanning.*/
   public void findBlocks() {
      this.status = Status.FINDING;
      this.findStatus = FindStatus.TURNING;
      findingFirst = true;
   }

   /** overridden from Robot: contains the code for finding blocks and placing them in the destination
	 @author Alex
	 @return void */
   protected void finding() {
         
      //find blocks from search point (starts in corner and progresses along board)
      if(this.findStatus == FindStatus.TURNING) {
         this.findStatus = FindStatus.SCANNING;
	      this.turnTo(0f,250);
      }
      //on the first iteration the robot is in the corner and only turns ninty degrees
      //findloop: scan -> if block: travel to destination -> travel to scan point and repeat
      else if(this.findStatus == FindStatus.SCANNING) { //SCAN
         boolean first = findingFirst;
         //update x and y
         if(first) {
            adjust_x += 30f;
         }
         adjust_y += 30f;

         if(first) {
            findingFirst = false; //false after the first iteration of the loop     
            scanLeft(90f); //turns left and scans to 90 degrees, disables the p error correction so the robot can ping at a constant speed
         }
         //if it is not the first iteration, the robot has moved to the center of the field and now turns the full 180 degrees
         //to search for blocks
         if(!first) {
            scanLeft(180f);
         }
         this.findStatus = FindStatus.SCANNED;
      }
      else if(this.findStatus == FindStatus.SCANNED) {
      //if there is a block...
         if(smallestPing < SCAN_THRESHOLD) { //FOUND BLOCK
            this.findStatus = FindStatus.ID;
            //when the robot encounters a block, the position of the block is calculated using the measured distance
            //the robot then move near that position and gets a colour reading of the block
            Position position = this.getPosition();
            targetX = position.x + (smallestPing-2)*(float)Math.cos(Math.toRadians(targetTheta+3));
            targetY = position.y + (smallestPing-2)*(float)Math.sin(Math.toRadians(targetTheta+3));
            checkTargetBounds();
            this.travelTo(targetX,targetY,250,250); //move near test object
         }
         else {
            Sound.buzz(); //no blocks found
            this.findStatus = FindStatus.RELOCATING;
         }
      }
      else if(this.findStatus == FindStatus.ID) { //ID BLOCK
         //if styroform go to destination!
         if(this.getColour() == Colour.Value.STYROFOAM) { //is styrofoam, grab and move
            Sound.beep();
            this.findStatus = FindStatus.FOUND;
         }
         else { //is wood move on
            Sound.buzz();
            this.findStatus = FindStatus.RELOCATING;
            checkTargetBounds();
            backup();
            //this.travelTo(targetX-15,targetY-15,250,250); //backup
         }
      }
      else if(this.findStatus == FindStatus.FOUND) {
         this.findStatus = FindStatus.FINISHED;
         checkTargetBounds();
         travelTo(DESTINATION_X,DESTINATION_Y,250,250);
         //then it stops
      }
      else if(this.findStatus == FindStatus.RELOCATING) {
         this.findStatus = FindStatus.TURNING;
         checkTargetBounds();
         this.travelTo(adjust_x,adjust_y,250,250); //TRAVEL TO NEXT SCAN POINT
      }
      else if(this.findStatus == FindStatus.AVOIDING) {
         //do nothing
      }
         
   }
   private void checkTargetBounds() {
      if(     targetX < X_LOW_BOUND ) targetX = X_LOW_BOUND; //bounds check
      else if(targetX > X_HIGH_BOUND) targetX = X_HIGH_BOUND;
      if(     targetY < Y_LOW_BOUND ) targetY = Y_LOW_BOUND;
      else if(targetY > Y_HIGH_BOUND) targetY = Y_HIGH_BOUND;
   }
   /** robot scans left to the given angle, and records the smallest distance and corresponding theta
	 @author Alex
	 @return void */
   public void scanLeft(float angle) {
      this.pingsList.clear();
      this.smallestPing = 254;
      this.targetTheta = 45;
      this.turn(100f,angle); //turn constantly (left when rate positive) to angle
      this.status = Status.SCANNING;
   }
   /** robot scans right to the given angle, and records the smallest distance and corresponding theta
	 @author Alex
	 @return void */
   public void scanRight(float angle) {
      this.pingsList.clear();
      this.smallestPing = 254;
      this.targetTheta = 45;
      this.turn(-100f,angle); //turn constantly (left when rate positive) to angle
      this.status = Status.SCANNING;
   }

   /**overridden from Robot: contains the code for scanning
    @author Alex
	 @return void */
protected void scanning() {
      //while scanning get smallest ping value and corresponding theta
      int ping = uListener.getDistance();
      Position pos = this.getPosition();

      if(smallestPing > ping) {
         smallestPing = ping;
         targetTheta = pos.getDegrees();
      }
      //record pings
		if(ping < 0 || ping > 255) { //check out of bounds
			Display.setText("Ping value out of bounds" + ping);
		} else {
			// record
         pingsList.add(new Ping(pos,ping));
		}


   }
   /** overwritten from Robot, avoidance maneuver runs when uDistance less than threshold
	 @author Alex
	 @return void */
   protected void avoidance(int threshold) {
      int ping = uListener.getDistance();
      if(ping < threshold) {
         if(this.getColour() == Colour.Value.STYROFOAM) { //is styrofoam, grab and move
         }
         else {
            Sound.beep();
            // ? avoidance.avoid(threshold);
         }
     }
   }
   /** robot backs up at a constant speed for a constant time, used in avoidance
	 @author Alex
	 @return void */
   public void backup() {
      this.setSpeeds(-200,-200);
      try {
         Thread.sleep(1000);
      } catch(InterruptedException ex) {
         Thread.currentThread().interrupt();
      }
      this.stop();
   }
} 
