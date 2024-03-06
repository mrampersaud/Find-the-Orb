package submit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import game.FindState;
import game.Finder;
import game.Node;
import game.NodeStatus;
import game.ScramState;

/** Student solution for two methods. */
public class Pollack extends Finder {
    /** Get to the orb in as few steps as possible. <br>
     * Once you get there, you must return from the function in order to pick it up. <br>
     * If you continue to move after finding the orb rather than returning, it will not count.<br>
     * If you return from this function while not standing on top of the orb, it will count as <br>
     * a failure.
     *
     * There is no limit to how many steps you can take, but you will receive<br>
     * a score bonus multiplier for finding the orb in fewer steps.
     *
     * At every step, you know only your current tile's ID and the ID of all<br>
     * open neighbor tiles, as well as the distance to the orb at each of <br>
     * these tiles (ignoring walls and obstacles).
     *
     * In order to get information about the current state, use functions<br>
     * state.currentLoc(), state.neighbors(), and state.distanceToOrb() in FindState.<br>
     * You know you are standing on the orb when distanceToOrb() is 0.
     *
     * Use function state.moveTo(long id) in FindState to move to a neighboring<br>
     * tile by its ID. Doing this will change state to reflect your new position.
     *
     * A suggested first implementation that will always find the orb, but <br>
     * likely won't receive a large bonus multiplier, is a depth-first walk. <br>
     * Some modification is necessary to make the search better, in general. */
    @Override
    public void findOrb(FindState state) {
        // TODO 1: Get the orb
        List<Long> visited= new ArrayList<>();
        dfsWalk(state, visited);
    }

    /** The walker is standing on a Node u (say) given by State state. Visit every node reachable
     * along paths of unvisited nodes from node u. End with walker standing on Node u. Precondition:
     * u is unvisited. */
    public static void dfsWalk(FindState state, List<Long> visited) {
        Long u= state.currentLoc();
        int distance= state.distanceToOrb();
        if (distance == 0) { return; }
        visit(u, visited);
        List<NodeStatus> nlist= new ArrayList<>(state.neighbors());
        Collections.sort(nlist);
        // System.out.println("sorted neighbors: " + nlist);
        // System.out.println("neighbors: " + neighbors);
        // Long closeID= closestNode(neighbors);
        for (NodeStatus w : nlist) {
            Long closeID= w.getId();
            // System.out.println("closeID: " + closeID);
            if (!visited.contains(closeID)) {
                state.moveTo(closeID);
                dfsWalk(state, visited);
                if (state.distanceToOrb() == 0) { return; }
                state.moveTo(u);
            }
        }
    }

    // WORKING SOLUTION
//        for (NodeStatus w : neighbors) {
//            Long wID= w.getId();
//            if (!visited.contains(wID)) {
//                state.moveTo(wID);
//                dfsWalk(state, visited);
//                if (state.distanceToOrb() == 0) { return; }
//                state.moveTo(u);
//            }
//        }
//    }

    public static void visit(Long current, List<Long> visited) {
        visited.add(current);
    }

    /** Pres Pollack is standing at a node given by parameter state.<br>
     *
     * Get out of the cavern before the ceiling collapses, trying to collect as <br>
     * much gold as possible along the way. Your solution must ALWAYS get out <br>
     * before time runs out, and this should be prioritized above collecting gold.
     *
     * You now have access to the entire underlying graph, which can be accessed <br>
     * through parameter state. <br>
     * state.currentNode() and state.getExit() will return Node objects of interest, and <br>
     * state.allNodes() will return a collection of all nodes on the graph.
     *
     * The cavern will collapse in the number of steps given by <br>
     * state.stepsLeft(), and for each step this number is decremented by the <br>
     * weight of the edge taken. <br>
     * Use state.stepsLeft() to get the time still remaining, <br>
     * Use state.moveTo() to move to a destination node adjacent to your current node.<br>
     * Do not call state.grabGold(). Gold on a node is automatically picked up <br>
     * when the node is reached.<br>
     *
     * The method must return from this function while standing at the exit. <br>
     * Failing to do so before time runs out or returning from the wrong <br>
     * location will be considered a failed run.
     *
     * You will always have enough time to scram using the shortest path from the <br>
     * starting position to the exit, although this will not collect much gold. <br>
     * For this reason, using the shortest path method to calculate the shortest <br>
     * path to the exit is a good starting solution */
    @Override
    public void scram(ScramState state) {
        // TODO 2: scram
        // List<Node> shortest= Path.shortest(state.currentNode(), state.getExit());
        List<Long> visited= new ArrayList<>();
        findGold(state, visited);
    }

    /** This method calls on sort to order all nodes in the map based on the amount of gold on each
     * tile. Then, it generates the shortest path from the current node to each node within the
     * sorted list and traverses that path with a call on shortestWalk only if the target node has
     * not already been visited on a previous walk.
     *
     * Returns if the weight of the SP from the current node to target node + the weight of the SP
     * from target node to exit is > number of steps left */
    public static void findGold(ScramState state, List<Long> visited) {
        // get all nodes, iterate and find which have most gold, sort??
        // shortest path to each max gold having tile, then go to exit at certain point
        // (while loop?) when steps left = distance to exit, start going to exit

        List<Node> alist= new ArrayList<>(state.allNodes());
        Collections.sort(alist, (node1,
            node2) -> node2.getTile().gold() / Path.shortest(node2, state.currentNode()).size() -
                node1.getTile().gold() / Path.shortest(node1, state.currentNode()).size());
        for (Node target : alist) {
            List<Node> currentToTarget= Path.shortest(state.currentNode(), target);
            List<Node> currentToExit= Path.shortest(state.currentNode(), state.getExit());
            List<Node> targetToExit= Path.shortest(target, state.getExit());

            // dont go to a node if placed out of range of exit
            if (Path.pathSum(currentToTarget) + Path.pathSum(targetToExit) > state.stepsLeft()) {
                shortestWalk(state, currentToExit, visited);
                return;
            }
            if (!visited.contains(target.getId())) {
                shortestWalk(state, currentToTarget, visited);
            }
        }
    }

    // num of steps required to go to x, if x to exit > to exit, just go to exit

    /** This method iterates over the nodes of a list and moves along the corresponding path. Takes
     * in a ScramState state and a List of Nodes corresponding to the path desired */
    public static void shortestWalk(ScramState state, List<Node> path, List<Long> visited) {
        for (int i= 1; i < path.size(); i++ ) {
            visit(path.get(i).getId(), visited);
            state.moveTo(path.get(i));
        }
    }
}
