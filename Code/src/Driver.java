import lejos.nxt.Button;

/* this is a driver that instantaties a Robot and makes it do stuff */

class Driver {

	private static final int DELAY = 100;

	public static void main(String args[]) {
		int key;
		float a = 0f, b;

		Swagbot robot = new Swagbot();
		Display display = new Display(robot); //instantiated and started

		for( ; ; ) {
			a += 90f;
			b = a % 360;
			if(b <= -180)    b += 360f;
			else if(b > 180) b -= 360f;
			robot.turnTo(b);

			while(robot.getStatus() != Robot.Status.IDLE) {
				try {
					Thread.sleep(DELAY);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			key = Button.waitForAnyPress();
			if((key & Button.ID_ESCAPE) != 0) break;
		}

		robot.travelTo(30.48f, 30.48f);

		System.err.println(robot);
		while(robot.getStatus() != Robot.Status.IDLE) {
			try {
				Thread.sleep(DELAY);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		key = Button.waitForAnyPress();
		if((key & Button.ID_ESCAPE) != 0) return;
	}

}
