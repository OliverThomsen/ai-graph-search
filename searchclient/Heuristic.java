package searchclient;

import java.awt.print.Pageable;
import java.util.Comparator;
import java.util.Map;

public abstract class Heuristic implements Comparator<SuperState> {
    private CostCalculator calculator;
    private Map<Integer, SubGoal> subGoals;

    public Heuristic(Integer[][] referenceMap, Map<Integer, SubGoal> subGoals)
    {

        this.subGoals = subGoals;
        // Here's a chance to pre-process the static parts of the level.
        this.calculator = new CostCalculator(referenceMap);
    }

    public int h(SuperState s) {
        int cost = 0;

        if(s instanceof AgentState) {
            AgentState state = (AgentState) s;
            SubGoal subGoal = this.subGoals.get(state.agent - '0');

            switch (subGoal.type) {
                case GET_TO_BOX:
                    cost += calculator.GetToBox(state.row, state.col, subGoal.row, subGoal.col);
                    break;
                case PUSH_BOX_TO_GOAL:
                    cost += calculator.PushBoxToGoal(state.boxes, state.row, state.col, subGoal.row, subGoal.col, subGoal.character);
                    break;
                case GET_TO_COORDINATE:
                    cost += calculator.GetToCoordinate(state.row, state.col, subGoal.row, subGoal.col);
                    break;
                case MOVE_BOX_TO_HELP:
                    cost += calculator.MoveBoxToHelp(state.row, state.col, subGoal.row, subGoal.col);
                    break;
                default:
                    cost += Integer.MAX_VALUE;
            }


        } else if(s instanceof State) {
            State state = (State) s;
            for (Map.Entry<Integer, Integer> entry : state.agentRows.entrySet()) {
                int a = entry.getKey();
                SubGoal subGoal = this.subGoals.get(a);

                switch (subGoal.type) {
                    case GET_TO_BOX:
                        cost += calculator.GetToBox(state.agentRows.get(a), state.agentCols.get(a), subGoal.row, subGoal.col);
                        break;
                    case PUSH_BOX_TO_GOAL:
                        cost += calculator.PushBoxToGoal(state.boxes, state.agentRows.get(a), state.agentCols.get(a), subGoal.row, subGoal.col, subGoal.character);
                        break;
                    case GET_TO_COORDINATE:
                        cost += calculator.GetToCoordinate(state.agentRows.get(a), state.agentCols.get(a), subGoal.row, subGoal.col);
                        break;
                    case MOVE_BOX_TO_HELP:
                        cost += calculator.MoveBoxToHelp(state.agentRows.get(a), state.agentCols.get(a), subGoal.row, subGoal.col);
                        break;
                    default:
                        cost += Integer.MAX_VALUE;
                }
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

    public Map<Integer, SubGoal> getSubGoals() {
        return this.subGoals;
    }
}


class HeuristicGreedy extends Heuristic {
    public HeuristicGreedy(Integer[][] referenceMap, Map<Integer, SubGoal> subGoals)
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

