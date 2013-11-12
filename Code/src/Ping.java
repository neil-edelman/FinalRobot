/* this is a struct to store ultrasonic sonar information, we want this to be
 as small as possible since we're storing a lot of them */

import java.lang.IllegalArgumentException;
import java.util.ArrayList;

public class Ping {

	private static final byte THRESHOLD = 50;
	private static final byte  MIN_LOCO = 7;

	private static Odometer odometer;

	private float angle; /* fixme later */
	private byte  cm;

	public Ping(final Position p, final int reading) {
		angle = (float)Math.toDegrees(p.getTheta());
		cm    = (byte)reading; /* fixme: ignores errors! */
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
		for(l = 0; !lt((left = list.get(l)).cm, THRESHOLD); l++) {
			if(l >= size) return false;
		}

		/* right hit; the list is garaunteed to have some elemnent lt by above */
		for(r = size - 1; !lt((right = list.get(r)).cm, THRESHOLD); r--);

		float deg = 45f - (left.angle + right.angle) * 0.5f;
		odometer.setDegrees(deg);
		Display.setText2(deg + ": " + (int)left.angle + "+" + (int)right.angle);

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
