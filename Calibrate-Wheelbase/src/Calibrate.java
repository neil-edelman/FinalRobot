/* calibrate the wheelbase;
 this should be after calibration of radius (you know this, so enter it in) */

import lejos.nxt.Button;
import lejos.nxt.NXTRegulatedMotor;

class Lab5 {
	private static final int NO_REPEATS    = 10;
	private static final int FORWARD_SPEED = 250;
	private static final int ROTATE_SPEED  = 150;
	/* you know this from experimenting, enter it in */
	private static final float RADIUS      = 2.665f;
	/* this is the variable that you are varying */
	private static final float WHEELBASE   = 15.8f;

	static final NXTRegulatedMotor lMotor = Motor.A, rMotor = Motor.B;

	public static void main(String args[]) {

		/* fun */
		Button.setKeyClickLength(500);
		Button.setKeyClickTone(Button.ID_ENTER, 500000);
		Button.setKeyClickVolume(10);

		for(int i = 0; i < NO_REPEATS; i++) {
			driveLeg(0.48f);
			driveLeg(0.48f);
			driveLeg(0.48f);
			driveLeg(0.48f);
		}

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
		
		/* turn 90 */
		lMotor.setSpeed(ROTATE_SPEED);
		rMotor.setSpeed(ROTATE_SPEED);
		lMotor.rotate((int)-(Math.toRadians(90.0) * WHEELBASE * 0.5 * toLinear), true);
		rMotor.rotate((int) (Math.toRadians(90.0) * WHEELBASE * 0.5 * toLinear), false);
		/* (wheelbase / 2) 360   ~50.0 */
		/* leftMotor.rotate((int)-((180.0 * Math.PI * Odometer.WIDTH * 90.0 / 360.0) / (Math.PI * Odometer.RADIUS)), true); */
		/* lMotor.rotate((int)-(90.0 * (Math.PI * WIDTH) / circumference), true); */
		/*
		90 * (PI / 180) * (WIDTH/2) / (2 PI R);
		compare
		((180.0 * PI * WIDTH * 90.0 / 360.0) / (PI * RADIUS))
		PI * WIDTH * 90 / (2 PI R)
		90 * WIDTH * PI / (2 PI R)
		90 * (PI / 0.5) * (WIDTH/2) / (2 PI R) */
	}
}
