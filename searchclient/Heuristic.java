package searchclient;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class Heuristic implements Comparator<AgentState> {
    private Map<Character,Integer[]> goalCoordinates = new HashMap<>();

    public Heuristic(AgentState initialState) {
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

    public int h(AgentState s) {
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

        // Agent distance to own goal
        if (goalCoordinates.containsKey(s.agent))
        {
            Integer[] goalcoor = goalCoordinates.get(s.agent);
            int rowdiff = Math.abs(s.row - goalcoor[0]);
            int coldiff = Math.abs(s.col - goalcoor[1]);
            cost += rowdiff + coldiff;
        }

        // Agent distance to the box closest to goal
        Iterator it2 = boxToGoalLength.entrySet().iterator();
        while (it2.hasNext()){
            Map.Entry pair = (Map.Entry) it2.next();
            Integer[] values = (Integer[]) pair.getValue();
            int rowdiff = Math.abs(s.row - values[0]);
            int coldiff = Math.abs(s.col - values[1]);
            cost += rowdiff + coldiff;
        }

        return cost;
    }

    public abstract int f(AgentState s);

    @Override
    public int compare(AgentState s1, AgentState s2)
    {
        return this.f(s1) - this.f(s2);
    }
}

class HeuristicGreedy extends Heuristic {
    public HeuristicGreedy(AgentState initialState) {
        super(initialState);
    }

    @Override
    public int f(AgentState s) {
        return this.h(s);
    }

    @Override
    public String toString() {
        return "greedy evaluation";
    }
}
