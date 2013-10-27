import lejos.nxt.Button;

/* this is a driver that instantaties a Robot and makes it do stuff */

class Driver {

	private static final int DELAY = 100;

	public static void main(String args[]) {

		Robot r = new Robot();

		r.turnTo(45f);

		System.err.println(r);
		while(r.getStatus() != Robot.Status.IDLE) {
			try {
				Thread.sleep(DELAY);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
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
		System.out.println("press enter");
		while((Button.waitForAnyPress() & Button.ID_ENTER) == 0);
	}

}
