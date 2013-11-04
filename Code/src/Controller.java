/* Controller: implements PID control with 0 as the setpoint */

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
	boolean isLimit, isFirst = true;

	/* just p */
	public Controller(final float p) {
		if(p <= 0) throw new IllegalArgumentException();
		kp = p;
		ki = 0;
		kd = 0;
	}

	/* pid */
	public Controller(final float p, final float i, final float d) {
		this(p);
		ki = i;
		kd = d;
	}

	/* with a (min,max) value */
	/*public Controller(final float p, final float min, final float max) {
		this(p);
		if(min > max) throw new IllegalArgumentException();
		this.min = min;
		this.max = max;
		isLimit  = true;
	}*/

	/** sets the limit that nextOutput returned value will be (positive, will
	 set min to -limit) */
	public void setLimit(final float limit) {
		if(limit <= 0) throw new IllegalArgumentException();
		min = -limit;
		max =  limit;
		isLimit = true;
	}
	
	public void clearLimit() {
		isLimit = false;
	}

	/** returns the next step */
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

		/* kp * e + ki * (int e) + kd * (d/dt e) */
		return pid;
	}

	/** reset the intergral and the derivative; this allows the controller to
	 be used more then once */
	public void reset() {
		isFirst    = true;
		integral   = 0f;
		derivative = 0f;
		e          = 0f;
	}

	/** checking if it's w/i epsilon */
	public boolean isWithin(final float tolerance) {
		return (e >= -tolerance) && (e <= tolerance);
	}
	public boolean isWithin(final float tolerance, final float derTol) {
		if((e < -tolerance) || (e > tolerance)) return false;
		/*if(isFirst) return true;*/
		return (derivative >= -derTol) || (derivative >= derTol);
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
