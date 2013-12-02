/**This was added to the AStar package because lejos does not support PriorityQueue from the standard java libraries.It replicated the functionality needed
for AStar to function correctly.
@author Alex
*/

package AStar;

import java.util.Iterator;
import java.util.ArrayList;

public class PriorityQueue implements Iterable<ISearchNode> {

   private ArrayList<ISearchNode> queue;

   public PriorityQueue() {
      queue = new ArrayList<ISearchNode>();
   }

   public ISearchNode poll() {
      ISearchNode smallestNode = null;
      if(size() > 0) {
         int index = 0;
         int count = 0;
         for(ISearchNode node : queue) {
            if(count == 0 || node.compareTo(smallestNode) == -1) { //if node < smallestNode
               smallestNode = node;
               index = count;
            }
            count ++;
         }
         queue.remove(index);
      }
      return smallestNode;
   }
   public void add(ISearchNode n) {
      queue.add(n);
   }
   public int size() {
      return queue.size();
   }
   public String toString() {
      return queue.toString();
   }
   public void remove(ISearchNode n) {
      queue.remove(n);
   }
   public Iterator<ISearchNode> iterator() {
      return queue.iterator();
   }
}
