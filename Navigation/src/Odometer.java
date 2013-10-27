/* Odometer running in background will keep track of proprioception.
 fixme: if we run faster, the period will have to decrese */

import lejos.util.Timer;
import lejos.util.TimerListener;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

public class Odometer implements TimerListener {

	private static final int   ODO_DELAY = 25;
	private static final float PI        = (float)Math.PI;
	private static final float RADIUS    = 2.72f;
	private static final float WHEELBASE = 16.15f;

	private final NXTRegulatedMotor leftMotor, rightMotor;

	private Timer timer = new Timer(ODO_DELAY, this);

	/* these are tach values, always from when the programme started;
	 they are better then floats because using floats for this is numerecally
	 unstable! when the robot moves around a lot, the odometer will be less
	 and less precise and eventually will cause a floating point overflow;
	 ints just loop back */
	int intTraveled, intTurn;

	Position position = new Position();
	Position    pCopy = new Position();

	/** constructor */
	public Odometer(final NXTRegulatedMotor leftMotor, final NXTRegulatedMotor rightMotor) {
		this.leftMotor  = leftMotor;
		this.rightMotor = rightMotor;
		timer.start();
	}

	public void shutdown() {
		timer.stop();
	}

	public void timedOut() {
		/* get tach values */
		int  left = leftMotor.getTachoCount();
		int right = rightMotor.getTachoCount();

		/* translate into traveled and turned (in some units) */
		int intTraveled = right + left;
		int intTurn     = right - left;

		/* subtract off the accumulated */
		intTraveled -= this.intTraveled;
		intTurn     -= this.intTurn;

		/* get it in real world units */
		/* (i/2) * (2Pi r) * (1/360) */
		float d = (intTraveled) * (PI * RADIUS) / (360f); /* cm */
		/* (i/2) / (w/2) * (2Pi r) * (1/360) */
		float r = (intTurn / WHEELBASE) * (PI * RADIUS) / (180f); /* radians */

		/* add it to the position at which the robot thinks it is */
		synchronized(this) {
			position.transform(r, d);
		}

		/* add it to the class variable */
		this.intTraveled += intTraveled;
		this.intTurn     += intTurn;
	}

	/** this gets a position copy so we can save the position for real-time
	 updates (position copy is assumed to be accessed one time) */
	public Position getPositionCopy() {
		synchronized(this) {
			pCopy.copy(position);
		}
		return pCopy;
	}

	public String toString() {
		synchronized(this) {
			return "Odo" + position;
		}
	}
}
