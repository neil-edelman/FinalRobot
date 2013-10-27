import java.lang.IllegalArgumentException;

import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

import lejos.util.Timer;
import lejos.util.TimerListener;

import lejos.nxt.Button;

////
import lejos.nxt.LCD;

/* Robot */

class Robot implements TimerListener {

	enum Status { IDLE, ROTATING, TRAVELLING };

	public final static String    NAME = "Sex Robot";
	private static final int NAV_DELAY = 100; /* ms */

	private static final float    ANGLE_TOLERANCE = (float)Math.toRadians(1.0);
	private static final float DISTANCE_TOLERANCE = 1f;

	private Controller           anglePID = new Controller(5f, 1f, 1f, -250, 250);
	private Controller        distancePID = new Controller(10f, 1f, 1f, -250, 250);

	private Status     status = Status.IDLE;
	private Odometer odometer = new Odometer(leftMotor, rightMotor);
	private Position   target = new Position(), delta = new Position();

	private Timer timer = new Timer(NAV_DELAY, this);

	/** the constructor */
	public Robot() {
		System.out.println(NAME);
		/* (?) leftMotor.setAccelertion(3000);
		rightotor.setAccelertion(3000);*/
		timer.start();
	}

	public Position getPosition() {
		return odometer.getPositionCopy();
	}

	public Status getStatus() {
		return status;
	}

	/** this acts as the control */
	public void timedOut() {
		/* what is it doing? */
		switch(status) {
			case IDLE:
				break;
			case ROTATING:
				this.rotate();
				LCD.drawString("Ro:R "+odometer+";", 0, 0);
				break;
			case TRAVELLING:
				this.travel();
				LCD.drawString("Ro:T "+odometer+";", 0, 0);
				break;
		}
	}

	public void shutdown() {
		odometer.shutdown();
		status = Status.IDLE;
	}

	/** this sets the target to a (-180,180] degree and turns */
	public void turnTo(final float degrees) {
		System.err.println("Ro:Tu " + degrees);
		if(degrees <= -180 || degrees > 180) throw new IllegalArgumentException();

		anglePID.reset();

		target.r = (float)Math.toRadians(degrees);
		status = Status.ROTATING;
	}

	/** this sets the target to (x, y) and travels */
	public void travelTo(final float x, final float y) {
		System.out.println("Ro:Tr "+(int)x+","+(int)y);

		distancePID.reset();
		anglePID.reset();

		target.x = x;
		target.y = y;
		status = Status.TRAVELLING;
	}

	/** this implements a rotation by parts */
	void rotate() {
		/* calculate the delta beteen the target and the current and apply magic */
		Position current = odometer.getPositionCopy();
		delta.subR(target, current);
		float right = anglePID.nextOutput(delta.r);

		/* tolerence */
		if(anglePID.isWithin(ANGLE_TOLERANCE)) {
			this.stop();
			status = Status.IDLE;
			return;
		}

		/* set */
		this.setSpeeds(-right, right);
	}

	/** travels to a certain position */
	void travel() {
		/* calculate */
		Position current = odometer.getPositionCopy();
		delta.subXY(target, current);
		target.r = (float)Math.atan2(delta.y, delta.x);
		delta.subR(target, current);
		float distance = (float)Math.sqrt(delta.x*delta.x + delta.y*delta.y);

		float turn = anglePID.nextOutput(delta.r);

		float speed = distancePID.nextOutput(distance);// * Math.cos(Math.toRadians(p.theta));

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

	private static final NXTRegulatedMotor leftMotor  = Motor.A;
	private static final NXTRegulatedMotor rightMotor = Motor.B;

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
