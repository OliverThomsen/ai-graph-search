package searchclient;

import java.util.ArrayList;
import java.util.Arrays;

public class AgentState implements SuperState{
    public int row, col;
    public Color color;
    public char agent;
    public boolean[][] walls;
    public char[][] boxes;
    public char[][] goals;
    public AgentState parent;
    public Action action;
    public int g;
    private int hash;

    // Initial agent state
    public AgentState(int row, int col, Color color, char agent, boolean[][] walls, char[][] boxes, char[][] goals) {
        this.color = color;
        this.agent = agent;
        this.walls = walls;
        this.goals = goals;
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
        this.color = parentState.color;
        this.agent = parentState.agent;
        this.walls = clone(parentState.walls);
        this.goals = clone(parentState.goals);
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
        this.color = state.color;
        this.agent = state.agent;
        this.walls = clone(state.walls);
        this.goals = clone(state.goals);
        this.row = state.row;
        this.col = state.col;
        this.boxes = clone(state.boxes);
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

    public char[][][] extractRoute() {
        AgentState state = this;
        int rows = this.walls.length;
        int cols = this.walls[0].length;
        char[][][] route = new char[this.g][rows][cols];
        while (state.action != null)
        {
            route[state.g - 1] = clone(state.boxes);
            route[state.g - 1][state.row][state.col] = state.agent;
            state = state.parent;
        }
        return route;
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

    public ArrayList<SuperState> getExpandedStates() {
        ArrayList<Action> applicableActions = new ArrayList<>(Action.values().length);
        for (Action action : Action.values()) {
            if (this.conflictingCell(action) == null) {
                applicableActions.add(action);
            }
        }
        ArrayList<SuperState> expandedStates = new ArrayList<>(16);
        for (Action action : applicableActions) {
            expandedStates.add(new AgentState(this, action));
        }
        return expandedStates;
    }

    public int[] conflictingCell(Action action) {
        int agentDestRow, agentDestCol;
        int boxDestRow, boxDestCol;
        int boxRow, boxCol;
        char box;

        switch (action.type) {
            case NoOp:
                return null;
            case Move:
                agentDestRow = this.row + action.agentRowDelta;
                agentDestCol = this.col + action.agentColDelta;
                return this.cellIsFree(agentDestRow, agentDestCol) ? null : new int[]{agentDestRow, agentDestCol};

            case Push:
                agentDestRow = this.row + action.agentRowDelta;
                agentDestCol = this.col + action.agentColDelta;
                // check if there is a box in the agent destination
                box = boxes[agentDestRow][agentDestCol];
                if(box!=0) {
                    // check if box destination is free
                    boxDestRow = agentDestRow + action.boxRowDelta;
                    boxDestCol = agentDestCol + action.boxColDelta;
                    return this.cellIsFree(boxDestRow, boxDestCol) ? null : new int[]{boxDestRow, boxDestCol};
                }
                return new int[]{-1, -1};
            case Pull:
                // Check if there is a box to pull
                boxRow = this.row - action.boxRowDelta;
                boxCol = this.col - action.boxColDelta;
                box = boxes[boxRow][boxCol];
                if (box != 0) {
                    // Check if agent destination is free
                    agentDestRow = row + action.agentRowDelta;
                    agentDestCol = col + action.agentColDelta;
                    return this.cellIsFree(agentDestRow, agentDestCol) ? null : new int[]{agentDestRow,agentDestCol};
                }
                return  new int[]{-1, -1};
        }
        return new int[]{-1, -1};
    }

    private boolean cellIsFree(int row, int col) {
        return !this.walls[row][col] && this.boxes[row][col] == 0;
    }

    public int g() {
        return this.g;
    }

    public static char[][] clone(char[][] matrix) {
        char[][] newMatrix = new char[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            newMatrix[i] = Arrays.copyOf(matrix[i], matrix[i].length);
        }
        return newMatrix;
    }

    public static boolean[][] clone(boolean[][] matrix) {
        boolean[][] newMatrix = new boolean[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            newMatrix[i] = Arrays.copyOf(matrix[i], matrix[i].length);
        }
        return newMatrix;
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
        for (int row = 0; row < this.walls.length; row++)
        {
            for (int col = 0; col < this.walls[row].length; col++)
            {

                if (this.boxes[row][col] > 0)
                {
                    s.append(this.boxes[row][col]);
                }
                else if (this.walls[row][col])
                {
                    s.append("+");
                }
                else if (this.row == row && this.col == col)
                {
                    s.append(this.agent);
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
