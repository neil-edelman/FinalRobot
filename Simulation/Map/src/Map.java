import java.lang.IllegalArgumentException;

public class Map {

	private static final int MAX_EXPLORE = 4;

	enum Square {
		//UNCHARTED((byte)0), OPEN((byte)1), OBJECT((byte)2), WOOD((byte)3), STYROFOAM((byte)4);
		//final byte value;
		//Square(byte value) { this.value = value; }
		UNCHARTED, OPEN, OBJECT, WOOD, STYROFOAM;
	}
	
	public static void main(String args[]) {
		Map map = new Map(12, 12);
		//map.fill(0, 9, 2, 11, Square.OPEN);
		//map.set(0, 0, Square.OPEN);
		//map.set(3, 3, Square.OPEN);
		map.line(0, 0, 11, 3, Square.OPEN);
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
		box[y][x] = (byte)sq.ordinal();
		explore[y][x] = MAX_EXPLORE;
		/* fixme: laughably inefficient! */
		for(int j = 0; j < ySize; j++) {
			for(int i = 0; i < xSize; i++) {
				int d = MAX_EXPLORE - Math.round((float)Math.hypot(i - x, j - y));
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

	/** Bresenham's (un-optimised) */
	public void line(int x1, int y1, int x2, int y2, final Square sq) {
		// fixme: some checks
		// fixme: hmm, draw a thick line?
		final int dx = (x1 > x2) ? (x1 - x2) : (x2 - x1);
		final int sx = (x1 > x2) ? -1 : 1;
		final int dy = (y1 > y2) ? (y1 - y2) : (y2 - y1);
		final int sy = (y1 > y2) ? -1 : 1;
		int err = dx - dy;
		/* fixme: this is great for drawing graphics, but we need a better way */
		/* fixme: draw a rectangle fountain-fill */
		for( ; ; ) {
			this.set(x1, y1, sq); /* fixme: aaaaauugh NO */
			if(x1 == x2 && y1 == y2) break;
			int e2 = err << 1;
			if(e2 > -dx) { err -= dy; x1 += sx; }
			if(e2 <  dy) { err += dx; y1 += sy; }
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
