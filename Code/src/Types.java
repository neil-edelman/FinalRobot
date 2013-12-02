package AStar;

/**Types contains the types of objects that can be stored in the map.The only types used for the competition were NONE and OBSTACLE.
@author Alex
*/
public enum Types {

	NONE((byte) 0, '.' ),
	STYROFOAM( (byte) 1, 's' ),
	OBSTACLE(  (byte) 2, '*' ),
	OTHER(     (byte) 3, '?' );

	private byte number;
	private char symbol;

	private Types(byte number, char symbol) {
		this.number = number;
		this.symbol = symbol;
	}

	public byte getValue() {
		return number;
	}

	public char getSymbol() {
		return symbol;
	}

}
