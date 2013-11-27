/** to keep the different robots in one place
 @author Neil */

import lejos.nxt.SensorPort;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

class Hardware {

	/* basic settings for Sex Robot (default) */

	/* from Driver / Swagbot */
	public static SensorPort  sonicPort = SensorPort.S4;
	public static SensorPort colourPort = SensorPort.S3;
	public static SensorPort  lightPort = SensorPort.S1;
	public static int    bluetoothDelay = 0; /* 10000 is to short */
	public static boolean  useBluetooth = false;
	public static boolean     useServer = false;
	public static boolean       useLoco = false;

	/* from Locobot */
	/* SONAR_DELAY > (255cm) * 2 / (340m/s * 100cm/m) = 15ms
	 leJOS says 20ms */
	public static final int      sonarDelay = 20;
	public static final int defaultMaxPings = 128;

	public static int             locoDelay = 30; /* SONAR_DELAY + processing */
	public static float           locoSpeed = 150;

	/* from Odometer */
	public static int    odoDelay = 25;
	public static float    radius = /*2.72f*/2.707f;
	public static float wheelbase = 16.15f;

	/* from Ping */
	public static final float  cutoffAngle = (float)Math.toRadians(60.0);
	public static final float       detMin = 0.8f;
	public static final float   maxR2Error = 2.0f;

	public static float    lightBack = 12.2f;
	public static float sonicForward = 10.4f;
	public static float    clearance = 12.0f;

	/* from Robot / Swagbot */
	public static NXTRegulatedMotor  leftMotor = Motor.A;
	public static NXTRegulatedMotor rightMotor = Motor.B;
	public static NXTRegulatedMotor stackerMotor= Motor.C;
	public static String                  name = "Sex Robot";
	public static int                 navDelay = 100; /* ms */
	public static float         angleTolerance = (float)Math.toRadians(0.1); /* rad */
	public static float angleMarginalTolerance = 2.0f; /* rad/s */
	public static float      distanceTolerance = 1f; /* cm */
	public static float      defaultLimitAngle = 350f;
	public static float   defaultLimitDistance = 350f;
	/* public static float               maxSpeed = 50f; */

	/** hw Sex Robot + localisation */
	public static void locobot() {
		name    = "Locobot";
		useLoco = true;

		lightBack    = 12.2f;
		sonicForward = 10.4f;
		clearance    = 12f;
	}

	/* Swagbot (different hardware) */
	public static void swagbotV2() {
		name = "Swagbot v2";
		useLoco = true;

		clearance    = 16f; // about
		//wheelbase    = 19.7f; //swagbot (version 2) tested, works well
		wheelbase    = 19.63f; /* more accurate -Neil */
		//sonicForward = 8f; //swagbot (version 2) tested: y value is off (doesn't work)
		sonicForward = 9.0f; /* measured -Neil */
		
		angleTolerance = (float)Math.toRadians(0.5); /* rad */
		angleMarginalTolerance = 2.0f; /* rad/s */
		distanceTolerance = 3f; /* cm */
	}

}
