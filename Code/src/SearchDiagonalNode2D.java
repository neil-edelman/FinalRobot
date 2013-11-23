package AStar;
import AStar.ASearchNode;
import AStar.ISearchNode;
import java.util.*;

/**This search nodes extends the 2D node to allow for diagonal values
 @author Alex */
public class SearchDiagonalNode2D extends SearchNode2D {

   public SearchDiagonalNode2D(int x, int y, SearchNode2D parent, GoalNode2D goal) {
      super(x, y, parent, goal);
   }

   /**Overridden from SearchNode2D, adds functionality for diagonal movement.
    @author Extended by Alex
    @return Arraylist<ISearchNode>*/
   @Override
   public ArrayList<ISearchNode> getSuccessors() {
       ArrayList<ISearchNode> successors = new ArrayList<ISearchNode>();
       successors.add(new SearchDiagonalNode2D(this.x-1, this.y, this, this.goal));
       successors.add(new SearchDiagonalNode2D(this.x+1, this.y, this, this.goal));
       successors.add(new SearchDiagonalNode2D(this.x, this.y+1, this, this.goal));
       successors.add(new SearchDiagonalNode2D(this.x, this.y-1, this, this.goal));
       successors.add(new SearchDiagonalNode2D(this.x-1, this.y-1, this, this.goal));
       successors.add(new SearchDiagonalNode2D(this.x-1, this.y+1, this, this.goal));
       successors.add(new SearchDiagonalNode2D(this.x+1, this.y-1, this, this.goal));
       successors.add(new SearchDiagonalNode2D(this.x+1, this.y+1, this, this.goal));
       return successors; 
   }
 
}
