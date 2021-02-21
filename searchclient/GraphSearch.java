package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class GraphSearch {

    public static Action[][] search(State initialState, Frontier frontier)
    {
        boolean outputFixedSolution = false;

        if (outputFixedSolution) {
            //Part 1:
            //The agents will perform the sequence of actions returned by this method.
            //Try to solve a few levels by hand, enter the found solutions below, and run them:

            // Solves MAPF00.lvl
            return new Action[][] {
                {Action.MoveS},
                {Action.MoveE},
                {Action.MoveE},
                {Action.MoveS},
                {Action.MoveE},
                {Action.MoveE},
                {Action.MoveE},
                {Action.MoveE},
                {Action.MoveE},
                {Action.MoveE},
                {Action.MoveE},
                {Action.MoveE},
                {Action.MoveS},
                {Action.MoveS},
            };
        } else {
            //Part 2:
            //Now try to implement the Graph-Search algorithm from R&N figure 3.7
            //In the case of "failure to find a solution" you should return null.
            //Some useful methods on the state class which you will need to use are:
            //state.isGoalState() - Returns true if the state is a goal state.
            //state.extractPlan() - Returns the Array of actions used to reach this state.
            //state.getExpandedStates() - Returns an ArrayList<State> containing the states reachable from the current state.
            //You should also take a look at Frontier.java to see which methods the Frontier interface exposes
            //
            //printSearchStates(explored, frontier): As you can see below, the code will print out status 
            //(#explored states, size of the frontier, #generated states, total time used) for every 10000th node generated.
            //You might also find it helpful to print out these stats when a solution has been found, so you can keep 
            //track of the exact total number of states generated.


            int iterations = 0;

            // initialize the frontier using the initial state of problem
            frontier.add(initialState);
            // initialize the explored set to be empty
            HashSet<State> explored = new HashSet<>();

            while (true) {
                //Print a status message every 10000 iteration
                if (++iterations % 10000 == 0) {
                    printSearchStatus(explored, frontier);
                }

                // if the frontier is empty then return failure
                if (frontier.isEmpty()) {
                    return null;
                }

                // choose a leaf node and remove it from the frontier
                // pop returns all leaf nodes from the frontier
                State currentState = frontier.pop();

                // if the node contains a goal state then return the corresponding solution
                // isGoalState runs through all leif nodes in the current state
                // to see if any of the nodes contain the goal
                if (currentState.isGoalState()) {
                    printSearchStatus(explored,frontier);
                    return currentState.extractPlan();
                }

                // add the node to the explored set
                // this adds all the leif nodes the explored set
                explored.add(currentState);

                // expand the chosen node, adding the resulting nodes to the frontier
                // This expands all leif nodes, and for each node
                // their corresponding new state is added to the frontier of not already explored
                for (State state : currentState.getExpandedStates()) {
                    // only if not in the frontier or explored set
                    if (!frontier.contains(state) && !explored.contains(state)) {
                        frontier.add(state);
                    }
                }
            }
        }
    }

    private static long startTime = System.nanoTime();

    private static void printSearchStatus(HashSet<State> explored, Frontier frontier)
    {
        String statusTemplate = "#Expanded: %,8d, #Frontier: %,8d, #Generated: %,8d, Time: %3.3f s\n%s\n";
        double elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000d;
        System.err.format(statusTemplate, explored.size(), frontier.size(), explored.size() + frontier.size(),
                          elapsedTime, Memory.stringRep());
    }
}
