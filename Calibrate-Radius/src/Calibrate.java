/* calibrate the radius */

import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

public class Calibrate {
	private static final int FORWARD_SPEED = 250;
	/* you want to know this */
	private static final float RADIUS      = 2.72f;
	/* 2.5 too small
	   2.665 is slighly small
	   2.7 just a little too small
	   2.8 too large
	   2.75 too large
	   2.72 pretty much */

	static final NXTRegulatedMotor lMotor = Motor.A, rMotor = Motor.B;

	public static void main(String args[]) {

		/* fun */
		Button.setKeyClickLength(500);
		Button.setKeyClickTone(Button.ID_ENTER, 500000);
		Button.setKeyClickVolume(10);

		driveLeg(30.48f * 2f);

		System.out.println("press enter");
		while((Button.waitForAnyPress() & Button.ID_ENTER) == 0);
	}

	/** this is for calibrating; 3 squares 91.44 -> 30.48 cm / tile */
	public static void driveLeg(final float cm) {
		float circumference = 2.0f * (float)Math.PI * RADIUS;
		float      toLinear = 360f / circumference;

		/* forward */
		lMotor.setSpeed(FORWARD_SPEED);
		rMotor.setSpeed(FORWARD_SPEED);
		lMotor.rotate((int)(cm * toLinear), true);
		rMotor.rotate((int)(cm * toLinear), false);
	}
}
