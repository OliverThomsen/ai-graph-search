package searchclient;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class Heuristic
        implements Comparator<State>
{
    private Map<Character,Integer[]> goalCoordinates = new HashMap<>();

    public Heuristic(State initialState)
    {
        // Here's a chance to pre-process the static parts of the level.
        for (int row = 1; row < initialState.goals.length -1 ; row++) {
            for (int col = 1; col < initialState.goals[row].length -1 ; col++) {

                char goal = initialState.goals[row][col];

                if ('0' <= goal && goal <= '9' ) {
                    Integer[] coordinates = new Integer[2];
                    coordinates[0] = row;
                    coordinates[1] = col;
                    goalCoordinates.put(goal,coordinates);
                }

                if ( 'A' <= goal && goal <= 'Z')   {
                    Integer[] coordinates = new Integer[2];
                    coordinates[0] = row;
                    coordinates[1] = col;
                    goalCoordinates.put(goal,coordinates);
                }
            }
        }
    }

    public int h(State s)
    {
        int cost = 0;
        Map<Character,Integer[]> boxToGoalLength = new HashMap<>(); // Shortest Manhattan length to goal, One entry for each box type A,B,C...

        // Loop through all box matrix to find each box
        for (int row = 1; row < s.boxes.length - 1; row++) {
            for (int col = 1; col < s.boxes[row].length - 1; col++) {
                char box = s.boxes[row][col];

                // Check if field contains box
                if('A' <= box && box <= 'Z') {
                    // Get goal coordinates for the box
                    Integer[] goalcoor = goalCoordinates.get(box);
                    // Find box Manhattan length to goal
                    int rowdiff = Math.abs(row - goalcoor[0]);
                    int coldiff = Math.abs(col - goalcoor[1]);
                    int manhLength = rowdiff + coldiff;

                    // Save the box's manhattan length in the map
                    if (boxToGoalLength.containsKey(box)){
                        Integer[] boxvalues = boxToGoalLength.get(box);
                        int prevmanhlenght = boxvalues[2];
                        // overwrite length if smaller than previous length
                        if (prevmanhlenght > manhLength){
                            boxvalues[0] = row;
                            boxvalues[1] = col;
                            boxvalues[2] = manhLength;
                            boxToGoalLength.replace(box,boxvalues);
                        }
                    }
                    // Always Save the box's manhattan length in the map if first time
                    else {
                        Integer[] boxvalues = new Integer[3];
                        boxvalues[0] = row;
                        boxvalues[1] = col;
                        boxvalues[2] = manhLength;
                        boxToGoalLength.put(box,boxvalues);
                    }
                }
            }
        }

        // Total distance of boxes to goal
        Iterator it = boxToGoalLength.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            Integer[] values = (Integer[]) pair.getValue();
            cost += values[2];
            if ( values[2] == 0) {
                it.remove();
            }
        }

        // Loop over each agent
        for (int row = 0; row < s.agentRows.length ; row++) {
            char agent = Character.forDigit(row,10);
            // Agent distance to own goal
            if (goalCoordinates.containsKey(agent))
            {
                Integer[] goalcoor = goalCoordinates.get(agent);
                int rowdiff = Math.abs(s.agentRows[row] - goalcoor[0]);
                int coldiff = Math.abs(s.agentCols[row] - goalcoor[1]);
                cost += rowdiff + coldiff;
            }

            // Agent distance to the box closest to goal
            int boxCost = 0;
            Iterator it2 = boxToGoalLength.entrySet().iterator();
            while (it2.hasNext()){
                Map.Entry pair = (Map.Entry) it2.next();
                Integer[] values = (Integer[]) pair.getValue();
                // Check if box and agent have same color
                Color agentColor = s.agentColors[agent - '0'];
                Color boxColor = s.boxColors[(char)pair.getKey() - 'A'];
                if (agentColor == boxColor) {
                    int rowdiff = Math.abs(s.agentRows[row] - values[0]);
                    int coldiff = Math.abs(s.agentCols[row] - values[1]);
                    // Divide by 2 to give lower weight compared to the box's distance to its goal
                    boxCost += rowdiff + coldiff;
                }
            }
            cost += boxCost;
        }


//        /// goal count heuristic
//        for (int row = 1; row < s.goals.length - 1; row++) {
//            for (int col = 1; col < s.goals[row].length - 1; col++) {
//
//                char goal = s.goals[row][col];
//
//                if ('0' <= goal && goal <= '9' ) {
//                    cost++;
//
//                    if(s.agentRows[goal - '0'] == row && s.agentCols[goal - '0'] == col){
//                        cost--;
//                    }
//                }
//
//                if ( 'A' <= goal && goal <= 'Z')   {
//                    cost++;
//
//                    if (s.boxes[row][col] == goal ) {
//                        cost--;
//                    }
//                }
//
//            }
//        }
        return cost;
    }

    public abstract int f(State s);

    @Override
    public int compare(State s1, State s2)
    {
        return this.f(s1) - this.f(s2);
    }
}

class HeuristicAStar
        extends Heuristic
{
    public HeuristicAStar(State initialState)
    {
        super(initialState);
    }

    @Override
    public int f(State s)
    {
        return s.g() + this.h(s);
    }

    @Override
    public String toString()
    {
        return "A* evaluation";
    }
}

class HeuristicWeightedAStar
        extends Heuristic
{
    private int w;

    public HeuristicWeightedAStar(State initialState, int w)
    {
        super(initialState);
        this.w = w;
    }

    @Override
    public int f(State s)
    {
        return s.g() + this.w * this.h(s);
    }

    @Override
    public String toString()
    {
        return String.format("WA*(%d) evaluation", this.w);
    }
}

class HeuristicGreedy
        extends Heuristic
{
    public HeuristicGreedy(State initialState)
    {
        super(initialState);
    }

    @Override
    public int f(State s)
    {
        return this.h(s);
    }

    @Override
    public String toString()
    {
        return "greedy evaluation";
    }
}
