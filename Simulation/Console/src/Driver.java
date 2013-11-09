import lejos.nxt.comm.RConsole;

/* this is a driver that instantaties a Robot and makes it do stuff */

class Driver {

	public static void main(String args[]) {
		RConsole.open();

		System.err.println("entered");
		RConsole.println("Hello");
		RConsole.close();
	}

	public Driver() {
	}

}