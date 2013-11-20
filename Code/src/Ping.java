/** this is a struct to store ultrasonic sonar information, we want this to be
 as small as possible since we're storing a lot of them
 
 also does the details of localisiation */

import java.lang.IllegalArgumentException;
import java.lang.IndexOutOfBoundsException;
import java.util.ArrayList;

public class Ping {
	/********* copy/paste here ************/

	private static final float TWO_PI = (float)(2*Math.PI);
	private static final float     PI = (float)(Math.PI);

	/* return values from strightish */
	private static int rangeLower, rangeHigher;
	
	/* struct is all these; fixme: compress space */
	private Position position = new Position();
	private int      cm;
	private float    x, y; /* derived */
	private int colour;    /* gnuplot */
	
	/** records a ping
	 @author Neil
	 @param p       position of the reading
	 @param reading cm reading of the ping */
	public Ping(final Position p, final int reading) {
		position.set(p);
		cm = reading;
		/* derive */
		float a = p.getRadians();
		float b = cm + Hardware.sonicForward;
		x = p.x + (float)Math.cos(a) * b;
		y = p.y + (float)Math.sin(a) * b;
	}
	
	/** getters for logging
	 @author Neil */
	Position getPosition() { return position; }
	
	/** getters for logging
	 @author Neil */
	int getCm() { return cm; }
	
	/** corrects the odometer heading and distances based on pings from
	 localisation; assumes (1) the robot is placed in a corner, the new
	 (x ,y) will be aligned with the corner; (2) the thing with the smallest
	 distance are the walls (could fix this, but we're gaurateed this;) (3) the
	 pings has sufficent pings to be useful (linear regression is taken of the
	 two sides, so we'll need at least four pings; realistically more)
	 @param pings    an ArrayList<Ping> of pings
	 @param odometer an odometer to correct
	 @return         wheter the odometer was successfully localised
	 @throws IllegalArgumentException the odometer is null (no longer)
	 @throws Exception                failed to localise */
	public static void correct(final ArrayList<Ping> pings, final Odometer odometer) throws Exception {
		
		//if(odometer == null) throw new IllegalArgumentException("no odometer");
		final int size = pings.size();
		
		/* determine the area that is closest taking the minimum;
		 should be the integral of all the area, but this is okay,
		 we expect the closest thing to be the wall */
		int closestIndex = 0;
		{
			int closest = 255, cm;
			for(int i = 0; i < size; i++) {
				cm = pings.get(i).cm;
				if(cm < closest) {
					closest      = cm;
					closestIndex = i;
				}
			}
			/* in a big room? */
			if(closest >= 255) throw new Exception("all 255");
		}
		
		/* now go to the 255's at either end (we assume that 255's exist) */
		int left255, right255;
		for(left255 = closestIndex - 1; ; left255--) {
			if(left255 < 0) left255 = size - 1;
			if(left255 == closestIndex) throw new Exception("all <255");
			if(pings.get(left255).cm >= 255) break;
		}
		for(right255 = closestIndex + 1; ; right255++) {
			if(right255 >= size) right255 = 0;
			/* if(r == closestIndex) . . . is never going to happen */
			if(pings.get(right255).cm >= 255) break;
		}
		
		/* select 1/4 and 3/4 of [left, right] to expand about to give a line */
		int yAxis, xAxis;
		{
			int rEff = right255;
			if(rEff < left255) rEff += size;
			yAxis = left255 + (rEff - left255) * 1 / 4;
			if(yAxis >= size) yAxis -= size;
			xAxis = left255 + (rEff - left255) * 3 / 4;
			if(xAxis >= size) xAxis -= size;
		}
		straightishLine(pings, yAxis);
		int yl = rangeLower;
		int yh = rangeHigher;
		straightishLine(pings, xAxis);
		int xl = rangeLower;
		int xh = rangeHigher;
		
		/* covarience */
		float s_xl = 0, s_yl = 0, ss_xxl = 0, ss_yyl = 0, ss_xyl = 0;
		float avg_xl, avg_yl, cov_xx_n2l, cov_yy_n2l, cov_xy_n2l;
		int nl = 0;
		float s_xr = 0, s_yr = 0, ss_xxr = 0, ss_yyr = 0, ss_xyr = 0;
		float avg_xr, avg_yr, cov_xx_n2r, cov_yy_n2r, cov_xy_n2r;
		int nr = 0;
		{
			Ping ping;
			
			for(int i = yl; ; i++) {
				if(i >= size) i = 0;
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
			if(nl <= 2) throw new Exception("y has <=2 points");
			avg_xl = s_xl / nl;
			avg_yl = s_yl / nl;
			cov_xx_n2l = ss_xxl*nl - s_xl*s_xl;
			cov_yy_n2l = ss_yyl*nl - s_yl*s_yl;
			cov_xy_n2l = ss_xyl*nl - s_xl*s_yl;
			System.err.println(" "+avg_xl+" "+avg_yl+" "+cov_xx_n2l+" "+cov_yy_n2l+" "+cov_xy_n2l);
			
			for(int i = xl; ; i++) {
				if(i >= size) i = 0;
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
			if(nr <= 2) throw new Exception("x has <=2 points");
			avg_xr = s_xr / nr;
			avg_yr = s_yr / nr;
			cov_xx_n2r = ss_xxr*nr - s_xr*s_xr;
			cov_yy_n2r = ss_yyr*nr - s_yr*s_yr;
			cov_xy_n2r = ss_xyr*nr - s_xr*s_yr;
		}
		
		/* get the equation from math and standard form;
		 fixme: principal component analysis allows this to be numically
		 stable, but I tried and it's hard; separate into 2 to be good */
		/*         y = mx + b
		 mx - y + b = 0
		 (ss_xy / ss_xx) x -        y + ((s_y/n) - ss_xy / ss_xx * (s_x/n)) = 0
		 (ss_xy)x          - (ss_xx)y + (ss_xx * (s_y/n) - ss_xy * (s_x/n)) = 0
		 Ax + By + C = 0 */
		
		boolean isYFlipped = false, isXFlipped = false;
		
		/* y(x) -> x(y) */
		if(Math.abs(cov_xx_n2l) < Math.abs(cov_yy_n2l)) {
			isYFlipped = true;
			/* I don't think Java would appriciate *(int *)&a ^= b ^= a ^= b */
			float t;
			t      = avg_xl;
			avg_xl = avg_yl;
			avg_yl = t;
			t          = cov_xx_n2l;
			cov_xx_n2l = cov_yy_n2l;
			cov_yy_n2l = t;
		}
		
		if(Math.abs(cov_xx_n2r) < Math.abs(cov_yy_n2r)) {
			isXFlipped = true;
			float t;
			t      = avg_xr;
			avg_xr = avg_yr;
			avg_yr = t;
			t          = cov_xx_n2r;
			cov_xx_n2r = cov_yy_n2r;
			cov_yy_n2r = t;
		}
		
		float ml  = cov_xy_n2l / cov_xx_n2l;
		float bl  = avg_yl - ml*avg_xl;
		
		float Al = (isYFlipped) ? -cov_xx_n2l : cov_xy_n2l;
		float Bl = (isYFlipped) ?  cov_xy_n2l : -cov_xx_n2l;
		float Cl =  cov_xx_n2l*avg_yl - cov_xy_n2l*avg_xl;
		
		float mr  = cov_xy_n2r / cov_xx_n2r;
		float br  = avg_yr - mr*avg_xr;
		
		float Ar = (isXFlipped) ? -cov_xx_n2r : cov_xy_n2r;
		float Br = (isXFlipped) ?  cov_xy_n2r : -cov_xx_n2r;
		float Cr =  cov_xx_n2r*avg_yr - cov_xy_n2r*avg_xr;
		
		/* normalise */
		{
			float one_norm;
			
			/*one_norm = 1f / (float)Math.hypot(Al, Bl); <- no Math.hypot in nxj */
			one_norm = 1f / (float)Math.sqrt(Al*Al + Bl*Bl);
			if(Cl < 0) one_norm = -one_norm;
			Al *= one_norm;
			Bl *= one_norm;
			Cl *= one_norm;
			
			one_norm = 1f / (float)Math.sqrt(Ar*Ar + Br*Br);
			if(Cr < 0) one_norm = -one_norm;
			Ar *= one_norm;
			Br *= one_norm;
			Cr *= one_norm;
		}
		if(Cl < Hardware.clearance || Cr < Hardware.clearance) {
			throw new Exception("" + Math.round(Cl) + "," + Math.round(Cr) +
								" unfeasible");
		}
		
		/* covarient basis . . . metric tensor . . . bla bla bla */
		
		/* Al*x + Bl*y + Cl = 0 (normalised) has direction -Bl*x + Al*y (y+)
		 Ar*x + Br*y + Cr = 0 (normalised) has direction  Br*x - Ar*y (x+)
		 [  Br -Ar Cr ]
		 [ -Bl  Al Cl ]
		 [   0   0  1 ] just for convenience */
		float a = Br,  b = -Ar;
		float c = -Bl, d = Al;
		
		/* compute the inverse */
		float det = a*d - b*c;
		/* not needed
		 float adbc = 1f / det;
		 float n =  d*adbc, m = -b*adbc;
		 float o = -c*adbc, p =  a*adbc;*/
		if(det < Hardware.detMin) throw new Exception("det " + det + " :(");

		/* so we need a to spectrally decompose the matrix into a
		 unitary (viz orthogonal) matrix (det 1) and diagonal eigenvalues by
		 eigendecomposition (not a good route)
		 [  Br -Ar ]    [ e1    ]
		 [ -Bl  Al ]= U [    e2 ] U^T */
		/*float determinant = a*a + d*d - 2*a*d + 4*b*c; <- sqrt is complex
		 float eigenvalue1 = (a + d - determinant) / 2f;
		 float eigenvalue2 = (a + d + determinant) / 2f; */
		
		/* since the metric is slanted (in general,) weight by the components
		 by the others' error values (hand wavey, but meh) */
		float rms_el = 0, rms_er = 0;
		{
			Ping ping;
			
			for(int i = yl; ; i++) {
				if(i >= size) i = 0;
				ping = pings.get(i);
				rms_el += (Al*ping.x + Bl*ping.y + Cl) * (Al*ping.x + Bl*ping.y + Cl);
				if(i == yh) break;
			}
			rms_el = (float)Math.sqrt(rms_el) / nl;
			
			for(int i = xl; ; i++) {
				if(i >= size) i = 0;
				ping = pings.get(i);
				rms_er += (Ar*ping.x + Br*ping.y + Cr) * (Ar*ping.x + Br*ping.y + Cr);
				if(i == xh) break;
			}
			rms_er = (float)Math.sqrt(rms_er) / nr;
		}
		/* there's not much chance of this given strightishLine */
		if(rms_el > Hardware.maxR2Error || rms_er > Hardware.maxR2Error) {
			throw new Exception("too noisy "+rms_er+","+rms_el);
		}
		
		/* in the QR and QL decomposion, the Q is unitary and the angle,
		 tan t = -b/a = Ar/Br; tan t = c/d = -Bl/Al */
		float angler = (float)Math.atan2(-b, a);
		float anglel = (float)Math.atan2(c,  d);
		/* make sure we're on the same branch */
		if     (anglel > angler + PI) angler += TWO_PI;
		else if(angler > anglel + PI) anglel += TWO_PI;
		float angle = (anglel*rms_er + angler*rms_el) / (rms_el + rms_er);
		if(angle > PI) angle -= TWO_PI;
		
		/* the xy c\:oordinates: x = distance to the y-axis, vise versa */
		/* fixme: check that it's w/i bounds */
		odometer.addXY(Cl, Cr);
		odometer.addRadians(angle);
		
		/* choose appropriate comment \/ */
		Display.setText("Ping ("+Cl+", "+Cr+":"+Math.toDegrees(angle)+")");
		//write(isYFlipped, isXFlipped, left255, yAxis, xAxis, right255, yl, yh, xl, xh, nl, nr, ml, bl, mr, br, Al, Bl, Cl, Ar, Br, Cr, det, rms_el, rms_er, angler, anglel, angle);
	}
	
	/** given an array of pings and an index, sets [rangeLower, rangeHigher]
	 to be anything that sort of looks like a stright line (within CUTOFF_ANGLE)
	 about that point
	 <p>
	 this was determined to be worse than blind picking the middle in some cases
	 but gets rid of objects on the walls
	 @author Neil
	 @param pings list of pings
	 @param about an index into pings
	 @throws IndexOutOfBoundsException if the about is not an index */
	private static void straightishLine(final ArrayList<Ping> pings, final int about) {
		final int size = pings.size();
		if(about < 0 || about >= size) throw new IndexOutOfBoundsException("ping " + about);
		boolean isLeftBlocked = false, isRightBlocked = false;
		boolean chooseLeft = false;
		float dx, dy, angle, dist, angleComp, distComp, abs_da, choice;
		int candidate;
		int left, right;
		Ping ping, leftPing, rightPing;
		
		left = right = about;
		for( ; ; ) {
			/* choose left and right alternatingly until there are none that
			 fit the critirion (Doctor Who: Turn Left) */
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
			/* compare the angle of the left-right and the potential new one */
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
				abs_da = (angleComp > angle) ?
				(angleComp - angle) : (angle - angleComp);
				if(abs_da > PI) abs_da = TWO_PI - abs_da;
				/*System.err.print("["+candidate+"]" +
				 Math.round(Math.toDegrees(angle))+
				 "-"+
				 Math.round(Math.toDegrees(angleComp))+
				 "="+
				 Math.round(Math.toDegrees(abs_da))+";");*/
				if(abs_da > Hardware.cutoffAngle) {
					/*System.err.print("stop("+chooseLeft+");");*/
					if(chooseLeft) isLeftBlocked  = true;
					else           isRightBlocked = true;
					continue;
				}
			}
			/* passed, grow the set */
			if(chooseLeft) left = candidate;
			else          right = candidate;
		}
		/*System.err.println("done");*/

		rangeLower  = left;
		rangeHigher = right;
	}

}
