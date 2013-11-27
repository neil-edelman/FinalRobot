package AStar;

import java.lang.IllegalArgumentException;
//import AStar;

public class FieldMap implements TypeMap {

	static final float CM_PER_MAP = 30.48f / 3f;
	/* 170 cm (that's what it says in the docs; don't have time to test) */
	static final float SCAN_RANGE = 17f;

   public byte[][] map;
   private int xSize;
   private int ySize;

   public FieldMap(int xRange, int yRange) {
      map = new byte[xRange][yRange];
      xSize = xRange;
      ySize = yRange;
   }

   public Types getType(int x, int y) {
      return Types.values()[map[x][y]];
   }

   public boolean isStyrofoam(int x, int y) {
      return getType(x,y) == Types.STYROFOAM;
   }

   public boolean isObstacle(int x, int y) {
      return getType(x,y) == Types.OBSTACLE;
   }

   public boolean isEmpty(int x, int y) {
      return getType(x,y) == Types.NONE;
   }

   public void set(int x, int y, Types type) {
      map[x][y] = type.getValue();
   }

   public boolean checkBounds(int x, int y) {
      return !( x > map.length || x < 0 || y > map[0].length || y < 0 );
   }

	/** fill the rectangle [x1, y1]--[x2, y2] with sq
	 @param x1
	 @param y1
	 @param x2
	 @param y2 the recangle (x1, y1)--(x2, y2)
	 @param sq what to set it to */
	public void fill(int x1, int y1, int x2, int y2, Types type) {
		if(x1 > x2 || y1 > y2 || x1 >= xSize || y1 >= ySize || x2 < 0 || y2 < 0) {
         //throw new IllegalArgumentException("Illegal Fill");
      }
		if(x1 < 0)      x1 = 0;
		if(y1 < 0)      y1 = 0;
		if(x2 >= xSize) x2 = xSize - 1;
		if(y2 >= ySize) y2 = ySize - 1;
		for(int y = y1; y <= y2; y++) {
			for(int x = x1; x <= x2; x++) {
				map[y][x] = type.getValue();
			}
		}
	}

	/** this is very inefficient (because of String)
	 @author Neil */
	public String toString() {
		String m = "";
		int x = 0, y = 0;
		for(byte yIso[] : map) {
			for(byte b : yIso) {
				m += " " + Types.values()[b].getSymbol();
				x++;
			}
			m += "\n";
			x = 0;
			y++;
		}
		return m;
	}

	/** Bresenham's (un-optimised) with constant lenght
	 @param x1
	 @param y1
	 @param x2
	 @param y2 the line (x1, y1)--(x2, y2)
	 @param sq how high to make it */
/*	public void ping(Ping ping) {
		int x1 = Math.round(ping.position.x / CM_PER_MAP);
		int y1 = Math.round(ping.position.x / CM_PER_MAP);
		int x2 = ping.x, y2 = ping.y;
		Types sq = Types.NONE;
		// fixme: hmm, draw a thick line?
		final int dx = (x1 > x2) ? (x1 - x2) : (x2 - x1);
		final int sx = (x1 > x2) ? -1 : 1;
		final int dy = (y1 > y2) ? (y1 - y2) : (y2 - y1);
		final int sy = (y1 > y2) ? -1 : 1;
		int err = dx - dy;
		// fixme: draw a rectangle fountain-fill *
		for(int length = 0; length < SCAN_RANGE; length++) {
			if(x1 == x2 && y1 == y2) {
				this.set(x1, y1, Types.OBSTACLE);
				break;
			}
			this.set(x1, y1, sq); // fixme: aaaaauugh NO *
			int e2 = err << 1;
			if(e2 > -dx) { err -= dy; x1 += sx; }
			if(e2 <  dy) { err += dx; y1 += sy; }
		}
	}
*/

}
