/** Defines a position as x, y, theta and the things you can do (set,
 getDegrees, etc.) Of particular interest, since Positions are used in
 Odometer, you have: public void arc(final float angle, final float dist)
 which adds to the position an arc specified by a distance and an angle which
 is divided up along the distance evenly; uses affine transformations and the
 calculus of rectification; the Taylor expansion is O(3); and
 public void expectation(final Position p, final Position o) which sets this to
 the prediction of the O(1) future.
 <p>
 Position: defines a position as x, y, theta
 @author Neil */

import java.lang.IllegalArgumentException;

class Position {
	private static final float PI     = (float)Math.PI;
	private static final float TWO_PI = (float)(2.0 * Math.PI);

	/* these should be ints; I am lazy; (x, y) can be any value so I am making
	 them public */
	public  float x, y;
	private float t;

	/* constructors */

	/** empty constructor */
	public Position() {
	}

	/** float (x, y) constructor
	 @param x
	 @param y The (x, y). */
	public Position(final float x, final float y) {
		this.x = x;
		this.y = y;
	}

	/** float (x, y, degrees) constructor
	 @param x
	 @param y   (x, y)
	 @param deg degrees */
	public Position(final float x, final float y, final float deg) {
		if(deg > 180 || deg <= -180) {
			throw new IllegalArgumentException("Pos t=" + deg);
		}
		this.x = x;
		this.y = y;
		this.t = (float)Math.toRadians(deg);
	}

	/* getters */

	/** convert to degrees
	 @return degrees (-180, 180] */
	public float getDegrees() {
		return (float)Math.toDegrees(t);
	}

	/** native
	 @return radians (-PI, PI] */
	public float getRadians() {
		return t;
	}

	/** phased out; do not use; too confusing
	 @depreciated use :getRadians() */
	public float getTheta() {
		return t;
	}

	/* setters */

	/** set the position, overrides the old
	 @param p A position object that you wish to clone. */
	public void set(final Position p) {
		x = p.x;
		y = p.y;
		t = p.t;
	}

	/** set the degrees, overrides the old
	 @param deg Degrees (-180, 180] */
	public void setDegrees(final float deg) {
		if(t <= -180 || t > 180) throw new IllegalArgumentException();
		this.t = (float)Math.toRadians(deg);
	}

	/** set the radians, overrides the old
	 @param t radians (-PI, PI] */
	public void setRadians(final float t) {
		if(t <= -PI || t > PI) throw new IllegalArgumentException();
		this.t = t;
	}

	/** phased out; do not use (too confusing)
	 @depreciated use :setRadians(t) */
	public void setTheta(final float t) {
		if(t <= -PI || t > PI) throw new IllegalArgumentException();
		this.t = t;
	}

	/** set the x, y, overrides the old
	 @param x
	 @param y Any value. */
	public void setXY(final float x, final float y) {
		this.x = x;
		this.y = y;
	}

	/* operators */

	/** add the x, y to the current x, y
	 @param x
	 @param y Any value. */
	public void addXY(final float x, final float y) {
		this.x += x;
		this.y += y;
	}

	/** set the position to a - b, overrides the old
	 @param a
	 @param b The positions. */
	public void subXY(final Position a, final Position b) {
		x = a.x - b.x;
		y = a.y - b.y;
	}

	/** add t to the current theta
	 @param t An angle within (-PI, PI]
	 @throws IllegalArgumentException if t <= -PI || t > PI */
	public void addRadians(final float t) {
		if(t <= -PI || t > PI) throw new IllegalArgumentException();
		this.t += t;
		if(this.t <= -PI)    this.t += TWO_PI;
		else if(this.t > PI) this.t -= TWO_PI;
	}

	/** set theta to theta_a - theta_b, overrides the old
	 @param a
	 @param b */
	public void subTheta(final Position a, final Position b) {
		t = a.t - b.t;
		if(t <= -PI)    t += TWO_PI;
		else if(t > PI) t -= TWO_PI;
	}

	/** add to the position an arc specified by a distance and an angle which
	 is divided up along the distance evenly; uses affine transformations and
	 the calculus of rectification; the Taylor expansion is O(3)
	 <p>
	 this results in a heart, if you draw it out for parametric const dist
	 <p>
	 this is used in odometer
	 @param angle (-PI, PI]
	 @param dist  A distance. */
	public void arc(final float angle, final float dist) {

		/* the exact answer ran ten times slower then the separated code; I
		 guess the hardware is slow at trig; we're using small theta, so we can
		 expand this in a Taylor series; it makes a lot of sense and eliminates
		 l'H\^opital's rule about close-to-zero resulting in cache-friendly
		 code; it runs in faster then the separated (angle, forward) code */

		final float angle2 = angle  * angle;
		final float angle3 = angle2 * angle;
		/* fixme: we may use these elsewhere */
		final float c      = (float)Math.cos(t);
		final float s      = (float)Math.sin(t);

		/* object co-ordinates expanded in a Taylor series:
		 dx = dist*Sinc[angle]; dy = dist/angle(Cos[angle]-1) */
		float dx = dist * (1f          - angle2 / 6f  /* + O(angle^4) */);
		float dy = dist * (-angle / 2f + angle3 / 24f /* - O(angle^5) */);
		/* inverse rotation + transformation matrix transformation */
		x +=  c*dx - s*dy;
		y +=  s*dx + c*dy;
		/* update the angle */
		t += angle;
		if(t <= -PI)    t += TWO_PI;
		else if(t > PI) t -= TWO_PI;
	}

	/** sets this to the prediction of the O(1) future
	 @param final Position p  the current position
	 @param final Position o  the old position (assumes you take approimately
	 equal time steps) */
	public void expectition(final Position p, final Position o) {
		float dt1, dt2, dt;
		x = 1.5f*p.x - 0.5f*o.x;
		y = 1.5f*p.y - 0.5f*o.y;
		/* selects the minimum angle */
		if(p.t >= o.t) {
			dt1 = p.t - o.t;
			dt2 = o.t - p.t + TWO_PI;
		} else {
			dt1 = o.t - p.t;
			dt2 = p.t - o.t + TWO_PI;
		}
		dt = (dt1 >= dt2) ? dt2 : dt1;
		/* add it to t */
		t = p.t + 0.5f*dt;
		if(t <= -PI)    t += TWO_PI;
		else if(t > PI) t -= TWO_PI;
	}

	/** prints useful debug info
	 @return string */
	public String toString() {
		return "(" + (int)x + "," + (int)y + ":" + (int)Math.toDegrees(t) + ")";
	}

}
