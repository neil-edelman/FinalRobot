/** @author Xavier */

import lejos.nxt.Sound;
import lejos.nxt.NXTRegulatedMotor;
import lejos.util.Delay;

class Stacker {

	/************* this is untested! if you get to testing it, @Alex *******/
	private static final float STACKING_SPEED = 100f;

	private static final int openAngle  = -15;
	private static final int closeAngle = 60;

	private int    collected;
	private final Colour colour;
	private final Swagbot swag;

	public Stacker(final Colour colour, final Swagbot swag) {
		this.colour = colour;
		this.swag   = swag;
		collected = 0;
	}
	
	public void resetMotor() {
		Hardware.stackerMotor.resetTachoCount();
	}

	public void openStacker() {
		resetMotor();
		Hardware.stackerMotor.rotateTo(openAngle);
	}

	public void closeStacker() {
		resetMotor();
		Hardware.stackerMotor.rotateTo(closeAngle);
	}

	/** okay, non-blocking; whatever
	 @author Neil */
	public boolean hasBlock() {
		return colour.getStyrofoamProbability() > 0.77f;
	}

	/** only called on hasBlock true (this needs to be re-written) */
	public boolean hasTwoBricks() {
		collected++;
		Sound.beep();
		swag.setSpeeds(STACKING_SPEED, STACKING_SPEED);
		/* forward ? cm */
		Delay.msDelay(3000); /* check this! */
		/* this may or may not be neccessary -Neil */
		swag.stop();
		/* return */
		if(collected==2){
			Sound.buzz();
			return true;
		}
		else{
			return false;
		}
	}
	
	public void greenZone() {
		Display.setText("Victory!");
		swag.stop();
		openStacker();
      System.out.print("hello");
		swag.setSpeeds(STACKING_SPEED, STACKING_SPEED); /* such a hack -Neil */
		//forward 6 cm (fixme: check)
		Delay.msDelay(1500);
		swag.stop();
		closeStacker();
      System.out.print("hello");
		//forward 11 cm (fixme: check)
		swag.setSpeeds(STACKING_SPEED, STACKING_SPEED); /* such a hack -Neil */
		Delay.msDelay(2000);
		swag.stop();
		openStacker();
      System.out.print("hello");
      Hardware.stackerMotor.stop();
		swag.stop();
		Delay.msDelay(1000);
		swag.setSpeeds(300, 300); /* go somewhere... out of green zone -Alex */
		Delay.msDelay(2000);
      swag.stop();
      System.exit(0);
	}
	
}
