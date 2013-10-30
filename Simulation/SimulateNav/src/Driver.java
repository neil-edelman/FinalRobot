/**** comment ****/
/*import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;*/

/* this is a driver that instantaties a Robot and makes it do stuff */

class Driver {

	public static void main(String args[]) {
		Driver d = new Driver();

		System.err.println(d.controlAngle);
		System.err.println(d.controlSpeed);
		d.setTarget(new Position(10f, 10f));
		d.simulate();
		for(int i = 0; i < 100; i++) {
			d.travel();
			d.simulate();
		}
	}

	private static final float SIM_SPEED = 0.01f;
	private static final float WHEELBASE = 10f;
	private static final float PI = (float)Math.PI;

	Controller controlAngle = new Controller(2f, 0.01f, 0.005f, -250, 250);
	Controller controlSpeed = new Controller(5f, 0.05f, 0.5f, -250, 250);

	private Position current = new Position();
	private Position delta   = new Position();
	private float l, r;
	private Position target;

	public Driver() {
	}

	public void setTarget(final Position p) {
		target = p;
		System.err.println("setTarget " + target);
	}

	public void travel() {
		delta.subXY(target, current);
		target.r = (float)Math.atan2(delta.y, delta.x);
		delta.subR(target, current);
		float turn = controlAngle.nextOutput(delta.r);
		turn = delta.r * 3f;
		l = -turn;
		r = turn;
		float distance = (float)Math.sqrt(delta.x*delta.x + delta.y*delta.y);
		float speed = controlSpeed.nextOutput(distance) * (float)Math.cos(delta.r);
		speed = distance * 6f * (float)Math.cos(delta.r);
		l += speed;
		r += speed;
		System.err.println("travel target " + target + "-" + current + "=" + delta + " -> speeds [" + l + ", " + r + "]");
	}

	public void simulate() {
		float speed = (l + r) / 2f * SIM_SPEED /* * (180 / PI) */;
		float angle = (r - l) * WHEELBASE / 2f * SIM_SPEED /* * (180f / PI) */;
		current.transform(angle, speed);
		System.out.println("" + current.x + "\t" + current.y + "\t" + Math.toDegrees(current.r));
		/**** comment ****/
		this.setSpeeds(speed - angle, speed + angle);
	}
	
	/**** comment ****/
	//private static final NXTRegulatedMotor leftMotor = Motor.A, rightMotor = Motor.B;
	
	/** set r/l speeds indepedently */
	private void setSpeeds(final float l, final float r) {
		/**** comment ****/
		/*leftMotor.setSpeed(l);
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
		}*/
	}
	
	/** [emergency/idle] stop */
	public void stop() {
		/**** comment ****/		
		/*leftMotor.stop();
		rightMotor.stop();*/
	}
}
