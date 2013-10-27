/* check the odometer */

import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

public class Driver {
	private static final int FORWARD_SPEED = 250;
	private static final int ROTATE_SPEED  = 150;
	private static final float RADIUS      = 2.72f;
	private static final float WHEELBASE   = 16.15f;

	static final NXTRegulatedMotor lMotor = Motor.A, rMotor = Motor.B;

	public static void main(String args[]) {
		Odometer odometer = new Odometer(lMotor, rMotor);

		/* fun */
		Button.setKeyClickLength(500);
		Button.setKeyClickTone(Button.ID_ENTER, 500000);
		Button.setKeyClickVolume(10);

		for( ; ; ) {
			System.err.println("moving");
			driveLeg(30.48f);
			System.err.println("Odo " + odometer);
			System.err.println("press enter ");
			int key = Button.waitForAnyPress();
			if((key & Button.ID_ESCAPE) != 0) break;
		}

		
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

		/* turn 90 */
		lMotor.setSpeed(ROTATE_SPEED);
		rMotor.setSpeed(ROTATE_SPEED);
		lMotor.rotate((int)-(Math.toRadians(90.0) * WHEELBASE * 0.5 * toLinear), true);
		rMotor.rotate((int) (Math.toRadians(90.0) * WHEELBASE * 0.5 * toLinear), false);
	}

}
