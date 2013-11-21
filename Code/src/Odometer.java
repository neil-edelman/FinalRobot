/** Odometer running in background will keep track of proprioception.
<p>
 The odometer runs in standard co-ordinates (ISO 80000-2:2009)
 with the branch cut (-Pi, Pi] (we don't want a branch cut in our small angle
 approxomations about zero) eg counter-clockwise radians from x; this is the
 same used in Math.atan2 and all trig and grade school. The travelTo (and
 turnTo) take degrees and convert them to radians; it's much faster converting
 them once vs converting atan2 10 times a second.
<p>
 This is the improved, non-numerically-unstable int math odometer.
<p>
 fixme: if we run the robot faster, the period should decrese
<p>
 The x and y orientation is identical to that used in the labs. However,
 theta=0 starts at the x-axis instead of the y-axis, and increments
 counterclockwise and opposed to clockwise. The conversion is
 old_theta = -new_theta + 90 and conversly new_theta = -old_theta + 90. -Alex
<p>
 from the TA:
 Coordinate System:
					  90 Deg: y axis                     
                            |                            
       positive theta       |      positive theta        
                            |                            
                            |                            
 180 Deg: -x axis __________|__________ 0 Deg: x axis
  branch cut (-180, 180]    |                            
                            |                            
                            |                            
       negative theta       |      negative theta        
                            |                            
                     -90 Deg: -y axis                    
 
 @author Neil */

import lejos.util.Timer;
import lejos.util.TimerListener;
import lejos.nxt.NXTRegulatedMotor;

public class Odometer implements TimerListener {

	private static final float PI        = (float)Math.PI;

	/* this is commented because now we have hardware profiles */
	/*private static final int   ODO_DELAY = 25;
	private static final float RADIUS    = 2.72f;
	private static final float WHEELBASE = 16.15f; //sex robot (version 1)
	private static final float WHEELBASE = 19.7f;  //swagbot (version 2) tested, works well */

	private final NXTRegulatedMotor leftMotor, rightMotor;

	private Timer timer = new Timer(Hardware.odoDelay, this);

	/* these are tach values, always from when the programme started;
	 they are better then floats because using floats for this is numerecally
	 unstable! when the robot moves around a lot, the odometer will be less
	 and less precise and eventually will cause a floating point overflow;
	 ints just loop back */
	private int intTraveled, intTurn;

	Position position = new Position();
	Position      old = new Position();
	Position    pCopy = new Position();

	/** constructor
	 @param leftMotor
	 @param rightMotor NXTRegulated motors that are attached to wheels that are
	 RADIUS and WHEELBASE apart (potentially calibrated using
	 CalibrateRadius/Wheelbase) */
	public Odometer() {
		this.leftMotor  = Hardware.leftMotor;
		this.rightMotor = Hardware.rightMotor;
		timer.start();
	}

	/** shuts down the odometer so it isn't taking data (it currently has no
	 method to start it again) */
	public void shutdown() {
		timer.stop();
	}

	/** this goes every ODO_DELAY ms until it is shutdown(); just adds the
	 little bit to the odometer */
	public void timedOut() {

		/* get tach values (cm*360) */
		int  left = leftMotor.getTachoCount();
		int right = rightMotor.getTachoCount();

		/* translate into traveled and turned (in some int units) */
		int delTraveled = right + left;
		int delTurn     = right - left;

		/* subtract off the accumulated */
		delTraveled -= this.intTraveled;
		delTurn     -= this.intTurn;

		/* get it in real world units */
		/* (i/2) * (2Pi r) * (1/360) */
		float d = (delTraveled) * (PI * Hardware.radius) / (360f); /* cm */
		/* (i/2) / (w/2) * (2Pi r) * (1/360) */
		float r = (delTurn / Hardware.wheelbase) * (PI * Hardware.radius) / (180f); /* radians */

		/* add it to the position at which the robot thinks it is */
		synchronized(this) {
			position.arc(r, d);
			/* I know this isn't neccessary, just alternate, but it results in
			 unreadability */
			old.set(position);
		}

		/* add it to the class variable */
		this.intTraveled += delTraveled;
		this.intTurn     += delTurn;
	}

	/** this stores a copy of the actal position (which is volitale) in pCopy;
	 pCopy is called when reading position . . . viz you must call this to
	 update position used in getPosition, but only once per movement; you will
	 probably want to call premonitionUpdate() instead to get rid of one-sided
	 errors
	 @deprecated
	 @see #premonitionUpdate() */
	public void positionSnapshot() {
		synchronized(this) {
			pCopy.set(position);
		}
	}

	/** this eliminates one-sided errors by predicting the future; also
	 this stores a copy of the actal position (which is volitale) in pCopy;
	 pCopy is called when reading position . . . you must call this instead
	 of positionSnapshot */
	public void premonitionUpdate() {
		synchronized(this) {
			pCopy.expectition(position, old);
		}
	}

	/** returns the last position when positionSnapshot was taken
	 @return The position when the last snapshot was taken. */
	public Position getPosition() {
		return pCopy;
	}

	/* setters */

	/** sets the position
	 @param pos position object */
	public void setPosition(final Position pos) {
		synchronized(this) {
			position.set(pos);
			old.set(pos);
		}
	}
	
	/* deprecated */

	/*public void setRadians(final float t) {
		synchronized(this) {
			position.setRadians(t);
		}
	}
	public void setDegrees(final float deg) {
		synchronized(this) {
			position.setTheta((float)Math.toRadians(deg));
		}
	}
	public void setXY(final float x, final float y) {
		synchronized(this) {
			position.setXY(x, y);
		}
	}*/

	/** adds the radians directly to the current value
	 @param t [-PI, PI) */
	public void addRadians(final float t) {
		synchronized(this) {
			position.addRadians(t);
			old.addRadians(t);
		}
	}

	/** adds (x, y) directly to the current value
	 @param x
	 @param y The values to add. */
	public void addXY(final float x, final float y) {
		synchronized(this) {
			position.addXY(x, y);
			old.addXY(x, y);
		}
	}

	/** prints the postion when the last snapshot was taken
	 @return string */
	public String toString() {
		return "Odo" + pCopy;
	}

}
