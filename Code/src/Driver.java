import lejos.nxt.Button;

/* this is a driver that instantaties a Robot and makes it do stuff */

class Driver {

	private static final int DELAY = 100;

	public static void main(String args[]) {
		int key;
		float a = 0f, b;

		Robot r = new Robot();

		for( ; ; ) {
			a += 90f;
			b = a % 360;
			if(b <= -180)    b += 360f;
			else if(b > 180) b -= 360f;
			r.turnTo(b);

			System.err.println(r);
			while(r.getStatus() != Robot.Status.IDLE) {
				try {
					Thread.sleep(DELAY);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			System.err.println(r + " (press)");
			key = Button.waitForAnyPress();
			if((key & Button.ID_ESCAPE) != 0) break;
		}

		r.travelTo(20f, 20f);

		System.err.println(r);
		while(r.getStatus() != Robot.Status.IDLE) {
			try {
				Thread.sleep(DELAY);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		System.err.println(r);
		System.err.println(r + " (press)");
		key = Button.waitForAnyPress();
		if((key & Button.ID_ESCAPE) != 0) return;
	}

}
