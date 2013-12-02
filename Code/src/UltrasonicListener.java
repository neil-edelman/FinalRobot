import lejos.nxt.UltrasonicSensor;
import lejos.util.TimerListener;
import java.util.Arrays;
import java.lang.Math;

/**
This class pings the ultrasonic sensor after 10ms and accumulates values. getDistance simply gets the last pinged distance.
The getFilteredDistance() method implements the median standard deviation method to return a median filtered distance.
The set of the data sets used can be adjusted.

--THE FOLLOWING MOVED TO NEWBOT(SWAGBOT V2)--
Additionally, the get smallestping method allows the listener to accumulate a data set of pings while a blocking navigation
or odometer method is running. Get smallest ping is used in this lab to scan from scan points and find the blocks using the
ultrasonic sensor. The method sets the smallestping variables to the closest distance recored and corresponding theta from
the odometer to targetTheta.
 @author Alex
*/
public class UltrasonicListener implements TimerListener {

   //   public static final int OUTLIER_THESHOLD = 40;
   public static final float b = 1.4826f; //normality data constant
   public UltrasonicSensor uSensor;
   public int[] dist = new int[5];
   public int count = 0;
   public int mad;

   public UltrasonicListener(UltrasonicSensor uSensor) {

      this.uSensor = uSensor;
      this.uSensor.setMode(UltrasonicSensor.MODE_PING);

   }

   public void timedOut() {

      uSensor.ping();
      count++;
      if (count == 5) {
         uSensor.getDistances(dist,0,5);
         count = 0;
         //recompute filtered distance using mad
         mad=mad(dist);
      }

   }
   /** Returns the latest raw distance value reported by the sensor.
   @author Alex
   */
   public int getDistance() {
      return uSensor.getDistance();
   }
   
   /** Returns the latest filtered distance value reported by the sensor.
   @author Alex
   */  
   public int getFilteredDistance() {
      //median filter
      return mad;
   }
   /** Generates a median filted value of the measured distances by the ultrasonic sensor.
   @author Alex
   */
   int mad(int[] dist) {

      Arrays.sort(dist);
      int medianIndex = dist.length/2; //array length assumed > 1
      int median = dist[medianIndex]; //eighth element is the median
      return median;
   }


}
