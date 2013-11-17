import java.lang.IllegalArgumentException;

import lejos.nxt.Motor; /* workaround for nxj error */
import lejos.nxt.NXTRegulatedMotor;

import lejos.nxt.Sound;
import lejos.nxt.SensorPort;
import lejos.nxt.LightSensor;
import lejos.nxt.UltrasonicSensor;

import lejos.util.Timer;
import lejos.util.TimerListener;
import lejos.nxt.Button;

/* Robot */

public class Robot implements TimerListener {

	/* should be in Driver, but causes crash in nxj api; just hard code */
	private static final NXTRegulatedMotor  leftMotor = Motor.A;
	private static final NXTRegulatedMotor rightMotor = Motor.B;

	public enum Status { IDLE, ROTATING, TRAVELLING, LOCALISING, SCANNING, FINDING };
   public enum FindStatus { IDLE, TURNING, SCANNING, SCANNED, ID, FOUND, RELOCATING, FINISHED, AVOIDING };

	private   final static String   NAME = "Locobot";
	protected static final int NAV_DELAY = 100; /* ms */

	private static final float             ANGLE_TOLERANCE = (float)Math.toRadians(0.1); /* rad */
	private static final float    ANGLE_MARGINAL_TOLERANCE = 2.0f; /* rad/s */
	private static final float          DISTANCE_TOLERANCE = 1f; /* cm */
   private static final boolean  avoid = true;

   private float turnRate;

	private static final float    DEFAULT_LIMIT_ANGLE = 350f;
	private static final float DEFAULT_LIMIT_DISTANCE = 350f;

	/* Ziegler-Nichols method was used to get close to the optimum;
	 the battery voltage causes some lag when low */
	private Controller    anglePID = new Controller(0.6f * 2077f, 0.6f * 2077f / 1000f, 0.6f * 2077f * 1000f / 8f);
	/* fixme!!!! this has not been optimised */
	private Controller distancePID = new Controller(30f, 2f, 1f);

	protected Status     status = Status.IDLE;
   protected FindStatus findStatus = FindStatus.IDLE;
	protected Position   target = new Position(), delta = new Position();
	protected Odometer odometer;

	protected Timer timer = new Timer(NAV_DELAY, this);

	/** the constructor */
	public Robot() {
		odometer = new Odometer(leftMotor, rightMotor);
		/* set smooth -- DO NOT DO THIS IT MAKES IT LOCO; figure-8's, crashing
		 on walls, etc; who know what it does, but it's NOT a accelertion
		 limiter */
		/*leftMotor.setAcceleration(500);
		rightMotor.setAcceleration(500);*/
		/* start the timer for updates (timedOut) */
		timer.start();
	}

	/** this shuts down all components that have timers, etc */
	public void shutdown() {
		odometer.shutdown();
		status = Status.IDLE;
	}

	/** this acts as the control; selects based on what it is doing as a state
	 machine */
	public void timedOut() {
		/* get the latest from the odometer */
		/*odometer.positionSnapshot();*/
		/* forget that, our robot can predict the future */
		odometer.premonitionUpdate();
		/* state machine */
		switch(status) {
			case TRAVELLING:
            if(avoid) {
                //avoidance when relocating to next scan point or when taking a block to the destination (i.e. not when moving to block or scanning)
                if( findStatus == FindStatus.RELOCATING || findStatus == FindStatus.FINISHED ) {
                   this.avoidance(30); 
                }
                //assuming its ID'ing and set lower limit
                else {
                   //this.avoidance(15);
                }
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
   /** this sets a constant turing speed to angle (used in scanning) */
   //position rate turns the robot left, negitive turns the robot right, to the specified angle
   protected void turn(final float rate, final float angle) {
      this.setSpeeds(-rate,rate);
      target.setDegrees(angle);
      status = Status.SCANNING;
      this.turnRate = rate;
   }
   /** used with scanning, allows the robot to constantly turn to an angle */
   private void constantlyTurningTo() {
      Position p = odometer.getPosition();
      float angle = p.getDegrees();
      //turning right
      if(this.turnRate < 0f && target.getDegrees() > angle) {
         status = Status.IDLE;
         this.stop();
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

	/** this is a shorcut to just specify the DEFAULT_LIMIT_ANGLE */
	public void turnTo(final float degrees) {
		this.turnTo(degrees, DEFAULT_LIMIT_ANGLE);
	}

	/** this sets the target to a (-180,180] degree and the speed limit, turns */
	public void turnTo(final float degrees, final float limit) {
		if(degrees <= -180 || degrees > 180) throw new IllegalArgumentException();

		/* anglePID, which we need, could have old values, reset it */
		anglePID.reset(limit);

		/* set the target's angle and set rotate (the timedOut method will call
		 turn until it turns or is stopped) */
		target.setDegrees(degrees);
		status = Status.ROTATING;
	}

	/** this sets the target to (x, y) and travels */
	public void travelTo(final float x, final float y) {

		/* distance and angle need to be reset (we use them) */
		anglePID.reset(DEFAULT_LIMIT_ANGLE);
		distancePID.reset(DEFAULT_LIMIT_DISTANCE);

		/* fixme: we should do a thing here that sets the line perp to the
		 dest for travelTo oscillations */

		/* we set distance, TRAVELLING, and let timedOut's travel do the rest */
		target.x = x;
		target.y = y;
		status = Status.TRAVELLING;
	}

	/** this sets the target to (x, y) and the travel and turn limits and travels */
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
		float right = anglePID.nextOutput(delta.getTheta(), NAV_DELAY);

		/* the PID control goes forever, but it's good enough within this
		 tolerence (angle, derivative) then STOP */
		if(anglePID.isWithin(ANGLE_TOLERANCE, ANGLE_MARGINAL_TOLERANCE)) {
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
		float turn  = anglePID.nextOutput(delta.getTheta(), NAV_DELAY);
		float speed = distancePID.nextOutput(distance,      NAV_DELAY);

		/* was going to put Math.cos(delta.getRadians()) to get lightning fast
		 turns when starting away from the destiantion; a glaring bug with the
		 firmwear causes the 2nd motor, when swiching signs, to stop, but the
		 report back to the odometer that it's kept going */

		/* tolerence on the distance; fixme: have a tolerance on the derivative
		 as soon as it won't go crazy and turn 180 degrees on overshoot */
		if(distancePID.isWithin(DISTANCE_TOLERANCE)) {
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
   /** complementary method: runs in parallel with travel */
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
		return NAME;
	}

	/** what should be printed when our robot is called eg in printf */
	public String toString() {
		synchronized(this) {
			return NAME /*+ this.hashCode()*/ + " is " + status + " at " + odometer;
		}
	}

	/* output functions */

   public void setPosition(Position pos) {
      odometer.setPosition(pos);
   }

	/** set r/l speeds indepedently */
	protected void setSpeeds(final float l, final float r) {
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

	/** [emergency/idle] stop (fixme: protected?) */
	public void stop() {
		leftMotor.stop();
		rightMotor.stop();
	}

}
