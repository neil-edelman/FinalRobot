/* this is a struct to store ultrasonic sonar information, we want this to be
 as small as possible since we're storing a lot of them */

import java.lang.IllegalArgumentException;
import java.util.ArrayList;

//import java.io.BufferedReader;
import java.util.Scanner;
import java.io.FileReader;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Ping {

	public static void main(String args[]) {
		//BufferedReader in = null;
		Scanner in = null;
		String line;
		ArrayList<Ping> pings = new ArrayList<Ping>(128);
		Position p = new Position();
		float x;
		int y;

		/* read */
		try {
			//in = new BufferedReader(new FileReader("pings.data"));
			//for(int i = 0; (line = in.readLine()) != null; i++) {
			in = new Scanner(new FileReader("pings.data"));
			for(int i = 0; in.hasNextFloat(); i++) {
				x = in.nextFloat();
				p.setTheta((float)Math.toRadians(x));
				y = in.nextInt();
				pings.add(new Ping(p, y));
			}
		} catch(FileNotFoundException e) {
			System.err.println("Not found.");
			return;
		} catch (IOException e) {
			System.err.println("IO crazy!");
			return;
		} finally {
			in.close();
		}

		/* out */
		for(Ping ping : pings) {
			System.out.println(ping.angle + "\t" + toInt(ping.cm));
		}
		Ping.correct(pings);
	}

	private static void close(Closeable c) {
		if(c != null) {
			try {
				c.close();
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	private static final byte THRESHOLD = 50;
	private static final byte  MIN_LOCO = 7;

	private float angle; /* fixme later */
	private byte  cm;

	public Ping(final Position p, final int reading) {
		angle = (float)Math.toDegrees(p.getTheta());
		cm    = (byte)reading; /* fixme: ignores errors! */
	}

	/* fixme: very rought */
	/* fixme: it only localises facing out, but the same idea */
	public static boolean correct(final ArrayList<Ping> list) {
		int size, r, l;
		Ping left, right;

		size = list.size();
		if(size < 8) return false;

		/* left hit; for(left : list) oh good grief, really? */
		for(l = 0; !lt((left = list.get(l)).cm, THRESHOLD); l++) {
			if(l >= size) return false;
		}

		/* right hit; the list is garaunteed to have some elemnent lt by above */
		for(r = size - 1; !lt((right = list.get(r)).cm, THRESHOLD); r--);

		float deg = 45f - (left.angle + right.angle) * 0.5f;
		System.err.println(deg + ": " + (int)left.angle + " + " + (int)right.angle);

		return true;
	}

	/** why they don't have unsigned compare in the Java specs is beyond me,
	 something like <{unsigned}; I mean it's in the hardware, and I occasionally
	 need to use it (okay, so this is the only time I've used it) */
	private static boolean lt(final byte a, final byte b) {
		return (a < b) ^ ((a < 0) != (b < 0));
	}
	private static int toInt(final byte a) {
		return (a < 0) ? (a + 256) : (a);
	}
}
