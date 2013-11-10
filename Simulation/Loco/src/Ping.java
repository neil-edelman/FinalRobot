/* this is a struct to store ultrasonic sonar information, we want this to be
 as small as possible since we're storing a lot of them */

import java.lang.IllegalArgumentException;
import java.util.ArrayList;

import java.util.Scanner;
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
			in = new Scanner(System.in);
			for(int i = 0; in.hasNextFloat(); i++) {
				p.setXY(in.nextFloat(), in.nextFloat());
				p.setDegrees(in.nextFloat());
				cm = in.nextInt();
				pings.add(new Ping(p, cm));
				System.err.println(i + " -- " + p + ": " + cm);
			}
		} catch(Exception e) {
			System.err.println(e.getMessage());
		} finally {
			in.close();
		}

		/* do calculations! */
		if(!Ping.correct(pings)) System.err.println("Couldn't localise.");

		/* out */
		for(Ping ping : pings) {
			System.out.println("" + ping.x + "\t" + ping.y + "\t" + ping.colour);
		 }
	}

	/***** fixme: measure don't guess */
	private static final float SONIC_IN_ROBOT = 15;

	private Position position = new Position();
	private int      cm;
	private float    x, y; /* derived */
	private int colour;    /* gnuplot */

	public Ping(final Position p, final int reading) {
		position.set(p);
		cm = reading;
		/* derive */
		float a = p.getRadians();
		float b = cm + SONIC_IN_ROBOT;
		x = p.x + (float)Math.cos(a) * b;
		y = p.y + (float)Math.sin(a) * b;
	}

	public static boolean correct(final ArrayList<Ping> pings) {
		/* determine the area that is most close taking the minimum;
		 should be the integral of all the area, but this is okay
		 (2n instead of n;) we don't expect to be placed 1.7m away */
		int closest = 255, cm;
		int index = 0, left, right, len = pings.size();
		for(int i = 0; i < len; i++) {
			cm = pings.get(i).cm;
			if(cm < closest) {
				closest = cm;
				index   = i;
			}
		}
		if(closest >= 255) return false; /* in a big room? */

		/* now go to the 255's at either end (we assume that 255's exist) */
		/* FIXME: more! what if the block is against the wall? this is where
		 we'd weed it out (easily! just check the radial derivative) */
		for(left = index - 1; ; left--) {
			if(left < 0) left = len - 1;
			if(left == index) return false;
			if(pings.get(left).cm >= 255) break;
		}
		for(right = index + 1; ; right++) {
			if(right >= len) right = 0;
			/* if(r == index) return false; is never going to happen */
			if(pings.get(right).cm >= 255) break;
		}

		/* hand-wavey say that this has been constantly sampled for conveniece */
		/* FIXME: check to make sure it's the middle, adjust */
		int rEff = right;
		if(rEff < left) rEff += len;
		int mid = (rEff + left) / 2;
		if(mid >= len) mid -= len;
		System.err.println("left " + left + "--> " + mid + " <--" + right + " right");

		/*for(Ping ping : pings)
		 if(((i > left) && (i < mid)) ^ (left > mid))
		 else if(((i > mid) && (i < right)) ^ (mid > right))*/

		/* covarience */
		Ping ping;
		float s_xl = 0, s_yl = 0, ss_xxl = 0, ss_yyl = 0, ss_xyl = 0;
		int nl = 0;
		for(int i = left + 1; ; i++) {
			if(i >= len) i = 0;
			if(i == mid) break;
			ping = pings.get(i);
			ping.colour = 1;
			nl++;
			s_xl   += ping.x;
			s_yl   += ping.y;
			ss_xxl += ping.x * ping.x;
			ss_yyl += ping.y * ping.y;
			ss_xyl += ping.x * ping.y;
		}
		if(nl < 2) return false;
		ss_xxl -= s_xl * s_xl / nl;
		ss_yyl -= s_yl * s_yl / nl;
		ss_xyl -= s_xl * s_yl / nl;
		/*float cov_xx = ss_xx / n;
		 float cov_yy = ss_yy / n;
		 float cov_xy = ss_xy / n;*/
		System.err.println("Left covarience of " + nl + " points.");
		float s_xr = 0, s_yr = 0, ss_xxr = 0, ss_yyr = 0, ss_xyr = 0;
		int nr = 0;
		for(int i = mid; ; i++) {
			if(i >= len) i = 0;
			if(i == right) break;
			ping = pings.get(i);
			ping.colour = 2;
			nr++;
			s_xr   += ping.x;
			s_yr   += ping.y;
			ss_xxr += ping.x * ping.x;
			ss_yyr += ping.y * ping.y;
			ss_xyr += ping.x * ping.y;
		}
		if(nr < 2) return false;
		ss_xxr -= s_xr * s_xr / nr;
		ss_yyr -= s_yr * s_yr / nr;
		ss_xyr -= s_xr * s_yr / nr;
		System.err.println("Right covarience of " + nr + " points.");

		/* get the equation from magic */
		/* FIXME!!! I don't know how to do PCA! it hurts my brain! for now,
		 just blindly hope that the line isn't too vertical */
		float ml = ss_xyl / ss_xxl;
		float bl = s_yl / nl - ml * s_xl / nl;

		float mr = ss_xyr / ss_xxr;
		float br = s_yr / nr - mr * s_xr / nr;

		/*System.err.println("ss_xx " + ss_xx + "; ss_yy + " + ss_yy + "; ss_xy " + ss_xy);*/
		/*System.err.println("cov(xx) " + cov_xx + ", cov(yy) " + cov_yy + ", cov(xy) " + cov_xy);*/
		/*System.err.println("" + A + "x + " + B + "y = " + C);*/
		System.err.println("yl = " + ml + "*xl + " + bl);
		System.err.println("yr = " + mr + "*xr + " + br);

		/* fixme: this is stupid; if we knew how, we would just go here
		 directly */
		/*         y = mx + b
		  mx - y + b = 0
		 (ss_xy / ss_xx) x -        y + ((s_y/n) - ss_xy / ss_xx * (s_x/n)) = 0
		 (ss_xy)x          - (ss_xx)y + (ss_xx * (s_y/n) - ss_xy * (s_x/n)) = 0
		 Ax + By + C = 0 */
		float one_norm;

		float Al = ss_xyl;
		float Bl = -ss_xxl;
		float Cl = ss_xxl * (s_yl/nl) - ss_xyl * (s_xl/nl);
		one_norm = 1f / (float)Math.hypot(Al, Bl);
		if(Cl < 0) one_norm = -one_norm;
		Al *= one_norm;
		Bl *= one_norm;
		Cl *= one_norm;
		
		float Ar = ss_xyr;
		float Br = -ss_xxr;
		float Cr = ss_xxr * (s_yr/nr) - ss_xyr * (s_xr/nr);
		one_norm = 1f / (float)Math.hypot(Ar, Br);
		if(Cr < 0) one_norm = -one_norm;
		Ar *= one_norm;
		Br *= one_norm;
		Cr *= one_norm;
		
		System.err.println("" + Al + "*xl + " + Bl + "*yl + " + Cl + " = 0");
		System.err.println("" + Ar + "*xr + " + Br + "*yr + " + Cr + " = 0");

		/* covarient basis . . . metric tensor . . . bla bla bla */
		/* [ Ar Br Cr ]
		   [ Al Bl Cl ]
		   [  0  0  1 ] */

		/* write gnuplot file */
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("robot.gnu", "UTF-8");
			writer.println("set term postscript eps enhanced");
			writer.println("set output \"robot.eps\"");
			writer.println("set xlabel \"x\"");
			writer.println("set ylabel \"y\"");
			writer.println("set size ratio -1");
			writer.println("#set size square");
			writer.println("set palette maxcolors 3");
			writer.println("set palette defined (0 '#bbbbbb', 1 '#990000', 2 '#009999')");
			writer.println("l(x) = " + ml + "*x + " + bl);
			writer.println();
			writer.println("r(x) = " + mr + "*x + " + br);
			writer.println("set object circle at first 0,0 radius char 0.5 fillcolor rgb 'red' fillstyle solid noborder");
			writer.println("plot \"robot.data\" using 1:2:3 title \"Robot\" with linespoints palette, \\");
			writer.println("l(x) title \"left " + Al + "*x + " + Bl + "*y + " + Cl + " = 0\", \\");
			writer.println("r(x) title\"right " + Ar + "*x + " + Br + "*y + " + Cr + " = 0\"");
		} catch(Exception e) {
			System.err.println("Not created: " + e.getMessage());
		} finally {
			writer.close();
		}

		return true;
	}

}
