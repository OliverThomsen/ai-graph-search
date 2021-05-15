package searchclient;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class Heuristic implements Comparator<SuperState> {
    private CostCalculator calculator;
    private SubGoal[] subGoals;

    public Heuristic(Integer[][] referenceMap, SubGoal[] subGoals)
    {

        this.subGoals = subGoals;
        // Here's a chance to pre-process the static parts of the level.
        this.calculator = new CostCalculator(referenceMap);
    }

    public int h(SuperState s) {
        int cost = 0;
        for (SubGoal subgoal: subGoals) {


            switch (subgoal.type) {
                case GET_TO_BOX:
                    cost += calculator.GetToBox(s.agentRows[0], s.agentCols[0], subgoal.goalrow, subgoal.goalcol);

                case PUSH_BOX_TO_GOAL:
                    cost += calculator.PushBoxToGoal(s.boxes, s.agentRows[0], s.agentCols[0], subgoal.goalrow, subgoal.goalcol, subgoal.box);

                case GET_TO_COORDINATE:
                    cost += calculator.GetToCoordinate(s.agentRows[0], s.agentCols[0], subgoal.goalrow, subgoal.goalcol);

                case MOVE_BOX_TO_HELP:
                    cost += calculator.MoveBoxToHelp(s.agentRows[0], s.agentCols[0], subgoal.goalrow, subgoal.goalcol);

                default:
                    cost += Integer.MAX_VALUE;


            }
        }
        return cost;

    }

    public abstract int f(SuperState superState);



    @Override
    public int compare(SuperState s1, SuperState s2)
    {
        return this.f(s1) - this.f(s2);
    }
}


class HeuristicGreedy
        extends Heuristic
{
    public HeuristicGreedy(SuperState initialState, Integer[][] referenceMap, SubGoal[] subGoals)
    {
        super(referenceMap, subGoals);
    }

    @Override
    public int f(SuperState s) {
        return this.h(s);
    }

    @Override
    public String toString() {
        return "greedy evaluation";
    }


}
class SubGoal{
    int goalrow;
    int goalcol;
    SubGoalType type;
    char box;


}
