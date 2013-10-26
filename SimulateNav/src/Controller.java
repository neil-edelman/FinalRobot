/* Controller: implements PID control with 0 as the setpoint */

import java.lang.IllegalArgumentException;

/*<N extends Number> was so cool, but aritmetic operations can't be applied to
 Number */
public class Controller {
	/* who has time to fine tune these? */
	private static final float kiTokpDef = 0.4f;
	private static final float kdTokpDef = 0.2f;

	float kp, ki, kd; /* proportional, intergal, derivative */
	float e, eLast;   /* setpoint - current value */
	float integral;   /* keep track */
	float min, max;   /* limits */
	boolean isLimit, isLast;

	/* just give convenient values */
	public Controller(final float p) {
		if(p <= 0) throw new IllegalArgumentException();
		kp = p;
		ki = p * kiTokpDef;
		kd = p * kdTokpDef;
	}

	/* with a (min,max) value */
	public Controller(final float p, final float min, final float max) {
		this(p);
		if(min > max) throw new IllegalArgumentException();
		this.min = min;
		this.max = max;
		isLimit = true;
	}

	/* with a (min,max) value */
	public Controller(final float p, final float i, final float d, final float min, final float max) {
		this(p, min, max);
		ki = i;
		kd = d;
	}

	/** returns the next step; fixme: add time (more complicated!)? */
	public float nextOutput(final float error) {

		/* this is a remenant from where the setpoint could NOT equal zero
		 pv = presentValue;
		 e = sp - pv;*/
		/* this is much easier */
		e = error;

		/* p */
		final float p = kp * e;

		/* i */
		final float i = ki * integral;
		
		/* d */
		float derivative;
		if(isLast) {
			derivative = e - eLast;
		} else {
			derivative = 0;
			isLast     = true;
		}
		final float d = kd * derivative;

		/* pid */
		float pid = p + i + d;

		/* limit output and update to values for the next time */
		eLast = e;
		if(isLimit) {
			if(pid > max)      pid  = max;
			else if(pid < min) pid  = min;
			else          integral += e;
		} else {
			integral += e;
		}

		/* kp * e + ki * (int e) + kd * (d/dt e) */
		return pid;
	}

	/** reset the intergral and the derivative; this allows the controller to
	 be used more then once */
	public void reset() {
		isLast = false;
		integral = 0;
		e = 0f;
	}

	/** checking if it's w/i epsilon */
	public boolean isWithin(final float tolerance) {
		return (e > -tolerance) && (e < tolerance);
	}

	/** print useful things */
	public String toString() {
		return "Controller" + this.hashCode() + "(" + (int)e + ":" + kp + "," + ki + "," + kd + ")";
	}

	/** this fn is not needed now; the setpoint is always zero */
	/*public void setSetpoint(final N setpoint) {
		sp = setpoint;
	}*/
}
