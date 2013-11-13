/* this is a struct to store ultrasonic sonar information, we want this to be
 as small as possible since we're storing a lot of them
 
 also does locolisiation */

import java.lang.IllegalArgumentException;
import java.util.ArrayList;

public class Ping {
	/********* copy/paste here ************/
	
	/***** fixme: have it in Robot.java? */
	private static final float LIGHT_BACK    = 12.2F;
	private static final float SONIC_FORWARD = 10.4f;
	
	private static Odometer odometer;
	
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
		if(nl <= 2) return false;
		float avg_xl = s_xl / nl, avg_yl = s_yl / nl;
		float cov_xx_n2l = ss_xxl*nl - s_xl*s_xl;
		float cov_yy_n2l = ss_yyl*nl - s_yl*s_yl;
		float cov_xy_n2l = ss_xyl*nl - s_xl*s_yl;
		
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
		/*float adbc = 1f / (a*d + b*c);
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
		odometer.setXY(Cl, Cr);
		odometer.setRadians(angle);
		
		/* comment this \/ */
		//write(left, mid, right, nl, nr, ml, bl, mr, br, Al, Bl, Cl, Ar, Br, Cr, rms_el, rms_er, angler, anglel, angle);
		/* /\ */
		
		return true;
	}
	
}
