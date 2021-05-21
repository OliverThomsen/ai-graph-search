package searchclient;

import java.util.ArrayList;
import java.util.Map;

public class CostCalculator {
    private Map<Integer, Integer[][]> referenceMaps;

    public CostCalculator(Map<Integer, Integer[][]> referenceMaps){
        this.referenceMaps = referenceMaps;
    }

    public int goalBoxes(char[][] boxes, char[][] goals, ArrayList<Character> goalBoxes, int agent){
        int cost = 0;
        for (char box: goalBoxes) {
            int boxRow = findBox(boxes, box)[0];
            int boxCol = findBox(boxes, box)[1];
            int goalRow = findBox(goals, box)[0];
            int goalCol = findBox(goals, box)[1];
            cost += distanceBetween(boxRow, boxCol, goalRow, goalCol, agent);
        }

        return cost;
    }

    public int GetToBox(int agentRow, int agentCol, int goalRow, int goalCol, int agent){
        return distanceBetween(agentRow, agentCol, goalRow, goalCol, agent);
    }

    public int PushBoxToGoal(char[][] boxes,int agentRow, int agentCol, int goalRow, int goalCol, char box, int agent){
        // find box
        int cost = 0;
        int boxRow = findBox(boxes, box)[0];
        int boxCol = findBox(boxes, box)[1];

        //calculate distance from agent to box
        cost += distanceBetween(agentRow, agentCol, boxRow, boxCol, agent);
        //calculate distance of box to goal
        cost += distanceBetween(boxRow, boxCol, goalRow, goalCol, agent);
        return cost;
    }

    public int GetToCoordinate(int agentRow, int agentCol, int goalRow, int goalCol, int agent){
        return distanceBetween(agentRow, agentCol, goalRow, goalCol, agent);
    }

    public int MoveBoxToHelp(char[][] boxes, char[][] goals, int agentRow, int agentCol, int goalRow, int goalCol, char box, int agent){
        int cost = 0;
        // find box
        int boxRow = findBox(boxes, box)[0];
        int boxCol = findBox(boxes, box)[1];
        //calculate distance from agent to box
        cost += distanceBetween(agentRow, agentCol, boxRow, boxCol, agent);
        //calculate distance of box to goal
        // find box goal
        int goalRow1 = findBox(goals, box)[0];
        int goalCol1 = findBox(goals, box)[1];
        if (goalRow1!=0) {
            cost += distanceBetween(boxRow, boxCol, goalRow1, goalCol1, agent);
        }
        //penalize box for obstructing
        cost += (20-distanceBetween(boxRow, boxCol, goalRow, goalCol, agent));
        return cost;
    }

    public int MoveOutOfTheWay(int agentRow, int agentCol, int goalRow, int goalCol, int agent){
        int cost = 0;
        //penalize agent for obstructing for obstructing
        cost += (20-distanceBetween(agentRow, agentCol, goalRow, goalCol, agent));
        return cost;
    }

//    public int obstructionPenalty(int agentRow, int agentCol, int goalRow, int goalCol, int agent){
//        int cost = 0;
//        //penalize agent for keeping still
//        cost += (2-distanceBetween(agentRow, agentCol, goalRow, goalCol, agent));
//        return cost;
//    }

    public int distanceBetween(int startRow, int startCol, int endRow, int endCol, int agent) {
        int referenceLength = Math.abs(referenceMaps.get(agent)[startRow][startCol] - referenceMaps.get(agent)[endRow][endCol]);
        int rowDiff = Math.abs(startRow - endRow);
        int colDiff = Math.abs(startCol - endCol);
        int manHLength = rowDiff + colDiff;
        return Math.max(referenceLength, manHLength);
    }

    public static int[] findBox(char[][] boxes, char box){
        // find box
        int[] boxCor = new int[2];
        int boxRow = 0;
        int boxCol= 0;
        for (int row = 0; row < boxes.length ; row++) {
            for (int col = 0; col < boxes[0].length; col++) {
                char boxCheck = boxes[row][col];
                if (box == boxCheck) {
                    boxRow = row;
                    boxCol = col;
                }
            }
        }
        boxCor[0] = boxRow;
        boxCor[1] = boxCol;
        return boxCor;
    }

//    public int boxGoalPennalty(ArrayList<Character> goalBoxes, char[][] boxes) {
//        for (int row = 0; row < boxes.length ; row++) {
//            for (int col = 0; col < boxes[0].length; col++) {
//                char box = boxes[row][col];
//                if (goalBoxes.contains(box)) {
//                    distanceBetween()
//                }
//            }
//        }
//
//    }


//    public int Overall_Agent_Cost(State s) {
//            int cost = 0;
//            Map<Character, Integer[]> boxToGoalLength = new HashMap<>(); // Shortest Manhattan length to goal, One entry for each box type A,B,C...
//
//            // Loop through all box matrix to find each box
//            for (int row = 1; row < s.boxes.length - 1; row++) {
//                for (int col = 1; col < s.boxes[row].length - 1; col++) {
//                    char box = s.boxes[row][col];
//
//                    // Check if field contains box
//                    if ('A' <= box && box <= 'Z') {
//                        // Get goal coordinates for the box
//                        Integer[] goalcoor = goalCoordinates.get(box);
//                        // Find box Manhattan length to goal
//                        int referenceLengthfromgoal = Math.abs(referenceMap[row][col] - referenceMap[goalcoor[0]][goalcoor[1]]);
//                        int rowdiff = Math.abs(row - goalcoor[0]);
//                        int coldiff = Math.abs(col - goalcoor[1]);
//                        int manhLength = rowdiff + coldiff;
//                        int maxLengthfromgoal = Math.max(referenceLengthfromgoal, manhLength);
//                        //int maxLengthfromgoal = manhLength;
//
//                        // Save the box's manhattan length in the map
//                        if (boxToGoalLength.containsKey(box)) {
//                            Integer[] boxvalues = boxToGoalLength.get(box);
//                            int prevmanhlenght = boxvalues[2];
//                            // overwrite length if smaller than previous length
//                            if (prevmanhlenght > maxLengthfromgoal) {
//                                boxvalues[0] = row;
//                                boxvalues[1] = col;
//                                boxvalues[2] = maxLengthfromgoal;
//                                boxToGoalLength.replace(box, boxvalues);
//                            }
//                        }
//                        // Always Save the box's manhattan length in the map if first time
//                        else {
//                            Integer[] boxvalues = new Integer[3];
//                            boxvalues[0] = row;
//                            boxvalues[1] = col;
//                            boxvalues[2] = maxLengthfromgoal;
//                            boxToGoalLength.put(box, boxvalues);
//                        }
//                    }
//                }
//            }
//
//            // Total distance of boxes to goal
//            Iterator it = boxToGoalLength.entrySet().iterator();
//            while (it.hasNext()) {
//                Map.Entry pair = (Map.Entry) it.next();
//                Integer[] values = (Integer[]) pair.getValue();
//                cost += values[2];
//                if (values[2] == 0) {
//                    it.remove();
//                }
//            }
//
//            // Loop over each agent
//            for (int row = 0; row < s.agentRows.length; row++) {
//                char agent = Character.forDigit(row, 10);
//                int agentcost = 0;
//                // Agent distance to own goal
//                if (goalCoordinates.containsKey(agent)) {
//                    Integer[] goalcoor = goalCoordinates.get(agent);
//                    int rowdiff = Math.abs(s.agentRows[agent] - goalcoor[0]);
//                    int coldiff = Math.abs(s.agentCols[agent] - goalcoor[1]);
//                    int referenceLengthfromgoal = Math.abs(referenceMap[s.agentRows[agent]][s.agentCols[agent]] - referenceMap[goalcoor[0]][goalcoor[1]]);
//                    int manhLength = rowdiff + coldiff;
//                    int maxLengthfromgoal = Math.max(referenceLengthfromgoal, manhLength);
//                    cost += maxLengthfromgoal;
//                }
//
//                // Agent distance the box closest to goal
//                Iterator it2 = boxToGoalLength.entrySet().iterator();
//                while (it2.hasNext()) {
//                    Map.Entry pair = (Map.Entry) it2.next();
//                    Integer[] values = (Integer[]) pair.getValue();
//                    int rowdiff = Math.abs(s.agentRows[agent] - values[0]);
//                    int coldiff = Math.abs(s.agentCols[agent] - values[1]);
//                    Integer agentReference = referenceMap[s.agentRows[agent]][s.agentCols[agent]];
//                    Integer boxReference = referenceMap[values[0]][values[1]];
//                    if (agentReference == null) {
//                        agentReference = 1;
//                        //System.err.println(s.agentRows[row] + " " + onelength);
//                    }
//                    if (boxReference == null) {
//                        boxReference = 1;
//                        //System.err.println(values[0] +" " + values[1] + " " + another );
//                    }
//                    int manhLength = rowdiff + coldiff;
//                    Integer referenceLengthfromgoal = Math.abs(agentReference - boxReference);
//                    int maxLengthfromgoal = Math.max(referenceLengthfromgoal, manhLength);
//                    cost += (maxLengthfromgoal / 2);
//                }
//
//            }
//            return cost;
//        }
    }
