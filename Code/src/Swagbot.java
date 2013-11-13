import lejos.nxt.SensorPort;
import lejos.util.Timer;
import lejos.nxt.Sound;

public class Swagbot extends Locobot {//Swagbot extends Localisingbot

   private UltrasonicListener uListener;
   private Timer uTimer;
   private float targetTheta = -1;
   private int smallestPing = 254;
   private boolean findingFirst = true;
   private static final float DESTINATION_X = 0;
   private static final float DESTINATION_Y = 50;
   private static final int   SCAN_THRESHOLD = 70; 
   private float adjust_x = 0; //designates the point on the field to be searched from
   private float adjust_y = 0; //values are (0,0);(30,30);(30,60);(30,90)...etc
   private float targetX,targetY;

	/** the constructor */
   public Swagbot(final SensorPort sonicPort, final SensorPort colourPort, final SensorPort lightPort) {
      super(sonicPort,colourPort,lightPort);
      uListener = new UltrasonicListener(this.sonic);
      uTimer = new Timer(10/*round-up to int 9.375*/,uListener); //timeout value in ms
      uTimer.start();
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
         set's the robot status to finding and the finding status to scanning.*/
   public void findBlocks() {
      this.status = Status.FINDING;
      this.findStatus = FindStatus.SCANNING;
      findingFirst = true;
   }

   /** overridden from Robot: contains the code for finding blocks and placing them in the destination */
   protected void finding() {

      //FindStatus is idle if the find loop is just starting
      //findloop: scan -> if block: travel to destination -> travel to scan point and repeat
      if(this.findStatus == FindStatus.SCANNING) { //SCAN
         boolean first = findingFirst;
         //update x and y
         if(first) {
            adjust_x += 30f;
         }
         adjust_y += 30f;

	      //find blocks from search point (starts in corner and progresses along board)
         //on the first iteration the robot is in the corner and only turns ninty degrees
         this.turnTo(0f);
         if(first)
            findingFirst = false; //false after the first iteration of the loop     
            scanLeft(90f); //turns left and scans to 90 degrees, disables the p error correction so the robot can ping at a constant speed
         //if it is not the first iteration, the robot has moved to the center of the field and now turns the full 180 degrees
         //to search for blocks
         if(!first)
            scanLeft(180f);
         this.findStatus = FindStatus.SCANNED;
      }
      else if(this.findStatus == FindStatus.SCANNED) {
      //if there is a block...
         if(smallestPing < SCAN_THRESHOLD) { //FOUND BLOCK
            this.findStatus = FindStatus.ID;
            //when the robot encounters a block, the position of the block is calculated using the measured distance
            //the robot then move near that position and gets a colour reading of the block
            Position position = this.getPosition();
            targetX = position.y + (smallestPing-2)*(float)Math.cos(Math.toRadians(targetTheta+3));
            targetY = position.x + (smallestPing-2)*(float)Math.sin(Math.toRadians(targetTheta+3));
            this.travelTo(targetX,targetY); //move near test object
         }
         else
            Sound.buzz(); //no blocks found
            this.findStatus = FindStatus.RELOCATING;
      }
      else if(this.findStatus == FindStatus.ID) { //ID BLOCK
         //if styroform go to destination!
         if(this.getColour() == Colour.Value.STYROFOAM) { //is styrofoam, grab and move
            Sound.beep();
            this.status = Status.IDLE;
            this.findStatus = FindStatus.FOUND;
            //travel with avoidance, go to the destination
            this.travelTo(DESTINATION_X,DESTINATION_Y); //TRAVEL TO DESTIONATION
         }
         else { //is wood move on
            this.findStatus = FindStatus.RELOCATING;
            Sound.buzz();
            this.travelTo(targetX-15,targetY-15); //backup
         }
      }
      else if(this.findStatus == FindStatus.FOUND) {
         this.findStatus = FindStatus.FINISHED;
      }
      else if(this.findStatus == FindStatus.RELOCATING) {
         this.travelTo(adjust_x,adjust_y); //TRAVEL TO NEXT SCAN POINT
      }
         
   }
   /** robot scans left to the given angle, and records the smallest distance and corresponding theta */
   public void scanLeft(float angle) {
      this.smallestPing = 254;
      this.targetTheta = 45;
      this.turn(100f,angle); //turn constantly (left when rate positive) to angle
      this.status = Status.SCANNING;
   }
   /** robot scans right to the given angle, and records the smallest distance and corresponding theta */
   public void scanRight(float angle) {
      this.smallestPing = 254;
      this.targetTheta = 45;
      this.turn(-100f,-angle); //turn constantly (left when rate positive) to angle
      this.status = Status.SCANNING;
   }

   /**overridden from Robot: contains the code for scanning */
   protected void scanning() {
      //while scanning get smallest ping value and corresponding theta
      int ping = uListener.getDistance();

      if(smallestPing > ping) {
         smallestPing = ping;
         targetTheta = this.getPosition().getDegrees();
      }
   }

}   
