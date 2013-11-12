import lejos.nxt.SensorPort;
import lejos.util.Timer;

public class Swagbot extends Locobot {//Swagbot extends Localisingbot

   private UltrasonicListener uListener;
   private Timer uTimer;
   private float targetTheta = -1;
   private int smallestPing = 254;

   public Swagbot(final SensorPort sonicPort, final SensorPort colourPort, final SensorPort lightPort) {
      super(sonicPort,colourPort,lightPort);
      uListener = new UltrasonicListener(this.sonic);
      uTimer = new Timer(10/*round-up to int 9.375*/,uListener); //timeout value in ms
      uTimer.start();
   }


   public int getDistance() {
      return uListener.getDistance();
   }
   public int getFilteredDistance() {
      return uListener.getFilteredDistance();
   }

   public void scanLeft(float angle) {
      this.smallestPing = 254;
      this.targetTheta = 45;
      this.turn(100f,angle); //turn constantly (left when rate positive) to angle
      this.status = Status.SCANNING;
   }
   //overridden from Robot
   protected void scanning() {
      //while scanning get smallest ping value and corresponding theta
      int ping = uListener.getDistance();
      if(smallestPing > ping) {
         smallestPing = ping;
         targetTheta = this.getPosition().getTheta();
      }
   }

 
}   
