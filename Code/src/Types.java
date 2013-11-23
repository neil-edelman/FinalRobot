package AStar;

public enum Types { NONE(      (byte) 0 ), 
                    STYROFOAM( (byte) 1 ), 
                    OBSTACLE(  (byte) 2 ), 
                    OTHER(     (byte) 3 );
   private byte number;
   private Types(byte number) {
      this.number = number;
   }
   public byte getValue() {
      return number;
   }
}
