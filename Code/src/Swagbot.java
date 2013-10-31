import lejos.nxt.UltrasonicSensor;
import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;

public class Swagbot extends Robot {

	Colour       colour = new Colour();

	public Swagbot() {
		super();
	}

	/** this is used to rotate the robot at a constant speed */
	public void turnConstantlyTo(final float angleTo,final int speed) {
		status = Status.LOCALISING;
		this.setSpeeds(-speed,speed);
	}

	/** override the method */
	protected void localising() {
		System.err.println("Loco!");
		status = Status.IDLE;
	}
//
//      status = Status.LOCALISING;
//      
//      USLocalizer usl = new USLocalizer(this, new UltrasonicSensor(SensorPort.S4), USLocalizer.LocalizationType.FALLING_EDGE);
//      usl.doLocalization();
//      if(doLight) {
//         robot.travelTo(-3f,-3f);
//         robot.turnTo(45f);
//         LightLocalizer lsl = new LightLocalizer(this, new LightSensor(SensorPort.S1));
//         lsl.doLocalization();
//      }
//
//		status = Status.IDLE;
//	}

	public Colour.Value getColour() {
		return colour.getColourValue();
	}

}
