package searchclient;

import java.util.Comparator;

public abstract class Heuristic implements Comparator<AgentState> {
    private CostCalculator calculator;
    private SubGoal subGoal;

    public Heuristic(Integer[][] referenceMap, SubGoal subGoal)
    {

        this.subGoal = subGoal;
        // Here's a chance to pre-process the static parts of the level.
        this.calculator = new CostCalculator(referenceMap);
    }

    public int h(AgentState s) {
        int cost = 0;

        switch (subGoal.type) {
            case GET_TO_BOX:
                cost += calculator.GetToBox(s.row, s.col, subGoal.row, subGoal.col);

            case PUSH_BOX_TO_GOAL:
                cost += calculator.PushBoxToGoal(s.boxes, s.row, s.col, subGoal.row, subGoal.col, subGoal.character);

            case GET_TO_COORDINATE:
                cost += calculator.GetToCoordinate(s.row, s.col, subGoal.row, subGoal.col);

            case MOVE_BOX_TO_HELP:
                cost += calculator.MoveBoxToHelp(s.row, s.col, subGoal.row, subGoal.col);

            default:
                cost += Integer.MAX_VALUE;
        }

        return cost;

    }

    public abstract int f(AgentState superState);



    @Override
    public int compare(AgentState s1, AgentState s2)
    {
        return this.f(s1) - this.f(s2);
    }
}


class HeuristicGreedy
        extends Heuristic
{
    public HeuristicGreedy(Integer[][] referenceMap, SubGoal subGoal)
    {
        super(referenceMap, subGoal);
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

