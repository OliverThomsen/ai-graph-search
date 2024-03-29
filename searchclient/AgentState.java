package searchclient;

import java.util.ArrayList;
import java.util.Arrays;

public class AgentState {
    public int row, col;
    public static Color color;
    public static char agent;
    public static boolean[][] walls;
    public char[][] boxes;
    public static char[][] goals;
    public AgentState parent;
    public Action action;
    private final int g;
    private int hash;

    // Initial agent state
    public AgentState(int row, int col, Color color, char agent, boolean[][] walls, char[][] boxes, char[][] goals) {
        AgentState.color = color;
        AgentState.agent = agent;
        AgentState.walls = walls;
        AgentState.goals = goals;
        this.row = row;
        this.col = col;
        this.boxes = boxes;
        this.parent = null;
        this.action = null;
        this.g = 0;
    }

    // Construct state from applied action
    public AgentState(AgentState parentState, Action action) {
        // Copy parent
        this.row = parentState.row;
        this.col = parentState.col;
        this.boxes = new char[parentState.boxes.length][];
        for (int i = 0; i < parentState.boxes.length; i++)
        {
            this.boxes[i] = Arrays.copyOf(parentState.boxes[i], parentState.boxes[i].length);
        }
        // Set own params
        this.parent = parentState;
        this.action = action;
        this.g = parentState.g + 1;
        // apply action
        char box;
        switch (action.type)
        {
            case NoOp:
                break;

            case Move:
                this.row += action.agentRowDelta;
                this.col += action.agentColDelta;
                break;

            case Push:
                this.row += action.agentRowDelta;
                this.col += action.agentColDelta;
                int prevBoxRow = this.row;
                int prevBoxCol = this.col;
                int destBoxRow = prevBoxRow + action.boxRowDelta;
                int destBoxCol = prevBoxCol + action.boxColDelta;
                box = this.boxes[prevBoxRow][prevBoxCol];
                this.boxes[prevBoxRow][prevBoxCol] = 0;
                this.boxes[destBoxRow][destBoxCol] = box;
                break;

            case Pull:
                prevBoxRow = this.row - action.boxRowDelta;
                prevBoxCol = this.col - action.boxColDelta;
                destBoxRow = this.row;
                destBoxCol = this.col;
                this.row += action.agentRowDelta;
                this.col += action.agentColDelta;
                box = this.boxes[prevBoxRow][prevBoxCol];
                this.boxes[prevBoxRow][prevBoxCol] = 0;
                this.boxes[destBoxRow][destBoxCol] = box;
        }
    }

    // Constructs copy of state
    public AgentState(AgentState state) {
        this.row = state.row;
        this.col = state.col;
        this.boxes = new char[state.boxes.length][];
        for (int i = 0; i < state.boxes.length; i++)
        {
            this.boxes[i] = Arrays.copyOf(state.boxes[i], state.boxes[i].length);
        }
        this.parent = null;
        this.action = null;
        this.g = 0;
    }

    public Action[] extractPlan() {
        Action[] plan = new Action[this.g];
        AgentState state = this;
        while (state.action != null)
        {
            plan[state.g - 1] = state.action;
            state = state.parent;
        }
        return plan;
    }

    public boolean isGoalState() {
        for (int row = 1; row < this.goals.length - 1; row++) {
            for (int col = 1; col < this.goals[row].length - 1; col++) {
                char goal = this.goals[row][col];
                if ('A' <= goal && goal <= 'Z' && this.boxes[row][col] != goal) {
                    return false;
                }
                else if ('0' <= goal && goal <= '9' && !(this.row == row && this.col == col)) {
                    return false;
                }
            }
        }
        return true;
    }

    public ArrayList<AgentState> getExpandedStates() {
        ArrayList<Action> applicableActions = new ArrayList<>(Action.values().length);
        for (Action action : Action.values()) {
            if (this.isApplicable(action)) {
                applicableActions.add(action);
            }
        }
        ArrayList<AgentState> expandedStates = new ArrayList<>(16);
        for (Action action : applicableActions) {
            expandedStates.add(new AgentState(this, action));
        }
        return expandedStates;
    }

    public boolean isApplicable(Action action) {
        int agentDestRow, agentDestCol;
        int boxDestRow, boxDestCol;
        int boxRow, boxCol;
        char box;

        switch (action.type) {
            case NoOp:
                return true;
            case Move:
                agentDestRow = this.row + action.agentRowDelta;
                agentDestCol = this.col + action.agentColDelta;
                return this.cellIsFree(agentDestRow, agentDestCol);
            case Push:
                agentDestRow = this.row + action.agentRowDelta;
                agentDestCol = this.col + action.agentColDelta;
                // check if there is a box in the agent destination
                box = boxes[agentDestRow][agentDestCol];
                if(box!=0) {
                    // check if box destination is free
                    boxDestRow = agentDestRow + action.boxRowDelta;
                    boxDestCol = agentDestCol + action.boxColDelta;
                    return this.cellIsFree(boxDestRow, boxDestCol);
                }
                return false;
            case Pull:
                // Check if there is a box to pull
                boxRow = this.row - action.boxRowDelta;
                boxCol = this.col - action.boxColDelta;
                box = boxes[boxRow][boxCol];
                if (box != 0) {
                    // Check if agent destination is free
                    agentDestRow = row + action.agentRowDelta;
                    agentDestCol = col + action.agentColDelta;
                    return this.cellIsFree(agentDestRow, agentDestCol);
                }
                return false;
        }
        return false;
    }

    private boolean cellIsFree(int row, int col) {
        return !this.walls[row][col] && this.boxes[row][col] == 0;
    }

    public int g() {
        return this.g;
    }

    @Override
    public int hashCode() {
        if (this.hash == 0) {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.deepHashCode(this.walls);
            result = prime * result + Arrays.deepHashCode(this.goals);
            result = prime * result + this.row;
            result = prime * result + this.col;
            for (int row = 0; row < this.boxes.length; ++row) {
                for (int col = 0; col < this.boxes[row].length; ++col) {
                    char c = this.boxes[row][col];
                    if (c != 0)
                    {
                        result = prime * result + (row * this.boxes[row].length + col) * c;
                    }
                }
            }
            this.hash = result;
        }
        return this.hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        AgentState other = (AgentState) obj;
        return  this.row == other.row &&
                this.col == other.col &&
                Arrays.deepEquals(this.walls, other.walls) &&
                Arrays.deepEquals(this.boxes, other.boxes) &&
                Arrays.deepEquals(this.goals, other.goals);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int row = 0; row < AgentState.walls.length; row++)
        {
            for (int col = 0; col < AgentState.walls[row].length; col++)
            {

                if (this.boxes[row][col] > 0)
                {
                    s.append(this.boxes[row][col]);
                }
                else if (AgentState.walls[row][col])
                {
                    s.append("+");
                }
                else if (this.row == row && this.col == col)
                {
                    s.append(AgentState.agent);
                }
                else
                {
                    s.append(" ");
                }
            }
            s.append("\n");
        }
        return s.toString();
    }
}
