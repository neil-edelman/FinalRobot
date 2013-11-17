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

		Ping.setOdometer(new Odometer()); /* stub */

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
			if(ping.cm >= 255) continue;
			System.out.println("" + ping.x + "\t" + ping.y + "\t" + ping.colour);
		 }
	}

	/* write(left, mid, right, nl, nr, ml, bl, mr, br, Al, Bl, Cl, Ar, Br, Cr, rms_el, rms_er, angler, anglel, angle); */
	static void write(final int left, final int mid, final int right,
					  final int nl, final int nr,
					  final float ml, final float bl,
					  final float mr, final float br,
					  final float Al, final float Bl, final float Cl,
					  final float Ar, final float Br, final float Cr,
					  final float rms_el, final float rms_er,
					  final float angler, final float anglel, final float angle) {

		System.err.println("left " + left + "--> " + mid + " <--" + right + " right");
		
		System.err.println("Left covarience of " + nl + " points.");
		System.err.println("Right covarience of " + nr + " points.");
		
		System.err.println("yl = " + ml + "*xl + " + bl);
		System.err.println("yr = " + mr + "*xr + " + br);
		
		System.err.println("" + Al + "*xl + " + Bl + "*yl + " + Cl + " = 0");
		System.err.println("" + Ar + "*xr + " + Br + "*yr + " + Cr + " = 0");
		
		/*System.err.println("{{" + a + ", " + b + "}, {" + c + ", " + d + "}}");*/
		/*System.err.println("det " + determinant + " eigenvalues {" + eigenvalue1 + ", " + eigenvalue2 + "}");*/

		System.err.println("error rms l " + rms_el + "; r " + rms_er);

		System.err.println("angle + using the right " + Math.toDegrees(angler));
		System.err.println("angle + using the left "  + Math.toDegrees(anglel));
		System.err.println("angle + using the the weighted averge " + Math.toDegrees(angle));
		
		System.err.println("Add (" + Cl + ", " + Cr + " : " + Math.toDegrees(angle) + ")");

		/* write gnuplot file */
		Position p = odometer.getPositionCopy();
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
			writer.println("set palette defined (0 '#bbbbbb', 1 '#990000', 2 '#009999', 3 '#90ff99')");
			writer.println("set label \"(" + Math.round(p.x) + ", " +
						   Math.round(p.y) + " : " +
						   Math.round(p.getDegrees()) + ")\" at graph 0.2, graph 0.2");
			writer.println("l(x) = " + ml + "*x + " + bl);
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
	}

	/********* copy/paste here ************/

	/***** fixme: have it in Robot.java? */
	private static final float LIGHT_BACK    = 12.2f;
	private static final float SONIC_FORWARD = 10.4f;
	//	private static final float  CUTOFF_ANGLE = (float)Math.toRadians(50.0);
	private static final float  CUTOFF_ANGLE = (float)Math.toRadians(60.0);

	private static Odometer odometer;
	private static int rangeLower, rangeHigher;

	private Position position = new Position();
	private int      cm;
	private float    x, y; /* derived */
	private int colour;    /* gnuplot */

	public Ping(final Position p, final int reading) {
		position.set(p);
		cm = reading;
		/* derive */
		float a = p.getRadians();
		float b = cm + SONIC_FORWARD;
		x = p.x + (float)Math.cos(a) * b;
		y = p.y + (float)Math.sin(a) * b;
	}

	/** sets the odometer used by correct */
	public static void setOdometer(final Odometer o) {
		odometer = o;
	}

	/** getters for logging */
	Position getPosition() { return position; }
	int getCm() { return cm; }

	public static boolean correct(final ArrayList<Ping> pings) {

		if(odometer == null) throw new IllegalArgumentException("no odometer");

		/* determine the area that is most close taking the minimum;
		 should be the integral of all the area, but this is okay,
		 we expect the closest thing to be the wall at the start */
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

		rEff = right;
		if(rEff < left) rEff += len;
		int yAxis = left + (rEff - left) * 1 / 4;
		if(yAxis >= len) yAxis -= len;
		int xAxis = left + (rEff - left) * 3 / 4;
		if(xAxis >= len) xAxis -= len;

		System.err.println("["+right+": x "+xAxis+" / y "+yAxis+" :"+left+"]");
		straightishLine(pings, yAxis);
		int yl = rangeLower;
		int yh = rangeHigher;
		straightishLine(pings, xAxis);
		int xl = rangeLower;
		int xh = rangeHigher;

		/*for(Ping ping : pings)
		 if(((i > left) && (i < mid)) ^ (left > mid))
		 else if(((i > mid) && (i < right)) ^ (mid > right))*/

		/* covarience */
		Ping ping;
		float s_xl = 0, s_yl = 0, ss_xxl = 0, ss_yyl = 0, ss_xyl = 0;
		int nl = 0;
		for(int i = yl; ; i++) {
			if(i > len) i = 0;
			ping = pings.get(i);
			ping.colour += 1;
			nl++;
			s_xl   += ping.x;
			s_yl   += ping.y;
			ss_xxl += ping.x * ping.x;
			ss_yyl += ping.y * ping.y;
			ss_xyl += ping.x * ping.y;
			if(i == yh) break;
		}
		if(nl <= 2) return false;
		float avg_xl = s_xl / nl, avg_yl = s_yl / nl;
		float cov_xx_n2l = ss_xxl*nl - s_xl*s_xl;
		float cov_yy_n2l = ss_yyl*nl - s_yl*s_yl;
		float cov_xy_n2l = ss_xyl*nl - s_xl*s_yl;

		float s_xr = 0, s_yr = 0, ss_xxr = 0, ss_yyr = 0, ss_xyr = 0;
		int nr = 0;
		for(int i = xl; ; i++) {
			if(i >= len) i = 0;
			ping = pings.get(i);
			ping.colour += 2;
			nr++;
			s_xr   += ping.x;
			s_yr   += ping.y;
			ss_xxr += ping.x * ping.x;
			ss_yyr += ping.y * ping.y;
			ss_xyr += ping.x * ping.y;
			if(i == xh) break;
		}
		if(nr <= 2) return false;
		float avg_xr = s_xr / nr, avg_yr = s_yr / nr;
		float cov_xx_n2r = ss_xxr*nr - s_xr*s_xr;
		float cov_yy_n2r = ss_yyr*nr - s_yr*s_yr;
		float cov_xy_n2r = ss_xyr*nr - s_xr*s_yr;

		/* get the equation from math */
		float ml  = cov_xy_n2l / cov_xx_n2l;
		float bl  = avg_yl - ml*avg_xl;

		float mr  = cov_xy_n2r / cov_xx_n2r;
		float br  = avg_yr - mr*avg_xr;

		/* get the standard form; fixme: principal component analysis allows
		 this to be numically stable, but I tried and it's hard */
		/*         y = mx + b
		  mx - y + b = 0
		 (ss_xy / ss_xx) x -        y + ((s_y/n) - ss_xy / ss_xx * (s_x/n)) = 0
		 (ss_xy)x          - (ss_xx)y + (ss_xx * (s_y/n) - ss_xy * (s_x/n)) = 0
		 Ax + By + C = 0 */
		float one_norm;

		float Al =  cov_xy_n2l;
		float Bl = -cov_xx_n2l;
		float Cl =  cov_xx_n2l*avg_yl - cov_xy_n2l*avg_xl;
		/*one_norm = 1f / (float)Math.hypot(Al, Bl); <- no Math.hypot in nxj */
		one_norm = 1f / (float)Math.sqrt(Al*Al + Bl*Bl);
		if(Cl < 0) one_norm = -one_norm;
		Al *= one_norm;
		Bl *= one_norm;
		Cl *= one_norm;
		
		float Ar =  cov_xy_n2r;
		float Br = -cov_xx_n2r;
		float Cr =  cov_xx_n2r*avg_yr - cov_xy_n2r*avg_xr;
		one_norm = 1f / (float)Math.sqrt(Ar*Ar + Br*Br);
		if(Cr < 0) one_norm = -one_norm;
		Ar *= one_norm;
		Br *= one_norm;
		Cr *= one_norm;
		
		/* covarient basis . . . metric tensor . . . bla bla bla */

		/* Al*x + Bl*y + Cl = 0 (normalised) has direction -Bl*x + Al*y (y+)
		   Ar*x + Br*y + Cr = 0 (normalised) has direction  Br*x - Ar*y (x+)
		 [  Br -Ar Cr ]
		 [ -Bl  Al Cl ]
		 [   0   0  1 ] just for convenience */
		float a = Br,  b = -Ar;
		float c = -Bl, d = Al;

		/* compute the inverse (not needed) */
		float det = a*d - b*c;
		/* fixme: if the det is too far from one, we're skewed and should loco
		 again */
		/*float adbc = 1f / (a*d - b*c);
		float n =  d*adbc, m = -b*adbc;
		float o = -c*adbc, p =  a*adbc;*/

		/* so we need a to spectrally decompose the matrix into a
		 unitary (viz orthogonal) matrix (det 1) and diagonal eigenvalues (no)
		[  Br -Ar ]    [ e1    ]
		[ -Bl  Al ]= U [    e2 ] U^T */
		/*float determinant = a*a + d*d - 2*a*d + 4*b*c;
		float eigenvalue1 = (a + d - determinant) / 2f;
		float eigenvalue2 = (a + d + determinant) / 2f;*/

		/* since the metric is slanted (in general,) weight by the components
		 by the others' error values (hand wavey, but in practice the difference
		 is minimal) . . . I tried eigendecomposition, it hurt my brian */
		float rms_el = 0, rms_er = 0;
		for(int i = left + 1; ; i++) {
			if(i >= len) i = 0;
			if(i == mid) break;
			ping = pings.get(i);
			rms_el += (Al*ping.x + Bl*ping.y + Cl) * (Al*ping.x + Bl*ping.y + Cl);
		}
		rms_el = (float)Math.sqrt(rms_el) / nl;
		for(int i = mid; ; i++) {
			if(i >= len) i = 0;
			if(i == right) break;
			ping = pings.get(i);
			rms_er += (Ar*ping.x + Br*ping.y + Cr) * (Ar*ping.x + Br*ping.y + Cr);
		}
		rms_er = (float)Math.sqrt(rms_er) / nr;

		/* in the QR and QL decomposion, the Q is unitary and the angle,
		 tan t = -b/a = Ar/Br; tan t = c/d = -Bl/Al */
		float angler = (float)Math.atan2(-b, a);
		float anglel = (float)Math.atan2(c,  d);
		/* make sure we're on the same branch */
		if     (anglel > angler + Math.PI) angler += 2f*Math.PI;
		else if(angler > anglel + Math.PI) anglel += 2f*Math.PI;
		float angle = (anglel*rms_er + angler*rms_el) / (rms_el + rms_er);
		if(angle > Math.PI) angle -= 2f*Math.PI;

		/* the xy c\:oordinates: x = distance to the y-axis, vise versa */
		/* fixme: check that it's w/i bounds */
		odometer.addXY(Cl, Cr);
		odometer.addRadians(angle);

		/* comment this \/ */
		write(left, mid, right, nl, nr, ml, bl, mr, br, Al, Bl, Cl, Ar, Br, Cr, rms_el, rms_er, angler, anglel, angle);
		/* /\ */

		return true;
	}

	/** expands a strightish line (within CUTOFF_ANGLE) and places the restults
	 in rangeLower and rangeHigher; this was determined to be worse than blind
	 picking the middle but gets rid of objects on the walls
	 @author Neil */
	private static void straightishLine(final ArrayList<Ping> pings, final int about) {
		final int size = pings.size();
		if(about < 0 || about >= size) throw new IndexOutOfBoundsException("ping " + about);
		boolean isLeftBlocked = false, isRightBlocked = false;
		/*boolean isLeftFishy = false, isRightFishy = false;*/
		boolean chooseLeft = false;
		float dx, dy, angle = 0, angleComp = 0, da = 0, ada = 0;
		int candidate;
		int left, right;
		Ping ping, leftPing, rightPing;

		left = right = about;
		for( ; ; ) {
			/* choose left and right until there are none,
			 like Doctor Who: Turn Left */
			if(isLeftBlocked) {
				if(isRightBlocked) break;
				chooseLeft = false;
			} else {
				if(isRightBlocked) {
					chooseLeft = true;
				} else {
					chooseLeft = !chooseLeft;
				}
			}
			/* move one over */
			if(chooseLeft) {
				candidate = left - 1;
				if(candidate  < 0)    candidate = size - 1;
			} else {
				candidate = right + 1;
				if(candidate >= size) candidate = 0;
			}
			ping = pings.get(candidate);
			/* if it's 255, stop */
			if(ping.cm >= 255) {
				if(chooseLeft) isLeftBlocked  = true;
				else           isRightBlocked = true;
				continue;
			}
			/* the angle */
			if(left != right) {
				leftPing  = pings.get(left);
				rightPing = pings.get(right);
				dx    = leftPing.x - rightPing.x;
				dy    = leftPing.y - rightPing.y;
				angle = (float)Math.atan2(dy, dx);
				if(chooseLeft) {
					dx = ping.x - leftPing.x;
					dy = ping.y - leftPing.y;
				} else {
					dx = rightPing.x - ping.x;
					dy = rightPing.y - ping.y;
				}
				angleComp = (float)Math.atan2(dy, dx);
				da = angleComp - angle;
				ada = (da > 0) ? (da) : (-da);
				if(ada > CUTOFF_ANGLE) {
					if(chooseLeft) isLeftBlocked  = true;
					else           isRightBlocked = true;
					continue;
					/*if(chooseLeft) isLeftFishy  = true;
					else           isRightFishy = true;
					if(isLeftFishy && isRightFishy) break;
					continue;
				} else {
					if(chooseLeft) isLeftFishy  = false;
					else           isRightFishy = false;*/
				}
			}
			/* passed */
			if(chooseLeft) left = candidate;
			else          right = candidate;
			System.err.print(candidate + "("+Math.round(Math.toDegrees(ada))+");");
		}
		System.err.println("(" + left + ", " + right + ")");
		rangeLower  = left;
		rangeHigher = right;
	}

}
