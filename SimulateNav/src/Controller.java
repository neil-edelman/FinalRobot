/* Controller: implements PID control with 0 as the setpoint */

import java.lang.IllegalArgumentException;

/* <N extends Number> was so cool but aritmetic operations can't be applied to
 Number */
public class Controller {

	private final static float kiOverkpDefault = 0.4f;
	private final static float kdOverkpDefault = 0.2f;

	float kp, ki, kd;      /* proportional, intergal, derivative, constants */
	float e;               /* error */
	float min, max;        /* limits */
	boolean isLimit, isLast;
	float integral, eLast; /* memory */

	/** new controller with convenient values */
	public Controller(final float p) {
		if(p <= 0) throw new IllegalArgumentException();
		kp = p;
		ki = p * kiOverkpDefault;
		kd = p * kdOverkpDefault;
	}

	/** p and max/min values */
	public Controller(final float p, final float min, final float max) {
		if(min > max || p <= 0) throw new IllegalArgumentException();
		kp = p;
		ki = p * kiOverkpDefault;
		kd = p * kdOverkpDefault;
		this.min = min;
		this.max = max;
		isLimit = true;
	}

	/** returns the next step; fixme: add time (more complicated!)? */
	public float next(final float error) {
		float derivative;

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
		if(isLast) {
			derivative = e - eLast;
		} else {
			derivative = 0;
			isLast     = true;
		}
		final float d = kd * derivative;

		float pid = p + i + d;

		/* limit output */
		if(isLimit) {
			if(pid > max)      pid = max;
			else if(pid < min) pid = min;
		}

		/* update to values for the next time */
		integral += e;
		eLast     = e;

		/* kp * e + ki * (int e) + kd * (d/dt e) */
		return pid;
	}

	/** this method is a convineience; it's always checking if it's w/i epsilon */
	public boolean isWithin(final float tolerance) {
		return (e > -tolerance) && (e < tolerance);
	}
	
	/** reset the intergral and the derivative; this allows the controller to
	 be used more then once */
	public void reset() {
		isLast = false;
		integral = 0;
		e = 0f;
	}

	/** print the last error */
	public String toString() {
		return "{"+(int)e+"}";
		/* return "Controller"+this.hashCode()+" with pid "+kp+", "+ki+", "+kd+" at setpoint "+sp+" is at "+pv+""; */
	}

	/** this fn is not needed now; the setpoint is always zero */
	/*public void setSetpoint(final N setpoint) {
		sp = setpoint;
	}*/
}
