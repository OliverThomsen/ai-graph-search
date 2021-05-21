package searchclient;

public class Conflict {
    private Integer conflictAgent;
    private int coordinatesOfConflictRow;
    private int coordinateOfConflictCol;
    private boolean isStationary;
    private char conflictChar;
    private Action conflictAction;

    public Conflict(Integer conflictAgent, int coordinatesOfConflictRow, int coordinateOfConflictCol, boolean isStationary, char conflictChar, Action conflictAction){
        this.conflictAgent = conflictAgent;
        this.coordinatesOfConflictRow = coordinatesOfConflictRow;
        this.coordinateOfConflictCol = coordinateOfConflictCol;
        this.isStationary = isStationary;
        this.conflictChar = conflictChar;
        this.conflictAction = conflictAction;

    }

    public int[] getCoordinatesOfConflict() {
        int[] coordinatesOfConflict = new int[2];
        coordinatesOfConflict[0] = coordinatesOfConflictRow;
        coordinatesOfConflict[1] = coordinateOfConflictCol;
        return coordinatesOfConflict;
    }

    public Integer getConflictAgent() {
        return conflictAgent;
    }

    public boolean isStationary() {
        return isStationary;
    }

    public char getConflictChar() {
        return conflictChar;
    }
    public Action getConflictAction() {return conflictAction;}
}
