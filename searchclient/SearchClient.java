package searchclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class SearchClient {
    static Color currentColor;
    static State originalState;
    public static void main(String[] args) throws IOException {
        System.out.println("New and improved SearchClient");

        // Parse level
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.US_ASCII));
        originalState = Parser.parseLevel(serverMessages);

        // Final plan
        ArrayList<Action[]> finalPlan = new ArrayList<>();

        // Split state into sub goals by splitting state into sub states
        // find partial plan and append to final plan

        // Choose one box color. Replace all other boxes with a wall. Save this in partial state

        int colorIndex = 0;
        currentColor = originalState.agentColors[colorIndex];
        State partialState = extractPartialState(originalState);
        while(!partialState.isGoalState()) {
            ArrayList<Action[]> partialPlan;
            try {
                Frontier frontier = new FrontierBestFirst(new HeuristicGreedy(partialState));
                partialState = search(partialState, frontier);
            } catch (OutOfMemoryError err) {
                System.err.println("Max memory usage exceeded");
            }
            System.err.println(partialState.toString());
            partialPlan = new ArrayList<>(Arrays.asList(partialState.extractPlan()));
            finalPlan.addAll(partialPlan);
            // Find new partial state for next round
            currentColor = originalState.agentColors[++colorIndex];
            partialState = extractPartialState(partialState);
        }


        // Send final plan to server
        if (finalPlan.size() > 0) {
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

    static State extractPartialState(State previousState) {

        // look for boxes at goal state, make them walls
        // take next box color, and make these boxes not be a wall anymore
        // find these boxes by comparing to originalState
        // If a box from the originalState is a wall in the input partialState, make it a box again

        State state = new State(previousState);

        // Loop over all boxes
        for (int row = 1; row < originalState.boxes.length - 1; row++) {
            for (int col = 1; col < originalState.boxes[row].length - 1; col++) {

                // Make previously greyed out boxes, that have the current color, visible again
                char originalBox = originalState.boxes[row][col];
                if (isBox(originalBox)) {
                    Color color = originalState.boxColors[originalBox - 'A'];
                    if (currentColor == color && state.walls[row][col]) {
                        state.walls[row][col] = false;
                        state.boxes[row][col] = originalBox;
                    }
                }

                // Make previously greyed out goals, that have current color, visible again
                char originalGoal = originalState.goals[row][col];
                if (isBox(originalGoal)) {
                    Color color = originalState.boxColors[originalGoal - 'A'];
                    if (currentColor == color) {
                        state.goals[row][col] = originalGoal;
                    }
                }

                // Grey out boxes that does not have the current color
                char box = state.boxes[row][col];
                if (isBox(box)) {
                    Color color = state.boxColors[box - 'A'];
                    if (color != currentColor) {
                        state.boxes[row][col] = 0;
                        state.walls[row][col] = true;
                    }
                }

                //Grey out goals that does not have current color
                char goal = state.goals[row][col];
                if (isBox(goal)) {
                    Color color = state.boxColors[goal - 'A'];
                    if (color != currentColor) {
                        state.goals[row][col] = 0;
                    }
                }
            }
        }
        return state;
    }

    static State search(State state, Frontier frontier) {
        return GraphSearch.search(state, frontier);
    }

    static boolean isBox(char c) {
        return 'A' <= c && c <= 'Z';
    }
}
