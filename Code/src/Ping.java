/* this is a struct to store ultrasonic sonar information, we want this to be
 as small as possible */

import java.util.ArrayList;

public class Ping {

	private static final byte THRESHOLD = 50;
	private static final byte  MIN_LOCO = 7;

	private float angle; /* fixme later */
	private byte  cm;

	public Ping(final Position p, final int reading) {
		angle = (float)Math.toDegrees(p.getTheta());
		cm    = (byte)reading; /* fixme: ignores errors! */
	}

	/* fixme: it only localises facing out, but the same idea */
	public static boolean correct(final ArrayList<Ping> list) {
		int size, r, l;
		Ping left, right;

		size = list.size();
		if(size < 8) return false;

		/* left hit (oh good grief) */
		/*for(left : list) if(lt(ping.cm, THRESHOLD)) break;
		if(left == null) return false;*/
		for(l = 0; !lt((left = list.get(l)).cm, THRESHOLD); l++) {
			if(l >= size) return false;
		}

		/* right hit; the list is garaunteed to have some elemnent lt by above */
		for(r = size - 1; !lt((right = list.get(r)).cm, THRESHOLD); r--);

		Display.setText2("(" + size + ") " + (int)left.angle + "; " + (int)right.angle);
		return true;
	}

	/** why they don't have unsigned compare in the Java specs is beyond me,
	 something like <{unsigned}; I mean it's in the hardware, and I occasionally
	 need to use it (okay, so this is the only time I've used it) */
	private static boolean lt(final byte a, final byte b) {
		return (a < b) ^ ((a < 0) != (b < 0));
	}

}
