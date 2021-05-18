package searchclient;

import java.util.*;

public class State implements SuperState
{
    private static final Random RNG = new Random(1);

    /*
        The agent rows, columns, and colors are indexed by the agent number.
        For example, this.agentRows[0] is the row location of agent '0'.
    */
    public Map<Integer,Integer> agentRows;
    public Map<Integer,Integer> agentCols;
    public Map<Integer,Color> agentColors;

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
    public Map<Character,Color> boxColors;
 
    public final State parent;
    public final Map<Integer, Action> jointAction;
    private final int g;

    private int hash = 0;


    // Constructs copy of state
    public State(State state) {
        this.boxColors = new HashMap<>(state.boxColors);
        this.agentColors = new HashMap<>(state.agentColors);
        this.agentRows = new HashMap<>(state.agentRows);
        this.agentCols = new HashMap<>(state.agentCols);
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
    public State(Map<Integer,Integer> agentRows, Map<Integer,Integer> agentCols, Map<Integer,Color> agentColors, boolean[][] walls,
                 char[][] boxes, Map<Character,Color> boxColors, char[][] goals
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
    public State(State parent, Map<Integer, Action> jointAction)
    {
        // Copy parent
        this.boxColors = new HashMap<>(parent.boxColors);
        this.agentColors = new HashMap<>(parent.agentColors);
        this.agentRows = new HashMap<>(parent.agentRows);
        this.agentCols = new HashMap<>(parent.agentCols);
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
        this.jointAction = new HashMap<>(jointAction);
        this.g = parent.g + 1;

        // Apply each action
        int numAgents = this.agentRows.size();
        for (int agent = 0; agent < numAgents; ++agent)
        {
            while (!jointAction.containsKey(agent)) agent++;

            Action action = jointAction.get(agent);
            char box;

            switch (action.type)
            {
                case NoOp:
                    break;

                case Move:
                    this.agentRows.computeIfPresent(agent, (k,val) -> val += action.agentRowDelta);
                    this.agentCols.computeIfPresent(agent, (k,val) -> val += action.agentColDelta);
                    break;

                case Push:
                    this.agentRows.computeIfPresent(agent, (k,val) -> val += action.agentRowDelta);
                    this.agentCols.computeIfPresent(agent, (k,val) -> val += action.agentColDelta);
                    int prevBoxRow = this.agentRows.get(agent);
                    int prevBoxCol = this.agentCols.get(agent);
                    int destBoxRow = this.agentRows.get(agent) + action.boxRowDelta;
                    int destBoxCol = this.agentCols.get(agent) + action.boxColDelta;
                    box = this.boxes[prevBoxRow][prevBoxCol];
                    this.boxes[prevBoxRow][prevBoxCol] = 0;
                    this.boxes[destBoxRow][destBoxCol] = box;
                    break;

                case Pull:
                    prevBoxRow = this.agentRows.get(agent) - action.boxRowDelta;
                    prevBoxCol = this.agentCols.get(agent) - action.boxColDelta;
                    destBoxRow = this.agentRows.get(agent);
                    destBoxCol = this.agentCols.get(agent);
                    this.agentRows.computeIfPresent(agent,(k,val) -> val += action.agentRowDelta);
                    this.agentCols.computeIfPresent(agent,(k,val) -> val += action.agentColDelta);
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
                         !(this.agentRows.get(goal - '0') == row && this.agentCols.get(goal - '0') == col))
                {
                    return false;
                }
            }
        }
        return true;
    }

    public ArrayList<SuperState> getExpandedStates()
    {
        int numAgents = this.agentRows.size();

        // Determine list of applicable actions for each individual agent.
        Map<Integer,Action[]> applicableActions = new HashMap<>(numAgents);
        for (int agent = 0; agent < numAgents; ++agent)
        {
            while (!agentRows.containsKey(agent)) agent++;
            ArrayList<Action> agentActions = new ArrayList<>(Action.values().length);
            for (Action action : Action.values())
            {
                if (this.isApplicable(agent, action))
                {
                    agentActions.add(action);
                }
            }
            applicableActions.put(agent, agentActions.toArray(new Action[0]));
        }

        // Iterate over joint actions, check conflict and generate child states.
        Map<Integer,Action> jointAction = new HashMap<>(numAgents);
        Map<Integer,Integer> actionsPermutation = new HashMap<>(numAgents);
        for (Map.Entry<Integer,Integer> entry : agentRows.entrySet()) {
            actionsPermutation.put(entry.getKey(),0);
        }
        ArrayList<SuperState> expandedStates = new ArrayList<>(16);
        while (true)
        {
            for (int agent = 0; agent < numAgents; ++agent) {
                while (!agentRows.containsKey(agent)) agent++;
                jointAction.put(agent, applicableActions.get(agent)[actionsPermutation.get(agent)]);
            }

            if (this.conflictingAgents(jointAction).length == 0)
            {
                expandedStates.add(new State(this, jointAction));
            }

            // Advance permutation
            boolean done = false;
            for (int agent = 0; agent < numAgents; ++agent)
            {
                while (!agentRows.containsKey(agent)) agent++;
                if (actionsPermutation.get(agent) < applicableActions.get(agent).length - 1)
                {
                    actionsPermutation.computeIfPresent(agent, (k,val) -> ++val);
                    break;
                }
                else
                {
                    actionsPermutation.put(agent, 0);
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

    public boolean isApplicable(int agent, Action action)
    {
        int agentRow = this.agentRows.get(agent);
        int agentCol = this.agentCols.get(agent);
        Color agentColor = this.agentColors.get(agent);
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
                    boolean sameColor = this.boxColors.get(box) == agentColor;
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
                    boolean sameColor = this.boxColors.get(box) == agentColor;
                    destRowAgent = agentRow + action.agentRowDelta;
                    destColAgent = agentCol + action.agentColDelta;
                    return this.cellIsFree(destRowAgent, destColAgent) && sameColor;
                }
                return false;
        }

        // Unreachable:
        return false;
    }

    public int[] conflictingAgents(Map<Integer, Action> jointAction)
    {
        int numAgents = this.agentRows.size();

        Map<Integer,Integer> agentRows = new HashMap<>(numAgents); // row of new cell to become occupied by action
        Map<Integer,Integer> agentCols = new HashMap<>(numAgents); // column of new cell to become occupied by action
        Map<Integer,Integer> boxRows = new HashMap<>(numAgents); // current row of box moved by action
        Map<Integer,Integer> boxCols = new HashMap<>(numAgents); // current column of box moved by action
        char[][] map = AgentState.clone(this.boxes);

        // Collect cells to be occupied and boxes to be moved
        for (Map.Entry<Integer, Integer> entry : this.agentRows.entrySet()) {
            int agent = entry.getKey();
            Action action = jointAction.get(agent);
            int agentRow = this.agentRows.get(agent);
            int agentCol = this.agentCols.get(agent);

            switch (action.type)
            {
                case NoOp:
                    // add agent to static map
                    map[agentRow][agentCol] = (char) (agent +'0');
                    break;

                case Move:
                    agentRows.put(agent, agentRow + action.agentRowDelta);
                    agentCols.put(agent, agentCol + action.agentColDelta);
                    boxRows.put(agent, -1); // Distinct dummy value
                    boxCols.put(agent, -1); // Distinct dummy value
                    break;

                case Push:
                    agentRows.put(agent, agentRow + action.agentRowDelta);
                    agentCols.put(agent, agentCol + action.agentColDelta);
                    boxRows.put(agent, agentRows.get(agent) + action.boxRowDelta);
                    boxCols.put(agent, agentCols.get(agent) + action.boxColDelta);
                    // remove box from static map
                    map[agentRows.get(agent)][agentCols.get(agent)] = 0;
                    break;

                case Pull:
                    agentRows.put(agent, agentRow + action.agentRowDelta);
                    agentCols.put(agent, agentCol + action.agentColDelta);
                    boxRows.put(agent, agentRow);
                    boxCols.put(agent, agentCol);
                    int oldBoxRow = agentRow - action.boxRowDelta;
                    int oldBoxCol = agentCol - action.boxColDelta;
                    // remove box from static map
                    map[oldBoxRow][oldBoxCol] = 0;
                    break;
            }
        }

        for (int a1 = 0; a1 < numAgents; ++a1)
        {
            while (!agentRows.containsKey(a1)) a1++;

            if (jointAction.get(a1) == Action.NoOp)
            {
                continue;
            }

            // Agent moving into stationary box or agent
//            System.err.println(jointAction.get(a1));
//            System.err.println(this);

            System.err.println();
            if (map[agentRows.get(a1)][agentCols.get(a1)] != 0) {
                char c = map[agentRows.get(a1)][agentCols.get(a1)];
                if (SearchClient.isBox(c)) {
                    Color agentColor = this.boxColors.get(c);
                    for (Map.Entry<Integer,Color> entry : this.agentColors.entrySet()) {
                        int agent = entry.getKey();
                        Color color = entry.getValue();
                        if (agentColor.equals(color)) {
                            return new int[]{a1,agent};
                        }
                    }

                } else {
                    return new int[] {a1, c-'0'};
                }

                return new int[] {a1, };
            }

            for (int a2 = a1 + 1; a2 < numAgents; ++a2)
            {
                while (!agentRows.containsKey(a2)) a2++;

                if (jointAction.get(a2) == Action.NoOp)
                {
                    continue;
                }

                // Agents moving into same cell?
                if (agentRows.get(a1).equals(agentRows.get(a2)) && agentCols.get(a1).equals(agentCols.get(a2)))
                {
                    return new int[] {a1,a2};
                }

                // Boxes moving into same cell
                if ( (boxRows.get(a1).equals(boxRows.get(a2)) && boxRows.get(a1) != -1)  && (boxCols.get(a1).equals(boxCols.get(a2)) && boxCols.get(a1) != -1) ) {
                    return new int[] {a1,a2};
                }

                // Agent 1 and Box 2 moving into same cell
                if (agentRows.get(a1).equals(boxRows.get(a2)) && agentCols.get(a1).equals(boxCols.get(a2))) {
                    return new int[] {a1,a2};
                }

                // Box 1 and Agent 2 moving into same cell
                if (boxRows.get(a1).equals(agentRows.get(a2)) && boxCols.get(a1).equals(agentCols.get(a2))) {
                    return new int[] {a1,a2};
                }


            }
        }

        return new int[0];
    }

    private boolean cellIsFree(int row, int col)
    {
        return !this.walls[row][col] && this.boxes[row][col] == 0 && this.agentAt(row, col) == 0;
    }

    private char agentAt(int row, int col)
    {
        for (int i = 0; i < this.agentRows.size(); i++)
        {
            while (!agentRows.containsKey(i)) i++;
            if (this.agentRows.get(i) == row && this.agentCols.get(i) == col)
            {
                return (char) ('0' + i);
            }
        }
        return 0;
    }

    public Map<Integer, Action>[] extractPlan()
    {
        @SuppressWarnings("unchecked")
        Map<Integer, Action>[] plan = new HashMap[this.g];
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
            result = prime * result + this.agentColors.hashCode();
            result = prime * result + this.boxColors.hashCode();
            result = prime * result + Arrays.deepHashCode(this.walls);
            result = prime * result + Arrays.deepHashCode(this.goals);
            result = prime * result + this.agentRows.hashCode();
            result = prime * result + this.agentCols.hashCode();
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
        return this.agentRows.equals(other.agentRows) &&
               this.agentCols.equals(other.agentCols) &&
               this.agentColors.equals(other.agentColors) &&
               Arrays.deepEquals(this.walls, other.walls) &&
               Arrays.deepEquals(this.boxes, other.boxes) &&
               this.boxColors.equals(other.boxColors) &&
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
