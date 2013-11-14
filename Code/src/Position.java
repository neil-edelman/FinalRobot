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

	public Position(final float x, final float y, final float deg) {
		if(deg > 180 || deg <= -180) throw new IllegalArgumentException();
		this.x = x;
		this.y = y;
		this.t = (float)Math.toRadians(deg);
	}

	/* getters */

	public float getDegrees() {
		return (float)Math.toDegrees(t);
	}
	
	public float getRadians() {
		return t;
	}

	/** phased out; do not use */
	public float getTheta() {
		return t;
	}

	/* setters */

	public void set(final Position p) {
		x = p.x;
		y = p.y;
		t = p.t;
	}

	public void setDegrees(final float deg) {
		if(t <= -180 || t > 180) throw new IllegalArgumentException();
		this.t = (float)Math.toRadians(deg);
	}

	public void setRadians(final float t) {
		if(t <= -PI || t > PI) throw new IllegalArgumentException();
		this.t = t;
	}

	/** phased out; do not use */
	public void setTheta(final float t) {
		if(t <= -PI || t > PI) throw new IllegalArgumentException();
		this.t = t;
	}

	public void setXY(final float x, final float y) {
		this.x = x;
		this.y = y;
	}

	/* operators */

	public void addXY(final float x, final float y) {
		this.x += x;
		this.y += y;
	}

	public void subXY(final Position a, final Position b) {
		x = a.x - b.x;
		y = a.y - b.y;
	}

	public void addRadians(final float t) {
		if(t <= -PI || t > PI) throw new IllegalArgumentException();
		this.t += t;
		if(this.t <= -PI)    this.t += TWO_PI;
		else if(this.t > PI) this.t -= TWO_PI;
	}

	public void subTheta(final Position a, final Position b) {
		t = a.t - b.t;
		if(t <= -PI)    t += TWO_PI;
		else if(t > PI) t -= TWO_PI;
	}

	/** behold the awesome power of math! given a distance and an angle which
	 is divided up along the distance evenly (an arc,) compute the next
	 distance, angle using the affine transformations and the calculus of
	 rectification (this results in a heart, if you draw it out for parametric
	 const dist) */
	public void arc(final float angle, final float dist) {

		/* the exact answer ran ten times slower then the separated code; I
		 guess the hardware is slow at trig; we're using small theta, so we can
		 expand this in a Taylor series; it makes a lot of sense and eliminates
		 l'H\^opital's rule about close-to-zero resulting in cache-friendly
		 code; it runs in faster then the separated (angle, forward) code */

		final float angle2 = angle  * angle;
		final float angle3 = angle2 * angle;
		final float c      = (float)Math.cos(t); /* we may use these elsewhere */
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

	public String toString() {
		return "(" + (int)x + "," + (int)y + ":" + (int)Math.toDegrees(t) + ")";
	}

}
