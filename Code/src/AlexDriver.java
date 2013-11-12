import lejos.nxt.Button;
import lejos.nxt.SensorPort;


/* this is a driver that instantaties a Robot and makes it do stuff */

class AlexDriver {

	private static final SensorPort  sonicPort = SensorPort.S4;
	private static final SensorPort colourPort = SensorPort.S3;
   private static final SensorPort lightPort = SensorPort.S1;

	public static void main(String args[]) {

		Swagbot robot = new Swagbot(sonicPort,colourPort,lightPort);
      Display display = new Display(robot);
      monitorForExit();
      robot.localise();
//		robot.travelTo(30.48f, 30.48f);
		while(robot.getStatus() != Robot.Status.IDLE) {
		}
      boolean twa = true;
      while(twa){}

	}

   private static void monitorForExit() {
      //spawn thread for exit
      Thread exitThread = new Thread() {
         public void run() {
            if (Button.waitForAnyPress() == Button.ID_ESCAPE)
   	         System.exit(0);
         } 
      };
      exitThread.start();
   }
}
