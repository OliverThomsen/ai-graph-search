package searchclient;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CostCalculator {
    private Integer[][] referenceMap;
    private Map<Character,Integer[]> goalCoordinates;

    public CostCalculator(Integer[][] referenceMap){
        this.referenceMap = referenceMap;
    }


    public int GetToBox(int agentRow, int agentCol, int goalRow, int goalCol){
        int referenceLengthfromgoal = Math.abs(referenceMap[agentRow][agentCol]-referenceMap[goalRow][goalCol]);
        int rowdiff = Math.abs(agentRow - goalRow);
        int coldiff = Math.abs(agentCol - goalCol);
        int manhLength = rowdiff + coldiff;
        int maxLengthfromgoal = Math.max(referenceLengthfromgoal,manhLength);

        return maxLengthfromgoal;


    }

    public int PushBoxToGoal(char[][] boxes,int agentRow, int agentCol, int goalRow, int goalCol, char box){
        // find box
        int cost = 0;
        int boxRow = 0;
        int boxCol= 0;
        for (int i = -1; i <= 1; i++) {
            char foundChar1 = boxes[agentRow + i][agentCol];
            char foundChar2 = boxes[agentRow][agentCol + i];

            if (foundChar1 == box) {
                boxRow = agentRow + i;
                boxCol = agentCol;
                break;
            }
            if (foundChar2 == box) {
                boxRow = agentRow;
                boxCol = agentCol + i;
                break;
            }
            i++;
        }

        //calculate distance from agent to gox
        int agentrowdiff = Math.abs(agentRow - boxRow);
        int agentcoldiff = Math.abs(agentCol - boxCol);
        cost += agentrowdiff + agentcoldiff;

        //calculate distance of box to goal
        int referenceLengthfromgoal = Math.abs(referenceMap[boxRow][boxCol]-referenceMap[goalRow][goalCol]);
        int boxrowdiff = Math.abs(boxRow - goalRow);
        int boxcoldiff = Math.abs(boxCol - goalCol);
        int manhLength = boxrowdiff + boxcoldiff;
        int maxLengthfromgoal = Math.max(referenceLengthfromgoal,manhLength);

        return cost += maxLengthfromgoal;


    }

    public int GetToCoordinate(int agentRow, int agentCol, int goalRow, int goalCol){
        int referenceLengthfromgoal = Math.abs(referenceMap[agentRow][agentCol]-referenceMap[goalRow][goalCol]);
        int rowdiff = Math.abs(agentRow - goalRow);
        int coldiff = Math.abs(agentCol - goalCol);
        int manhLength = rowdiff + coldiff;
        int maxLengthfromgoal = Math.max(referenceLengthfromgoal,manhLength);

        return maxLengthfromgoal;


    }

    public int MoveBoxToHelp(int agentRow, int agentCol, int goalRow, int goalCol){
        int referenceLengthfromgoal = Math.abs(referenceMap[agentRow][agentCol]-referenceMap[goalRow][goalCol]);
        int rowdiff = Math.abs(agentRow - goalRow);
        int coldiff = Math.abs(agentCol - goalCol);
        int manhLength = rowdiff + coldiff;
        int maxLengthfromgoal = Math.max(referenceLengthfromgoal,manhLength);

        return maxLengthfromgoal;


    }


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
