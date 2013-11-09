import java.lang.IllegalArgumentException;

public class Map {

	enum Square {
		//UNCHARTED((byte)0), OPEN((byte)1), OBJECT((byte)2), WOOD((byte)3), STYROFOAM((byte)4);
		//final byte value;
		//Square(byte value) { this.value = value; }
		UNCHARTED, OPEN, OBJECT, WOOD, STYROFOAM;
	}
	
	public static void main(String args[]) {
		Map map = new Map(12, 12);
		//map.fill(0, 9, 2, 11, Square.OPEN);
		map.set(0, 0, Square.OPEN);
		System.err.println(map);
	}

	private byte box[][];
	private byte explore[][];
	private final int xSize, ySize;

	public Map(final int x, final int y) {
		if(x <= 0 || y <= 0) throw new IllegalArgumentException();
		box     = new byte[y][x];
		explore = new byte[y][x];
		xSize = x;
		ySize = y;
	}

	public void set(final int x, final int y, final Square sq) {
		if(x < 0 || y < 0 || x >= xSize || y >= ySize) return;
		box[y][x] = (byte)sq.ordinal();//sq.value;
		explore[y][x] = 4;
		//for(byte yIso[] : explore) {
			//for(byte b : yIso) { this is why you need pointers
		for(int j = 0; j < ySize; j++) {
			for(int i = 0; i < xSize; i++) {
				int d = 4 - Math.round((float)Math.hypot(i - x, j - y));
				if(explore[j][i] < d) explore[j][i] = (byte)d;
			}
		}
	}

	public void fill(int x1, int y1, int x2, int y2, final Square sq) {
		if(x1 > x2 || y1 > y2) return;
		if(x1 >= xSize || y1 >= ySize || x2 < 0 || y2 < 0) return;
		if(x1 < 0)      x1 = 0;
		if(y1 < 0)      y1 = 0;
		if(x2 >= xSize) x2 = xSize - 1;
		if(y2 >= ySize) y2 = ySize - 1;
		for(int y = y1; y <= y2; y++) {
			for(int x = x1; x <= x2; x++) {
				box[y][x] = (byte)sq.ordinal();
			}
		}
	}

	/** this is very inefficient */
	public String toString() {
		String m = "";
		for(byte yIso[] : box) {
			for(byte b : yIso) {
				m += "(" + b + ")";
			}
			m += "\n";
		}
		boolean isFirst = true;
		for(byte yIso[] : explore) {
			/* print */
			if(isFirst) {
				isFirst = false;
			} else {
				m += "\n";
			}
			/* data */
			for(byte b : yIso) {
				m += "[" + b + "]";
			}
		}
		return m;
	}
}
