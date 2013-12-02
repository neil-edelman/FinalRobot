import lejos.nxt.SensorPort;
import lejos.util.Timer;
import lejos.nxt.Sound;

import java.util.ArrayList;

import AStar.Types;
import AStar.AStar;
import AStar.TypeMap;
import AStar.FieldMap;
import AStar.GoalNode2D;
import AStar.ISearchNode;
import AStar.SearchAndAvoidNode2D;
import java.util.ArrayList;
import java.util.*;

/** Swagbot adds functionality for findingBlocks and avoidance.AStar was initially supposed to be implemented here in it's entirety.However we did not
plan this correctly as travelWithAStar is blocking and this class is a timerlistener.The travelWithAStar method was moved to the main method where it was
used during the first two heats of the competition but later removed because it was causing problems.It would be possible to implement AStar correctly with a
couple days more work, but we learned about the issues too late to deal with them.
Additionally, the get smallestping method allows the listener to accumulate a data set of pings while a blocking navigation
or odometer method is running. Get smallest ping is used in this lab to scan from scan points and find the blocks using the
ultrasonic sensor. The method sets the smallestping variables to the closest distance recored and corresponding theta from
the odometer to targetTheta.
 @author Alex */
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
   private static final float X_HIGH_BOUND = 12*30.48f - FRONT_EXTENSION_LENGTH; //4 tiles by
   private static final float Y_HIGH_BOUND = 12*30.48f - FRONT_EXTENSION_LENGTH; //8 tiles
   private float adjust_x = 30.48f; //designates the point on the field to be searched from
   private float adjust_y = 30.48f; //values are (0,0);(30,30);(30,60);(30,90)...etc
   private float targetX,targetY;
   private Position storeTarget;
   private FindStatus storeFindStatus;
	private ArrayList<Ping> pingsList = new ArrayList<Ping>(128);
   private FieldMap map;
   private Colour altColour = new Colour(SensorPort.S2);

	private Colour  colour;
	private Stacker stacker;

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
   public Swagbot(FieldMap map, final float x, final float y) {
      super();
      uListener = new UltrasonicListener(this.sonic);
      uTimer = new Timer(10/*round-up to int 9.375*/,uListener); //timeout value in ms
      uTimer.start(); /* holy cow -Neil */
      Sound.setVolume(100);
      this.DESTINATION_X = x;
      this.DESTINATION_Y = y;
	   colour  = new Colour(Hardware.colourPort);
	   stacker = new Stacker(altColour, this);
      this.map = map;
   }

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

		/* @author Neil
		 I've appropriated the findStatus.FINISHED to say when we've collected
		 two blocks and are returning; this tests (hopefully) when the
		 Status.TRAVELLING is finished and the FindStatus.FINISHED is set
		 which would be at the end when we've collected blocks and are at the
		 green zone */
		if(findStatus == FindStatus.FINISHED) {
			findStatus = FindStatus.IDLE;
			timer.stop();
			stacker.greenZone(); /* blocks */
			this.setSpeeds(-100, 100);
			timer.start(); /* <- done, not really necesary */
		}
		/* I don't know what Swagbot's behaivoir should be, but I itend to test
		 every 100 ms if it has a block */
		if(stacker.hasBlock()) {
			timer.stop();
			/* hasTwoBricks() has code for storing a block (blocking! that's
			 why we pause the timer) and returns whether we've stacked two */
			if(stacker.hasTwoBricks()) {
				/* travel to the destination! fixme: this is the dest, right? */
				this.travelTo(DESTINATION_X, DESTINATION_Y);
				findStatus = FindStatus.FINISHED;
			}
			timer.start();
			return;
		}


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
            Position position = this.getPosition();
            int x = (int)(position.x + (smallestPing)*(float)Math.cos(position.getRadians()));
            int y = (int)(position.y + (smallestPing)*(float)Math.sin(position.getRadians()));
            map.fill(x-1,y-1,x+1,y+1,Types.NONE);
            this.findStatus = FindStatus.FOUND;
            this.travelTo(x,y,250,250);
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
		} 
      else if(ping < SCAN_THRESHOLD){
			// record
         pingsList.add(new Ping(pos,ping));
         int x = (int)(pos.x + (ping)*(float)Math.cos(pos.getRadians()));
         int y = (int)(pos.y + (ping)*(float)Math.sin(pos.getRadians()));
         map.fill(x-1,y-1,x+1,y+1,Types.OBSTACLE);
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
   /** Use A* algorithm to determine the shortest path to the target while avoiding known obstacles
    @author Alex
    @return ArrayList<ISearchNode>*/
   public ArrayList<ISearchNode> getPathTo(int destinationX, int destinationY) {
      GoalNode2D goalNode = new GoalNode2D(destinationX, destinationY);
      ISearchNode initialNode = new SearchAndAvoidNode2D(this.map, (int)this.getPosition().x/10, (int)this.getPosition().y/10, null, goalNode);
      ArrayList<ISearchNode> path = new AStar().shortestPath(initialNode, goalNode);
      path = straightenPath(path);
//      for (int i = 0; i < path.size(); i++) {
//         System.out.println("Element number " + i + " is " + path.get(i));
      return path;
   }
   /** A* package used returns the path in terms of points every node, this removes nodes that form a line
    @author Alex
    @return ArrayList<ISearchNode>*/
   public ArrayList<ISearchNode> straightenPath(ArrayList<ISearchNode> path) {
      int px = 0, py = 0, count;
      float m = 0;

      ArrayList<ISearchNode> output = new ArrayList<ISearchNode>();

      count = 0;
      
      for (ISearchNode current : path) {
         if (count >= 2) { //Determine if new point is on the line
            float newM = (float)(current.getY() - py) / (current.getX() - px);
            if (Math.abs(m - newM) > 0.00001f) { //Is not on the line
               output.add(current);
               count = 0;
            } else {
               output.remove(output.size() - 1);
               output.add(current);
               count = 3;
            }
         } else {
            count++;
            if (count == 2) {
               m = (float)(current.getY() - py) / (current.getX() - px);
            }
            px = current.getX();
            py = current.getY();
            output.add(current);
         }
      }
      return output;
   }
} 
