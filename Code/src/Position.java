/* Position: defines a position */

class Position {
	private static final float PI     = (float)Math.PI;
	private static final float TWO_PI = (float)(2.0 * Math.PI);

	/* these should be ints; I am lazy */
	public float x, y;
	public float r;

	public Position() {
	}

	public Position(final float x, final float y) {
		this.x = x;
		this.y = y;
	}

	public Position(final float x, final float y, final float r) {
		this.x = x;
		this.y = y;
		this.r = r;
	}

	public void set(Position p) {
		x = p.x;
		y = p.y;
		r = p.r;
	}

	public void subXY(final Position a, final Position b) {
		x = a.x - b.x;
		y = a.y - b.y;
	}

	public void subR(final Position a, final Position b) {
		r = a.r - b.r;
		if(r <= -PI)    r += TWO_PI;
		else if(r > PI) r -= TWO_PI;
	}

	public void transform(final float angle, final float dist) {
		r += angle;
		if(r <= -PI)    r += TWO_PI;
		else if(r > PI) r -= TWO_PI;
		x += dist * Math.cos(r);
		y += dist * Math.sin(r);
	}

	public String toString() {
		return "("+(int)x+","+(int)y+":"+(int)Math.toDegrees(r)+")";
	}

}
