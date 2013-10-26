/* calibrate the radius */

import lejos.nxt.Button;
import lejos.nxt.NXTRegulatedMotor;

class Lab5 {
	private static final int FORWARD_SPEED = 250;
	/* you want to know this */
	private static final float RADIUS      = 2.665f;

	static final NXTRegulatedMotor lMotor = Motor.A, rMotor = Motor.B;

	public static void main(String args[]) {

		/* fun */
		Button.setKeyClickLength(500);
		Button.setKeyClickTone(Button.ID_ENTER, 500000);
		Button.setKeyClickVolume(10);

		driveLeg(30.48f * 2f);

		System.out.println("press enter");
		while((Button.waitForAnyPress() & Button.ID_ENTER) != 0);

	}

	/** this is for calibrating; 3 squares 91.44 -> 30.48 cm / tile */
	public void driveLeg(final float cm) {
		float circumference = 2.0 * Math.PI * RADIUS;
		float      toLinear = 360f / circumference;

		/* forward */
		lMotor.setSpeed(FORWARD_SPEED);
		rMotor.setSpeed(FORWARD_SPEED);
		lMotor.rotate((int)(cm * toLinear), true);
		rMotor.rotate((int)(cm * toLinear), false);
	}
}
