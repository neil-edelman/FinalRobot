/* localising robot extends robot */

import lejos.nxt.UltrasonicSensor;
import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;         /* pause */

import lejos.nxt.comm.RConsole; /* take out in production */

import java.util.ArrayList;

public class Locobot extends Robot {
	/* SONAR_DELAY > (255cm) * 2 / (340m/s * 100cm/m) = 15ms (leJOS says 20ms) */
	private static final int     SONAR_DELAY = 20;
	private static final int BLUETOOTH_DELAY = 10000;

	private   Colour           colour;
	protected UltrasonicSensor sonic;
	protected LightSensor      light;

	public Locobot(final SensorPort sonicPort, final SensorPort lightPort, final SensorPort colourPort) {
		super();
		sonic  = new UltrasonicSensor(sonicPort);
		light  = new LightSensor(lightPort);
		colour = new Colour(colourPort);
	}

	private ArrayList<Ping> pings = new ArrayList<Ping>(128);
	private boolean isTurned = false;

	/** this is overriden */
	protected void localise() {
		Display.setText("Waiting for Blue");
		RConsole.openBluetooth(BLUETOOTH_DELAY);
		if(!RConsole.isOpen()) Display.setText("Never mind.");
		status = Status.LOCALISING;
		this.turn(100f);
	}

	/** this is overriden */
	protected void localising() {

		Position p = odometer.getPositionCopy();
		float    t = (float)Math.toDegrees(p.getTheta());
		/* sonic is after odometer.getPositionCopy() because it moved */
		int  sonic = pingSonar();

		/* record; fixme: check error out of 0 .. 255, it hasn't happened yet */
		pings.add(new Ping(p, sonic));

		/* display */
		Display.setText("" + (int)t + ": #" + pings.size() + ",us" + sonic);

		/* if it has not turned, return */
		if(!isTurned) {
			if(t <= -90f) {
				isTurned = true;
				Display.setText("turned!");
			}
			return;
		} else if(t <= 0f) {
			return;
		}

		/* code only goes though to this point on last localising */
		this.stop();
		status = Status.IDLE;

		/* send? */
		if(RConsole.isOpen()) {
			for(Ping ping : pings) {
				p = ping.getPosition();
				RConsole.println("" + p.x + "\t" + p.y + "\t" + p.getDegrees() + "\t" + ping.getCm());
			}
			RConsole.close();
		}

		/* calculate */
		Ping.setOdometer(odometer);
		if(Ping.correct(pings)) {
			Display.setText("loco " + odometer.getPositionCopy());
		} else {
			Display.setText("loco failed");
		}
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
	/* fixme: move to swagbot? */
	public Colour.Value getColour() {
		return colour.getColourValue();
	}
	
	public float getStyrofoam() {
		return colour.getStyrofoamProbability();
	}

}
