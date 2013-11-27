package AStar;

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
