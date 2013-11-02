import java.lang.IllegalArgumentException;

/* Position: defines a position */

class Position {
	private static final float PI     = (float)Math.PI;
	private static final float TWO_PI = (float)(2.0 * Math.PI);

	/* these should be ints; I am lazy; (x, y) can be any value so I am making
	 them public */
	public  float x, y;
	private float t;

	/* constructors */

	public Position() {
	}

	public Position(final float x, final float y) {
		this.x = x;
		this.y = y;
	}

	public Position(final float x, final float y, final float t) {
		if(t > 180 || t <= -180) throw new IllegalArgumentException();
		this.x = x;
		this.y = y;
		this.t = (float)Math.toRadians(t);
	}

	/* getters */

	public float getTheta() {
		return t;
	}

	/* setters */

	public void set(final Position p) {
		x = p.x;
		y = p.y;
		t = p.t;
	}

	public void setTheta(final float t) {
		if(t <= -PI || t > PI) throw new IllegalArgumentException();
		this.t = t;
	}

	/* operators */

	public void subXY(final Position a, final Position b) {
		x = a.x - b.x;
		y = a.y - b.y;
	}

	public void subTheta(final Position a, final Position b) {
		t = a.t - b.t;
		if(t <= -PI)    t += TWO_PI;
		else if(t > PI) t -= TWO_PI;
	}

	/* affine transformation; given a distance and an angle (assume divided up
	 along the distance,) compute the approximate rectification (assuming the
	 distance or the angle is small) using the expectation value of the angle */
	/* fixme: compute it exactly */
	public void transform(final float angle, final float dist) {
		float tIntermedate = t + angle * 0.5f;
		x += dist * Math.cos(tIntermedate);
		y += dist * Math.sin(tIntermedate);
		t += angle;
		if(t <= -PI)    t += TWO_PI;
		else if(t > PI) t -= TWO_PI;
	}

	public String toString() {
		return "(" + (int)x + "," + (int)y + ":" + (int)Math.toDegrees(t) + ")";
	}

}
