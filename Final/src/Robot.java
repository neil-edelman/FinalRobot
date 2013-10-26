/* Lab 5, Group 51 -- Alex Bhandari-Young and Neil Edelman */

import lejos.nxt.Motor;
import lejos.nxt.Sound;
import lejos.nxt.SensorPort;
import lejos.nxt.LightSensor;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.NXTRegulatedMotor;

import lejos.nxt.Timer
import lejos.nxt.Button;
import lejos.nxt.TimerListener

/* Robot */

class Robot extends TimerListener {

	enum Status { IDLE, SUCCESS, LOCALISING, ROTATING, TRAVELLING, EVADING, EXPLORING, PUSHING };
	
	public final static String NAME = "Swagbot";
	static final int   NAV_DELAY    = 100; /* ms */
	static final int SONAR_DELAY    = 10;  /* ms */
	static final NXTRegulatedMotor leftMotor = Motor.A, rightMotor = Motor.B;

	Controller anglePID = new Controller (5f, 1f, 1f);
	Controller distancePID = new Controller (10f, 1f, 1f);
	final float  angleTolerance = (float)Math.toRadians(0.5f);
	final float  distanceTolerance = 3f;

	Status       status = Status.IDLE;
	UltrasonicSensor us = new UltrasonicSensor(SensorPort.S4);
	LightSensor      ls = new LightSensor(SensorPort.S1);
	Odometer   odometer = new Odometer(leftMotor, rightMotor);
	Position     target = new Position(), d = new Position();
	Colour       colour = new Colour();
	int lastDistance;

	/** the constructor */
	public Robot() {
      this.timer = new Timer(NAV_DELAY);
      Sound.setVolume(80);
	}

	public int getLastDistance() {
		synchronized(this) {
			return lastDistance;
		}
	}
	
	public Position getPosition() {
		synchronized(this) {
			return odometer.getPositionCopy();
		}
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

	public Colour.Value getColour() {
		return colour.getColourValue();
	}

	/** this acts as the control */
	public void timedOut() {

	   lastDistance = us.getDistance();
		if(lastDistance < 15) {}

		switch(status) { //idle,running localization, rotating, traveling, or finished
			case IDLE:
				break;
			case LOCALISING:
				break;
			case SUCCESS:
				return;
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
		status = Status.SUCCESS;
	}


	void localise(boolean doLight) {

      status = STATUS.LOCALISING;
      
      USLocalizer usl = new USLocalizer(odometer, new UltrasonicSensor(SensorPort.S4), USLocalizer.LocalizationType.FALLING_EDGE);
      usl.doLocalization();
      if(doLight) {
         odometer.travelTo(-3f,-3f);
         odometer.turnTo(45f);
         LightLocalizer lsl = new LightLocalizer(odometer, new LightSensor(SensorPort.S1));
         lsl.doLocalization();
      }

		status = Status.IDLE;
	}

	/** this sets the target to a [0,360) degree */
	public void turnTo(final float theta/*degrees*/) {
		anglePID.reset();
		/*this.turnTo(Position.fromDegrees(degrees));*/
//		System.out.println("Turn("+(int)theta+")");
		target.theta = theta;
		/*anglePID.setSetpoint(theta);*/
		status = Status.ROTATING;
	}

	/** this sets the target to a {0,32} fixed point angle; flag rotating */
	/*public void turnTo(final int theta)*/

	public void travelTo(final float x, final float y) {
//		System.out.println("Goto("+(int)x+","+(int)y+")");

		distancePID.reset();
		anglePID.reset();

		target.x = x;
		target.y = y;
		//status = Status.TRAVELLING;

		/* fixme: ghetto */

		Position p;
		float dx, dy, dt;
		float right, dist, speed;

		for( ; ; ) {
			p = odometer.getPositionCopy();
			dx                      = target.x - p.x;
			dy                      = target.y - p.y;
			target.theta            = (float)Math.toDegrees(Math.atan2(dy, dx));
			dt                      = target.theta - p.theta;
			if(dt < -180f)      dt += 360f;
			else if(dt >= 180f) dt -= 360f;
//			LCD.drawString(""+(int)dx+","+(int)dy+":"+(int)dt+";", 0,0);
			right = anglePID.next(dt);
			if(anglePID.isWithin(angleTolerance)) break;
			this.setLeftSpeed(-right);
			this.setRightSpeed(right);
			try { Thread.sleep(50); } catch (InterruptedException e) { }
		}
		float targetDist = (float)Math.sqrt(dx*dx + dy*dy) * 0.2f;
		for( ; ; ) {
			p = odometer.getPositionCopy();
			dx   = target.x - p.x;
			dy   = target.y - p.y;
			dist = (float)Math.sqrt(dx*dx + dy*dy);
			if(dist < targetDist) break;
			if(dist < 1.5f) break;
			//speed = dist * 5f;
			speed = distancePID.next(dist - targetDist);
         this.setLeftSpeed(speed);
			this.setRightSpeed(speed);
			try { Thread.sleep(50); } catch (InterruptedException e) { }
		}
		this.stop();
		status = Status.IDLE;
	}

	/** this implements a rotation by the angle controller */
	void rotate() {
		Position  p = odometer.getPositionCopy();
		float    dt = target.theta - p.theta;
		if(dt < -180f)     dt += 360f;
		else if(dt > 180f) dt -= 360f;
		float right = anglePID.next(dt);

//		LCD.drawString("r:a "+(int)dt+" "+angle+"", 0, 1);
		if(anglePID.isWithin(angleTolerance)) {
			this.stop();
			status = Status.IDLE;
			return;
		}
		this.setLeftSpeed(-right);
		this.setRightSpeed(right);
	}

	/** travels to a certain position */
	void travel() {
		Position p = odometer.getPositionCopy();
		d.x     = target.x - p.x;
		d.y     = target.y - p.y;
		d.theta = (float)Math.toDegrees(Math.atan2(d.y, d.x)) - p.theta;
		if(d.theta < -180f)      d.theta += 360f;
		else if(d.theta >= 180f) d.theta -= 360f;
		float dist = (float)Math.sqrt(d.x*d.x + d.y*d.y);

		float l = anglePID.next(d.theta);
		float r = -l;

		float d = distancePID.next(dist);// * Math.cos(Math.toRadians(p.theta));
		r += d;
		l += d;

		if(distancePID.isWithin(distanceTolerance)) {
			this.stop();
			status = Status.IDLE;
			return;
		}
		this.setLeftSpeed(l);
		this.setRightSpeed(r);

//		LCD.drawString("t"+d+"  ", 0, 0);
	}

	/** set r/l speeds indepedently is good for pid-control */
	private void setLeftSpeed(final /*int*/float s) {
		leftMotor.setSpeed(s);
		if(s > 0) {
			leftMotor.forward();
		} else if(s < 0) {
			leftMotor.backward();
		} else {
			leftMotor.stop();
		}
	}
	private void setRightSpeed(final /*int*/float s) {
		rightMotor.setSpeed(s);
		if(s > 0) {
			rightMotor.forward();
		} else if(s < 0) {
			rightMotor.backward();
		} else {
			rightMotor.stop();
		}
	}
	public void stop() {
		leftMotor.stop();
		rightMotor.stop();
	}	

	/** fixme: get FILTED data; this is especially critical when two robots are
	 getting sound distances at the same time at the same frequency */
	public int pingSonar() {
		us.ping();
		try {
			Thread.sleep(SONAR_DELAY);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return us.getDistance();
	}

	public String toString() {
		return NAME+/*this.hashCode()+*/" is "+status;
	}

	/** this is for calibrating; 3 squares 91.44 -> 30.48 cm / tile */
	public void driveLeg(final float cm) {
		final int FORWARD_SPEED = 250;
		final int ROTATE_SPEED = 150;

		/* forward */
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		leftMotor.rotate((int)((180.0 * cm) / (Math.PI * Odometer.RADIUS)), true);
		rightMotor.rotate((int)((180.0 * cm) / (Math.PI * Odometer.RADIUS)), false);

		/* turn 90 */
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		leftMotor.rotate((int)-((180.0 * Math.PI * Odometer.WIDTH * 90.0 / 360.0) / (Math.PI * Odometer.RADIUS)), true);
		rightMotor.rotate((int)((180.0 * Math.PI * Odometer.WIDTH * 90.0 / 360.0) / (Math.PI * Odometer.RADIUS)), false);
	}
}
