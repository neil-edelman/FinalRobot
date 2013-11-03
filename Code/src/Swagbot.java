import lejos.nxt.UltrasonicSensor;
import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound; /* pause */

public class Swagbot extends Robot {
	class Loco {
		byte  cm;
		float theta;
	}

	/* SONAR_DELAY > (255cm) * 2 / (340m/s * 100cm/m) = 15ms (leJOS says 20ms) */
	private static final int SONAR_DELAY = 20;

	private Colour       colour = new Colour();
	private UltrasonicSensor us = new UltrasonicSensor(SensorPort.S4);

	private Loco locoData[] = new Loco[128]; /* ~76 */
	private int locoCount;

	public Swagbot() {
		super();
	}

	/** override this method */
	protected void localise() {
		status = Status.LOCALISING;
		this.turn(100f);
	}

	/** override this method */
	protected void localising() {
		Position p = odometer.getPositionCopy();
		int  sonic = pingSonar();
		float    t = (float)Math.toDegrees(p.getTheta());
		locoData[locoCount].theta = p.getTheta();
		locoData[locoCount].cm = (byte)sonic; /* fixme: ignores errors! */
		Display.setText("t=" + (int)t + ";#" + locoCount + ",us" + sonic);
		if(t >= 0f || t <= -5f) return;

		/* code only goes though to this point on last localise */
		this.stop();
		status = Status.IDLE;

		/* calculate; locoData[locoCount-1] is the distance (FIXME!) */
		/* fixme: it only localises facing out, but the same idea */
		int left, right;
		for(left = 0; left < locoCount; left++) {
			if(locoData[left].cm < 50) break;
		}
		for(right = locoCount - 1; right >= 0; right++) {
			if(locoData[right].cm < 50) break;
		}
		Display.setText(locoData[left].theta + "; " + locoData[right].theta);
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
	 public int getDistances(int[8] dist);*/
	public int pingSonar() {
		us.ping();
		Sound.pause(SONAR_DELAY);
		return us.getDistance();
	}

	/** returns the colour as enum Value (STYROFOAM/WOOD) */
	public Colour.Value getColour() {
		return colour.getColourValue();
	}

}
