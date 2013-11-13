import lejos.nxt.Button;
import lejos.nxt.SensorPort;


/* this is a driver that instantaties a Robot and makes it do stuff */

class AlexDriver {

	private static final SensorPort  sonicPort = SensorPort.S4;
	private static final SensorPort colourPort = SensorPort.S3;
   private static final SensorPort lightPort = SensorPort.S1;
   private static Swagbot robot = new Swagbot(sonicPort,colourPort,lightPort);
   private static Display display = new Display(robot);

	public static void main(String args[]) {

      monitorForExit();

      runTests();

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



   public static void waitForIdle() {
         while(robot.getStatus() != Robot.Status.IDLE || robot.getFindStatus() != Robot.FindStatus.IDLE) {
		}
   }

   /** unit tests for robot code, when you complete a function write a test method
   say what it's supposed to do and run it here, then test to make sure everything
   functions properly */
   public static void runTests() {
      robot.localise(); 
//      robot.turnTo(90);
//      waitForIdle();
//      robot.turnTo(0);
//      waitForIdle();
//		robot.travelTo(30.48f, 30.48f);
//      waitForIdle();
//    robot.findBlocks();
//      waitForIdle();
//      robot.stop();
      boolean forever = true;
      while(forever){}
      return;
   }
}
