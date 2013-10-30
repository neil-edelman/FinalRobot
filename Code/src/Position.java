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

	/* fixme: add the expectation value (1/2 rotate, advance, 1/2 rotate) */
	public void transform(final float angle, final float dist) {
		t += angle;
		if(t <= -PI)    t += TWO_PI;
		else if(t > PI) t -= TWO_PI;
		x += dist * Math.cos(t);
		y += dist * Math.sin(t);
	}

	public String toString() {
		return "(" + (int)x + "," + (int)y + ":" + (int)Math.toDegrees(t) + ")";
	}

}
