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
    static AgentSearch[] agentSearches;
    static Map<Integer, SubGoal> currentSubGoals;

    public static void main(String[] args) throws IOException {
        System.out.println("MOLILAK");

        // Parse level
        serverMessages = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.US_ASCII));
        originalState = Parser.parseLevel(serverMessages);

        int numAgents = originalState.agentRows.size();

        agentPlans = new ArrayList<>(numAgents);
        agentSearches = new AgentSearch[numAgents];
        currentSubGoals = new HashMap<>(numAgents);

        for (int a=0 ; a < numAgents ; a++) {
            agentPlans.add(new ArrayList<>(0));
            AgentState agentState = extractAgentState(originalState, a);
            agentSearches[a] = new AgentSearch(agentState);
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

            SubGoal subGoal = agentSearches[agent].getNextSubGoal();
            System.err.println("Sub Goal: " + subGoal);

            currentSubGoals.put(agent, subGoal);
            agentSearches[agent].mainState.parent = null;
            agentSearches[agent].mainState.action = null;
            agentSearches[agent].mainState.g = 0;
            Action[] plan = agentSearches[agent].getNextSubPLan(subGoal);

            System.err.println(agentSearches[agent].mainState);
            agentPlans.set(agent, new ArrayList<>(Arrays.asList(plan)));

        }
    }

    static void combineAndApplyPlans() {
        boolean moreMoves = true;
        int step = 0;
        int longestPlan = 0;

        while (true) {
            // Find the longest agent plan
            for (ArrayList<Action> plan : agentPlans) {
                longestPlan = Math.max(longestPlan, plan.size());
            }

            // loop over each agent to extract their move, and collect them in a joint action
            Map<Integer, Action> jointAction = new HashMap<>(agentPlans.size());
            for (int agent=0; agent<agentPlans.size(); agent++) {

                // Agent is in goal state and has no more moves
                if (agentSearches[agent].mainState.isGoalState() && agentPlans.get(agent).size() == 0) {
                    jointAction.put(agent, Action.NoOp);
                    // Last agent has finish executing their plan
                    if (step >= longestPlan) {
                        saveRemainingPlans(step);
                        return;
                    }
                    else continue;
                }

                // Agent has no more moves
                if (agentPlans.get(agent).size() == step) {
                    saveRemainingPlans(step); // saves the remaining plans for the other agents
                   return;
                }

                jointAction.put(agent, agentPlans.get(agent).get(step));
            }

            // Check if joint action is conflicting in original state
            int[] conflictingAgents = originalState.conflictingAgents(jointAction);

            if (conflictingAgents.length > 1) {

                jointAction.forEach((key, val) -> System.err.print(key +": "+ val+", "));
                System.err.println("");
                System.err.print("conflicting agents: " );
                for (int a : conflictingAgents) System.err.print(a +" "+ jointAction.get(a) +"; " );
                System.err.println(" ");

                // todo: improve by giving new sub goals to agents instead of putting them in same state ex. if only one conflicting agent with a box
                // todo: check if box is blocking or if agent can move around
                AgentState[] conflictingStates = new AgentState[conflictingAgents.length];
                // Save the good plan so far
                saveRemainingPlans(step);
                int i = 0;
                for (int agent : conflictingAgents) {
                    // roll back individual agent states to before conflict
                    agentSearches[agent].rollBackState(agentPlans.get(agent).size());
                    // delete remaining plan after the conflict
                    agentPlans.get(agent).clear();
                    conflictingStates[i] = agentSearches[agent].mainState;
                    i++;
                }
                State conflictState = putAgentsIntoSameState(conflictingStates);
                solveConflictingState(conflictState);
                return;
            }

            else {
                // Apply joint action to original state
                jointAction.forEach((key, val) -> System.err.print(key +": "+val+ ", "));
                System.err.println("\n"+originalState);
                originalState = new State(originalState, jointAction);
                step++;
            }
        }
    }

    static void solveConflictingState(State state) {
        System.err.println("CONFLICT");
        System.err.println(state);
        Map<Integer, Integer[][]> referenceMaps = new HashMap<>(state.agentRows.size());
        for (Map.Entry<Integer,Integer> entry : state.agentRows.entrySet()) {
            int agent = entry.getKey();
            SubGoal subGoal = currentSubGoals.get(agent);
            Integer[][] referenceMap = Preprocessing.getReferenceMap(state.walls, subGoal);
            referenceMaps.put(agent, referenceMap);
        }

        Frontier frontier = new FrontierBestFirst(new HeuristicGreedy(referenceMaps, currentSubGoals));
        System.err.println(state);
        State searchedState = (State) GraphSearch.search(state, frontier);
        System.err.println("Solved conflict");
        System.err.println(searchedState);
        Map<Integer, Action>[] jointActions = searchedState.extractPlan();
        for (Map<Integer, Action> jointAction : jointActions) {
            for (Map.Entry<Integer, Action> entry: jointAction.entrySet()) {
                int agent = entry.getKey();
                Action action = entry.getValue();
                agentPlans.get(agent).add(action);
                agentSearches[agent].applyAction(action);
            }
        }
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
        for (int row = 0; row < rows ; row++) {
            for (int col = 0; col < cols; col++) {
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
        System.err.println("Final state");
        System.err.println(originalState);
        Map<Integer, Action>[] finalPlan = originalState.extractPlan();
        if (finalPlan.length > 0) {
            System.err.format("Found solution of length %d", finalPlan.length);
            for (Map<Integer, Action> jointAction : finalPlan) {
                System.out.print(jointAction.get(0).name);
                for (int i = 1; i < jointAction.size(); i++) {
                    System.out.print("|");
                    System.out.print(jointAction.get(i).name);
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
