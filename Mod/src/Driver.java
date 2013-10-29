/* Java */

public class Driver {
	public static void main(String args[]) {
		float i, j;
		for(i = -720; i < 720; i += 10) {
			j = i % 360;
			if(j <= -180)    j += 360f;
			else if(j > 180) j -= 360f;
			System.out.print(i + "\t" + j + "\n");
		}
	}
}
