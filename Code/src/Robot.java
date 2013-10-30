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

////

/* Robot */

class Robot implements TimerListener {

	public enum Status { IDLE, ROTATING, TRAVELLING, LOCALISING };

	public final static String    NAME = "Sex Robot";
	private static final int NAV_DELAY = 100; /* ms */

	private static final float    ANGLE_TOLERANCE = (float)Math.toRadians(0.1);
	private static final float DISTANCE_TOLERANCE = 1f;
	
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
   public void setPosition(Position position) {
      synchronized(this) {
         odometer.setPosition(position);
      }
   }

	public Status getStatus() {
		return status;
	}


   public Position getTarget() {
      synchronized(this) {
         return target;
      }
   }





	/** this acts as the control */
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

	public void shutdown() {
		odometer.shutdown();
		status = Status.IDLE;
	}

	/** this sets the target to a (-180,180] degree and turns */
	public void turnTo(final float degrees) {
		if(degrees <= -180 || degrees > 180) throw new IllegalArgumentException();

		anglePID.reset();

		target.r = (float)Math.toRadians(degrees);
		status = Status.ROTATING;
	}

	/** this sets the target to (x, y) and travels */
	public void travelTo(final float x, final float y) {

		distancePID.reset();
		anglePID.reset();

		target.x = x;
		target.y = y;
		status = Status.TRAVELLING;
	}

	/** this implements a rotation by parts */
	private void rotate() {
		/* calculate the delta beteen the target and the current and apply magic */
		Position current = odometer.getPositionCopy();
		delta.subR(target, current);
		float right = anglePID.nextOutput(delta.r, NAV_DELAY);

		/* tolerence */
		if(anglePID.isWithin(ANGLE_TOLERANCE, 1)) {
			this.stop();
			status = Status.IDLE;
			return;
		}

		/* set */
		this.setSpeeds(-right, right);
	}

	/** travels to a certain position */
	private void travel() {
		/* calculate */
		Position current = odometer.getPositionCopy();
		delta.subXY(target, current);
		target.r = (float)Math.atan2(delta.y, delta.x);
		delta.subR(target, current);
		float distance = (float)Math.sqrt(delta.x*delta.x + delta.y*delta.y);

		float turn = anglePID.nextOutput(delta.r, NAV_DELAY);

		float speed = distancePID.nextOutput(distance, NAV_DELAY);// * Math.cos(Math.toRadians(p.theta));

		/* tolerence */
		if(distancePID.isWithin(DISTANCE_TOLERANCE)) {
			this.stop();
			status = Status.IDLE;
			return;
		}

		/* set */
		this.setSpeeds(speed - turn, speed + turn);
	}

	public String toString() {
		return NAME + /*this.hashCode()+*/" is " + status;
	}


	/** set r/l speeds indepedently */
	private void setSpeeds(final float l, final float r) {
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
