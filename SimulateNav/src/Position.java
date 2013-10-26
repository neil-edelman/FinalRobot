/* Position: defines a position */

class Position {
	private static final float PI     = (float)Math.PI;
	private static final float TWO_PI = (float)(2.0 * Math.PI);

	/* these should be ints; I am lazy */
	public float x, y;
	public float r;

	public Position() {
	}

	public void copy(Position p) {
		x = p.x;
		y = p.y;
		r = p.r;
	}

	public void addLocation(final float x, final float y) {
		this.x += x;
		this.y += y;
	}
	
	public void subLocation(final Position a, final Position b) {
		this.x = a.x - b.x;
		this.y = a.y - b.x;
	}
	
	public void addTheta(final float t) {
		r += t;
		if(r <= -PI)    r += TWO_PI;
		else if(r > PI) r -= TWO_PI;
	}

	public void subTheta(final Position a, final Position b) {
		r -= a.r + b.r;
		if(r <= -PI)    r += TWO_PI;
		else if(r > PI) r -= TWO_PI;
	}

	public String toString() {
		return "("+(int)x+","+(int)y+":"+(int)Math.toDegrees(r)+")";
	}

}
