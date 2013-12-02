/** Interface for the goal node used to specify the goal for A*
*/
package AStar;
import AStar.ISearchNode;

/**
 * GoalNodes don't need as much Information
 * as SearchNodes.
@author Jonas David Nick*/
public interface IGoalNode{
    public boolean inGoal(ISearchNode other);
} 
