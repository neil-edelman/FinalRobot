/** Controller: implements float PID control with 0 as the setpoint.
 @author Neil */

import java.lang.IllegalArgumentException;

/*<N extends Number> was so cool, but aritmetic operations can't be applied to
 Number */
public class Controller {
	/* this causes the controller to slowly forget values in the distant past;
	 should be < 1 */
	private final static float FORGET = 0.99f;

	float kp, ki, kd; /* proportional, intergal, derivative */
	float e, eLast;   /* setpoint - current value */
	float integral, derivative;
	float min, max;   /* limits */
	float lastPid = 0;/* for limiting the accel */
	float accelerationLimit;
	boolean isLimit, isAccelerationLimit, isFirst = true;

	/* just p
	 @param p proportional gain */
	public Controller(final float p) {
		if(p <= 0) throw new IllegalArgumentException();
		kp = p;
		ki = 0;
		kd = 0;
	}

	/* pid
	 @param p proportional gain
	 @param i integral gain
	 @param d derivative gain
	 @see #Controller(final float) */
	public Controller(final float p, final float i, final float d) {
		this(p);
		ki = i;
		kd = d;
	}

	/** returns the next step for the error and dt specified
	 @param error input error
	 @param dt the time in milliseconds
	 @return the output according to the pid values set */
	public float nextOutput(final float error, final float dt) {

		/* this is a remenant from where the setpoint could NOT equal zero
		 pv = presentValue;
		 e = sp - pv;*/
		/* this is much easier */
		e = error;

		/* p */
		final float p = kp * e;

		/* i */
		final float i = ki * integral * dt;
		
		/* d */
		if(isFirst) {
			isFirst = true;
		} else {
			derivative = (e - eLast) / dt;
		}
		final float d = kd * derivative;

		/* pid */
		float pid = p + i + d;

		/* limit output and update to values for the next time; should be
		 integral * FORGET ^ dt + e but not noticable */
		eLast = e;
		if(isLimit) {
			if(pid > max)      pid = max;
			else if(pid < min) pid = min;
			else          integral = integral /* * FORGET*/ + e;
		} else {
			integral = integral /* * FORGET*/ + e;
		}

		/* limit the acceleration */
		if(isAccelerationLimit) {
			float allowed;
			if(pid < lastPid) {
				allowed = lastPid - accelerationLimit * dt;
				if(pid < allowed) pid = allowed;
			} else if(pid > lastPid) {
				allowed = lastPid + accelerationLimit * dt;
				if(pid > allowed) pid = allowed;
			}
			lastPid = pid;
		}

		/* kp * e + ki * (int e) + kd * (d/dt e) */
		return pid;
	}

	/** reset the all the state varibles; this allows the controller to
	 be used more then once */
	public void reset() {
		isFirst    = true;
		isLimit    = false;
		integral   = 0f;
		derivative = 0f;
		e          = 0f;
	}

	/** resets the pid and sets [limit] on the next time you use it;
	 it is perfectly all right to reset(limit) the first time you use it
	 to have a limit
	 @param limit The limit (-/+) on the output and past which it will not
	 record the integral.
	 @throws IllegalArgumentException if the limit <= 0 */
	public void reset(final float limit) {
		if(limit <= 0) throw new IllegalArgumentException();
		this.reset();
		min = -limit;
		max =  limit;
		isLimit = true;		
	}

	/** limit acceleration; fixes stalling when battery voltage is low?
	 @param limit in /s
	 @throws IllegalArgumentException if the limit <= 0 */
	public void limitAcceleration(final float limit) {
		if(limit <= 0f) throw new IllegalArgumentException("limit " + limit);
		accelerationLimit = limit;
		isAccelerationLimit = true;
	}

	/** lifts the acceleration limit */
	public void limitAcceleration() {
		accelerationLimit = 0f;
		isAccelerationLimit = false;
	}

	/** checking if it's w/i epsilon
	 @param tolerance
	 @return True if the last error is within tolerance. */
	public boolean isWithin(final float tolerance) {
		return (e >= -tolerance) && (e <= tolerance);
	}

	/** checking if it's w/i epsilon and has settled
	 @param tolerance
	 @param marginal
	 @return True if the last error is within tolerance and marginal
	 tolerance. */
	public boolean isWithin(final float tolerance, final float marginal) {
		if((e < -tolerance) || (e > tolerance)) return false;
		/*if(isFirst) return true; <- always true in the next line */
		return (derivative >= -marginal) || (derivative >= marginal);
	}

	/** print useful things */
	public String toString() {
		return "Cont" + this.hashCode() + "(" + (int)e + ":" + kp + "," + ki + "," + kd + ")";
	}

	/** this fn is not needed now; the setpoint is always zero */
	/*public void setSetpoint(final N setpoint) {
		sp = setpoint;
	}*/

}
