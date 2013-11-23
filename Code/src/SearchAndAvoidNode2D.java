package AStar;
import AStar.ASearchNode;
import AStar.ISearchNode;
import java.util.*;

/**This search nodes extends the 2D node to allow for avoidance of forbidden areas during searching 
 @author Alex */
public class SearchAndAvoidNode2D extends SearchNode2D {

   private TypeMap map;

   public SearchAndAvoidNode2D(TypeMap map, int x, int y, SearchNode2D parent, GoalNode2D goal) {
      super(x, y, parent, goal);
      this.map = map;
   }

   /**Overridden from SearchNode2D, adds functionality for diagonal movement and returns only values that are not forbidden as specified by the map.
    @author Extended by Alex
    @return Arraylist<ISearchNode>*/
   @Override
   public ArrayList<ISearchNode> getSuccessors() {
       ArrayList<ISearchNode> successors = new ArrayList<ISearchNode>();

       int[][] p = { {-1,-1},{0,-1},{1,-1},
                     {-1, 0},{1, 0},
                     {-1, 1},{0, 1},{1, 1} };

       for (int index = 0; index < p.length; index++) {
          int i = x + p[index][0];
          int j = y + p[index][1];
          if ( map.checkBounds(i,j) && (map.isEmpty(i,j) || map.isStyrofoam(i,j)) )
             successors.add(new SearchAndAvoidNode2D(this.map, i, j, this, this.goal));
       }
       return successors;
   }
 
}
