import java.lang.IllegalArgumentException;

import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

import lejos.nxt.Sound;
import lejos.nxt.SensorPort;
import lejos.nxt.LightSensor;
import lejos.nxt.UltrasonicSensor;

import lejos.util.Timer;
import lejos.util.TimerListener;
import lejos.nxt.Button;

/* Robot */

class Robot implements TimerListener {

	public enum Status { IDLE, ROTATING, TRAVELLING, LOCALISING };

	public final static String    NAME = "Sex Robot"; /* change */
	private static final int NAV_DELAY = 100; /* ms */

	private static final float             ANGLE_TOLERANCE = (float)Math.toRadians(0.1); /* rad */
	private static final float    ANGLE_MARGINAL_TOLERANCE = 2.0f; /* rad/s */
	private static final float          DISTANCE_TOLERANCE = 1f; /* cm */

	private static final NXTRegulatedMotor leftMotor  = Motor.A;
	private static final NXTRegulatedMotor rightMotor = Motor.B;

	/* Ziegler-Nichols method was used to get close to the optimum */
	private Controller    anglePID = new Controller(0.6f * 2077f, 0.6f * 2077f / 1000f, 0.6f * 2077f * 1000f / 8f, -350, 350);
	/* fixme!!!! this has not been optimised */
	private Controller distancePID = new Controller(10f, 0f, 0f, -250, 250);

	protected Status     status = Status.IDLE;
	private Odometer odometer = new Odometer(leftMotor, rightMotor);
	private Position   target = new Position(), delta = new Position();

	private Timer timer = new Timer(NAV_DELAY, this);

	/** the constructor */
	public Robot() {
		/* (?) leftMotor.setAccelertion(3000);
		rightotor.setAccelertion(3000);*/
		timer.start();
	}

	public Position getPosition() {
		return odometer.getPositionCopy();
	}

	/* odometer is syncronised */
	public void setPosition(Position position) {
		odometer.setPosition(position);
	}

	public Status getStatus() {
		return status;
	}

	public Position getTarget() {
		synchronized(this) {
			return target;
		}
	}

	/** this acts as the control; selects based on what it is doing */
	public void timedOut() {
      switch(status) { //idle, rotating, traveling
			case IDLE:
				break;
			case ROTATING:
				this.rotate();
				break;
			case TRAVELLING:
				this.travel();
				break;
		}
	}

	/** this shuts down all components that have timers, etc */
	public void shutdown() {
		odometer.shutdown();
		status = Status.IDLE;
	}

	/** this sets the target to a (-180,180] degree and turns */
	public void turnTo(final float degrees) {
		if(degrees <= -180 || degrees > 180) throw new IllegalArgumentException();

		/* anglePID, which we need, could have old values, reset it */
		anglePID.reset();

		/* set the target's angle and set rotate (the timedOut method will call
		 turn until it turns or is stopped) */
		target.setTheta((float)Math.toRadians(degrees));
		status = Status.ROTATING;
	}

	/** this sets the target to (x, y) and travels */
	public void travelTo(final float x, final float y) {

		/* distance and angle need to be reset (we use them) */
		distancePID.reset();
		anglePID.reset();

		/* fixme: we should do a thing here that sets the line perp to the
		 dest for travelTo oscillations */

		/* we set distance, TRAVELLING, and let timedOut's travel do the rest */
		target.x = x;
		target.y = y;
		status = Status.TRAVELLING;
	}

	/** this implements a rotation by parts */
	private void rotate() {
		/* calculate the delta beteen the target and the current */
		Position current = odometer.getPositionCopy();
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
		Position current = odometer.getPositionCopy();
		delta.subXY(target, current);
		target.setTheta((float)Math.atan2(delta.y, delta.x));
		delta.subTheta(target, current);
		float distance = (float)Math.sqrt(delta.x*delta.x + delta.y*delta.y);

		/* apply magic */
		float turn  = anglePID.nextOutput(delta.getTheta(), NAV_DELAY);
		float speed = distancePID.nextOutput(distance,      NAV_DELAY);
		// haven't decided where to put this: * Math.cos(Math.toRadians(p.r));

		/* tolerence on the distance */
		if(distancePID.isWithin(DISTANCE_TOLERANCE)) {
			this.stop();
			status = Status.IDLE;
			return;
		}

		/* set */
		this.setSpeeds(speed - turn, speed + turn);
	}

	/** what should be printed when our robot is called eg in printf */
	public String toString() {
		return NAME + /*this.hashCode()+*/" is " + status + " at " + odometer;
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

	/** [emergency/idle] stop */
	public void stop() {
		leftMotor.stop();
		rightMotor.stop();
	}

}
