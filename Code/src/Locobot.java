/** localising robot extends robot */

import lejos.nxt.UltrasonicSensor;
import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.comm.RConsole;
import lejos.util.Delay;

import java.util.ArrayList;

public class Locobot extends Robot {

	protected UltrasonicSensor sonic;
	protected LightSensor      light;

	/** creates the Locobot
	 @param sonicPort SensorPort on which the sonic sensor is
	 @param lightPort SensorPort on which the light sensor facing the grond is */
	public Locobot() {
		super();
		sonic  = new UltrasonicSensor(Hardware.sonicPort);
		light  = new LightSensor(Hardware.lightPort); /* not used, yet */
	}

	private ArrayList<Ping> pings = new ArrayList<Ping>(Hardware.defaultMaxPings);
	private boolean isTurned = false;

	/** overrides localise in Robot; this uses a faster timer and assumes that
	 the localasition continues to completion where it will set it back
	 @author Neil */
	protected void localise() {
		this.turn(Hardware.locoSpeed);
		/* "Safe to call while start()ed" */
		timer.setDelay(Hardware.locoDelay);
		status = Status.LOCALISING;
	}

	/** overrides localising in Robot; turns and computes best-fit lines from
	 sensor data and modifies the odometer accordingly
	 @author Neil */
	protected void localising() {

		Position p = odometer.getPosition();
		float    t = (float)Math.toDegrees(p.getTheta());
		/* sonic is after odometer.getPosition() because it moved; I'm not sure
		 about this */
		int  sonic = pingSonar();
		/* check error: out of 0 .. 255 (it hasn't happened yet, but it's in
		 the docs) */
		if(sonic < 0 || sonic > 255) {
			Display.setText("err sonic " + sonic);
		} else {
			/* record */
			pings.add(new Ping(p, sonic));
			//Display.setText2("" + (int)t + ": #" + pings.size() + ",us" + sonic);
		}

		/* return without doing anything if it has not turned a full rotation */
		if(!isTurned) {
			if(t <= -90f) {
				isTurned = true;
				Display.setText("Halfway there.");
			}
			return;
		} else if(t <= 0f) {
			return;
		}
		isTurned = false;

		/* code only goes though to this point on last localising; return all
		 values to what they were pre-localising */
		this.stop();
		status = Status.IDLE;
		timer.setDelay(Hardware.navDelay);

		/* calculate */
		try {
			Ping.correct(pings, odometer);
		} catch(Exception e) {
			Display.setText(e.getMessage());
			Sound.beep();
			/* fixme: try again!!! */
		}

		/* send? */
		if(RConsole.isOpen()) {
			for(Ping ping : pings) {
				p = ping.getPosition();
				RConsole.println("" + p.x + "\t" + p.y + "\t" + p.getDegrees() + "\t" + ping.getCm());
			}
		}

	}

	/** pings the sonar; blocks SONAR_DELAY + calculation time
	<p>
	 "The return value is in centimeters. If no echo was detected, the
	 returned value is 255. The maximum range of the sensor is about
	 170 cm."
	<p>
	fixme: should be in it's own class Sensors
	<p>
	fixme: should be a timerlistener to wait for return
	<p>
	fixme: capture() mode should be used as there are two robots on the
	 field? definately with two sonic sensors on one robot; there is no
	 documentation on this (in fact, I think very little is know about sonic
	 sensors save by the person at lego who designed them)
	<p>
	fixme: it returns actually up to 8 distances of separate objects
	 public int getDistances(int[8] dist); (how are these returned?)
	 @author Neil, Alex
	 @return The cm distance (unless some error.) */
	public int pingSonar() {
		sonic.ping();
		Delay.msDelay(Hardware.sonarDelay);
		return sonic.getDistance();
	}

}
