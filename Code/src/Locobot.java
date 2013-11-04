/* localising robot extends robot */

import lejos.nxt.UltrasonicSensor;
import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound; /* pause */

public class Locobot extends Robot {
	/* SONAR_DELAY > (255cm) * 2 / (340m/s * 100cm/m) = 15ms (leJOS says 20ms) */
	private static final int SONAR_DELAY = 20;

	private   Colour           colour;
	protected UltrasonicSensor sonic;

	public Locobot(final SensorPort sonicPort, final SensorPort colourPort) {
		super();
		sonic  = new UltrasonicSensor(sonicPort);
		colour = new Colour(colourPort);
	}

	/* temp sensing array (fixme! varible numbers) */
	private static final int LOCO_NO = 128;
	private byte locoCm[] = new byte[LOCO_NO]; /* ~76 */
	private float locoT[] = new float[LOCO_NO]; /* ~76 */
	private int locoCount;

	/** override this method */
	protected void localise() {
		status = Status.LOCALISING;
		//Display.setText2(""+this.pingSonar());
		this.turn(100f);
	}

	/** override this method */
	protected void localising() {

		Position p = odometer.getPositionCopy();
		int  sonic = pingSonar();
		float    t = (float)Math.toDegrees(p.getTheta());

		/* record */
		locoCm[locoCount] = (byte)sonic; /* fixme: ignores errors! */
		locoT[locoCount]  = t;
		locoCount++;

		/* display */
		Display.setText("t=" + (int)t + ";#" + locoCount + ",us" + sonic);
		if(t >= 0f || t <= -5f) return;

		/* code only goes though to this point on last localise */
		this.stop();
		status = Status.IDLE;

		/* calculate (FIXME!) */
		/* fixme: it only localises facing out, but the same idea */
		int left, right;
		for(left = 0; left < locoCount; left++) {
			if(lt(locoCm[left], (byte)50)) break;
		}
		for(right = locoCount - 1; right >= 0; right--) {
			if(lt(locoCm[right], (byte)50)) break;
		}
		Display.setText2("(" + locoCount + ") " + (int)locoT[left] + "; " + (int)locoT[right]);
	}

	/** why they don't have unsigned compare in the Java specs is beyond me,
	 something like <{unsigned}; I mean it's in the hardware, and I occasionally
	 need to use it (okay, so this is the only time I've used it) */
	private static boolean lt(final byte a, final byte b) {
		return (a < b) ^ ((a < 0) != (b < 0));
	}

	/** "The return value is in centimeters. If no echo was detected, the
	 returned value is 255. The maximum range of the sensor is about
	 170 cm." */
	/* fixme: should be in it's own class Sensors */
	/* fixme: should be a timerlistener to wait for return */
	/* fixme: capture() mode should be used as there are two robots on the
	 field? definately with two sonic sensors on one robot; there is no
	 documentation on this (in fact, I think very little is know about sonic
	 sensors save by the person at lego who designed them) */
	/* fixme: it returns actually up to 8 distances of separate objects
	 public int getDistances(int[8] dist); (how are these returned?) */
	public int pingSonar() {
		sonic.ping();
		Sound.pause(SONAR_DELAY);
		return sonic.getDistance();
	}

	/** returns the colour as enum Value (STYROFOAM/WOOD) */
	public Colour.Value getColour() {
		return colour.getColourValue();
	}

}
