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
	private static final int      LOCO_DELAY = 30; /* SONAR_DELAY + processing */
	private static final int BLUETOOTH_DELAY = 0/*10000*/;

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

	/** overrides localise in Robot */
	protected void localise() {
		Display.setText("Bluetooth " + RConsole.isOpen());
		/* fixme: disable the timer? */
		//RConsole.openBluetooth(BLUETOOTH_DELAY);
		if(!RConsole.isOpen()) Display.setText("Never mind.");
		status = Status.LOCALISING;
		this.turn(100f);
		/* timer.setDelay(LOCO_DELAY)? */
	}

	/** overrides localising in Robot */
	protected void localising() {

		Position p = odometer.getPosition();
		float    t = (float)Math.toDegrees(p.getTheta());
		/* sonic is after odometer.getPositionCopy() because it moved */
		int  sonic = pingSonar();
		/* check error: out of 0 .. 255 (it hasn't happened yet, but it's in
		 the docs) */
		if(sonic < 0 || sonic > 255) {
			Display.setText("err sonic " + sonic);
		} else {
			/* record */
			pings.add(new Ping(p, sonic));
			
			/* display */
			//Display.setText2("" + (int)t + ": #" + pings.size() + ",us" + sonic);
		}

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
		isTurned = false;

		/* code only goes though to this point on last localising */
		this.stop();
		status = Status.IDLE;
		/* timer.setDelay(NAV_DELAY)? */

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
			Display.setText("loco " + odometer.getPosition());
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

	/** @return the colour as enum Value (STYROFOAM/WOOD) */
	/* fixme: move to swagbot? */
	public Colour.Value getColour() {
		return colour.getColourValue();
	}
	/** @return a range that indicates our certaitanty that it's styrofoam
	 in [0, 1] */
	public float getStyrofoam() {
		return colour.getStyrofoamProbability();
	}

}
