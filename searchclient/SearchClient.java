package searchclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SearchClient {
    static State originalState;
    static ArrayList<ArrayList<Action>> agentPlans;
    static ArrayList<Action[]> finalPlan = new ArrayList<>();
    static BufferedReader serverMessages;
    static Preprocessing preprocessing;
    static Integer[][] referenceMap;
    static AgentSearch[] agentSearches;

    public static void main(String[] args) throws IOException {
        System.out.println("MOLILAK");

        // Parse level
        serverMessages = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.US_ASCII));
        originalState = Parser.parseLevel(serverMessages);

        preprocessing = new Preprocessing(originalState);
        referenceMap = preprocessing.getReferenceMap();

        agentPlans = new ArrayList<>(originalState.agentRows.size());

        agentSearches = new AgentSearch[originalState.agentRows.size()];
        for (int a=0 ; a < originalState.agentRows.size() ; a++) {
            agentPlans.add(new ArrayList<>(0));
            AgentState agentState = extractAgentState(originalState, a);
            agentSearches[a] = new AgentSearch(agentState, referenceMap);
        }

        while (!originalState.isGoalState()) {
            findPartialAgentPlans();
            combineAndApplyPlans();
        }

        sendPlanToServer();
    }

    static void findPartialAgentPlans() {
        for (int agent = 0; agent < agentSearches.length ; agent++ ) {
            // If agent already has a plan then skip agent
            if (agentPlans.get(agent).size() != 0) {
                continue;
            }

            // If agent is in final goal state do not get new sub plan
            if (agentSearches[agent].mainState.isGoalState()) {
                continue;
            }

            Action[] plan = agentSearches[agent].getNextSubPLan();
            agentPlans.set(agent, new ArrayList<>(Arrays.asList(plan)));

        }
    }

    static void combineAndApplyPlans() {
        boolean moreMoves = true;
        int step = 0;
        int longestPlan = 0;

        while (moreMoves) {
            // make sure at least on agent still has a plan
            for (ArrayList<Action> plan : agentPlans) {
                longestPlan = Math.max(longestPlan, plan.size());
            }

            // loop over each agent to extract their move, and collect them in a joint action
            Action[] jointAction = new Action[agentPlans.size()];
            for (int agent=0; agent<agentPlans.size(); agent++) {

                // Agent is in goal state and has no more moves
                if (agentSearches[agent].mainState.isGoalState() && agentPlans.get(agent).size() == 0) {
                    jointAction[agent] = Action.NoOp;
                    // If last agent to finish executing their plan
                    if (step >= longestPlan) {
                        moreMoves = false;
                        break;
                    } else continue;
                }

                // Agent has no more moves
                if (agentPlans.get(agent).size() == step) {
                    saveRemainingPlans(step); // saves the remaining plans for the other agents
                    moreMoves = false;
                    break;
                }

                jointAction[agent] = agentPlans.get(agent).get(step);;
            }

            if (!moreMoves) continue;

            // Check if joint action is conflicting in original state
            int[] conflictingAgents = originalState.conflictingAgents(jointAction);

            while (conflictingAgents.length > 0) {
                AgentState[] states = new AgentState[conflictingAgents.length];
                // Save the good plan so far
                saveRemainingPlans(step);
                // roll back individual agent states to before conflict
                int i = 0;
                for (int agent : conflictingAgents) {
                    agentSearches[agent].rollBackState(agentPlans.get(agent).size() - step);
                    states[i] = agentSearches[agent].mainState;
                    i++;
                }

                // put conflicting agents into same state
                State conflictState = putAgentsIntoSameState(states);
                // todo: find sub goals for each agent
                solveConflictingState(conflictState);

            }

            // Apply joint action to original state
            originalState = new State(originalState, jointAction);
            step++;
        }
    }

    static void solveConflictingState(State state) {
        // search on state
        // merge state plan with agent plans

    }


    static State putAgentsIntoSameState(AgentState[] agentStates) {
        int rows = originalState.boxes.length;
        int cols = originalState.boxes[0].length;

        Map<Integer,Integer> agentRows = new HashMap<>();
        Map<Integer,Integer> agentCols = new HashMap<>();
        Map<Integer,Color> agentColors = new HashMap<>();
        boolean[][] walls = originalState.walls;
        char[][] boxes = new char[rows][cols];
        Map<Character,Color> boxColors = new HashMap<>();
        char[][] goals = new char[rows][cols];

        for (AgentState agentState : agentStates) {
            int a = agentState.agent - '0';
            agentRows.put(a, agentState.row);
            agentCols.put(a, agentState.col);
            agentColors.put(agentState.agent - '0', agentState.color);
        }

        // loop through map
        for (int row = 0; row > rows ; row++) {
            for (int col = 0; col > cols; col++) {
                for (AgentState agentState : agentStates) {
                    char box = agentState.boxes[row][col];
                    if (isBox(box)) {
                        boxes[row][col] = box;
                        boxColors.put(box, agentState.color);
                    }

                    char goal = agentState.goals[row][col];
                    if (isGoal(goal)) {
                        goals[row][col] = goal;
                    }
                }
            }
        }

        return new State(agentRows, agentCols, agentColors, walls, boxes, boxColors, goals);
    }


    static void saveRemainingPlans(int step) {
        for (int agent=0 ; agent < agentPlans.size() ; agent++) {
            ArrayList<Action> plan = agentPlans.get(agent);
            if (step <= plan.size()){
                ArrayList<Action> remainingPlan = new ArrayList<>(plan.subList(step, plan.size()));
                agentPlans.set(agent, remainingPlan);
            }
        }

    }

    static void sendPlanToServer() throws IOException{
        System.err.println(originalState);
        Action[][] finalPlan = originalState.extractPlan();
        if (finalPlan.length > 0) {
            System.err.format("Found solution of length %d", finalPlan.length);
            for (Action[] jointAction : finalPlan) {
//                System.err.print(jointAction[0].name);
                System.out.print(jointAction[0].name);
                for (int i = 1; i < jointAction.length; i++) {
//                    System.err.print("|");
//                    System.err.print(jointAction[i].name);
                    System.out.print("|");
                    System.out.print(jointAction[i].name);
                }
//                System.err.println();
                System.out.println();
                serverMessages.readLine();
            }
        } else {
            System.err.println("Unable to solve level.");
            System.exit(0);
        }
    }

    static AgentState extractAgentState(State state, int agent) {
        int agentRow = state.agentRows.get(agent);
        int agentCol = state.agentCols.get(agent);
        Color color = state.agentColors.get(agent);
        char[][] boxes = new char[state.boxes.length][state.boxes[0].length];
        char[][] goals = new char[state.goals.length][state.goals[0].length];
        boolean[][] walls = state.walls;
        // Loop through all cells to find agent boxes
        for (int row = 0; row < state.boxes.length; row++) {
            for (int col = 0; col < state.boxes[0].length; col++) {
                char box = state.boxes[row][col];
                if (isBox(box)) {
                    // Box belongs to agent - keep
                    if (state.boxColors.get(box) == color) {
                        boxes[row][col] = box;
                    }
                }
                char goal = state.goals[row][col];
                if (isGoal(goal) && state.boxColors.get(goal) == color) {
                    goals[row][col] = goal;
                }
            }
        }

        return new AgentState(agentRow, agentCol, color, Character.forDigit(agent, 10), walls, boxes, goals);
    }


    static boolean isBox(char c) {
        return 'A' <= c && c <= 'Z';
    }

    static boolean isGoal(char c) {
        return ('A' <= c && c <= 'Z') || ('0' <= c && c <= '9');
    }
}
