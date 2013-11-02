import java.lang.IllegalArgumentException;

/* Position: defines a position */

class Position {
	private static final float PI        = (float)Math.PI;
	private static final float TWO_PI    = (float)(2.0 * Math.PI);
	/* it will break down at 32 cm / 100 ms = 11.52 km/h (a world record pace) */
	private static final float MIN_ANGLE = Float.MIN_NORMAL * 32f;

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

	/** behold the awesome power of math! given a distance and an angle which
	 is divided up along the distance evenly, compute the next distance, angle
	 using the affine transformations and the calculus of rectification
	 (this results in a heart, if you draw it out for parametric const dist) */
	public void transform(final float angle, final float dist) {
		/* too bad sinc is in Java Oracle extended :[ */
		/* float exp_min is -126 but exp_max is 127, we're going to ignore the
		 unbalanced last one */
//		if(angle >= MIN_ANGLE || angle <= -MIN_ANGLE) {
			/* object co-ordinates */
//			float div = dist / angle;
//			float xObject = div * ((float)Math.sin(angle));
//			float yObject = div * ((float)Math.cos(angle) - 1f);
			/* matrix transformation */
//			float c = (float)Math.cos(t);
//			float s = (float)Math.sin(t);
//			x +=  c*xObject + s*yObject;
//			y += -s*xObject + c*yObject;
//		} else {
			/* l'H\^opital's rule about close-to-zero */
//			float tIntermedate = t + angle * 0.5f;
//			x += dist * Math.cos(tIntermedate);
//			y += dist * Math.sin(tIntermedate);
//		}
		/* we need these */
		final float angle2 = angle * angle;
		final float angle3 = angle2 * angle;
		final float c      = (float)Math.cos(t);
		final float s      = (float)Math.sin(t);

		/* object co-ordinates expanded in a Taylor series */
		float dx = dist * (1f          - angle2 / 6f  /* + O(angle^4) */);
		float dy = dist * (-angle / 2f + angle3 / 24f /* - O(angle^5) */);
		/* matrix transformation */
		x +=  c*dx + s*dy;
		y += -s*dx + c*dy;
		/* update the angle */
		t += angle;
		if(t <= -PI)    t += TWO_PI;
		else if(t > PI) t -= TWO_PI;
	}

	public String toString() {
		return "(" + (int)x + "," + (int)y + ":" + (int)Math.toDegrees(t) + ")";
	}

}
