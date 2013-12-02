/** Code blonges to Jonas David Nick, https://github.com/jonasnick
*/
package AStar;
import AStar.ISearchNode;

/**
 * Implements trivial functions for a search node.
@author Jonas David Nick*/
public abstract class ASearchNode implements ISearchNode {
    private Double g = 0.0;
    // total estimated cost of the node
    public double f() {
        return this.g() + this.h();
    }
    //"tentative" g, cost from the start node 
    public double g() {
        return this.g;
    }
    //set "tentative" g
    public void setG(double g) {
        this.g = g;
    } 
    
}

