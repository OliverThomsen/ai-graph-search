package searchclient;

import java.util.HashSet;

public class GraphSearch {

    public static SuperState search(SuperState initialState, Frontier frontier)
    {
        int iterations = 0;

        // initialize the frontier using the initial state of problem
        frontier.add(initialState);
        // initialize the explored set to be empty
        HashSet<SuperState> explored = new HashSet<>();

        while (true) {
            // if the frontier is empty then return failure
            if (frontier.isEmpty()) {
                return null;
            }

            // choose a leaf node and remove it from the frontier
            SuperState currentState = frontier.pop();


            //Print a status message every 10000 iteration
            if (++iterations % 10000 == 0) {
                printSearchStatus(explored, frontier);
                System.err.println(currentState.toString());
            }

            // if the sub subGoal is completed then return the corresponding solution
            if (currentState instanceof State) {
                State state = (State) currentState;
                if (SubGoal.completedFirstSubGoal(state, frontier.getSubGoals())) {
                    printSearchStatus(explored,frontier);
                    return state;
                }
            } else if (currentState instanceof AgentState) {
                AgentState state = (AgentState) currentState;
                SubGoal subGoal = frontier.getSubGoals().get(state.agent - '0');
                if (subGoal.completed(state)) {
                    printSearchStatus(explored,frontier);
                    return state;
                }
            }

            // add the node to the explored set
            explored.add(currentState);

            // expand the chosen node, adding the resulting nodes to the frontier
            for (SuperState state : currentState.getExpandedStates()) {
                // only if not in the frontier or explored set
                if (!frontier.contains(state) && !explored.contains(state)) {
                    frontier.add(state);
                }
            }
        }
    }

    private static long startTime = System.nanoTime();

    private static void printSearchStatus(HashSet<SuperState> explored, Frontier frontier)
    {
        String statusTemplate = "#Expanded: %,8d, #Frontier: %,8d, #Generated: %,8d, Time: %3.3f s\n%s\n";
        double elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000d;
        System.err.format(statusTemplate, explored.size(), frontier.size(), explored.size() + frontier.size(),
                          elapsedTime, Memory.stringRep());
    }


}
