import lejos.nxt.Button;

/* this is a driver that instantaties a Robot and makes it do stuff */

class Driver {

	private static final int DELAY = 100;

	public static void main(String args[]) {
		int key;

		Robot r = new Robot();

		r.turnTo(90f);

		System.err.println(r);
		while(r.getStatus() != Robot.Status.IDLE) {
			try {
				Thread.sleep(DELAY);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		System.out.println("press!");
		key = Button.waitForAnyPress();
		if((key & Button.ID_ESCAPE) != 0) return;

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
		System.out.println("press!");
		key = Button.waitForAnyPress();
		if((key & Button.ID_ESCAPE) != 0) return;
	}

}
