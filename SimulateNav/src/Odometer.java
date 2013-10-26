
public class Odometer {

	Position position = new Position();
	Position    pCopy = new Position();

	/** constructor */
	public Odometer() {
	}

	public void timedOut() {
		float d = 0;
		float t = 0;

		float x = d * (float)Math.cos(t);
		float y = d * (float)Math.sin(t);

		position.x += x;
		position.y += y;
		position.r += t;
	}

	/** accessors */
	public Position getPositionCopy() {
		synchronized(this) {
			pCopy.copy(position);
		}
		return pCopy;
	}

	public String toString() {
		synchronized(this) {
			return "" + position;
		}
	}
}
