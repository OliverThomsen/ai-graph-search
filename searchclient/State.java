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
    public Map<Integer, ArrayList<Character>> agentBoxes;

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
        this.agentBoxes = parent.agentBoxes;
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
        for (Map.Entry<Integer,Action> entry : jointAction.entrySet()) {
            int agent = entry.getKey();
            Action action = entry.getValue();
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

        for (Map.Entry<Integer,Integer> entry : agentRows.entrySet()) {
            int agent = entry.getKey();

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

            for (Map.Entry<Integer,Integer> entry : agentRows.entrySet()) {
                int agent = entry.getKey();
                int permutation = actionsPermutation.get(agent);
                Action action = applicableActions.get(agent)[permutation];
                jointAction.put(agent, action);
            }

            if (this.conflictingAgents(jointAction).size() == 0)
            {
                expandedStates.add(new State(this, jointAction));
            }

            // Advance permutation
            boolean done = false;

            int highestAgentNumber = 0;
            for (Map.Entry<Integer,Integer> entry : agentRows.entrySet()) {
                highestAgentNumber = Math.max(entry.getKey(), highestAgentNumber);
            }


            for (Map.Entry<Integer,Integer> entry : agentRows.entrySet()) {
                int agent = entry.getKey();
                if (actionsPermutation.get(agent) < applicableActions.get(agent).length - 1)
                {
                    actionsPermutation.computeIfPresent(agent, (k,val) -> ++val);
                    break;
                }
                else
                {
                    actionsPermutation.put(agent, 0);
                    if (agent == highestAgentNumber)
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
                    return this.cellIsFree(destRowBox, destColBox) && sameColor && agentBoxes.get(agent).contains(box);
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
                    return this.cellIsFree(destRowAgent, destColAgent) && sameColor && agentBoxes.get(agent).contains(box);
                }
                return false;
        }

        // Unreachable:
        return false;
    }

    public Conflict conflictingAgent(int agent, Action action)
    {
        Map<Integer, Conflict> conflictMap = new HashMap<>();
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
                return null;

            case Move:
                destRowAgent = agentRow + action.agentRowDelta;
                destColAgent = agentCol + action.agentColDelta;
                if(!cellIsFree(destRowAgent, destColAgent)){
                    char unknown = boxes[destRowAgent][destColAgent];
                    if (SearchClient.isBox(unknown)){
                        return new Conflict(getBoxOwner(unknown),destRowAgent,destColAgent,true,unknown, action);

                    }
                    unknown = agentAt(destRowAgent,destColAgent);
                    if (unknown >= '0' && unknown <= '9'){
                        return new Conflict(unknown-'0',destRowAgent,destColAgent,true, unknown, action);
                    }
                    return null;
                }
                return null;

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
                    if (sameColor) {
                        if(!cellIsFree(destRowBox, destColBox)){
                            char unknown = boxes[destRowBox][destColBox];
                            if (SearchClient.isBox(unknown)){
                                return new Conflict(getBoxOwner(unknown),destRowBox,destColBox,true, unknown, action);

                            }
                            unknown = agentAt(destRowBox,destColBox);
                            if (unknown >= '0' && unknown <= '9'){
                                return new Conflict(unknown-'0',destRowBox,destColBox,true, unknown, action);
                            }
                            return null;
                        }
                    }
                }
                return null;

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
                    if (sameColor){
                        if(!cellIsFree(destRowAgent, destColAgent)){
                            char unknown = boxes[destRowAgent][destColAgent];
                            if (SearchClient.isBox(unknown)){
                                return new Conflict(getBoxOwner(unknown),destRowAgent,destColAgent,true, unknown, action);
                            }
                            unknown = agentAt(destRowAgent,destColAgent);
                            if (unknown >= '0' && unknown <= '9') {
                                return new Conflict(unknown-'0',destRowAgent,destColAgent,true, unknown, action);
                            }
                            return null;
                        }
                    }
                }
                return null;
        }

        // Unreachable:
        return null;
    }

    public Map<Integer, Conflict> allConflictingAgents(Map<Integer, Action> jointAction) {
        Map<Integer,Conflict> conflictingAgents = new HashMap<>();

        for (Map.Entry<Integer,Action> entry : jointAction.entrySet()) {
            int agent = entry.getKey();
            Action action = entry.getValue();
            Conflict conflict = conflictingAgent(agent, action);
            if (conflict != null) {
                conflictingAgents.put(agent,conflict);

            }
        }

        Map<Integer, Conflict> otherConflictingAgents = conflictingAgents(jointAction);

        conflictingAgents.putAll(otherConflictingAgents);

        return conflictingAgents;
    }

    public Map<Integer, Conflict> conflictingAgents(Map<Integer, Action> jointAction)
    {
        int highestAgentNumber = this.agentRows.size();
        for (Map.Entry<Integer,Integer> entry : agentRows.entrySet()) {
            highestAgentNumber = Math.max(entry.getKey(), highestAgentNumber);
        }

        Map<Integer,Integer> agentRows = new HashMap<>(highestAgentNumber); // row of new cell to become occupied by action
        Map<Integer,Integer> agentCols = new HashMap<>(highestAgentNumber); // column of new cell to become occupied by action
        Map<Integer,Integer> boxRows = new HashMap<>(highestAgentNumber); // current row of box moved by action
        Map<Integer,Integer> boxCols = new HashMap<>(highestAgentNumber); // current column of box moved by action

        // Collect cells to be occupied and boxes to be moved
        for (Map.Entry<Integer, Integer> entry : this.agentRows.entrySet()) {
            int agent = entry.getKey();
            Action action = jointAction.get(agent);
            int agentRow = this.agentRows.get(agent);
            int agentCol = this.agentCols.get(agent);

            switch (action.type)
            {
                case NoOp:
                    agentRows.put(agent, agentRow);
                    agentCols.put(agent, agentCol);
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
                    int prevBoxRow = agentRows.get(agent);
                    int prevBoxCol = agentCols.get(agent);
                    boxRows.put(agent, prevBoxRow + action.boxRowDelta);
                    boxCols.put(agent, prevBoxCol + action.boxColDelta);
                    break;

                case Pull:
                    agentRows.put(agent, agentRow + action.agentRowDelta);
                    agentCols.put(agent, agentCol + action.agentColDelta);
                    boxRows.put(agent, agentRow);
                    boxCols.put(agent, agentCol);
                    break;
            }
        }

        Map<Integer, Conflict> conflictingAgents = new HashMap<>();

        for (Map.Entry<Integer, Integer> entry : this.agentRows.entrySet()) {
            int a1 = entry.getKey();

            if (jointAction.get(a1) == Action.NoOp)
            {
                continue;
            }

            for (Map.Entry<Integer, Integer> entry2 : this.agentRows.entrySet()) {
                int a2 = entry2.getKey();
                if (entry2.getKey() == entry.getKey()){
                    continue;
                }

                if (jointAction.get(a2) == Action.NoOp)
                {
                    continue;
                }
                // agents moving into same cell
                if (agentRows.get(a1).equals(agentRows.get(a2)) && agentCols.get(a1).equals(agentCols.get(a2)))
                {
                    Conflict conflict = new Conflict(a2,agentRows.get(a2),agentCols.get(a2),false,(char) (a2+'0'), jointAction.get(a1));
                    conflictingAgents.put(a1,conflict);
                    Conflict conflict2 = new Conflict(a1,agentRows.get(a1),agentCols.get(a1),false,(char) (a1+'0'), jointAction.get(a2));
                    conflictingAgents.put(a2,conflict2);
                }

                // Boxes moving into same cell
                if (       (boxRows.get(a1).equals(boxRows.get(a2)) && boxRows.get(a1) != -1)
                        && (boxCols.get(a1).equals(boxCols.get(a2)) && boxCols.get(a1) != -1) ) {
                    Conflict conflict = new Conflict(a2,boxRows.get(a2),boxCols.get(a2),false,boxes[boxRows.get(a2)][boxCols.get(a2)], jointAction.get(a1));
                    conflictingAgents.put(a1,conflict);
                    Conflict conflict2 = new Conflict(a1,boxRows.get(a1),boxCols.get(a1),false,boxes[boxRows.get(a1)][boxCols.get(a1)], jointAction.get(a2));
                    conflictingAgents.put(a2,conflict2);
                }

                // Agent 1 and Box 2 moving into same cell
                if (agentRows.get(a1).equals(boxRows.get(a2)) && agentCols.get(a1).equals(boxCols.get(a2))) {
                    Conflict conflict = new Conflict(a2,boxRows.get(a2),boxCols.get(a2),false,boxes[boxRows.get(a2)][boxCols.get(a2)], jointAction.get(a1));
                    conflictingAgents.put(a1,conflict);
                    Conflict conflict2 = new Conflict(a1,agentRows.get(a1),agentCols.get(a1),false,(char) (a1+'0'), jointAction.get(a2));
                    conflictingAgents.put(a2,conflict2);
                }

                // Box 1 and Agent 2 moving into same cell
                if (boxRows.get(a1).equals(agentRows.get(a2)) && boxCols.get(a1).equals(agentCols.get(a2))) {
                    Conflict conflict = new Conflict(a1,boxRows.get(a1),boxCols.get(a1),false,boxes[boxRows.get(a1)][boxCols.get(a1)], jointAction.get(a1));
                    conflictingAgents.put(a2,conflict);
                    Conflict conflict2 = new Conflict(a2,agentRows.get(a2),agentCols.get(a2),false,(char) (a2+'0'), jointAction.get(a2));
                    conflictingAgents.put(a1,conflict2);
                }

            }
        }

//        for (int a1 = 0; a1 < highestAgentNumber; ++a1)
//        {
//            while (!agentRows.containsKey(a1)) a1++;
//
//            if (jointAction.get(a1) == Action.NoOp)
//            {
//                continue;
//            }
//
//            for (int a2 = a1 + 1; a2 < highestAgentNumber; ++a2)
//            {
//                while (!agentRows.containsKey(a2)) a2++;
//
//                if (jointAction.get(a2) == Action.NoOp)
//                {
//                    continue;
//                }
//
//                // Agents moving into same cell
//                if (agentRows.get(a1).equals(agentRows.get(a2)) && agentCols.get(a1).equals(agentCols.get(a2)))
//                {
//                    Conflict conflict = new Conflict(a2,agentRows.get(a2),agentCols.get(a2),false,(char) (a2+'0'));
//                    conflictingAgents.put(a1,conflict);
//                    Conflict conflict2 = new Conflict(a1,agentRows.get(a1),agentCols.get(a1),false,(char) (a1+'0'));
//                    conflictingAgents.put(a2,conflict2);
//                }
//
//                // Boxes moving into same cell
//                if (       (boxRows.get(a1).equals(boxRows.get(a2)) && boxRows.get(a1) != -1)
//                        && (boxCols.get(a1).equals(boxCols.get(a2)) && boxCols.get(a1) != -1) ) {
//                    Conflict conflict = new Conflict(a2,boxRows.get(a2),boxCols.get(a2),false,boxes[boxRows.get(a2)][boxCols.get(a2)]);
//                    conflictingAgents.put(a1,conflict);
//                    Conflict conflict2 = new Conflict(a1,boxRows.get(a1),boxCols.get(a1),false,boxes[boxRows.get(a1)][boxCols.get(a1)]);
//                    conflictingAgents.put(a2,conflict2);
//                }
//
//                // Agent 1 and Box 2 moving into same cell
//                if (agentRows.get(a1).equals(boxRows.get(a2)) && agentCols.get(a1).equals(boxCols.get(a2))) {
//                    Conflict conflict = new Conflict(a2,boxRows.get(a2),boxCols.get(a2),false,boxes[boxRows.get(a2)][boxCols.get(a2)]);
//                    conflictingAgents.put(a1,conflict);
//                    Conflict conflict2 = new Conflict(a1,agentRows.get(a1),agentCols.get(a1),false,(char) (a1+'0'));
//                    conflictingAgents.put(a2,conflict2);
//                }
//
//                // Box 1 and Agent 2 moving into same cell
//                if (boxRows.get(a1).equals(agentRows.get(a2)) && boxCols.get(a1).equals(agentCols.get(a2))) {
//                    Conflict conflict = new Conflict(a1,boxRows.get(a1),boxCols.get(a1),false,boxes[boxRows.get(a1)][boxCols.get(a1)]);
//                    conflictingAgents.put(a2,conflict);
//                    Conflict conflict2 = new Conflict(a2,agentRows.get(a2),agentCols.get(a2),false,(char) (a2+'0'));
//                    conflictingAgents.put(a1,conflict2);
//                }
//
//
//            }
//        }

        return conflictingAgents;
    }

    public int getBoxOwner(char box) {
        int owner = -1;
        Color agentColor = this.boxColors.get(box);

        for (Map.Entry<Integer,ArrayList<Character>> listEntry: agentBoxes.entrySet()) {
            if (listEntry.getValue().contains(box)){
                owner = listEntry.getKey();
            }
        }
        return owner;
    }

    private boolean cellIsFree(int row, int col)
    {
        return !this.walls[row][col] && this.boxes[row][col] == 0 && this.agentAt(row, col) == 0;
    }

    private char agentAt(int row, int col)
    {
        for (Map.Entry<Integer,Integer> entry : agentRows.entrySet()) {
            int i = entry.getKey();
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

    public void setAgentBoxes(Map<Integer, ArrayList<Character>> agentBoxes) {
        this.agentBoxes = agentBoxes;
    }
}
