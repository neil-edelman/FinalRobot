/** The Robot class provides the basic functionality of the robot (turnTo and
 travelTo as well as getters and setters to the odometer)
 as well as the structural framework for classes that extend it such as
 Swagbot. It is a state machine that stores the status of the
 robot in the status variable of type Status.The findStatus variable and enum
 are used by Swagbot when the robot is finding blocks.
 The case statments in the timedOut method explain the flow of the program.
<p> 
 The Robot class contains the enums Status and FindStatus. A case statement
 in the timedOut method of this class calls the method corresponding to the
 robot’s state. This method will contain the suffix "ing", which denotes that
 it is constantly called for the duration of the given state and will check
 conditions to determine when to given task has been completed. For example
 when the robot’s status is IDLE, it checks its status every timedOut and then
 breaks from the case statement, doing nothing else. Calling the turnTo method
 will set the robot’s status  to ROTATING and the target heading to turn to.
 Following timedOut’s will call the turning method, which checks the robot's
 heading relative to the target heading. When the robot has turned within the
 angle threshold of the target angle, the robot’s status is set back to IDLE.
<p>
Status<hr>
states: IDLE, ROTATING, TRAVELLING, LOCALISING, SCANNING, FINDING 
 <p>
FindStatus <hr>
states: IDLE, TURNING, SCANNING, SCANNED, ID, FOUND, RELOCATING, FINISHED, AVOIDING
<p>
Status is used by the Robot class whereas FindStatus is inherited and used by
 Swagbot.
<p>
 this is the basic robot; just methods for moving around
 @author Neil, Alex */

import java.lang.IllegalArgumentException;

import lejos.nxt.NXTRegulatedMotor;

import lejos.nxt.Sound;
import lejos.nxt.SensorPort;
import lejos.nxt.LightSensor;
import lejos.nxt.UltrasonicSensor;

import lejos.util.Timer;
import lejos.util.TimerListener;
import lejos.nxt.Button;

//import lejos.nxt.comm.RConsole;

/* Robot */

public class Robot implements TimerListener {

	public enum Status { IDLE, ROTATING, TRAVELLING, LOCALISING, SCANNING, FINDING };
   public enum FindStatus { IDLE, TURNING, SCANNING, SCANNED, ID, FOUND, RELOCATING, FINISHED, AVOIDING };

	private static NXTRegulatedMotor leftMotor, rightMotor;
   private static final boolean  avoid = false;

   private float turnRate;

	/* Ziegler-Nichols method was used to get close to the optimum;
	 the battery voltage causes some lag when low */
	/* classic (aggresive) control; we want errors in the angle corrected fast */
	private Controller    anglePID = new Controller(0.6f * 2077f, 0.6f * 2077f / 1000f, 0.6f * 2077f * 1000f / 8f);
	/* fixme!!!! this has not been optimised */
	private Controller distancePID = new Controller(30f, 2f, 1f);

	protected Status     status = Status.IDLE;
   protected FindStatus findStatus = FindStatus.IDLE;
	protected Position   target = new Position(), delta = new Position();
	protected Odometer odometer;

	protected Timer timer = new Timer(Hardware.navDelay, this);

	/** the constructor; make sure you set the hardware profile first! */
	public Robot() {
		leftMotor  = Hardware.leftMotor;
		rightMotor = Hardware.rightMotor;
		odometer = new Odometer();
		/* set smooth -- DO NOT DO THIS IT MAKES IT LOCO; figure-8's, crashing
		 on walls, etc; who know what it does, but it's NOT a accelertion
		 limiter */ //TODO:lol -alex
		/*leftMotor.setAcceleration(500);
		rightMotor.setAcceleration(500);*/
		/*anglePID.limitAcceleration(0.05f);
		distancePID.limitAcceleration(0.05f); <- nope */
		/* start the timer for updates (timedOut) */
		timer.start();
	}

	/** this shuts down all components that have timers, etc */
	public void shutdown() {
		odometer.shutdown();
		status = Status.IDLE;
	}

	/** this acts as the control called every navDelay ms; selects based on
	 what it is doing as a state machine; we were going to make it more
	 complex, but it works well */
	public void timedOut() {
		/* get the latest from the odometer */
		/*odometer.positionSnapshot();*/
		/* forget that, our robot can predict the future; it's like that ep of
		 Stargate, Avatar, where T'elc can see, in this case, 7.5 ms into the
		 future in the simulation */
		odometer.premonitionUpdate();
		/* state machine */
		switch(status) {
			case TRAVELLING:
            if(avoid) {
               this.avoidance(30);
//                //avoidance when relocating to next scan point or when taking a block to the destination (i.e. not when moving to block or scanning)
//                if( findStatus == FindStatus.RELOCATING || findStatus == FindStatus.FINISHED ) {
//                   this.avoidance(20); 
//                }
//                //assuming its ID'ing and set lower limit
//                else {
//                   //this.avoidance(15);
//                }
            }
				this.travel();
				break;
			case ROTATING:
				this.rotate();
				break;
			case LOCALISING:
				this.localising();
				break;
         case SCANNING:
            this.constantlyTurningTo();
            this.scanning();
            break;
         case FINDING:
            this.finding();
            break;
			case IDLE:
            if(findStatus != FindStatus.IDLE)
               status = Status.FINDING;
				break;
		}
	}

	/****************** this is what's exposed to public or protected;
	 you should set the appropiate status and the private methods called from
	 timedOut do the rest *****************/

	/** set localising */
	protected void localise() {
		status = Status.LOCALISING;
	}

	/** this sets a constant turning speed (used in localising;) don't need to
	 set status as it is not controlled; as usual, turn(+) is left (increasing
	 theta) and turn(-) is right (decreasing theta) */
	protected void turn(final float rate) {
		/* sketchy; you should only be calling it as part of a loco routine */
		if((status == Status.ROTATING) || (status == Status.TRAVELLING)) {
			status = Status.IDLE;
		}
		this.setSpeeds(-rate, rate);
	}
   /** this sets a constant turing speed to angle (used in scanning)
	<p>
   position rate turns the robot left, negitive turns the robot right, to the
	specified angle
	@param rate specifies the rate at which the turn happens
	@param angle the angle in degrees (-180,180] that you want to turn to */
   protected void turn(final float rate, final float angle) {
      this.setSpeeds(-rate,rate);
      target.setDegrees(angle);
      status = Status.SCANNING;
      this.turnRate = rate;
   }
   /** used with scanning, allows the robot to constantly turn to an angle
	(use :turn to initate) */
   private void constantlyTurningTo() {
      Position p = odometer.getPosition();
      float angle = p.getDegrees();
      //turning right
      if(this.turnRate < 0f) {
         float left = target.getTheta() - 45f;
         if(left < 0f)
            left += 360f;
         if( (angle < target.getDegrees() || angle < 0) && (angle > left || angle < 0) ) {
            status = Status.IDLE;
            this.stop();
         }
      }
      //turning left
      if(this.turnRate > 0f) {
         float right = target.getTheta() + 45f;
         if(right > 180f)
            right -= 360f;
         if( (angle > target.getDegrees() || angle < 0) && (angle < right || angle > 0) ) {
            status = Status.IDLE;
            this.stop();
         }
      }
   }

	/** this is a shorcut to just specify the DEFAULT_LIMIT_ANGLE
	 @param degrees (-180,180] */
	public void turnTo(final float degrees) {
		this.turnTo(degrees, Hardware.defaultLimitAngle);
	}

	/** this sets the target to a (-180,180] degree and the speed limit, turns
	 @param degrees (-180,180]
	 @param limit sets the speed limit */
	public void turnTo(final float degrees, final float limit) {
		if(degrees <= -180 || degrees > 180) {
			throw new IllegalArgumentException("turnTo " + degrees);
		}

		/* anglePID, which we need, could have old values, reset it */
		anglePID.reset(limit);

		/* set the target's angle and set rotate (the timedOut method will call
		 turn until it turns or is stopped) */
		target.setDegrees(degrees);
		status = Status.ROTATING;
	}

	/** this sets the target to (x, y) in cm and travels
	 @param x
	 @param y */
	public void travelTo(final float x, final float y) {

		/* distance and angle need to be reset (we use them) */
		anglePID.reset(Hardware.defaultLimitAngle);
		distancePID.reset(Hardware.defaultLimitDistance);

		/* fixme: we should do a thing here that sets the line perp to the
		 dest for travelTo oscillations */

		/* we set distance, TRAVELLING, and let timedOut's travel do the rest */
		target.x = x;
		target.y = y;
		status = Status.TRAVELLING;
	}

	/** this sets the target to (x, y) in cm and the travel and turn limits and
	 travels
	 @param x
	 @param y
	 @param travelLimit the speed limit
	 @param turnLimit   the speed limit */
	public void travelTo(final float x, final float y, final float travelLimit, final float turnLimit) {

		/* distance and angle need to be reset (we use them) */
		anglePID.reset(turnLimit);
		distancePID.reset(travelLimit);

		/* fixme: we should do a thing here that sets the line perp to the
		 dest for travelTo oscillations */

		/* we set distance, TRAVELLING, and let timedOut's travel do the rest */
		target.x = x;
		target.y = y;
		status = Status.TRAVELLING;
	}

	/*************** this section is the private (or protected in cases where
	 you are meant to override the method) methods used for the above; these
	 methods are (should be) called in timedOut ******************/

	/** the other robots' extending it (ie Locobot?) will override this method */
	protected void localising() {
		System.err.println("no localising");
		status = Status.IDLE;
	}

	/** this implements a rotation by parts */
	private void rotate() {
		/* calculate the delta beteen the target and the current */
		Position current = odometer.getPosition();
		delta.subTheta(target, current);

		/* apply magic (PID control, input the error and the time, output what
		 the value should be so it gets to the setpoint fastest, in this case,
		 the right wheel; the left is the inverse) */
		float right = anglePID.nextOutput(delta.getTheta(), Hardware.navDelay);

		/* the PID control goes forever, but it's good enough within this
		 tolerence (angle, derivative) then STOP */
		if(anglePID.isWithin(Hardware.angleTolerance, Hardware.angleMarginalTolerance)) {
			this.stop();
			status = Status.IDLE;
			return;
		}

		/* set */
		this.setSpeeds(-right, right);
	}

	/** travels to a certain position */
	private void travel() {
		/* calculate the angle from the current heading to the desired heading
		 and the speed; fixme: if it goes over, it has to come at it again:
		 signed distance (but that opens up a whole can of worms) */
		Position current = odometer.getPosition();
		delta.subXY(target, current);
		target.setRadians((float)Math.atan2(delta.y, delta.x));
		delta.subTheta(target, current);
		/* test: (forget it, I'll just use sqrt) excessively slow on some
		 machines, we are going no where near overflow */
		/*float distance = (float)Math.hypot(delta.x, delta.y);*/
		float distance = (float)Math.sqrt(delta.x*delta.x + delta.y*delta.y);

		/* apply magic */
		float turn  = anglePID.nextOutput(delta.getTheta(), Hardware.navDelay);
		float speed = distancePID.nextOutput(distance, Hardware.navDelay) *
		              (float)Math.cos(delta.getRadians());

		/* tolerence on the distance; fixme: have a tolerance on the derivative
		 as soon as it won't go crazy and turn 180 degrees on overshoot */
		if(distancePID.isWithin(Hardware.distanceTolerance)) {
			this.stop();
			status = Status.IDLE;
			return;
		}

		/* set */
		this.setSpeeds(speed - turn, speed + turn);
	}

   /************Overwritten Subsection**********
         the following methods are called in timedout
         and need to be overwritten to function */

   /** overwritten for scanning */
   protected void scanning() {
		System.err.println("no scanning");
      status = Status.IDLE;
   }
   /** overwritten for finding blocks */
   protected void finding() {
      System.err.println("no finding blocks");
      status = Status.IDLE;
   }
   /** complementary method: runs in parallel with travel
	@param threshold in cm */
   protected void avoidance(int threshold) {
      System.err.println("no avoidance");
   }


	/**************************************/

   /** overwritten for display */

   protected int getFilteredDistance() {
      return 0;
   }
   protected int getDistance() {
      return 0;
   }

	/**************************************/

	/* accesors/modifiers */

	/** pass this on to the odometer */
	public Position getPosition() {
		return odometer.getPosition();
	}
   /** returns the robot's status */
	public Status getStatus() {
		synchronized(this) {
			return status;
		}
	}
   public FindStatus getFindStatus() {
      synchronized(this) {
         return findStatus;
      }
   }
   /** returns the target position */
	public Position getTarget() {
		synchronized(this) {
			return target;
		}
	}

	/** returns conatant */
	public String getName() {
		return Hardware.name;
	}

	/** what should be printed when our robot is called eg in printf */
	public String toString() {
		synchronized(this) {
			return Hardware.name /*+ this.hashCode()*/ + " is " + status + " at " + odometer;
		}
	}

	/* output functions */

	/** correct the position
	 @param pos the new position */
   public void setPosition(Position pos) {
      odometer.setPosition(pos);
   }

	/** set r/l speeds indepedently; doesn't always work
	 fixme: more testing, extensively
	 1) hardware?
	 2) chosen after? swap
	 3) brick ports error?
	 4) firmware error?
	 5) nxj error?
	 ahh I figured it out, I think, the motors were stalling due to low battery
	 voltage; that's what you get for using an acutator as a motor; fixed in
	 Controller; untested
	 @param final float l  left speed degrees/sec
	 @param final float r  right speed degrees/sec */
	protected void setSpeeds(final float l, final float r) {
		/* this is a hack, but better than the the more complex solution */
		/*if(l > Hardware.maxSpeed)       l =  Hardware.maxSpeed;
		else if(l < -Hardware.maxSpeed) l = -Hardware.maxSpeed;
		if(r > Hardware.maxSpeed)       r =  Hardware.maxSpeed;
		else if(r < -Hardware.maxSpeed) r = -Hardware.maxSpeed;
		 okay, the problem is definately an NXT error */

		/* send? */
		/*if(RConsole.isOpen()) {
			RConsole.println("" + l + ", " + r);
		}*/
		
		leftMotor.setSpeed(l);
		if(l > 0) {
			leftMotor.forward();
		} else if(l < 0) {
			leftMotor.backward();
		} else {
			leftMotor.stop();
		}
		rightMotor.setSpeed(r);
		if(r > 0) {
			rightMotor.forward();
		} else if(r < 0) {
			rightMotor.backward();
		} else {
			rightMotor.stop();
		}
	}

	/** [emergency/idle] stop */
	protected void stop() {
		leftMotor.stop();
		rightMotor.stop();
	}

}
