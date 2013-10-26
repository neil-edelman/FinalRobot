import java.lang.IllegalArgumentException;

class Robot {

	enum Status { PLOTTING, SUCCESS, ROTATING, TRAVELLING };

	Status status;

	Controller    angle = new Controller(5f, -250f, 250f);
	Controller distance = new Controller(10f, -300f, 300f);
	final float    angleTolerance = (float)Math.toRadians(1);
	final float distanceTolerance = 1f;

	Position target = new Position();
	Position delta  = new Position();

	Odometer odometer = new Odometer();

	/** the constructor */
	public Robot() {
	}

	/** this acts as the control */
	public void run() {
		for( ; ; ) {
			/* what is it doing? */
			switch(status) {
				case PLOTTING:
					/* muhahahaha */
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
	}

	/** theta degrees */
	public void turnTo(final float theta) {
		if(theta <= -180 || theta > 180) throw new IllegalArgumentException();
		angle.reset();
		System.out.println("Turn("+(int)theta+")");
		target.r = (float)Math.toRadians(theta);
		status = Status.ROTATING;
	}

	public void travelTo(final float x, final float y) {
		System.out.println("Goto("+(int)x+","+(int)y+")");

		distance.reset();
		angle.reset();

		target.x = x;
		target.y = y;
		status = Status.TRAVELLING;
	}
	/*public void travelTo(final float xTarget, final float yTarget) {
		float xCurrent, yCurrent, tCurrent, tTarget, x, y, t, dist2, dist;
		float l, r, speed;
		for( ; ; ) {
			xCurrent = odo.getX();
			yCurrent = odo.getY();
			x = xTarget - xCurrent;
			y = yTarget - yCurrent;
			dist2 = x*x + y*y;
			if(dist2 < dist2Tolerance) break;
			tCurrent = odo.getTheta();
			tCurrent = -tCurrent + 90f;
			if(tCurrent < 180f) tCurrent += 360f;
			tTarget = (float)Math.toDegrees(Math.atan2(y, x));
			t = tTarget - tCurrent;
			if(t < -180f)     t += 360f;
			else if(t > 180f) t -= 360f;
			LCD.drawString("x "+x+"\ny "+y+"\nt "+t, 0,1);
			l = -t * pTheta;
			r =  t * pTheta;
			dist = (float)Math.sqrt(dist2);
			LCD.drawString("d "+dist, 0,5);
			speed = dist * pDist * (float)Math.cos(Math.toRadians(t));
			l += speed;
			r += speed;
			robot.setLeftSpeed(l);
			robot.setRightSpeed(r);
			try { Thread.sleep(100); } catch (InterruptedException e) { }
		}
		robot.stop();
	}*/

	/** this implements a rotation by the angle controller */
	void rotate() {
		Position current = odometer.getPositionCopy();
		delta.subTheta(target, current);
		float right = angle.next(delta.r);

		if(angle.isWithin(angleTolerance)) {
			this.stop();
			status = Status.PLOTTING;
			return;
		}
		this.setLeftSpeed(-right);
		this.setRightSpeed(right);
	}

	/** travels to a certain position */
	void travel() {
		Position current = odometer.getPositionCopy();
		delta.subLocation(target, current);
		target.r = (float)Math.atan2(delta.y, delta.x);
		delta.subTheta(target, current);
		float dist = (float)Math.sqrt(delta.x*delta.x + delta.y*delta.y);

		float l = angle.next(delta.r);
		float r = -l;

		float d = distance.next(dist);// * Math.cos(Math.toRadians(p.theta));
		r += d;
		l += d;

		if(distance.isWithin(distanceTolerance)) {
			this.stop();
			status = Status.PLOTTING;
			return;
		}
		this.setLeftSpeed(l);
		this.setRightSpeed(r);
	}

	/** set r/l speeds indepedently is good for pid-control */
	private void setLeftSpeed(final float s) {
		/*leftMotor.setSpeed(s);
		if(s > 0) {
			leftMotor.forward();
		} else if(s < 0) {
			leftMotor.backward();
		} else {
			leftMotor.stop();
		}*/
	}
	private void setRightSpeed(final float s) {
		/*rightMotor.setSpeed(s);
		if(s > 0) {
			rightMotor.forward();
		} else if(s < 0) {
			rightMotor.backward();
		} else {
			rightMotor.stop();
		}*/
	}
	public void stop() {
		/*leftMotor.stop();
		rightMotor.stop();*/
	}

	public String toString() {
		return "Robot is " + status;
	}
}
