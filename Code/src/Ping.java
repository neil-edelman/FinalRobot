/* this is a struct to store ultrasonic sonar information, we want this to be
 as small as possible since we're storing a lot of them */

import java.lang.IllegalArgumentException;
import java.util.ArrayList;

public class Ping {

	/***** fixme: measure don't guess */
	/***** fixme: have it in Robot.java? */
	private static final float SONIC_FORWARD = 15;
	private static final int   THRESHOLD     = 50;

	private static Odometer odometer;

	private Position position = new Position();
	private int      cm;
	public  float    x, y; /* derived */
	public  int colour;    /* gnuplot */

	public Ping(final Position p, final int reading) {
		position.set(p);
		cm = reading;
		/* derive */
		float a = p.getRadians();
		float b = cm + SONIC_FORWARD;
		x = p.x + (float)Math.cos(a) * b;
		y = p.y + (float)Math.sin(a) * b;
	}

	/* sets the odometer used by correct */
	public static void setOdometer(final Odometer o) {
		odometer = o;
	}

	/* fixme: very rought */
	/* fixme: it only localises facing out, but the same idea */
	public static boolean correct(final ArrayList<Ping> list) {
		int size, r, l;
		Ping left, right;

		if(odometer == null) throw new IllegalArgumentException("no odometer");

		size = list.size();
		if(size < 8) return false;

		/* left hit; for(left : list) oh good grief, really? */
		for(l = 0; (left = list.get(l)).cm >= THRESHOLD; l++) {
			if(l >= size) return false;
		}

		/* right hit; the list is garaunteed to have some elemnent lt by above */
		for(r = size - 1; (right = list.get(r)).cm >= THRESHOLD; r--);

		float deg = 45f - (left.position.getDegrees() + right.position.getDegrees()) * 0.5f;
		odometer.setDegrees(deg);
		Display.setText2(deg + ": " + (int)left.position.getDegrees() + "+" + (int)right.position.getDegrees());

		/* do some tranforms and set xy */
		odometer.setXY(15, 15);

		return true;
	}

	/** why they don't have unsigned compare in the Java specs is beyond me,
	 something like <{unsigned}; I mean it's in the hardware, and I occasionally
	 need to use it (okay, so this is the only time I've used it) */
	private static boolean lt(final byte a, final byte b) {
		return (a < b) ^ ((a < 0) != (b < 0));
	}

}
