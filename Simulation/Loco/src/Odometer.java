/* odometer stub */

public class Odometer {

	Position p = new Position();

	/** constructor */
	public Odometer() {
	}

	public void shutdown() {
	}

	/** this gets a position copy so we can save the position for real-time
	 updates (position copy is assumed to be accessed one time) */
	public Position getPositionCopy() {
		return p;
	}

	/** for info methods */
	public Position getLastPosition() {
		return p;
	}

	public String toString() {
		synchronized(this) {
			return "Odo";
		}
	}

	/** setters */
	public void setDegrees(final float deg) {
	}
	public void setRadians(final float t) {
		p.setRadians(t);
	}
	public void setXY(final float x, final float y) {
		p.setXY(x, y);
	}

}
