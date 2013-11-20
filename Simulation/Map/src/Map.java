import java.lang.IllegalArgumentException;

public class Map {

	private static final int MAX_EXPLORE = -5;
	private static final int SCAN_RANGE = 7; /* dm, supposed to be 17, not really */

	enum Square { UNCHARTED, OPEN, OBJECT, WOOD, STYROFOAM; }

	public static void main(String args[]) {
		Map map = new Map(12, 12);
		/* assume the top square is open */
		map.fill(0, 0, 2, 2, Square.OPEN);
		map.localise(2, 2);
		System.err.println("Initial map:\n" + map);
		for(int scans = 0; ; ) {
			if(!map.move()) {
				map.choose();
				map.ping();
				scans++;
			}
			System.err.println("After " + scans + " pings:\n" + map + "\n");
			if(map.xSwag == map.xSize - 1) break;
		}
	}

	private byte map[][];
	private final int xSize, ySize;
	private       int xSwag, ySwag;
	private       int xExpl, yExpl;

	/* create a map
	 @param x
	 @param y the size
	 @throws IllegalArgumentException if x <= 0 || y <= 0 */
	public Map(final int x, final int y) {
		if(x <= 0 || y <= 0) throw new IllegalArgumentException();
		map   = new byte[y][x];
		xSize = x;
		ySize = y;
	}

	public boolean move() {
		int sq;
		if(xSwag == xSize - 1 || ySwag == ySize - 1) return false;
		sq = map[ySwag + 1][xSwag + 1];
		if(sq != 1) return false;
		sq = map[ySwag][xSwag + 1];
		if(sq != 1) return false;
		sq = map[ySwag + 1][xSwag];
		if(sq != 1) return false;
		System.out.println("Moving to (" + (xSwag + 1) + ", " + (ySwag + 1) + "):");
		xSwag++;
		ySwag++;
		return true;
	}

	/** fill the rectangle [x1, y1]--[x2, y2] with sq
	 @param x1
	 @param y1
	 @param x2
	 @param y2 the recangle (x1, y1)--(x2, y2)
	 @param sq what to set it to */
	public void fill(int x1, int y1, int x2, int y2, final Square sq) {
		if(x1 > x2 || y1 > y2) return;
		if(x1 >= xSize || y1 >= ySize || x2 < 0 || y2 < 0) return;
		if(x1 < 0)      x1 = 0;
		if(y1 < 0)      y1 = 0;
		if(x2 >= xSize) x2 = xSize - 1;
		if(y2 >= ySize) y2 = ySize - 1;
		for(int y = y1; y <= y2; y++) {
			for(int x = x1; x <= x2; x++) {
				map[y][x] = (byte)sq.ordinal();
			}
		}
	}

	/** set is inefficent in the extreme */
	public void set(final int x, final int y, final Square sq) {
		if(x < 0 || y < 0 || x >= xSize || y >= ySize) return;
		map[y][x] = (byte)sq.ordinal();
		/* fixme: laughably inefficient! */
		byte box;
		for(int j = 0; j < ySize; j++) {
			for(int i = 0; i < xSize; i++) {
				if((box = map[j][i]) > 0) continue;
				int d = MAX_EXPLORE + Math.round((float)Math.hypot(i - x, j - y));
				if(box > d) map[j][i] = (byte)d;
			}
		}
	}

	/** places the robot in the course
	 @param x
	 @param y the map coords
	 @throws IllegalArgumentException (x, y) is not on the map */
	public void localise(final int x, final int y) {
		if(x < 0 || x > xSize || y < 0 || y > ySize)
			throw new IllegalArgumentException("illeagal "+x+","+y);
		this.xSwag = x;
		this.ySwag = y;
	}

	/** Bresenham's (un-optimised) with constant lenght
	 @param x1
	 @param y1
	 @param x2
	 @param y2 the line (x1, y1)--(x2, y2)
	 @param sq how high to make it */
	public void ping() {
		int x1 = xSwag, y1 = ySwag, x2 = xExpl, y2 = yExpl;
		Square sq = Square.OPEN;
		// fixme: hmm, draw a thick line?
		final int dx = (x1 > x2) ? (x1 - x2) : (x2 - x1);
		final int sx = (x1 > x2) ? -1 : 1;
		final int dy = (y1 > y2) ? (y1 - y2) : (y2 - y1);
		final int sy = (y1 > y2) ? -1 : 1;
		int err = dx - dy;
		/* fixme: this is great for drawing graphics, but we need a better way */
		/* fixme: draw a rectangle fountain-fill */
		for(int length = 0; length < SCAN_RANGE; length++) {
			this.set(x1, y1, sq); /* fixme: aaaaauugh NO */
			int e2 = err << 1;
			if(e2 > -dx) { err -= dy; x1 += sx; }
			if(e2 <  dy) { err += dx; y1 += sy; }
			/* fixme: ridicolous */
			if(dx < 0 || dx > xSize) break;
			if(dy < 0 || dy > ySize) break;
		}
	}

	/** Bresenham's (un-optimised)
	 @param x1
	 @param y1
	 @param x2
	 @param y2 the line (x1, y1)--(x2, y2)
	 @param sq how high to make it */
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

	/** choose the smallest value+dist out of the unexplored regions and put it
	 in xExp, yExp */
	public void choose() {
		byte box;
		byte least = 99;
		xExpl = -1;
		yExpl = -1;
		System.out.print("Scanning an unexlored place t:\n");
		for(int j = 0; j < ySize; j++) {
			for(int i = 0; i < xSize; i++) {
				if((box = map[j][i]) > 0) {
					//System.out.print("  *");
					continue;
				}
				int d = box + Math.round((float)Math.hypot(i - xSwag, j - ySwag));
				//System.out.print(((d < 0 || d > 9) ? (" ") : ("  ")) + d);
				//System.out.print("[" + d + " < " + least + (d < least) + "]");
				if(d < least) {
					least = (byte)d;
					xExpl = i;
					yExpl = j;
				}
			}
			//System.out.print(" : "+least+" at (" + xExpl + ", " + yExpl + ")\n");
		}
	}

	/** this is very inefficient */
	public String toString() {
		String m = "";
		int x = 0, y = 0;
		for(byte yIso[] : map) {
			for(byte b : yIso) {
				if((x == xSwag) && (y == ySwag)) {
					m += " s ";
				} else if((x == xExpl) && (y == yExpl)) {
						m += " t ";
				} else {
					m += "" + ((b < 0 || b > 9) ? ("") : (" ")) + ((b > 0) ? "*" : b) + " ";
				}
				x++;
			}
			m += "\n";
			x = 0;
			y++;
		}
		//m += "the robot (" + xSwag + ", " + ySwag + ") the target (" + xExpl + ", " + yExpl + ")";
		return m;
	}
}
