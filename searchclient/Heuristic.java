package searchclient;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class Heuristic
        implements Comparator<State>
{
    private Map<Character,Integer[]> goalcoordinates = new HashMap<>();
    private Map<Character,Integer[]> boxgoallenght = new HashMap<>();




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
                    goalcoordinates.put(goal,coordinates);



                }

                if ( 'A' <= goal && goal <= 'Z')   {
                    Integer[] coordinates = new Integer[2];
                    coordinates[0] = row;
                    coordinates[1] = col;
                    goalcoordinates.put(goal,coordinates);

                }


            }
        }



        }







    public int h(State s)
    {

        int cost = 0;

        boxgoallenght = new HashMap<>();

        for (int row = 1; row < s.goals.length - 1; row++) {
            for (int col = 1; col < s.goals[row].length - 1; col++) {
                char box = s.boxes[row][col];

                if(goalcoordinates.containsKey(box))
                {

                    Integer[] goalcoor = goalcoordinates.get(box);
                    int rowdiff = Math.abs(row - goalcoor[0]);
                    int coldiff = Math.abs(col - goalcoor[1]);
                    int manhlenght = rowdiff + coldiff;

                    if (boxgoallenght.containsKey(box)){
                        Integer[] boxvalues = boxgoallenght.get(box);
                        int prevmanhlenght = boxvalues[2];
                        if (prevmanhlenght > manhlenght){
                            boxvalues[0] = row;
                            boxvalues[1] = col;
                            boxvalues[2] = manhlenght;
                            boxgoallenght.replace(box,boxvalues);

                        }
                    }
                    else {
                        Integer[] boxvalues = new Integer[3];
                        boxvalues[0] = row;
                        boxvalues[1] = col;
                        boxvalues[2] = manhlenght;
                        boxgoallenght.put(box,boxvalues);

                    }

                    }
                }

            }

        // Total distance of each box of a type which is closets to goal
        Iterator it = boxgoallenght.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            Integer[] values = (Integer[]) pair.getValue();
            cost += values[2];
            if ( values[2] == 0) {
                it.remove();
            }
        }

        for (int row = 0; row < s.agentRows.length ; row++) {
            char agent = Character.forDigit(row,10);

            // Agent distance to own goal
            if (goalcoordinates.containsKey(agent))
            {
                Integer[] goalcoor = goalcoordinates.get(agent);
                int rowdiff = Math.abs(s.agentRows[row] - goalcoor[0]);
                int coldiff = Math.abs(s.agentCols[row] - goalcoor[1]);
                cost += rowdiff + coldiff;
            }

            // Agent distance to box of type closest to goal
            Iterator it2 = boxgoallenght.entrySet().iterator();
            while (it2.hasNext()){
                Map.Entry pair = (Map.Entry) it2.next();
                Integer[] values = (Integer[]) pair.getValue();
                int rowdiff = Math.abs(s.agentRows[row] - values[0]);
                int coldiff = Math.abs(s.agentCols[row] - values[1]);
                cost += rowdiff + coldiff;
            }


        }




        /// goal count heuristic
        /*
        int goalCount = 0;

        for (int row = 1; row < s.goals.length - 1; row++) {
            for (int col = 1; col < s.goals[row].length - 1; col++) {

                char goal = s.goals[row][col];

                if ('0' <= goal && goal <= '9' ) {
                    goalCount++;

                    if(s.agentRows[goal - '0'] == row && s.agentCols[goal - '0'] == col){
                        goalCount--;
                    }
                }

                if ( 'A' <= goal && goal <= 'Z')   {
                    goalCount++;

                    if (s.boxes[row][col] == goal ) {
                        goalCount--;
                    }
                }

            }
        }
        return goalCount;

        */
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
