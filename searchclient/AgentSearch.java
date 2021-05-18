package searchclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AgentSearch {
    public AgentState mainState;

    public AgentSearch(AgentState state) {
        this.mainState = state;
    }

    public Action[] getNextSubPLan(SubGoal goal) {
        Map<Integer, SubGoal> subGoal = new HashMap<>();
        subGoal.put(mainState.agent - '0', goal);

        Integer[][] referenceMap = Preprocessing.getReferenceMap(mainState.walls, goal);
        Map<Integer, Integer[][]> referenceMaps = new HashMap<>(1);
        referenceMaps.put(mainState.agent - '0', referenceMap);

        System.err.println();
        Frontier frontier = new FrontierBestFirst(new HeuristicGreedy(referenceMaps, subGoal));
        mainState = (AgentState) GraphSearch.search(mainState, frontier);
        assert mainState != null;
        return mainState.extractPlan();
    }

    public void rollBackState(int steps) {
        for (int step = 0; step < steps ; step++) {
            mainState = mainState.parent;
        }
    }

    public SubGoal getNextSubGoal() {
        ArrayList<Character> goalBoxes = new ArrayList<>();
        for (int row = 0; row < mainState.boxes.length; row++) {
            for (int col = 0; col < mainState.boxes[0].length; col++) {
                char box = mainState.boxes[row][col];
                if (SearchClient.isBox(box)) {
                    if (mainState.goals[row][col] == box) {
                        goalBoxes.add(box);
                    }
                }
            }
        }


        // Look for a box next to the agent
        char boxNextTo = 0;
        int boxNextToRow = -1;
        int boxNextToCol = -1;

        int[][] coordinates = new int[][]{{-1,0},{1,0},{0,-1},{0,1}};
        for (int[] c : coordinates) {
            int boxRow = mainState.row+c[0];
            int boxCol = mainState.col+c[1];
            char box = mainState.boxes[boxRow][boxCol];
            if (SearchClient.isBox(box)) {
                boxNextTo = box;
                boxNextToRow = boxRow;
                boxNextToCol = boxCol;
            }
        }
        if (boxNextTo != 0) {
            for (int row = 0; row < mainState.goals.length; row++) {
                for (int col = 0; col < mainState.goals[0].length; col++) {
                    char goal = mainState.goals[row][col];
                    // if goal belongs to box
                    if (goal == boxNextTo) {
                        // if not already on goals
                        if (!(row == boxNextToRow && col == boxNextToCol)) {
                            return new SubGoal(row, col, boxNextTo, SubGoalType.PUSH_BOX_TO_GOAL, goalBoxes);
                        }
                    }
                }
            }
        }

        // Else find box not on goal
        for (int row = 0; row < mainState.boxes.length; row++) {
            for (int col = 0; col < mainState.boxes[0].length; col++) {
                char lonelyBox = mainState.boxes[row][col];
                if (SearchClient.isBox(lonelyBox) && mainState.goals[row][col] != lonelyBox) {
                    return new SubGoal(row, col, lonelyBox, SubGoalType.GET_TO_BOX, goalBoxes);
                }
            }
        }

        // Else find agent goal
        for (int row = 0; row < mainState.goals.length; row++) {
            for (int col = 0; col < mainState.goals[0].length; col++) {
                char goal = mainState.goals[row][col];
                // if goal belongs to agent
                if (goal == this.mainState.agent) {
                    return new SubGoal(row, col, goal, SubGoalType.GET_TO_COORDINATE, goalBoxes);
                }
            }
        }

        return null;
    }

    public void applyAction(Action action) {
        this.mainState = new AgentState(this.mainState, action);
    }

    private AgentState getRelaxedState() {

        int agentRow = mainState.row;
        int agentCol = mainState.col;
        Color color = mainState.color;
        char agent = mainState.agent;
        char[][] boxes = new char[mainState.boxes.length][mainState.boxes[0].length];
        char[][] goals = new char[mainState.goals.length][mainState.goals[0].length];
        boolean[][] walls = mainState.walls;

        char chosenBox;
        char agentGoal;

        // Loop over all boxes
        for (int row = 1; row < mainState.boxes.length - 1; row++) {
            for (int col = 1; col < mainState.boxes[row].length - 1; col++) {

                char box =  mainState.boxes[row][col];
                if (SearchClient.isBox(box)) {
                    // always chose boxes that are already on their goal
                    if (mainState.goals[row][col] == box) {
                        goals[row][col] = box;
                        boxes[row][col] = box;
                    } else {
                        chosenBox = box;
                    }
                }

                char goal = mainState.goals[row][col];
                if (isAgentGoal(goal)) {
                    agentGoal = goal;
                }
            }
        }

        return new AgentState(agentRow, agentCol, color, agent, walls, boxes, goals);
    }

    // returns the cell where a box was in the way
    private int[] applyApplicablePlan(Action[] plan, AgentState relaxedState) {
        int[] cell = null;
        int i;
        for (i = 0 ; i < plan.length ; i++) {
            cell = mainState.conflictingCell(plan[i]);
            if (cell == null) {
                // correct move apply
                mainState = new AgentState(mainState, plan[i]);
                relaxedState = new AgentState(relaxedState, plan[i]);
            } else {
                break;
            }
        }
        return cell;
    }

    private boolean isAgentGoal(char c) {
        return ('0' <= c && c <= '9');
    }



}






