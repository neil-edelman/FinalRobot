/* this is a driver that instantaties a Robot and makes it do stuff */

class Driver {

	public static void main(String args[]) {
/*		Robot robot = new Robot();

		Thread rt = new Thread(robot, Robot.NAME);

		rt.start();

		robot.travelTo(10f, 10f);

		while(robot.getStatus() != Robot.Status.PLOTTING) {
			try {
				Thread.sleep(COMMAND_DELAY);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		robot.shutdown();
		try {
			rt.join();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}*/
		/*Controller c = new Controller(1f);*/

		Robot r = new Robot();
		r.turnTo(45);
	}

}
