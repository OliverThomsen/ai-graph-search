package searchclient;

import java.util.Map;

public class SubGoal {
    int row;
    int col;
    char character;
    SubGoalType type;

    public SubGoal (int row, int col, char goalChar, SubGoalType type) {
        this.row = row;
        this.col = col;
        this.character = goalChar;
        this.type = type;
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
            case GET_TO_COORDINATE:
                return agentState.col == col && agentState.row == row;
            default:
                return false;
        }
    }

    static boolean completed(State state, Map<Integer, SubGoal> subGoals) {
        boolean completed = true;

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
            }
            if (!completed) break;
        }
        return completed;
    }
}

