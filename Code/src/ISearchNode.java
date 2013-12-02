/**Code blonges to Jonas David Nick, https://github.com/jonasnick.Interface for the goal node used to specify the goal for A*
*/
package AStar;
import java.util.*;

/**
 * Interface of a search node.
 @author Jonas David Nick
 */
public interface ISearchNode extends Comparable<ISearchNode> {
    // total estimated cost of the node
    public double f();
    //"tentative" g, cost from the start node 
    public double g();
    //set "tentative" g
    public void setG(double g);
    //heuristic cost to the goal node
    public double h();
    //costs to a successor
    public double c(ISearchNode successor);
    // a node possesses or computes his successors
    public ArrayList<ISearchNode> getSuccessors();
    // get parent of node in a path
    public ISearchNode getParent();
    //set parent
    public void setParent(ISearchNode parent);

    public boolean equals(Object other);

    public int hashCode();

    public int getX();
    public int getY();
}
