package searchclient;

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
            case AGENT_TO_BOX:
                return (Math.abs(agentState.row - row) == 1
                        && Math.abs(agentState.col - col) == 0)
                        ||
                        (Math.abs(agentState.row - row) == 0
                        && Math.abs(agentState.col - col) == 1);
            case BOX_TO_GOAL:
                return agentState.boxes[row][col] == character;
            case AGENT_TO_GOAL:
                return agentState.col == col && agentState.row == row;
            default:
                return false;
        }
    }
}

enum SubGoalType {
    AGENT_TO_BOX,
    BOX_TO_GOAL,
    AGENT_TO_GOAL
}
