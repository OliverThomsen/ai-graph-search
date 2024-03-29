package searchclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class State
{
    private static final Random RNG = new Random(1);

    /*
        The agent rows, columns, and colors are indexed by the agent number.
        For example, this.agentRows[0] is the row location of agent '0'.
    */
    public int[] agentRows;
    public int[] agentCols;
    public Color[] agentColors;

    /*
        The walls, boxes, and goals arrays are indexed from the top-left of the level, row-major order (row, col).
               Col 0  Col 1  Col 2  Col 3
        Row 0: (0,0)  (0,1)  (0,2)  (0,3)  ...
        Row 1: (1,0)  (1,1)  (1,2)  (1,3)  ...
        Row 2: (2,0)  (2,1)  (2,2)  (2,3)  ...
        ...

        For example, this.walls[2] is an array of booleans for the third row.
        this.walls[row][col] is true if there's a wall at (row, col).
    */
    public boolean[][] walls;
    public char[][] boxes;
    public char[][] goals;

    /*
        The box colors are indexed alphabetically. So this.boxColors[0] is the color of A boxes, 
        this.boxColor[1] is the color of B boxes, etc.
    */
    public Color[] boxColors;
 
    public final State parent;
    public final Action[] jointAction;
    private final int g;

    private int hash = 0;


    // Constructs copy of state
    public State(State state) {
        this.boxColors = Arrays.copyOf(state.boxColors, state.boxColors.length);
        this.agentColors = Arrays.copyOf(state.agentColors, state.agentColors.length);
        this.agentRows = Arrays.copyOf(state.agentRows, state.agentRows.length);
        this.agentCols = Arrays.copyOf(state.agentCols, state.agentCols.length);
        this.boxes = new char[state.boxes.length][];
        this.walls = new boolean[state.walls.length][];
        this.goals = new char[state.goals.length][];
        for (int i = 0; i < state.boxes.length; i++)
        {
            this.boxes[i] = Arrays.copyOf(state.boxes[i], state.boxes[i].length);
            this.walls[i] = Arrays.copyOf(state.walls[i], state.walls[i].length);
            this.goals[i] = Arrays.copyOf(state.goals[i], state.goals[i].length);
        }


        this.parent = null;
        this.jointAction = null;
        this.g = 0;
    }

    // Constructs an initial state.
    // Arguments are not copied, and therefore should not be modified after being passed in.
    public State(int[] agentRows, int[] agentCols, Color[] agentColors, boolean[][] walls,
                 char[][] boxes, Color[] boxColors, char[][] goals
    )
    {
        this.boxColors = boxColors;
        this.agentColors = agentColors;
        this.agentRows = agentRows;
        this.agentCols = agentCols;
        this.walls = walls;
        this.boxes = boxes;
        this.goals = goals;
        this.parent = null;
        this.jointAction = null;
        this.g = 0;
    }


    // Constructs the state resulting from applying jointAction in parent.
    // Precondition: Joint action must be applicable and non-conflicting in parent state.
    private State(State parent, Action[] jointAction)
    {
        // Copy parent
        this.boxColors = Arrays.copyOf(parent.boxColors, parent.boxColors.length);
        this.agentColors = Arrays.copyOf(parent.agentColors, parent.agentColors.length);
        this.agentRows = Arrays.copyOf(parent.agentRows, parent.agentRows.length);
        this.agentCols = Arrays.copyOf(parent.agentCols, parent.agentCols.length);
        this.boxes = new char[parent.boxes.length][];
        this.walls = new boolean[parent.walls.length][];
        this.goals = new char[parent.goals.length][];
        for (int i = 0; i < parent.boxes.length; i++)
        {
            this.boxes[i] = Arrays.copyOf(parent.boxes[i], parent.boxes[i].length);
            this.walls[i] = Arrays.copyOf(parent.walls[i], parent.walls[i].length);
            this.goals[i] = Arrays.copyOf(parent.goals[i], parent.goals[i].length);
        }

        // Set own parameters
        this.parent = parent;
        this.jointAction = Arrays.copyOf(jointAction, jointAction.length);
        this.g = parent.g + 1;

        // Apply each action
        int numAgents = this.agentRows.length;
        for (int agent = 0; agent < numAgents; ++agent)
        {
            Action action = jointAction[agent];
            char box;

            switch (action.type)
            {
                case NoOp:
                    break;

                case Move:
                    this.agentRows[agent] += action.agentRowDelta;
                    this.agentCols[agent] += action.agentColDelta;
                    break;

                case Push:
                    this.agentRows[agent] += action.agentRowDelta;
                    this.agentCols[agent] += action.agentColDelta;
                    int prevBoxRow = this.agentRows[agent];
                    int prevBoxCol = this.agentCols[agent];
                    int destBoxRow = this.agentRows[agent] + action.boxRowDelta;
                    int destBoxCol = this.agentCols[agent] + action.boxColDelta;
                    box = this.boxes[prevBoxRow][prevBoxCol];
                    this.boxes[prevBoxRow][prevBoxCol] = 0;
                    this.boxes[destBoxRow][destBoxCol] = box;
                    break;

                case Pull:
                    prevBoxRow = this.agentRows[agent] - action.boxRowDelta;
                    prevBoxCol = this.agentCols[agent] - action.boxColDelta;
                    destBoxRow = this.agentRows[agent];
                    destBoxCol = this.agentCols[agent];
                    this.agentRows[agent] += action.agentRowDelta;
                    this.agentCols[agent] += action.agentColDelta;
                    box = this.boxes[prevBoxRow][prevBoxCol];
                    this.boxes[prevBoxRow][prevBoxCol] = 0;
                    this.boxes[destBoxRow][destBoxCol] = box;
            }
        }
    }

    public int g()
    {
        return this.g;
    }

    public boolean isGoalState()
    {
        for (int row = 1; row < this.goals.length - 1; row++)
        {
            for (int col = 1; col < this.goals[row].length - 1; col++)
            {
                char goal = this.goals[row][col];

                if ('A' <= goal && goal <= 'Z' && this.boxes[row][col] != goal)
                {
                    return false;
                }
                else if ('0' <= goal && goal <= '9' &&
                         !(this.agentRows[goal - '0'] == row && this.agentCols[goal - '0'] == col))
                {
                    return false;
                }
            }
        }
        return true;
    }

    public ArrayList<State> getExpandedStates()
    {
        int numAgents = this.agentRows.length;

        // Determine list of applicable actions for each individual agent.
        Action[][] applicableActions = new Action[numAgents][];
        for (int agent = 0; agent < numAgents; ++agent)
        {
            ArrayList<Action> agentActions = new ArrayList<>(Action.values().length);
            for (Action action : Action.values())
            {
                if (this.isApplicable(agent, action))
                {
                    agentActions.add(action);
                }
            }
            applicableActions[agent] = agentActions.toArray(new Action[0]);
        }

        // Iterate over joint actions, check conflict and generate child states.
        Action[] jointAction = new Action[numAgents];
        int[] actionsPermutation = new int[numAgents];
        ArrayList<State> expandedStates = new ArrayList<>(16);
        while (true)
        {
            for (int agent = 0; agent < numAgents; ++agent)
            {
                jointAction[agent] = applicableActions[agent][actionsPermutation[agent]];
            }

            if (!this.isConflicting(jointAction))
            {
                expandedStates.add(new State(this, jointAction));
            }

            // Advance permutation
            boolean done = false;
            for (int agent = 0; agent < numAgents; ++agent)
            {
                if (actionsPermutation[agent] < applicableActions[agent].length - 1)
                {
                    ++actionsPermutation[agent];
                    break;
                }
                else
                {
                    actionsPermutation[agent] = 0;
                    if (agent == numAgents - 1)
                    {
                        done = true;
                    }
                }
            }

            // Last permutation?
            if (done)
            {
                break;
            }
        }

        Collections.shuffle(expandedStates, State.RNG);
        return expandedStates;
    }

    private boolean isApplicable(int agent, Action action)
    {
        int agentRow = this.agentRows[agent];
        int agentCol = this.agentCols[agent];
        Color agentColor = this.agentColors[agent];
        int boxRow;
        int boxCol;
        char box;
        int destRowAgent;
        int destColAgent;
        int destRowBox;
        int destColBox;
        switch (action.type)
        {
            case NoOp:
                return true;

            case Move:
                destRowAgent = agentRow + action.agentRowDelta;
                destColAgent = agentCol + action.agentColDelta;
                return this.cellIsFree(destRowAgent, destColAgent);

            case Push:
                destRowAgent = agentRow + action.agentRowDelta;
                destColAgent = agentCol + action.agentColDelta;
                // check if there is a box in the agent destination
                box = boxes[destRowAgent][destColAgent];
                if (box != 0) {
                    // check if the box destination is free and box has same color as agent
                    boolean sameColor = this.boxColors[box - 'A'] == agentColor;
                    destRowBox = destRowAgent + action.boxRowDelta;
                    destColBox = destColAgent + action.boxColDelta;
                    return this.cellIsFree(destRowBox, destColBox) && sameColor;
                }
                return false;

            case Pull:
                // Check if there is a box to pull
                boxRow = agentRow - action.boxRowDelta;
                boxCol = agentCol - action.boxColDelta;
                box = boxes[boxRow][boxCol];
                if (box != 0) {
                    // Check if agent destination is free and agent has same color as box
                    boolean sameColor = this.boxColors[box - 'A'] == agentColor;
                    destRowAgent = agentRow + action.agentRowDelta;
                    destColAgent = agentCol + action.agentColDelta;
                    return this.cellIsFree(destRowAgent, destColAgent) && sameColor;
                }
                return false;
        }

        // Unreachable:
        return false;
    }

    private boolean isConflicting(Action[] jointAction)
    {
        int numAgents = this.agentRows.length;

        int[] agentRows = new int[numAgents]; // row of new cell to become occupied by action
        int[] agentCols = new int[numAgents]; // column of new cell to become occupied by action
        int[] boxRows = new int[numAgents]; // current row of box moved by action
        int[] boxCols = new int[numAgents]; // current column of box moved by action

        // Collect cells to be occupied and boxes to be moved
        for (int agent = 0; agent < numAgents; ++agent)
        {
            Action action = jointAction[agent];
            int agentRow = this.agentRows[agent];
            int agentCol = this.agentCols[agent];

            switch (action.type)
            {
                case NoOp:
                    break;

                case Move:
                    agentRows[agent] = agentRow + action.agentRowDelta;
                    agentCols[agent] = agentCol + action.agentColDelta;
                    boxRows[agent] = -1; // Distinct dummy value
                    boxCols[agent] = -1; // Distinct dummy value
                    break;

                case Push:
                    agentRows[agent] = agentRow + action.agentRowDelta;
                    agentCols[agent] = agentCol + action.agentColDelta;
                    boxRows[agent] = agentRows[agent] + action.boxRowDelta;
                    boxCols[agent] = agentCols[agent] + action.boxColDelta;
                    break;

                case Pull:
                    agentRows[agent] = agentRow + action.agentRowDelta;
                    agentCols[agent] = agentCol + action.agentColDelta;
                    boxRows[agent] = agentRow - action.boxRowDelta;
                    boxCols[agent] = agentCol - action.boxColDelta;
                    break;
           }
        }

        for (int a1 = 0; a1 < numAgents; ++a1)
        {
            if (jointAction[a1] == Action.NoOp)
            {
                continue;
            }

            for (int a2 = a1 + 1; a2 < numAgents; ++a2)
            {
                if (jointAction[a2] == Action.NoOp)
                {
                    continue;
                }

                // Agents moving into same cell?
                if (agentRows[a1] == agentRows[a2] && agentCols[a1] == agentCols[a2])
                {
                    return true;
                }

                // Boxes moving into same cell
                if ( (boxRows[a1] == boxRows[a2] && boxRows[a1] != -1)  && (boxCols[a1] == boxCols[a2] && boxCols[a1] != -1) ) {
                    return true;
                }

                // Agent moves into box
                if (agentRows[a1] == boxRows[a2] && agentCols[a1] == boxCols[a2]) {
                    return true;
                }

                // Box moves into agent
                if (boxRows[a1] == agentRows[a2] && boxCols[a1] == agentCols[a2]) {
                    return true;
                }


            }
        }

        return false;
    }

    private boolean cellIsFree(int row, int col)
    {
        return !this.walls[row][col] && this.boxes[row][col] == 0 && this.agentAt(row, col) == 0;
    }

    private char agentAt(int row, int col)
    {
        for (int i = 0; i < this.agentRows.length; i++)
        {
            if (this.agentRows[i] == row && this.agentCols[i] == col)
            {
                return (char) ('0' + i);
            }
        }
        return 0;
    }

    public Action[][] extractPlan()
    {
        Action[][] plan = new Action[this.g][];
        State state = this;
        while (state.jointAction != null)
        {
            plan[state.g - 1] = state.jointAction;
            state = state.parent;
        }
        return plan;
    }

    @Override
    public int hashCode()
    {
        if (this.hash == 0)
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(this.agentColors);
            result = prime * result + Arrays.hashCode(this.boxColors);
            result = prime * result + Arrays.deepHashCode(this.walls);
            result = prime * result + Arrays.deepHashCode(this.goals);
            result = prime * result + Arrays.hashCode(this.agentRows);
            result = prime * result + Arrays.hashCode(this.agentCols);
            for (int row = 0; row < this.boxes.length; ++row)
            {
                for (int col = 0; col < this.boxes[row].length; ++col)
                {
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
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (this.getClass() != obj.getClass())
        {
            return false;
        }
        State other = (State) obj;
        return Arrays.equals(this.agentRows, other.agentRows) &&
               Arrays.equals(this.agentCols, other.agentCols) &&
               Arrays.equals(this.agentColors, other.agentColors) &&
               Arrays.deepEquals(this.walls, other.walls) &&
               Arrays.deepEquals(this.boxes, other.boxes) &&
               Arrays.equals(this.boxColors, other.boxColors) &&
               Arrays.deepEquals(this.goals, other.goals);
    }

    @Override
    public String toString()
    {
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
                else if (this.agentAt(row, col) != 0)
                {
                    s.append(this.agentAt(row, col));
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
