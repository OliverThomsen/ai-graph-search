package searchclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class SearchClient {
    static State originalState;
    static AgentState[] agentStates;
    static ArrayList<ArrayList<Action>> agentPlans;
    static ArrayList<Action[]> finalPlan = new ArrayList<>();
    static BufferedReader serverMessages;
    static Preprocessing preprocessing;
    static Integer[][] referenceMap;

    public static void main(String[] args) throws IOException {
        System.out.println("Molilak");

        // Parse level
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.US_ASCII));
        originalState = Parser.parseLevel(serverMessages);

        preprocessing = new Preprocessing(originalState);
        referenceMap = preprocessing.getReferenceMap();

        agentPlans = new ArrayList<>(originalState.agentRows.length);
        for (int a=0 ; a < originalState.agentRows.length ; a++) {
            agentPlans.add(new ArrayList<>(0));
        }

        while (!originalState.isGoalState()) {
            findPartialAgentPlans();
            combineAndApplyPlans();
        }

        sendPlanToServer(finalPlan);
    }

    static void findPartialAgentPlans() {
        int agentIndex = 0;
        while(agentIndex < originalState.agentRows.length) {

            // If agent already has a plan then skip agent
            if (agentPlans.get(agentIndex).size() == 0) {
                agentIndex++;
                continue;
            }

            AgentState agentState = extractAgentState(originalState, agentIndex);
            try {
                Frontier frontier = new FrontierBestFirst(new HeuristicGreedy(agentState,referenceMap));
                agentState = search(agentState, frontier);
            } catch (OutOfMemoryError err) {
                System.err.println("Max memory usage exceeded");
            }
            Action[] agentPlan = agentState.extractPlan();
            // save agent state
            agentStates[agentIndex] = agentState;
            // Append agent plan to previous agent plan
            agentPlans.get(agentIndex).addAll(new ArrayList<>(Arrays.asList(agentPlan)));
            // mergeIntoFinalPlan(agentPlan, agentIndex);
            agentIndex++;
        }
    }

    static void combineAndApplyPlans() {
        boolean moreMoves = true;
        int step = 0;
        while (moreMoves) {
            // loop over each agent to extract their move, and collect them in a joint action
            Action[] jointAction = new Action[agentPlans.size()];
            for (int agent=0; agent<agentPlans.size(); agent++) {
                // make sure that there are more moves in the agent plan
                if (agentPlans.get(agent).size() == step) {
                    saveRemainingPlans(step);
                    moreMoves = false;
                    break;
                }
                Action action = agentPlans.get(agent).get(step);
                jointAction[agent] = action;
            }

            if (!moreMoves) continue;

            // check if joint action is conflicting in original state
            int[] conflictingAgents = originalState.conflictingAgents(jointAction);
            while (conflictingAgents.length > 0) {
                // Insert NoOp into one of the agent plans
                int agent = conflictingAgents[0];
                agentPlans.get(agent).add(step,Action.NoOp);
                jointAction[agent] = Action.NoOp;
                conflictingAgents = originalState.conflictingAgents(jointAction);
            }
            // apply joint action to original state
            originalState = new State(originalState, jointAction);
            step++;
        }
    }

    static void saveRemainingPlans(int step) {
        for (int agent=0 ; agent < agentPlans.size() ; agent++) {
            ArrayList<Action> plan = agentPlans.get(agent);
            ArrayList<Action> remainingPlan = new ArrayList<>(plan.subList(step, plan.size()));
            agentPlans.set(agent, remainingPlan);
        }

    }

    static void sendPlanToServer(ArrayList<Action[]> finalPlan) throws IOException{
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

    static AgentState extractAgentState(State state, int agent) {
        int agentRow = state.agentRows[agent];
        int agentCol = state.agentCols[agent];
        Color color = state.agentColors[agent];
        char[][] boxes = new char[state.boxes.length][state.boxes[0].length];
        char[][] goals = new char[state.goals.length][state.goals[0].length];
        boolean[][] walls = state.walls;
        // Loop through all cells to find agent boxes
        for (int row = 0; row < state.boxes.length; row++) {
            for (int col = 0; col < state.boxes[0].length; col++) {
                char box = state.boxes[row][col];
                if (isBox(box)) {
                    // Box belongs to agent - keep
                    if (state.boxColors[box - 'A'] == color) {
                        boxes[row][col] = box;
                    }
                    // Box belongs to other agent - make wall
                    else {
                        walls[row][col] = true;
                    }
                }
                char goal = state.goals[row][col];
                if (isGoal(goal) && state.boxColors[goal - 'A'] == color) {
                    goals[row][col] = goal;
                }
            }
        }
        // Make other agent positions into walls
        for (int i = 0; i < originalState.agentRows.length; i++) {
            if (i != agent) {
                walls[originalState.agentRows[i]][originalState.agentCols[i]] = true;
            }
        }
        return new AgentState(agentRow, agentCol, color, Character.forDigit(agent, 10), walls, boxes, goals);
    }

    static void updateState(AgentState agentState, int agentIndex) {
        Color agentColor = originalState.boxColors[agentIndex];
        for (int row = 1; row < originalState.boxes.length - 1; row++) {
            for (int col = 1; col < originalState.boxes[row].length - 1; col++) {
                char boxOriginal = originalState.boxes[row][col];
                char boxAgent = agentState.boxes[row][col];
                // Cell does not have box anymore, remove box
                if (isBox(boxOriginal) && !isBox(boxAgent)) {
                    originalState.boxes[row][col] = 0;
                }
                // Cell has new box, add box
                if (!isBox(boxOriginal) && isBox(boxAgent)) {
                    originalState.boxes[row][col] = boxAgent;
                }
                // Move agent from old cell to new cell
                originalState.agentRows[agentIndex] = agentState.row;
                originalState.agentCols[agentIndex] = agentState.col;
            }
        }
    }

    static State extractPartialState(State previousState, int agentIndex) {

        // look for boxes at goal state, make them walls
        // take next box color, and make these boxes not be a wall anymore
        // find these boxes by comparing to originalState
        // If a box from the originalState is a wall in the input partialState, make it a box again

        State state = new State(previousState);
        Color currentColor = originalState.agentColors[agentIndex];

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
                if (isGoal(goal)) {
                    Color color = state.boxColors[goal - 'A'];
                    if (color != currentColor) {
                        state.goals[row][col] = 0;
                    }
                }
            }
        }
        return state;
    }

    static void mergeIntoFinalPlan(Action[] agentPlan, int agentIndex) {
        int numAgents = originalState.agentRows.length;
        Action[][] partialPlan = new Action[agentPlan.length][numAgents];
        for (int i = 0; i < agentPlan.length; i++) {
            for (int a = 0; a < numAgents; a++) {
                if (a == agentIndex) {
                    partialPlan[i][a] = agentPlan[i];
                } else {
                    partialPlan[i][a] = Action.NoOp;
                }

            }
        }
        finalPlan.addAll(new ArrayList<>(Arrays.asList(partialPlan)));
    }

    static AgentState search(AgentState state, Frontier frontier) {
        return GraphSearch.search(state, frontier);
    }

    static boolean isBox(char c) {
        return 'A' <= c && c <= 'Z';
    }

    static boolean isGoal(char c) {
        return ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9');
    }
}
