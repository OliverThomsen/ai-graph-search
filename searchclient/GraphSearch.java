package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class GraphSearch {

    public static AgentState search(AgentState initialState, Frontier frontier)
    {
        int iterations = 0;

        // initialize the frontier using the initial state of problem
        frontier.add(initialState);
        // initialize the explored set to be empty
        HashSet<AgentState> explored = new HashSet<>();

        while (true) {
            // if the frontier is empty then return failure
            if (frontier.isEmpty()) {
                return null;
            }

            // choose a leaf node and remove it from the frontier
            AgentState currentState = frontier.pop();


            //Print a status message every 10000 iteration
            if (++iterations % 10000 == 0) {
                printSearchStatus(explored, frontier);
                System.err.println(currentState.toString());
            }

            // if the node contains a goal state then return the corresponding solution
            if (currentState.isGoalState()) {
                printSearchStatus(explored,frontier);
                return currentState;
            }

            // add the node to the explored set
            explored.add(currentState);

            // expand the chosen node, adding the resulting nodes to the frontier
            for (AgentState state : currentState.getExpandedStates()) {
                // only if not in the frontier or explored set
                if (!frontier.contains(state) && !explored.contains(state)) {
                    frontier.add(state);
                }
            }
        }
    }

    private static long startTime = System.nanoTime();

    private static void printSearchStatus(HashSet<AgentState> explored, Frontier frontier)
    {
        String statusTemplate = "#Expanded: %,8d, #Frontier: %,8d, #Generated: %,8d, Time: %3.3f s\n%s\n";
        double elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000d;
        System.err.format(statusTemplate, explored.size(), frontier.size(), explored.size() + frontier.size(),
                          elapsedTime, Memory.stringRep());
    }
}
