package searchclient;

import java.util.ArrayList;
import java.util.Map;

public class SubGoal {
    int row;
    int col;
    char character;
    SubGoalType type;
    ArrayList<Character> goalBoxes;
   // boolean obstruction;

    public SubGoal (int row, int col, char goalChar, SubGoalType type, ArrayList<Character> goalBoxes) {
        this.row = row;
        this.col = col;
        this.character = goalChar;
        this.type = type;
        this.goalBoxes = goalBoxes;
        //this.obstruction = obstruction;
    }

    public boolean completed(AgentState agentState) {
        switch (type){
            case GET_TO_BOX:
                return (Math.abs(agentState.row - row) == 1
                        && Math.abs(agentState.col - col) == 0)
                        ||
                        (Math.abs(agentState.row - row) == 0
                        && Math.abs(agentState.col - col) == 1);
            case PUSH_BOX_TO_GOAL:
                return agentState.boxes[row][col] == character;

            case MOVE_BOX_TO_HELP:
                int endRow = CostCalculator.findBox(agentState.boxes, character)[0];
                int endCol = CostCalculator.findBox(agentState.boxes, character)[1];
                int rowDiff = Math.abs(row - endRow);
                int colDiff = Math.abs(col - endCol);
                int manHLength = rowDiff + colDiff;
                return (manHLength>20);
            case MOVE_OUT_OF_THE_WAY:
                int rowDiff1 = Math.abs(row - agentState.row);
                int colDiff1 = Math.abs(col - agentState.col);
                int manHLength1 = rowDiff1 + colDiff1;
                return (manHLength1>20);
            case GET_TO_COORDINATE:
            case DONE:
                return agentState.col == col && agentState.row == row;
            default:
                return false;
        }
    }


    static boolean completedFirstSubGoal(State state, Map<Integer, SubGoal> subGoals) {
        boolean completed = false;

        for (Map.Entry<Integer, Integer> entry : state.agentRows.entrySet()) {
            int a = entry.getKey();
            SubGoal subGoal = subGoals.get(a);

            int agentRow = state.agentRows.get(a);
            int agentCol = state.agentCols.get(a);

            switch (subGoal.type) {
                case GET_TO_BOX:
                    completed = (Math.abs(agentRow - subGoal.row) == 1
                            && Math.abs(agentCol - subGoal.col) == 0)
                            ||
                            (Math.abs(agentRow - subGoal.row) == 0
                            && Math.abs(agentCol - subGoal.col) == 1);
                    break;
                case PUSH_BOX_TO_GOAL:
                    completed = state.boxes[subGoal.row][subGoal.col] == subGoal.character;
                    break;
                case GET_TO_COORDINATE:
                    completed = agentCol == subGoal.col && agentRow == subGoal.row;
                    break;
                case MOVE_BOX_TO_HELP:
                    int endRow = CostCalculator.findBox(state.boxes, subGoal.character)[0];
                    int endCol = CostCalculator.findBox(state.boxes, subGoal.character)[1];
                    int rowDiff = Math.abs(subGoal.row - endRow);
                    int colDiff = Math.abs(subGoal.col - endCol);
                    int manHLength = rowDiff + colDiff;
                    completed = (manHLength>20);
                    break;
                case MOVE_OUT_OF_THE_WAY:
                    int rowDiff1 = Math.abs(subGoal.row - agentRow);
                    int colDiff1 = Math.abs(subGoal.col - agentCol);
                    int manHLength1 = rowDiff1 + colDiff1;
                    completed = (manHLength1>20);
            }
            if (completed) break;
        }
        return completed;
    }

    @Override
    public String toString() {
        String name = "none";
        switch (this.type) {
            case GET_TO_COORDINATE:
                name = "GET_TO_COORDINATE";
                break;
            case GET_TO_BOX:
                name = "GET_TO_BOX";
                break;
            case PUSH_BOX_TO_GOAL:
                name = "PUSH_BOX_TO_GOAL";
                break;
            case OVERALL_AGENT_COST:
                name = "OVERALL_AGENT_COST";
                break;
            case MOVE_BOX_TO_HELP:
                name = "MOVE_BOX_TO_HELP";
                break;
            case MOVE_OUT_OF_THE_WAY:
                name = "MOVE_OUT_OF_THE_WAY";
                break;
            case DONE:
                name = "DONE";
                break;
        }
        return name+" "+character+" ("+row+","+col+")";
    }
}

