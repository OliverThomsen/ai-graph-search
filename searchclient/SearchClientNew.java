package searchclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class SearchClientNew {
    public static void main(String[] args) throws IOException {
        System.out.println("New and improved SearchClient");

        // Parse level
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.US_ASCII));
        State originalState = SearchClient.parseLevel(serverMessages);

        Frontier frontier = new FrontierBestFirst(new HeuristicGreedy(originalState));

        // Final plan
        ArrayList<Action[]> finalPlan = new ArrayList<>();

        // Split state into sub goals by splitting state into sub states
        // find partial plan and append to final plan

        State partialState = originalState;

        // Choose one box color. Replace all other boxes with a wall. Save this in partial state
        extractPartialState(partialState);

        // Do while partial states exist
            ArrayList<Action[]> partialPlan;
            try {
                partialPlan = search(partialState, frontier);
            } catch (OutOfMemoryError err) {
                System.err.println("Max memory usage exceeded");
                partialPlan = null;
            }

            finalPlan.addAll(partialPlan);
            // Find new partial state for next round
            extractPartialState(partialState);

        // end Do while


        // Send final plan to server
        if (finalPlan != null) {
            System.err.format("Found solution og length %d", finalPlan.size());
            for (Action[] jointAction : finalPlan) {
                System.out.print(jointAction[0].name);
                for (int i = 1; i < jointAction.length; i++) {
                    System.out.print("|");
                    System.out.print(jointAction[i].name);
                }
                System.out.println();
                serverMessages.readLine();
            }
        } else {
            System.err.println("Unable to solve level.");
            System.exit(0);
        }
    }

    static void extractPartialState(State state) {
        // Black out boxes that we want the search algorithm to ignore
        // Should know which color we are currently looking at
        // look for boxes at goal state, make them walls
        // take next box color, and make these boxes not be a wall anymore
        // find these boxes by comparing to originalState
        // If a box from the originalState is a wall in the input partialState, make it a box again
    }

    static ArrayList<Action[]> search(State state, Frontier frontier) {
        Action[][] plan = GraphSearch.search(state, frontier);
        return (ArrayList) Arrays.asList(plan);
    }
}
