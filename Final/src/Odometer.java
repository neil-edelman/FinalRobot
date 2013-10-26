/* Lab 4, Group 51 -- Alex Bhandari-Young and Neil Edelman */

/* experiment going left with 15.8 width; using +/-
	 ((180.0 * Math.PI * 15.8 * 90.0 / 360.0) / (Math.PI * Odometer.RADIUS)
	 ((180.0 * 15.8 * 90.0 / 360.0) / (Odometer.RADIUS)
	 RADIUS error times
	 2.8:   -60   4x
	 2.75:  -60  10x
	 2.7:   -30  10x
	 2.65:   15  10x
	 2.67:   -3  10x
	 2.665:   0  10x -> 2.665(3) */

/*
Coordinate System:

                     90 Deg: y axis                     
                           |                            
      positive theta       |      positive theta        
                           |                            
                           |                            
180 Deg: -x axis __________|__________ 0 Deg: x axis    
                           |                            
                           |                            
                           |                            
      negative theta       |      negative theta        
                           |                            
                    -90 Deg: -y axis                    


Theta range: (-180,180]

Ints are used to store the tachocount because floats have an upper bound of 10^6 (reached at 2778 revolutions).
This necessitates using ints until the final calculation. Thus twice the displacment is stored to prevent having
to divide by two until the final calculation.

The x and y orientation is identical to that used in the labs. However, theta=0 starts at the x-axis instead of
the y-axis, and increments counterclockwise and opposed to clockwise. 
The conversion is old_theta = -new_theta + 90 
and conversly new_theta = -old_theta + 90.

*/

import lejos.util.Timer;
import lejos.util.TimerListener;
import lejos.nxt.NXTRegulatedMotor;

public class Odometer implements TimerListener {

	/* MAYBE: have the time be a fn of the speed */
	static final int ODOMETER_DELAY = 25;
	static final float RADIUS       = 2.665f;
	static final float WIDTH        = 19.595f; /*value used with experimental mul_width: 15.8f*/ /* 15.9 16.0 15.24 */
	/* experiment: rotated by 10 000 went 4.75, 10000/360/4.75 */
	//static final float MUL_WIDTH    = 0.171f;

	final NXTRegulatedMotor leftMotor, rightMotor;

	Timer timer = new Timer(ODOMETER_DELAY, this);

   /*Displacemnt and heading are stored as ints. Division is not performed
    *until the final calculation to prevent accumulated error*/
	int dispTimesTwo, headingTimesTwo;
	Position p     = new Position();
	Position pCopy = new Position();

	/** constructor */
	public Odometer(final NXTRegulatedMotor leftMotor, final NXTRegulatedMotor rightMotor) {
		this.leftMotor  = leftMotor;
		this.rightMotor = rightMotor;
		timer.start();
	}

	public void shutdown() {
		timer.stop();
	}

	/** TimerListener function */
	public void timedOut() {

		/* get tach values */
		int leftTacho = leftMotor.getTachoCount();
		int rightTacho = rightMotor.getTachoCount();
      /* compute change in 2*position and 2*heading */
      int dDispTimesTwo = rightTacho + leftTacho - this.dispTimesTwo;
		int dHeadingTimesTwo = rightTacho - leftTacho - this.headingTimesTwo;

		/* Convert change in displacment to cm */
		float radianDisp = (float)Math.toRadians(0.5f * dDispTimesTwo) * RADIUS;
		/* Convert change in theta to radians */
		float dtheta = (float)Math.toRadians(0.5f * dHeadingTimesTwo) * WIDTH; //MUL_WIDTH was here

		float dx = radianDisp * (float)Math.cos(dtheta);
		float dy = radianDisp * (float)Math.sin(dtheta);

		synchronized(this) {
			p.x += dx;
			p.y += dy;
			p.theta += dtheta;
         /*theta kept in the range: (-180,180]*/
			if(p.theta <= -180f) p.theta += 360f;
			if(180f < p.theta) p.theta -= 360f;
		}

		this.dispTimesTwo += dDispTimesTwo;
		this.headingTimesTwo += dHeadingTimesTwo;
	}

	/** accessors */
	public Position getPositionCopy() {
		synchronized(this) {
			pCopy.copy(p);
		}
		return pCopy;
	}

	public String toString() {
		synchronized(this) {
			return "" + p;
		}
	}

   /** setters */
   public void setPosition(Position position) {
      synchronized(this) {
         p.x = position.x;
         p.y = position.y;
         p.theta = position.theta;
      }
   }
}
