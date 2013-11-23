/** Code blonges to Jonas David Nick, https://github.com/jonasnick, changed to abstract class by me */
package AStar;
import AStar.ASearchNode;
import AStar.ISearchNode;
import java.util.*;

public abstract class SearchNode2D extends ASearchNode {
    protected int x;
    protected int y;
    protected SearchNode2D parent;
    protected GoalNode2D goal;

    public SearchNode2D(int x, int y, SearchNode2D parent, GoalNode2D goal){
        this.x = x;
        this.y = y;
        this.parent = parent;
        this.goal = goal;

    }    
    public SearchNode2D getParent(){
        return this.parent;
    }
    public abstract ArrayList<ISearchNode> getSuccessors();

    public double h() {
        return this.dist(goal.getX(), goal.getY());
    }
    public double c(ISearchNode successor) {
        SearchNode2D successorNode = this.castToSearchNode2D(successor);
        return 1;
    }
    public void setParent(ISearchNode parent) {
        this.parent = this.castToSearchNode2D(parent);
    }
    public boolean equals(Object other) {
        if(other instanceof SearchNode2D) {
            SearchNode2D otherNode = (SearchNode2D) other;
            return (this.x == otherNode.getX()) && (this.y == otherNode.getY());
        }
        return false;
    }
    // compare the f values
    public int compareTo(ISearchNode other) {
        SearchNode2D otherNode = this.castToSearchNode2D(other);
        if(this.f() < otherNode.f()) {
            return -1;
        } else if(this.f() == otherNode.f()) {
            return 0;
        } else {
            return 1;
        }
    }
    public int hashCode() {
        return (41 * (41 + this.x + this.y));
    }
    public double dist(int otherX, int otherY) {
        return Math.sqrt(Math.pow(this.x-otherX,2) + Math.pow(this.y-otherY,2));
    }
    public int getX() {
        return this.x;
    }
    public int getY() {
        return this.y;
    }
    public String toString(){
        return "(" + Integer.toString(this.x) + ";" + Integer.toString(this.y) 
                + ";h:"+ Double.toString(this.h()) 
                + ";g:" +  Double.toString(this.g()) + ")";
    }

    private SearchNode2D castToSearchNode2D(ISearchNode other) {
        return (SearchNode2D) other;
    }
}
