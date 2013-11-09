/* this is a struct to store ultrasonic sonar information, we want this to be
 as small as possible since we're storing a lot of them */

import java.lang.IllegalArgumentException;
import java.util.ArrayList;

import java.util.Scanner;
import java.io.FileReader;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class Ping {

	public static void main(String args[]) {
		Scanner in = null;
		String line;
		ArrayList<Ping> pings = new ArrayList<Ping>(128);
		Position p = new Position();
		int cm;

		/* read */
		try {
			in = new Scanner(new FileReader("outloco1.data"));
			for(int i = 0; in.hasNextFloat(); i++) {
				p.setXY(in.nextFloat(), in.nextFloat());
				p.setDegrees(in.nextFloat());
				cm = in.nextInt();
				pings.add(new Ping(p, cm));
				//System.err.println(p + ": " + cm);
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

		Ping.correct(pings);

		/* out */
		for(Ping ping : pings) {
			/*System.out.println(ping.position.getDegrees() + "\t" + ping.cm);*/
			if(ping.cm >= 255 || ping.cm < 0) continue;
			System.out.println("" + ping.x + "\t" + ping.y);
		 }
	}

	private static final float SONIC_IN_ROBOT = 20;

	private static final byte THRESHOLD = 50;
	private static final byte  MIN_LOCO = 7;

	private Position position = new Position();
	private int      cm;
	private float    x, y; /* derived */
	private float    chiSq;

	public Ping(final Position p, final int reading) {
		position.set(p);
		cm = reading;
		/* derive */
		float a = p.getRadians();
		float b = cm + SONIC_IN_ROBOT;
		x = p.x + (float)Math.cos(a) * b;
		y = p.y + (float)Math.sin(a) * b;
	}

	/* fixme: very rought */
	/* fixme: it only localises facing out, but the same idea */
	public static boolean correct(final ArrayList<Ping> pings) {
		int size, r, l;
		Ping left, right;

		size = pings.size();
		if(size < MIN_LOCO) return false;

		/* left hit; for(left : list) */
		for(l = 0; (left = pings.get(l)).cm > THRESHOLD; l++) {
			if(l >= size) return false;
		}

		/* right hit; the list is garaunteed to have some elemnent lt by above */
		for(r = size - 1; (right = pings.get(r)).cm > THRESHOLD; r--);

		float deg = 45f - (left.position.getDegrees() + right.position.getDegrees()) * 0.5f;
		System.err.println(deg + ": " + (int)left.position.getDegrees() + " + " + (int)right.position.getDegrees());

		/* real */
		float s_x = 0, s_y = 0, ss_xx = 0, ss_yy = 0, ss_xy = 0, a, b;
		int n = 0;
		for(Ping ping : pings) {
			if(ping.cm >= 255 || ping.cm < 0 || ping.y < 0) continue;
			n++;
			s_x   += ping.x;
			s_y   += ping.y;
			ss_xx += ping.x * ping.x;
			ss_yy += ping.y * ping.y;
			ss_xy += ping.x * ping.y;
		}
		ss_xx -= s_x * s_x / n;
		ss_yy -= s_y * s_y / n;
		ss_xy -= s_x * s_y / n;
		a = ss_xy / ss_xx;
		b = s_y / n - a * s_x / n;
		System.err.println("" + a + "x + " + b);

		/* read */
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("robot.gnu", "UTF-8");
			writer.println("set term postscript eps enhanced");
			writer.println("set output \"robot.eps\"");
			writer.println("set xlabel \"x\"");
			writer.println("set ylabel \"y\"");
			writer.println("set size ratio -1");
			writer.println("#set size square");
			writer.println("y(x) = " + a + "*x + " + b);
			writer.println("plot \"robot.data\" using 1:2 title \"Robot\" with linespoints, \\");
			writer.println("y(x) title \"Fit\"");
		} catch(FileNotFoundException e) {
			System.err.println("Not created: " + e.getMessage());
		} catch(SecurityException e) {
			System.err.println("Can not create: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("IO crazy!: " + e.getMessage());
		} finally {
			writer.close();
		}

		return true;
	}
}
