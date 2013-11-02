/* Java */

class Heart {
	private static final float PI     = (float)Math.PI;
	private static final float TWO_PI = (float)(2.0 * Math.PI);
	private static final float MIN_ANGLE = Float.MIN_NORMAL * 32f;

	private float x, y, t;

	public static void main(String args[]) {
		Heart h = new Heart();
		float a = -Float.MIN_NORMAL;
		for(int i = 0; i < 20; i++) {
			h.transform(a, 10);
			a /= 2;
			System.out.println(""+a+"\t"+h);
			h.reset();
		}
		for(a = 2f*(float)Math.PI; a > -2f*(float)Math.PI; a -= 0.1f) {
			h.transform(a, 10);
			System.out.println(""+a+"\t"+h);
			h.reset();
		}
	}
	
	public Heart() {
	}

	public String toString() {
		return ""+x+"\t"+y+"\t"+t;
	}

	public void reset() {
		x = 0;
		y = 0;
		t = 0;
	}

	public void transform(final float angle, final float dist) {
		/* too bad sinc is in Java Oracle extended :[ */
		if(angle >= MIN_ANGLE || angle <= -MIN_ANGLE) {
			/* object co-ordinates */
			float div = dist / angle;
			float xObject = div * ((float)Math.sin(angle));
			float yObject = div * ((float)Math.cos(angle) - 1f);
			/* matrix transformation */
			float c = (float)Math.cos(t);
			float s = (float)Math.sin(t);
			x +=  c*xObject + s*yObject;
			y += -s*xObject + c*yObject;
		} else {
			/* l'H\^opital's rule about 0 */
			float tIntermedate = t + angle * 0.5f;
			tIntermedate = t;
			x += dist * Math.cos(tIntermedate);
			y += dist * Math.sin(tIntermedate);
		}
		/* update the angle */
		t += angle;
		if(t <= -PI)    t += TWO_PI;
		else if(t > PI) t -= TWO_PI;
	}	
}
