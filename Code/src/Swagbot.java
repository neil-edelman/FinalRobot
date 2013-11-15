import lejos.nxt.SensorPort;
import lejos.util.Timer;
import lejos.nxt.Sound;

public class Swagbot extends Locobot {//Swagbot extends Localisingbot

   private UltrasonicListener uListener;
   private Timer uTimer;
   private float targetTheta = -1f;
   private int smallestPing = 254;
   private boolean findingFirst = true;
   private static final float DESTINATION_X = 0f;
   private static final float DESTINATION_Y = 30f;
   private static final int   SCAN_THRESHOLD = 70; 
   private static final float FRONT_EXTENSION_LENGTH = 22.5f; //bumper length from wheel base
   private static final float X_LOW_BOUND  = 0f      + FRONT_EXTENSION_LENGTH; //used as mins and maxes in determining target
   private static final float Y_LOW_BOUND  = 0f      + FRONT_EXTENSION_LENGTH; //origin in corner
   private static final float X_HIGH_BOUND = 121.92f - FRONT_EXTENSION_LENGTH; //4 tiles by
   private static final float Y_HIGH_BOUND = 243.84f - FRONT_EXTENSION_LENGTH; //8 tiles
   private float adjust_x = 30.48f; //designates the point on the field to be searched from
   private float adjust_y = 30.48f; //values are (0,0);(30,30);(30,60);(30,90)...etc
   private float targetX,targetY;
   private Position storeTarget;
   private FindStatus storeFindStatus;

	/** the constructor */
   public Swagbot(final SensorPort sonicPort, final SensorPort colourPort, final SensorPort lightPort) {
      super(sonicPort,colourPort,lightPort);
      uListener = new UltrasonicListener(this.sonic);
      uTimer = new Timer(10/*round-up to int 9.375*/,uListener); //timeout value in ms
      uTimer.start();
      Sound.setVolume(100);
   }

   //**********************************
   //override color hack -- for demo, color in locobot not working -- TODO:can remove this when we get it working
   Colour colour = new Colour(SensorPort.S3); 
   public Colour.Value getColour() {
      return colour.getColourValue();
   }
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
         set's the robot status to finding and the finding status to scanning.*/
   public void findBlocks() {
      this.status = Status.FINDING;
      this.findStatus = FindStatus.TURNING;
      findingFirst = true;
   }

   /** overridden from Robot: contains the code for finding blocks and placing them in the destination */
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
            this.travelTo(targetX-15,targetY-15,250,250); //backup
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
   /** overwritten from Robot, avoidance maneuver runs when uDistance less than threshold */
   protected void avoidance(int threshold) {
      int ping = uListener.getDistance();
      if(ping < threshold) {
         //need to remember where the robot was going
         storeTarget = this.getTarget();  
         storeFindStatus = this.findStatus;
         //determine obstacle position
         Position pos = this.getPosition();
         //turn avoidance on
         this.findStatus = FindStatus.AVOIDING;
         this.status = Status.FINDING;
         //blocks while avoiding
         //determines alternate point by shifting angle to obstacle by 45 left or right depending on orientation in the field and resumes
/*         if((int)pos.x < X_LOW_BOUND/2) {
            this.travelTo(pos.x,pos.y);
         }
         else {
            this.travelTo(pos.x,pos.y);
         }
*/ 
         float avoidAngleAdjust = 30f;
         float avoidX = pos.x + (ping)/2*(float)Math.cos(Math.toRadians(pos.getDegrees() + avoidAngleAdjust));
         float avoidY = pos.y + (ping)/2*(float)Math.sin(Math.toRadians(pos.getDegrees() + avoidAngleAdjust));
         backup();
         this.travelTo(avoidX,avoidY);
         //revert back to old state
         this.findStatus = storeFindStatus;
         travelTo(storeTarget.x,storeTarget.y);
      }
   }
   public void backup() {
      this.setSpeeds(-100,-100);

   }

} 
