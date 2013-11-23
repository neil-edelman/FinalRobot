package AStar;

public class FieldMap implements TypeMap {

   public byte[][] map;

   public FieldMap(int xRange, int yRange) {
      map = new byte[xRange][yRange];
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
}
