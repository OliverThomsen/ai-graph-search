package searchclient;

import java.awt.print.Pageable;
import java.util.Comparator;
import java.util.Map;

public abstract class Heuristic implements Comparator<SuperState> {
    private CostCalculator calculator;
    private Map<Integer, SubGoal> subGoals;

    public Heuristic(Map<Integer, Integer[][]> referenceMaps, Map<Integer, SubGoal> subGoals)
    {

        this.subGoals = subGoals;
        // Here's a chance to pre-process the static parts of the level.
        this.calculator = new CostCalculator(referenceMaps);
    }

    public int h(SuperState s) {
        int cost = 0;
        if(s instanceof AgentState) {
            AgentState state = (AgentState) s;
            SubGoal subGoal = this.subGoals.get(state.agent - '0');
            cost += calculator.goalBoxes(state.boxes, state.goals, subGoal.goalBoxes, state.agent-'0');
//            if (subGoal.obstruction) {
//                cost += calculator.obstructionPenalty(state.row, state.col, subGoal.row, subGoal.col, state.agent-'0');
//            }

            switch (subGoal.type) {
                case GET_TO_BOX:
                    cost += calculator.GetToBox(state.row, state.col, subGoal.row, subGoal.col, state.agent-'0');
                    break;
                case PUSH_BOX_TO_GOAL:
                    cost += calculator.PushBoxToGoal(state.boxes, state.row, state.col, subGoal.row, subGoal.col, subGoal.character, state.agent-'0');
                    break;
                case GET_TO_COORDINATE:
                    cost += calculator.GetToCoordinate(state.row, state.col, subGoal.row, subGoal.col, state.agent-'0');
                    break;
                case MOVE_BOX_TO_HELP:
                    cost += calculator.MoveBoxToHelp(state.boxes, state.goals, state.row, state.col, subGoal.row, subGoal.col, subGoal.character, state.agent-'0');
                    break;
                case MOVE_OUT_OF_THE_WAY:
                    cost += calculator.MoveOutOfTheWay(state.row, state.col, subGoal.row, subGoal.col, state.agent-'0');
                    break;
                case DONE:
                    cost += calculator.GetToCoordinate(state.row, state.col, subGoal.row, subGoal.col, state.agent-'0');
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
                        cost += calculator.GetToBox(state.agentRows.get(a), state.agentCols.get(a), subGoal.row, subGoal.col, a);
                        break;
                    case PUSH_BOX_TO_GOAL:
                        cost += calculator.PushBoxToGoal(state.boxes, state.agentRows.get(a), state.agentCols.get(a), subGoal.row, subGoal.col, subGoal.character, a);
                        break;
                    case GET_TO_COORDINATE:
                        cost += calculator.GetToCoordinate(state.agentRows.get(a), state.agentCols.get(a), subGoal.row, subGoal.col, a);
                        break;
                    case MOVE_BOX_TO_HELP:
                        cost += calculator.MoveBoxToHelp(state.boxes, state.goals, state.agentRows.get(a), state.agentCols.get(a), subGoal.row, subGoal.col, subGoal.character, a);
                        break;
                    case MOVE_OUT_OF_THE_WAY:
                        cost += calculator.MoveOutOfTheWay(state.agentRows.get(a), state.agentCols.get(a), subGoal.row, subGoal.col, a);
                        break;
                    case DONE:
                        cost += calculator.GetToCoordinate(state.agentRows.get(a), state.agentCols.get(a), subGoal.row, subGoal.col, a);
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
    public HeuristicGreedy(Map<Integer, Integer[][]> referenceMaps, Map<Integer, SubGoal> subGoals)
    {
        super(referenceMaps, subGoals);
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

