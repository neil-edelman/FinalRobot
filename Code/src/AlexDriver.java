/** this is a driver that instantaties a Robot and makes it do stuff
 @author Alex */

import lejos.nxt.Button;
import lejos.nxt.SensorPort;
import lejos.nxt.LCD;
import lejos.nxt.comm.RConsole;

import AStar.Types;
import AStar.AStar;
import AStar.TypeMap;
import AStar.FieldMap;
import AStar.GoalNode2D;
import AStar.ISearchNode;
import AStar.SearchAndAvoidNode2D;
import java.util.ArrayList;
import java.util.*;

import bluetooth.*;

class AlexDriver {

   private static Swagbot robot;
   private static Display display;

	/** the entry-point where we create our robot
	 @param args[] ignored
	 @author Alex, Neil */
	public static void main(String args[]) {

		/* the hardware profile for the robot */
		Hardware.swagbotV2();
		Hardware.useBluetooth = false;
		Hardware.useServer    = false; /* change to true in our final robot */
		Hardware.useLoco      = true;

		float destination_x = 50f;
		float destination_y = 50f;

		if(Hardware.useServer) {
			BluetoothConnection conn = new BluetoothConnection();
			// as of this point the bluetooth connection is closed again, and you can pair to another NXT (or PC) if you wish
			
			// example usage of Tranmission class
			Transmission t = conn.getTransmission();
			if (t == null) {
				LCD.drawString("Failed to read transmission", 0, 5);
				destination_x = 0;
				destination_y = 0;
			} else {
				StartCorner corner = t.startingCorner;
				PlayerRole role = t.role;
				// green zone is defined by these (bottom-left and top-right) corners:
				int[] greenZone = t.greenZone;
				// red zone is defined by these (bottom-left and top-right) corners:
				int[] redZone = t.redZone;
				
				//convert to cm, unrotated
				float x1 = 30.48f * (float)greenZone[0] + 30.48f;
				float y1 = 30.48f * (float)greenZone[1] + 30.48f;
				float x2 = 30.48f * (float)greenZone[2] + 30.48f;
				float y2 = 30.48f * (float)greenZone[3] + 30.48f;
				
				//[cos -sin]-1
				//[sin  cos]
				float a = 1f,b = 0f,c = 0f,d = 1f;
				//rotate coordinates to robot's localise position
				// what is StateCorner? FIXME!
				/*switch(corner) {
				 case StateCorner.BOTTOM_LEFT: //0 degrees
				 break;
				 case StateCorner.BOTTOM_RIGHT: //-90 degrees
				 a = 0;
				 b = -1;
				 c = 1;
				 d = 0;
				 break;
				 case StateCorner.TOP_LEFT: //90 degrees
				 a = 0;
				 b = 1;
				 c = -1;
				 d = 0;
				 break;
				 case StateCorner.TOP_RIGHT: // 180 degrees
				 a = -1f;
				 d = -1f;
				 break;
				 }*/
				
				float greenZone_x1 = a*x1 + b*y1;
				float greenZone_y1 = c*x1 + d*y1;
				float greenZone_x2 = a*x2 + b*y2;
				float greenZone_y2 = c*x2 + d*y2;
				
				if (corner == StartCorner.BOTTOM_LEFT) {
				}
				else if (corner == StartCorner.BOTTOM_RIGHT) {
					float hold = destination_y;
					destination_y = 8 - destination_x;
					destination_x = hold;
				}
				else if (corner == StartCorner.TOP_LEFT) {
					float hold = destination_x;
					destination_x = 8 - destination_y;
					destination_y = hold;
				}
				else if (corner == StartCorner.TOP_RIGHT) {
					destination_x = 8 - destination_x;
					destination_y = 8 - destination_y;
				}
				
				// print out the transmission information to the LCD
				conn.printTransmission();
			}
		}

		if(Hardware.useBluetooth) {
			Display.setText("Bluetooth...");
			RConsole.openBluetooth(Hardware.bluetoothDelay);
			if(!RConsole.isOpen()) Display.setText("Never mind.");
		}

		robot   = new Swagbot(destination_x,destination_y);
		display = new Display(robot);
		monitorForExit();

		if(Hardware.useLoco) {
			robot.localise();
		} else {
			robot.setPosition(new Position(30.48f,30.48f,0f));
		}
		waitForIdle();

		/* Neil: loco, travel to the 2nd square, and turn to 90 */
		robot.travelTo(60.96f, 60.96f);
		waitForIdle();
		robot.turnTo(90f);
		waitForIdle();

		//robot.travelTo(destination_x, destination_y);
		//runTests();
		//runAbridgedTests();
		//robot.scanLeft(90f);
//      travelWithAStar(180,180);

//		robot.findBlocks();
//		waitForIdle();

		// stall until user decides to end program
		//Button.waitForAnyPress();

		/* colour test */
//		int press;
//		Colour colour = new Colour(colourPort);
//		for( ; ; ) {
//			System.out.print("press: ");
//            press = Button.waitForAnyPress();
//			if((press & Button.ID_ESCAPE) != 0) break;
//			System.out.println((int)(colour.getStyrofoamProbability() * 100f) + "% styrofoam");
//         System.out.println(colour.getColourValue() == Colour.Value.STYROFOAM);
//		}
		
		/* close bt connection */
		if(RConsole.isOpen()) RConsole.close();
	}

	/** Waits for the robot subtask to finish and return to finding or become idle.
	 @author Alex */
   public static void waitForSubTask() {
         while(robot.getStatus() != Robot.Status.IDLE || robot.getStatus() != Robot.Status.FINDING) {
		}
   }
   /** Robot will travel to coordinates using AStar.
    @author Alex
    @return void
   */
   public static void travelWithAStar(int x, int y) {
      followPath(robot.straightenPath(robot.getPathTo(x/10,y/10)));
   }
   /** Robot will follow the input path.
    @author Alex
    @return void
   */
   public static void followPath(ArrayList<ISearchNode> path) {
      path.remove(0);
//      int count = 1;
      for(ISearchNode node : path) {
         robot.travelTo(node.getX()*10,node.getY()*10,350,350);
         waitForIdle();
//           System.out.println("T"+count+": ("+node.getX()*10+","+node.getY()*10+")");
//           count++;
      }
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


	/** waits for the robot to be idle (eg completed a turn, or travelled to a
	 destination
	 @author Alex */
   public static void waitForIdle() {
         while(robot.getStatus() != Robot.Status.IDLE || robot.getFindStatus() != Robot.FindStatus.IDLE) {
		}
   }

   /** unit tests for robot code, when you complete a function write a test method
   say what it's supposed to do and run it here, then test to make sure everything
   functions properly.
   localise: rotates but values are off
      test: rotates counterclockwise and updates odometer (update not implemented yet)
   turnTo/travelTo: works
      test:
         turn left, turn right
         travel to the first cross of vertical and horizontal lines
         face 90 (straight)
   findBlocks: not working -- in progress
	@author Alex
   */
   public static void runTests() {
      robot.localise(); 
      waitForIdle();
      robot.turnTo(0f,250);
      waitForIdle();
      robot.turnTo(90f,250);
      waitForIdle();
      robot.turnTo(0f,250);
      waitForIdle();
      robot.travelTo(60.96f,60.96f,250,250);
      waitForIdle();
      robot.turnTo(90f,250);
      waitForIdle();
		robot.travelTo(30.48f, 30.48f,250,250);
//    robot.findBlocks();
//      waitForIdle();
      robot.stop();
      return;
   }
   /** quick test -- turns left 30 degrees and back, spin/travels to -5,-5 and returns
	@author Alex */
   public static void runAbridgedTests() {
      robot.turnTo(30f,250);
      waitForIdle();
      robot.turnTo(0f,250);
      waitForIdle();
		robot.travelTo(-5f, -5f,250,250);
      waitForIdle();
      robot.travelTo(0f,0f,250,250);
      waitForIdle();
      robot.stop();
      return;
   }

}
