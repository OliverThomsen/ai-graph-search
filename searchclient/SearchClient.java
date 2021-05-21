package searchclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SearchClient {
    static State originalState;
    static ArrayList<ArrayList<Action>> agentPlans;
    static ArrayList<Action[]> finalPlan = new ArrayList<>();
    static BufferedReader serverMessages;
    static AgentSearch[] agentSearches;
    static HashMap<Integer, SubGoal> subGoals;
    static Map<Integer, ArrayList<Character>> agentBoxes;

    //static Map<Integer,Conflict> recentConflicts = new HashMap<>();

    public static void main(String[] args) throws IOException {
        System.out.println("MOLILAK");

        // Parse level
        serverMessages = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.US_ASCII));
        originalState = Parser.parseLevel(serverMessages);
        Map<Integer,AgentState> agentStatesOfSameColor = new HashMap<>();

        int numAgents = originalState.agentRows.size();
        subGoals = new HashMap<>(numAgents);

        agentPlans = new ArrayList<>(numAgents);
        agentSearches = new AgentSearch[numAgents];
        Set<Integer> agentList;
        Map<Color,Set<Integer>> agentsOfSameColor = new HashMap<>();


        for (Color color: Color.values()) {
            agentList = new HashSet<>();
            for (int a = 0; a < numAgents; a++) {
                if (originalState.agentColors.get(a) == color) {
                    agentList.add(a);
                    agentsOfSameColor.put(color, agentList);
                }

            }
        }
        agentStatesOfSameColor = extractAgentStateOfSameColor(originalState,agentsOfSameColor);


        for (int a=0 ; a < numAgents ; a++) {
            AgentState agentState = agentStatesOfSameColor.get(a);
            System.err.println(agentState.toString());
            agentPlans.add(new ArrayList<>(0));
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

            SubGoal subGoal = agentSearches[agent].getNextSubGoal();

            //If agent is has subGoal done do not get new sub plan
            if (subGoal.type==SubGoalType.DONE) {
                continue;
            }

            // if last subGoal was get to box, push that box to its goal
            if (subGoals.get(agentSearches[agent].mainState.agent-'0')!=null) {
                System.err.println(subGoals.get(agentSearches[agent].mainState.agent-'0').type.equals(SubGoalType.GET_TO_BOX));
                if (subGoals.get(agentSearches[agent].mainState.agent-'0').type.equals(SubGoalType.GET_TO_BOX)) {
                    char box = subGoals.get(agent).character;
                    int boxRow = subGoals.get(agent).row;
                    int boxCol = subGoals.get(agent).col;
                    // find box goal
                    int goalRow = CostCalculator.findBox(agentSearches[agent].mainState.goals, box)[0];
                    int goalCol = CostCalculator.findBox(agentSearches[agent].mainState.goals, box)[1];
                    if (!(boxRow==goalRow && boxCol==goalCol)) {
                        subGoal = new SubGoal(goalRow, goalCol, box, SubGoalType.PUSH_BOX_TO_GOAL, subGoal.goalBoxes);
                    }

                }
            }



            System.err.println("Sub Goal: " + subGoal);
            subGoals.put(agent,subGoal);

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
                        //recentConflicts.clear();
                        return;
                    }
                    else continue;
                }

                // Agent has no more moves
                if (agentPlans.get(agent).size() == step) {
                    saveRemainingPlans(step); // saves the remaining plans for the other agents
                    //recentConflicts.clear();
                    return;
                }

                jointAction.put(agent, agentPlans.get(agent).get(step));
            }

            // Check if joint action is conflicting in original state
            Map<Integer, Conflict> conflictingAgents = originalState.allConflictingAgents(jointAction);
            Set<Integer> conflictingAgentNumbers = new HashSet<>();
            for (Map.Entry<Integer, Conflict> entry: conflictingAgents.entrySet()) {
                conflictingAgentNumbers.add(entry.getKey());
                conflictingAgentNumbers.add(entry.getValue().getConflictAgent());
                System.err.print("line 112 conflict, the agent is : " + entry.getKey() +" "+ jointAction.get(entry.getKey()) +"; " );
                System.err.println(" ");
                Conflict co = (Conflict) entry.getValue();
                System.err.println("the conflict is this: conflicting agent" + co.getConflictAgent() + " the coordinates is " + co.getCoordinatesOfConflict()[0] + co.getCoordinatesOfConflict()[1]);
            }
            System.err.println(" ");


            if (conflictingAgentNumbers.size() >= 1) {
                //conflictingAgents.putAll(recentConflicts);
                jointAction.forEach((key, val) -> System.err.print(key +": "+ val+", "));
                System.err.println("");
                System.err.print("conflicting agents: " );
                for (Map.Entry entry: conflictingAgents.entrySet()) {
                    System.err.print(entry.getKey() +" "+ jointAction.get(entry.getKey()) +"; " );
                }
                System.err.println(" ");

                // todo: improve by giving new sub goals to agents instead of putting them in same state ex. if only one conflicting agent with a box
                // todo: check if box is blocking or if agent can move around

                AgentState[] conflictingStates = new AgentState[conflictingAgentNumbers.size()];
                // Save the good plan so far
                saveRemainingPlans(step);
                int i = 0;
                for (int agentNumber: conflictingAgentNumbers) {
                    // roll back individual agent states to before conflict
                    agentSearches[agentNumber].rollBackState(agentPlans.get(agentNumber).size());
                    // delete remaining plan after the conflict
                    agentPlans.get(agentNumber).clear();

                    System.err.println("now the conflicting agents own states");
                    System.err.println(agentSearches[agentNumber].mainState);
                    conflictingStates[i] = agentSearches[agentNumber].mainState;
                    i++;
                }
                State conflictState = putAgentsIntoSameState(conflictingStates);
                solveConflictingState(conflictState, conflictingAgents);
                //recentConflicts.putAll(conflictingAgents);
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

    static void solveConflictingState(State state, Map<Integer, Conflict> conflictMap) {
        System.err.println("CONFLICT");
        System.err.println(state);
        int numAgents = state.agentRows.size();
        Map<Integer,Integer[][]> referenceMaps = new HashMap<>(numAgents);

        for (Map.Entry<Integer,Conflict> entry : conflictMap.entrySet()) {
            int agent = entry.getKey();
            Conflict co = (Conflict) entry.getValue();

            System.err.println("the conflict is this: this agent has a conflict " + agent + " conflicting agent: " + co.getConflictAgent() + " the coordinates is " + co.getCoordinatesOfConflict()[0] + co.getCoordinatesOfConflict()[1]+
                    " " +co.isStationary() + "the conflicting char: " + co.getConflictChar());
            if (entry.getValue().isStationary() && entry.getValue().getConflictChar() >= 'A' && entry.getValue().getConflictChar() <= 'Z'){
                co = entry.getValue();
                SubGoal subGoal = new SubGoal(co.getCoordinatesOfConflict()[0],co.getCoordinatesOfConflict()[1]
                ,co.getConflictChar(),SubGoalType.MOVE_BOX_TO_HELP,agentSearches[co.getConflictAgent()].getGoalboxes());
                subGoals.put(co.getConflictAgent(),subGoal);
            }
            if (entry.getValue().isStationary() && entry.getValue().getConflictChar() >= '0' && entry.getValue().getConflictChar() <= '9' &&
                    (subGoals.get(entry.getValue().getConflictAgent()).type == SubGoalType.DONE ||
                            subGoals.get(entry.getValue().getConflictAgent()).type == SubGoalType.GET_TO_COORDINATE) ){
                co = entry.getValue();
                SubGoal subGoal = new SubGoal(co.getCoordinatesOfConflict()[0],co.getCoordinatesOfConflict()[1]
                        ,co.getConflictChar(),SubGoalType.MOVE_OUT_OF_THE_WAY,agentSearches[co.getConflictAgent()].getGoalboxes());
                subGoals.put(co.getConflictAgent(),subGoal);
            }


            System.err.print(agent + ": "+subGoals.get(agent)+", ");
            Integer[][] referenceMap = Preprocessing.getReferenceMap(state.walls, subGoals.get(agent));
            referenceMaps.put(agent, referenceMap);
            Integer[][] referenceMap2 = Preprocessing.getReferenceMap(state.walls, subGoals.get(co.getConflictAgent()));
            referenceMaps.put(co.getConflictAgent(), referenceMap2);
        }

        Frontier frontier = new FrontierBestFirst(new HeuristicGreedy(referenceMaps, subGoals));
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
        State state = new State(agentRows, agentCols, agentColors, walls, boxes, boxColors, goals);
        state.setAgentBoxes(agentBoxes);
        return state;
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
                if ((isBox(goal) && state.boxColors.get(goal) == color) || goal-'0' == agent) {
                    goals[row][col] = goal;
                }
            }
        }

        return new AgentState(agentRow, agentCol, color, Character.forDigit(agent, 10), walls, boxes, goals);
    }

    static Map<Integer, AgentState> extractAgentStateOfSameColor(State state, Map<Color,Set<Integer>> agentsOfSameColor) {
        agentBoxes = new HashMap<>();
        ArrayList<Character> agentsBoxes;

        for (Map.Entry<Color,Set<Integer>> entry: agentsOfSameColor.entrySet()) {
            for (Integer agent: entry.getValue()){
                agentBoxes.put(agent,new ArrayList<>());
            }
        }

        for (Map.Entry<Color,Set<Integer>> entry: agentsOfSameColor.entrySet())
        {
            List<Character> boxes = new ArrayList<>();
            int[] modulusConverter = new int[entry.getValue().size()];
            int j = 0;
            for (int agentIndex : entry.getValue()){
                modulusConverter[j] = agentIndex;
                j++;
            }
            for (int row = 0; row < state.boxes.length; row++) {
                for (int col = 0; col < state.boxes[0].length; col++) {
                    char box = state.boxes[row][col];
                    if (isBox(box)) {
                        // Box belongs to agent - keep
                        if (state.boxColors.get(box) == entry.getKey()) {
                            boxes.add(box);
                        }
                    }

                }
            }

            for (int i = 0; i < boxes.size(); i++) {
                int agent = i % entry.getValue().size();
                agentsBoxes = agentBoxes.get(modulusConverter[agent]);
                agentsBoxes.add(boxes.get(i));
                agentBoxes.put(modulusConverter[agent],agentsBoxes);
                System.err.println( agent + " " + modulusConverter[agent] + " " + boxes.get(i));
            }
        }
        for (Map.Entry<Integer,ArrayList<Character>> entry: agentBoxes.entrySet() ){
            System.err.println("i am agent: "+ entry.getKey());
            for (char c : entry.getValue()){
                System.err.print(c);
            }
        }
        Map<Integer,AgentState> agentStates = new HashMap<Integer, AgentState>();
        for (Map.Entry<Integer, ArrayList<Character>> agent: agentBoxes.entrySet()) {
            AgentState agentState = extractAgentState2(originalState,agent.getKey(),agent.getValue());
            agentStates.put(agent.getKey(),agentState);

        }

        originalState.setAgentBoxes(agentBoxes);
        return agentStates;

    }

    static AgentState extractAgentState2(State state, int agent, ArrayList<Character> agentboxes) {
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
                    if (state.boxColors.get(box) == color && agentboxes.contains(box)) {
                        boxes[row][col] = box;
                    }
                }
                char goal = state.goals[row][col];
                if ((isBox(goal) && state.boxColors.get(goal) == color) || goal-'0' == agent) {
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
